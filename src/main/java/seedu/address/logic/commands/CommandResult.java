package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    /** Describes how a command result changes the patient details panel. */
    public enum DetailsPanelAction {
        KEEP,
        SHOW,
        CLEAR,
        CLEAR_IF_SHOWING
    }

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /** The current address book state should be saved. */
    private final boolean saveAddressBook;

    /** The next command-line input should be handled by this request. */
    private final CommandInputRequest inputRequest;

    /** Change to apply to the patient details panel. */
    private final DetailsPanelAction detailsPanelAction;

    /** Patient associated with a show or conditional-clear action. */
    private final Person detailsPerson;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, true, null, DetailsPanelAction.KEEP, null);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, boolean saveAddressBook,
            CommandInputRequest inputRequest, DetailsPanelAction detailsPanelAction, Person detailsPerson) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.saveAddressBook = saveAddressBook;
        this.inputRequest = inputRequest;
        this.detailsPanelAction = requireNonNull(detailsPanelAction);
        this.detailsPerson = detailsPerson;
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
        return new CommandResult(inputRequest.getPrompt(), false, false, false, inputRequest,
                DetailsPanelAction.KEEP, null);
    }

    /**
     * Creates a result that displays a request prompt and a person's details while waiting for the next input.
     * The current address book state is not saved for this result.
     *
     * @param inputRequest Request that will handle the next input.
     * @param personToView Person whose details should be displayed.
     * @return Result containing the input request and person to display.
     */
    public static CommandResult requestInput(CommandInputRequest inputRequest, Person personToView) {
        requireNonNull(inputRequest);
        requireNonNull(personToView);
        return new CommandResult(inputRequest.getPrompt(), false, false, false, inputRequest,
                DetailsPanelAction.SHOW, personToView);
    }

    /**
     * Creates a result that displays feedback without saving the current address book state.
     *
     * @param feedbackToUser Feedback to display.
     * @return Result that does not trigger an address book save.
     */
    public static CommandResult withoutSaving(String feedbackToUser) {
        return new CommandResult(feedbackToUser, false, false, false, null, DetailsPanelAction.KEEP, null);
    }

    /**
     * Creates a result that displays the specified person's details without saving the address book.
     *
     * @param feedbackToUser Feedback to display.
     * @param personToView Person whose details should be displayed.
     * @return Result containing the person to display.
     */
    public static CommandResult viewPerson(String feedbackToUser, Person personToView) {
        requireNonNull(personToView);
        return new CommandResult(feedbackToUser, false, false, false, null,
                DetailsPanelAction.SHOW, personToView);
    }

    /**
     * Creates a saving result that shows the added or updated patient, even if persistence fails.
     *
     * @param feedbackToUser Feedback to display.
     * @param personToView Added or updated patient to display.
     * @return Saving result that displays the patient.
     */
    public static CommandResult showUpdatedPerson(String feedbackToUser, Person personToView) {
        requireNonNull(personToView);
        return new CommandResult(feedbackToUser, false, false, true, null,
                DetailsPanelAction.SHOW, personToView);
    }

    /**
     * Creates a saving result that clears the details panel if it shows the deleted patient, even if saving fails.
     *
     * @param feedbackToUser Feedback to display.
     * @param deletedPerson Deleted patient whose details should be cleared.
     * @return Saving result that conditionally clears the details panel.
     */
    public static CommandResult clearPersonIfShowing(String feedbackToUser, Person deletedPerson) {
        requireNonNull(deletedPerson);
        return new CommandResult(feedbackToUser, false, false, true, null,
                DetailsPanelAction.CLEAR_IF_SHOWING, deletedPerson);
    }

    /**
     * Creates a saving result that clears the details panel, even if persistence fails.
     *
     * @param feedbackToUser Feedback to display.
     * @return Saving result that clears the details panel.
     */
    public static CommandResult clearDetailsPanel(String feedbackToUser) {
        return new CommandResult(feedbackToUser, false, false, true, null, DetailsPanelAction.CLEAR, null);
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

    public Optional<Person> getPersonToView() {
        return detailsPanelAction == DetailsPanelAction.SHOW
                ? Optional.of(detailsPerson)
                : Optional.empty();
    }

    public DetailsPanelAction getDetailsPanelAction() {
        return detailsPanelAction;
    }

    /**
     * Returns the deleted patient associated with a conditional-clear action.
     */
    public Optional<Person> getPersonToClearFromView() {
        return detailsPanelAction == DetailsPanelAction.CLEAR_IF_SHOWING
                ? Optional.of(detailsPerson)
                : Optional.empty();
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
                && (inputRequest != null) == (otherCommandResult.inputRequest != null)
                && detailsPanelAction == otherCommandResult.detailsPanelAction
                && Objects.equals(detailsPerson, otherCommandResult.detailsPerson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, saveAddressBook, inputRequest != null,
                detailsPanelAction, detailsPerson);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("saveAddressBook", saveAddressBook)
                .add("inputRequested", inputRequest != null)
                .add("detailsPanelAction", detailsPanelAction)
                .add("detailsPerson", detailsPerson)
                .toString();
    }

}
