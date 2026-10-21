package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALCOHOL_HISTORY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALLERGIES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BLOOD_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE_OF_BIRTH;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMERGENCY_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SMOKING_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VISIT_DATE;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Address;
import seedu.address.model.person.AlcoholHistory;
import seedu.address.model.person.Allergies;
import seedu.address.model.person.BloodType;
import seedu.address.model.person.DateOfBirth;
import seedu.address.model.person.Email;
import seedu.address.model.person.EmergencyContact;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.SmokingStatus;
import seedu.address.model.person.VisitDate;
import seedu.address.model.tag.Tag;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the index number used in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_DATE_OF_BIRTH + "DATE] "
            + "[" + PREFIX_BLOOD_TYPE + "BLOOD_TYPE] "
            + "[" + PREFIX_ALLERGIES + "ALLERGIES] "
            + "[" + PREFIX_ALCOHOL_HISTORY + "ALCOHOL_HISTORY] "
            + "[" + PREFIX_SMOKING_STATUS + "STATUS] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMERGENCY_CONTACT + "[NAME, ]PHONE_NUMBER] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_VISIT_DATE + "DATE_TIME] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_NOTE + "NOTES] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_NAME + "John Doe "
            + PREFIX_DATE_OF_BIRTH + "15/6/1990 "
            + PREFIX_BLOOD_TYPE + "A+ "
            + PREFIX_ALLERGIES + "Penicillin; latex "
            + PREFIX_ALCOHOL_HISTORY + "Occasional drinking "
            + PREFIX_SMOKING_STATUS + "former "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_VISIT_DATE + "8/10/2026 1000 "
            + PREFIX_EMAIL + "johndoe@example.com "
            + PREFIX_NOTE + "Review medication";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited person: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "A patient with the same name and phone number already exists in the address book. "
                    + "The existing patient is shown in the patient details panel.";
    public static final String MESSAGE_DUPLICATE_VISIT_DATE =
            "Another patient already has a current or previous visit at %1$s. "
                    + "The patient is shown in the patient details panel. Choose a different date and time.";
    public static final String MESSAGE_VISIT_DATE_NOT_AFTER_PREVIOUS =
            "The current visit date and time must be later than the latest previous visit (%1$s).";
    public static final String MESSAGE_CONFIRM_DUPLICATE_NAME =
            "A patient named %1$s already exists. The matching patient is shown in the patient details panel. "
                    + "Apply this edit anyway? Type y to confirm. Any other input cancels.";
    public static final String MESSAGE_CONFIRM_DUPLICATE_NAMES =
            "%2$d patients named %1$s already exist. Only one matching patient is shown in the patient details "
                    + "panel. Apply this edit anyway? Type y to confirm. Any other input cancels.";
    public static final String MESSAGE_CONFIRM_FULL_DAY =
            "Heads up: you already have %1$d visits on %2$s. Schedule %3$s on that day anyway? "
                    + "Type y to confirm. Any other input cancels.";
    public static final String MESSAGE_EDIT_CANCELLED = "Edit cancelled.";
    public static final String MESSAGE_PERSON_NO_LONGER_EXISTS =
            "The person selected for editing no longer exists.";

    private static final Logger logger = LogsCenter.getLogger(EditCommand.class);

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * Creates a command that edits the person at the displayed index.
     *
     * @param index Index of the person in the displayed person list.
     * @param editPersonDescriptor Details to edit the person with.
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    /**
     * {@inheritDoc}
     * Replaces the person at the displayed index and restores the full list in stored order.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        validateNoConflicts(model, personToEdit, editedPerson);

        boolean isNormalizedNameChanged = !personToEdit.hasSameName(editedPerson);
        if (isNormalizedNameChanged) {
            List<Person> nameDuplicates =
                    DuplicatePersonFinder.findNameDuplicates(model.getAddressBook(), editedPerson);
            if (!nameDuplicates.isEmpty()) {
                String prompt = formatDuplicateNamePrompt(editedPerson, nameDuplicates.size());
                return requestConfirmation(prompt,
                        confirmedModel -> editPersonAfterVisitDayCheck(confirmedModel, personToEdit, editedPerson),
                        nameDuplicates.getFirst());
            }
        }

        return editPersonAfterVisitDayCheck(model, personToEdit, editedPerson);
    }

    /**
     * Replaces the person, first asking for confirmation if the visit moves to another day that already has
     * {@value Model#MAX_VISITS_PER_DAY} or more visits. Edits that keep the visit on the same day never ask,
     * since they do not add to that day's visits.
     *
     * @throws CommandException If the captured person no longer exists or the edit would create a conflict.
     */
    private CommandResult editPersonAfterVisitDayCheck(Model model, Person personToEdit, Person editedPerson)
            throws CommandException {
        boolean isVisitDayChanged = !personToEdit.getVisitDate().isSameDay(editedPerson.getVisitDate());
        if (!isVisitDayChanged) {
            return editPerson(model, personToEdit, editedPerson);
        }

        int visitsOnNewDay = model.countPersonsWithSameVisitDay(editedPerson);
        if (visitsOnNewDay >= Model.MAX_VISITS_PER_DAY) {
            String prompt = String.format(MESSAGE_CONFIRM_FULL_DAY, visitsOnNewDay,
                    editedPerson.getVisitDate().toDayString(), editedPerson.getName());
            return requestConfirmation(prompt,
                    confirmedModel -> editPerson(confirmedModel, personToEdit, editedPerson));
        }

        return editPerson(model, personToEdit, editedPerson);
    }

    /**
     * Returns a result asking the user to confirm with {@code y} before running {@code confirmedAction}.
     */
    private static CommandResult requestConfirmation(String prompt,
            CommandInputRequest.ConfirmedAction confirmedAction) {
        CommandInputRequest inputRequest = createConfirmationRequest(prompt, confirmedAction);
        return CommandResult.requestInput(inputRequest);
    }

    /**
     * Returns a result that also displays one matching patient while requesting confirmation.
     */
    private static CommandResult requestConfirmation(String prompt,
            CommandInputRequest.ConfirmedAction confirmedAction, Person personToView) {
        CommandInputRequest inputRequest = createConfirmationRequest(prompt, confirmedAction);
        return CommandResult.requestInput(inputRequest, personToView);
    }

    /**
     * Returns a confirmation request that edits the person only after a {@code y} response.
     */
    private static CommandInputRequest createConfirmationRequest(String prompt,
            CommandInputRequest.ConfirmedAction confirmedAction) {
        return CommandInputRequest.createConfirmation(prompt, MESSAGE_EDIT_CANCELLED, confirmedAction);
    }

    /**
     * Replaces the person captured when this command was first executed.
     *
     * @throws CommandException If the captured person no longer exists or the edit would create a conflict.
     */
    private CommandResult editPerson(Model model, Person personToEdit, Person editedPerson) throws CommandException {
        if (!model.getAddressBook().getPersonList().contains(personToEdit)) {
            throw new CommandException(MESSAGE_PERSON_NO_LONGER_EXISTS);
        }

        validateNoConflicts(model, personToEdit, editedPerson);

        model.setPerson(personToEdit, editedPerson);
        logOptionalFieldUpdates();
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)));
    }

    /**
     * Logs supplied optional-field updates after the model accepts the edit, without including patient values.
     */
    private void logOptionalFieldUpdates() {
        editPersonDescriptor.getDateOfBirth().ifPresent(dateOfBirth -> logger.fine(dateOfBirth.isEmpty()
                ? "Cleared patient date of birth in model" : "Set patient date of birth in model"));
        editPersonDescriptor.getBloodType().ifPresent(bloodType -> logger.fine(bloodType.isEmpty()
                ? "Cleared patient blood type in model" : "Set patient blood type in model"));
        editPersonDescriptor.getAllergies().ifPresent(allergies -> logger.fine(allergies.isEmpty()
                ? "Cleared patient allergies in model" : "Set patient allergies in model"));
        editPersonDescriptor.getAlcoholHistory().ifPresent(alcoholHistory -> logger.fine(alcoholHistory.isEmpty()
                ? "Cleared patient alcohol history in model" : "Set patient alcohol history in model"));
        editPersonDescriptor.getSmokingStatus().ifPresent(smokingStatus -> logger.fine(smokingStatus.isEmpty()
                ? "Cleared patient smoking status in model" : "Set patient smoking status in model"));
        editPersonDescriptor.getEmergencyContact().ifPresent(emergencyContact -> logger.fine(emergencyContact.isEmpty()
                ? "Cleared patient emergency contact in model" : "Set patient emergency contact in model"));
    }

    /**
     * Checks that the edited person has neither an identity nor a visit-date conflict with another patient.
     */
    private static void validateNoConflicts(Model model, Person personToEdit, Person editedPerson)
            throws CommandException {
        if (!personToEdit.isSamePerson(editedPerson)) {
            Optional<Person> identityDuplicate =
                    DuplicatePersonFinder.findIdentityDuplicate(model.getAddressBook(), editedPerson);
            if (identityDuplicate.isPresent()) {
                throw new CommandException(MESSAGE_DUPLICATE_PERSON, identityDuplicate.get());
            }
        }

        boolean isVisitDateChanged = !personToEdit.getVisitDate().equals(editedPerson.getVisitDate());
        if (isVisitDateChanged) {
            Optional<Person> visitDateDuplicate = DuplicatePersonFinder.findVisitDateDuplicate(
                    model.getAddressBook(), editedPerson, personToEdit);
            if (visitDateDuplicate.isPresent()) {
                throw new CommandException(String.format(MESSAGE_DUPLICATE_VISIT_DATE, editedPerson.getVisitDate()),
                        visitDateDuplicate.get());
            }
        }
    }

    /**
     * Returns a grammatically correct confirmation prompt for the duplicate-name matches.
     */
    private static String formatDuplicateNamePrompt(Person editedPerson, int duplicateCount) {
        assert duplicateCount > 0;

        return (duplicateCount == 1)
                ? String.format(MESSAGE_CONFIRM_DUPLICATE_NAME, editedPerson.getName())
                : String.format(MESSAGE_CONFIRM_DUPLICATE_NAMES, editedPerson.getName(), duplicateCount);
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     *
     * @throws CommandException If the proposed current date would invalidate the retained previous dates.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor)
            throws CommandException {
        assert personToEdit != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        DateOfBirth updatedDateOfBirth = editPersonDescriptor.getDateOfBirth().orElse(personToEdit.getDateOfBirth());
        BloodType updatedBloodType = editPersonDescriptor.getBloodType().orElse(personToEdit.getBloodType());
        Allergies updatedAllergies = editPersonDescriptor.getAllergies().orElse(personToEdit.getAllergies());
        AlcoholHistory updatedAlcoholHistory = editPersonDescriptor.getAlcoholHistory()
                .orElse(personToEdit.getAlcoholHistory());
        SmokingStatus updatedSmokingStatus = editPersonDescriptor.getSmokingStatus()
                .orElse(personToEdit.getSmokingStatus());
        Phone updatedPhone = editPersonDescriptor.getPhone().orElse(personToEdit.getPhone());
        Email updatedEmail = editPersonDescriptor.getEmail().orElse(personToEdit.getEmail());
        Address updatedAddress = editPersonDescriptor.getAddress().orElse(personToEdit.getAddress());
        VisitDate updatedVisitDate = editPersonDescriptor.getVisitDate().orElse(personToEdit.getVisitDate());
        Note updatedNote = editPersonDescriptor.getNote().orElse(personToEdit.getNote());
        EmergencyContact updatedEmergencyContact = editPersonDescriptor.getEmergencyContact()
                .orElse(personToEdit.getEmergencyContact());
        Set<Tag> updatedTags = editPersonDescriptor.getTags().orElse(personToEdit.getTags());

        List<VisitDate> previousVisitDates = personToEdit.getPreviousVisitDates();
        if (!previousVisitDates.isEmpty()
                && !updatedVisitDate.value.isAfter(previousVisitDates.getLast().value)) {
            throw new CommandException(String.format(MESSAGE_VISIT_DATE_NOT_AFTER_PREVIOUS,
                    previousVisitDates.getLast()));
        }

        return new Person(updatedName, updatedDateOfBirth, updatedBloodType, updatedAllergies, updatedAlcoholHistory,
                updatedSmokingStatus, updatedPhone, updatedEmail, updatedAddress, updatedVisitDate, previousVisitDates,
                updatedNote, updatedEmergencyContact, updatedTags);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand otherEditCommand)) {
            return false;
        }

        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each supplied field replaces the corresponding
     * value, including explicitly empty optional fields and tag sets.
     * Omitted fields remain unchanged.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private DateOfBirth dateOfBirth;
        private BloodType bloodType;
        private Allergies allergies;
        private AlcoholHistory alcoholHistory;
        private SmokingStatus smokingStatus;
        private Phone phone;
        private Email email;
        private Address address;
        private VisitDate visitDate;
        private Note note;
        private EmergencyContact emergencyContact;
        private Set<Tag> tags;

        /**
         * Creates an empty descriptor with no fields to replace.
         */
        public EditPersonDescriptor() {}

        /**
         * Creates a copy of the supplied descriptor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setDateOfBirth(toCopy.dateOfBirth);
            setBloodType(toCopy.bloodType);
            setAllergies(toCopy.allergies);
            setAlcoholHistory(toCopy.alcoholHistory);
            setSmokingStatus(toCopy.smokingStatus);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setAddress(toCopy.address);
            setVisitDate(toCopy.visitDate);
            setNote(toCopy.note);
            setEmergencyContact(toCopy.emergencyContact);
            setTags(toCopy.tags);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, dateOfBirth, bloodType, allergies, alcoholHistory, smokingStatus,
                    phone, email, address, visitDate, note, emergencyContact, tags);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setDateOfBirth(DateOfBirth dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public Optional<DateOfBirth> getDateOfBirth() {
            return Optional.ofNullable(dateOfBirth);
        }

        public void setBloodType(BloodType bloodType) {
            this.bloodType = bloodType;
        }

        public Optional<BloodType> getBloodType() {
            return Optional.ofNullable(bloodType);
        }

        public void setAllergies(Allergies allergies) {
            this.allergies = allergies;
        }

        public Optional<Allergies> getAllergies() {
            return Optional.ofNullable(allergies);
        }

        public void setAlcoholHistory(AlcoholHistory alcoholHistory) {
            this.alcoholHistory = alcoholHistory;
        }

        public Optional<AlcoholHistory> getAlcoholHistory() {
            return Optional.ofNullable(alcoholHistory);
        }

        public void setSmokingStatus(SmokingStatus smokingStatus) {
            this.smokingStatus = smokingStatus;
        }

        public Optional<SmokingStatus> getSmokingStatus() {
            return Optional.ofNullable(smokingStatus);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }

        public void setVisitDate(VisitDate visitDate) {
            this.visitDate = visitDate;
        }

        public Optional<VisitDate> getVisitDate() {
            return Optional.ofNullable(visitDate);
        }

        public void setEmergencyContact(EmergencyContact emergencyContact) {
            this.emergencyContact = emergencyContact;
        }

        public Optional<EmergencyContact> getEmergencyContact() {
            return Optional.ofNullable(emergencyContact);
        }

        public void setNote(Note note) {
            this.note = note;
        }

        public Optional<Note> getNote() {
            return Optional.ofNullable(note);
        }

        /**
         * Sets {@code tags} to this object's {@code tags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setTags(Set<Tag> tags) {
            this.tags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Tag>> getTags() {
            return (tags != null) ? Optional.of(Collections.unmodifiableSet(tags)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor otherEditPersonDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(dateOfBirth, otherEditPersonDescriptor.dateOfBirth)
                    && Objects.equals(bloodType, otherEditPersonDescriptor.bloodType)
                    && Objects.equals(allergies, otherEditPersonDescriptor.allergies)
                    && Objects.equals(alcoholHistory, otherEditPersonDescriptor.alcoholHistory)
                    && Objects.equals(smokingStatus, otherEditPersonDescriptor.smokingStatus)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(address, otherEditPersonDescriptor.address)
                    && Objects.equals(visitDate, otherEditPersonDescriptor.visitDate)
                    && Objects.equals(note, otherEditPersonDescriptor.note)
                    && Objects.equals(emergencyContact, otherEditPersonDescriptor.emergencyContact)
                    && Objects.equals(tags, otherEditPersonDescriptor.tags);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("dateOfBirth", dateOfBirth)
                    .add("bloodType", bloodType)
                    .add("allergies", allergies)
                    .add("alcoholHistory", alcoholHistory)
                    .add("smokingStatus", smokingStatus)
                    .add("phone", phone)
                    .add("email", email)
                    .add("address", address)
                    .add("visitDate", visitDate)
                    .add("note", note)
                    .add("emergencyContact", emergencyContact)
                    .add("tags", tags)
                    .toString();
        }
    }
}
