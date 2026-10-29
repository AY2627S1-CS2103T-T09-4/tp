package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.LogicManagerIntegrationFixture;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests rollback parsing, edits, subsequent advancement, and persistence through real components.
 */
public class PrevCommandIntegrationTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-21T04:00:30Z"),
            ZoneId.of("Asia/Singapore"));
    private static final Person AMY = new PersonBuilder().withVisitDate("30/10/2026 1000")
            .withPreviousVisitDates("10/10/2026 1000", "20/10/2026 1000")
            .withDateOfBirth("15/6/1990").withBloodType("A+")
            .withAllergies("Penicillin; latex").withAlcoholHistory("Does not drink").withSmokingStatus("former")
            .withEmergencyContact("Mary Tan, 91234567")
            .withNote("Review medication").withTags("priority").build();

    @TempDir
    private Path testFolder;

    private Model model;
    private LogicManager logic;
    private LogicManagerIntegrationFixture fixture;

    @BeforeEach
    public void setUp() throws IOException {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(AMY);
        model = new ModelManager(addressBook, new UserPrefs());
        fixture = new LogicManagerIntegrationFixture(model, testFolder, FIXED_CLOCK);
        logic = fixture.getLogic();
    }

    @Test
    public void execute_prev_persistsRestoredDateAndSurvivesReload() throws Exception {
        String savedDataBeforePrompt = Files.readString(fixture.getAddressBookStorage().getAddressBookFilePath());
        CommandResult requestResult = logic.execute("prev\t1");
        assertEquals(String.format(PrevCommand.MESSAGE_CONFIRM_PREV, AMY.getName(), AMY.getPhone(),
                "20/10/2026 1000", AMY.getVisitDate()), requestResult.getFeedbackToUser());
        assertFalse(requestResult.shouldSaveAddressBook());
        assertTrue(logic.isInputRequestPending());
        assertEquals(AMY, model.getAddressBook().getPersonList().getFirst());
        assertEquals(savedDataBeforePrompt,
                Files.readString(fixture.getAddressBookStorage().getAddressBookFilePath()));
        assertEquals(0, fixture.getSaveCount());

        CommandResult result = logic.execute("Y");
        Person expectedPerson = new PersonBuilder(AMY).withVisitDate("20/10/2026 1000")
                .withPreviousVisitDates("10/10/2026 1000").build();

        assertEquals("Visit date restored for Amy Bee: 20/10/2026 1000\n"
                + "Discarded visit date and time: 30/10/2026 1000", result.getFeedbackToUser());
        fixture.assertPersistedPatients(expectedPerson);
        assertEquals(1, fixture.getSaveCount());
        assertTrue(result.shouldSaveAddressBook());
        assertEquals(expectedPerson, result.getPersonToView().orElseThrow());
        assertFalse(logic.isInputRequestPending());
        Model reloadedModel = new ModelManager(fixture.getAddressBookStorage().readAddressBook().orElseThrow(),
                new UserPrefs());
        assertEquals(model.getAddressBook(), reloadedModel.getAddressBook());
    }

    @Test
    public void execute_repeatedRollbackUntilEmpty_rejectsFurtherRollbackWithoutSaving() throws Exception {
        executeConfirmedPrev("prev 1");
        CommandResult result = executeConfirmedPrev("prev 1");

        Person expectedPerson = new PersonBuilder(AMY).withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates().build();
        assertEquals("Visit date restored for Amy Bee: 10/10/2026 1000\n"
                + "Discarded visit date and time: 20/10/2026 1000", result.getFeedbackToUser());
        fixture.assertPersistedPatients(expectedPerson);
        assertEquals(2, fixture.getSaveCount());
        fixture.assertRejectedCommand("prev 1", CommandException.class,
                String.format(PrevCommand.MESSAGE_NO_PREVIOUS_VISIT_DATES, AMY.getName()));
    }

    @Test
    public void execute_afterDateAndOtherFieldEdits_restoresOnlyVisitDateState() throws Exception {
        logic.execute("edit 1 n/Amy Updated vd/2/11/2026 1000 p/92345678 e/updated@example.com"
                + " a/20 New Road note/Updated note t/followup dob/20/7/1985 bt/O- ec/David Tan, 87654321"
                + " all/No known allergies alc/Occasional drinking smk/current");
        CommandResult result = executeConfirmedPrev("prev 1");

        Person expectedPerson = new PersonBuilder(AMY).withVisitDate("20/10/2026 1000")
                .withPreviousVisitDates("10/10/2026 1000").withName("Amy Updated").withPhone("92345678")
                .withEmail("updated@example.com").withAddress("20 New Road")
                .withDateOfBirth("20/7/1985").withBloodType("O-").withEmergencyContact("David Tan, 87654321")
                .withAllergies("No known allergies").withAlcoholHistory("Occasional drinking")
                .withSmokingStatus("current")
                .withNote("Updated note").withTags("followup").build();
        assertEquals("Visit date restored for Amy Updated: 20/10/2026 1000\n"
                + "Discarded visit date and time: 2/11/2026 1000", result.getFeedbackToUser());
        fixture.assertPersistedPatients(expectedPerson);
        assertEquals(2, fixture.getSaveCount());
    }

    @Test
    public void execute_afterClearingOptionalFields_preservesEmptyFieldsAndPersistsRollback() throws Exception {
        logic.execute("edit 1 e/ note/ t/ dob/ bt/ ec/ all/ alc/ smk/");
        Person clearedPerson = new PersonBuilder(AMY).withEmail("").withNote("").withTags()
                .withDateOfBirth("").withBloodType("").withEmergencyContact("")
                .withAllergies("").withAlcoholHistory("").withSmokingStatus("").build();
        fixture.assertPersistedPatients(clearedPerson);

        executeConfirmedPrev("prev 1");

        Person expectedPerson = new PersonBuilder(clearedPerson).withVisitDate("20/10/2026 1000")
                .withPreviousVisitDates("10/10/2026 1000").build();
        fixture.assertPersistedPatients(expectedPerson);
        assertEquals(2, fixture.getSaveCount());
        Model reloadedModel = new ModelManager(fixture.getAddressBookStorage().readAddressBook().orElseThrow(),
                new UserPrefs());
        assertEquals(List.of(expectedPerson), reloadedModel.getAddressBook().getPersonList());
    }

    @Test
    public void execute_rollbackThenNext_usesRestoredStateAndFreesDiscardedDate() throws Exception {
        executeConfirmedPrev("prev 1");
        logic.execute("next 1 1/11/2026 1000");

        Person expectedPerson = new PersonBuilder(AMY).withVisitDate("1/11/2026 1000").build();
        fixture.assertPersistedPatients(expectedPerson);

        logic.execute("add n/Benson Meier p/91234567 a/10 Test Road vd/30/10/2026 1000");
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertEquals(model.getAddressBook(), fixture.getAddressBookStorage().readAddressBook().orElseThrow());
        assertEquals(3, fixture.getSaveCount());
    }

    @Test
    public void execute_invalidCommands_preservesDataDiskFilterAndSort() throws Exception {
        logic.execute("find n/Amy");
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        logic.execute("view 1");
        fixture.assertRejectedCommand("prev", ParseException.class,
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, PrevCommand.MESSAGE_USAGE));
        for (String commandText : List.of("prev 0", "prev -1", "prev 1 extra")) {
            fixture.assertRejectedCommand(commandText, ParseException.class,
                    ParserUtil.MESSAGE_INVALID_INDEX + "\n" + PrevCommand.MESSAGE_USAGE);
        }
        fixture.assertRejectedCommand("prev 2147483648", ParseException.class,
                ParserUtil.MESSAGE_INDEX_TOO_LARGE + "\n" + PrevCommand.MESSAGE_USAGE);
        fixture.assertRejectedCommand("prev 2", CommandException.class,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(AMY, logic.execute("view 1").getPersonToView().orElseThrow());
    }

    @Test
    public void execute_saveFailures_reportsExistingErrorAndRollsBackModel() throws Exception {
        for (IOException failure : List.of(new IOException("test save failure"),
                new AccessDeniedException("addressBook.json"))) {
            assertSaveFailure(failure);
        }
    }

    private void assertSaveFailure(IOException failure) throws Exception {
        model.setPerson(model.getAddressBook().getPersonList().getFirst(), AMY);
        String savedData = Files.readString(fixture.getAddressBookStorage().getAddressBookFilePath());
        AddressBook addressBookBefore = new AddressBook(model.getAddressBook());
        List<Person> displayedPersonsBefore = List.copyOf(model.getFilteredPersonList());
        fixture.useFailingStorage(failure);
        logic = fixture.getLogic();
        String errorFormat = failure instanceof AccessDeniedException
                ? LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT : LogicManager.FILE_OPS_ERROR_FORMAT;

        CommandResult requestResult = logic.execute("prev 1");
        assertFalse(requestResult.shouldSaveAddressBook());
        assertEquals(0, fixture.getSaveCount());
        String expectedErrorMessage = String.format(errorFormat, failure.getMessage());
        assertThrows(CommandException.class, expectedErrorMessage, () -> logic.execute("y"));

        assertEquals(savedData, Files.readString(fixture.getAddressBookStorage().getAddressBookFilePath()));
        assertEquals(addressBookBefore, model.getAddressBook());
        assertEquals(displayedPersonsBefore, model.getFilteredPersonList());
        assertFalse(logic.isInputRequestPending());
    }

    @Test
    public void execute_cancelledResponse_preservesStateAndConsumesCommandText() throws Exception {
        String savedData = Files.readString(fixture.getAddressBookStorage().getAddressBookFilePath());
        CommandResult requestResult = logic.execute("prev 1");

        CommandResult cancelledResult = logic.execute("list");

        assertEquals(PrevCommand.MESSAGE_PREV_CANCELLED, cancelledResult.getFeedbackToUser());
        assertFalse(cancelledResult.shouldSaveAddressBook());
        assertFalse(logic.isInputRequestPending());
        assertEquals(AMY, model.getAddressBook().getPersonList().getFirst());
        assertEquals(savedData, Files.readString(fixture.getAddressBookStorage().getAddressBookFilePath()));
        assertEquals(0, fixture.getSaveCount());
        assertTrue(requestResult.getInputRequest().isPresent());

        assertEquals(ListCommand.MESSAGE_SUCCESS, logic.execute("list").getFeedbackToUser());
    }

    @Test
    public void execute_confirmationAfterPatientEdit_rejectsStaleRequest() throws Exception {
        logic.execute("prev 1");
        Person replacementPerson = new PersonBuilder(AMY).withName("Amy Updated").build();
        model.setPerson(AMY, replacementPerson);

        assertThrows(CommandException.class, PrevCommand.MESSAGE_PERSON_NO_LONGER_EXISTS, () -> logic.execute("y"));

        assertEquals(replacementPerson, model.getAddressBook().getPersonList().getFirst());
        assertFalse(logic.isInputRequestPending());
        assertEquals(0, fixture.getSaveCount());
    }

    @Test
    public void execute_sharedNameDifferentPhones_targetsDisplayedPatient() throws Exception {
        Person otherPatient = new PersonBuilder(AMY).withPhone("91234567").withVisitDate("5/11/2026 1000")
                .withPreviousVisitDates("4/11/2026 1000").build();
        model.addPerson(otherPatient);
        model.updateFilteredPersonList(person -> person.getPhone().equals(otherPatient.getPhone()));

        CommandResult requestResult = logic.execute("prev 1");

        assertEquals(String.format(PrevCommand.MESSAGE_CONFIRM_PREV, otherPatient.getName(), otherPatient.getPhone(),
                "4/11/2026 1000", otherPatient.getVisitDate()), requestResult.getFeedbackToUser());
        CommandResult result = logic.execute("y");
        Person expectedPatient = new PersonBuilder(otherPatient).withVisitDate("4/11/2026 1000")
                .withPreviousVisitDates().build();

        assertEquals(String.format(PrevCommand.MESSAGE_SUCCESS, expectedPatient.getName(),
                expectedPatient.getVisitDate(), otherPatient.getVisitDate()), result.getFeedbackToUser());
        fixture.assertPersistedPatients(AMY, expectedPatient);
    }

    @Test
    public void execute_filteredSortedIndex_capturesPatientBeforeConfirmation() throws Exception {
        Person earlierPatient = new PersonBuilder().withName("Benson Meier").withPhone("91234567")
                .withVisitDate("1/10/2026 1000").withPreviousVisitDates("1/9/2026 1000").build();
        model.addPerson(earlierPatient);
        model.updateFilteredPersonList(person -> true);
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));

        CommandResult requestResult = logic.execute("prev 2");
        model.updateFilteredPersonList(person -> true);
        CommandResult result = logic.execute("y");
        Person expectedAmy = new PersonBuilder(AMY).withVisitDate("20/10/2026 1000")
                .withPreviousVisitDates("10/10/2026 1000").build();

        assertEquals(String.format(PrevCommand.MESSAGE_SUCCESS, expectedAmy.getName(), expectedAmy.getVisitDate(),
                AMY.getVisitDate()), result.getFeedbackToUser());
        assertEquals(List.of(expectedAmy, earlierPatient), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
        assertTrue(result.shouldSaveAddressBook());
        assertTrue(requestResult.getInputRequest().isPresent());
    }

    @Test
    public void execute_confirmationThenNormalCommand_parsesNewCommand() throws Exception {
        executeConfirmedPrev("prev 1");

        assertEquals(ListCommand.MESSAGE_SUCCESS, logic.execute("list").getFeedbackToUser());
    }

    private CommandResult executeConfirmedPrev(String commandText) throws Exception {
        CommandResult requestResult = logic.execute(commandText);
        assertTrue(requestResult.getInputRequest().isPresent());
        assertFalse(requestResult.shouldSaveAddressBook());
        return logic.execute("  y  ");
    }

}
