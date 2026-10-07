package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_requestsConfirmation() throws Exception {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        CommandResult result = deleteCommand.execute(model);

        assertEquals(String.format(DeleteCommand.MESSAGE_CONFIRM_DELETE, personToDelete.getName()),
                result.getFeedbackToUser());
        assertTrue(result.getInputRequest().isPresent());
        assertFalse(result.shouldSaveAddressBook());
        assertEquals(expectedModel, model);
    }

    @Test
    public void confirmation_lowercaseY_deletesCapturedPerson() throws Exception {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        CommandResult requestResult = deleteCommand.execute(model);

        CommandResult confirmedResult = requestResult.getInputRequest().orElseThrow().respond("y", model);

        assertEquals(String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)),
                confirmedResult.getFeedbackToUser());
        assertFalse(model.getAddressBook().getPersonList().contains(personToDelete));
        assertTrue(confirmedResult.shouldSaveAddressBook());
    }

    @Test
    public void confirmation_nonY_cancelsDeletion() throws Exception {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        CommandResult requestResult = new DeleteCommand(INDEX_FIRST_PERSON).execute(model);

        CommandResult cancelledResult = requestResult.getInputRequest().orElseThrow().respond("list", model);

        assertEquals(DeleteCommand.MESSAGE_DELETE_CANCELLED, cancelledResult.getFeedbackToUser());
        assertFalse(cancelledResult.shouldSaveAddressBook());
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_confirmsDisplayedPerson() throws Exception {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        CommandResult requestResult = deleteCommand.execute(model);

        requestResult.getInputRequest().orElseThrow().respond("Y", model);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        showNoPerson(expectedModel);
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void confirmation_capturedPersonChanged_throwsCommandExceptionWithoutDeletingReplacement() throws Exception {
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        CommandResult requestResult = new DeleteCommand(INDEX_FIRST_PERSON).execute(model);
        Person editedPerson = new PersonBuilder(originalPerson)
                .withPhone("91234567")
                .build();
        model.setPerson(originalPerson, editedPerson);
        int expectedPersonCount = model.getAddressBook().getPersonList().size();

        CommandException exception = assertThrows(
                CommandException.class, () -> requestResult.getInputRequest().orElseThrow().respond("y", model));

        assertEquals(DeleteCommand.MESSAGE_PERSON_NO_LONGER_EXISTS, exception.getMessage());
        assertEquals(expectedPersonCount, model.getAddressBook().getPersonList().size());
        assertTrue(model.getAddressBook().getPersonList().contains(editedPerson));
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_PERSON);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteCommand deleteCommand = new DeleteCommand(targetIndex);
        String expected = DeleteCommand.class.getCanonicalName() + "{targetIndex=" + targetIndex + "}";
        assertEquals(expected, deleteCommand.toString());
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredPersonList(p -> false);

        assertTrue(model.getFilteredPersonList().isEmpty());
    }
}
