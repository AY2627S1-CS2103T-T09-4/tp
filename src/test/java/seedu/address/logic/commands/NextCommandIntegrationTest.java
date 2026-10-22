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
import seedu.address.logic.parser.NextCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests next parsing, command execution, persistence, and storage errors through real components.
 */
public class NextCommandIntegrationTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-14T04:00:30Z"),
            ZoneId.of("Asia/Singapore"));
    private static final String NEW_VISIT_DATE = "1/11/2026 0930";

    @TempDir
    private Path testFolder;

    private Model model;
    private LogicManager logic;
    private JsonAddressBookStorage addressBookStorage;
    private Person amy;
    private Person benson;
    private int saveCount;

    @BeforeEach
    public void setUp() throws IOException {
        amy = new PersonBuilder().withVisitDate("13/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000", "2/10/2026 1000")
                .withNote("Review  medication").withTags("priority", "friend").build();
        benson = new PersonBuilder().withName("Benson Meier").withPhone("91234567")
                .withVisitDate("12/10/2026 1000").withEmail("").withNote("").withTags().build();
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(amy, benson));
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
        logic = createLogic(model, addressBookStorage, FIXED_CLOCK);
    }

    @Test
    public void execute_next_appendsCurrentDateAndSurvivesReload() throws Exception {
        CommandResult result = logic.execute("next 1 " + NEW_VISIT_DATE);
        Person expectedPerson = new PersonBuilder(amy).withVisitDate(NEW_VISIT_DATE)
                .withPreviousVisitDates("1/10/2026 1000", "2/10/2026 1000", "13/10/2026 1000").build();

        assertEquals(String.format(NextCommand.MESSAGE_SUCCESS, amy.getName(), NEW_VISIT_DATE),
                result.getFeedbackToUser());
        assertPersistedPatients(expectedPerson, benson);
        assertEquals(1, saveCount);
        assertTrue(result.shouldSaveAddressBook());
        assertTrue(result.getPersonToView().isEmpty());
        assertFalse(logic.isInputRequestPending());
        Model reloadedModel = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloadedModel.getAddressBook());
        assertEquals(model.getFilteredPersonList(), reloadedModel.getFilteredPersonList());
    }

    @Test
    public void execute_filteredIndex_persistsDisplayedPatientAndRestoresStoredOrder() throws Exception {
        logic.execute("find n/Meier");
        assertEquals(List.of(benson), model.getFilteredPersonList());

        logic.execute("next\t1 \t01/11/2026  0930");

        assertPersistedPatients(amy, new PersonBuilder(benson).withVisitDate(NEW_VISIT_DATE)
                .withPreviousVisitDates("12/10/2026 1000").build());
    }

    @Test
    public void execute_sortedIndexAndView_persistsDisplayedPatientAndClearsSort() throws Exception {
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        CommandResult viewResult = logic.execute("view 1");
        assertEquals(benson, viewResult.getPersonToView().orElseThrow());

        logic.execute("next 1 " + NEW_VISIT_DATE);

        assertPersistedPatients(amy, new PersonBuilder(benson).withVisitDate(NEW_VISIT_DATE)
                .withPreviousVisitDates("12/10/2026 1000").build());
    }

    @Test
    public void execute_hiddenConflict_preservesDataDiskFilterAndSort() throws Exception {
        Person hiddenPatient = new PersonBuilder(benson).withVisitDate("1/11/2026 1000").build();
        model.setPerson(benson, hiddenPatient);
        addressBookStorage.saveAddressBook(model.getAddressBook());
        logic.execute("find n/Amy");
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));

        CommandException exception = assertRejectedNext("next 1 01/11/2026 1000", CommandException.class,
                String.format(NextCommand.MESSAGE_DUPLICATE_VISIT_DATE,
                        hiddenPatient.getName(), hiddenPatient.getVisitDate()));
        assertEquals(hiddenPatient, exception.getPersonToView().orElseThrow());
    }

    @Test
    public void execute_hiddenPreviousDateConflict_preservesDataDiskFilterAndSort() throws Exception {
        Person hiddenPatient = new PersonBuilder(benson).withVisitDate("2/11/2026 1000")
                .withPreviousVisitDates("1/11/2026 0930").build();
        model.setPerson(benson, hiddenPatient);
        addressBookStorage.saveAddressBook(model.getAddressBook());
        logic.execute("find n/Amy");
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        logic.execute("view 1");

        CommandException exception = assertRejectedNext("next 1 01/11/2026 0930", CommandException.class,
                String.format(NextCommand.MESSAGE_DUPLICATE_VISIT_DATE, hiddenPatient.getName(), NEW_VISIT_DATE));
        assertEquals(hiddenPatient, exception.getPersonToView().orElseThrow());
    }

    @Test
    public void execute_nextThenEditThenNext_retainsBothPreviousCurrentDates() throws Exception {
        logic.execute("next 1 " + NEW_VISIT_DATE);
        logic.execute("find n/Amy");
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        logic.execute("view 1");
        assertRejectedNext("next 1 2/11/2026 1000", CommandException.class,
                String.format(NextCommand.MESSAGE_CURRENT_VISIT_IN_FUTURE, amy.getName(), NEW_VISIT_DATE));

        logic.execute("edit 1 vd/14/10/2026 1000 note/Updated note");
        logic.execute("next 1 2/11/2026 1000");

        Person expectedPerson = new PersonBuilder(amy).withVisitDate("2/11/2026 1000").withNote("Updated note")
                .withPreviousVisitDates("1/10/2026 1000", "2/10/2026 1000", "13/10/2026 1000", "14/10/2026 1000")
                .build();
        assertPersistedPatients(expectedPerson, benson);
        assertEquals(4, saveCount); // Two next commands, find, and edit save; view and the rejected next do not.
    }

    @Test
    public void execute_validationFailures_preservesDataDiskAndDisplay() throws Exception {
        logic.execute("find n/Amy");
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        assertRejectedNext("next 1 13/10/2026 1000", CommandException.class,
                String.format(NextCommand.MESSAGE_UNCHANGED_VISIT_DATE, amy.getName(), amy.getVisitDate()));
        assertRejectedNext("next 1 1/1/2025 1000", CommandException.class,
                String.format(NextCommand.MESSAGE_NON_FUTURE_VISIT_DATE, "14/10/2026 1200"));
        assertRejectedNext("next 1 14/10/2026 1200", CommandException.class,
                String.format(NextCommand.MESSAGE_NON_FUTURE_VISIT_DATE, "14/10/2026 1200"));
        assertRejectedNext("next 2 " + NEW_VISIT_DATE, CommandException.class,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertRejectedNext("next 1 31/2/2026 0930", ParseException.class, VisitDate.MESSAGE_CONSTRAINTS);
        assertRejectedNext("next 2147483648 " + NEW_VISIT_DATE, ParseException.class,
                NextCommandParser.MESSAGE_INDEX_TOO_LARGE + "\n" + NextCommand.MESSAGE_USAGE);
        assertRejectedNext("next 999999999999999999999999 " + NEW_VISIT_DATE, ParseException.class,
                NextCommandParser.MESSAGE_INDEX_TOO_LARGE + "\n" + NextCommand.MESSAGE_USAGE);
        assertRejectedNext("next 1 vd/" + NEW_VISIT_DATE, ParseException.class,
                NextCommandParser.MESSAGE_PREFIX_NOT_SUPPORTED + "\n" + NextCommand.MESSAGE_USAGE);
        assertRejectedNext("next 1 " + NEW_VISIT_DATE + " extra", ParseException.class,
                NextCommandParser.MESSAGE_EXTRA_ARGUMENTS + "\n" + NextCommand.MESSAGE_USAGE);
    }

    @Test
    public void execute_saveFailure_reportsExistingErrorWithoutRollback() throws Exception {
        IOException failure = new IOException("test save failure");
        assertSaveFailure(failure, String.format(LogicManager.FILE_OPS_ERROR_FORMAT, failure.getMessage()));
    }

    @Test
    public void execute_permissionFailure_reportsExistingPermissionError() throws Exception {
        IOException failure = new AccessDeniedException("addressBook.json");
        assertSaveFailure(failure,
                String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, failure.getMessage()));
    }

    @Test
    public void readAddressBook_timeAdvancedPastAppointment_stillLoadsRecord() throws Exception {
        logic.execute("next 1 " + NEW_VISIT_DATE);
        Clock laterClock = Clock.fixed(Instant.parse("2027-01-01T00:00:00Z"), FIXED_CLOCK.getZone());
        Model reloadedModel = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager laterLogic = createLogic(reloadedModel, addressBookStorage, laterClock);
        String expectedMessage = String.format(NextCommand.MESSAGE_NON_FUTURE_VISIT_DATE, "1/1/2027 0800");

        assertEquals(model.getAddressBook(), reloadedModel.getAddressBook());
        assertThrows(CommandException.class, expectedMessage, ()
                -> laterLogic.execute("next 1 1/11/2026 0931"));
        assertEquals(model.getAddressBook(), reloadedModel.getAddressBook());

        laterLogic.execute("edit 1 vd/14/10/2026 1000");
        assertEquals(new VisitDate("14/10/2026 1000"),
                addressBookStorage.readAddressBook().orElseThrow().getPersonList().getFirst().getVisitDate());
    }

    private LogicManager createLogic(Model targetModel, JsonAddressBookStorage storage, Clock clock) {
        return new LogicManager(targetModel,
                new StorageManager(storage, new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json"))),
                new AddressBookParser(clock));
    }

    private void assertPersistedPatients(Person... expectedPersons) throws Exception {
        List<Person> expectedPatients = List.of(expectedPersons);
        assertEquals(expectedPatients, model.getAddressBook().getPersonList());
        assertEquals(expectedPatients, model.getFilteredPersonList());
        assertEquals(expectedPatients, addressBookStorage.readAddressBook().orElseThrow().getPersonList());
    }

    private <T extends Exception> T assertRejectedNext(String commandText, Class<T> exceptionClass,
            String expectedMessage) throws IOException {
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedPersons = new ArrayList<>(model.getFilteredPersonList());
        String savedData = Files.readString(addressBookStorage.getAddressBookFilePath());
        int savesBefore = saveCount;

        T exception = assertThrows(exceptionClass, expectedMessage, () -> logic.execute(commandText));

        assertEquals(before, model.getAddressBook());
        assertEquals(displayedPersons, model.getFilteredPersonList());
        assertEquals(savedData, Files.readString(addressBookStorage.getAddressBookFilePath()));
        assertEquals(savesBefore, saveCount);
        assertFalse(logic.isInputRequestPending());
        return exception;
    }

    private void assertSaveFailure(IOException failure, String expectedMessage) throws Exception {
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
        logic = createLogic(model, failingStorage, FIXED_CLOCK);

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("next 1 " + NEW_VISIT_DATE));

        assertEquals(savedData, Files.readString(addressBookStorage.getAddressBookFilePath()));
        assertEquals(new VisitDate(NEW_VISIT_DATE), model.getAddressBook().getPersonList().getFirst().getVisitDate());
        assertEquals(List.of(new VisitDate("1/10/2026 1000"), new VisitDate("2/10/2026 1000"), amy.getVisitDate()),
                model.getAddressBook().getPersonList().getFirst().getPreviousVisitDates());
        assertTrue(model.getAddressBook().getPersonList().contains(benson));
    }
}
