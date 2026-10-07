package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "Address book has been cleared!";
    public static final String MESSAGE_ALREADY_EMPTY = "Address book is already empty.";
    public static final String MESSAGE_CLEAR_CANCELLED = "Clear cancelled.";
    public static final String MESSAGE_CONFIRM_CLEAR_ONE =
            "Clear 1 entry? Type y to confirm. Any other input cancels.";
    public static final String MESSAGE_CONFIRM_CLEAR_MULTIPLE =
            "Clear %1$d entries? Type y to confirm. Any other input cancels.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);

        int entryCount = model.getAddressBook().getPersonList().size();
        if (entryCount == 0) {
            return CommandResult.withoutSaving(MESSAGE_ALREADY_EMPTY);
        }

        String prompt = entryCount == 1
                ? MESSAGE_CONFIRM_CLEAR_ONE
                : String.format(MESSAGE_CONFIRM_CLEAR_MULTIPLE, entryCount);
        CommandInputRequest inputRequest = CommandInputRequest.createConfirmation(
                prompt, MESSAGE_CLEAR_CANCELLED, this::clearAddressBook);
        return CommandResult.requestInput(inputRequest);
    }

    /**
     * Clears the address book after the user confirms the request.
     */
    private CommandResult clearAddressBook(Model model) {
        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
