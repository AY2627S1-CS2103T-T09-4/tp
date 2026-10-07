package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /** The current address book state should be saved. */
    private final boolean saveAddressBook;

    /** The next command-line input should be handled by this request. */
    private final CommandInputRequest inputRequest;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, true, null);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, boolean saveAddressBook,
            CommandInputRequest inputRequest) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.saveAddressBook = saveAddressBook;
        this.inputRequest = inputRequest;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    /**
     * Creates a result that displays the request prompt and waits for the next command-line input.
     * The current address book state is not saved for this result.
     *
     * @param inputRequest Request that will handle the next input.
     * @return Result containing the input request.
     */
    public static CommandResult requestInput(CommandInputRequest inputRequest) {
        requireNonNull(inputRequest);
        return new CommandResult(inputRequest.getPrompt(), false, false, false, inputRequest);
    }

    /**
     * Creates a result that displays feedback without saving the current address book state.
     *
     * @param feedbackToUser Feedback to display.
     * @return Result that does not trigger an address book save.
     */
    public static CommandResult withoutSaving(String feedbackToUser) {
        return new CommandResult(feedbackToUser, false, false, false, null);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    public boolean shouldSaveAddressBook() {
        return saveAddressBook;
    }

    public Optional<CommandInputRequest> getInputRequest() {
        return Optional.ofNullable(inputRequest);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && saveAddressBook == otherCommandResult.saveAddressBook
                && (inputRequest != null) == (otherCommandResult.inputRequest != null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, saveAddressBook, inputRequest != null);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("saveAddressBook", saveAddressBook)
                .add("inputRequested", inputRequest != null)
                .toString();
    }

}
