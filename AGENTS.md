# Project context

Doc is a CS2103/T team project based on AddressBook-Level3 (AB3): a JavaFX
desktop application for house-call doctors to manage patients and plan visits.
Users enter CLI commands and view results in the GUI. See [README.md](README.md)
for the product scope and [DeveloperGuide.md](docs/DeveloperGuide.md) for the
architecture.

Keep the existing `seedu.address` package root, architecture, and naming when
extending the application.

## Repository layout

- `src/main/java/seedu/address/`: `commons`, `logic`, `model`, `storage`, and `ui`.
- `src/main/resources/`: JavaFX FXML, CSS, and other application resources.
- `src/test/java/` and `src/test/data/`: automated tests, helpers, and fixtures.
- `docs/`: MarkBind user, developer, setup, testing, and DevOps guides.
- `config/checkstyle/`: the project's automated Java style checks.
- `.codex/skills/`: shared workflows and conventions for coding agents.

# Guidance for working with the team

Assume a CS2103/T student with basic Java and OOP knowledge unless told otherwise.
Adapt to the user's stated experience and role; teammates may use different IDEs.

- Briefly explain significant changes, design rationale, and unfamiliar Git
  commands. Choose the simplest sufficient design; discuss alternatives when
  they affect a meaningful decision.
- Make code self-explanatory; follow the Java skill's Javadoc requirements and
  exceptions, and comment on purpose and non-obvious rationale.
- Inspect affected code and documentation first. Preserve behavior and public
  APIs during refactoring unless the task authorizes changing them.
- Keep changes focused and preserve unrelated work already in the repository.

# Required skills

Use each applicable skill without waiting for the user to name it, unless the
user explicitly waives it for the current task. Announce it and read its full
`SKILL.md` before the corresponding work, even if the client does not list it.
Read once per task unless the instructions change; apply it throughout editing.

- [seedu-code-quality](.codex/skills/seedu-code-quality/SKILL.md): writing or
  reviewing project code, including tests and support code.
- [seedu-java-coding-standard](.codex/skills/seedu-java-coding-standard/SKILL.md):
  writing or reviewing Java, together with the code-quality skill.
- [seedu-git-standard](.codex/skills/seedu-git-standard/SKILL.md): drafting or
  reviewing commit messages and branch names, creating a branch, or preparing
  an authorized commit or amendment.
- [present-changes-visually](.codex/skills/present-changes-visually/SKILL.md):
  run its generator after final edits and checks, before handing off changes
  to application code, tests, support scripts, or UI resources; also use it for
  requested visual comparisons. Follow its defaults and user-specified scope,
  verify the output, and provide the link and changed-file count. Regenerate
  after further relevant edits; open the page only when requested.

Pure documentation and agent-instruction changes are exempt from coding skills
and visual-diff generation unless requested; executable code and code examples
still require applicable code review. Read-only work needs no visual diff unless
requested, but code reviews still use the coding skills.

Keep detailed conventions in the skills and respect their requirements,
recommendations, review triggers, and permitted exceptions. Checkstyle covers
only part of compliance; do not weaken checks or add suppressions to conceal
violations.

# Java and build commands

Use JDK 25 for the application, IDE project SDK, and Gradle JVM, matching the
build and CI. Run the repository's Gradle wrapper from the repository root.

| Purpose | Windows PowerShell | macOS/Linux |
| --- | --- | --- |
| Run the application | `.\gradlew.bat run` | `./gradlew run` |
| Run automated tests | `.\gradlew.bat test` | `./gradlew test` |
| Check main and test Java style | `.\gradlew.bat checkstyleMain checkstyleTest` | `./gradlew checkstyleMain checkstyleTest` |
| Run the checks and coverage used in CI | `.\gradlew.bat check coverage` | `./gradlew check coverage` |
| Build the distributable JAR | `.\gradlew.bat shadowJar` | `./gradlew shadowJar` |

See [SettingUp.md](docs/SettingUp.md), [Testing.md](docs/Testing.md), and
[DevOps.md](docs/DevOps.md) for setup, test types, and build details.

# Verification and handoff

- For Java changes, review affected tests, add or update focused tests for changed
  behavior and regressions, and run appropriate tests and Checkstyle. Use
  `check coverage` for the full Java CI checks; inspect JaCoCo when useful and
  claim coverage only with supporting evidence.
- Manually verify affected command/GUI flows using the Developer Guide's
  manual-testing appendix as a starting point. Report cases, results, and limits.
- Update relevant guides when behavior, commands, architecture, or setup changes.
  For documentation-only edits, review accuracy, links, Markdown/MarkBind
  structure, and the diff; Java checks need not run without code/build impact.
- Review the final diff and surrounding code against applicable skills. Repeat
  affected reviews and checks after further relevant edits. A visual diff
  complements verification; identify included pre-existing work without claiming
  to have authored or verified it during this task.
- At handoff, report skills actually applied, checks actually run, results,
  unresolved violations, justified exceptions, explicit waivers, and applicable
  exemptions. Reading a skill alone does not prove its workflow ran. Report any
  unreadable required skill or blocked workflow and the remaining work; never
  silently skip it or claim unperformed verification succeeded.

# Git

- Follow the Git skill for branch names, messages, and diff review; keep unrelated
  changes out of commits and explain what and why for non-trivial changes.
- Do not commit, amend a commit, or push unless the user explicitly authorizes
  that action.
- When creating an authorized tag, use a lightweight tag unless the user asks
  for an annotated tag.
