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

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

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

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

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
| `* * *`  | doctor                  | add a patient record with name, address, phone number and conditions      | keep everything I need for a house visit in one place                                   |
| `* * *`  | doctor                  | list all my patients                                                      | see my whole caseload at a glance                                                       |
| `* * *`  | doctor                  | find a patient by name                                                    | pull up their details without scrolling through the entire list                         |
| `* * *`  | doctor                  | view a patient's full record including their medical conditions           | prepare for a visit before I arrive at their home                                       |
| `* * *`  | doctor                  | edit a patient's details                                                  | keep their record accurate when their address or condition changes                      |
| `* * *`  | doctor                  | delete a patient record                                                   | remove patients I no longer treat                                                       |
| `* * *`  | doctor                  | record that I visited a patient on a given date                           | know when I last saw each patient                                                       |
| `* * *`  | doctor                  | set the next visit date for a patient                                     | be reminded when that patient is due for a checkup                                      |
| `* * *`  | doctor                  | see the patients due to be visited today                                  | know who to see and where to go next                                                    |
| `* * *`  | doctor                  | have my data saved automatically after every change                       | not lose patient records if the app or my laptop shuts down                             |
| `* *`    | doctor                  | write a note against a patient after a visit                              | recall what we discussed the last time I saw them                                       |
| `* *`    | doctor                  | assign a priority level to a patient                                      | tell at a glance which patients need closer attention                                   |
| `* *`    | doctor                  | sort my patients by their next visit date                                 | deal with the most overdue visits first                                                 |
| `* *`    | doctor                  | filter patients by medical condition                                      | review together all the patients I treat for the same condition                         |
| `* *`    | doctor                  | filter patients by how long ago they were last visited                    | find patients who have gone too long without a checkup                                  |
| `* *`    | doctor                  | see the coming week's visits as a schedule                                | plan my week before it starts                                                           |
| `* *`    | doctor                  | copy a patient's address or phone number in one command                   | paste it into my maps or phone app without retyping it                                  |
| `* *`    | doctor                  | archive a patient who no longer needs regular visits                      | keep my active list short without losing their history                                  |
| `* *`    | doctor                  | view my archived patients                                                 | look up the history of a patient who returns after a long gap                           |
| `* *`    | doctor with many visits | see the estimated travel time between consecutive visits                  | schedule a realistic number of visits in a day                                          |
| `* *`    | long-time user          | define my own aliases for the commands I use most                         | enter routine commands with fewer keystrokes                                            |
| `*`      | new user                | import my existing patient records from a file                            | move my caseload into Doc without retyping every record                                 |
| `*`      | doctor                  | export a patient's record to a file                                       | hand it over to a colleague covering my rounds                                          |
| `*`      | doctor                  | lock the app behind a password                                            | keep patient data private if someone else uses my laptop                                |
| `*`      | doctor                  | see my scheduled visits plotted on a map                                  | choose the shortest route between homes                                                 |
| `*`      | doctor                  | message a patient from within the app                                     | confirm a visit without switching to another app                                        |

### Use cases

(For all use cases below, the **System** is `Doc` and the **Actor** is the `doctor`, unless specified otherwise)

**Use case: UC01 - Add a patient**

**MSS**

1.  Doctor requests to add a patient, supplying the patient's name, address, phone number and medical conditions.
2.  Doc adds the patient to the caseload and shows the newly added record.

    Use case ends.

**Extensions**

* 1a. A required detail is missing or in the wrong format.

    * 1a1. Doc shows an error message describing the expected format.

      Use case resumes at step 1.

* 1b. A patient with the same name and phone number already exists.

    * 1b1. Doc shows an error message identifying the existing patient.

      Use case resumes at step 1.

**Use case: UC02 - Record a completed visit and schedule the next one**

**MSS**

1.  Doctor requests to list patients.
2.  Doc shows the caseload.
3.  Doctor requests to record a visit for a specific patient in the list, supplying the visit date and a note.
4.  Doc saves the visit against that patient and shows the updated record.
5.  Doctor requests to set the next visit date for the same patient.
6.  Doc saves the next visit date and shows the updated record.

    Use case ends.

**Extensions**

* 2a. The caseload is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. Doc shows an error message.

      Use case resumes at step 2.

* 3b. The given visit date is in the future.

    * 3b1. Doc shows an error message stating that a completed visit cannot be dated in the future.

      Use case resumes at step 3.

* 5a. The given next visit date is in the past.

    * 5a1. Doc shows an error message.

      Use case resumes at step 5.

* 5b. Doctor does not schedule a next visit.

  Use case ends.

**Use case: UC03 - Plan the day's visits**

**MSS**

1.  Doctor requests the list of patients due to be visited today.
2.  Doc shows those patients ordered by priority level, then by next visit date.
3.  Doctor requests to see a specific patient's full record.
4.  Doc shows the patient's address, contact number, medical conditions and past visit notes.
5.  Doctor requests to copy that patient's address.
6.  Doc copies the address to the clipboard.

    Steps 3 to 6 are repeated for each patient the doctor wants to prepare for.

    Use case ends.

**Extensions**

* 2a. No patient is due today.

    * 2a1. Doc shows a message stating there are no visits due.

      Use case ends.

* 3a. The given index is invalid.

    * 3a1. Doc shows an error message.

      Use case resumes at step 2.

**Use case: UC04 - Find the patients treated for a condition**

**MSS**

1.  Doctor requests the patients matching a given medical condition.
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
3.  Doctor requests to archive a specific patient in the list.
4.  Doc removes the patient from the active caseload and keeps the record, with its visit history, among the archived patients.

    Use case ends.

**Extensions**

* 3a. The given index is invalid.

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
4.  Should be able to hold up to 1000 patient records, each with up to 100 visit notes, without noticeable sluggishness in performance for typical usage.
5.  Should respond to any command within 2 seconds when holding 1000 patient records.
6.  A doctor with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
7.  A doctor who has not used a CLI application before should be able to add a patient and look up that patient's record within 15 minutes of reading the User Guide.
8.  Should save every change to the local data file before accepting the next command, so that at most one command's worth of data is lost if the app terminates unexpectedly.
9.  Should store all patient data only on the user's own machine, so that no patient data leaves the doctor's computer.
10. Should start up and be ready to accept a command within 5 seconds on a typical modern laptop.
11. Should not crash or lose existing records if the data file has been manually edited into an invalid state; it should instead report the problem and start with an empty caseload.
12. Should be delivered as a single JAR file of no more than 100MB, so that it can be copied onto a laptop taken on house visits.

### Glossary

* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **CLI**: Command Line Interface; an interface in which the user types commands rather than clicking on controls
* **House call**: A medical consultation carried out at the patient's home rather than at a clinic
* **Patient record**: The set of details Doc holds for one patient, i.e. their contact details, medical conditions, visit history and next visit date
* **Caseload**: The set of patients a doctor is currently responsible for visiting
* **Visit**: A single house call to a patient, recorded in Doc with its date and an optional note
* **Next visit date**: The date on which a patient is next due to be visited
* **Priority level**: A label on a patient record indicating how closely that patient needs to be followed up, used to order the day's visits
* **Archived patient**: A patient who no longer needs regular visits, kept in Doc for reference but excluded from the active caseload and from visit planning
* **MSS**: Main Success Scenario; the sequence of steps of a use case when nothing goes wrong

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
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
