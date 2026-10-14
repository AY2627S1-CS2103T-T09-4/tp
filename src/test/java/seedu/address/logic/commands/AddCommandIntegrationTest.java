package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, validPerson.getName()),
                expectedModel);
    }

    @Test
    public void execute_duplicatePhoneWithDifferentName_success() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person samePhoneDifferentName = new PersonBuilder(personInList).withName("Alex Different").build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(samePhoneDifferentName);

        assertCommandSuccess(new AddCommand(samePhoneDifferentName), model,
                String.format(AddCommand.MESSAGE_SUCCESS, samePhoneDifferentName.getName()), expectedModel);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                String.format(AddCommand.MESSAGE_NOT_ADDED, AddCommand.MESSAGE_DUPLICATE_PERSON));
    }

    @Test
    public void execute_duplicateName_requestsConfirmationWithoutAdding() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameDifferentPhone = new PersonBuilder(personInList).withPhone("91234567").build();

        CommandResult result = new AddCommand(sameNameDifferentPhone).execute(model);

        assertEquals(String.format(AddCommand.MESSAGE_CONFIRM_DUPLICATE_NAME, sameNameDifferentPhone.getName()),
                result.getFeedbackToUser());
        assertTrue(result.getInputRequest().isPresent());
        assertFalse(result.shouldSaveAddressBook());
        assertFalse(model.getAddressBook().getPersonList().contains(sameNameDifferentPhone));
    }

    @Test
    public void confirmation_duplicateNameConfirmed_addsPerson() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameDifferentPhone = new PersonBuilder(personInList).withPhone("91234567").build();
        CommandResult requestResult = new AddCommand(sameNameDifferentPhone).execute(model);

        CommandResult confirmedResult = requestResult.getInputRequest().orElseThrow().respond(" y ", model);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, sameNameDifferentPhone.getName()),
                confirmedResult.getFeedbackToUser());
        assertTrue(confirmedResult.shouldSaveAddressBook());
        assertTrue(model.getAddressBook().getPersonList().contains(sameNameDifferentPhone));
    }

    @Test
    public void confirmation_duplicateNameNotConfirmed_cancelsAddition() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameDifferentPhone = new PersonBuilder(personInList).withPhone("91234567").build();
        CommandResult requestResult = new AddCommand(sameNameDifferentPhone).execute(model);

        CommandResult cancelledResult = requestResult.getInputRequest().orElseThrow().respond("n", model);

        assertEquals(AddCommand.MESSAGE_ADD_CANCELLED, cancelledResult.getFeedbackToUser());
        assertFalse(cancelledResult.shouldSaveAddressBook());
        assertFalse(model.getAddressBook().getPersonList().contains(sameNameDifferentPhone));
    }

    @Test
    public void confirmation_trueDuplicateAppeared_throwsCommandException() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameDifferentPhone = new PersonBuilder(personInList).withPhone("91234567").build();
        CommandResult requestResult = new AddCommand(sameNameDifferentPhone).execute(model);
        model.addPerson(sameNameDifferentPhone);

        String expectedMessage = String.format(AddCommand.MESSAGE_NOT_ADDED, AddCommand.MESSAGE_DUPLICATE_PERSON);
        assertThrows(CommandException.class, expectedMessage, () ->
                requestResult.getInputRequest().orElseThrow().respond("y", model));
    }

}
