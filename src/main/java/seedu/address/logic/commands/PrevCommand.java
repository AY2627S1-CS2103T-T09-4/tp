package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;

/**
 * Restores a patient's latest previous visit date and time.
 * Discards the visit date and time stored before rollback.
 */
public class PrevCommand extends Command {

    public static final String COMMAND_WORD = "prev";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Restores the latest previous visit for the patient identified by the displayed index.\n"
            + "The visit date and time stored before rollback is discarded. There is no redo.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_SUCCESS = "Visit date restored for %1$s: %2$s\n"
            + "Discarded visit date and time: %3$s";
    public static final String MESSAGE_NO_PREVIOUS_VISIT_DATES =
            "Patient %1$s has no previous visit dates to restore.";

    private final Index index;

    /**
     * Creates a command that restores the latest previous visit for the patient at {@code index}.
     *
     * @throws NullPointerException If {@code index} is null.
     */
    public PrevCommand(Index index) {
        this.index = requireNonNull(index);
    }

    /**
     * {@inheritDoc}
     * Restores the latest previous datetime without clock restrictions, preserves all other patient fields,
     * and resets the displayed list to stored order. The result requests persistence.
     *
     * @throws CommandException If the displayed index is out of range or the patient has no previous dates.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUpdate = displayedPersons.get(index.getZeroBased());
        if (personToUpdate.getPreviousVisitDates().isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_PREVIOUS_VISIT_DATES, personToUpdate.getName()));
        }

        Person updatedPerson = createUpdatedPerson(personToUpdate);
        model.setPerson(personToUpdate, updatedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return CommandResult.showUpdatedPerson(String.format(MESSAGE_SUCCESS, updatedPerson.getName(),
                updatedPerson.getVisitDate(), personToUpdate.getVisitDate()), updatedPerson);
    }

    /**
     * Returns a replacement patient with the last previous datetime moved to the current datetime.
     * The remaining dates are a subset of the original record, so rollback cannot introduce a visit-time conflict.
     */
    private Person createUpdatedPerson(Person personToUpdate) {
        assert !personToUpdate.getPreviousVisitDates().isEmpty() : "Rollback requires a previous visit date.";
        List<VisitDate> previousVisitDates = new ArrayList<>(personToUpdate.getPreviousVisitDates());
        VisitDate restoredVisitDate = previousVisitDates.removeLast();
        return personToUpdate.copyWithVisitDates(restoredVisitDate, previousVisitDates);
    }

    /**
     * Returns whether both commands target the same displayed index.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof PrevCommand otherPrevCommand && index.equals(otherPrevCommand.index);
    }

    @Override
    public int hashCode() {
        return index.getZeroBased();
    }

    /**
     * Returns a description of the target index.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).toString();
    }
}
