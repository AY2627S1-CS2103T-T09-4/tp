package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Deletes a person identified using its displayed index from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the person identified by the index number used in the displayed person list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted person: %1$s";
    public static final String MESSAGE_CONFIRM_DELETE = "Delete %1$s? Type y to confirm. Any other input cancels.";
    public static final String MESSAGE_DELETE_CANCELLED = "Deletion cancelled.";
    public static final String MESSAGE_PERSON_NO_LONGER_EXISTS =
            "The person selected for deletion no longer exists.";

    private final Index targetIndex;

    /**
     * Creates a command that requests confirmation before deleting the person at {@code targetIndex}.
     */
    public DeleteCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToDelete = lastShownList.get(targetIndex.getZeroBased());
        String prompt = String.format(MESSAGE_CONFIRM_DELETE, personToDelete.getName());
        CommandInputRequest inputRequest = CommandInputRequest.createConfirmation(
                prompt, MESSAGE_DELETE_CANCELLED,
                confirmedModel -> deletePerson(confirmedModel, personToDelete));
        return CommandResult.requestInput(inputRequest);
    }

    /**
     * Deletes the person captured when the command was first executed.
     *
     * @throws CommandException If that exact person no longer exists in the address book.
     */
    private CommandResult deletePerson(Model model, Person personToDelete) throws CommandException {
        if (!model.getAddressBook().getPersonList().contains(personToDelete)) {
            throw new CommandException(MESSAGE_PERSON_NO_LONGER_EXISTS);
        }

        model.deletePerson(personToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetIndex.equals(otherDeleteCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
