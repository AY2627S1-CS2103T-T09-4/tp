package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
import seedu.address.logic.parser.NextCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;
import seedu.address.testutil.CountingClock;
import seedu.address.testutil.LogicManagerIntegrationFixture;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests next parsing, command execution, persistence, and storage errors through real components.
 */
public class NextCommandIntegrationTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-14T04:00:30Z"),
            ZoneId.of("Asia/Singapore"));
    private static final String NEW_VISIT_DATE = "1/11/2026 0930";
    private static final String FULL_DAY = "2/11/2026";
    private static final String FULL_DAY_VISIT_DATE = FULL_DAY + " 1630";
    private static final String FULL_DAY_DISPLAY_DATE = new VisitDate(FULL_DAY_VISIT_DATE).toDayString();

    @TempDir
    private Path testFolder;

    private Model model;
    private LogicManager logic;
    private LogicManagerIntegrationFixture fixture;
    private CountingClock clock;
    private Person amy;
    private Person benson;

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
        clock = new CountingClock(FIXED_CLOCK);
        fixture = new LogicManagerIntegrationFixture(model, testFolder, clock);
        logic = fixture.getLogic();
    }

    @Test
    public void execute_next_appendsCurrentDateAndSurvivesReload() throws Exception {
        CommandResult result = logic.execute("next 1 " + NEW_VISIT_DATE);
        Person expectedPerson = new PersonBuilder(amy).withVisitDate(NEW_VISIT_DATE)
                .withPreviousVisitDates("1/10/2026 1000", "2/10/2026 1000", "13/10/2026 1000").build();

        assertEquals(String.format(NextCommand.MESSAGE_SUCCESS, amy.getName(), NEW_VISIT_DATE),
                result.getFeedbackToUser());
        fixture.assertPersistedPatients(expectedPerson, benson);
        assertEquals(1, fixture.getSaveCount());
        assertTrue(result.shouldSaveAddressBook());
        assertEquals(expectedPerson, result.getPersonToView().orElseThrow());
        assertFalse(logic.isInputRequestPending());
        Model reloadedModel = new ModelManager(fixture.getAddressBookStorage().readAddressBook().orElseThrow(),
                new UserPrefs());
        assertEquals(model.getAddressBook(), reloadedModel.getAddressBook());
        assertEquals(model.getFilteredPersonList(), reloadedModel.getFilteredPersonList());
    }

    @Test
    public void execute_filteredIndex_persistsDisplayedPatientAndRestoresStoredOrder() throws Exception {
        logic.execute("find n/Meier");
        assertEquals(List.of(benson), model.getFilteredPersonList());

        logic.execute("next\t1 \t01/11/2026  0930");

        Person expectedBenson = new PersonBuilder(benson).withVisitDate(NEW_VISIT_DATE)
                .withPreviousVisitDates("12/10/2026 1000").build();
        fixture.assertPersistedPatients(amy, expectedBenson);
        assertEquals(List.of(expectedBenson), model.getFilteredPersonList());
    }

    @Test
    public void execute_sortedIndexAndView_persistsDisplayedPatientAndClearsSort() throws Exception {
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        CommandResult viewResult = logic.execute("view 1");
        assertEquals(benson, viewResult.getPersonToView().orElseThrow());

        logic.execute("next 1 " + NEW_VISIT_DATE);

        fixture.assertPersistedPatients(amy, new PersonBuilder(benson).withVisitDate(NEW_VISIT_DATE)
                .withPreviousVisitDates("12/10/2026 1000").build());
    }

    @Test
    public void execute_hiddenConflict_preservesDataDiskFilterAndSort() throws Exception {
        Person hiddenPatient = new PersonBuilder(benson).withVisitDate("1/11/2026 1000").build();
        model.setPerson(benson, hiddenPatient);
        fixture.getAddressBookStorage().saveAddressBook(model.getAddressBook());
        logic.execute("find n/Amy");
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));

        CommandException exception = fixture.assertRejectedCommand("next 1 01/11/2026 1000", CommandException.class,
                String.format(NextCommand.MESSAGE_DUPLICATE_VISIT_DATE,
                        hiddenPatient.getName(), hiddenPatient.getVisitDate()));
        assertEquals(hiddenPatient, exception.getPersonToView().orElseThrow());
    }

    @Test
    public void execute_hiddenPreviousDateConflict_preservesDataDiskFilterAndSort() throws Exception {
        Person hiddenPatient = new PersonBuilder(benson).withVisitDate("2/11/2026 1000")
                .withPreviousVisitDates("1/11/2026 0930").build();
        model.setPerson(benson, hiddenPatient);
        fixture.getAddressBookStorage().saveAddressBook(model.getAddressBook());
        logic.execute("find n/Amy");
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        logic.execute("view 1");

        CommandException exception = fixture.assertRejectedCommand("next 1 01/11/2026 0930", CommandException.class,
                String.format(NextCommand.MESSAGE_DUPLICATE_VISIT_DATE, hiddenPatient.getName(), NEW_VISIT_DATE));
        assertEquals(hiddenPatient, exception.getPersonToView().orElseThrow());
    }

    @Test
    public void execute_nextThenEditThenNext_retainsBothPreviousCurrentDates() throws Exception {
        logic.execute("next 1 " + NEW_VISIT_DATE);
        logic.execute("find n/Amy");
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        logic.execute("view 1");
        fixture.assertRejectedCommand("next 1 2/11/2026 1000", CommandException.class,
                String.format(NextCommand.MESSAGE_CURRENT_VISIT_IN_FUTURE, amy.getName(), NEW_VISIT_DATE));

        logic.execute("edit 1 vd/14/10/2026 1000 note/Updated note");
        logic.execute("next 1 2/11/2026 1000");

        Person expectedPerson = new PersonBuilder(amy).withVisitDate("2/11/2026 1000").withNote("Updated note")
                .withPreviousVisitDates("1/10/2026 1000", "2/10/2026 1000", "13/10/2026 1000", "14/10/2026 1000")
                .build();
        fixture.assertPersistedPatients(expectedPerson, benson);
        assertEquals(List.of(expectedPerson), model.getFilteredPersonList());
        assertEquals(4, fixture.getSaveCount());
        // Two next commands, find, and edit save; view and the rejected next do not.
    }

    @Test
    public void execute_validationFailures_preservesDataDiskAndDisplay() throws Exception {
        logic.execute("find n/Amy");
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        fixture.assertRejectedCommand("next 1 13/10/2026 1000", CommandException.class,
                String.format(NextCommand.MESSAGE_UNCHANGED_VISIT_DATE, amy.getName(), amy.getVisitDate()));
        fixture.assertRejectedCommand("next 1 1/1/2025 1000", CommandException.class,
                String.format(NextCommand.MESSAGE_NON_FUTURE_VISIT_DATE, "14/10/2026 1200"));
        fixture.assertRejectedCommand("next 1 14/10/2026 1200", CommandException.class,
                String.format(NextCommand.MESSAGE_NON_FUTURE_VISIT_DATE, "14/10/2026 1200"));
        fixture.assertRejectedCommand("next 2 " + NEW_VISIT_DATE, CommandException.class,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        fixture.assertRejectedCommand("next 1 31/2/2026 0930", ParseException.class, VisitDate.MESSAGE_CONSTRAINTS);
        fixture.assertRejectedCommand("next 2147483648 " + NEW_VISIT_DATE, ParseException.class,
                NextCommandParser.MESSAGE_INDEX_TOO_LARGE + "\n" + NextCommand.MESSAGE_USAGE);
        fixture.assertRejectedCommand("next 999999999999999999999999 " + NEW_VISIT_DATE, ParseException.class,
                NextCommandParser.MESSAGE_INDEX_TOO_LARGE + "\n" + NextCommand.MESSAGE_USAGE);
        fixture.assertRejectedCommand("next 1 vd/" + NEW_VISIT_DATE, ParseException.class,
                NextCommandParser.MESSAGE_PREFIX_NOT_SUPPORTED + "\n" + NextCommand.MESSAGE_USAGE);
        fixture.assertRejectedCommand("next 1 " + NEW_VISIT_DATE + " extra", ParseException.class,
                NextCommandParser.MESSAGE_EXTRA_ARGUMENTS + "\n" + NextCommand.MESSAGE_USAGE);
    }

    @Test
    public void execute_saveFailure_reportsExistingErrorAndRollsBack() throws Exception {
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
        Model reloadedModel = new ModelManager(fixture.getAddressBookStorage().readAddressBook().orElseThrow(),
                new UserPrefs());
        LogicManager laterLogic = fixture.createLogic(reloadedModel, laterClock);
        String expectedMessage = String.format(NextCommand.MESSAGE_NON_FUTURE_VISIT_DATE, "1/1/2027 0800");

        assertEquals(model.getAddressBook(), reloadedModel.getAddressBook());
        assertThrows(CommandException.class, expectedMessage, () ->
                laterLogic.execute("next 1 1/11/2026 0931"));
        assertEquals(model.getAddressBook(), reloadedModel.getAddressBook());

        laterLogic.execute("edit 1 vd/14/10/2026 1000");
        VisitDate storedVisitDate = fixture.getAddressBookStorage().readAddressBook().orElseThrow()
                .getPersonList().getFirst().getVisitDate();
        assertEquals(new VisitDate("14/10/2026 1000"), storedVisitDate);
    }

    @Test
    public void execute_deferredConfirmationAccepted_persistsOnceAndSurvivesReload() throws Exception {
        // EP: Confirmation operates on a filtered, reordered display while capacity counts hidden patients.
        int displayedAmyIndex = prepareFullDayRequest();
        DeferredSnapshot beforeRequest = takeSnapshot();
        openDeferredRequest(displayedAmyIndex, beforeRequest);

        CommandResult confirmedResult = logic.execute(" Y ");

        Person expectedAmy = new PersonBuilder(amy).withVisitDate(FULL_DAY_VISIT_DATE)
                .withPreviousVisitDates("1/10/2026 1000", "2/10/2026 1000", "13/10/2026 1000").build();
        AddressBook expectedAddressBook = new AddressBook(beforeRequest.addressBook());
        expectedAddressBook.setPerson(amy, expectedAmy);
        assertEquals(String.format(NextCommand.MESSAGE_SUCCESS, amy.getName(), FULL_DAY_VISIT_DATE),
                confirmedResult.getFeedbackToUser());
        assertTrue(confirmedResult.shouldSaveAddressBook());
        assertFalse(logic.isInputRequestPending());
        assertTrue(confirmedResult.getInputRequest().isEmpty());
        assertEquals(expectedAmy, confirmedResult.getPersonToView().orElseThrow());
        fixture.assertPersistedPatients(expectedAddressBook.getPersonList().toArray(Person[]::new));
        assertEquals(beforeRequest.saveCount() + 1, fixture.getSaveCount());
        Model reloadedModel = new ModelManager(fixture.getAddressBookStorage().readAddressBook().orElseThrow(),
                new UserPrefs());
        assertEquals(model.getAddressBook(), reloadedModel.getAddressBook());
        assertEquals(List.of(benson), model.getFilteredPersonList());
    }

    @Test
    public void execute_deferredConfirmationCancelled_doesNotPersistOrMutate() throws Exception {
        // EP: Blank input belongs to the pending request, rather than the normal command parser.
        int displayedAmyIndex = prepareFullDayRequest();
        DeferredSnapshot beforeRequest = takeSnapshot();
        openDeferredRequest(displayedAmyIndex, beforeRequest);

        CommandResult cancelledResult = logic.execute("");

        assertEquals(NextCommand.MESSAGE_NEXT_CANCELLED, cancelledResult.getFeedbackToUser());
        assertFalse(cancelledResult.shouldSaveAddressBook());
        assertFalse(logic.isInputRequestPending());
        assertTrue(cancelledResult.getInputRequest().isEmpty());
        assertUnchanged(beforeRequest);
    }

    @Test
    public void execute_deferredConfirmationAtAppointmentTime_rejectsAndClearsPendingInput() throws Exception {
        // Boundary: Confirmation exactly at the requested appointment must revalidate and clear the request.
        int displayedAmyIndex = prepareFullDayRequest();
        DeferredSnapshot beforeRequest = takeSnapshot();
        openDeferredRequest(displayedAmyIndex, beforeRequest);
        clock.setDelegate(Clock.fixed(Instant.parse("2026-11-02T08:30:00Z"), FIXED_CLOCK.getZone()));

        String expectedMessage = String.format(NextCommand.MESSAGE_NON_FUTURE_VISIT_DATE, FULL_DAY_VISIT_DATE);
        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("y"));

        assertFalse(logic.isInputRequestPending());
        assertUnchanged(beforeRequest);
        CommandResult viewResult = logic.execute("view " + displayedAmyIndex);
        assertEquals(amy, viewResult.getPersonToView().orElseThrow());
        assertFalse(viewResult.shouldSaveAddressBook());
        assertTrue(viewResult.getInputRequest().isEmpty());
        assertFalse(logic.isInputRequestPending());
        assertUnchanged(beforeRequest);
    }

    /** Opens the pending request through the real parser and verifies its no-write contract. */
    private void openDeferredRequest(int displayedAmyIndex, DeferredSnapshot beforeRequest) throws Exception {
        CommandResult requestResult = logic.execute("next " + displayedAmyIndex + " " + FULL_DAY_VISIT_DATE);
        assertEquals(String.format(NextCommand.MESSAGE_CONFIRM_FULL_DAY, Model.MAX_VISITS_PER_DAY,
                FULL_DAY_DISPLAY_DATE, amy.getName()), requestResult.getFeedbackToUser());
        assertTrue(requestResult.getInputRequest().isPresent());
        assertFalse(requestResult.shouldSaveAddressBook());
        assertTrue(logic.isInputRequestPending());
        assertUnchanged(beforeRequest);
    }

    private void assertUnchanged(DeferredSnapshot snapshot) throws IOException {
        assertEquals(snapshot.addressBook(), model.getAddressBook());
        assertEquals(snapshot.displayedPersons(), model.getFilteredPersonList());
        assertArrayEquals(snapshot.savedBytes(),
                Files.readAllBytes(fixture.getAddressBookStorage().getAddressBookFilePath()));
        assertEquals(snapshot.saveCount(), fixture.getSaveCount());
    }

    private DeferredSnapshot takeSnapshot() throws IOException {
        return new DeferredSnapshot(new AddressBook(model.getAddressBook()),
                List.copyOf(model.getFilteredPersonList()),
                Files.readAllBytes(fixture.getAddressBookStorage().getAddressBookFilePath()), fixture.getSaveCount());
    }

    /** Persists the complete busy-day fixture before recording any command writes or display state. */
    private int prepareFullDayRequest() throws IOException {
        for (int i = 0; i < Model.MAX_VISITS_PER_DAY; i++) {
            model.addPerson(new PersonBuilder().withName("Busy Patient " + i).withPhone("8000000" + i)
                    .withVisitDate(String.format("%s %02d00", FULL_DAY, 8 + i)).build());
        }
        fixture = new LogicManagerIntegrationFixture(model, testFolder, clock);
        logic = fixture.getLogic();
        model.updateFilteredPersonList(person -> person.equals(amy) || person.equals(benson));
        model.sortFilteredPersonList(Comparator.comparing((Person person) -> person.getName().fullName)
                .reversed());
        assertEquals(List.of(benson, amy), model.getFilteredPersonList());
        int amyPosition = model.getFilteredPersonList().indexOf(amy);
        assertTrue(amyPosition >= 0);
        assertEquals(0, fixture.getSaveCount());
        return amyPosition + 1;
    }

    /** Captures all state that a pending, cancelled, or rejected request must preserve. */
    private record DeferredSnapshot(AddressBook addressBook, List<Person> displayedPersons, byte[] savedBytes,
            int saveCount) {
    }

    private void assertSaveFailure(IOException failure, String expectedMessage) throws Exception {
        String savedData = Files.readString(fixture.getAddressBookStorage().getAddressBookFilePath());
        model.updateFilteredPersonList(person -> person.equals(amy));
        model.sortFilteredPersonList(Comparator.comparing((Person person) -> person.getVisitDate().value).reversed());
        AddressBook addressBookBefore = new AddressBook(model.getAddressBook());
        List<Person> displayedPersonsBefore = List.copyOf(model.getFilteredPersonList());
        fixture.useFailingStorage(failure);
        logic = fixture.getLogic();

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("next 1 " + NEW_VISIT_DATE));

        assertEquals(savedData, Files.readString(fixture.getAddressBookStorage().getAddressBookFilePath()));
        assertEquals(addressBookBefore, model.getAddressBook());
        assertEquals(displayedPersonsBefore, model.getFilteredPersonList());
    }
}
