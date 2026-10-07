package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_emptyAddressBook_returnsAlreadyEmptyWithoutRequest() {
        Model model = new ModelManager();

        CommandResult result = new ClearCommand().execute(model);

        assertEquals(ClearCommand.MESSAGE_ALREADY_EMPTY, result.getFeedbackToUser());
        assertTrue(result.getInputRequest().isEmpty());
        assertFalse(result.shouldSaveAddressBook());
    }

    @Test
    public void execute_oneEntry_requestsSingularConfirmation() {
        Model model = new ModelManager();
        model.addPerson(ALICE);

        CommandResult result = new ClearCommand().execute(model);

        assertEquals(ClearCommand.MESSAGE_CONFIRM_CLEAR_ONE, result.getFeedbackToUser());
        assertTrue(result.getInputRequest().isPresent());
        assertTrue(model.hasPerson(ALICE));
    }

    @Test
    public void execute_multipleEntries_requestsPluralConfirmation() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        int entryCount = model.getAddressBook().getPersonList().size();

        CommandResult result = new ClearCommand().execute(model);

        assertEquals(String.format(ClearCommand.MESSAGE_CONFIRM_CLEAR_MULTIPLE, entryCount),
                result.getFeedbackToUser());
        assertFalse(result.shouldSaveAddressBook());
        assertEquals(entryCount, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void confirmation_lowercaseY_clearsAddressBook() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        CommandResult requestResult = new ClearCommand().execute(model);

        CommandResult confirmedResult = requestResult.getInputRequest().orElseThrow().respond(" y ", model);

        assertEquals(ClearCommand.MESSAGE_SUCCESS, confirmedResult.getFeedbackToUser());
        assertEquals(new AddressBook(), model.getAddressBook());
        assertTrue(confirmedResult.shouldSaveAddressBook());
    }

    @Test
    public void confirmation_nonY_cancelsClear() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        CommandResult requestResult = new ClearCommand().execute(model);

        CommandResult cancelledResult = requestResult.getInputRequest().orElseThrow().respond("n", model);

        assertEquals(ClearCommand.MESSAGE_CLEAR_CANCELLED, cancelledResult.getFeedbackToUser());
        assertEquals(expectedModel, model);
        assertFalse(cancelledResult.shouldSaveAddressBook());
    }

}
