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

`MainApp` passes `HostServices.showDocument` through `UiManager` and `MainWindow` to `HelpWindow` as a browser-opening
action. `HelpWindow` also accepts a replaceable clipboard action for isolated tests. It logs detectable failures
and displays feedback without disabling help. Browser feedback confirms only a launch request, not page loading;
clipboard success requires `Clipboard.setContent` to return `true`. No network preflight is performed.

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

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
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
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

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

Editing a patient or copying it with `PersonBuilder` preserves previous dates. `next` appends the current datetime
to the previous-date list before setting a new current datetime. `prev` removes the latest datetime from that list
and uses it as the current datetime, discarding the former current datetime. A replacement current date that would
invalidate the record produces a command error before model or list changes.

`JsonAdaptedPerson` writes `previousVisitDates` as an ordered array of visit-date strings. Missing or JSON-null
arrays load as empty lists for compatibility with older files. Non-arrays, null or malformed entries, invalid
chronology, and duplicates within a record are rejected through the existing invalid-data path. The required
`visitDate` field remains required. No entries are sorted, deduplicated, or dropped to repair an invalid file.

Previous dates are persisted and shown by `view INDEX`, with no arbitrary limit on their number.

`PatientDetailsPanel` retains the current visit datetime and adds `Previous visit dates:` immediately below it,
followed by the note. It formats the immutable previous dates with `VisitDate#toString`, one full datetime per line
newest first, or shows `No previous visit dates` for an empty record. The label keeps its preferred height inside
the existing `ScrollPane`, so long records remain accessible by scrolling. Rendering preserves the patient's dates
and their oldest-first order in the model and JSON file.
Run `view` again after a mutation to refresh the details.

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
check. Phone comparison continues to use exact `Phone` value equality.

Visit dates and times form a separate invariant across all current and previous dates.
`Person#hasCommonVisitDate` compares the complete records using parsed `VisitDate` equality.
`UniquePersonList#containsSameVisitDate` checks the full stored collection, including patients hidden by a filter.
`add`, `setPerson`, and `setPersons` throw `DuplicateVisitDateException` rather than allowing two records to use
the same value. This covers command mutations,
model replacement, sample data, and direct model use. `JsonSerializableAddressBook` performs the same check while
loading and reports an invalid data file with a visit-date-specific message.

`Model#hasPersonWithSameVisitDate` exposes the scheduling check. `AddCommand` rejects a true duplicate first, then a
visit-date conflict. For a name duplicate with an available visit time, it captures the proposed immutable `Person` in
a `CommandInputRequest#createConfirmation` action. Confirmation rechecks both rejection rules before adding, which
prevents a conflicting record from being introduced if model state changes while the prompt is pending.

`EditCommand` similarly rejects a true duplicate or a datetime shared with another patient's current or previous dates
before requesting confirmation. Replacement checks compare the complete candidate while excluding the original
target. This permits unchanged dates and moving a patient's own dates between current and previous fields.
The command asks for confirmation only when the requested edit changes the name to one already in use; editing
other fields of a patient whose name is already shared does not prompt again. It captures the original and edited
`Person` objects, then verifies on confirmation that the original record still exists and that the edit still creates
neither a true duplicate nor a visit-date conflict.

#### Duplicate-detection limitations

Duplicate detection is a heuristic because Doc does not assign a permanent patient identifier:

* Two different patients with the same normalized name and phone number cannot both be stored, such as family members
  who share a name and contact number. A distinguishing name component or different contact number is required.
* A returning patient with both a changed name and changed phone number is not detected as a possible duplicate.
* Normalization does not understand nicknames, reordered names, misspellings, transliterations, or equivalent accented
  forms. It performs no fuzzy or culture-specific matching.
* Phone numbers are compared exactly. Equivalent numbers entered with different country-code forms are not detected as
  the same number.
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

### Editing patient details

