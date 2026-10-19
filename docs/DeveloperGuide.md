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

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Optional field visibility in patient details

`PatientDetailsPanel#setPerson` updates the patient's required fields and uses `setOptionalField` for email, notes,
formatted tags, and emergency contact. A nonempty value shows its label and value. An empty value clears the label
text and sets both `visible` and `managed` to false, so the `VBox` does not reserve space for the hidden row.
Each update also restores both properties for populated fields, preventing stale visibility when switching patients.
This changes only how a supplied patient is displayed; refreshing the panel automatically after edits is separate.

`PatientDetailsPanelTest` covers all-empty records, populated-to-empty-to-populated transitions, mixed optional
fields, and required-field visibility. To verify manually, view a patient with email, notes, and tags, then clear
these with `edit INDEX e/ note/ t/` and run `view INDEX` again. Expect the empty rows and labels to disappear.
Add the details back and view the patient again; expect the populated rows to return. View another patient without
an emergency contact and expect that row to be hidden too. Required fields should remain visible throughout.

### Optional emergency contacts

`Person` stores an immutable `EmergencyContact` value. Its empty string represents an absent contact, so
existing constructors and records remain compatible. Nonempty values contain a name and phone number separated
by one comma, validated using the existing `Name` and `Phone` rules. Contact details do not affect patient identity.

`AddCommandParser` and `EditCommandParser` recognize `ec/NAME, PHONE_NUMBER` and reject empty, incomplete,
invalid, or repeated contact fields before execution. `EditPersonDescriptor` distinguishes an omitted contact
from a replacement. `EditCommand` and `NextCommand` preserve contacts when updating unrelated fields.

`JsonAdaptedPerson` saves the canonical `NAME, PHONE_NUMBER` string and maps missing or null contact fields from
older records to an absent contact. Invalid nonempty stored contacts cause a data-loading error.
`PatientDetailsPanel` shows `Emergency Contact : PHONE_NUMBER (NAME)` immediately below the patient's phone
number and hides the row for absent contacts.

#### Representation and validation

The model uses an empty `EmergencyContact` value rather than a null field. This preserves the non-null invariant
of `Person` and allows callers of the existing constructor to create patients without contacts. A supplied `ec/`
must nevertheless be nonempty: `ParserUtil#parseEmergencyContact` rejects blank input before constructing the value.
There is no contact-removal command or empty-prefix clearing behavior.

Validation requires exactly two comma-separated details. The name must satisfy `Name#isValidName`, and the phone
must satisfy `Phone#isValidPhone`. The constructor trims surrounding whitespace from each detail and stores the
canonical form `NAME, PHONE_NUMBER`. `EmergencyContact#toDisplayString` produces `PHONE_NUMBER (NAME)` for the UI.
`Person#equals` and `Person#hashCode` include the contact; `Person#isSamePerson` continues to use only the patient's
normalized name and phone. Sharing an emergency contact does not make two patients duplicates.

#### Automated verification

* `EmergencyContactTest` covers valid, absent, malformed, and null values, formatting, equality, and patient identity.
* `EmergencyContactIntegrationTest` exercises parsing, execution, viewing, and storage through `LogicManager`.
  It checks adding and replacing contacts, field order, whitespace, duplicate prefixes, invalid indices,
  filtered-list selection, and preservation during unrelated commands. Rejected inputs must leave both the model
  and saved file unchanged.
* `JsonAdaptedPersonTest` checks missing contact fields and invalid stored contact details.
* `NextCommandTest` includes a patient with a contact to check preservation when rescheduling visits.
* `PatientDetailsPanelTest` checks the exact display text, its position immediately after the phone label, wrapping,
  and hiding the row when switching to a patient without a contact.

#### Testing a newly built JAR

Use JDK 25 and run `./gradlew shadowJar` from the repository root (`.\gradlew.bat shadowJar` on Windows).
Close any previously running instance before launching `build/libs/doc.jar`. Building or replacing the JAR does
not reload classes in an existing application process. An `edit` usage message without
`[ec/NAME, PHONE_NUMBER]` indicates that the running build does not include emergency contact support.
Use a separate folder with disposable data for manual testing; the application uses paths relative to its working
directory for patient data and preferences.

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

Visit dates and times form a separate scheduling invariant. `UniquePersonList#containsSameVisitDate` compares
`VisitDate` values, whose equality is based on their parsed `LocalDateTime`. `add`, `setPerson`, and `setPersons` throw
`DuplicateVisitDateException` rather than allowing two records to use the same value. This covers command mutations,
model replacement, sample data, and direct model use. `JsonSerializableAddressBook` performs the same check while
loading and reports an invalid data file with a visit-date-specific message.

