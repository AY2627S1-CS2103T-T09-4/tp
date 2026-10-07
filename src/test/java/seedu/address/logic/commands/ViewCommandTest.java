package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

/**
 * Contains integration tests with the model and unit tests for {@code ViewCommand}.
 */
public class ViewCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_displaysSelectedPerson() {
        Person personToView = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        ViewCommand viewCommand = new ViewCommand(INDEX_SECOND_PERSON);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(personToView::equals);
        CommandResult expectedResult = CommandResult.withoutSaving(
                String.format(ViewCommand.MESSAGE_VIEW_PERSON_SUCCESS, personToView.getName()));

        assertCommandSuccess(viewCommand, model, expectedResult, expectedModel);
        assertEquals(List.of(personToView), model.getFilteredPersonList());
        assertFalse(expectedResult.shouldSaveAddressBook());
    }

    @Test
    public void execute_validIndexFilteredList_displaysSelectedPerson() {
        Person personToView = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        ViewCommand viewCommand = new ViewCommand(INDEX_FIRST_PERSON);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(personToView::equals);
        CommandResult expectedResult = CommandResult.withoutSaving(
                String.format(ViewCommand.MESSAGE_VIEW_PERSON_SUCCESS, personToView.getName()));

        assertCommandSuccess(viewCommand, model, expectedResult, expectedModel);
        assertEquals(List.of(personToView), model.getFilteredPersonList());
    }

    @Test
    public void execute_validIndexSortedList_displaysSelectedPerson() {
        model.sortFilteredPersonList(Comparator.comparing((Person person) -> person.getName().fullName).reversed());
        Person firstDisplayedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        ViewCommand viewCommand = new ViewCommand(INDEX_FIRST_PERSON);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(firstDisplayedPerson::equals);
        CommandResult expectedResult = CommandResult.withoutSaving(
                String.format(ViewCommand.MESSAGE_VIEW_PERSON_SUCCESS, firstDisplayedPerson.getName()));

        assertCommandSuccess(viewCommand, model, expectedResult, expectedModel);
        assertEquals(List.of(firstDisplayedPerson), model.getFilteredPersonList());
    }

    @Test
    public void execute_viewThenList_restoresFullList() throws Exception {
        List<Person> originalList = List.copyOf(model.getFilteredPersonList());

        new ViewCommand(INDEX_SECOND_PERSON).execute(model);
        new ListCommand().execute(model);

        assertEquals(originalList, model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        ViewCommand viewCommand = new ViewCommand(outOfBoundIndex);

        assertCommandFailure(viewCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertTrue(INDEX_SECOND_PERSON.getZeroBased() < model.getAddressBook().getPersonList().size());
        ViewCommand viewCommand = new ViewCommand(INDEX_SECOND_PERSON);

        assertCommandFailure(viewCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        ViewCommand viewFirstCommand = new ViewCommand(INDEX_FIRST_PERSON);
        ViewCommand viewSecondCommand = new ViewCommand(INDEX_SECOND_PERSON);

        assertTrue(viewFirstCommand.equals(viewFirstCommand));
        assertTrue(viewFirstCommand.equals(new ViewCommand(INDEX_FIRST_PERSON)));
        assertFalse(viewFirstCommand.equals(viewSecondCommand));
        assertFalse(viewFirstCommand.equals(1));
        assertFalse(viewFirstCommand.equals(null));
    }

    @Test
    public void toStringMethod() {
        ViewCommand viewCommand = new ViewCommand(INDEX_FIRST_PERSON);
        String expected = ViewCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON + "}";

        assertEquals(expected, viewCommand.toString());
    }
}
