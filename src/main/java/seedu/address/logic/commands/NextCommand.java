package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
 * Retains a patient's current visit date and time before scheduling an available future appointment.
 * The current visit must have arrived or passed; retained dates do not verify attendance or completion.
 */
public class NextCommand extends Command {

    public static final String COMMAND_WORD = "next";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Schedules the next visit for the patient identified by the displayed index.\n"
            + "The current visit must have arrived or passed. Use edit to reschedule a future current visit.\n"
            + "Parameters: INDEX (must be a positive integer) DATETIME (d/M/yyyy HHmm, must be in the future)\n"
            + "Example: " + COMMAND_WORD + " 1 1/11/2026 0930";
    public static final String MESSAGE_SUCCESS = "Next visit scheduled for patient %1$s: %2$s";
    public static final String MESSAGE_UNCHANGED_VISIT_DATE = "Patient %1$s already has an appointment at %2$s.";
    public static final String MESSAGE_NON_FUTURE_VISIT_DATE =
            "The next visit must be later than the current time (%1$s). Choose a later date and time.";
    public static final String MESSAGE_CURRENT_VISIT_IN_FUTURE =
            "Patient %1$s has a current visit at %2$s that is still in the future. Use edit to reschedule it.";
    public static final String MESSAGE_DUPLICATE_VISIT_DATE =
            "Patient %1$s already has a current or previous visit at %2$s. Choose a different date and time.";

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
     * Appends the current visit to the previous dates, replaces it with the requested future visit,
     * and restores the full displayed list in stored order after validation.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUpdate = displayedPersons.get(index.getZeroBased());
        validateVisitDate(personToUpdate);

        Person updatedPerson = createUpdatedPerson(personToUpdate);
        validateNoConflicts(model, personToUpdate, updatedPerson);
        model.setPerson(personToUpdate, updatedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_SUCCESS, updatedPerson.getName(), visitDate));
    }

    /**
     * Returns a replacement patient with the current date appended to the previous dates.
     * All fields other than the current and previous visit dates are preserved.
     */
    private Person createUpdatedPerson(Person personToUpdate) {
        List<VisitDate> previousVisitDates = new ArrayList<>(personToUpdate.getPreviousVisitDates());
        previousVisitDates.add(personToUpdate.getVisitDate());
        return new Person(personToUpdate.getName(), personToUpdate.getPhone(),
                personToUpdate.getEmail(), personToUpdate.getAddress(), visitDate,
                previousVisitDates, personToUpdate.getNote(), personToUpdate.getTags());
    }

    /**
     * Requires the current visit to be at or before now and the requested visit to be strictly after now.
     * Reads the execution-time clock once for both comparisons, retaining its full precision.
     */
    private void validateVisitDate(Person personToUpdate) throws CommandException {
        if (visitDate.equals(personToUpdate.getVisitDate())) {
            throw new CommandException(String.format(MESSAGE_UNCHANGED_VISIT_DATE,
                    personToUpdate.getName(), visitDate));
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (personToUpdate.getVisitDate().value.isAfter(now)) {
            throw new CommandException(String.format(MESSAGE_CURRENT_VISIT_IN_FUTURE,
                    personToUpdate.getName(), personToUpdate.getVisitDate()));
        }
        if (!visitDate.value.isAfter(now)) {
            throw new CommandException(String.format(MESSAGE_NON_FUTURE_VISIT_DATE, now.format(VisitDate.FORMATTER)));
        }
    }

    /**
     * Checks the full replacement record against every other stored patient's visit record.
     */
    private void validateNoConflicts(Model model, Person personToUpdate, Person updatedPerson) throws CommandException {
        // The shared boolean conflict check cannot identify the patient for the error message.
        for (Person person : model.getAddressBook().getPersonList()) {
            if (!person.equals(personToUpdate) && updatedPerson.hasCommonVisitDate(person)) {
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
