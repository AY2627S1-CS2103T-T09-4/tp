package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

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
            + ": Requests confirmation to restore the latest previous visit for the displayed patient index.\n"
            + "The visit date and time stored before rollback is discarded. There is no redo.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_SUCCESS = "Visit date restored for %1$s: %2$s\n"
            + "Discarded visit date and time: %3$s";
    public static final String MESSAGE_NO_PREVIOUS_VISIT_DATES =
            "Patient %1$s has no previous visit dates to restore.";
    public static final String MESSAGE_CONFIRM_PREV = "Restore the previous visit for %1$s (phone: %2$s)? "
            + "Type y to confirm. Any other input cancels.\n"
            + "Restore: %3$s\n"
            + "Discard permanently: %4$s";
    public static final String MESSAGE_PREV_CANCELLED = "Visit restoration cancelled.";
    public static final String MESSAGE_PERSON_CHANGED_OR_REMOVED =
            "The patient selected for rollback has changed or no longer exists. Run prev again.";

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
     * Requests confirmation before restoring the latest previous datetime.
     * The captured patient is restored only after confirmation, without clock restrictions.
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

        VisitDate restoredVisitDate = personToUpdate.getPreviousVisitDates().getLast();
        String prompt = String.format(MESSAGE_CONFIRM_PREV, personToUpdate.getName(), personToUpdate.getPhone(),
                restoredVisitDate, personToUpdate.getVisitDate());
        CommandInputRequest inputRequest = CommandInputRequest.createConfirmation(
                prompt, MESSAGE_PREV_CANCELLED,
                confirmedModel -> restorePreviousVisit(confirmedModel, personToUpdate));
        return CommandResult.requestInput(inputRequest);
    }

    /**
     * Restores the previous visit for the patient captured when the command was first executed.
     *
     * @throws CommandException If the captured patient has changed or no longer exists.
     */
    private CommandResult restorePreviousVisit(Model model, Person personToUpdate) throws CommandException {
        if (!model.getAddressBook().getPersonList().contains(personToUpdate)) {
            throw new CommandException(MESSAGE_PERSON_CHANGED_OR_REMOVED);
        }

        Person updatedPerson = createUpdatedPerson(personToUpdate);
        model.setPerson(personToUpdate, updatedPerson);
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