`EditCommandParser` accepts optional replacement fields, including `vd/` and `note/`. The displayed format is
`edit INDEX [n/NAME] [p/PHONE] [a/ADDRESS] [vd/DATE_TIME] [e/EMAIL] [note/NOTES] [t/TAG]...`.
It checks index syntax and duplicate single-valued prefixes before parsing field values, then rejects a
descriptor with no supplied fields. When `vd/` is supplied, `ParserUtil#parseVisitDate` reuses the existing strict
`VisitDate` validation
for `d/M/yyyy HHmm`; valid past dates are accepted, while an empty `vd/` is rejected.

`EditPersonDescriptor` stores the supplied visit date and time and note along with the optional replacement fields.
`EditCommand` copies the descriptor, resolves the patient using the displayed list, and replaces the immutable
`Person`. Omitted fields, including the visit date/time and notes, are preserved. An explicitly empty
email, note, or tag field clears its existing value and counts as a supplied field.

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

The existing tokenizer uses `String#trim()` on outer whitespace, preserves internal spaces, and recognizes
prefixes preceded by a literal space. A recognized prefix inside notes delimits another field; there is no
escaping or extra note validation. Repeated `note/` prefixes are rejected before field validation, including identical or empty
values. Repeated `t/TAG` prefixes retain their existing replacement and clearing behavior.

A successful edit restores the full displayed list in stored order and uses the existing logic/storage path
to save the updated record, including empty notes, in the existing JSON format. The existing patient card displays
the replacement visit date/time and notes. Editing replaces one stored visit date/time and one stored note;
it does not append visit-history entries or per-visit notes.

### Scheduling the next visit

