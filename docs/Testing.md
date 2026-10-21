---
  layout: default.md
  title: "Testing guide"
  pageNav: 3
---

# Testing guide

<!-- * Table of Contents -->
<page-nav-print />

<!-- -------------------------------------------------------------------------------------------------------------------- -->

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

JavaFX UI tests load production controls and FXML files. Linux environments without a graphical desktop need both
a virtual display and a window manager: Xvfb provides the display, while Openbox handles the native minimize and
restore requests exercised by `HelpWindowIntegrationTest`. Xvfb alone cannot verify those window transitions.

On Ubuntu, install the test tools with `sudo apt-get install --no-install-recommends xvfb xauth openbox`, then run:

```bash
xvfb-run --auto-servernum sh -eu -c '
  openbox --sm-disable &
  window_manager_pid=$!
  trap "kill $window_manager_pid 2>/dev/null || true" EXIT
  ./gradlew clean test
'
```

The Linux CI job uses this setup with `./gradlew check coverage`. Openbox is a test-environment dependency;
users running Doc on a normal desktop do not need to install it.

<box type="info" seamless>

**Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</box>

### Assertions

Use JDK 25 for the project SDK and Gradle JVM. Java assertions help detect internal programming errors;
user-input and saved-data validation must still work when assertions are disabled.

* **Gradle launches:** The `run` task enables assertions in `build.gradle`. The Gradle `test` task also enables
  assertions. These settings apply when launching those tasks from a terminal or through IntelliJ's Gradle runner.
* **Direct IntelliJ application launches:** Open **Run → Edit Configurations**, select the application configuration
  for `seedu.address.Main`, then choose **Modify options → Add VM options** and add `-ea`. Apply the change.
  Repeat for each direct application configuration used during development; do not assume a Gradle setting applies.
* **Direct IntelliJ JUnit launches:** Check the selected JUnit configuration's VM options and enable `-ea` when
  testing internal assertions. JUnit assertions such as `assertEquals` are separate from Java's `assert` keyword.

See the [SE-EDU IntelliJ assertion guide](https://se-education.org/guides/tutorials/intellijUsefulSettings.html#enabling-assertions)
for the IDE setup steps. Keep machine-specific run configurations local.

### Visit record verification

The [Developer Guide's manual-testing appendix](DeveloperGuide.md#appendix-instructions-for-manual-testing) contains
repeatable cases for loading previous dates, advancing and rolling back visits, editing, conflicts, viewing
empty or long records, and checking calendar-day labels.
Use disposable data and choose past and future datetimes relative to the actual execution time. Recheck displayed
indices after successful mutations, which restore the full list in stored order.

Automated visit-record tests use injected fixed clocks for exact equality, sub-minute boundaries, time zones,
repeated advances, and calendar-label changes when details are rendered across midnight.
Do not change the OS clock or wait for an appointment to pass to test those boundaries manually.
Run the repository wrapper from the project root:

| Purpose | Windows PowerShell | macOS/Linux |
| --- | --- | --- |
| Java tests, Checkstyle, and coverage reports | `.\gradlew.bat check coverage` | `./gradlew check coverage` |
| Interactive command and details-panel checks | `.\gradlew.bat run` | `./gradlew run` |

Review the test results and coverage reports under `build/reports/`. An automated pass does not replace manual
checks of wrapping and scrolling at the window sizes and display scaling being supported.

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`
