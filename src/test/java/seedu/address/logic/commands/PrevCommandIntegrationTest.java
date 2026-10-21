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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests rollback parsing, edits, subsequent advancement, and persistence through real components.
 */
public class PrevCommandIntegrationTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-21T04:00:30Z"),
            ZoneId.of("Asia/Singapore"));
    private static final Person AMY = new PersonBuilder().withVisitDate("30/10/2026 1000")
            .withPreviousVisitDates("10/10/2026 1000", "20/10/2026 1000")
            .withDateOfBirth("15/6/1990").withBloodType("A+").withEmergencyContact("Mary Tan, 91234567")
            .withNote("Review medication").withTags("priority").build();

    @TempDir
    private Path testFolder;

    private Model model;
    private LogicManager logic;
    private JsonAddressBookStorage addressBookStorage;
    private int saveCount;

    @BeforeEach
    public void setUp() throws IOException {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(AMY);
        model = new ModelManager(addressBook, new UserPrefs());
        addressBookStorage = new JsonAddressBookStorage(testFolder.resolve("addressBook.json")) {
            /**
             * Saves the address book and counts this write.
             */
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                saveCount++;
                super.saveAddressBook(addressBook);
            }
        };
        addressBookStorage.saveAddressBook(addressBook);
        saveCount = 0;
        logic = createLogic(addressBookStorage);
    }

    @Test
    public void execute_prev_persistsRestoredDateAndSurvivesReload() throws Exception {
        CommandResult result = logic.execute("prev\t1");
        Person expectedPerson = new PersonBuilder(AMY).withVisitDate("20/10/2026 1000")
                .withPreviousVisitDates("10/10/2026 1000").build();

        assertEquals("Visit date restored for Amy Bee: 20/10/2026 1000\n"
                + "Discarded visit date and time: 30/10/2026 1000", result.getFeedbackToUser());
        assertPersistedPatient(expectedPerson);
        assertEquals(1, saveCount);
        assertTrue(result.shouldSaveAddressBook());
        assertTrue(result.getPersonToView().isEmpty());
        assertFalse(logic.isInputRequestPending());
        Model reloadedModel = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloadedModel.getAddressBook());
    }

    @Test
    public void execute_repeatedRollbackUntilEmpty_rejectsFurtherRollbackWithoutSaving() throws Exception {
        logic.execute("prev 1");
        CommandResult result = logic.execute("prev 1");

        Person expectedPerson = new PersonBuilder(AMY).withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates().build();
        assertEquals("Visit date restored for Amy Bee: 10/10/2026 1000\n"
                + "Discarded visit date and time: 20/10/2026 1000", result.getFeedbackToUser());
        assertPersistedPatient(expectedPerson);
        assertEquals(2, saveCount);
        assertRejectedCommand("prev 1", CommandException.class,
                String.format(PrevCommand.MESSAGE_NO_PREVIOUS_VISIT_DATES, AMY.getName()));
    }

    @Test
    public void execute_afterDateAndOtherFieldEdits_restoresOnlyVisitDateState() throws Exception {
        logic.execute("edit 1 n/Amy Updated vd/2/11/2026 1000 p/92345678 e/updated@example.com"
                + " a/20 New Road note/Updated note t/followup dob/20/7/1985 bt/O- ec/David Tan, 87654321");
        CommandResult result = logic.execute("prev 1");

        Person expectedPerson = new PersonBuilder(AMY).withVisitDate("20/10/2026 1000")
                .withPreviousVisitDates("10/10/2026 1000").withName("Amy Updated").withPhone("92345678")
                .withEmail("updated@example.com").withAddress("20 New Road")
                .withDateOfBirth("20/7/1985").withBloodType("O-").withEmergencyContact("David Tan, 87654321")
                .withNote("Updated note").withTags("followup").build();
        assertEquals("Visit date restored for Amy Updated: 20/10/2026 1000\n"
                + "Discarded visit date and time: 2/11/2026 1000", result.getFeedbackToUser());
        assertPersistedPatient(expectedPerson);
        assertEquals(2, saveCount);
    }

    @Test
    public void execute_afterClearingOptionalFields_preservesEmptyFieldsAndPersistsRollback() throws Exception {
        logic.execute("edit 1 e/ note/ t/ dob/ bt/ ec/");
        Person clearedPerson = new PersonBuilder(AMY).withEmail("").withNote("").withTags()
                .withDateOfBirth("").withBloodType("").withEmergencyContact("").build();
        assertPersistedPatient(clearedPerson);

        logic.execute("prev 1");

        Person expectedPerson = new PersonBuilder(clearedPerson).withVisitDate("20/10/2026 1000")
                .withPreviousVisitDates("10/10/2026 1000").build();
        assertPersistedPatient(expectedPerson);
        assertEquals(2, saveCount);
        Model reloadedModel = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(expectedPerson), reloadedModel.getAddressBook().getPersonList());
    }

    @Test
    public void execute_rollbackThenNext_usesRestoredStateAndFreesDiscardedDate() throws Exception {
        logic.execute("prev 1");
        logic.execute("next 1 1/11/2026 1000");

        Person expectedPerson = new PersonBuilder(AMY).withVisitDate("1/11/2026 1000").build();
        assertPersistedPatient(expectedPerson);

        logic.execute("add n/Benson Meier p/91234567 a/10 Test Road vd/30/10/2026 1000");
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
        assertEquals(3, saveCount);
    }

    @Test
    public void execute_invalidCommands_preservesDataDiskFilterAndSort() throws Exception {
        logic.execute("find Amy");
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        logic.execute("view 1");
        String formatError = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, PrevCommand.MESSAGE_USAGE);

        for (String commandText : List.of("prev", "prev 0", "prev -1", "prev 2147483648", "prev 1 extra")) {
            assertRejectedCommand(commandText, ParseException.class, formatError);
        }
        assertRejectedCommand("prev 2", CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(AMY, logic.execute("view 1").getPersonToView().orElseThrow());
    }

    @Test
    public void execute_saveFailures_reportsExistingErrorWithoutRollingBackModel() throws Exception {
        for (IOException failure : List.of(new IOException("test save failure"),
                new AccessDeniedException("addressBook.json"))) {
            assertSaveFailure(failure);
        }
    }

    private void assertSaveFailure(IOException failure) throws Exception {
        model.setPerson(model.getAddressBook().getPersonList().getFirst(), AMY);
        String savedData = Files.readString(addressBookStorage.getAddressBookFilePath());
        Path storagePath = addressBookStorage.getAddressBookFilePath();
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(storagePath) {
            /**
             * Simulates a storage write failure.
             */
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw failure;
            }
        };
        logic = createLogic(failingStorage);
        String errorFormat = failure instanceof AccessDeniedException
                ? LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT : LogicManager.FILE_OPS_ERROR_FORMAT;

        assertThrows(CommandException.class, String.format(errorFormat, failure.getMessage()), ()
                -> logic.execute("prev 1"));

        assertEquals(savedData, Files.readString(addressBookStorage.getAddressBookFilePath()));
        assertEquals(new PersonBuilder(AMY).withVisitDate("20/10/2026 1000")
                .withPreviousVisitDates("10/10/2026 1000").build(),
                model.getAddressBook().getPersonList().getFirst());
    }

    private LogicManager createLogic(JsonAddressBookStorage storage) {
        return new LogicManager(model,
                new StorageManager(storage, new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json"))),
                new AddressBookParser(FIXED_CLOCK));
    }

    private void assertPersistedPatient(Person expectedPerson) throws Exception {
        assertEquals(List.of(expectedPerson), model.getAddressBook().getPersonList());
        assertEquals(List.of(expectedPerson), model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    private void assertRejectedCommand(String commandText, Class<? extends Exception> exceptionClass,
            String expectedMessage) throws IOException {
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedPersons = new ArrayList<>(model.getFilteredPersonList());
        String savedData = Files.readString(addressBookStorage.getAddressBookFilePath());
        int savesBefore = saveCount;

        assertThrows(exceptionClass, expectedMessage, () -> logic.execute(commandText));

        assertEquals(before, model.getAddressBook());
        assertEquals(displayedPersons, model.getFilteredPersonList());
        assertEquals(savedData, Files.readString(addressBookStorage.getAddressBookFilePath()));
        assertEquals(savesBefore, saveCount);
        assertFalse(logic.isInputRequestPending());
    }
}
