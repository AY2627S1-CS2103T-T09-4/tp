package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a request for the next command-line input to be handled without command parsing.
 * A response handler may finish the interaction or return another input request.
 */
public class CommandInputRequest {

    private static final String CONFIRMATION_RESPONSE = "y";

    private final String prompt;
    private final ResponseHandler responseHandler;

    /**
     * Creates an input request with the prompt to display and the handler for the next input.
     *
     * @param prompt Prompt shown in the command result area.
     * @param responseHandler Handler that consumes the next command-line input.
     */
    public CommandInputRequest(String prompt, ResponseHandler responseHandler) {
        this.prompt = requireNonNull(prompt);
        this.responseHandler = requireNonNull(responseHandler);
    }

    /**
     * Creates a request that runs {@code confirmedAction} only when the trimmed response is {@code y},
     * ignoring case. Every other response returns the supplied cancellation message without saving.
     *
     * @param prompt Prompt shown in the command result area.
     * @param cancellationMessage Feedback shown when the user does not confirm.
     * @param confirmedAction Action to run after confirmation.
     * @return Confirmation input request.
     */
    public static CommandInputRequest createConfirmation(String prompt, String cancellationMessage,
            ConfirmedAction confirmedAction) {
        requireNonNull(cancellationMessage);
        requireNonNull(confirmedAction);

        return new CommandInputRequest(prompt, (input, model) -> {
            if (input.trim().equalsIgnoreCase(CONFIRMATION_RESPONSE)) {
                return confirmedAction.execute(model);
            }
            return CommandResult.withoutSaving(cancellationMessage);
        });
    }

    public String getPrompt() {
        return prompt;
    }

    /**
     * Handles the next command-line input using this request's response handler.
     *
     * @param input Raw input entered by the user.
     * @param model Model on which the response may act.
     * @return Result of handling the input.
     * @throws CommandException If the response cannot be handled.
     */
    public CommandResult respond(String input, Model model) throws CommandException {
        requireNonNull(input);
        requireNonNull(model);
        return requireNonNull(responseHandler.handle(input, model));
    }

    /**
     * Handles input supplied in response to a {@link CommandInputRequest}.
     */
    @FunctionalInterface
    public interface ResponseHandler {
        /**
         * Handles the supplied input and returns the resulting command outcome.
         *
         * @param input Raw input entered by the user.
         * @param model Model on which the response may act.
         * @return Result of handling the input.
         * @throws CommandException If the response cannot be handled.
         */
        CommandResult handle(String input, Model model) throws CommandException;
    }

    /**
     * Runs an action after the user confirms a request.
     */
    @FunctionalInterface
    public interface ConfirmedAction {
        /**
         * Executes the confirmed action on the model.
         *
         * @param model Model on which to run the action.
         * @return Result of the confirmed action.
         * @throws CommandException If the action cannot be completed.
         */
        CommandResult execute(Model model) throws CommandException;
    }
}