`next INDEX DATETIME` retains the current datetime and schedules the following appointment for a displayed patient.
Corrections use `edit ... vd/`, which preserves the previous dates unchanged. See the
[User Guide](UserGuide.md#scheduling-the-next-visit-next) for syntax and errors.

For example, assume the clock reads `31/10/2026 1200`, Betsy is displayed at index 1, and the requested slot is unused.
The two commands below are alternatives applied to the same initial record:

| Operation | Current datetime | Previous datetimes |
| --- | --- | --- |
| Initial record | `30/10/2026 1000` | `[10/10/2026 1000]` |
| `next 1 1/11/2026 0930` | `1/11/2026 0930` | `[10/10/2026 1000, 30/10/2026 1000]` |
| `edit 1 vd/1/11/2026 0930` | `1/11/2026 0930` | `[10/10/2026 1000]` |

The command follows the existing parser, model, and storage flow:

1. `NextCommandParser` parses the displayed index and datetime, then passes its injected clock to `NextCommand`.
1. `NextCommand` selects the displayed patient and rejects an unchanged requested datetime. It reads the clock once
   and requires `current <= now < requested`, retaining seconds and nanoseconds for both comparisons.
1. It constructs a replacement `Person`, appending the old current datetime and preserving every other field.
   Before mutation, it compares the candidate's complete visit record against every other stored patient,
   including hidden patients and excluding the original record. The
   [visit-time conflict limitations](#visit-time-conflict-limitations) apply.
1. On success, it replaces the patient and restores the full list in stored order, clearing the filter and sort.
   `LogicManager` saves through the existing storage flow before returning the result to the UI.

A future current visit is rejected with advice to use `edit`. A requested datetime at or before now is rejected
with the captured local time formatted as `d/M/yyyy HHmm`, without seconds. An immediate second `next` is rejected
because the new current visit is still in the future. Details do not refresh automatically; users run `view` again
using the patient's newly displayed index.

Validation failures stop before any mutation or save. A storage I/O failure happens after the in-memory update;
the existing error handling reports it without rolling back the record or list. If no further visit is planned,
the current datetime remains stored as it becomes past. There is no attendance or completion state to update.

The design keeps three decisions explicit:

* **Separate advancing from correction.** `next` retains the old current datetime; `edit` replaces it without
  changing previous dates. The user chooses when to advance after handling a visit.
* **Keep clock validation in the command.** Keeping eligibility outside `VisitDate` and storage allows past
  appointments to remain valid for corrections and loading. Existing record chronology and `current <= now < requested`
  ensure that appending the current date preserves strict order before the new current date.
* **Inject the clock.** Production uses the system's local time zone and reads time once at execution without
  truncating seconds. Fixed clocks make boundary and persistence tests deterministic without changing global time.

Tests cover parsing, displayed indices, hidden current and previous-date conflicts, appending to empty and populated
records, field preservation, list reset, exact clock boundaries, and persistence. They also cover repeated advancement
after an intervening edit, unchanged saved data after rejection, and saved appointments becoming past as the test
clock advances. Manual cases appear in the
[testing appendix](#testing-the-next-command).

### Restoring the previous visit

`prev INDEX` restores the latest previous datetime and discards the former current datetime, without a redo.
It preserves every other patient field, including edits made since advancing. See the
[User Guide](UserGuide.md#restoring-the-previous-visit-prev) for syntax and an example.

1. `PrevCommandParser` uses `ParserUtil#parseIndex` to require a single positive displayed index and wraps parsing
   errors with Prev usage, preserving their exception cause.
1. `PrevCommand` selects the displayed patient and rejects an invalid index or empty previous-date list before
   changing model or list state.
1. The private rollback helper asserts that previous dates are nonempty after command validation. It copies the
   immutable previous dates, removes the last entry, and constructs a replacement `Person` using that
   datetime as current. All other fields come from the selected patient's latest record. The constructor still
   enforces chronology. The replacement uses only dates from the original record, so it cannot introduce a new
   cross-patient datetime conflict; the model also retains its replacement checks.
1. On success, it replaces the patient and restores the full list in stored order, clearing filtering and sorting.
   The normal `CommandResult` reports the restored and discarded datetimes on separate lines and requests saving
   through `LogicManager`. Details refresh when the user runs `view` using the patient's newly displayed index.

Rollback has no clock eligibility check, including when the restored datetime is still in the future. It does not
request confirmation. Repeated rollback consumes the list until it is empty. A later `next` applies its usual
eligibility checks to the restored current datetime and appends that datetime on success.
The helper assertion documents an internal precondition; empty-history user input is rejected with a command
exception regardless of whether Java assertions are enabled.

Validation failures do not save or mutate data or list context. Storage I/O failures retain the existing error
handling: a failed save can leave the in-memory rollback applied, without automatic restoration.

Tests cover one, several, and no previous dates, future dates, displayed indices, preservation of other fields after
edits, repeated rollback, subsequent advancement, released datetime slots, persistence across reloads, and rejected
commands leaving saved data unchanged. Manual cases appear under
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

For same-name additions and edits, `AddCommand` and `EditCommand` use the same confirmation helper as destructive
commands. The prompt does not mutate or save patient data. Confirmation performs the captured mutation and saves
once; every other response cancels without saving. The proposed patient is captured before the prompt, and confirmed
actions recheck both the true-duplicate and unique-visit-date invariants before modifying the model.

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
| `* * *`  | doctor                  | record that I visited a patient on a given date                           | know when I last saw each patient                                                       |
| `* * *`  | doctor                  | schedule a patient's following visit                                     | plan their follow-up care                                                             |
| `* * *`  | doctor                  | find patients whose next visit date is today                              | be reminded who is due for a visit                                                      |
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
* Rejected input leaves patient records and the displayed list unchanged.

**MSS**

1.  Doctor requests a future appointment for a patient whose current visit has arrived or passed, identified by
    their displayed index.
2.  Doc retains the current datetime in the previous dates, saves the new appointment, and shows the full caseload
    in stored order.

    Use case ends.

**Extensions**

* 1a. The displayed index does not identify a patient.

    * 1a1. Doc explains that the selection is invalid.

      Use case resumes at step 1.

* 1b. The current visit is still in the future, or the requested appointment is invalid, not in the future, or unchanged.

    * 1b1. Doc explains the specific reason for rejection.

      Use case resumes at step 1.

* 1c. Another patient's current or previous visit already uses the requested datetime.

    * 1c1. Doc identifies the conflicting patient and time.

      Use case resumes at step 1.

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should work without requiring an installer, and without the user having to install any software other than Java.
3.  Should work fully offline, with no dependency on a remote server or an internet connection, so that it remains usable in homes with poor reception.
4. Should support at least 1,000 patient records, with up to 100 visit notes per record.
5. Should respond to each valid command within 2 seconds when operating on
   1,000 patient records with up to 100 visit notes each. This excludes the
   time spent entering the command.
6. For common tasks such as adding, finding, listing, editing, and archiving
   patients, a user who types at above-average speed should be able to complete
   the task faster through the CLI than by using the mouse.
7.  A doctor who has not used a CLI application before should be able to add a patient and look up that patient's record within 15 minutes of reading the User Guide.
8.  Should save every change to the local data file before accepting the next command, so that at most one command's worth of data is lost if the app terminates unexpectedly.
9.  Should store all patient data only on the user's own machine, so that no patient data leaves the doctor's computer.
10. Should start up and be ready to accept a command within 5 seconds on a typical modern laptop.
11. Should not crash or lose existing records if the data file has been manually edited into an invalid state; it should instead report the problem and start with an empty caseload.
12. Should be delivered as a single JAR file of no more than 100MB, so that it can be copied onto a laptop taken on house visits.

### Glossary

* **Active patient record**: A patient record included in the doctor's current caseload and visit planning.
* **Archived patient record**: A patient record retained for future reference but excluded from the active caseload and visit planning.
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
* **Visit**: A single house call. Doc stores one current datetime, ordered previous datetimes, and one patient-level note.
* **Visit record**: A patient's current and previous visit datetimes. These record scheduled times retained by the
  user and do not verify attendance or completion.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

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

### Help window lifecycle

1. With no follow-up prompt pending, enter `help`, then repeat with `help add` and `help 123`.
   Expect the same non-modal Help window each time, showing the User Guide URL and working Open User Guide,
   Copy URL, and Close controls.
   Move and resize Help, then request it again; expect its position and size to remain unchanged.
1. Use Help's native minimize control. Expect Help to minimize independently while the main window stays usable.
   Request Help again using `help`, F1, and **Help → Help**, minimizing it before each attempt.
   Expect the existing window to restore and receive focus rather than remain minimized or create another window.
1. Type `find Be` without submitting it. Press F1 with the command box focused, then close Help using Close.
   Expect focus to return to the command box with `find Be` unchanged and unsubmitted. Repeat using Escape and the
   native window close control, reopening Help each time. Repeat after selecting part of the draft; expect the caret
   and selection to return to their earlier positions.
1. Focus the result display and press F1. Expect Help to open. Dismiss it; expect command entry to receive focus.
   Also verify that Tab moves between Help's buttons, that focused buttons have a visible outline, and that Space
   activates Close without submitting text in the main window.
1. With a disposable patient data copy, enter `delete 1`, then type `y` without submitting it.
   Open Help using F1 and the Help menu, dismissing it after each attempt. Expect the confirmation prompt and `y`
   draft to remain unchanged, with no deletion or data-file write. Enter `n` to cancel.
1. Request `delete 1` again, then enter `help`. Expect deletion to be cancelled without opening Help: the typed
   input remains a confirmation response rather than bypassing the pending-input flow.
1. Open Help and exit Doc using `exit`, **File → Exit**, and the main window's native close control, restarting
   between attempts. Repeat with Help minimized. Expect both windows to close with no orphan Help window.
1. Repeat the relevant flows at 1920×1080 with 100% and 125% OS scaling, and at 1280×720 with 150% OS scaling.
   Check that Help's guide text and buttons remain accessible after resizing. Automated scene-layout checks do not
   replace these OS-level checks.

### User Guide actions

1. Open Help and choose **Open User Guide**. Expect a request to the default browser for the displayed URL and
   feedback saying that the launch was requested. Separately check whether a browser actually opens and loads the
   guide; Doc cannot confirm that. Repeat using Tab and Space (or Enter where the platform supports it).
1. Choose **Copy URL**, then paste into a disposable text field. Expect the displayed URL and copy-success feedback
   only when the clipboard accepts it. Do not confuse this with confirmation that the guide is reachable.
1. If the browser does not appear, use **Copy URL** and open the address manually. With networking unavailable,
   expect the help window and its URL to remain usable; page-loading errors belong to the browser.
1. Use `HelpWindowActionsTest` to simulate a browser exception, clipboard rejection, and clipboard exception without
   launching a browser or modifying the system clipboard. Expect specific fallback feedback, a readable URL,
   working alternative controls, successful retries, and a working Close button after each recoverable failure.

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
   Expect that displayed patient to be edited, followed by the full list in stored order.
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

Use a disposable data copy and fresh test names. Choose globally unused datetimes satisfying
`P1 < P2 < actual now < F1 < F2 < F3`. Choose another unused past datetime `P3` for Nextbeta.
Replace each placeholder with a full `d/M/yyyy HHmm` datetime before submitting commands. This permits repeated
successful advances in one session without waiting or changing the system clock.

1. Add the two patients in this order:

   ```text
   add n/Nextalpha Patient p/81234567 a/10 Test Road vd/P1 e/alpha@example.com note/Keep this note t/priority
   add n/Nextbeta Patient p/82345678 a/20 Test Road vd/F3
   ```

   Expect success, with nonempty optional fields for Nextalpha and empty email, notes, and tags for Nextbeta.

1. Run `find Nextalpha`, then `next 1 F3`.<br>
   Expect a conflict naming hidden Nextbeta and the occupied time, with data and the filtered list unchanged.
   Repeat with a leading-zero day or month when applicable; expect the same conflict.

1. While only Nextalpha is displayed, try each of these commands:

   | Command | Expected feedback |
   | --- | --- |
   | `next` | Next usage. |
   | `next 1` | Missing arguments and usage. |
   | `next 0 F1` | Invalid index syntax and usage. |
   | `next 2147483648 F1` | Oversized index error and usage. |
   | `next 2 F1` | Index outside the displayed list. |
   | `next 1 P1` | Unchanged appointment. |
   | `next 1 P2` | Non-future appointment. |
   | `next 1 31/2/2027 0930` | Invalid date. |
   | `next 1 1/11/2026 2400` | Invalid time. |
   | `next 1 vd/F1` | Prefix misuse and usage. |
   | `next 1 F1 extra` | Extra arguments and usage. |

   Each rejection must preserve records, file contents, and the displayed list. Also test today's current local
   minute; expect rejection. Repeat the unchanged-date test with equivalent leading zeros when applicable.

1. Run `find Nextalpha Nextbeta`, then `sort date`.<br>
   Expect Nextalpha first. Try `next 1 F3`; expect rejection with the sorted order preserved.
   Run `view 1`, then `next 1 F1`; expect Nextalpha's current datetime to become `F1`, retaining all other fields,
   and the full list to return in stored order. Run `find Nextalpha`, then `view 1`, to refresh its details.
   Close Doc and inspect the JSON: expect current `F1` and previous dates `[P1]`.

1. Reopen Doc, run `find Nextalpha`, then immediately try `next 1 F2`.<br>
   Expect the future-current-visit error advising `edit`, with record, saved file, and list unchanged.
   Run `edit 1 vd/P2`, then `next   1   F2`. Expect repeated spaces to be accepted and the full list to return.
   Inspect the JSON after closing Doc: expect current `F2` and previous dates `[P1, P2]`, in that order.
   The edit does not append the replaced `F1`.

1. Reopen Doc, run `find Nextbeta`, then `edit 1 vd/P3`. Run `find Nextbeta` again before `next 1 F1`, since the
   successful edit resets the list.<br>
   Expect Nextbeta to change, retaining empty email, notes, and tags. Its previous dates contain `[P3]`.

1. Close and reopen the application. Find and view each test patient.<br>
   Expect Nextalpha at `F2` with saved previous dates `[P1, P2]` and `P2` displayed above `P1`. Expect Nextbeta at
   `F1` with previous dates `[P3]` and every other field preserved. Check the previous-date section below the current
   datetime and the persisted JSON.
   Use the newly displayed indices after each successful mutation.

1. Repeat the conflict case using another patient's previous datetime, loaded through the disposable-file setup in
   [Loading and validating previous visit dates](#loading-and-validating-previous-visit-dates). Choose a future
   previous datetime and a later current datetime for that other patient, and an eligible current datetime for the
   selected patient. Expect rejection without mutation or saving, including when the other patient is hidden.

Fixed-clock automated tests cover exact equality to now, nanosecond boundaries, one clock read, and saved appointments
becoming past as time advances. These boundaries are not reliably reproducible by typing against the system clock.

### Testing the prev command

Use disposable data. Start with Nextalpha from [Testing the next command](#testing-the-next-command), whose current
datetime is `F2` and whose previous dates are `[P1, P2]`, with `P1 < P2 < actual now < F1 < F2`.
Choose another globally unused future datetime `F4` for an intervening edit.
Find the patient again after each successful mutation, since success restores the full list in stored order.

1. Run `find Nextalpha`, then `view 1`. Try `prev`, `prev 0`, `prev 2147483648`, `prev 1 extra`, and `prev 2`.<br>
   Expect usage for malformed arguments and the invalid-index error for `prev 2`. Verify that patient data, the
   saved JSON, filtered list, and displayed details remain unchanged.
1. Run `edit 1 vd/F4 note/Updated before rollback`, then `find Nextalpha` and `prev 1`.<br>
   Expect success even though `F4` is in the future. Verify that feedback identifies `P2` as restored and `F4`
   as discarded, including the time for both. Find and view the patient again: current is `P2`, previous
   dates are `[P1]`, and the updated note and all other fields remain. The replaced current datetime `F4` is discarded.
   Close and reopen Doc; verify that this state was saved.
1. Run `find Nextalpha`, then `next 1 F2`.<br>
   Expect current `F2` and previous dates `[P1, P2]`. Rollback did not retain either discarded future datetime.
1. Run `find Nextalpha`, then `sort date`, then `prev 1`.<br>
   Expect the full list in stored order. Find and view Nextalpha again: current is `P2`, previous dates are `[P1]`.
1. Run `prev 1` while only Nextalpha is displayed.<br>
   Find and view it again: current is `P1`, previous dates are empty, and the note remains updated. Close and reopen
   Doc and verify that this state persists.
1. Run `find Nextalpha`, then `prev 1` again.<br>
   Expect `Patient Nextalpha Patient has no previous visit dates to restore.` Verify that the record, saved file,
   filtered list, and details are unchanged. There is no redo command.

### Viewing one patient

1. Run `list`, followed by `view 1`. Expect the full list to remain on the left and the first patient's complete
   details to appear on the right.
1. Run `list`, followed by `find Alex`, and then `view 1`. Expect the search results to remain on the left and the
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
1. Close and reopen the application. Expect all patient data to remain unchanged.

### Navigating command history

1. Recalling submitted commands

   1. Restart the application. Enter `list`, then enter `find Alex`.<br>
      Expected: Both commands run normally.

   1. Press the Up arrow key once, then again.<br>
      Expected: The command box shows `find Alex`, then `list`. The caret is after the recalled text. Further Up arrow
      presses leave `list` displayed.

   1. Press the Down arrow key twice.<br>
      Expected: The command box shows `find Alex`, then becomes empty. Further Down arrow presses leave it empty.

1. Restoring unfinished text

   1. Type `find Be` without pressing Enter, press the Up arrow key, then press the Down arrow key.<br>
      Expected: A previous command is recalled, then the unfinished text `find Be` is restored exactly.

1. Recording command text

   1. Submit an invalid command containing leading or trailing spaces, then press the Up arrow key.<br>
      Expected: The invalid command is recalled with its original spaces.

   1. Restart the application. Submit `list`, then submit `find Alex` twice. Press the Up arrow key twice.<br>
      Expected: The command box shows `find Alex`, then `list`. The consecutive duplicate occupies one history
      position.

   1. Restart the application. Submit `list`, `find Alex`, then `list`. Press the Up arrow key three times.<br>
      Expected: The command box shows `list`, `find Alex`, then `list`. Identical commands separated by another
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
      Expected: No patient is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: The existing invalid-command response is shown and no confirmation is requested.

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
