---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# Doc User Guide

Doc is a **desktop application for house-call doctors to manage patient records and plan home visits**. It is
optimized for use through a Command Line Interface (CLI) while retaining the benefits of a Graphical User Interface
(GUI). If you type quickly, Doc can help you manage your caseload faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103T-T09-4/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for Doc.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. When no saved data file exists, Doc starts with
   seven sample patient records: George Miller, Priya Nair, Aisha Rahman, Marcus Lim, Mei Lin Tan, Daniel Wong, and
   Noor Aziz.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all patients.

   * `add n/John Doe dob/15/6/1990 bt/A+ all/Penicillin; latex alc/Occasional drinking smk/former p/98765432 e/johnd@example.com a/John street, block 123, #01-01 vd/18/9/2026 1000` : Adds a patient named `John Doe` to Doc.

   * `delete 3` : Asks for confirmation before deleting the 3rd patient shown in the current list.

   * `clear` : Asks for confirmation before deleting all patient records.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Prefixed fields can be in any order after any required positional arguments.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Positional arguments must follow the command's documented order.<br>
  For example, `edit INDEX ...` starts with the index, and `next INDEX DATETIME` requires the index before the date
  and time.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Visit records

Each patient has one required **current visit date and time** and zero or more **previous visit dates**.
Together, these form the patient's **visit record**. The current datetime can be past, present, or future.
Previous dates are stored oldest first and displayed newest first by [view](#viewing-a-patient-s-details-view).
Notes belong to the patient; separate notes are not stored for each visit. Retained datetimes do not verify
attendance, cancellation, or completion.

Choose the command according to what you want to change:

| Goal | Command | Effect on the visit record |
| --- | --- | --- |
| Correct or reschedule the current appointment. | [edit](#editing-a-patient-edit) `INDEX vd/DATETIME` | Replace the current datetime; keep previous dates unchanged. |
| Plan the following visit after the current visit time has arrived. | [next](#scheduling-the-next-visit-next) `INDEX DATETIME` | Retain the old current datetime and set an unused future datetime. |
| Restore the latest previous datetime. | [prev](#restoring-the-previous-visit-prev) `INDEX` | Remove the latest previous datetime from the list and use it to replace the current datetime; discard the replaced datetime. |
| Leave a final visit recorded without planning another. | No command needed. | Keep the current datetime, including after it becomes past. |

Successful `edit`, `next`, and `prev` commands show the full patient list in stored order, clearing filtering and
sorting. The details panel continues showing the last record viewed. Check the newly displayed index and run
`view INDEX` to refresh the record and its calendar label. To return to search results, run `find` again.

When `next` moves an appointment to a different day that already has seven or more current appointments, Doc asks for
confirmation. Patients hidden by filtering still count, but previous visit history does not. Moving the appointment to
a later time on the same day does not trigger this warning.

### Navigating command history

The command box keeps commands submitted during the current application session so that you can reuse or edit them.

* Press <kbd>Up</kbd> to recall the most recently submitted command. Press it again to move to older commands. The
  oldest command remains displayed when there are no earlier commands.
* Press <kbd>Down</kbd> to move towards newer commands. After the newest command, pressing <kbd>Down</kbd> restores the
  unfinished text that was in the command box before you started browsing.
* Commands are recalled exactly as submitted, including spaces. Consecutive commands with exactly the same text are
  kept as one history entry so that navigation does not appear unresponsive. The same command is kept again when
  another command separates the submissions. Both successful and invalid commands are kept.
* Empty ordinary submissions and responses to follow-up prompts, such as `y` for a deletion confirmation, are not
  kept. While a follow-up prompt is pending, any recalled text you submit is still handled as the response to that
  prompt.
* Submitting a new command returns navigation to the newest command. The history is cleared when the application
  closes.

### Viewing help: `help`

Opens a separate help window listing **Available commands** in one column. The offline reference provides
short descriptions, syntax templates, optional fields, and a notation legend for quick lookup while entering
commands. Detailed validation rules and examples remain in the command sections below.

Format: `help`

You can also press **F1** or choose **Help → Help**. Repeating a help request brings the existing
window to the front, restoring it if minimized. You can resize Help and continue using the main window.

* **Browse commands:** The command list receives keyboard focus when Help opens. Use **Up** and **Down** to browse,
  or **Home** and **End** to reach the top and bottom. The User Guide URL and buttons stay below the list.
* **Read the full guide:** Choose **Open User Guide** to open it in your default browser. This requires
  an Internet connection. The launch message does not guarantee that the page loaded. If nothing opens,
  choose **Copy URL** and paste the address into your browser. If copying fails, retry or enter the displayed URL manually.
* **Close Help:** Choose **Close**, press **Escape**, or use the window's close control. Focus returns to the
  command box with your unfinished input unchanged. For example, type `find Be`, press F1, then close Help
  to continue editing the same draft.

Use **Tab** and **Shift+Tab** to move between the command list and buttons. Press **Space** to activate a focused
button. On Windows and Linux, you can also press **Enter**.

<box type="warning" seamless>

While a confirmation or follow-up prompt is pending, open Help with **F1** or **Help → Help**.
Typing `help` and pressing Enter answers that prompt instead of opening Help.
</box>

![Help window](images/helpMessage.png)


### Adding a patient: `add`

Adds a patient record to Doc.

Format: `add n/NAME [dob/DATE] [bt/BLOOD_TYPE] [all/ALLERGIES] [alc/ALCOHOL_HISTORY] [smk/STATUS] p/PHONE_NUMBER a/ADDRESS vd/VISIT_DATE [e/EMAIL] [note/NOTES] [ec/[NAME, ]PHONE_NUMBER] [t/TAG]... `

* `VISIT_DATE` must be a date and time in the format `d/M/yyyy HHmm`, e.g. `18/9/2026 1000`. Past dates are allowed.
* Each current or previous visit date and time must be unique across patients. If another patient's visit record
  already uses the supplied date and time, including a patient hidden by filtering, the addition is rejected,
  the existing patient is shown in the patient details panel, and the app asks you to choose a different slot.
* Date of birth, blood type, allergies, alcohol history, smoking status, `EMAIL`, `NOTES`, and the emergency contact are optional.
* Use `ec/[NAME, ]PHONE_NUMBER` to record one emergency contact, e.g. `ec/Mary Tan, 98765432`.
  The phone number is required; the name is optional. Use `ec/98765432` when the name is unknown.
  When supplying a name, include exactly one comma between the name and phone number.
  Names follow the patient-name rules (alphanumeric characters and spaces, not blank); phone numbers contain
  only digits and are at least 3 digits long. Surrounding whitespace is trimmed from both details.
  An empty `ec/`, missing phone number, invalid detail, or repeated `ec/` is rejected.
* Use `dob/DATE` to record the date of birth in `d/M/yyyy` format, e.g. `dob/15/6/1990`.
  The year must have four digits, from `0001` onwards. The date must exist and must not be in the future;
  today's date is allowed. An empty or repeated `dob/` is rejected. Surrounding whitespace is trimmed.
  Leading zeroes in the day and month are accepted and removed when displaying or saving.
  Omitting `dob/` leaves the date of birth unrecorded. Existing saved records without this field still load.
  It does not change how duplicate patients are identified.
* Use `bt/BLOOD_TYPE` after the name, e.g. `bt/A+`. Prefixes can appear in any order.
  Accepted blood types are `A+`, `A-`, `B+`, `B-`, `AB+`, `AB-`, `O+`, and `O-`.
  Letter case is ignored and surrounding whitespace is trimmed; `bt/ab-` is stored as `AB-`.
  Omitting `bt/` leaves blood type unrecorded. An empty or repeated `bt/` is rejected when adding a patient.
  Blood type does not affect duplicate-patient detection. Older saved records without blood type still load.
* Use `all/ALLERGIES` to record free-text allergy information, e.g. `all/Penicillin; latex`.
  Surrounding whitespace is removed; internal spacing, punctuation, and letter case are preserved.
  Omitting `all/` leaves allergy information unrecorded. An empty, whitespace-only, or repeated `all/` is rejected on add.
  An unrecorded field does not mean no known allergies. Record `all/No known allergies` explicitly when appropriate.
  The examples place `all/` before `alc/` and `smk/`; prefixes can still appear in any order.
* Use `alc/ALCOHOL_HISTORY` to record free-text alcohol history, e.g. `alc/Occasional drinking`.
  Surrounding whitespace is removed; internal spacing, punctuation, and letter case are preserved.
  Omitting `alc/` leaves the history unrecorded. An empty, whitespace-only, or repeated `alc/` is rejected on add.
  An unrecorded history is distinct from an explicit `alc/Does not drink`.
  The examples place `alc/` immediately after `all/`; prefixes can still appear in any order.
* Use `smk/STATUS` to record smoking status: `never`, `former`, `current`, or `unknown`.
  Letter case is ignored and surrounding whitespace is trimmed; `smk/ FORMER ` is stored as `former`.
  Omitting `smk/` leaves the status unrecorded. Explicit `smk/unknown` records that the status is unknown;
  it is distinct from having no record. Blank values, unsupported statuses, and repeated `smk/` are rejected on add.
  The examples place `smk/` immediately after `alc/`; prefixes can still appear in any order.
* On success, the app shows `Added patient NAME` and the new patient appears in the list.
* On failure, the app shows `The patient is not added due to: ` followed by the reason, and no patient is added.
* A patient with the same normalized name and phone number as an existing patient is a duplicate and cannot be added.
* If only the name matches an existing patient, the app shows the matching patient in the patient details panel and
  asks `A patient named NAME already exists. The matching patient is shown in the patient details panel. Add NAME
  anyway? Type y to confirm. Any other input cancels.`
* Type `y` and press Enter to add the patient. Surrounding spaces and letter case are ignored. Any other input cancels
  the addition and is not run as a command. Confirmation rechecks that the visit date and time are still available.
* If 7 or more patients already have a visit on the same day (at any time), the app warns you before adding another,
  e.g. `Heads up: you already have 7 visits on Fri, 18 Sep 2026. Add Jane Tan anyway? Type y to confirm. Any other
  input cancels.` Type `y` to add the patient anyway.
* If the name matches an existing patient **and** the day already has 7 or more visits, the app asks only once,
  listing both warnings before a single `Add NAME anyway?` question. One `y` adds the patient; any other input
  cancels the addition.
* For duplicate detection, names are compared without letter case after surrounding whitespace is removed and repeated
  spaces are collapsed to one space. For example, `John Doe`, `john doe`, and `John   Doe` are treated as the same
  name. The accepted name's spelling, capitalization, and internal spacing are still displayed and saved.

Duplicate detection has these limitations:

* It does not recognize nicknames, reordered names, misspellings, transliterations, or equivalent accented forms.
* Phone numbers are compared exactly, so different country-code forms of the same number are not recognized as equal.
* Two different patients with the same normalized name and phone number cannot both be added. Include a distinguishing
  name component, such as a middle name or suffix, or use a different contact number.
* Patients whose intentional name difference consists only of capitalization or spacing will trigger duplicate
  handling.

Visit-time conflict detection has these limitations:

* It compares the parsed date and time exactly to the minute. It does not account for appointment duration, travel
  time, preparation time, or partially overlapping visits.
* It does not manage doctor availability, time zones, recurring visits, or separate schedules for multiple doctors.
* Past datetimes are subject to the same uniqueness rule, including dates retained as previous visits.

<box type="tip" seamless>

**Tip:** A patient can have any number of tags, including zero.
</box>

Examples:
* `add n/John Doe dob/15/6/1990 bt/A+ all/Penicillin; latex alc/Occasional drinking smk/former p/98765432 a/John street, block 123, #01-01 vd/18/9/2026 1000 e/JohnDoe@gmail.com note/This man is sick`
* `add n/John Tan p/91234567 a/123 Clementi Road vd/22/10/2026 1000 ec/Mary Tan, 98765432`
* `add n/Betsy Crowe t/friend a/Newgate Prison p/1234567 vd/1/10/2026 1430 t/criminal`

#### Emergency contact examples

The emergency contact belongs to the patient record; it does not create another patient. Different patients may
share the same emergency contact. Enter the contact inside the existing `add` or `edit` command.

| Input | Result |
| --- | --- |
| `ec/98765432` | Accepts the phone number without a name. |
| `ec/Mary Tan, 98765432` | Accepts the name and phone number. |
| `ec/Mary Tan,98765432` | Accepts the details; a space after the comma is optional. |
| `ec/Mary Tan` | Rejects the contact because a phone number is required. |
| `ec/Mary Tan, abc` | Rejects the contact because the phone number contains letters. |
| `ec/` | Rejects the field in `add`; clears the emergency contact in `edit`. |

To update an existing patient's contact, run `edit 2 ec/David Tan, 87654321`, replacing `2` with the patient's
current displayed index. Run `view 2` afterwards to see `Emergency Contact: 87654321 (David Tan)` below the
patient's phone number. Use `edit 2 ec/87654321` to store just the number; the display becomes
`Emergency Contact: 87654321` without parentheses. Editing only the contact keeps other patient details unchanged.
Changes to other fields, including scheduling a visit with `next`, keep the emergency contact unchanged.
To remove the contact, run `edit 2 ec/`. Run `view 2` again; the emergency contact row will be hidden.

### Listing all patients: `list`

Shows all patient records in Doc.

Format: `list`

Patients with a visit date before today are shown using darker backgrounds and muted text. This visual distinction
also appears in patient lists produced by commands such as `find` and `sort date`. Past patients remain selectable,
and their complete details remain readable in the details panel.

### Sorting visits by date: `sort date [START_DATE]`

Sorts the currently displayed patients by visit date and time around an optional cutoff date. Visits on or after the
cutoff date are shown first, followed by visits before it.

Format: `sort date [START_DATE]`

* `START_DATE` must use the format `d/M/yyyy`, e.g. `18/9/2026`.
* If `START_DATE` is omitted, the cutoff is your computer's local date when you run the command.
* Visits earlier on the cutoff date remain in the first group because the cutoff applies to the whole calendar day.
* Visual muting is always based on today's date, not `START_DATE`. A custom cutoff changes the order only.
* Within the on-or-after and before-cutoff groups, visits are ordered by date and time, earliest first.
* The command preserves the current displayed set. For example, `find n/Tan` followed by `sort date` sorts only the
  matching patients.
* Use the displayed indices for subsequent commands such as `edit` or `delete`.
* `list` shows all patients again in stored order; a new `find` also returns its matches in stored order.
* Missing, invalid, or additional arguments show the required format and example commands.
* When omitting `START_DATE`, run `sort date` again after midnight to refresh which visits belong to each group.

Examples:
* `sort date` uses today as the cutoff date.
* `sort date 18/9/2026` places visits on or after 18 September 2026 before earlier visits.

### Viewing a patient's details: `view`

Displays the selected patient's full record in the details panel on the right.

Format: `view INDEX`

* If recorded, the date of birth appears immediately below the name in both the left-hand patient list
  and the right-hand details panel as
  `Date of birth: 15/6/1990 (N years old)`. Age is calculated in completed years using the computer's local
  date when the record is viewed. For a February 29 birthday, age increases on March 1 in non-leap years.
  The row is hidden when no date of birth is recorded.
* Displays the patient at the specified `INDEX`.
* The index refers to the index number shown in the currently displayed patient list.
* The index **must be a positive integer** 1, 2, 3, ...
* The command works on lists produced by commands such as `find` and `sort date`.
* The currently displayed patient list remains visible on the left.
* The current visit date and time remain visible as `Visit date`. The `Previous visit dates:` section immediately
  below it lists retained datetimes in `d/M/yyyy HHmm` format, newest first and oldest last. The note follows.
* A label beside the current datetime compares its calendar date with your computer's local date:
  `Past` means before today, `Today` means today (including earlier or later times today), and `Upcoming` means
  after today. For example, `Visit date: 21/10/2026 0900 (Today)` when viewed on 21 October 2026.
* These labels do not indicate attendance or completion and do not change the time-based rules for `next`.
  They update when you run `view`; run it again after midnight or a date change to refresh the details.
  A visit later today shows `Today`; `next` still rejects it while its datetime is in the future.
* A patient with no previous dates shows `No previous visit dates`. Scroll the details panel to read long records;
  all previous dates are included.
* Viewing a patient does not change their stored details.
* Recorded blood type appears below the date of birth (or directly below the name when DOB is absent) as
  `Blood type: A+` in both the left-hand patient list and the right-hand details panel. The row is hidden without reserving space when blood type is unrecorded.
* Recorded allergies appear immediately below blood type and before alcohol history and smoking status in the
  right-hand details panel as `Allergies: Penicillin; latex`. The row is hidden when allergy information is unrecorded.
  An explicit `No known allergies` entry remains visible.
* Recorded alcohol history appears below allergies in the right-hand details panel as
  `Alcohol history: Occasional drinking`. If allergies are absent, it follows the preceding visible field.
  The alcohol history row is hidden without reserving space when unrecorded; the left-hand patient cards stay compact.
* Recorded smoking status appears immediately below alcohol history in the right-hand details panel as
  `Smoking status: former`. If alcohol history is absent, it follows the preceding visible field.
  An explicit `unknown` status is displayed; the row is hidden without reserving space when unrecorded.
* After editing a patient, run `view INDEX` again to refresh the selected patient's details.

* If an emergency contact is recorded, it appears directly below the patient's phone number as
  `Emergency Contact: 98765432 (Mary Tan)`, or `Emergency Contact: 98765432` when the name is omitted.
  The emergency contact row is hidden when no contact is recorded.

Example:
* `view 1` displays the first patient's full record in the details panel.

### Editing a patient: `edit`

Edits an existing patient record in Doc.

Format: `edit INDEX [n/NAME] [dob/DATE] [bt/BLOOD_TYPE] [all/ALLERGIES] [alc/ALCOHOL_HISTORY] [smk/STATUS] [p/PHONE] [ec/[NAME, ]PHONE_NUMBER] [a/ADDRESS] [vd/DATE_TIME] [e/EMAIL] [note/NOTES] [t/TAG]...`

* Edits the patient at the specified `INDEX`. The index refers to the index number shown in the displayed patient
  list. The index **must be a positive integer** 1, 2, 3, ...
* All editable fields are optional, but at least one must be provided.
* Use `dob/DATE` to add or replace the date of birth, e.g. `edit 1 n/John Doe dob/15/6/1990`.
  The same date rules as `add` apply to nonempty values. Omitting `dob/` preserves the current value.
  Use an empty `dob/`, e.g. `edit 1 dob/`, to clear the date of birth. Whitespace-only values also clear it.
  Clearing an already empty date of birth is allowed. Run `view INDEX` again to hide the date of birth row.
  Repeated `dob/` prefixes are rejected, including when one or both values are empty.
* Use `bt/BLOOD_TYPE` to add or replace blood type, e.g. `edit 1 bt/O-`. The same valid values and normalization
  rules as `add` apply. Omitted `bt/` preserves the current blood type.
  Use an empty `bt/`, e.g. `edit 1 bt/`, to clear it; whitespace-only values also clear it.
  Repeated `bt/` prefixes are rejected, including identical or empty values.

* Use `all/ALLERGIES` to add or replace allergy information, e.g. `edit 1 all/Penicillin; latex alc/Does not drink`.
  Omitting `all/` preserves existing information. Use `edit 1 all/` to clear it; whitespace-only values also clear it.
  Clearing an already unrecorded field succeeds. Repeated `all/` prefixes are rejected, including blank values.
  Clearing allergies does not record `No known allergies`. Other patient details and visit history are preserved.
* Use `alc/ALCOHOL_HISTORY` to add or replace alcohol history, e.g. `edit 1 bt/A+ alc/Does not drink`.
  Omitting `alc/` preserves the current history. Use `edit 1 alc/` to clear it; whitespace-only values also clear it.
  Clearing an already unrecorded value succeeds. Repeated `alc/` prefixes are rejected, including empty values.
  Other patient details and visit history are preserved.
* Use `smk/STATUS` to add or replace smoking status, e.g. `edit 1 alc/Does not drink smk/never`.
  The same supported values and normalization as `add` apply. Omitting `smk/` preserves the current status.
  Use `edit 1 smk/` to clear it; whitespace-only values also clear it, including an already unrecorded status.
  Unsupported statuses and repeated `smk/` prefixes are rejected. Other patient details and visit history are preserved.
* Use `ec/[NAME, ]PHONE_NUMBER` to add or replace the emergency contact, e.g. `edit 1 ec/Mary Tan, 98765432`.
  Use `edit 1 ec/98765432` when no contact name is known. Nonempty values follow the same validation rules as `add`.
  Omitting `ec/` preserves the existing contact.
  Use an empty `ec/` to clear the contact, e.g. `edit 1 ec/`. Whitespace-only values also clear it.
  Clearing an already-empty contact succeeds; repeated `ec/` prefixes are rejected.
* To update the visit date and time, use `vd/` with the format `d/M/yyyy HHmm`
  (e.g., `8/10/2026 1000`). A valid date and time in the past is accepted.
* The visit date and time can be edited by itself. Omitting `vd/` preserves the existing visit date and time.
* Editing preserves any previous visit dates. The current datetime must remain strictly later than the latest
  previous datetime, when one exists.
* Re-entering the selected patient's existing visit date and time is allowed. Changing it to a slot used by another
  patient's current or previous visits is rejected, including hidden patients and edits to other fields. The patient
  using that slot is shown in the patient details panel, while the patient list remains unchanged.
* Omitted fields, including email and existing notes, keep their values. Use an empty `e/` to clear email.
* Use `note/NOTES` to replace the entire stored note. Use an empty `note/` to clear it; notes can be edited
  without `vd/`. An explicitly empty note counts as a supplied field, even when the note is already empty.
* Leading and trailing spaces and tabs in notes are trimmed; internal spaces are preserved. An empty value
  after trimming clears the note. A recognized prefix preceded by a literal space starts another field,
  even inside notes (e.g., `note/Review medication e/new@example.com` edits both notes and email).
  There is no escaping for literal prefix text.
* Prefixes can appear in any order. Repeated single-valued prefixes, including `note/`, are rejected,
  even when their values are identical or empty. Multiple `t/TAG` prefixes are allowed.
* Editing replaces the stored visit date and time; it does not append a visit-history entry.
  Use `edit ... vd/` to correct a missed or rescheduled appointment, or
  [next](#scheduling-the-next-visit-next) to schedule the following visit.
* After editing, follow the [visit-record interaction rules](#visit-records) to find the patient and refresh details.
* When editing tags, all of the patient's existing tags are removed; adding tags is not cumulative.
* To remove all of a patient's tags, enter `t/` without a tag after it.
* An edit that would give a patient the same normalized name and phone number as another patient is rejected.
* When an edit changes the patient's name to one already used by another patient with a different phone number, the
  app asks `A patient named NAME already exists. Apply this edit anyway? Type y to confirm. Any other input cancels.`
* Type `y` and press Enter to apply the edit. Surrounding spaces and letter case are ignored. Any other input cancels
  the edit and is not run as a command. Editing other details of a patient whose name is already shared does not ask
  for confirmation again. A confirmed name-duplicate edit rechecks that its requested visit slot is still available.
* When an edit moves a patient's visit to a different day that already has 7 or more visits, the app asks, e.g.
  `Heads up: you already have 7 visits on Fri, 18 Sep 2026. Schedule Jane Tan on that day anyway? Type y to confirm.
  Any other input cancels.` Changing only the time within the same day, or editing other fields, does not ask. If the
  name also matches another patient, you are asked to confirm the name first and then the busy day.
* The normalized-name comparison and duplicate-detection limitations described under `add` also apply to edits.
* The visit-time conflict limitations described under `add` also apply to edits.

Examples:
* `edit 1 vd/8/10/2026 1000` Changes the 1st patient's visit to 8 October 2026 at 10:00.
* `edit 1 p/91234567 vd/8/10/2026 1000 e/johndoe@example.com` Updates the visit, phone number, and email.
* `edit 1 p/91234567 e/johndoe@example.com` Updates the phone number and email, keeping the visit unchanged.
* `edit 1 ec/Mary Tan, 98765432` Adds or replaces the emergency contact, keeping other details unchanged.
* `edit 1 ec/98765432` Records only the emergency contact's phone number.
* `edit 1 ec/` Clears the emergency contact, keeping other details unchanged.
* `edit 1 e/` Clears the email, keeping the visit and other details unchanged.
* `edit 1 note/Patient requests a morning visit` Replaces the notes, keeping all other details unchanged.
* `edit 1 note/` Clears the notes, keeping all other details unchanged.
* `edit 1 e/ note/` Clears both email and notes, keeping the visit and other details unchanged.
* `edit 1 vd/8/10/2026 1000 e/new@example.com note/Review medication` Updates the visit, email, and notes.
* `edit 2 n/Betsy Crower t/` Updates the name and clears all existing tags, keeping the visit unchanged.

### Scheduling the next visit: `next`

Schedules a patient's following appointment and retains the current visit date and time in their visit record.
Use `next` after handling the current visit and planning another. Retained dates do not verify attendance or
completion. Use `edit ... vd/` to correct or reschedule an existing appointment without adding a previous date.

Format: `next INDEX DATETIME`

* Use a positive `INDEX` from the currently displayed list, including results of `find` or `sort date`.
  `view` keeps that list and its indices unchanged.
* Enter `DATETIME` as `d/M/yyyy HHmm`, with a four-digit 24-hour time. Spaces or tabs separate the arguments;
  repeated and outer whitespace are accepted. Do not use `vd/`, quotation marks, or extra arguments.
* The current visit must be at or before your computer's current local time at execution. If it is still in the
  future, use `edit ... vd/` to reschedule it.
* The requested appointment must be strictly after that execution time; the current minute's start is rejected.
* The requested appointment must differ from every other patient's current and previous visit dates and times,
  including hidden patients. Leading-zero dates compare equally: `1/11/2026 0930` and `01/11/2026 0930` are the same time.
* Conflict detection covers identical start times, not visit duration, travel time, or partial overlaps.
  The [visit-time conflict limitations under add](#adding-a-patient-add) also apply.
* When the requested appointment is on a different day that already has 7 or more visits, the app asks, e.g.
  `Heads up: you already have 7 visits on Mon, 2 Nov 2026. Schedule Jane Tan's next visit on that day anyway? Type y
  to confirm. Any other input cancels.` Type `y` and press Enter to schedule it; surrounding spaces and letter case
  are ignored. Any other input shows `Scheduling cancelled.`, keeps the data unchanged, and is not run as a command.
  If the requested time has passed, or another patient has taken it, by the time you enter `y`, the visit is not
  scheduled and the usual error is shown.
  A next visit later on the same day as the current visit does not ask, since that day's visit count does not grow.
* Success appends the old current date and time to the previous dates, then sets the new current date and time.
  All other details, including empty optional fields, are preserved. Previous dates are saved in order in the
  JSON data file and appear below the current visit date when you run `view INDEX`.
* If no further visit is planned, leave the current date and time unchanged, even after it passes.
* Success shows `Next visit scheduled for patient NAME: DATETIME` and saves the record. Follow the
  [visit-record interaction rules](#visit-records) to find the patient and refresh details.
* Validation errors preserve data and the displayed list. A save failure reports a storage error; the in-memory
  appointment may already have changed.

Common errors:

| Input problem | Feedback |
| --- | --- |
| Current visit still in the future | `Patient NAME has a current visit at DATETIME that is still in the future. Use edit to reschedule it.` |
| Time at or before now | `The next visit must be later than the current time (DATETIME). Choose a later date and time.` |
| Unchanged appointment | `Patient NAME already has an appointment at DATETIME.` |
| Another patient's current or previous visit | `Patient NAME already has a current or previous visit at DATETIME and is shown in the patient details panel. Choose a different date and time.` |

Bare `next` shows usage. Missing or extra arguments, `vd/` syntax, and incorrect index syntax show a reason and usage.
Invalid dates show the required format; an index outside the displayed list shows the existing invalid-index error.
The current time in the error uses `d/M/yyyy HHmm`, without seconds; validation still uses the full execution time.

Examples (the selected patient's current visit must have arrived or passed; choose an unused future datetime):

* `next 1 1/11/2026 0930` schedules the first displayed patient's next appointment for 1 November 2026 at 09:30.
* `find n/Betsy`, then `next 1 2/11/2026 1430`, schedules the first matching patient and restores the full list.

### Restoring the previous visit: `prev`

Restores the patient's latest previous visit date and time. Use this to roll back visit-date advancement;
it does not undo changes to the patient's other details.

Format: `prev INDEX`

* Use a positive `INDEX` from the currently displayed list, including results of `find` or `sort date`.
  Supply only the index, without a datetime or `vd/` prefix.
* The current datetime is discarded and replaced with the latest previous datetime.
  The restored datetime is removed from the previous-date list. There is no redo.
* Rollback is allowed even when the current or restored datetime is in the future. It does not apply `next`'s
  clock restrictions and does not ask for confirmation.
* Rollback also works after an intervening `edit`. It preserves the patient's latest name, contact details,
  address, allergies, alcohol history, smoking status, note, and tags; it restores only visit-date state.
* Success shows `Visit date restored for NAME: RESTORED_DATETIME` followed by
  `Discarded visit date and time: DISCARDED_DATETIME` on the next line. Both values include the time.
  It saves the record. Follow the [visit-record interaction rules](#visit-records) to find the patient and refresh details.
* Repeating `prev` restores one earlier datetime each time. When none remain, it reports
  `Patient NAME has no previous visit dates to restore.` without changing data or the displayed list.
* Missing, invalid, or additional arguments show usage. Oversized positive indices show `Index is too large.`
  followed by guidance and usage. An index within the supported range but outside the displayed list shows
  the invalid-index error. Rejected commands do not save changes.
* A later `next` uses the restored state and still requires the current visit to have arrived or passed and the
  requested datetime to be in the future. A save failure reports a storage error; the in-memory record may
  already have changed.

Example: suppose the clock reads `21/10/2026 1200`. Use globally unused replacement datetimes and Betsy's currently
displayed `INDEX` for each command. Each row continues from the previous row:

| Operation | Current datetime | Previous datetimes (stored oldest first) |
| --- | --- | --- |
| Initial record | `30/10/2026 1000` | `[10/10/2026 1000, 20/10/2026 1000]` |
| `edit INDEX vd/1/11/2026 0930 note/Review medication` | `1/11/2026 0930` | `[10/10/2026 1000, 20/10/2026 1000]` |
| `prev INDEX` | `20/10/2026 1000` | `[10/10/2026 1000]` |
| `next INDEX 2/11/2026 0930` | `2/11/2026 0930` | `[10/10/2026 1000, 20/10/2026 1000]` |
| `prev INDEX` | `20/10/2026 1000` | `[10/10/2026 1000]` |
| `prev INDEX` again | `10/10/2026 1000` | `[]` |
| `prev INDEX` again | Unchanged; no previous dates to restore. | `[]` |

After the edit, the note remains `Review medication` through all later steps. The first rollback discards
`1/11/2026 0930`; it does not restore the
`30/10/2026 1000` replaced by the edit. The later `next` schedules a new appointment under its normal rules and
does not act as redo. Repeated rollback eventually empties the previous-date list.

### Locating patients: `find`

Finds patients by name, phone number, address, tag, visit date or notes.

Format: `find [n/NAME_KEYWORD...] [p/PHONE_KEYWORD...] [a/ADDRESS_KEYWORD...] [t/TAG_KEYWORD...] [vd/DATE...] [note/NOTE_KEYWORD...]`

* Give at least one field. Every keyword must follow a prefix; for example, use `find n/george`, not `find george`.
* How each field matches (all case-insensitive):
  * `n/`: the start of any word in the name, first name or surname. For example, `n/m` matches `George Miller`,
    `Marcus Lim`, and `Mei Lin Tan`, but `n/ans` does not match `Hans`.
  * `p/`: any part of the phone number, so the last few digits are enough. For example, `p/8901` matches `85678901`.
    Phone keywords may contain only digits.
  * `a/`: the start of any word in the address. For example, `a/tamp` matches `46 Tampines Street 21, #08-03`.
  * `t/`: the start of any tag. For example, `t/morn` matches a patient tagged `morning`.
  * `vd/`: patients whose current visit is on that day, at any time. Give the date without a time in the format
    `d/M/yyyy`, e.g. `vd/15/10/2026`. Previous visit dates are not searched. An impossible date, another format,
    or a time such as `vd/15/10/2026 0900` is rejected with `"VALUE" is not a valid visit date.` and the format.
  * `note/`: the start of any word in the patient's notes. For example, `note/gluc` matches
    `Discuss glucose readings`, but `note/cose` does not. Patients with no notes never match.
* Several keywords for one field find patients matching **any** of them (an `OR` search); for example,
  `n/george priya` returns `George Miller` and `Priya Nair`.
* Different fields must **all** match (an `AND` search); for example, `n/m t/morning` returns only patients with a
  name word starting with `m` who are tagged `morning`.
* Fields can be given in any order, but each prefix can appear only once. Put several keywords after one prefix
  instead, e.g. `n/george priya` rather than `n/george n/priya`.
* Spaces, tabs or line breaks can separate fields; for example, `n/alex` and `p/9123` separated by a tab are still
  two separate fields.
* The app shows how many patients were found, e.g. `3 patients found.` If none match, it lists every search prefix:
  `No patients found. Search with n/ for names, p/ for phone numbers, a/ for addresses, t/ for tags, vd/ for visit dates, or note/ for notes.`

Examples:
* `find n/m` returns `George Miller`, `Marcus Lim`, and `Mei Lin Tan`
* `find n/george priya` returns `George Miller` and `Priya Nair`
* `find p/8901` returns `Noor Aziz`, whose number ends in `8901`
* `find a/tampines` returns `Marcus Lim`
* `find vd/15/10/2026` returns `Priya Nair`, whose visit is at 09:00 that day. Follow it with `sort date` to list
  that day's visits by time when planning your route.
* `find vd/14/10/2026 15/10/2026` returns `Priya Nair` and `Mei Lin Tan`
* `find note/review` returns `George Miller` and `Marcus Lim`, whose notes start with `Review`
* `find n/m t/morning` returns `Mei Lin Tan`; `George Miller` and `Marcus Lim` also match `n/m` but are not tagged
  `morning`

### Deleting a patient: `delete`

Deletes the specified patient record from Doc.

Format: `delete INDEX`

* Deletes the patient at the specified `INDEX`.
* The index refers to the index number shown in the displayed patient list.
* The index **must be a positive integer** 1, 2, 3, ...
* A valid command displays `Delete NAME? Type y to confirm. Any other input cancels.` without deleting the patient.
* Type `y` and press Enter to confirm. Surrounding spaces and letter case are ignored.
* Any other input, including an empty response or another valid command, cancels the deletion and is not run as a
  command. The result display then shows `Deletion cancelled.`
* Invalid `delete` commands continue to show the usual invalid-command response and do not ask for confirmation.

Examples:
* `list` followed by `delete 2`, then `y`, deletes the 2nd patient in Doc.
* `find n/Betsy` followed by `delete 1`, then `y`, deletes the 1st patient in the results of the `find` command.

### Clearing all entries: `clear`

Clears all patient records from Doc.

Format: `clear`

* When Doc contains patient records, the result display asks for confirmation and shows the number of entries
  that will be cleared.
* Type `y` and press Enter to confirm. Surrounding spaces and letter case are ignored.
* Any other input, including an empty response or another valid command, cancels the clear operation and is not run as
  a command. The result display then shows `Clear cancelled.`
* If Doc has no patient records, no confirmation is requested and the result display shows
  `Address book is already empty.`

Example: `clear` followed by `y` clears every entry.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

Doc automatically saves completed changes, including successful `add`, `edit`, `next`, and `prev` commands.
`view` displays details without saving patient data. Confirmation prompts, cancelled operations, and
`clear` when there are no patient records do not write a file because they do not change stored data. You do not need
to save manually.

Emergency contacts are saved with the patient records and remain available after restarting Doc. Older valid
data files without emergency contacts still load; their patients initially have no emergency contact.

### Editing the data file

Doc's patient data is saved automatically as a JSON file at `[JAR file location]/data/addressbook.json`. Advanced
users may update the data directly by editing that file.

Each patient has a required `visitDate` and a `previousVisitDates` array of datetime strings in `d/M/yyyy HHmm`
format. Previous dates must be strictly increasing, oldest first, and earlier than the current date. All current
and previous datetimes must be unique across patients. Missing or `null` previous-date arrays in older files load
as empty arrays. Invalid records are rejected without sorting, deduplicating, or dropping entries.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, Doc starts with an empty patient list at the next run. The invalid
file remains on disk until you run a command that triggers saving, such as `list`. Still, we recommend backing up the
file before editing it.<br>
Furthermore, certain edits can cause Doc to behave in unexpected ways (e.g., if a value entered is outside the
acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
Files created by older versions can also be invalid if two patient records use the same visit date and time. Assign a
different visit slot to each record before opening such a file with this version.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: Why does `edit 2 ec/David Tan, 87654321` show an invalid-command-format error?<br>
**A**: Check that the running version supports emergency contacts. Its `edit` usage message should list
`[ec/[NAME, ]PHONE_NUMBER]`. Close the older application before opening the updated JAR; replacing the JAR on disk
does not update an application that is already running. Also check that the index is a positive number and that
there is a space before `ec/`.

**Q**: How do I transfer my data to another computer?<br>
**A**: Install Doc on the other computer and overwrite the data file it creates with the data file from your previous
Doc home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME [dob/DATE] [bt/BLOOD_TYPE] [all/ALLERGIES] [alc/ALCOHOL_HISTORY] [smk/STATUS] p/PHONE_NUMBER a/ADDRESS vd/VISIT_DATE [e/EMAIL] [note/NOTES] [ec/[NAME, ]PHONE_NUMBER] [t/TAG]... ` <br> e.g., `add n/James Ho p/22224444 a/123, Clementi Rd, 1234665 vd/18/9/2026 1000 e/jamesho@example.com t/friend t/colleague`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [dob/DATE] [bt/BLOOD_TYPE] [all/ALLERGIES] [alc/ALCOHOL_HISTORY] [smk/STATUS] [p/PHONE_NUMBER] [ec/[NAME, ]PHONE_NUMBER] [a/ADDRESS] [vd/DATE_TIME] [e/EMAIL] [note/NOTES] [t/TAG]...`<br> e.g., `edit 2 note/Review medication`
**Find**   | `find [n/NAME_KEYWORD...] [p/PHONE_KEYWORD...] [a/ADDRESS_KEYWORD...] [t/TAG_KEYWORD...] [vd/DATE...] [note/NOTE_KEYWORD...]`<br> e.g., `find n/James Jake a/clementi`, `find vd/18/9/2026`, `find note/insulin`
**List**   | `list`
**Next**   | `next INDEX DATETIME`<br> e.g., `next 1 1/11/2026 0930` (current visit must have arrived; choose an available future appointment)
**Prev**   | `prev INDEX`<br> e.g., `prev 1` (requires a previous datetime; replaces and discards the current datetime; no redo)
**Sort**   | `sort date [START_DATE]`<br> e.g., `sort date 18/9/2026`
**View**   | `view INDEX`<br> e.g., `view 1` (refreshes details and the calendar label)
**Help**   | `help`
**Exit**   | `exit`