`Model#hasPersonWithSameVisitDate` exposes the scheduling check. `AddCommand` rejects a true duplicate first, then a
visit-date conflict. For a name duplicate with an available visit time, it captures the proposed immutable `Person` in
a `CommandInputRequest#createConfirmation` action. Confirmation rechecks both rejection rules before adding, which
prevents a conflicting record from being introduced if model state changes while the prompt is pending.

`EditCommand` similarly rejects a true duplicate or a changed visit date that another patient uses before requesting
confirmation. Re-entering the selected patient's existing visit date is allowed because it does not allocate another
slot. The command asks for confirmation only when the requested edit changes the name to one already in use; editing
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
* Automated tests do not currently construct `SampleDataUtil#getSampleAddressBook`. Consequently, an accidental
  duplicate among the sample visit times could break first-run sample-data initialization without failing the test
  suite.

#### Visit-time conflict limitations

The visit-date rule prevents only exact scheduling collisions:

* Equality is checked to the minute using the parsed `LocalDateTime`. The rule does not model visit duration, travel
  time, preparation time, or partial overlaps.
* Doc has one schedule and does not model multiple doctors, doctor availability, time zones, or recurring visits.
* Past visit dates are also unique because every current `Person` has one required `VisitDate`; the model does not
  distinguish an historical visit from an upcoming appointment.
* A data file created by an older version may contain duplicate visit dates. The migration and data-preservation risks
  are described under the current implementation limitations above.

### Editing patient details

`EditCommandParser` accepts optional replacement fields, including `vd/` and `note/`. The displayed format is
`edit INDEX [n/NAME] [dob/DATE] [p/PHONE] [a/ADDRESS] [vd/DATE_TIME] [e/EMAIL] [note/NOTES] [t/TAG]...`.
It checks index syntax and duplicate single-valued prefixes before parsing field values, then rejects a
descriptor with no supplied fields. When `vd/` is supplied, `ParserUtil#parseVisitDate` reuses the existing strict
`VisitDate` validation
for `d/M/yyyy HHmm`; valid past dates are accepted, while an empty `vd/` is rejected.

`EditPersonDescriptor` stores the supplied visit date and time and note along with the optional replacement fields.
`EditCommand` copies the descriptor, resolves the patient using the displayed list, and replaces the immutable
`Person`. Omitted fields, including the visit date/time and notes, are preserved. An explicitly empty
email, note, or tag field clears its existing value and counts as a supplied field.

Before replacement, `EditCommand` permits an unchanged visit date but rejects a changed value already assigned to
another patient. This check is repeated after a name-duplicate confirmation so a slot taken while the prompt was
pending cannot be introduced into the model.

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

