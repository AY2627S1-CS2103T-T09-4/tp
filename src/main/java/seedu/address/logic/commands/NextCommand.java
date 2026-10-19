package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;

/**
 * Replaces a patient's visit date and time with an available future appointment.
 * Does not record a completed visit or visit history.
 */
public class NextCommand extends Command {

    public static final String COMMAND_WORD = "next";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Schedules the next visit for the patient identified by the displayed index.\n"
            + "Parameters: INDEX (must be a positive integer) DATETIME (d/M/yyyy HHmm, must be in the future)\n"
            + "Example: " + COMMAND_WORD + " 1 1/11/2026 0930";
    public static final String MESSAGE_SUCCESS = "Next visit scheduled for patient %1$s: %2$s";
    public static final String MESSAGE_UNCHANGED_VISIT_DATE = "Patient %1$s already has an appointment at %2$s.";
    public static final String MESSAGE_NON_FUTURE_VISIT_DATE = "The next visit must be in the future.";
    public static final String MESSAGE_DUPLICATE_VISIT_DATE =
            "Patient %1$s already has an appointment at %2$s. Choose a different date and time.";

    private final Index index;
    private final VisitDate visitDate;
    private final Clock clock;

    /**
     * Creates a command using the computer's local clock.
     */
    public NextCommand(Index index, VisitDate visitDate) {
        this(index, visitDate, Clock.systemDefaultZone());
    }

    /**
     * Creates a command using the supplied clock to validate the appointment at execution time.
     */
    public NextCommand(Index index, VisitDate visitDate, Clock clock) {
        requireAllNonNull(index, visitDate, clock);
        this.index = index;
        this.visitDate = visitDate;
        this.clock = clock;
    }

    /**
     * {@inheritDoc}
     * Updates only the visit date and restores the full displayed list in stored order after validation.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUpdate = displayedPersons.get(index.getZeroBased());
        validateVisitDate(model, personToUpdate);

        Person updatedPerson = new Person(personToUpdate.getName(), personToUpdate.getDateOfBirth(),
                personToUpdate.getPhone(), personToUpdate.getEmail(), personToUpdate.getAddress(), visitDate,
                personToUpdate.getNote(), personToUpdate.getEmergencyContact(), personToUpdate.getTags());
        model.setPerson(personToUpdate, updatedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_SUCCESS, updatedPerson.getName(), visitDate));
    }

    /**
     * Rejects unchanged or non-future appointments and identifies conflicts across the full stored list.
     */
    private void validateVisitDate(Model model, Person personToUpdate) throws CommandException {
        if (visitDate.equals(personToUpdate.getVisitDate())) {
            throw new CommandException(String.format(MESSAGE_UNCHANGED_VISIT_DATE,
                    personToUpdate.getName(), visitDate));
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (!visitDate.value.isAfter(now)) {
            throw new CommandException(MESSAGE_NON_FUTURE_VISIT_DATE);
        }

        // The shared boolean conflict check cannot identify the patient for the error message.
        for (Person person : model.getAddressBook().getPersonList()) {
            if (!person.equals(personToUpdate) && visitDate.equals(person.getVisitDate())) {
                throw new CommandException(String.format(MESSAGE_DUPLICATE_VISIT_DATE, person.getName(), visitDate));
            }
        }
    }

    /**
     * Returns whether both commands have the same target, appointment, and validation clock.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof NextCommand otherNextCommand)) {
            return false;
        }
        return index.equals(otherNextCommand.index)
                && visitDate.equals(otherNextCommand.visitDate)
                && clock.equals(otherNextCommand.clock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), visitDate, clock);
    }

    /**
     * Returns a description of the target index and requested appointment.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("visitDate", visitDate).toString();
    }
}
