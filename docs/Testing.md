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

`CommandBoxTest`, `HelpWindowTest`, `MainWindowTest`, `PatientDetailsPanelTest`, `UiManagerTest`, and `MainAppTest`
load production JavaFX controls and FXML files.
They share the toolkit through `JavaFxExtension`, which shuts it down after the test run, rather than after each
test class. Browser actions use an injected callback; automated tests do not open a real browser or need Internet
access. Browser-wiring tests activate the help link and check the exact requested guide URL.
Default-browser constructor tests only show the windows; launch failures use an injected throwing callback,
without changing the JVM's cached AWT headless state. Check native browser integration manually with the packaged
JAR as described in the [Developer Guide](DeveloperGuide.md#accessing-the-user-guide).
Linux environments without a graphical display
must provide a virtual display, for example by running
`xvfb-run --auto-servernum sh -c 'openbox --sm-disable & ./gradlew clean test'` after installing Openbox.
The window manager supplies native minimize and restore events that Xvfb alone does not provide.
The restoration test waits for and asserts the minimized state before invoking help, then waits for restoration
on the JUnit thread so JavaFX can process those events.
Clipboard assertions restore only contents JavaFX can read, omitting advertised formats with null contents.
The Linux CI job uses the same display and window-manager setup.

<box type="info" seamless>

**Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</box>

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`
