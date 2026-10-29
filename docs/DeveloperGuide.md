---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# Doc Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).
Before relying on internal assertions, follow the [assertion setup instructions](Testing.md#assertions).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-T09-4/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-T09-4/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact when the user issues `delete 1` and then confirms
the deletion with `y`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T09-4/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T09-4/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-T09-4/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

`CommandBox` owns a session-scoped `CommandHistory`. It records ordinary command submissions and uses it to replace
the command box text when the user presses the Up or Down arrow key.

`MainWindow` installs a scene-level key-event filter for Tab navigation. It treats the command box, result display,
patient list, and patient-details `ScrollPane` as four focus sections, routing both Tab directions explicitly so that
the empty details panel remains reachable and descendants such as details labels or scrollbar controls do not become
additional stops. The currently focused section receives a light-gray, 2-pixel outline; every section reserves the same
border space even when unfocused. The menu bar is not part of this cycle. The details panel handles its six scrolling
keys locally, so command-history and patient-list arrow-key behavior remains scoped to those controls. The patient list
scrolls only as needed to keep the selected patient card fully visible.

`MainWindow` manages a separate `HelpWindow` for offline command discovery and access to the full User Guide.
See [Help window](#help-window) for its layout, lifecycle, and external-action boundaries.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-T09-4/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component when `execute("delete 1")`
requests confirmation and `execute("y")` confirms it.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which dispatches it to the parser for that command. Parser instances may be reused or created on demand depending on their state and dependencies.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. A command that needs more input returns a `CommandResult` containing a `CommandInputRequest`. `LogicManager`
   stores at most one pending request and sends the next command-line input directly to it without parsing that input
   as a command.
1. The command can communicate with the `Model` when it is executed or when requested input is handled (e.g. to
   delete a person after confirmation).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of command execution or input handling is encapsulated as a `CommandResult` object which is returned
   from `Logic`. The result also tells `LogicManager` whether it should save the patient data.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, `AddressBookParser` creates or reuses the appropriate `XYZCommandParser`
  (`XYZ` is a placeholder for the command name, e.g., `AddCommandParser`). That parser creates an `XYZCommand`
  object (e.g., `AddCommand`), which `AddressBookParser` returns as a `Command`.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

`AddressBookParser` creates and retains one `NextCommandParser` when it is constructed, supplying a configurable
`Clock`. Each `next` command uses that parser and receives the same clock. Other parsers, such as
`PrevCommandParser`, are created on demand for each command. Reusing the next-command parser retains the clock
dependency, not a startup timestamp: eligibility checks read the clock during command execution.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T09-4/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores Doc's patient records i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T09-4/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both patient data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

When the patient data file does not exist, `MainApp` initializes the model with the seven records returned by
`SampleDataUtil#getSampleAddressBook`. The sample patients are George Miller, Priya Nair, Aisha Rahman, Marcus Lim,
Mei Lin Tan, Daniel Wong, and Noor Aziz. If an existing data file can be loaded successfully, including one with no
records, its contents take precedence over these samples. If an existing data file cannot be loaded—for example,
because it is malformed or contains conflicting records—`MainApp` starts with an empty address book.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Help window

Help is an offline command directory with short descriptions and syntax templates for all supported commands,
with access to the full User Guide for validation rules and examples.
See [Viewing help](UserGuide.md#viewing-help-help) for user instructions.

`MainWindow` retains one `HelpWindow` and reuses it for command, F1, and menu requests. The window is non-modal,
so the main window remains usable. Closing Help returns focus to `CommandBox`, which restores the saved caret
and selection. F1 and the menu call the UI directly; unlike typed `help`, they do not answer a pending input request.

#### Layout and content

`HelpWindow.fxml` places the command groups in one column inside a `ScrollPane`, in the centre of a `BorderPane`.
The groups are **Manage patients and visits** (`add`, `edit`, `next`, `prev`, `delete`, `clear`),
**Find and view patients** (`list`, `find`, `view`, `sort`, `agenda`), and **Application controls** (`help`, `exit`).
Each command entry contains its description, a syntax template, and any optional-field continuation row. The
default window may require vertical scrolling as the templates make the directory longer. A fixed footer holds
the URL, buttons, and feedback so these controls remain accessible while the command list scrolls.
`HelpWindow.css` supplies the presentation styles.

The complete user-facing syntax reference is maintained in the [User Guide command summary](UserGuide.md#command-summary).
This section focuses on the Help window's implementation boundary instead of duplicating every command template. The
`edit` and `find` descriptions state that at least one field is required. The legend is
`UPPERCASE: replace with your values · [ ]: optional · ...: repeatable`. `INDEX` uses the displayed patient
list number; date and emergency-contact meanings follow the detailed User Guide.

`HelpWindow.show()` focuses the command list, which is a Tab stop so users can return from the guide buttons.
A muted keyboard hint and the notation legend `UPPERCASE: replace with your values · [ ]: optional · ...: repeatable`
introduce scrolling and the syntax placeholders. They belong to the scrolling content, not the fixed footer, to
preserve command space in small windows.

Static labels keep the directory independent of parser implementation; the FXML entries must still be maintained
when command syntax changes. When `AddressBookParser` gains, loses, or renames a command, or when a command's
syntax changes, update its FXML entry, the expected inventory and templates in `HelpWindowTest`, and the command
summary in the User Guide. That test is the executable inventory check: it verifies command order, descriptions,
templates, notation, focus, wrapping, scrolling, final-command access, and minimum-size footer space with feedback
shown.

#### External guide actions

`MainApp` passes `HostServices.showDocument` through `UiManager` and `MainWindow` as a browser-opening action.
`HelpWindow` also accepts a replaceable clipboard action, so tests can simulate failures without opening a browser
or changing the system clipboard. For example, when copying is rejected, **Copy URL** shows failure feedback but
**Open User Guide** remains available.

Browser feedback confirms only a launch request, not page loading; no network preflight is performed.
Clipboard success requires `Clipboard.setContent` to return `true`. Detectable failures are logged and shown
without hiding the URL or disabling the controls. `HelpWindowActionsTest` checks failure feedback, fallback actions,
and retries. The [manual checks](#help-window-lifecycle) cover native window behaviour and real browser/clipboard integration.

### Optional emergency contacts

`Person` stores an immutable `EmergencyContact` value. Its empty string represents an absent contact, so
existing constructors and records remain compatible. Nonempty values contain a phone number, optionally preceded
by a name and comma, validated using the existing `Name` and `Phone` rules. Contact details do not affect identity.

`AddCommandParser` and `EditCommandParser` recognize `ec/[NAME, ]PHONE_NUMBER` and reject incomplete, invalid,
or repeated contact fields before execution. `add` rejects an empty `ec/`; `edit` uses it to clear the contact.
`EditPersonDescriptor` distinguishes an omitted contact from a replacement or an explicitly empty contact.
`EditCommand` and `NextCommand` preserve contacts when updating unrelated fields.
Patients can retain both an emergency contact and previous visit dates. Contact-only edits preserve the visit
record, and scheduling the next visit appends the current datetime without clearing the contact.

`JsonAdaptedPerson` saves `PHONE_NUMBER` or the canonical `NAME, PHONE_NUMBER` string. Missing or null contact fields
from older records map to an absent contact. Invalid nonempty stored contacts cause a data-loading error.
`PatientDetailsPanel` shows `Emergency Contact: PHONE_NUMBER (NAME)` immediately below the patient's phone
number, omits the parentheses when the name is absent, and hides the row for absent contacts.

#### Representation and validation

The model uses an empty `EmergencyContact` value rather than a null field. This preserves the non-null invariant
of `Person` and allows callers of the existing constructor to create patients without contacts.
`ParserUtil#parseEmergencyContact` rejects blank input for additions and validates nonempty replacement contacts.
For edits, `EditCommandParser` maps an explicitly empty or whitespace-only `ec/` to `EmergencyContact("")`.
An omitted prefix leaves the descriptor field null, preserving the existing contact. An empty contact object
counts as a supplied field and clears the saved value, even when no contact was previously recorded.

Validation accepts a phone number alone or exactly two comma-separated details. A supplied name must satisfy
`Name#isValidName`, and the required phone must satisfy `Phone#isValidPhone`. Blank names beside a comma are rejected;
omit both the name and comma for a phone-only contact. The constructor trims whitespace and stores `PHONE_NUMBER`
or `NAME, PHONE_NUMBER`. `EmergencyContact#toDisplayString` produces `PHONE_NUMBER` or `PHONE_NUMBER (NAME)` for the UI.
`Person#equals` and `Person#hashCode` include the contact; `Person#isSamePerson` continues to use only the patient's
normalized name and phone. Sharing an emergency contact does not make two patients duplicates.

#### Automated verification

* `EmergencyContactTest` covers valid, absent, malformed, and null values, formatting, equality, and patient identity.
* `EmergencyContactIntegrationTest` exercises parsing, execution, viewing, and storage through `LogicManager`.
  It checks adding, replacing, and clearing contacts, field order, whitespace, duplicate prefixes, invalid indices,
  filtered-list selection, and preservation during unrelated commands. Rejected inputs must leave both the model
  and saved file unchanged.
* `JsonAdaptedPersonTest` checks missing contact fields and invalid stored contact details.
* `NextCommandTest` includes a patient with a contact to check preservation when scheduling the next visit.
* `PatientDetailsPanelTest` checks the exact display text, its position immediately after the phone label, wrapping,
  and hiding the row when switching to a patient without a contact.

#### Testing a newly built JAR

Use JDK 25 and run `./gradlew shadowJar` from the repository root (`.\gradlew.bat shadowJar` on Windows).
Close any previously running instance before launching `build/libs/doc.jar`. Building or replacing the JAR does
not reload classes in an existing application process. An `edit` usage message without
`[ec/[NAME, ]PHONE_NUMBER]` indicates that the running build does not include optional contact-name support.
Use a separate folder with disposable data for manual testing; the application uses paths relative to its working
directory for patient data and preferences.

### Visit record model

A visit record contains the patient's current datetime and the previous datetimes retained by `next`.
The current datetime is always required, even when no further visit is planned. Retained datetimes do not verify
attendance or completion, and notes remain patient-level. See the [User Guide](UserGuide.md#visit-records) for the
user-facing distinction between advancing and rescheduling.

Each immutable `Person` has one required current `VisitDate` and a non-null `List<VisitDate>` named
`previousVisitDates`. The previous dates are strictly increasing, oldest first, and each must be strictly earlier
than the current date. Comparisons use parsed `LocalDateTime` values, including the time: leading-zero variants
of the same datetime are equal, while different times on one day are allowed. These invariants do not depend on
the clock, so the passage of time does not invalidate a record.

The constructor takes an immutable defensive copy and rejects null entries or invalid chronology rather than
sorting or repairing the input. The constructor without previous dates creates an empty list. Equality and hashing
include previous dates; patient identity still uses the normalized name and phone number.

Editing a patient or copying it with `PersonBuilder` preserves previous dates. The command-specific transitions
are described under [Scheduling the next visit](#scheduling-the-next-visit) and
[Restoring the previous visit](#restoring-the-previous-visit).

`JsonAdaptedPerson` writes `previousVisitDates` as an ordered array of visit-date strings. Missing or JSON-null
arrays load as empty lists for compatibility with older files; `visitDate` is still required. Non-arrays, null or
malformed entries, invalid chronology, and duplicates within a record follow the existing invalid-data path.
No entries are sorted, deduplicated, or dropped to repair an invalid file.

#### Displaying visit records

`PatientDetailsPanel` shows `Previous visit dates:` below the current visit and above the note, with one datetime
per line or `No previous visit dates` for an empty record. It reverses the dates only for display, placing recent
visits first while preserving oldest-first storage. The existing `ScrollPane` accommodates long records without
imposing a limit on the number of retained dates.

The details `ScrollPane` resets its vertical value immediately when a record is rendered and once more after JavaFX
lays out the updated content. This handles records of different lengths and repeated views of the same patient. Each
main-window section reserves a fixed-width border whose color changes only for the focused section, keeping the panel
geometry stable while making the focus stop visible.

The current datetime includes a calendar-day label in parentheses: `Past`, `Today`, or `Upcoming`.
`PatientDetailsPanel` uses an injected `Clock` (the system's local clock in production) and reads its current
`LocalDate` once per `setPerson` call. It compares calendar dates, so both earlier and later times today receive
`Today`. This display-only label does not affect stored data, `next` eligibility, or the sort cutoff. Re-rendering
details recomputes it; there is no automatic midnight refresh. Fixed-clock tests cover day and time-zone boundaries
without changing the system clock. See [Calendar labels for current visits](#calendar-labels-for-current-visits)
for manual checks.

**Visit-record roadmap:** `Person` owns the current and immutable previous-date state; `NextCommand` appends the old
current datetime before replacing it; `PrevCommand` pops the latest previous datetime and discards the old current one;
`LogicManager` saves successful mutations and the UI renders the resulting record. The sections below follow this same
model → command → persistence and display path.

### Duplicate patient identity, visit dates, and name confirmation

Doc normalizes names only for duplicate detection. Normalization trims surrounding whitespace, replaces each sequence
of spaces with one space, and converts letters to lowercase using `Locale.ROOT`. The accepted `Name#fullName` value is
retained for display and storage, and `Name#equals` remains an exact value comparison.

Doc distinguishes a **true duplicate** from a **name duplicate**:

* A true duplicate has the same normalized name and exact phone number as an existing patient.
  `Person#isSamePerson` implements this identity rule. `UniquePersonList` rejects true duplicates, so the same
  invariant applies to commands, model replacement, and data loaded from JSON. A true duplicate is rejected without
  offering confirmation.
* A name duplicate has the same normalized name but a different phone number. `Person#hasSameName` identifies this
  weaker match. `UniquePersonList` permits name duplicates, but `add` and an `edit` that changes a patient's name
  require confirmation before creating one.

Patients with different names but the same phone number are neither true duplicates nor name duplicates, so they are
accepted without confirmation. This preserves phone numbers shared by a household or clinic while using name and
phone together to identify a patient record.

`Model#hasPerson` exposes the true-duplicate check, while `Model#hasPersonWithSameName` exposes the weaker warning
check. `Person#isSamePerson` compares phones with `Phone#isSameNumber`, which ignores everything except digits, so a
number re-entered with spaces or dashes is still recognized as the same patient. `Phone#equals` stays an exact
comparison, so changing only a number's formatting is still a real edit.

Visit dates and times form a separate invariant across all current and previous dates.
`Person#hasCommonVisitDate` compares the complete records using parsed `VisitDate` equality.
`UniquePersonList#containsSameVisitDate` checks the full stored collection, including patients hidden by a filter.
`add`, `setPerson`, and `setPersons` throw `DuplicateVisitDateException` rather than allowing two records to use
the same value. This covers command mutations,
model replacement, sample data, and direct model use. `JsonSerializableAddressBook` performs the same check while
loading and reports an invalid data file with a visit-date-specific message.

`Model#hasPersonWithSameVisitDate` exposes the scheduling check. How `add` applies these rules, and when it asks
for confirmation, is described in
[Adding a patient: validation and confirmation](#adding-a-patient-validation-and-confirmation).

`EditCommand` captures the selected patient without changing the displayed list. It similarly rejects a true
duplicate or a datetime shared with another patient's current or previous dates before requesting confirmation,
previewing the matching identity or visit-date record. Replacement checks compare the complete candidate while
excluding the original target. This permits unchanged dates and moving a patient's own dates between current and
previous fields. The command asks for confirmation only when the requested edit changes the name to one already in
use; editing other fields of a patient whose name is already shared does not prompt again. It reports the number of
matching names, previews the first match, captures the original and edited `Person` objects, then verifies on
confirmation that the original record still exists and that the edit still creates neither a true duplicate nor a
visit-date conflict.

`CommandResult` carries the preview together with a name-duplicate confirmation request. `CommandException` carries
the preview for banned identity and visit-date conflicts. `MainWindow` sends either preview to `PatientDetailsPanel`,
using the same presentation as `view`, while feedback remains in `ResultDisplay`. A successful add restores the full
patient list, while a successful edit preserves the active filter and sort comparator. A rejected or cancelled
operation leaves the displayed list unchanged. While a confirmation is pending, the command box treats the next
submission as the confirmation response, so displayed indices cannot be used by another command until the doctor
confirms or cancels.

#### Adding a patient: validation and confirmation

`add` checks a new patient in three stages. Errors stop the command, while warnings only ask the doctor to confirm.

1. **Parsing.** `AddCommandParser` requires `n/`, `p/`, `a/`, and `vd/`, rejects text before the first prefix and
   repeated single-valued prefixes, and validates each field through `ParserUtil`. Any `ParseException` is wrapped
   so that every failure starts with `The patient is not added due to:`.
1. **Rejection rules.** `AddCommand#execute` calls `validateNoConflicts`, which uses `DuplicatePersonFinder` to look
   for a true duplicate (same normalized name and phone) and then for a stored patient using the same current or
   previous visit time. Either case throws a `CommandException` that carries the matching patient, so `MainWindow`
   shows that patient in the details panel. These rules are never overridable.
1. **Warnings.** `collectWarnings` gathers every warning that applies: a name duplicate, and a visit day that already
   has `Model.MAX_VISITS_PER_DAY` (7) or more current visits. With no warnings, the patient is added immediately.
   Otherwise, the warnings are joined into **one** prompt ending with `Add NAME anyway?`, so the doctor confirms once
   even when both apply. When there is a name duplicate, the result also previews the first matching patient.

<puml src="diagrams/AddCommandActivityDiagram.puml" alt="How add validates a patient and asks for confirmation" width="600" />

The prompt is a `CommandInputRequest` created with `CommandInputRequest#createConfirmation`, whose confirmed action is
`AddCommand#addPerson`. The sequence diagram below shows a name duplicate on a full day. The first `execute` returns a
result that holds the request and does not save. `LogicManager` keeps the request pending, so the doctor's next input,
`y`, goes to the request instead of the command parser.

<puml src="diagrams/AddConfirmationSequenceDiagram.puml" alt="Interactions when add asks for confirmation and the doctor enters y" />

`addPerson` calls `validateNoConflicts` again before `Model#addPerson`. The proposed `Person` was captured before the
prompt, so this recheck stops a duplicate or a visit-time conflict that appeared while the prompt was open from being
saved. Only the confirmed result asks `LogicManager` to save, so a cancelled addition never writes the data file.

**Aspect: how several warnings are confirmed**

* **Alternative 1 (current choice):** Collect every warning and ask once.
  * Pros: One `y` per addition. The doctor sees every concern together before deciding.
  * Cons: A longer prompt when both warnings apply.
* **Alternative 2:** Ask about each warning in turn, chaining one confirmation request per warning.
  * Pros: Each prompt is short and focused.
  * Cons: A second `y` is easy to give without reading it, and the first `y` can look like it already added the
    patient.

#### Duplicate-detection limitations

Duplicate detection is a heuristic because Doc does not assign a permanent patient identifier:

* Two different patients with the same normalized name and phone number cannot both be stored, such as family members
  who share a name and contact number. A distinguishing name component or different contact number is required.
* A returning patient with both a changed name and changed phone number is not detected as a possible duplicate.
* Normalization does not understand nicknames, reordered names, misspellings, transliterations, or equivalent accented
  forms. It performs no fuzzy or culture-specific matching.
* Phone numbers are compared by their digits only. Equivalent numbers entered with and without a country code are not
  detected as the same number.
* Collapsing whitespace and ignoring case can warn about two distinct names whose intentional difference is only
  capitalization or spacing.

A future patient-ID feature should replace name and phone as identity fields if Doc needs to distinguish these cases
without relying on user confirmation or spelling conventions.

#### Current implementation limitations

The current implementation does not provide an in-app migration or recovery workflow for data that conflicts with the
new duplicate rules:

* A file accepted by an older version can become invalid when two records have the same phone number and names that
  differ only in letter case or repeated spacing. The normalized names now make those records true duplicates.
* A file accepted by an older version can also become invalid when two records use the same visit date and time.
* For either conflict, Doc reports the loading problem and starts with an empty patient list. The conflicting file is
  not backed up automatically, and a command that triggers saving, such as `list`, can replace it with the empty
  patient list. Users must back up the file and resolve the conflicts manually before migration.

#### Visit-time conflict limitations

The visit-date rule prevents only exact scheduling collisions:

* Equality is checked to the minute using the parsed `LocalDateTime`. The rule does not model visit duration, travel
  time, preparation time, or partial overlaps.
* Doc has one schedule and does not model multiple doctors, doctor availability, time zones, or recurring visits.
* All current and previous datetimes are unique across patients, including past datetimes. These records do not
  verify attendance or completion. Previous dates do not count toward per-day current-appointment capacity.
* A data file created by an older version may contain duplicate visit dates. The migration and data-preservation risks
  are described under the current implementation limitations above.

### Optional date of birth

`Person` retains an optional `DateOfBirth` alongside blood type, emergency contact, and visit history.
Add and edit accept `dob/`; omitted values preserve existing data, while an empty value clears it during edit.
Storage defaults missing or null `dateOfBirth` fields to an unrecorded value for older files.
Patient cards and details show the recorded date and completed age below the name, before blood type.

### Optional blood type

`Person` stores a `BloodType` enum containing the eight blood types and `UNKNOWN` for unrecorded values.
The existing constructor defaults to `UNKNOWN`. `ParserUtil#parseBloodType` trims whitespace, ignores letter
case, and rejects empty or invalid input. Add and edit recognize `bt/` and reject repeated prefixes;
edit maps an explicitly empty value to `UNKNOWN`. Other edits and visit rescheduling preserve blood type.

`JsonAdaptedPerson` stores the display value (e.g. `A+`) in the `bloodType` property. Missing or null properties
from older files load as `UNKNOWN`; an empty string represents a cleared value. Invalid stored values are rejected.
Both patient cards and the details panel show recorded blood type below the date of birth (or the name when
DOB is absent), hiding and unmanaging the row
when it is unrecorded. Patient identity continues to use name and phone number.

### Optional allergies

`Person` stores immutable free-text `Allergies`. Surrounding whitespace is removed; empty text represents
unrecorded information and is distinct from an explicit `No known allergies` entry. Multiple allergies can be
written in one value, such as `Penicillin; latex`; the field does not interpret, split, or infer clinical information.
`ParserUtil#parseAllergies` rejects blank add values, while edit maps blank `all/` to an empty value. Both parsers
reject repeated prefixes. Unrelated edits, `next`, and `prev` preserve the field along with other patient details.

`JsonAdaptedPerson` stores the `allergies` string and treats missing or null fields in older files as unrecorded.
The right-hand details panel displays recorded allergies after blood type, before alcohol history and smoking
status. Long values wrap within the scrollable panel. Unrecorded values hide the row without implying no allergies.
Add/edit help follows the same field order; patient list cards and patient identity remain unchanged.

### Optional alcohol history

`Person` stores an immutable `AlcoholHistory` alongside blood type. It holds free text with surrounding whitespace
removed; empty text represents unrecorded history and remains distinct from the explicit text `Does not drink`.
`ParserUtil#parseAlcoholHistory` rejects blank add values, while edit maps an explicitly blank `alc/` to an empty
history. Both parsers reject repeated `alc/` prefixes. Unrelated edits, `next`, and `prev` preserve the history.

`JsonAdaptedPerson` saves the `alcoholHistory` string and treats missing or null fields in older files as unrecorded.
The right-hand details panel displays recorded alcohol history immediately after allergies, wraps long text, and
hides the row when unrecorded. Patient list cards and duplicate-patient identity remain unchanged.

### Optional smoking status

`Person` stores a `SmokingStatus` enum with `NEVER`, `FORMER`, `CURRENT`, `UNKNOWN`, and `UNRECORDED`.
`UNKNOWN` represents explicitly recorded uncertainty; only `UNRECORDED` hides the row in the details panel.
`SmokingStatus#fromString` trims surrounding whitespace and normalizes letter case using `Locale.ROOT`.
`ParserUtil#parseSmokingStatus` rejects blank or unsupported recorded values, while edit maps blank `smk/` to
`UNRECORDED`. Both parsers reject repeated `smk/` prefixes. Other edits, `next`, and `prev` preserve smoking status.

`JsonAdaptedPerson` stores the normalized lowercase status, or an empty string for unrecorded values.
Missing or null `smokingStatus` fields in older files load as `UNRECORDED`; unsupported saved values are rejected.
The right-hand details panel displays smoking status immediately after alcohol history. Patient list cards and
patient identity remain unchanged. Add/edit help places `[smk/STATUS]` after `[alc/ALCOHOL_HISTORY]`.

### Editing patient details

`EditCommandParser` accepts optional replacement fields, including `vd/` and `note/`. The displayed format is
`edit INDEX [n/NAME] [p/PHONE] [a/ADDRESS] [vd/DATETIME] [e/EMAIL] [ec/[NAME, ]PHONE_NUMBER] [dob/DATE] [bt/BLOOD_TYPE] [all/ALLERGIES] [alc/ALCOHOL_HISTORY] [smk/STATUS] [note/NOTES] [t/TAG]...`.
It checks index syntax and duplicate single-valued prefixes before parsing field values, then rejects a
descriptor with no supplied fields. When `vd/` is supplied, `ParserUtil#parseVisitDate` reuses the existing strict
`VisitDate` validation
for `d/M/yyyy HHmm`; valid past dates are accepted, while an empty `vd/` is rejected.

`EditPersonDescriptor` stores the supplied visit date and time and note along with the optional replacement fields.
`EditCommand` copies the descriptor, resolves the patient using the displayed list, and replaces the immutable
`Person`. Omitted fields, including the visit date/time and notes, are preserved. An explicitly empty
email, note, emergency contact, or tag field clears its existing value and counts as a supplied field.

Before replacement, `EditCommand` requires the current datetime to remain strictly later than the latest previous
datetime. A valid past datetime is still allowed, and history is preserved unchanged. It permits an unchanged current
date but rejects a value already used by another patient's current or previous dates, including hidden patients.
The conflict check is repeated after confirmation so a slot taken while the prompt was pending cannot be introduced
into the model. Chronology failures produce a command error explaining that the current datetime must be later
than the latest previous visit and showing that previous datetime. This check occurs before constructing the
replacement patient; the `Person` constructor independently enforces the record invariants for all callers.

The descriptor's nullable `Note` distinguishes omission from an explicitly empty `Note("")`.
When `note/` is present, the parser constructs the existing immutable `Note` from the tokenizer's value;
otherwise, the descriptor's note remains null. Notes-only replacement and clearing both succeed without `vd/`.
The descriptor copy preserves this distinction, and `createEditedPerson` uses the supplied note or preserves
the patient's existing note. Supplying the same note again, or clearing an already-empty note, succeeds.

The existing tokenizer uses `String#strip()` on outer whitespace, preserves internal spaces, and recognizes prefixes
at the start of the arguments or after Java whitespace. A recognized prefix inside notes delimits another field;
there is no quoting or escaping for literal prefix text, or extra note validation. Repeated `note/` prefixes are
rejected before field validation, including identical or empty values.
Repeated `t/TAG` prefixes retain their existing replacement and clearing behavior.

Editing replaces the supplied fields without appending visit-history entries or per-visit notes. Saving and UI
behavior follow the [shared visit-change flow](#saving-and-displaying-visit-changes) below.

### Saving and displaying visit changes

`edit`, `next`, and confirmed `prev` validate the proposed change before replacing the patient. A valid `prev` first
returns a non-saving `CommandInputRequest`; only its confirmed callback replaces the captured patient. Successful
commands preserve the active filter and sort comparator, allowing the replacement patient to move or disappear as
those rules are reevaluated, and return feedback with a `CommandResult` that requests saving. `LogicManager` saves
through storage before returning the result to the UI.

These results select the replacement patient for the details panel, refreshing the record and calendar label even if
the patient moves or disappears from the displayed list. `view` can still select any displayed patient without
requesting a save.

Validation errors and cancelled confirmations leave patient data, saved data, and list context unchanged.
Duplicate-related errors and name-confirmation prompts can display an existing patient in the details panel;
other rejected inputs do not change that panel.
If storage reports an I/O or permission failure, `LogicManager` restores the patient data, active filter, and sort
captured before command execution. The command reports the save error, while the data file and displayed model remain
consistent with the last successful save.

### Sorting displayed visits by date

`sort date [START_DATE]` orders the patients in the currently displayed list without changing the active filter.
`SortCommandParser` accepts an optional cutoff date in `d/M/yyyy` format. When it is present, the parser supplies the
date to `SortCommand`; otherwise, the command obtains today's date from its clock when it executes. The command then
creates a comparator that separates visits before the cutoff date from those on or after it.

The activity diagram shows how the cutoff date is selected and the comparison decisions made while the displayed
list is being sorted. The underlying `SortedList` decides how many comparisons are required.

<puml src="diagrams/SortCommandActivityDiagram.puml" alt="How sort date orders displayed visits" width="500" />

When exactly one visit is before the cutoff date, the comparator places the other visit first. Otherwise, it places
the earlier visit date and time first. For example, with 21 October as the cutoff, visits on 22 October, 24 October,
and 20 October are displayed in that order.

`SortCommand` passes the comparator to `Model#sortFilteredPersonList`. `ModelManager` applies it to the `SortedList`
layered over the current `FilteredList`, so the displayed patients are reordered without replacing the stored
patient list. A later operation that resets the filter also clears this display-only ordering.

### Displaying visits as an agenda

`agenda START_DATE [DAYS]` shows current visits in a consecutive range of calendar days. `AgendaCommandParser`
requires a start date and accepts an optional duration from 1 to 365 days. A bare `agenda` command displays the
usage message, while an omitted duration defaults to seven days.
The constructor checks that the final included day is on or before 31 December 2099, the last day a visit date can
have (`VisitDate.MAX_YEAR`), so a range never extends past dates that can hold visits. The parser reports an invalid
range before execution when this bound is exceeded.

`AgendaCommand` constructs a `VisitDateWithinRangePredicate` using an inclusive start and exclusive end. The
exclusive end keeps the predicate independent of visit times: a seven-day agenda starting on 27 October accepts
every time from 27 October through 2 November and rejects every time on 3 November. The command replaces the current
filter with this predicate and applies a chronological comparator to the existing `SortedList`.

<puml src="diagrams/AgendaCommandActivityDiagram.puml" alt="How agenda filters and orders visits" width="500" />

Filtering and sorting remain display-only. The address book retains its stored order, and the command returns a
non-saving result. A later `list` restores the full list in stored order.

### Finding patients by field

`find` filters the displayed patient list by name, phone number, address, tag, visit date, and notes. Each field has
its own prefix, and every keyword must follow one: `n/`, `p/`, `a/`, `t/`, `vd/`, or `note/`. Keywords given for one
field match a patient if **any** of them matches, while different fields must **all** match. For example,
`find n/m t/morning` lists patients with a name word starting with `m` who also have a tag starting with `morning`.

#### Search criteria as predicates

Each field is searched by its own `Predicate<Person>` class in the Model component, and
`MatchesAllCriteriaPredicate` combines them. `FindCommand` stores this single combined predicate, so it does not need
to know which fields were searched.

<puml src="diagrams/FindPredicateClassDiagram.puml" alt="Classes used to represent find search criteria" width="650" />

* `FieldContainsKeywordsPredicate` holds a field's keywords and implements `test` as "any keyword matches".
  It also implements `equals`, `hashCode`, and `toString`, comparing exact classes so that, for example, a name
  predicate and an address predicate with the same keywords are not equal. Each subclass only defines how one keyword
  matches its field:
  * `NameContainsKeywordsPredicate`, `AddressContainsKeywordsPredicate`, and `NoteContainsKeywordsPredicate` match
    the start of any word, ignoring case, using `StringUtil#containsWordStartingWithIgnoreCase`.
  * `PhoneContainsKeywordsPredicate` matches any part of the number, so remembered final digits are enough.
  * `TagContainsKeywordsPredicate` matches the start of any tag, ignoring case.
* `VisitDateOnDaysPredicate` does not extend the keyword base class because it stores parsed `LocalDate` values.
  It matches a patient whose **current** visit falls on any given calendar day, at any time. Previous visit dates
  are history, so they are not searched.
* `MatchesAllCriteriaPredicate` returns true only when every criterion matches, which gives the `AND` behavior
  across fields.

#### Parsing and execution

The sequence diagram below shows `find n/m t/morning`. The filtering loop is conceptual: the model's `FilteredList`
evaluates the predicate for each stored patient whenever the displayed list is recomputed.

<puml src="diagrams/FindSequenceDiagram.puml" alt="Interactions inside the Logic and Model components for find n/m t/morning" />

`FindCommandParser#parse` handles the arguments in these steps:

1. It tokenizes the arguments. `ArgumentTokenizer` recognizes prefixes at the start of the arguments or after Java
   whitespace, so spaces, tabs, and line breaks separate fields consistently. A prefix-looking token separated by
   whitespace still starts a new field because the tokenizer has no quoting or escaping mechanism.
1. It rejects text before the first prefix with `MESSAGE_KEYWORDS_NEED_PREFIX` and
   repeated prefixes with the shared duplicate-prefix error.
1. For each prefix that is present, it splits the value into keywords and creates that field's predicate.
   An empty prefix such as `n/` is rejected. Phone keywords must be digits. Visit dates are parsed strictly with
   `VisitDate#DAY_INPUT_FORMATTER` (`d/M/yyyy`), so impossible dates and dates followed by a time are rejected and the
   error names the invalid value.
1. If no prefix was given, it shows the usage message; otherwise it wraps the predicates in a
   `MatchesAllCriteriaPredicate` and returns a `FindCommand`.

`FindCommand#execute` calls `Model#updateFilteredPersonList`, which also clears any `sort` ordering, and reports the
number of matches. When nothing matches, the message lists every search prefix rather than a name-only hint, so the
advice also suits phone, address, tag, visit date, and note searches.

#### Design considerations

**Aspect: how plain keywords such as `find alex` are handled**

* **Alternative 1 (current choice):** Require a prefix for every keyword and reject plain keywords with an error that
  shows how to search by name and by phone number.
  * Pros: Every keyword visibly belongs to one field, so a search never mixes fields by accident.
  * Cons: Users of the earlier name-only `find` must add `n/`.
* **Alternative 2:** Treat keywords before the first prefix as name keywords.
  * Pros: `find alex` keeps working.
  * Cons: Two ways to search names, and mistakes such as a missing prefix silently become name searches.

**Aspect: how fields are represented**

* **Alternative 1 (current choice):** One predicate class per field, combined by `MatchesAllCriteriaPredicate`.
  * Pros: Adding a field, as `vd/` and `note/` did, needs a new small class and one parser line, without changing
    `FindCommand` or the existing predicates. Each predicate is tested on its own.
  * Cons: More classes than a single predicate holding every field's keywords.
* **Alternative 2:** One predicate that stores a keyword list for each field and checks them all.
  * Pros: Fewer classes.
  * Cons: Every new field changes the same class, and its `test` method grows with a branch per field.

### Scheduling the next visit

`NextCommand` appends the current datetime to the previous-date list and sets the requested datetime as current,
preserving all other patient fields. This separates advancement from `edit`, which corrects the current datetime
without adding history. The [User Guide](UserGuide.md#scheduling-the-next-visit-next) provides syntax and errors.

Eligibility requires `current <= now` and `current < requested`. The requested visit may be in the past, which
supports entering follow-up visits retrospectively without inventing a temporary future appointment. Combined
with the existing record chronology, this preserves `history < old current < requested`. An unchanged datetime
keeps its specific error; an earlier datetime reports that it must be later than the current recorded visit.

The parser passes an injected clock to the command. Each eligibility check reads it once in the local time zone.
Comparisons retain seconds and nanoseconds. An appointment at or before now requires confirmation; a future
appointment does not require the time warning. Fixed clocks make boundary tests deterministic.

Before replacement, the command rejects an unchanged datetime and checks the complete candidate record against
all other stored patients, including hidden patients and excluding the original record. The shared
[visit-date uniqueness rule](#duplicate-patient-identity-visit-dates-and-name-confirmation) applies.

If the requested visit is at or before now, or moves to a different day with `Model.MAX_VISITS_PER_DAY` (7) or more
current visits, the command combines all applicable warnings in one `CommandInputRequest` before mutating.
A later visit on the same day skips only the capacity warning, because it replaces that day's visit.
On confirmation, the command rechecks eligibility using a fresh clock reading, verifies that the original record
is unchanged, and repeats the conflict check before replacing the patient. If a future appointment reaches or passes
now while a capacity prompt is open, it returns a new prompt including the time warning; only a subsequent `y`
applies that operation. Prompts and cancellation do not mutate, save, or refresh the details panel.

The following sequence diagram focuses on command execution, model updates, and saving through `Storage` for `next`
when no confirmation is required. Command parsing and object construction are omitted; see the
[Logic overview](#logic-component) for parser routing and lifecycles. Validation, conflict checking, and the optional
confirmation branch are also omitted.

<puml src="diagrams/NextSequenceDiagram.puml" alt="Successful execution of next when no confirmation is required" width="800" />

A visit-time conflict supplies the conflicting patient through `CommandException` so `MainWindow` displays that
record without changing the displayed list. If no further visit is planned, the current datetime remains stored
after it becomes past; no attendance or completion state is updated.
Manual cases appear under [Testing the next command](#testing-the-next-command).

### Restoring the previous visit

`PrevCommand` discards the current datetime and replaces it with the latest previous datetime, removing the
restored datetime from the previous-date list. There is no redo. It copies all other fields from the selected
patient's latest record, preserving intervening edits.
The [User Guide](UserGuide.md#restoring-the-previous-visit-prev) includes syntax, feedback, and a running example.

There is no clock eligibility check: rollback must allow immediate correction of mistaken advancement, even while
the current or restored datetime is still in the future. Every valid rollback first returns a
`CommandInputRequest` containing the selected patient's current name and phone, the restored and discarded
datetimes, and the confirmation instruction. The initial execution does not mutate, reset, refresh, or save anything.

The shared [follow-up input handling](#follow-up-command-line-input-and-confirmation) trims the response and
compares it to `y` case-insensitively; other responses are consumed as cancellation. The shared
[visit-change persistence flow](#saving-and-displaying-visit-changes) describes when the confirmed mutation is
saved. For this feature, the request captures the immutable selected `Person` and invokes a private rollback helper
only after confirmation. The helper checks the stored person list with full `Person.equals` semantics before creating
the replacement. If an intervening edit or deletion changed the record, it throws the stale-selection error without
changing the replacement record or displayed list. Otherwise it calls `createUpdatedPerson` and replaces the captured
patient while retaining the active filter and sort comparator.

Empty previous dates produce a `CommandException` before the private rollback helper runs. The helper's assertion
documents that internal precondition, while user-input validation remains effective with assertions disabled.
The replacement contains only dates from the original record, so it cannot introduce a new cross-patient datetime
conflict. The model still enforces chronology and uniqueness on replacement.

The following sequence diagram focuses on confirmation and successful execution of `prev` for a patient with visit
history. The first command returns a confirmation request without changing or saving patient data. The second input,
`y`, invokes the rollback on the same captured patient, then saves through `Storage`. Command parsing and object
construction are omitted; see the [Logic overview](#logic-component) for parser routing and lifecycles. Error and
cancellation paths are also omitted. `capturedPatient`
denotes the selected `Person` retained by the request; `updatedPerson` is its replacement after rollback.

<puml src="diagrams/PrevSequenceDiagram.puml" alt="Two-stage successful execution of prev with confirmation and persistence" width="800" />

Manual checks for repeated rollback, subsequent advancement, and persistence appear under
[Testing the prev command](#testing-the-prev-command).

### Follow-up command-line input and confirmation

`CommandInputRequest` lets any command collect an additional command-line input through the existing command box and
result display. It contains the prompt and a response handler. The handler receives the raw next input and the
`Model`, then returns a `CommandResult`. That result can complete the interaction or contain another
`CommandInputRequest`, so future commands can implement multi-step input without adding command-specific state to the
UI or parser.

`LogicManager` stores at most one pending request. While a request is pending, `LogicManager#execute(String)` sends
the submitted text to the request handler instead of `AddressBookParser`. This includes blank input and text that
would otherwise be a valid command. After the handler returns or throws a `CommandException`, the old request is no
longer pending. A handler can continue the interaction by returning a result containing a new request.

`CommandResult` separates displaying feedback from saving patient data. Prompts, cancellations, and the
already-empty `clear` result do not cause a file write. A confirmed mutation returns a result that causes
`LogicManager` to save the updated patient data once.

The shared `CommandInputRequest#createConfirmation` helper trims the response and compares it to `y` without regard
to case. It runs the supplied action only for that response; every other response returns the supplied cancellation
message.

For additions with a name duplicate or a full visit day, and for same-name edits, `AddCommand` and `EditCommand` use
the same confirmation helper as destructive commands. The prompt does not mutate or save patient data. Confirmation
performs the captured mutation and saves once; every other response cancels without saving. The proposed patient is
captured before the prompt, and confirmed actions recheck both the true-duplicate and unique-visit-date invariants
before modifying the model.

For `delete INDEX`, `DeleteCommand` validates the index and captures the exact displayed `Person` before requesting
confirmation. The prompt contains only that person's name because other fields can be absent. Confirmation deletes
that captured object rather than resolving the index again. If the record changed or disappeared while confirmation
was pending, the command reports that the selected person no longer exists and deletes nothing.

For `clear`, `ClearCommand` requests confirmation only when Doc contains patient records. Its prompt uses
`entry` for one record and `entries` otherwise. Confirming clears the patient list; every other response cancels.

### Command history navigation

`CommandHistory` stores each non-empty ordinary submission as an exact string unless it is identical to the newest
history entry. This collapses consecutive exact duplicates while retaining identical submissions separated by another
command. `CommandBox` records the text before calling its command executor, so commands that later fail parsing or
execution remain available. Before recording, `CommandBox` checks whether `Logic` has a pending
`CommandInputRequest`; follow-up responses are excluded from command history because they are input for an existing
command rather than new commands.

The history position starts immediately after the newest entry. On the first Up arrow press, `CommandHistory` saves
the current command box text as a draft and returns the newest command. Further Up presses move towards older
commands and stop at the oldest entry. Down presses move towards newer commands; moving past the newest entry restores
the saved draft. A new ordinary submission resets the position to immediately after the newest entry.

`CommandBox` handles JavaFX key-pressed events for the Up and Down arrow keys, replaces the text, positions the caret
after the recalled text, and consumes the event so the key does not also move the caret. Other keys retain the usual
`TextField` behavior. The history belongs only to the current `CommandBox`, so it is discarded when the application
closes.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current patient-record state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous patient-record state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone patient-record state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial patient-record state, and the `currentStatePointer` pointing to that single state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` to delete the 5th patient in Doc. The `delete` command calls
`Model#commitAddressBook()`, causing the modified patient-record state to be saved in `addressBookStateList`, and the
`currentStatePointer` is shifted to the newly inserted state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new patient. The `add` command also calls
`Model#commitAddressBook()`, causing another modified patient-record state to be saved into `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the patient-record state
will not be saved into `addressBookStateList`.
</box>

Step 4. The user decides that adding the patient was a mistake and executes `undo`. The command calls
`Model#undoAddressBook()`, shifts `currentStatePointer` once to the left, and restores the previous patient-record
state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If `currentStatePointer` is at index 0, pointing to the initial patient-record state, there are no previous
states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, shifts `currentStatePointer` once to the
right, and restores the previously undone patient-record state.

<box type="info" seamless>

**Note:** If `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest patient-record
state, there are no undone states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is
the case. If so, it returns an error instead of attempting the redo.
</box>

Step 5. The user executes `list`. Commands that do not modify patient data, such as `list`, do not call
`Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, `addressBookStateList`
remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since `currentStatePointer` is not at the
end of `addressBookStateList`, all patient-record states after it are purged because redoing `add n/David …` would no
longer be valid. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire patient-record state.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is a house call doctor who treats patients in their homes rather than at a clinic
* sees the same patients repeatedly for routine checkups, and makes 4-10 house visits on a typical working day
* looks after patients who often have multiple ongoing medical conditions
* needs a patient's address, contact number and medical history on hand before and during a visit
* plans each day's visits around patient severity, visit due dates and travel between homes
* prefers desktop apps over other types of applications
* can type fast and prefers typing to mouse interactions
* is reasonably comfortable using CLI apps

**Value proposition**: A house call doctor currently keeps patient contacts, addresses and medical histories across paper notes, phone contacts and memory, which makes planning and preparing for home visits slow and error-prone. Doc centralises all of it in one offline desktop app, and orders the day's visits by patient priority and next visit date, so the doctor can prepare for a visit and plan a route between visits faster than with a typical mouse-driven contact app.

### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …                  | I want to …                                                              | So that I can…                                                                       |
|----------|-------------------------|--------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| `* * *`  | new user                | see the app preloaded with sample patient records                         | try out the commands before entering real patient data                                  |
| `* * *`  | new user                | see usage instructions                                                    | refer to them when I forget how to use the app                                          |
| `* * *`  | new user                | purge all sample data                                                     | start entering my own patient records on a clean list                                   |
| `* * *`  | doctor                  | add a patient record with a name, phone number, address and next visit date, with an optional email and notes about medical conditions | keep everything I need for a house visit in one place |
| `* * *`  | doctor                  | list all my patients                                                      | see my whole caseload at a glance                                                       |
| `* * *`  | doctor                  | find a patient by name                                                    | pull up their details without scrolling through the entire list                         |
| `* * *`  | doctor                  | view a patient's full record including notes about their medical conditions | prepare for a visit before I arrive at their home                                     |
| `* * *`  | doctor                  | edit a patient's details                                                  | keep their record accurate when their contact information, address, next visit date or medical notes change |
| `* * *`  | doctor                  | permanently delete an incorrectly created or duplicate patient record     | ensure erroneous records do not remain in my caseload                                   |
| `* * *`  | doctor                  | retain a previous scheduled datetime when planning a patient's following visit | review the patient's planned visit history                                             |
| `* * *`  | doctor                  | schedule a patient's following visit                                     | plan their follow-up care                                                             |
| `* * *`  | doctor                  | find patients whose next visit date is today                              | be reminded who is due for a visit                                                      |
| `* *`    | doctor                  | restore the latest previous visit datetime                               | correct a visit-date advancement while keeping changes to the patient's other details |
| `* *`    | doctor                  | add or update notes about a patient's medical conditions                  | prepare for future visits                                                               |
| `* *`    | doctor                  | assign a priority level to a patient                                      | tell at a glance which patients need closer attention                                   |
| `* *`    | doctor                  | sort my patients by their next visit date                                 | plan upcoming visits while retaining past schedules for reference                       |
| `* *`    | doctor                  | search patient notes for a medical condition                              | review together all the patients I treat for the same condition                         |
| `* *`    | doctor                  | filter patients by how long ago they were last visited                    | find patients who have gone too long without a checkup                                  |
| `* *`    | doctor                  | see the coming week's visits as a schedule                                | plan my week before it starts                                                           |
| `* *`    | doctor                  | copy a patient's address or phone number in one command                   | paste it into my maps or phone app without retyping it                                  |
| `* *`    | doctor                  | archive a patient who no longer needs regular visits                      | keep my active list short without losing their history                                  |
| `* *`    | doctor                  | view my archived patients                                                 | look up the history of a patient who returns after a long gap                           |
| `* *`    | doctor                  | find patients whose next visit date has passed                            | follow up on overdue visits                                                             |
| `* *`    | doctor                  | find patients due within a specified date range                           | plan visits for the coming days                                                        |
| `* *`    | doctor                  | undo my most recent record-changing command                               | recover quickly from a typing mistake                                                   |
| `* *`    | long-time user          | define my own aliases for the commands I use most                         | enter routine commands with fewer keystrokes                                            |
| `*`      | new user                | import my existing patient records from a file                            | move my caseload into Doc without retyping every record                                 |
| `*`      | doctor                  | export a patient's record to a file                                       | hand it over to a colleague covering my rounds                                          |
| `*`      | doctor                  | lock the app behind a password                                            | keep patient data private if someone else uses my laptop                                |

### Use cases

(For all use cases below, the **System** is `Doc` and the **Actor** is the `doctor`, unless specified otherwise)

**Use case: UC01 - Add a patient**

**MSS**

1.  Doctor requests to add a patient, providing the patient's name, address, phone number, next visit date and time,
    and medical conditions.
2.  Doc adds the patient to the caseload and shows the newly added patient record.

    Use case ends.

**Extensions**

* 1a. A required detail is missing or invalid.

    * 1a1. Doc shows an error message describing the expected input.

      Use case resumes at step 1.

* 1b. A patient with the same name and phone number already exists.

    * 1b1. Doc shows an error message identifying the existing patient.

      Use case resumes at step 1.

* 1c. A patient with the same name but a different phone number already exists.

    * 1c1. Doc asks the doctor to confirm adding the patient.
    * 1c2. The doctor confirms the addition.
    * 1c3. Doc adds the patient to the caseload and shows the newly added patient record.

      Use case ends.

    * 1c2a. The doctor does not confirm the addition.

        * 1c2a1. Doc cancels the addition without changing the caseload.

          Use case ends.

* 1d. Another patient already has the requested visit date and time.

    * 1d1. Doc shows an error asking the doctor to choose a different date and time.

      Use case resumes at step 1.

**Use case: UC02 - Edit a patient's details**

**Guarantees**

* Only the details that the doctor requests to change are replaced; other details remain unchanged.
* If Doc rejects the requested changes, the patient's existing details remain unchanged.

**MSS**

1.  Doctor requests to list patients.
2.  Doc shows the caseload.
3.  Doctor requests to edit a selected patient, specifying one or more changes to the patient's details.
4.  Doc updates the patient's details and shows the updated patient record.

    Use case ends.

**Extensions**

* 2a. The caseload is empty.

  Use case ends.

* 3a. The selected patient cannot be found in the current list.

    * 3a1. Doc shows an error message explaining that the selection is invalid.

      Use case resumes at step 3.

* 3b. Doctor does not specify any changes.

    * 3b1. Doc shows an error message explaining that at least one detail must be supplied.

      Use case resumes at step 3.

* 3c. A supplied detail is invalid.

    * 3c1. Doc shows an error message describing the expected input.

      Use case resumes at step 3.

* 3d. The requested changes would create a true duplicate with the same name and phone number as another patient.

    * 3d1. Doc shows an error message explaining that the patient record already exists.

      Use case resumes at step 3.

* 3e. The requested changes would use another patient's name with a different phone number.

    * 3e1. Doc asks the doctor to confirm the name duplicate.
    * 3e2. The doctor confirms the changes.
    * 3e3. Doc updates the patient's details and shows the updated record.

      Use case ends.

    * 3e2a. The doctor does not confirm the changes.

        * 3e2a1. Doc cancels the edit without changing the patient record.

          Use case ends.

* 3f. The requested changes would use another patient's visit date and time.

    * 3f1. Doc shows an error asking the doctor to choose a different date and time.

      Use case resumes at step 3.

**Use case: UC03 - Prepare for the day's visits**

**MSS**

1.  Doctor requests the list of patients due to be visited today.
2.  Doc shows those patients ordered by priority level, then by next visit date.
3.  Doctor requests to see the full record of a patient identified by their displayed index.
4.  Doc shows the patient's address, contact number, medical conditions and past visit notes.

    Steps 3 and 4 are repeated for each patient the doctor wants to prepare for.

    Use case ends.

**Extensions**

* 2a. No patient is due today.

    * 2a1. Doc shows a message stating there are no visits due.

      Use case ends.

* 3a. The displayed index does not identify a patient in the current list.

    * 3a1. Doc shows an error message.

      Use case resumes at step 2.

**Use case: UC04 - Review patients treated for a condition**

**MSS**

1.  Doctor requests to find patients treated for a given medical condition.
2.  Doc shows the patients whose records list that condition.
3.  Doctor requests to sort the shown patients by next visit date.
4.  Doc shows the same patients ordered by next visit date.

    Use case ends.

**Extensions**

* 2a. No patient has the given condition.

    * 2a1. Doc shows a message stating that no patient matched.

      Use case ends.

**Use case: UC05 - Archive a patient**

**MSS**

1.  Doctor requests to list patients.
2.  Doc shows the caseload.
3.  Doctor requests to archive a patient identified by their displayed index.
4.  Doc removes the patient from the active caseload and keeps the record, with its visit history, among the archived patients.

    Use case ends.

**Extensions**

* 2a. The caseload is empty.

  Use case ends.

* 3a. The displayed index does not identify a patient in the current list.

    * 3a1. Doc shows an error message.

      Use case resumes at step 2.

* 3b. The patient still has a next visit date scheduled.

    * 3b1. Doc asks the doctor to confirm the archival.
    * 3b2. Doctor confirms.

      Use case resumes at step 4.

    * 3b3. Doctor declines.

      Use case ends.

**Use case: UC06 - Schedule the next visit**

**Guarantees**

* Only the selected patient's current and previous visit datetimes change; these do not verify completion.
* Rejected input leaves patient records and the displayed list unchanged. Appointment conflicts display the
  conflicting patient in the patient details panel.

**MSS**

1.  Doctor requests a future appointment for a patient whose current visit has arrived or passed, identified by
    their displayed index.
2.  Doc retains the current datetime in the previous dates, saves the new appointment, and refreshes the patient's
    details while preserving the active filter and sort order.

    Use case ends.

**Extensions**

* 1a. The displayed index does not identify a patient.

    * 1a1. Doc explains that the selection is invalid.

      Use case resumes at step 1.

* 1b. The current visit is still in the future, or the requested appointment is invalid, earlier than the current visit,
  or unchanged.

    * 1b1. Doc explains the specific reason for rejection.

      Use case resumes at step 1.

* 1c. Another patient's current or previous visit already uses the requested datetime.

    * 1c1. Doc identifies the conflicting patient and time.

      Use case resumes at step 1.

* 1d. The requested datetime is at or before now, or is on a different day with at least seven current appointments.

    * 1d1. Doc combines all applicable time and busy-day warnings and asks for confirmation once.
    * 1d2. The doctor confirms the warning.
    * 1d3. Doc rechecks eligibility, the unchanged patient, and the unoccupied slot, then resumes step 2.

        Use case ends.

    * 1d2a. The doctor does not confirm the warning.

        * 1d2a1. Doc cancels the scheduling request without changing the record or displayed list.

          Use case ends.

    * 1d3a. A previously future datetime reaches or passes now when the doctor confirms.

        * 1d3a1. Doc adds the new time warning and asks for confirmation again without changing the record.

          Use case resumes at step 1d2.

    * 1d3b. Another patient takes the requested datetime before approval.

        * 1d3b1. Doc identifies the conflict and rejects the request.

          Use case resumes at step 1.

**Use case: UC07 - Restore the previous visit datetime**

**Guarantees**

* Only the selected patient's current and previous visit datetimes change; intervening edits to other fields remain.
* The datetime stored as current before rollback is discarded, with no redo. Rollback does not verify attendance
  or completion.
* Prompting and cancellation leave patient data, saved data, list context, and details unchanged. Invalid selection
  or empty previous dates are rejected immediately without prompting.
* A confirmed rollback saves once. A captured patient that changed or disappeared is not overwritten.

**MSS**

1.  Doctor requests rollback for a patient identified by their displayed index.
2.  Doc displays the selected patient's name, phone, restored datetime, discarded datetime, and permanent-discard warning,
    then asks for confirmation.
3.  Doctor enters `y`, ignoring surrounding whitespace and letter case.
4.  Doc makes the latest previous datetime current and removes it from the previous-date list.
5.  Doc saves the changed record, reports the restored and discarded datetimes, and refreshes the patient's details
    while preserving the active filter and sort order.

    Use case ends.

**Extensions**

* 1a. The displayed index does not identify a patient.

    * 1a1. Doc explains that the selection is invalid.

      Use case resumes at step 1.

* 1b. The selected patient has no previous datetime to restore.

    * 1b1. Doc explains that no previous visit dates remain.

      Use case ends.

* 3a. Doctor enters any response other than trimmed, case-insensitive `y`, including blank input or valid command text.

    * 3a1. Doc reports `Visit restoration cancelled.` without changing or saving the record. The response is consumed.

      Use case ends.

* 4a. The captured patient changed or no longer exists before confirmation.

    * 4a1. Doc reports the stale-selection error without changing the replacement record or displayed list.

      Use case ends.

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should work without requiring an installer, and without the user having to install any software other than Java.
3.  Should support local patient management, visit planning, and the offline command directory without an internet
    connection. Opening the hosted full User Guide from Help requires internet access.
4. Should support at least 1,000 patient records, with up to 100 previous visit datetimes and one patient-level note
   per record.
5. **Target:** Should respond to each valid command within 2 seconds when operating on 1,000 patient records with up
   to 100 previous visit datetimes and one patient-level note per record. This excludes the time spent entering the
   command. **Verification:** Not yet verified; a benchmark under this workload remains to be run.
6. For common tasks such as adding, finding, listing, editing, scheduling, and restoring patient visits, a user who
   types at above-average speed should be able to complete the task faster through the CLI than by using the mouse.
7.  A doctor who has not used a CLI application before should be able to add a patient and look up that patient's record within 15 minutes of reading the User Guide.
8.  Should save every change to the local data file before accepting the next command, so that at most one command's worth of data is lost if the app terminates unexpectedly.
9.  Should store all patient data only on the user's own machine, so that no patient data leaves the doctor's computer.
10. Should start up and be ready to accept a command within 5 seconds on a typical modern laptop.
11. Should not crash or lose existing records if the data file has been manually edited into an invalid state; it should instead report the problem and start with an empty caseload.
12. Should be delivered as a single JAR file of no more than 100MB, so that it can be copied onto a laptop taken on house visits.

NFR 6 is an unverified usability goal. The team must agree on a concrete mouse-versus-CLI comparison procedure before
it can be assessed.

### Glossary

* **Active patient record**: A patient record included in the doctor's current caseload and visit planning.
* **Archived patient record**: A patient record retained for future reference but excluded from the active caseload and visit planning.
* **Calendar label**: `Past`, `Today`, or `Upcoming` beside the current datetime, based on its calendar date relative
  to the local date when details are rendered. It does not indicate attendance or completion.
* **Caseload**: The collection of active patient records currently managed by a doctor.
* **Current visit date**: The required date and time stored in `visitDate`. It can be past, present, or future and
  remains stored until changed using `edit`, `next`, or `prev`. It must differ from every other current or previous
  datetime.
* **House call**: A medical consultation carried out at the patient's home rather than at a clinic.
* **House-call doctor**: A doctor who travels to patients' homes to provide medical consultations and follow-up care.
* **Medical condition**: An ongoing health issue recorded in a patient's notes to help the doctor prepare for future visits.
* **MSS**: Main Success Scenario; the sequence of steps in a use case when nothing goes wrong.
* **Next visit date**: The current visit datetime used for planning. The field remains required after its time passes.
* **Overdue visit**: A planning term for a current visit datetime that has passed. Doc does not determine whether the
  visit took place.
* **Patient record**: The information Doc stores for one patient, including their contact details, medical notes,
  tags, current visit date and time, and previous visit dates and times retained by `next`.
* **Priority level**: A label indicating how closely a patient needs to be followed up, used when ordering planned visits.
* **Rollback**: Restoring the latest previous visit datetime with `prev`, replacing and discarding the current datetime
  while preserving the patient's other details. It is not a general undo operation and has no redo.
* **Visit**: A single house call. Doc stores one current datetime, ordered previous datetimes, and one patient-level note.
* **Visit record**: A patient's current and previous visit datetimes. These record scheduled times retained by the
  user and do not verify attendance or completion.

-------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Manual-testing conventions and disposable fixtures

For the visit-record testing routes below, use a newly created writable scratch folder and prepare the fixture stated for
each route. Other manual-testing sections retain their own setup instructions.
Never clear or edit the user's normal data folder.
When a route says **continue**, keep the state produced by the preceding numbered step. A route marked **independent**
starts from its own fixture. Reset the scratch data to the stated starting state before repeating a variation or moving
to another independent case.

For the visit-record routes, choose globally unused datetimes relative to the execution clock. A simple illustration
for an execution on 26 October 2026 is:

| Symbol | Full datetime in the illustration | Meaning |
| --- | --- | --- |
| `P0` | `25/10/2026 0800` | Sentinel patient's current datetime. |
| `P1` | `25/10/2026 0900` | Nextalpha's initial current datetime. |
| `P2` | `25/10/2026 1000` | Nextalpha's replacement current datetime. |
| `P3` | `25/10/2026 1100` | Nextbeta's replacement current datetime. |
| `P4` | `25/10/2026 1200` | An additional past datetime for an independent case. |
| `F1` | `27/10/2026 0900` | First future datetime. |
| `F2` | `28/10/2026 0900` | Second future datetime. |
| `F3` | `29/10/2026 0900` | Nextbeta's initial current datetime. |
| `F4` | `30/10/2026 0900` | Intervening-edit datetime for rollback. |

The required ordering is `P0 < P1 < P2 < actual now < F1 < F2 < F3 < F4`. `P1`, `P2`, `P3`, and `P4` must be on
calendar dates before today; `F1` through `F4` must be on dates after today. The example uses yesterday for the past
values and successive future dates; on another execution date, replace every symbol with a complete unused
`d/M/yyyy HHmm` value that preserves the same ordering and local-date relationship. `D` below means a calendar date
only, so replace `D 1700` with a full value such as `2/11/2026 1700` before submitting it. Symbols are explanatory
labels, not executable command arguments.

The continuous `next` route and the rollback route deliberately share the sentinel and Nextalpha/Nextbeta fixture. The
JSON, capacity, calendar, and Help checks are independent cases; preserve other patients and omitted optional fields when a
case does not say to reset them. Successful `edit`, `next`, and `prev` commands preserve the active filter and sort
comparator, although the updated patient may move or disappear when those rules are reevaluated. Their patient card
and details panel refresh to the replacement record.

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with seven sample patient records in this order: George Miller, Priya Nair, Aisha
      Rahman, Marcus Lim, Mei Lin Tan, Daniel Wong, and Noor Aziz. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Save failures and patient details

Use a disposable copy of Doc with disposable patient data. Do not modify the permissions of your working data file.

1. Clear the disposable list with `clear`, then `y`. Add a patient with
   `add n/Alpha p/90000001 a/1 Alpha Road vd/1/1/2000 0900`, then run `view 1`.
1. While Doc stays open, make its `data/addressbook.json` read-only (on Windows, use the file's Properties dialog).
   Run `next 1 1/1/2099 1000`, using a later supported future datetime if necessary.
   Expected: A save error remains in the response panel. The patient card and details continue to show the original
   appointment, and the saved JSON still contains the original visit.
1. Restore write access. Run `delete 1`, then `y`.
   Expected: Deletion succeeds, both the list and details panel are empty, and the saved JSON has no patients.
1. Repeat the setup, make the file read-only, then run `delete 1` and `y`.
   Expected: A save error appears, and the patient remains in both the list and details panel. Restore write access
   before continuing.

`MainWindowSaveFailureTest` automates the command-entry, display, and persistence assertions with injected
permission and I/O failures, including `add`, `edit`, confirmed `prev`, deletion, clearing, and unchanged details
after `list`. It does not replace checking actual file permissions on each supported operating system.

### Main-window keyboard navigation and details scrolling

Use a new writable folder containing only a copy of `doc.jar`, so no normal patient data is changed. Launch Doc there;
the first launch loads the sample data. The route below deliberately clears it before adding two known records, so
the patient list is in stored order with `Alpha Patient` at index 1 and `Beta Patient` at index 2.

1. **Reach an empty details panel:** Run `clear`, then submit `y` at the confirmation prompt.<br>
   Expected: The patient list is empty and the patient-details panel is blank. Click the command box and press Tab four
   times. Focus should move through command box, response panel, patient list, and patient details, then wrap to the
   command box. From the command box, press Shift+Tab four times and confirm the reverse cycle. The empty details panel
   remains reachable and has a visible outline.
1. **Create repeatable records:** Submit these commands in order:

   ```text
   add n/Alpha Patient p/90000001 a/1 Alpha Road vd/1/12/2099 0900
   add n/Beta Patient p/90000002 a/2 Beta Road vd/2/12/2099 0900 note/This deliberately long clinical note records medication tolerance, home blood-pressure observations, mobility changes, caregiver updates, hydration reminders, and follow-up instructions for the next house call. Repeat the details panel checks after viewing this record so the note extends beyond the visible viewport and requires vertical scrolling.
   ```

   Each successful `add` may display the newly added patient. Run `view 2` before the scrolling checks so the long
   `Beta Patient` record is definitely displayed and the list indices are known.
1. **Details scrolling and focus outline:** Use Tab or Shift+Tab to focus patient details. Confirm that the outline
   surrounds the whole panel rather than an individual label or scrollbar. Press <kbd>Up</kbd>, <kbd>Down</kbd>,
   <kbd>Page Up</kbd>, <kbd>Page Down</kbd>, <kbd>Home</kbd>, and <kbd>End</kbd> separately.<br>
   Expected: Up and Down move a small amount, Page Up and Page Down move about one viewport, and Home and End reach the
   top and bottom without overscrolling. Mouse-wheel scrolling still works.
1. **Fitting content:** Run `view 1`, then enlarge the window vertically until Alpha Patient's details fit without a
   scrollbar. Focus patient details and press each of the six scrolling keys.<br>
   Expected: The details remain unchanged because there is no content to scroll.
1. **View reset and command focus:** Run `view 2`, focus patient details, and press End. Return to the command box and
   run `view 2` again. Repeat after running `view 1`, then run `view 2` once more.<br>
   Expected: Every successful view starts Beta Patient or Alpha Patient at the top, including the repeated same-patient
   view and the switch between long and short records. After each command entered in the command box, focus remains in
   the command box.
1. **Empty-panel behavior after clearing:** Run `clear`, submit `y`, and focus the blank details panel through the Tab
   cycle. Press each scrolling key separately.<br>
   Expected: No key changes the blank panel, and Tab/Shift+Tab still move between all four sections.
1. **Existing shortcuts:** Focus the command box and confirm <kbd>Up</kbd> and <kbd>Down</kbd> still recall command
   history. Focus the patient list and move the selection down through at least four patients, then back up. Confirm
   that each selected patient card remains fully visible and the list does not jump when the selected card is already
   visible. Leave unfinished text in the command box, traverse with Tab and Shift+Tab, and confirm the text and
   displayed patient do not change. Repeat F1, Help, and mouse-scrolling checks; their existing behavior should be
   unchanged.

### Dates of birth

Use a disposable data file and available visit slots.

1. Run `add n/Dob Patient dob/15/6/1990 p/81234567 a/10 Test Road vd/22/10/2026 1000`, then `view 1`.
   Expect `Date of birth: 15/6/1990 (N years old)` immediately below the name in both the left-hand patient
   card and the right-hand details panel, with the age in completed years
   on the computer's current local date. A leap-day birthday advances on March 1 in non-leap years.
1. Run `edit 1 n/Dob Patient dob/20/7/1985`, then `view 1`; expect the replacement date and recalculated age.
1. Edit another field, then run `edit 1 vd/1/1/2000 1000` so the current visit has passed.
   Run `next 1 1/1/2099 1000`; expect the date of birth unchanged and the previous visit retained.
1. Restart the application and run `view 1`; expect the saved date of birth to remain.
1. Run `edit 1 dob/`, then `view 1`; expect the date of birth rows in both panels to disappear and all other
   details to remain.
   Restart and view the patient; expect the row to stay hidden. Whitespace-only `dob/` also clears the field,
   and clearing an already empty date of birth succeeds. Set a new DOB to restore the row.
1. Try `edit 1 dob/31/2/2000`, a future date, and repeated `dob/` prefixes (including `edit 1 dob/ dob/`).
   Expect errors and unchanged data. Repeat these invalid cases using `add`, and try an empty DOB with `add`;
   no patient should be added.
1. Add another patient without `dob/` and view it; expect no date of birth row or stale text. View the first
   patient again; expect the row to reappear. Set the second patient's DOB using `edit INDEX dob/1/1/2000`.
1. Load an older valid data file without `dateOfBirth`; expect the patients to load with the DOB row hidden.

### Allergies

Use a disposable data file, starting with no patients and available visit slots.

1. Run `add n/Allergy Test bt/A+ all/Penicillin; latex alc/Does not drink smk/former p/81234567 a/10 Test Road vd/1/1/2000 1000`.
   Run `view 1`; expect allergies below blood type and above alcohol history and smoking status on the right.
1. Run `edit 1 all/No known allergies`, then `view 1`; expect the explicit entry to remain visible.
1. Edit alcohol history or smoking status, then run `next 1 1/1/2099 1000`; expect allergies unchanged and the previous
   visit retained. Restart and view the patient; expect all recorded details to remain.
1. Run `edit 1 all/`, then `view 1`; expect the allergy row hidden without leaving a gap and other fields unchanged.
   Restart; expect the field to remain cleared. Whitespace-only edits and clearing an unrecorded field succeed.
1. Try a blank `all/` on add and repeated prefixes on add/edit, including `all/ all/`. Expect errors and unchanged data.
   Try a long description with punctuation; expect wrapping and scrolling. Switch patients to check for stale text.
1. Load an older valid file without `allergies`; expect an unrecorded value rather than an inferred absence of allergies.

### Smoking status

Use a disposable data file, starting with no patients and available visit slots.

1. Run `add n/Smoking Test alc/Does not drink smk/FORMER p/81234567 a/10 Test Road vd/1/1/2000 1000`.
   Run `view 1`; expect `Smoking status: former` immediately below alcohol history on the right.
1. Run `edit 1 alc/Occasional drinking smk/current`, then `view 1`; expect both replacements in that order.
   Repeat with `never`, `former`, and `unknown`, including mixed case and surrounding whitespace.
1. Edit another field, then run `next 1 1/1/2099 1000`; expect smoking status unchanged and the previous visit retained.
   Restart the application and view the patient; expect the status and visit history to remain.
1. Run `edit 1 smk/`, then `view 1`; expect the row hidden without leaving a gap and other details unchanged.
   Restart; expect the field to remain cleared. Whitespace-only edits and clearing an unrecorded status succeed.
1. Run `edit 1 smk/unknown`, then `view 1`; expect the explicit unknown status to remain visible.
1. Try `smk/daily`, blank `smk/` on add, and repeated prefixes on add/edit (including `smk/ smk/`).
   Expect errors and unchanged data. Switch between patients with and without a status; expect no stale text.
1. Load an older valid file without `smokingStatus`; expect an unrecorded value rather than explicit `unknown`.

### Alcohol history

Use a disposable data file, starting with no patients and available visit slots.

1. Run `add n/Alcohol Test bt/A+ alc/Occasional drinking p/81234567 a/10 Test Road vd/1/1/2000 1000`.
   Run `view 1`; expect alcohol history below blood type, with no gap for unrecorded allergies.
1. Run `edit 1 bt/O- alc/Does not drink`, then `view 1`; expect both replacement values in that order.
1. Edit another field, then run `next 1 1/1/2099 1000`; expect alcohol history unchanged and the previous visit retained.
   Restart the application and view the patient; expect the alcohol history and visit record to remain.
1. Run `edit 1 alc/`, then `view 1`; expect the row hidden without leaving a gap and other details unchanged.
   Restart; expect the field to remain cleared. Whitespace-only edits and clearing an empty history also succeed.
1. Try an empty `alc/` on add and repeated prefixes on add or edit, including `alc/ alc/`.
   Expect errors and unchanged data. Supply a long description with punctuation; expect wrapping and scrolling.
1. Add another patient without `alc/` in a different slot. Switch between records; expect no stale alcohol history.
   Load an older valid file without `alcoholHistory`; expect it to load with the field unrecorded.

### Blood types

Use a disposable data file, starting with no patients, and available visit slots.

1. Run `add n/Blood Test bt/a+ p/81234567 a/10 Test Road vd/1/12/2099 1000`, then `view 1`.
   Expect `Blood type: A+` directly below the name in the patient list and the details panel.
1. Run `edit 1 bt/ab-`, then `view 1`; expect `Blood type: AB-` in both panels.
1. Edit a different field, then run `edit 1 vd/1/1/2000 1000` so the current visit has passed.
   Run `next 1 2/12/2099 1000`; expect the blood type unchanged and `1/1/2000 1000` in the visit history.
1. Restart the app and view the patient; expect the saved blood type to remain.
1. Run `edit 1 bt/`, then `view 1`; expect the blood type row to disappear without leaving a gap.
   Restart and view the patient; expect the value to remain cleared. Whitespace-only input also clears it.
   Clearing an already empty blood type succeeds; `edit 1 bt/O+` restores the row.
1. Try all eight blood types using add and edit. Expect normalized uppercase values.
   Try `bt/A`, `bt/C+`, `bt/A +`, `bt/UNKNOWN`, and repeated prefixes, including `bt/ bt/`.
   Expect errors and unchanged data. An empty `bt/` is invalid for add.
1. Add a patient without `bt/` in a different visit slot. Switch between patients using `view INDEX`;
   expect the row to disappear or reappear according to the selected patient's recorded blood type.
1. Load a valid older record without `bloodType`; expect it to load with no blood type row.

### Emergency contacts

Use a disposable data file. Start with no patients and use available visit slots.

1. Run `add n/John Tan p/91234567 a/123 Clementi Road vd/22/10/2026 1000 ec/Mary Tan, 98765432`.
   Run `view 1`; expect `Emergency Contact: 98765432 (Mary Tan)` directly below the patient's phone number.
1. Run `edit 1 ec/David Tan, 87654321`, then `view 1`; expect the replacement contact in the same position.
1. Run `edit 1 note/Review medication`, then `edit 1 vd/1/1/2000 1000` so the current visit has passed.
   Run `next 1 1/1/2099 1000`, then `view 1`; expect the contact unchanged and `1/1/2000 1000` retained as a
   previous visit. Replacing or clearing the emergency contact must preserve that previous datetime.
1. Run `edit 1 ec/98765432`, then `view 1`; expect `Emergency Contact: 98765432` without a name or parentheses.
   Expect the visit history and other fields to remain unchanged.
1. Restart the application and run `view 1`; expect the saved contact to remain.
1. Run `edit 1 ec/`, then `view 1`; expect the emergency contact row to be hidden and other details unchanged.
   Restart and view the patient again; expect the contact to remain cleared. Repeating `edit 1 ec/` or using
   `edit 1 ec/   ` should also succeed. Add a contact again before testing invalid replacements.
1. Try `edit 1 ec/Mary Tan`, `edit 1 ec/Mary Tan, abc`, `edit 1 ec/ ec/`, and
   `edit 1 ec/Mary Tan, 98765432 ec/David Tan, 87654321`. Expect errors and unchanged patient details.
1. Add another patient without `ec/`, then view that patient; expect no emergency contact row or stale details
   from the previous patient. Add a contact using `edit INDEX ec/Mary Tan, 98765432` and view it again.
1. Load a valid older data file without `emergencyContact` fields; expect all patients to load with no contacts.

### Help window lifecycle

1. **Offline access:** Open Help with F1 while disconnected from the Internet.<br>
   Expected: The command directory remains available, and no browser opens automatically.

1. **Command directory:** Open Help at its default size.<br>
   Expected: **Manage patients and visits**, **Find and view patients**, and **Application controls** each appear once
   in the documented order. Every command has its short description, syntax template, optional-field rows where
   applicable, and notation legend. The complete directory is reachable using the available navigation controls.

1. **Navigation hint:** Inspect the hint at the top of the command list.<br>
   Expected: The Up, Down, Tab, Shift, and Escape key labels are visible as non-interactive visual keycaps. They do
   not receive focus or activate an action when clicked.

1. **Small window:** Resize Help to its minimum size and scroll through the list. Repeat after a guide action
   shows feedback.<br>
   Expected: Several command entries remain readable, all groups can be reached by scrolling, and text wraps
   without horizontal clipping. The URL, feedback, and buttons remain visible below the list.

1. **Window reuse:** With no confirmation or follow-up prompt pending, enter `help`. Move and resize Help,
   then enter `help add` and `help 123` in the main window.<br>
   Expected: Each request focuses the same window without changing its size or position. Extra arguments are ignored.

1. **Minimize and restore:** Minimize Help using its native control. Request it again using `help`, F1, and
   **Help → Help**, minimizing it before each attempt.<br>
   Expected: Help minimizes independently, the main window stays usable, and each request restores and focuses the
   existing window.

1. **Draft preservation:** Type `find Be` without submitting it, select part of the text, and press F1. Close Help
   using **Close**, **Escape**, and the native close control, reopening it for each attempt.<br>
   Expected: Focus returns to the command box with the draft unsubmitted and its caret and selection restored.

1. **Keyboard scrolling:** Focus the result display and press F1. Resize Help to its minimum size. Use Down and
   End to reach the later commands, then Home and Up to return towards the top.<br>
   Expected: Help opens with focus on the command list, and the keys allow every group to be reached.

1. **Button navigation:** Open Help. Press Tab until **Open User Guide** is focused, then press Shift+Tab to return
   to the command list. Press Tab through **Open User Guide** and **Copy URL** until **Close** is focused, then press
   Space.<br>
   Expected: Focus moves between the list and buttons, focused buttons have a visible outline, and Space activates Close.

1. **Help during confirmation:** Use disposable patient data with at least one patient.

   1. Enter `delete 1`, then type `y` without submitting it. Open and dismiss Help using F1 and the Help menu.<br>
      Expected: The confirmation prompt and draft remain unchanged, with no deletion or data-file write.<br>
      Cleanup: Replace the draft with `n` and submit it to cancel.

   1. Enter `delete 1`, then submit `help`.<br>
      Expected: Deletion is cancelled without opening Help or changing patient data. The typed input is treated
      as the confirmation response.

1. **Shutdown:** Open Help and exit Doc using `exit`, **File → Exit**, and the main window's native close control,
   restarting between attempts. Repeat with Help minimized.<br>
   Expected: Both windows close; no Help window remains.

1. **Display scaling:** Repeat the layout and keyboard checks at 1920×1080 with 100% and 125% OS scaling, and at
   1280×720 with 150% OS scaling.<br>
   Expected: Text and controls remain readable and accessible after resizing. Scene-layout tests do not replace
   these OS-level checks.

### User Guide actions

Open Help for these checks.

1. **Open the guide:** With an Internet connection and a default browser configured, choose **Open User Guide**.
   Repeat using Tab and Space, then Enter on Windows and Linux.<br>
   Expected: The browser opens the displayed URL. Doc reports a launch request, not confirmation that the page loaded.

1. **Copy the address:** Choose **Copy URL** (replacing the current clipboard contents), then paste into a scratch
   text field.<br>
   Expected: The pasted text matches the displayed URL exactly, and Doc reports copy success.

1. **Offline guide request:** Disconnect from the Internet and choose **Open User Guide**.<br>
   Expected: Help remains usable. Doc may still report a browser launch request; any connection errors appear in the browser.

Browser-launch and clipboard rejection cases are simulated by `HelpWindowActionsTest`; see
[External guide actions](#external-guide-actions). They do not require changing OS permissions or browser settings.

### Finding patients

Start with the seven sample patients by launching Doc from an empty folder. Run `list` before each step.

1. Run `find n/m`.<br>
   Expected: `3 patients found.` George Miller, Marcus Lim, and Mei Lin Tan are listed.

1. Run `find n/george priya`.<br>
   Expected: George Miller and Priya Nair are listed, because keywords for one field match any of them.

1. Run `find n/m t/morning`.<br>
   Expected: `1 patient found.` Only Mei Lin Tan is listed, because different fields must all match.

1. Run `find p/8901`, `find a/tampines`, and `find note/review`.<br>
   Expected: Noor Aziz; Marcus Lim; then George Miller and Marcus Lim.

1. Run `find vd/15/10/2026`, then `find vd/14/10/2026 15/10/2026`, then `sort date`.<br>
   Expected: Priya Nair; then Mei Lin Tan and Priya Nair, ordered by visit time after `sort date`.

1. Run `find p/0000`.<br>
   Expected: `No patients found.` followed by a hint listing every search prefix, and an empty list.

1. Try each of these commands:

   | Command | Expected feedback |
   | --- | --- |
   | `find` | Invalid command format, with find usage. |
   | `find george` | A reminder to put a prefix before keywords, with find usage. |
   | `find n/` | `Give at least one keyword after n/.` |
   | `find n/george n/priya` | The duplicate-prefix error for `n/`. |
   | `find p/9123abc` | `Phone keywords should only contain digits.` |
   | `find vd/31/2/2026` | `"31/2/2026" is not a valid visit date.` with the date format. |
   | `find vd/15/10/2026 0900` | `"0900" is not a valid visit date.` with the date format. |

   Expected: Each rejected command leaves the displayed list unchanged.

1. Paste `find n/george`, a tab character, and `p/0000` into the command box as one command, then press Enter.<br>
   Expected: `No patients found.`, because the tab separates two fields and George Miller's phone does not match.

### Sorting visits by date

`SortCommandParser` accepts the `date` field followed by an optional cutoff date in `d/M/yyyy`
format. It rejects missing fields, invalid dates, and additional arguments with the command
usage message. The model exposes a `SortedList` backed by its `FilteredList`. `SortCommand`
preserves the current filter and sorts its displayed patients into two groups around the
supplied cutoff date, or the local date at execution time when the cutoff is omitted: visits
on or after the cutoff first, followed by visits before it. Each group is ordered by visit date
and time, earliest first. Stored patient order is unchanged. Updating the filter resets the
displayed order, preserving the usual behavior of `list` and `find`.

1. Prepare patients with visits two days ago, yesterday, today at 0000, today at 2300, and tomorrow,
   entered in a different order from their visit dates.
1. Run `sort date`. Expect today's and tomorrow's patients first, followed by the two past visits;
   expect each group to be in ascending date-time order.
1. Run `sort date` with a cutoff equal to yesterday's date. Expect yesterday's, today's, and tomorrow's patients
   first, followed by the visit from two days ago.
1. Run `sort date` with a cutoff equal to tomorrow's date. Expect tomorrow's patient first, followed by all earlier
   visits in ascending date-time order.
1. Run `find` for one patient, then `sort date`. Expect only that matching patient to remain displayed.
1. Run `sort date` twice. Expect the same order; equal visit times retain stored order.
1. Run `delete 1`, then `y`. Expect the first displayed patient to be deleted, with the remaining visits still sorted.
1. Run `list`. Expect every remaining patient, including yesterday's, in stored order.
1. Try `sort date` on an empty patient list. Expect an empty list. Try it on a patient list containing only past
   visits; expect all visits in ascending date-time order.
1. Run `sort`, `sort name`, `sort date name`, `sort date 31/2/2026`, and `sort date 18/9/2026 extra`. Expect each
   command to show the required format and both examples without changing the displayed list.

### Viewing the agenda

1. Prepare patients with visits on 26 October 2026; 27 October at 0900 and 1800; 30 October; 2 November; and
   3 November. Enter them in an order different from their visit dates.
1. Run `agenda 27/10/2026`.<br>
   Expected: Visits from 27 October through 2 November 2026 appear chronologically.
1. Run `agenda 27/10/2026 1`.<br>
   Expected: Only visits on 27 October appear, including visits at 0000 and 2359.
1. Run `find` for a patient outside the agenda range, then run `agenda 27/10/2026 7`.<br>
   Expected: The previous search is replaced with every visit in the agenda range.
1. Run `list`.<br>
   Expected: All patients reappear in stored order.
1. Run `agenda`, `agenda 31/2/2026`, `agenda 27/10/2026 0`, `agenda 27/10/2026 366`, and
   `agenda 27/10/2026 7 extra`.<br>
   Expected: Each command shows the agenda usage message and leaves the displayed list unchanged.
1. Run `agenda 31/12/9999 1`, then `agenda 31/12/9999 2`.<br>
   Expected: The first shows visits on 31 December 9999; the second reports that the date range must end on or before
   31/12/9999 and leaves the displayed list unchanged.

### Distinguishing past visits in the patient list

`PersonCard` compares each patient's visit date with the computer's local date when the card is created. A visit
before today receives the `past-visit-card` style class. The dark theme uses this class to give both alternating row
colours darker variants and to mute the card's text and tags. Visits earlier today are not treated as past. Because
the styling belongs to the card rather than `SortCommand`, it applies to every patient-list view.

1. Prepare patients with visits yesterday, earlier today, later today, and tomorrow.
1. Run `list`. Expect only yesterday's patient card to have a darker background with muted text and tags. Expect
   alternating rows to remain visually distinct.
1. Run `find` for the patient with yesterday's visit. Expect the matching card to remain muted.
1. Run `sort date`. Expect all past patient cards to remain muted and to appear after today's and future visits.
1. Select a muted card. Expect its selection border and background to remain visible, and expect the details panel
   on the right to remain fully readable.
1. Edit the past visit to tomorrow's date. Expect the updated card to return to the normal styling.

### Preventing duplicate visit dates and times

Use a disposable copy of the data file. Start with `Alex Tan` scheduled for `8/10/2026 1000`.

1. Try to add a different patient scheduled for `8/10/2026 1000`. Expect an error identifying the occupied slot and
   no change to the displayed list or data file.
1. Add a different patient, using a different name and phone number, scheduled for `8/10/2026 1030`. Expect the
   addition to succeed.
1. Try to edit the second patient to `vd/8/10/2026 1000`. Expect the occupied-slot error and unchanged details.
1. Re-enter `vd/8/10/2026 1000` for Alex. Expect the edit to succeed because Alex retains the existing slot.
1. Try to add another `Alex Tan` with a different phone number but with `vd/8/10/2026 1000`. Expect the occupied-slot
   error without a duplicate-name confirmation prompt.

### Loading and validating previous visit dates

Use a disposable data file and close Doc before editing it. For the first patient, set `visitDate` to
`10/10/2026 1000` and add `"previousVisitDates": ["1/10/2026 1000", "2/10/2026 1000"]`.
Give every other patient's current and previous dates different datetimes.

1. Reopen Doc and run `edit 1 note/Review medication`, then `view 1`. Expect `Previous visit dates:` below the current
   datetime, with `2/10/2026 1000` above `1/10/2026 1000`, followed by the updated note. Close Doc and inspect the
   file. Expect the same dates in their original oldest-first order and the updated note.
1. Reopen Doc and try `edit 1 vd/2/10/2026 1000`, then `edit 1 vd/1/10/2026 1000`. Expect a chronology error with
   `2/10/2026 1000` identified as the latest previous visit. The record, saved file, and displayed list stay unchanged.
1. Run `edit 1 vd/3/10/2026 1000`. Expect success even though the date is in the past; the previous dates stay
   unchanged. Re-enter that datetime and expect success again.
1. Try to add a different patient at `01/10/2026 1000`. Expect a conflict with the first patient's previous date.
   Adding that patient at `1/10/2026 1030` succeeds. Use `find` to show only this second patient, then try
   `edit 1 vd/2/10/2026 1000`. Expect a conflict even though the first patient is hidden.
1. With Doc closed, try removing `previousVisitDates`, setting it to `null`, then setting it to `[]`, reopening
   after each separate edit. Expect each form to load with empty previous dates.
1. With Doc closed, test a non-array value, a null array entry, a malformed datetime, reversed dates, equivalent
   duplicate datetimes, a previous date equal to the current date, and a datetime shared by two patients.
   Expect the existing invalid-data handling for each file. Do not run saving commands after a load failure:
   the existing startup fallback can overwrite the file with an empty list. Restore the disposable fixture
   between cases. Removing or nulling the required `visitDate` remains invalid even with populated previous dates.

### Adding patients with duplicate names

Use a disposable copy of the data file for these tests. Start with a patient named `Alex Tan` whose phone number is
`91234567`. Use a different unused visit date and time for every proposed additional patient.

1. Add another patient named `Alex Tan` with phone number `92345678`. Expect a confirmation prompt and no immediate
   change. Enter `y`; expect both patients to be present and the data file to be saved once.
1. Add `alex   tan` with another unused phone number. Expect the same confirmation prompt because case and repeated
   spaces are ignored for duplicate detection. Enter `y`; expect the name to retain its entered capitalization and
   spacing when displayed and saved.
1. Repeat with another unused phone number, then enter `n`. Expect `Addition cancelled.`, no new patient, and no data
   file change.
1. Try to add another patient named `ALEX  TAN` with phone number `91234567`. Expect the true-duplicate error without
   a confirmation prompt, even if the remaining fields differ.
1. Close and reopen the application after confirming a name duplicate. Expect both same-name patients to load.

### Optional alcohol history

`Person` stores an immutable `AlcoholHistory` alongside blood type. It holds free text with surrounding whitespace
removed; empty text represents unrecorded history and remains distinct from the explicit text `Does not drink`.
`ParserUtil#parseAlcoholHistory` rejects blank add values, while edit maps an explicitly blank `alc/` to an empty
history. Both parsers reject repeated `alc/` prefixes. Unrelated edits, `next`, and `prev` preserve the history.

`JsonAdaptedPerson` saves the `alcoholHistory` string and treats missing or null fields in older files as unrecorded.
The right-hand details panel displays recorded alcohol history immediately after allergies, wraps long text, and
hides the row when unrecorded. Patient list cards and duplicate-patient identity remain unchanged.

### Optional smoking status

`Person` stores a `SmokingStatus` enum with `NEVER`, `FORMER`, `CURRENT`, `UNKNOWN`, and `UNRECORDED`.
`UNKNOWN` represents explicitly recorded uncertainty; only `UNRECORDED` hides the row in the details panel.
`SmokingStatus#fromString` trims surrounding whitespace and normalizes letter case using `Locale.ROOT`.
`ParserUtil#parseSmokingStatus` rejects blank or unsupported recorded values, while edit maps blank `smk/` to
`UNRECORDED`. Both parsers reject repeated `smk/` prefixes. Other edits, `next`, and `prev` preserve smoking status.

`JsonAdaptedPerson` stores the normalized lowercase status, or an empty string for unrecorded values.
Missing or null `smokingStatus` fields in older files load as `UNRECORDED`; unsupported saved values are rejected.
The right-hand details panel displays smoking status immediately after alcohol history. Patient list cards and
patient identity remain unchanged. Add/edit help places `[smk/STATUS]` after `[alc/ALCOHOL_HISTORY]`.

### Editing patient details

Use a disposable copy of the data file for these tests. Start with multiple patients, including one with
nonempty notes, and record the selected patient's existing details before editing.

1. Ensure `8/10/2026 1000` is unused, then run `edit 1 vd/8/10/2026 1000`. Expect the first displayed patient's card
   to show the new visit date/time; all other details, including notes, remain unchanged.
1. Run `edit 1 e/new@example.com`. Expect only the email to change; the visit date/time and notes are preserved.
1. Run `edit 1 note/Patient requests a morning visit`. Expect the card's note to update immediately;
   the visit date/time, name, phone, email, address, and tags remain unchanged.
1. Run `edit 1 note/  Review  medication  `. Expect the stored and displayed note to be `Review  medication`,
   with the two internal spaces preserved. Repeat the same note; expect success and the same details.
1. Run `edit 1 note/`, then `edit 1 note/   `. Expect the card's note to be cleared immediately and both commands
   to succeed, with other details preserved. Restore a nonempty note before testing omitted-field preservation.
1. Run `edit 1 vd/8/10/2026 1000 e/new@example.com note/Review medication`.
   Expect only the visit date/time, email, and notes to change. Repeat with prefixes in a different order.
1. Run `edit 1 e/ note/`. Expect email and notes to be cleared, with the visit and other details preserved.
   Restore email and notes, then run `edit 1 vd/8/10/2026 1000 e/ note/`; expect the visit to change and both to clear.
1. Run `edit 1`. Expect the message "At least one field to edit must be provided." and unchanged records.
1. Run `edit 1 vd/`, then `edit 1 vd/31/2/2026 1000`. Expect date validation errors and unchanged records.
1. Run `edit 1 vd/8/10/2026 1000 vd/9/10/2026 1000`. Expect a duplicate-prefix error and unchanged records.
1. Run `edit 1 note/First note/Second`, `edit 1 note/Same note/Same`, and `edit 1 note/ note/`.
   Expect duplicate-prefix errors and unchanged records, including saved data.
1. Run `edit 0 note/`, `edit 999 note/`, and `edit 1 p/911a note/Review medication`.
   Expect index or phone validation errors and unchanged records; use an index beyond the displayed list size.
1. Run `edit 1 vd/1/1/2025 0930`. Expect the valid past date/time to be accepted.
1. Run `edit 1 e/`. Expect the email to be cleared, with the visit date/time and notes preserved.
1. Run `edit 1 t/friend t/colleague`, then `edit 1 t/`. Expect tags to be replaced, then cleared;
   the visit date/time and notes remain unchanged.
1. Show a filtered or sorted list with a different first patient, then run `edit 1 note/Review medication`.
   Expect that displayed patient to be edited while the filter or sort order remains active.
1. With patients `Alex Tan` and `Betsy Crowe` using different phone numbers, rename Betsy to `Alex Tan`. Expect a
   confirmation prompt and no immediate change. Enter `y`; expect both records to use the name `Alex Tan`.
1. Repeat the rename using `alex   tan`. Expect the same confirmation prompt because the normalized name matches.
1. Change only Alex's capitalization or repeated spacing. Expect the edit to succeed without a confirmation prompt
   because the patient's normalized name has not changed, while the entered form is preserved for display and storage.
1. Repeat the rename setup, then enter `n`. Expect `Edit cancelled.` and the original record to remain unchanged.
1. Give Betsy both Alex's name and phone number. Expect the true-duplicate error without a confirmation prompt.
1. After confirming two patients with the same name, edit only one patient's note. Expect the edit to succeed without
   another confirmation prompt.
1. Close and reopen the application after replacing notes, then repeat after clearing them.
   Expect the updated or empty note, edited date/time, and cleared email to remain saved,
   with other omitted fields preserved.

### Testing the next command

This is the continuous `next`-command route. Use a new disposable data folder and the common fixture from
[Manual-testing conventions and disposable fixtures](#manual-testing-conventions-and-disposable-fixtures). In the
illustration, use the concrete values in the commands below; on another execution date, replace every datetime with a
full unused `d/M/yyyy HHmm` value that preserves `P0 < P1 < P2 < actual now < F1 < F2 < F3 < F4`.

1. Launch Doc from the disposable folder, run `clear`, and enter `y`. Expect an empty patient list before continuing.

1. Add the three patients in this order:

   ```text
   add n/Sentinel Patient p/80234567 a/1 Sentinel Road vd/25/10/2026 0800
   add n/Nextalpha Patient p/81234567 a/10 Test Road vd/25/10/2026 0900 e/alpha@example.com note/Keep this note t/priority
   add n/Nextbeta Patient p/82345678 a/20 Test Road vd/29/10/2026 0900
   ```

   The sentinel is deliberately stored before Nextalpha, so Nextalpha is not first in the full list. Expect success,
   with nonempty optional fields for Nextalpha and empty email, notes, and tags for Nextbeta.

1. Run `find n/Nextalpha Nextbeta`, then `sort date`.<br>
   The displayed list must be:

   | Displayed index | Patient | Current |
   | --- | --- | --- |
   | 1 | Nextbeta Patient | `29/10/2026 0900` (`F3`) |
   | 2 | Nextalpha Patient | `25/10/2026 0900` (`P1`) |

   Run `next 2 29/10/2026 0900` (the illustration's `next 2 F3`). Expect a conflict naming Nextbeta and the
   occupied time, with Nextbeta shown in the patient details panel. The sorted list, stored data, and file remain
   unchanged. This verifies that the command uses the current displayed index rather than the full-list index.

1. Run `find n/Nextalpha` so that only Nextalpha is displayed. Try each of these commands:

   | Command | Expected feedback |
   | --- | --- |
   | `next` | Next usage. |
   | `next 1` | Missing arguments and usage. |
   | `next 0 27/10/2026 0900` | Invalid index syntax and usage. |
   | `next 2147483648 27/10/2026 0900` | Oversized index error and usage. |
   | `next 2 27/10/2026 0900` | Index outside the displayed list. |
   | `next 1 25/10/2026 0900` | Unchanged appointment. |
   | `next 1 25/10/2026 0859` | Appointment before the current recorded visit. |
   | `next 1 31/2/2027 0930` | Invalid date. |
   | `next 1 1/11/2026 2400` | Invalid time. |
   | `next 1 vd/27/10/2026 0900` | Prefix misuse and usage. |
   | `next 1 27/10/2026 0900 extra` | Extra arguments and usage. |

   Each rejection must preserve records, file contents, and the displayed list. Also test today's current local
   minute, provided it is later than the recorded visit; expect a time warning and cancel with `n`.
   Repeat the unchanged-date test with equivalent leading zeros when applicable.

1. Run `find n/Nextalpha Nextbeta`, then `sort date` again. Run `view 2`, followed by
   `next 2 27/10/2026 0900` (the illustration's `next 2 F1`). Expect Nextalpha's current datetime to become
   `27/10/2026 0900`, retaining all other fields and the filtered, sorted Nextalpha/Nextbeta list. Success requests
   saving and refreshes Nextalpha's details. Close Doc and inspect the JSON: expect current `27/10/2026 0900` and
   previous dates `[25/10/2026 0900]` in stored order.

1. Reopen Doc, run `find n/Nextalpha`, then immediately try `next 1 28/10/2026 0900` (the illustration's `F2`).<br>
   Expect the future-current-visit error advising `edit`, with record, saved file, and list unchanged. Run
   `edit 1 vd/25/10/2026 1000` (the illustration's `P2`), then submit `next   1   28/10/2026 0900` without finding
   Nextalpha again. Expect repeated spaces to be accepted and the one-patient filter to remain active. Inspect the
   JSON after closing Doc: expect current `28/10/2026 0900` and previous dates
   `[25/10/2026 0900, 25/10/2026 1000]`, in that order. The edit does not append the replaced `27/10/2026 0900`.

1. Reopen Doc, run `find n/Nextbeta`, then `edit 1 vd/25/10/2026 1100` (the illustration's `P3`). Run
   `next 1 27/10/2026 0900` directly, since the successful edit preserves the one-patient filter.<br>
   Expect Nextbeta to change, retaining empty email, notes, and tags. Its previous dates contain
   `[25/10/2026 1100]`.

1. **Rollback starting state:** Close and reopen the application. Find and view each test patient.<br>
   Expect Nextalpha at `28/10/2026 0900` with saved previous dates `[25/10/2026 0900, 25/10/2026 1000]`, with the
   latter displayed above the former. Expect Nextbeta at `27/10/2026 0900` with previous dates
   `[25/10/2026 1100]` and every other field preserved. Check the previous-date section below the current datetime
   and the persisted JSON. Re-find the intended patient after every successful mutation. Close Doc, then copy this
   scratch folder's `data/addressbook.json` into a separate backup folder as `rollback-start-addressbook.json`.
   Keep this backup outside the active scratch folder as the rollback starting state before running the independent
   cases below.

1. Repeat the conflict case using another patient's previous datetime, loaded through the disposable-file setup in
   [Loading and validating previous visit dates](#loading-and-validating-previous-visit-dates). Choose a future
   previous datetime and a later current datetime for that other patient, and an eligible current datetime for the
   selected patient. Expect rejection without mutation or saving, including when the other patient is hidden.

1. **Independent seven-visit case:** choose a future calendar date `D`, distinct from `F1`–`F4`, and an unused past
   datetime `P4`. Create `Nextgamma Patient` at `P4`, then create seven filler patients whose current datetimes on
   `D` are `0800`, `0900`, `1000`, `1100`, `1200`, `1300`, and `1400`. Run `find n/Nextgamma`, then replace `D`
   with its full date in `next 1 D 1700` (for example, `next 1 2/11/2026 1700`). Expect the seven-visit warning.
   Enter `n`; expect `Scheduling cancelled.` with the current visit, history, saved file, and list unchanged. Re-find
   Nextgamma, submit the same concrete command, and enter `y`; expect the visit to be scheduled at 1700 on `D`, with
   `P4` appended to its history.

1. **Independent six-visit case:** reset the scratch data, create a new `Nextdelta Patient` at an unused past
   datetime, and create only six filler patients on a new future date `D` at `0800` through `1300`. Submit the
   concrete `next 1 D 1700` command after substituting the full date. Expect no busy-day prompt and a successful save.

1. **Independent retrospective-entry case:** use disposable data and choose two unused past datetimes `P1 < P2`
   and a later unused past datetime `P3`. Add a patient at `P1`, then run `next INDEX P2` with the full datetime.
   Expect the non-future warning with no record, file, list, or details change. Enter `n` and verify cancellation.
   Repeat and enter `y`; expect current `P2`, history `[P1]`, and refreshed details. Restart and verify persistence.
   Run `next INDEX P3`, confirm, and verify current `P3`, history `[P1, P2]`, and preserved other fields.
   Reject any requested date at or before the current visit, even if it is later than some existing history entries.
   Reset the disposable data and repeat with seven filler patients on `P2`'s day, distinct from `P1`'s day;
   expect both warnings in a single prompt and a single confirmation. When `P1` and `P2` are on the same day,
   expect only the time warning.

Fixed-clock automated tests cover exact equality to now, nanosecond boundaries, one initial clock read, and
appointments becoming past as time advances, including while confirmation is pending. These boundaries are not
reliably reproducible by typing against the system clock.

### Testing the prev command

Restore the preserved **rollback starting state** from the persistence checkpoint in
[Testing the next command](#testing-the-next-command) before testing `prev`:

1. Close Doc if it is running.
1. Copy the preserved `rollback-start-addressbook.json` over the disposable scratch folder's `data/addressbook.json`,
   replacing the active file and retaining the filename `addressbook.json`.
1. Relaunch Doc from the same scratch folder. Find and view Nextalpha and Nextbeta to verify the starting records.

Do not continue from either independent capacity case. The restored state should have Nextalpha
at `28/10/2026 0900` (`F2`) with history `[25/10/2026 0900, 25/10/2026 1000]` (`[P1, P2]`), and Nextbeta at
`27/10/2026 0900` (`F1`) with history `[25/10/2026 1100]` (`[P3]`). Restore this state before repeating an
independent rollback variation. On another execution date, substitute the full datetime values from the common fixture
table. Choose another globally unused future datetime for `F4`.
Keep using the preserved filtered or sorted context after each successful mutation, accounting for any natural move
or disappearance caused by the updated visit date.
Record `all/Penicillin; latex alc/Does not drink smk/former` before rollback and check that all three fields
remain unchanged after each `prev`, including after restarting Doc.
For every valid `prev`, verify that the first command only shows the confirmation prompt and leaves the record,
saved JSON, displayed context, and details panel unchanged. Enter `y` (or `Y` with surrounding spaces) to complete
the rollback; enter any other response to cancel. A cancellation response is consumed and must not execute as a
command.

1. Run `find n/Nextalpha`, then `view 1`. Try `prev`, `prev 0`, `prev 2147483648`, `prev 1 extra`, and `prev 2`.<br>
   Expect `Invalid command format!` with usage for `prev`, `Index must be a positive integer.` with usage for
   `prev 0` and `prev 1 extra`, `Index is too large.` with guidance and usage for `prev 2147483648`,
   and the invalid-index error for `prev 2`. Verify that patient data, the saved JSON, filtered list, and
   displayed details remain unchanged.
1. Run `edit 1 vd/30/10/2026 0900 p/84567890 e/updated@example.com a/30 Updated Road all/Penicillin; latex alc/Does not drink smk/former note/Updated before rollback t/followup dob/15/6/1990 bt/A+ ec/Mary Tan, 91234567`,
   then `prev 1` and `y` without finding Nextalpha again.<br>
   Expect the prompt before confirmation and success even though the replacement datetime is in the future. Verify that feedback identifies `P2` as restored and the replacement datetime
   as discarded, including the time for both. Find and view the patient again: current is `P2`, previous
   dates are `[P1]`, and the updated phone, email, address, note, tags, date of birth, blood type, and emergency
   contact remain. The replaced current datetime `F4` is discarded rather than restored or appended to previous dates.
   Close and reopen Doc; verify that this state was saved.
1. Run `find n/Nextalpha`, then `next 1 28/10/2026 0900` (the illustration's `F2`).<br>
   Expect current `F2` and previous dates `[P1, P2]`. Rollback did not retain either discarded future datetime.
1. Run `find n/Nextalpha Nextbeta`, then `sort date 1/1/2000`.<br>
   Expect Nextbeta at `F1` to be index 1 and Nextalpha at `F2` to be index 2. Run `view 2`, then `prev 2`, and `y`.
   Expect the prompt to identify Nextalpha and then the patient to move to index 1 as the preserved date sort
   reevaluates its restored `P2` visit. The right-hand details refresh automatically: current is `P2`, previous dates
   are `[P1]`.
   Find and view Nextbeta separately: its record remains unchanged at `F1` with previous dates `[P3]`.
   Run `find n/Nextalpha` and `view 1` before continuing.
1. With Nextalpha at `P2` and previous dates `[P1]`, run `find n/Nextalpha`, then start a fresh `prev 1` request
   and respond separately with `n`, an empty response, and `yes` in independent repetitions.<br>
   Expect `Visit restoration cancelled.` each time, with no model, saved-file, filter, sort, or details change. The
   response is consumed and must not execute as a command.
1. Repeat the cancellation case with `prev 1` followed by `list`. The `list` response is consumed; submit `list`
   again to execute it normally, then re-find and view Nextalpha before continuing.
1. Run `prev 1` while only Nextalpha is displayed, then enter `y`.<br>
   Find and view it again: current is `P1`, previous dates are empty, and the note remains updated. Close and reopen
   Doc and verify that this state persists.
1. Run `find n/Nextalpha Nextbeta`, then `sort date`, then `view 2` and `prev 2`.
   Expect future Nextbeta first and past Nextalpha second before the rejected rollback.
   Expect `Patient Nextalpha Patient has no previous visit dates to restore.` Verify that the record, saved file,
   filtered list, sorted order, and details are unchanged. There is no redo command.

### Viewing one patient

1. Run `list`, followed by `view 1`. Expect the full list to remain on the left and the first patient's complete
   details to appear on the right.
1. Run `list`, followed by `find n/George`, and then `view 1`. Expect the search results to remain on the left and the
   first patient's complete details to appear on the right.
1. Run `sort date`, followed by `view 1`. Expect the sorted list to remain on the left and the first patient's complete
   details to appear on the right.
1. View a newly added patient with no previous dates. Expect `Previous visit dates:` immediately below `Visit date`,
   followed by `No previous visit dates`, with the note below the previous-date section.
1. View the patient from [Testing the next command](#testing-the-next-command) with previous dates `[P1, P2]`.
   Expect one full datetime per line, with `P2` above `P1` and the time included for both. View the empty-record
   patient again; expect the empty-state text to replace the other patient's dates.
1. With Doc closed, extend a disposable patient's JSON `previousVisitDates` array with hundreds of globally unused
   datetimes in strictly increasing order, all earlier than that patient's current datetime. Reopen and view the
   patient. Expect the newest previous date at the top. Scroll the details panel to the bottom; expect every entry
   to be accessible, including the oldest date and the note and tags below it. Repeat with a long note and a narrower
   window to check wrapping and scrolling.
1. Record the data file contents, run `view` repeatedly for patients with empty and populated previous dates, and
   close Doc. Expect both the saved dates and their order to remain unchanged.
1. Run `view 999`. Expect an invalid-index error, no change to the displayed list, and no changes to stored patient
   data.
1. Run `view 0`, `view -1`, `view abc`, and `view 99999999999`. Expect `Index must be a positive integer.` with
   usage for the first three and `Index is too large.` with guidance and usage for the last one.
1. Close and reopen the application. Expect all patient data to remain unchanged.

### Calendar labels for current visits

Use disposable data and a fresh patient with no previous dates. Choose globally unused datetimes named
`YESTERDAY`, `TODAY_EARLY`, `TODAY_LATE`, and `TOMORROW`, using your computer's current local calendar date.
For today's values, use times near the start and end of the day, such as `0000` and `2359`.
Replace the placeholders with full `d/M/yyyy HHmm` datetimes before submitting commands.

1. Run these commands:

   ```text
   add n/Calendarcheck Patient p/83456789 a/30 Test Road vd/YESTERDAY
   find n/Calendarcheck
   view 1
   ```

   Expect the full current datetime followed by `(Past)`, with `No previous visit dates` below it.
1. For each value below, run `edit 1 vd/VALUE`, then `find n/Calendarcheck` and `view 1`.

   | Value | Expected label beside the current datetime |
   | --- | --- |
   | `TODAY_EARLY` | `Today`, even if the time has already passed. |
   | `TODAY_LATE` | `Today`, including when the time is still in the future. |
   | `TOMORROW` | `Upcoming`. |

   The successful edit preserves the one-patient filter and refreshes the details automatically. Verify that the label
   does not appear beside previous dates or in JSON.
1. While only Calendarcheck is displayed, run `sort date 1/1/2000`, then `view 1`.<br>
   Expect the label to remain based on today's local date, independently of the chosen sort cutoff.
1. Close and reopen Doc, run `find n/Calendarcheck`, and view it again.<br>
   Expect the saved datetime to remain unchanged and the label to reflect the local date when viewed.

Fixed-clock tests cover the exact midnight and year boundary and time-zone differences. Do not change the OS clock
or wait for midnight to exercise those cases manually. An already displayed label does not refresh automatically
when midnight passes; another `view` recalculates it.

### Navigating command history

1. Recalling submitted commands

   1. Restart the application. Enter `list`, then enter `find n/George`.<br>
      Expected: Both commands run normally.

   1. Press the Up arrow key once, then again.<br>
      Expected: The command box shows `find n/George`, then `list`. The caret is after the recalled text. Further Up arrow
      presses leave `list` displayed.

   1. Press the Down arrow key twice.<br>
      Expected: The command box shows `find n/George`, then becomes empty. Further Down arrow presses leave it empty.

1. Restoring unfinished text

   1. Type `find n/Geo` without pressing Enter, press the Up arrow key, then press the Down arrow key.<br>
      Expected: A previous command is recalled, then the unfinished text `find n/Geo` is restored exactly.

1. Recording command text

   1. Submit an invalid command containing leading or trailing spaces, then press the Up arrow key.<br>
      Expected: The invalid command is recalled with its original spaces.

   1. Restart the application. Submit `list`, then submit `find n/George` twice. Press the Up arrow key twice.<br>
      Expected: The command box shows `find n/George`, then `list`. The consecutive duplicate occupies one history
      position.

   1. Restart the application. Submit `list`, `find n/George`, then `list`. Press the Up arrow key three times.<br>
      Expected: The command box shows `list`, `find n/George`, then `list`. Identical commands separated by another
      command occupy separate history positions.

1. Excluding follow-up responses

   1. Enter `delete 1`, then `n`. Press the Up arrow key once.<br>
      Expected: `delete 1` is recalled. The confirmation response `n` is not recalled.

   1. Restart the application and press the Up arrow key before entering any command.<br>
      Expected: The command box remains unchanged because command history is not persisted.

### Deleting a patient

1. Deleting a patient while all patients are being shown

   1. Prerequisites: List all patients using the `list` command, with multiple patients in the list.

   1. Test case: `delete 1`<br>
      Expected: No patient is deleted. The result display shows
      `Delete NAME? Type y to confirm. Any other input cancels.` using the first patient's name.

   1. Test case: `y` immediately after the previous test case<br>
      Expected: The first patient is deleted from the list. The result display shows the deleted patient's details.

   1. Test case: `delete 1`, followed by `n`<br>
      Expected: No patient is deleted. The result display shows `Deletion cancelled.`

   1. Test case: `delete 1`, followed by `list`<br>
      Expected: No patient is deleted and `list` is consumed as the confirmation response. The result display shows
      `Deletion cancelled.` Entering `list` again lists all patients normally.

   1. Test case: `delete 1`, followed by pressing Enter with an empty command box<br>
      Expected: No patient is deleted. The result display shows `Deletion cancelled.`

   1. Test case: `delete 0`<br>
      Expected: No patient is deleted. The status message shows `Index must be a positive integer.` with usage.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: The matching index error is shown and no confirmation is requested.

### Clearing all patient records

1. Clearing a non-empty patient list

   1. Prerequisites: Doc contains multiple patient records.

   1. Test case: `clear`<br>
      Expected: No patient record is deleted. The result display shows
      `Clear COUNT entries? Type y to confirm. Any other input cancels.` with the current count.

   1. Test case: `Y` immediately after the previous test case<br>
      Expected: All patient records are deleted and the result display shows `Address book has been cleared!`.

1. Cancelling a clear operation

   1. Prerequisites: Doc contains at least one patient record.

   1. Test case: `clear`, followed by any input other than `y`, such as `list`<br>
      Expected: No patient record is deleted, the second input is not executed as a command, and the result display shows
      `Clear cancelled.`

1. Clearing an empty patient list

   1. Prerequisites: Doc contains no patient records.

   1. Test case: `clear`<br>
      Expected: No confirmation is requested and the result display shows `Address book is already empty.`

1. _{ more test cases … }_

### Saving data

1. Dealing with missing/corrupted data files

   1. Back up `data/addressbook.json`, then edit two otherwise valid patient records to use the same `visitDate`.

   1. Relaunch Doc.<br>
      Expected: Doc reports that the data file contains duplicate visit dates and times, retains the invalid file, and
      starts with an empty patient list under the existing invalid-data behavior.

   1. Restore the backup, assign distinct visit dates and times, and relaunch Doc.<br>
      Expected: Every patient record loads normally.

1. _{ more test cases … }_