`next INDEX DATETIME` schedules the following appointment for a displayed patient. Success changes only the visit
date and time and restores the full caseload in stored order. Corrections use `edit ... vd/`; completed-visit recording
and history remain deferred. See the [User Guide](UserGuide.md#scheduling-the-next-visit-next) for syntax and errors.

For example, assume the clock reads `31/10/2026 1200`, `find Betsy` displays only Betsy, and her existing
appointment is `2/11/2026 1000`. The doctor enters `next 1 1/11/2026 0930`, requesting an available future slot.

`AddressBookParser` delegates argument parsing to `NextCommandParser` and returns a `NextCommand` to `LogicManager`.
`LogicManager` executes the command against the model. The command selects Betsy from the displayed list and checks
that the requested appointment differs from her existing
one, is in the future, and is unused across all stored patients. Parsed equality detects equivalent spellings; the
[visit-time conflict limitations](#visit-time-conflict-limitations) apply.

On success, `NextCommand` replaces Betsy's immutable `Person`, preserving every other field, and restores the full
list in stored order by clearing the filter and sort. `LogicManager` then saves the updated patient data before
returning the success result to the UI.

Validation failures stop before any mutation or save. Storage failure happens after the in-memory update and follows
the existing error handling without rollback.

The design keeps three decisions explicit:

* **Separate scheduling from correction.** A dedicated command lets future visit-recording behavior evolve
  independently of `edit`.
* **Keep future validation in the command.** Comparing with now allows 1 November even though the old appointment
  is on 2 November. Keeping this rule outside `VisitDate` and storage allows past appointments to remain valid for
  corrections and loading.
* **Inject the clock.** Production uses the system's local time zone and reads time once at execution without
  truncating seconds. Fixed clocks make boundary and persistence tests deterministic without changing global time.

Tests cover parsing, displayed indices, hidden conflicts, field preservation, list reset, and persistence, including
saved appointments becoming past as the test clock advances. Manual cases appear in the
[testing appendix](#testing-the-next-command).

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

* 3a. Some of the shown patients have no next visit date.

    * 3a1. Doc lists those patients last.

      Use case resumes at step 4.

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

* Only the selected patient's appointment changes; completed-visit recording is outside this use case.
* Rejected input leaves patient records and the displayed list unchanged.

**MSS**

1.  Doctor requests a future appointment for a patient identified by their displayed index.
2.  Doc saves the new appointment and shows the full caseload in stored order.

    Use case ends.

**Extensions**

* 1a. The displayed index does not identify a patient.

    * 1a1. Doc explains that the selection is invalid.

      Use case resumes at step 1.

* 1b. The appointment is invalid, not in the future, or unchanged.

    * 1b1. Doc explains the specific reason for rejection.

      Use case resumes at step 1.

* 1c. Another patient already has the requested appointment.

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
* **House call**: A medical consultation carried out at the patient's home rather than at a clinic.
* **House-call doctor**: A doctor who travels to patients' homes to provide medical consultations and follow-up care.
* **Medical condition**: An ongoing health issue recorded in a patient's notes to help the doctor prepare for future visits.
* **MSS**: Main Success Scenario; the sequence of steps in a use case when nothing goes wrong.
* **Next visit date**: The date and time at which a patient is next scheduled to receive a house call. No two active
  patient records can use the same value.
* **Overdue visit**: A scheduled visit whose next visit date has passed without the visit being recorded or rescheduled.
* **Patient record**: The information Doc stores for one patient, including their contact details, medical notes,
  tags, and current visit date and time. Completed visit history is not stored in this increment.
* **Priority level**: A label indicating how closely a patient needs to be followed up, used when ordering planned visits.
* **Visit**: A single house call. Doc currently stores one appointment date and time and one patient-level note.
* **Visit history**: A collection of completed visits for a patient; recording this history is deferred.

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
      Expected: The GUI opens with a set of sample patient records. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Dates of birth

Use a disposable data file and available visit slots.

1. Run `add n/Dob Patient dob/15/6/1990 p/81234567 a/10 Test Road vd/22/10/2026 1000`, then `view 1`.
   Expect `Date of birth: 15/6/1990 (N years old)` immediately below the name, with the age in completed years
   on the computer's current local date. A leap-day birthday advances on March 1 in non-leap years.
1. Run `edit 1 n/Dob Patient dob/20/7/1985`, then `view 1`; expect the replacement date and recalculated age.
1. Edit another field and run `next 1 1/1/2099 1000`; expect the date of birth to remain unchanged.
1. Restart the application and run `view 1`; expect the saved date of birth to remain.
1. Try `edit 1 dob/`, `edit 1 dob/31/2/2000`, a future date, and repeated `dob/` prefixes.
   Expect errors and unchanged data. Repeat these invalid cases using `add`; no patient should be added.
1. Add another patient without `dob/` and view it; expect no date of birth row or stale text. View the first
   patient again; expect the row to reappear. Set the second patient's DOB using `edit INDEX dob/1/1/2000`.
1. Load an older valid data file without `dateOfBirth`; expect the patients to load with the DOB row hidden.

### Emergency contacts

Use a disposable data file. Start with no patients and use available visit slots.

1. Run `add n/John Tan p/91234567 a/123 Clementi Road vd/22/10/2026 1000 ec/Mary Tan, 98765432`.
   Run `view 1`; expect `Emergency Contact : 98765432 (Mary Tan)` directly below the patient's phone number.
1. Run `edit 1 ec/David Tan, 87654321`, then `view 1`; expect the replacement contact in the same position.
1. Run `edit 1 note/Review medication`, then `next 1 1/1/2099 1000`. Run `view 1`; expect the contact unchanged.
1. Restart the application and run `view 1`; expect the saved contact to remain.
1. Try `edit 1 ec/`, `edit 1 ec/Mary Tan`, `edit 1 ec/Mary Tan, abc`, and
   `edit 1 ec/Mary Tan, 98765432 ec/David Tan, 87654321`. Expect errors and unchanged patient details.
1. Add another patient without `ec/`, then view that patient; expect no emergency contact row or stale details
   from the previous patient. Add a contact using `edit INDEX ec/Mary Tan, 98765432` and view it again.
1. Load a valid older data file without `emergencyContact` fields; expect all patients to load with no contacts.

### Sorting visits by date

`SortCommandParser` accepts the `date` field and rejects missing, unsupported, or additional
arguments with the command usage message. The model exposes a `SortedList` backed by its
`FilteredList`. `SortCommand` preserves the current filter and sorts its displayed patients
into two groups using the local date at execution time: visits today or later first, followed
by past visits. Each group is ordered by visit date and time, earliest first. Stored patient
order is unchanged. Updating the filter resets the displayed order, preserving the usual
behavior of `list` and `find`.

1. Prepare patients with visits two days ago, yesterday, today at 0000, today at 2300, and tomorrow,
   entered in a different order from their visit dates.
1. Run `sort date`. Expect today's and tomorrow's patients first, followed by the two past visits;
   expect each group to be in ascending date-time order.
1. Run `find` for one patient, then `sort date`. Expect only that matching patient to remain displayed.
1. Run `sort date` twice. Expect the same order; equal visit times retain stored order.
1. Run `delete 1`, then `y`. Expect the first displayed patient to be deleted, with the remaining visits still sorted.
1. Run `list`. Expect every remaining patient, including yesterday's, in stored order.
1. Try `sort date` on an empty patient list. Expect an empty list. Try it on a patient list containing only past
   visits; expect all visits in ascending date-time order.
1. Run `sort`, `sort name`, and `sort date name`. Expect each command to show the required format and the
   `sort date` example without changing the displayed list.

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

Use a disposable data copy and fresh test names. Replace `DAY` with a date after today in `d/M/yyyy` format whose
0900, 0930, 1000, 1030, and 1100 slots are unused. Replace `PAST_DAY` with a date before today with an unused 1000 slot.
These placeholders must be replaced before submitting commands.

1. Add the two patients in this order:

   ```text
   add n/Nextalpha Patient p/81234567 a/10 Test Road vd/DAY 1000 e/alpha@example.com note/Keep this note t/priority
   add n/Nextbeta Patient p/82345678 a/20 Test Road vd/DAY 0900
   ```

   Expect success, with nonempty optional fields for Nextalpha and empty email, notes, and tags for Nextbeta.

1. Run `find Nextalpha`, then `next 1 DAY 0930`.<br>
   Expect Nextalpha to move to 0930, preserving all other fields, followed by the full list in stored order.
   The success message names Nextalpha and the new appointment. This checks a future time earlier than the old one.

1. Run `find Nextalpha`, then `next 1 DAY 0900`.<br>
   Expect a conflict naming hidden Nextbeta and the occupied time, with data and the filtered list unchanged.
   Repeat with a leading-zero day or month when applicable; expect the same conflict.

1. While only Nextalpha is displayed, try each of these commands:

   | Command | Expected feedback |
   | --- | --- |
   | `next` | Next usage. |
   | `next 1` | Missing arguments and usage. |
   | `next 0 DAY 0930` | Invalid index syntax and usage. |
   | `next 2147483648 DAY 0930` | Oversized index error and usage. |
   | `next 2 DAY 0930` | Index outside the displayed list. |
   | `next 1 DAY 0930` | Unchanged appointment. |
   | `next 1 PAST_DAY 1000` | Non-future appointment. |
   | `next 1 31/2/2027 0930` | Invalid date. |
   | `next 1 DAY 2400` | Invalid time. |
   | `next 1 vd/DAY 0930` | Prefix misuse and usage. |
   | `next 1 DAY 0930 extra` | Extra arguments and usage. |

   Each rejection must preserve records, file contents, and the displayed list. Also test today's current local
   minute; expect rejection. Repeat the unchanged-date test with equivalent leading zeros when applicable.

1. Run `find Nextalpha Nextbeta`, then `sort date`.<br>
   Expect Nextbeta first. Try `next 1 DAY 0900`; expect rejection with the sorted order preserved.
   Run `view 1`, then `next 1 DAY 1030`; expect Nextbeta to change, retaining empty optional fields, and the full list
   to return in stored order. Run `find Nextbeta`, then `view 1`, to refresh its details.

1. Run `find Nextalpha`, then `next   1   DAY   1100`.<br>
   Expect repeated spaces to be accepted, Nextalpha to move to 1100, and the full list to return.

1. Close and reopen the application. Find and view each test patient.<br>
   Expect Nextalpha at 1100, Nextbeta at 1030, and every other field preserved, with no visit-history entry.

1. After the persistence checks, run `find Nextalpha`, then `edit 1 vd/PAST_DAY 1000`. Close and reopen the app.<br>
   Expect the past appointment to load. Integration tests also cover saved appointments becoming past as time advances.

### Viewing one patient

1. Run `list`, followed by `view 1`. Expect the full list to remain on the left and the first patient's complete
   details to appear on the right.
1. Run `list`, followed by `find Alex`, and then `view 1`. Expect the search results to remain on the left and the
   first patient's complete details to appear on the right.
1. Run `sort date`, followed by `view 1`. Expect the sorted list to remain on the left and the first patient's complete
   details to appear on the right.
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
