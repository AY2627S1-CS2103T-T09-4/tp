package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Locale;

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

    private static final String AVAILABLE_VISIT_DATE = "2/1/2026 0900";
    /** A free time slot on 1/1/2026, the day on which all seven typical persons are already visited. */
    private static final String FULL_DAY_VISIT_DATE = "1/1/2026 1500";

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
        Person samePhoneDifferentName = new PersonBuilder(personInList).withName("Alex Different")
                .withVisitDate(AVAILABLE_VISIT_DATE).build();
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
    public void execute_duplicateVisitDate_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person personWithConflictingVisit = new PersonBuilder(personInList)
                .withName("Different Patient").withPhone("91234567").build();
        String conflictMessage = String.format(AddCommand.MESSAGE_DUPLICATE_VISIT_DATE,
                personWithConflictingVisit.getVisitDate());

        assertCommandFailure(new AddCommand(personWithConflictingVisit), model,
                String.format(AddCommand.MESSAGE_NOT_ADDED, conflictMessage));
    }

    @Test
    public void execute_duplicateName_requestsConfirmationWithoutAdding() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameDifferentPhone = new PersonBuilder(personInList).withPhone("91234567")
                .withVisitDate(AVAILABLE_VISIT_DATE).build();

        CommandResult result = new AddCommand(sameNameDifferentPhone).execute(model);

        assertEquals(String.format(AddCommand.MESSAGE_CONFIRM_DUPLICATE_NAME, sameNameDifferentPhone.getName()),
                result.getFeedbackToUser());
        assertTrue(result.getInputRequest().isPresent());
        assertFalse(result.shouldSaveAddressBook());
        assertFalse(model.getAddressBook().getPersonList().contains(sameNameDifferentPhone));
    }

    @Test
    public void execute_normalizedDuplicateName_requestsConfirmationWithoutAdding() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        String normalizedNameVariant = personInList.getName().fullName.toLowerCase(Locale.ROOT)
                .replace(" ", "   ") + " ";
        Person sameNameDifferentPhone = new PersonBuilder(personInList)
                .withName(normalizedNameVariant).withPhone("91234567")
                .withVisitDate(AVAILABLE_VISIT_DATE).build();

        CommandResult result = new AddCommand(sameNameDifferentPhone).execute(model);

        assertEquals(String.format(AddCommand.MESSAGE_CONFIRM_DUPLICATE_NAME, sameNameDifferentPhone.getName()),
                result.getFeedbackToUser());
        assertTrue(result.getInputRequest().isPresent());
        assertFalse(model.getAddressBook().getPersonList().contains(sameNameDifferentPhone));
    }

    @Test
    public void execute_normalizedNameAndSamePhone_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        String normalizedNameVariant = personInList.getName().fullName.toLowerCase(Locale.ROOT)
                .replace(" ", "   ") + " ";
        Person trueDuplicate = new PersonBuilder(personInList).withName(normalizedNameVariant).build();

        assertCommandFailure(new AddCommand(trueDuplicate), model,
                String.format(AddCommand.MESSAGE_NOT_ADDED, AddCommand.MESSAGE_DUPLICATE_PERSON));
    }

    @Test
    public void confirmation_duplicateNameConfirmed_addsPerson() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameDifferentPhone = new PersonBuilder(personInList).withPhone("91234567")
                .withVisitDate(AVAILABLE_VISIT_DATE).build();
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
        Person sameNameDifferentPhone = new PersonBuilder(personInList).withPhone("91234567")
                .withVisitDate(AVAILABLE_VISIT_DATE).build();
        CommandResult requestResult = new AddCommand(sameNameDifferentPhone).execute(model);

        CommandResult cancelledResult = requestResult.getInputRequest().orElseThrow().respond("n", model);

        assertEquals(AddCommand.MESSAGE_ADD_CANCELLED, cancelledResult.getFeedbackToUser());
        assertFalse(cancelledResult.shouldSaveAddressBook());
        assertFalse(model.getAddressBook().getPersonList().contains(sameNameDifferentPhone));
    }

    @Test
    public void execute_visitDayBelowLimit_addsWithoutConfirmation() {
        model.deletePerson(model.getAddressBook().getPersonList().get(0));
        Person seventhVisitOnDay = new PersonBuilder().withVisitDate(FULL_DAY_VISIT_DATE).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(seventhVisitOnDay);

        assertCommandSuccess(new AddCommand(seventhVisitOnDay), model,
                String.format(AddCommand.MESSAGE_SUCCESS, seventhVisitOnDay.getName()), expectedModel);
    }

    @Test
    public void execute_visitDayFull_requestsConfirmationWithoutAdding() throws Exception {
        Person eighthVisitOnDay = new PersonBuilder().withVisitDate(FULL_DAY_VISIT_DATE).build();

        CommandResult result = new AddCommand(eighthVisitOnDay).execute(model);

        assertEquals("Heads up: you already have 7 visits on Thu, 1 Jan 2026. Add Amy Bee anyway? "
                + "Type y to confirm. Any other input cancels.", result.getFeedbackToUser());
        assertTrue(result.getInputRequest().isPresent());
        assertFalse(result.shouldSaveAddressBook());
        assertFalse(model.getAddressBook().getPersonList().contains(eighthVisitOnDay));
    }

    @Test
    public void confirmation_visitDayFullConfirmed_addsPerson() throws Exception {
        Person eighthVisitOnDay = new PersonBuilder().withVisitDate(FULL_DAY_VISIT_DATE).build();
        CommandResult requestResult = new AddCommand(eighthVisitOnDay).execute(model);

        CommandResult confirmedResult = requestResult.getInputRequest().orElseThrow().respond("y", model);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, eighthVisitOnDay.getName()),
                confirmedResult.getFeedbackToUser());
        assertTrue(confirmedResult.shouldSaveAddressBook());
        assertTrue(model.getAddressBook().getPersonList().contains(eighthVisitOnDay));
    }

    @Test
    public void confirmation_visitDayFullNotConfirmed_cancelsAddition() throws Exception {
        Person eighthVisitOnDay = new PersonBuilder().withVisitDate(FULL_DAY_VISIT_DATE).build();
        CommandResult requestResult = new AddCommand(eighthVisitOnDay).execute(model);

        CommandResult cancelledResult = requestResult.getInputRequest().orElseThrow().respond("n", model);

        assertEquals(AddCommand.MESSAGE_ADD_CANCELLED, cancelledResult.getFeedbackToUser());
        assertFalse(cancelledResult.shouldSaveAddressBook());
        assertFalse(model.getAddressBook().getPersonList().contains(eighthVisitOnDay));
    }

    @Test
    public void confirmation_duplicateNameOnFullDay_requestsBothConfirmations() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameOnFullDay = new PersonBuilder(personInList).withPhone("91234567")
                .withVisitDate(FULL_DAY_VISIT_DATE).build();
        CommandResult nameRequestResult = new AddCommand(sameNameOnFullDay).execute(model);

        CommandResult dayRequestResult = nameRequestResult.getInputRequest().orElseThrow().respond("y", model);

        assertEquals(String.format(AddCommand.MESSAGE_CONFIRM_FULL_DAY, AddCommand.MAX_VISITS_PER_DAY,
                "Thu, 1 Jan 2026", sameNameOnFullDay.getName()), dayRequestResult.getFeedbackToUser());
        assertFalse(model.getAddressBook().getPersonList().contains(sameNameOnFullDay));

        CommandResult confirmedResult = dayRequestResult.getInputRequest().orElseThrow().respond("y", model);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, sameNameOnFullDay.getName()),
                confirmedResult.getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().contains(sameNameOnFullDay));
    }

    @Test
    public void confirmation_trueDuplicateAppeared_throwsCommandException() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameDifferentPhone = new PersonBuilder(personInList).withPhone("91234567")
                .withVisitDate(AVAILABLE_VISIT_DATE).build();
        CommandResult requestResult = new AddCommand(sameNameDifferentPhone).execute(model);
        model.addPerson(sameNameDifferentPhone);

        String expectedMessage = String.format(AddCommand.MESSAGE_NOT_ADDED, AddCommand.MESSAGE_DUPLICATE_PERSON);
        assertThrows(CommandException.class, expectedMessage, () ->
                requestResult.getInputRequest().orElseThrow().respond("y", model));
    }

    @Test
    public void confirmation_visitDateConflictAppeared_throwsCommandException() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameDifferentPhone = new PersonBuilder(personInList).withPhone("91234567")
                .withVisitDate(AVAILABLE_VISIT_DATE).build();
        CommandResult requestResult = new AddCommand(sameNameDifferentPhone).execute(model);
        Person conflictingVisit = new PersonBuilder().withName("Visit Conflict").withPhone("92345678")
                .withVisitDate(AVAILABLE_VISIT_DATE).build();
        model.addPerson(conflictingVisit);

        String conflictMessage = String.format(AddCommand.MESSAGE_DUPLICATE_VISIT_DATE,
                sameNameDifferentPhone.getVisitDate());
        String expectedMessage = String.format(AddCommand.MESSAGE_NOT_ADDED, conflictMessage);
        assertThrows(CommandException.class, expectedMessage, () ->
                requestResult.getInputRequest().orElseThrow().respond("y", model));
    }

}
