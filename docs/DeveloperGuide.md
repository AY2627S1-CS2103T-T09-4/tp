---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# AB-3 Developer Guide

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

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
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

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

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
   from `Logic`. The result also tells `LogicManager` whether it should save the address book.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Follow-up command-line input and destructive-command confirmation

`CommandInputRequest` lets any command collect an additional command-line input through the existing command box and
result display. It contains the prompt and a response handler. The handler receives the raw next input and the
`Model`, then returns a `CommandResult`. That result can complete the interaction or contain another
`CommandInputRequest`, so future commands can implement multi-step input without adding command-specific state to the
UI or parser.

`LogicManager` stores at most one pending request. While a request is pending, `LogicManager#execute(String)` sends
the submitted text to the request handler instead of `AddressBookParser`. This includes blank input and text that
would otherwise be a valid command. After the handler returns or throws a `CommandException`, the old request is no
longer pending. A handler can continue the interaction by returning a result containing a new request.

`CommandResult` separates displaying feedback from saving the address book. Prompts, cancellations, and the
already-empty `clear` result do not cause a file write. A confirmed mutation returns a result that causes
`LogicManager` to save the updated address book once.

The shared `CommandInputRequest#createConfirmation` helper trims the response and compares it to `y` without regard
to case. It runs the supplied action only for that response; every other response returns the supplied cancellation
message.

For `delete INDEX`, `DeleteCommand` validates the index and captures the exact displayed `Person` before requesting
confirmation. The prompt contains only that person's name because other fields can be absent. Confirmation deletes
that captured object rather than resolving the index again. If the record changed or disappeared while confirmation
was pending, the command reports that the selected person no longer exists and deletes nothing.

For `clear`, `ClearCommand` requests confirmation only when the address book contains entries. Its prompt uses
`entry` for one record and `entries` otherwise. Confirming clears the address book; every other response cancels.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
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
| `* * *`  | doctor                  | set the next visit date for a patient                                     | be reminded when that patient is due for a checkup                                      |
| `* * *`  | doctor                  | find patients whose next visit date is today                              | be reminded who is due for a visit                                                      |
| `* *`    | doctor                  | add or update notes about a patient's medical conditions                  | prepare for future visits                                                               |
| `* *`    | doctor                  | assign a priority level to a patient                                      | tell at a glance which patients need closer attention                                   |
| `* *`    | doctor                  | sort my patients by their next visit date                                 | deal with the most overdue visits first                                                 |
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

1.  Doctor requests to add a patient, providing the patient's name, address, phone number and medical conditions.
2.  Doc adds the patient to the caseload and shows the newly added patient record.

    Use case ends.

**Extensions**

* 1a. A required detail is missing or invalid.

    * 1a1. Doc shows an error message describing the expected input.

      Use case resumes at step 1.

* 1b. A patient with the same name and phone number already exists.

    * 1b1. Doc shows an error message identifying the existing patient.

      Use case resumes at step 1.

**Use case: UC02 - Update a patient record after a visit**

**MSS**

1.  Doctor requests to list patients.
2.  Doc shows the caseload.
3.  Doctor requests to record a completed visit for a patient identified by their displayed index, providing the
    visit date and, a note optionally.
4.  Doc records the visit and shows the updated patient record.
5.  Doctor requests to schedule the patient's next visit, providing the next visit date.
6.  Doc records the next visit date and shows the updated patient record.

    Use case ends.

**Extensions**

* 2a. The caseload is empty.

  Use case ends.

* 3a. The displayed index does not identify a patient in the current list.

    * 3a1. Doc shows an error message.

      Use case resumes at step 2.

* 3b. The visit date is in the future.

    * 3b1. Doc shows an error message stating that a completed visit cannot be dated in the future.

      Use case resumes at step 3.

* 5a. Doctor does not schedule a next visit.

  Use case ends.

* 5b. The next visit date is in the past.

    * 5b1. Doc shows an error message stating that the next visit date cannot be in the past.

      Use case resumes at step 5.

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
* **Next visit date**: The date on which a patient is next scheduled to receive a house call.
* **Overdue visit**: A scheduled visit whose next visit date has passed without the visit being recorded or rescheduled.
* **Patient record**: The information Doc stores for one patient, including their contact details, medical conditions, visit history and next visit date.
* **Priority level**: A label indicating how closely a patient needs to be followed up, used when ordering planned visits.
* **Visit**: A single house call recorded in Doc with its date and an optional note.
* **Visit history**: The collection of completed visits recorded for a patient.

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
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: No contact is deleted. The result display shows
      `Delete NAME? Type y to confirm. Any other input cancels.` using the first contact's name.

   1. Test case: `y` immediately after the previous test case<br>
      Expected: The first contact is deleted from the list. The result display shows the deleted contact's details.

   1. Test case: `delete 1`, followed by `n`<br>
      Expected: No contact is deleted. The result display shows `Deletion cancelled.`

   1. Test case: `delete 1`, followed by `list`<br>
      Expected: No contact is deleted and `list` is consumed as the confirmation response. The result display shows
      `Deletion cancelled.` Entering `list` again lists all contacts normally.

   1. Test case: `delete 1`, followed by pressing Enter with an empty command box<br>
      Expected: No contact is deleted. The result display shows `Deletion cancelled.`

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: The existing invalid-command response is shown and no confirmation is requested.

### Clearing all persons

1. Clearing a non-empty address book

   1. Prerequisites: The address book contains multiple persons.

   1. Test case: `clear`<br>
      Expected: No contact is deleted. The result display shows
      `Clear COUNT entries? Type y to confirm. Any other input cancels.` with the current count.

   1. Test case: `Y` immediately after the previous test case<br>
      Expected: All contacts are deleted and the result display shows `Address book has been cleared!`.

1. Cancelling a clear operation

   1. Prerequisites: The address book contains at least one person.

   1. Test case: `clear`, followed by any input other than `y`, such as `list`<br>
      Expected: No contact is deleted, the second input is not executed as a command, and the result display shows
      `Clear cancelled.`

1. Clearing an empty address book

   1. Prerequisites: The address book is empty.

   1. Test case: `clear`<br>
      Expected: No confirmation is requested and the result display shows `Address book is already empty.`

1. _{ more test cases … }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
