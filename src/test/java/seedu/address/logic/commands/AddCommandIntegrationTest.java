package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;
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
                CommandResult.showUpdatedPerson(
                        AddCommand.formatSuccessMessage(validPerson), validPerson),
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
                CommandResult.showUpdatedPerson(
                        AddCommand.formatSuccessMessage(samePhoneDifferentName),
                        samePhoneDifferentName), expectedModel);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        List<Person> displayedPersons = List.copyOf(model.getFilteredPersonList());
        String expectedMessage = String.format(AddCommand.MESSAGE_NOT_ADDED, AddCommand.MESSAGE_DUPLICATE_PERSON);

        CommandException exception = assertThrows(
                CommandException.class, expectedMessage, () -> new AddCommand(personInList).execute(model));

        assertEquals(personInList, exception.getPersonToView().orElseThrow());
        assertEquals(displayedPersons, model.getFilteredPersonList());
    }

    @Test
    public void execute_samePhoneDigitsFormattedDifferently_rejectedAsDuplicate() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        String expectedMessage = String.format(AddCommand.MESSAGE_NOT_ADDED, AddCommand.MESSAGE_DUPLICATE_PERSON);

        // the same patient re-entered with spaces or a dash in the phone number, at an unused time
        for (String formattedPhone : List.of("9435 1253", "9435-1253")) {
            Person reformattedDuplicate = new PersonBuilder(personInList).withPhone(formattedPhone)
                    .withVisitDate(AVAILABLE_VISIT_DATE).build();
            CommandException exception = assertThrows(CommandException.class, expectedMessage, () ->
                    new AddCommand(reformattedDuplicate).execute(model));
            assertEquals(personInList, exception.getPersonToView().orElseThrow());
        }
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
    }

    @Test
    public void execute_duplicateVisitDate_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person personWithConflictingVisit = new PersonBuilder(personInList)
                .withName("Different Patient").withPhone("91234567").build();
        List<Person> displayedPersons = List.copyOf(model.getFilteredPersonList());
        String conflictMessage = String.format(AddCommand.MESSAGE_DUPLICATE_VISIT_DATE,
                personWithConflictingVisit.getVisitDate());

        String expectedMessage = String.format(AddCommand.MESSAGE_NOT_ADDED, conflictMessage);
        CommandException exception = assertThrows(CommandException.class, expectedMessage, () ->
                new AddCommand(personWithConflictingVisit).execute(model));
        assertEquals(personInList, exception.getPersonToView().orElseThrow());
        assertEquals(displayedPersons, model.getFilteredPersonList());
    }

    @Test
    public void execute_duplicateName_requestsConfirmationWithoutAdding() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameDifferentPhone = new PersonBuilder(personInList).withPhone("91234567")
                .withVisitDate(AVAILABLE_VISIT_DATE).build();

        CommandResult result = new AddCommand(sameNameDifferentPhone).execute(model);

        assertEquals(expectedDuplicateNamePrompt(sameNameDifferentPhone), result.getFeedbackToUser());
        assertTrue(result.getInputRequest().isPresent());
        assertEquals(personInList, result.getPersonToView().orElseThrow());
        assertFalse(result.shouldSaveAddressBook());
        assertFalse(model.getAddressBook().getPersonList().contains(sameNameDifferentPhone));
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
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

        assertEquals(expectedDuplicateNamePrompt(sameNameDifferentPhone), result.getFeedbackToUser());
        assertTrue(result.getInputRequest().isPresent());
        assertEquals(personInList, result.getPersonToView().orElseThrow());
        assertFalse(model.getAddressBook().getPersonList().contains(sameNameDifferentPhone));
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_duplicateNameHiddenByFilter_previewsMatchWithoutChangingList() throws Exception {
        Person hiddenMatch = model.getAddressBook().getPersonList().get(0);
        Person sameNameDifferentPhone = new PersonBuilder(hiddenMatch).withPhone("91234567")
                .withVisitDate(AVAILABLE_VISIT_DATE).build();
        model.updateFilteredPersonList(person -> !person.equals(hiddenMatch));
        List<Person> displayedPersons = List.copyOf(model.getFilteredPersonList());

        CommandResult result = new AddCommand(sameNameDifferentPhone).execute(model);

        assertEquals(hiddenMatch, result.getPersonToView().orElseThrow());
        assertEquals(displayedPersons, model.getFilteredPersonList());
    }

    @Test
    public void execute_normalizedNameAndSamePhone_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        String normalizedNameVariant = personInList.getName().fullName.toLowerCase(Locale.ROOT)
                .replace(" ", "   ") + " ";
        Person trueDuplicate = new PersonBuilder(personInList).withName(normalizedNameVariant).build();

        String expectedMessage = String.format(AddCommand.MESSAGE_NOT_ADDED, AddCommand.MESSAGE_DUPLICATE_PERSON);
        CommandException exception = assertThrows(
                CommandException.class, expectedMessage, () -> new AddCommand(trueDuplicate).execute(model));

        assertEquals(personInList, exception.getPersonToView().orElseThrow());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_multipleDuplicateNames_displaysFirstMatchWithoutChangingList() throws Exception {
        Person firstMatch = model.getAddressBook().getPersonList().get(0);
        Person secondMatch = new PersonBuilder(firstMatch).withPhone("91234567")
                .withVisitDate(AVAILABLE_VISIT_DATE).build();
        Person personToAdd = new PersonBuilder(firstMatch).withPhone("92345678")
                .withVisitDate("3/1/2026 0900").build();
        model.addPerson(secondMatch);

        CommandResult result = new AddCommand(personToAdd).execute(model);

        assertEquals(String.format(AddCommand.MESSAGE_WARNING_DUPLICATE_NAMES, personToAdd.getName(), 2) + " "
                + String.format(AddCommand.MESSAGE_CONFIRM_ADD, personToAdd.getName()), result.getFeedbackToUser());
        assertTrue(result.getFeedbackToUser().contains("Only one matching patient is shown"));
        assertEquals(firstMatch, result.getPersonToView().orElseThrow());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
        assertFalse(model.getAddressBook().getPersonList().contains(personToAdd));
    }

    @Test
    public void confirmation_duplicateNameConfirmed_addsPerson() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameDifferentPhone = new PersonBuilder(personInList).withPhone("91234567")
                .withVisitDate(AVAILABLE_VISIT_DATE).build();
        CommandResult requestResult = new AddCommand(sameNameDifferentPhone).execute(model);

        CommandResult confirmedResult = requestResult.getInputRequest().orElseThrow().respond(" y ", model);

        assertEquals(AddCommand.formatSuccessMessage(sameNameDifferentPhone),
                confirmedResult.getFeedbackToUser());
        assertEquals(sameNameDifferentPhone, confirmedResult.getPersonToView().orElseThrow());
        assertTrue(confirmedResult.shouldSaveAddressBook());
        assertTrue(model.getAddressBook().getPersonList().contains(sameNameDifferentPhone));
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
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
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_visitDayBelowLimit_addsWithoutConfirmation() {
        model.deletePerson(model.getAddressBook().getPersonList().get(0));
        Person seventhVisitOnDay = new PersonBuilder().withVisitDate(FULL_DAY_VISIT_DATE).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(seventhVisitOnDay);

        assertCommandSuccess(new AddCommand(seventhVisitOnDay), model,
                CommandResult.showUpdatedPerson(
                        AddCommand.formatSuccessMessage(seventhVisitOnDay), seventhVisitOnDay),
                expectedModel);
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

        assertEquals(AddCommand.formatSuccessMessage(eighthVisitOnDay),
                confirmedResult.getFeedbackToUser());
        assertEquals(eighthVisitOnDay, confirmedResult.getPersonToView().orElseThrow());
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
    public void execute_duplicateNameOnFullDay_requestsOneCombinedConfirmation() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameOnFullDay = new PersonBuilder(personInList).withPhone("91234567")
                .withVisitDate(FULL_DAY_VISIT_DATE).build();

        CommandResult result = new AddCommand(sameNameOnFullDay).execute(model);

        String expectedPrompt = String.format(AddCommand.MESSAGE_WARNING_DUPLICATE_NAME, sameNameOnFullDay.getName())
                + " " + String.format(AddCommand.MESSAGE_WARNING_FULL_DAY, Model.MAX_VISITS_PER_DAY, "Thu, 1 Jan 2026")
                + " " + String.format(AddCommand.MESSAGE_CONFIRM_ADD, sameNameOnFullDay.getName());
        assertEquals(expectedPrompt, result.getFeedbackToUser());
        assertEquals(personInList, result.getPersonToView().orElseThrow());
        assertFalse(result.shouldSaveAddressBook());
        assertFalse(model.getAddressBook().getPersonList().contains(sameNameOnFullDay));
    }

    @Test
    public void confirmation_multipleDuplicateNamesOnFullDay_requestsOneCombinedConfirmation() throws Exception {
        Person firstMatch = model.getAddressBook().getPersonList().get(0);
        // the second match is on another day, so the full day still has the seven typical visits
        Person secondMatch = new PersonBuilder(firstMatch).withPhone("91234567")
                .withVisitDate(AVAILABLE_VISIT_DATE).build();
        model.addPerson(secondMatch);
        Person thirdSameName = new PersonBuilder(firstMatch).withPhone("92345678")
                .withVisitDate(FULL_DAY_VISIT_DATE).build();

        CommandResult requestResult = new AddCommand(thirdSameName).execute(model);

        String expectedPrompt = String.format(AddCommand.MESSAGE_WARNING_DUPLICATE_NAMES, thirdSameName.getName(), 2)
                + " " + String.format(AddCommand.MESSAGE_WARNING_FULL_DAY, Model.MAX_VISITS_PER_DAY, "Thu, 1 Jan 2026")
                + " " + String.format(AddCommand.MESSAGE_CONFIRM_ADD, thirdSameName.getName());
        assertEquals(expectedPrompt, requestResult.getFeedbackToUser());
        assertEquals(firstMatch, requestResult.getPersonToView().orElseThrow());
        assertFalse(model.getAddressBook().getPersonList().contains(thirdSameName));

        CommandResult confirmedResult = requestResult.getInputRequest().orElseThrow().respond("y", model);

        assertEquals(AddCommand.formatSuccessMessage(thirdSameName),
                confirmedResult.getFeedbackToUser());
        assertEquals(thirdSameName, confirmedResult.getPersonToView().orElseThrow());
        assertTrue(confirmedResult.getInputRequest().isEmpty());
        assertTrue(model.getAddressBook().getPersonList().contains(thirdSameName));
    }

    @Test
    public void confirmation_duplicateNameOnFullDayConfirmedOnce_addsPerson() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameOnFullDay = new PersonBuilder(personInList).withPhone("91234567")
                .withVisitDate(FULL_DAY_VISIT_DATE).build();
        CommandResult requestResult = new AddCommand(sameNameOnFullDay).execute(model);

        CommandResult confirmedResult = requestResult.getInputRequest().orElseThrow().respond("y", model);

        assertEquals(AddCommand.formatSuccessMessage(sameNameOnFullDay),
                confirmedResult.getFeedbackToUser());
        assertEquals(sameNameOnFullDay, confirmedResult.getPersonToView().orElseThrow());
        assertTrue(confirmedResult.getInputRequest().isEmpty());
        assertTrue(confirmedResult.shouldSaveAddressBook());
        assertTrue(model.getAddressBook().getPersonList().contains(sameNameOnFullDay));
    }

    @Test
    public void confirmation_duplicateNameOnFullDayNotConfirmed_cancelsAddition() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameOnFullDay = new PersonBuilder(personInList).withPhone("91234567")
                .withVisitDate(FULL_DAY_VISIT_DATE).build();
        CommandResult requestResult = new AddCommand(sameNameOnFullDay).execute(model);

        CommandResult cancelledResult = requestResult.getInputRequest().orElseThrow().respond("n", model);

        assertEquals(AddCommand.MESSAGE_ADD_CANCELLED, cancelledResult.getFeedbackToUser());
        assertFalse(cancelledResult.shouldSaveAddressBook());
        assertFalse(model.getAddressBook().getPersonList().contains(sameNameOnFullDay));
    }

    @Test
    public void confirmation_trueDuplicateAppeared_throwsCommandException() throws Exception {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        Person sameNameDifferentPhone = new PersonBuilder(personInList).withPhone("91234567")
                .withVisitDate(AVAILABLE_VISIT_DATE).build();
        CommandResult requestResult = new AddCommand(sameNameDifferentPhone).execute(model);
        model.addPerson(sameNameDifferentPhone);

        String expectedMessage = String.format(AddCommand.MESSAGE_NOT_ADDED, AddCommand.MESSAGE_DUPLICATE_PERSON);
        CommandException exception = assertThrows(CommandException.class, expectedMessage, () ->
                requestResult.getInputRequest().orElseThrow().respond("y", model));
        assertEquals(sameNameDifferentPhone, exception.getPersonToView().orElseThrow());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
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
        CommandException exception = assertThrows(CommandException.class, expectedMessage, () ->
                requestResult.getInputRequest().orElseThrow().respond("y", model));
        assertEquals(conflictingVisit, exception.getPersonToView().orElseThrow());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    private static String expectedDuplicateNamePrompt(Person personToAdd) {
        return String.format(AddCommand.MESSAGE_WARNING_DUPLICATE_NAME, personToAdd.getName()) + " "
                + String.format(AddCommand.MESSAGE_CONFIRM_ADD, personToAdd.getName());
    }
}
