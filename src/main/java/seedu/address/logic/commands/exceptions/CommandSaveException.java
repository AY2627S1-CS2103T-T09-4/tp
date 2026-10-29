package seedu.address.logic.commands.exceptions;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import seedu.address.logic.commands.CommandResult;

/**
 * Reports a save failure after a command has completed its in-memory changes.
 * Retains the command result so the UI can reflect those changes while displaying the save error.
 */
public class CommandSaveException extends CommandException {

    private final CommandResult commandResult;

    /**
     * Constructs a save failure with the completed command's result.
     *
     * @param message Save error to display.
     * @param cause Storage failure that prevented saving.
     * @param commandResult Result describing the completed in-memory operation.
     */
    public CommandSaveException(String message, IOException cause, CommandResult commandResult) {
        super(message, requireNonNull(cause));
        this.commandResult = requireNonNull(commandResult);
    }

    public CommandResult getCommandResult() {
        return commandResult;
    }
}
