package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;

public class CommandResultTest {
    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));

        // different saveAddressBook value -> returns false
        assertFalse(commandResult.equals(CommandResult.withoutSaving("feedback")));

        // different input-request state -> returns false
        CommandInputRequest inputRequest = new CommandInputRequest(
                "feedback", (input, model) -> CommandResult.withoutSaving(input));
        assertFalse(CommandResult.withoutSaving("feedback").equals(CommandResult.requestInput(inputRequest)));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());

        // different saveAddressBook value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), CommandResult.withoutSaving("feedback").hashCode());

        // different input-request state -> returns different hashcode
        CommandInputRequest inputRequest = new CommandInputRequest(
                "feedback", (input, model) -> CommandResult.withoutSaving(input));
        assertNotEquals(CommandResult.withoutSaving("feedback").hashCode(),
                CommandResult.requestInput(inputRequest).hashCode());
    }

    @Test
    public void requestInput_validRequest_returnsNonSavingResultWithRequest() {
        CommandInputRequest inputRequest = new CommandInputRequest(
                "Enter value", (input, model) -> CommandResult.withoutSaving(input));

        CommandResult result = CommandResult.requestInput(inputRequest);

        assertEquals("Enter value", result.getFeedbackToUser());
        assertFalse(result.shouldSaveAddressBook());
        assertEquals(inputRequest, result.getInputRequest().orElseThrow());
    }

    @Test
    public void withoutSaving_validFeedback_returnsNonSavingResultWithoutRequest() {
        CommandResult result = CommandResult.withoutSaving("feedback");

        assertFalse(result.shouldSaveAddressBook());
        assertTrue(result.getInputRequest().isEmpty());
    }

    @Test
    public void inputRequest_respondsWithArbitraryInputAndCanContinue() throws Exception {
        CommandInputRequest secondRequest = new CommandInputRequest(
                "Second value", (input, model) -> CommandResult.withoutSaving(input));
        CommandInputRequest firstRequest = new CommandInputRequest(
                "First value", (input, model) -> CommandResult.requestInput(secondRequest));

        CommandResult firstResult = firstRequest.respond("arbitrary input", new ModelManager());
        CommandResult secondResult = firstResult.getInputRequest().orElseThrow()
                .respond("another value", new ModelManager());

        assertEquals("Second value", firstResult.getFeedbackToUser());
        assertEquals("another value", secondResult.getFeedbackToUser());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + ", saveAddressBook="
                + commandResult.shouldSaveAddressBook() + ", inputRequested=false}";
        assertEquals(expected, commandResult.toString());
    }
}
