package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;

public class CommandInputRequestTest {

    private static final String PROMPT = "Confirm?";
    private static final String CANCELLED = "Cancelled.";

    private final Model model = new ModelManager();

    @Test
    public void respond_arbitraryInput_passesRawInputToHandler() throws Exception {
        CommandInputRequest request = new CommandInputRequest(
                PROMPT, (input, unusedModel) -> CommandResult.withoutSaving(input));

        CommandResult result = request.respond("  arbitrary command  ", model);

        assertEquals("  arbitrary command  ", result.getFeedbackToUser());
    }

    @Test
    public void createConfirmation_trimmedCaseInsensitiveY_executesAction() throws Exception {
        AtomicBoolean wasExecuted = new AtomicBoolean();
        CommandInputRequest request = createConfirmationRequest(wasExecuted);

        CommandResult lowercaseResult = request.respond("  y  ", model);
        assertTrue(wasExecuted.get());
        assertEquals("Confirmed.", lowercaseResult.getFeedbackToUser());

        wasExecuted.set(false);
        request.respond("Y", model);
        assertTrue(wasExecuted.get());
    }

    @Test
    public void createConfirmation_otherResponses_cancelWithoutExecutingAction() throws Exception {
        for (String response : new String[]{"", "n", "yes", "list"}) {
            AtomicBoolean wasExecuted = new AtomicBoolean();
            CommandInputRequest request = createConfirmationRequest(wasExecuted);

            CommandResult result = request.respond(response, model);

            assertFalse(wasExecuted.get());
            assertEquals(CANCELLED, result.getFeedbackToUser());
            assertFalse(result.shouldSaveAddressBook());
        }
    }

    private CommandInputRequest createConfirmationRequest(AtomicBoolean wasExecuted) {
        return CommandInputRequest.createConfirmation(PROMPT, CANCELLED, unusedModel -> {
            wasExecuted.set(true);
            return new CommandResult("Confirmed.");
        });
    }
}
