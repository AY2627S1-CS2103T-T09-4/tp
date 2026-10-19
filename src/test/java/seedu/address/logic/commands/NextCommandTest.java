package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests next-visit selection, validation, field preservation, and displayed-list behavior.
 */
public class NextCommandTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-14T04:00:30Z"),
            ZoneId.of("Asia/Singapore"));
    private static final String NEW_VISIT_DATE = "1/11/2026 0930";
    private static final Person AMY = new PersonBuilder().withVisitDate("2/11/2026 1000")
            .withNote("Review  medication").withTags("friend", "priority").build();
    private static final Person BENSON = new PersonBuilder().withName("Benson Meier").withPhone("91234567")
            .withVisitDate("1/11/2026 1000").withEmail("").withNote("").withTags().build();
    private static final Person CARL = new PersonBuilder().withName("Carl Kurz").withPhone("92345678")
            .withVisitDate("1/9/2026 1000").build();

    private final Model model = createModel(AMY, BENSON, CARL);

    @Test
    public void execute_validAppointment_preservesOtherFieldsAndPatients() throws Exception {
        assertScheduleSuccess(INDEX_FIRST_PERSON, AMY, NEW_VISIT_DATE);
    }

    @Test
    public void execute_emptyOptionalFields_preservesEmptyValues() throws Exception {
        assertScheduleSuccess(INDEX_SECOND_PERSON, BENSON, NEW_VISIT_DATE);
    }

    @Test
    public void execute_futureEarlierThanExistingAppointment_success() throws Exception {
        assertTrue(new VisitDate(NEW_VISIT_DATE).value.isBefore(AMY.getVisitDate().value));
        assertScheduleSuccess(INDEX_FIRST_PERSON, AMY, NEW_VISIT_DATE);
    }

    @Test
    public void execute_existingPastAppointment_success() throws Exception {
        assertScheduleSuccess(Index.fromOneBased(3), CARL, NEW_VISIT_DATE);
    }

    @Test
    public void execute_filteredIndex_updatesDisplayedPatientAndRestoresFullList() throws Exception {
        new FindCommand(new NameContainsKeywordsPredicate(List.of("Meier"))).execute(model);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());

        assertScheduleSuccess(INDEX_FIRST_PERSON, BENSON, NEW_VISIT_DATE);
    }

    @Test
    public void execute_sortedIndex_updatesDisplayedPatientAndClearsSort() throws Exception {
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        assertEquals(CARL, model.getFilteredPersonList().getFirst());

        assertScheduleSuccess(INDEX_FIRST_PERSON, CARL, NEW_VISIT_DATE);
    }

    @Test
    public void execute_filteredSortedList_updatesDisplayedPatientAndResetsList() throws Exception {
        model.updateFilteredPersonList(person -> !person.equals(CARL));
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        assertEquals(List.of(BENSON, AMY), model.getFilteredPersonList());

        assertScheduleSuccess(INDEX_FIRST_PERSON, BENSON, NEW_VISIT_DATE);
    }

    @Test
    public void execute_afterView_restoresFullListInStoredOrder() throws Exception {
        model.updateFilteredPersonList(person -> person.equals(BENSON));
        new ViewCommand(INDEX_FIRST_PERSON).execute(model);

        assertScheduleSuccess(INDEX_FIRST_PERSON, BENSON, NEW_VISIT_DATE);
    }

    @Test
    public void execute_unchangedAppointment_preservesDataAndDisplay() {
        assertScheduleFailure(INDEX_FIRST_PERSON, new VisitDate("02/11/2026 1000"), FIXED_CLOCK,
                String.format(NextCommand.MESSAGE_UNCHANGED_VISIT_DATE, AMY.getName(), AMY.getVisitDate()));
    }

    @Test
    public void execute_hiddenPatientConflict_previewsConflictWithoutChangingList() {
        model.updateFilteredPersonList(person -> !person.equals(BENSON));
        model.sortFilteredPersonList(Comparator.comparing((Person person) -> person.getVisitDate().value).reversed());
        assertEquals(List.of(AMY, CARL), model.getFilteredPersonList());

        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        NextCommand command = new NextCommand(INDEX_FIRST_PERSON, new VisitDate("01/11/2026 1000"), FIXED_CLOCK);
        String expectedMessage = String.format(NextCommand.MESSAGE_DUPLICATE_VISIT_DATE,
                BENSON.getName(), BENSON.getVisitDate());

        CommandException exception =
                assertThrows(CommandException.class, expectedMessage, () -> command.execute(model));

        assertEquals(originalAddressBook, model.getAddressBook());
        assertEquals(BENSON, exception.getPersonToView().orElseThrow());
        assertEquals(List.of(AMY, CARL), model.getFilteredPersonList());
    }

    @Test
    public void execute_pastAndCurrentMinuteAppointments_preservesDataAndDisplay() {
        for (String dateTime : List.of("1/1/2026 1000", "14/10/2026 1159", "14/10/2026 1200")) {
            assertScheduleFailure(INDEX_FIRST_PERSON, new VisitDate(dateTime), FIXED_CLOCK,
                    NextCommand.MESSAGE_NON_FUTURE_VISIT_DATE);
        }
    }

    @Test
    public void execute_equalToNow_rejectsAppointment() {
        Clock exactMinuteClock = Clock.fixed(Instant.parse("2026-10-14T04:00:00Z"), FIXED_CLOCK.getZone());
        assertScheduleFailure(INDEX_FIRST_PERSON, new VisitDate("14/10/2026 1200"), exactMinuteClock,
                NextCommand.MESSAGE_NON_FUTURE_VISIT_DATE);
    }

    @Test
    public void execute_nextMinute_acceptsAppointment() throws Exception {
        assertScheduleSuccess(INDEX_FIRST_PERSON, AMY, "14/10/2026 1201");
    }

    @Test
    public void execute_clockTimeZone_usesLocalDateTime() {
        Clock differentZoneClock = FIXED_CLOCK.withZone(ZoneId.of("Pacific/Auckland"));
        assertScheduleFailure(INDEX_FIRST_PERSON, new VisitDate("14/10/2026 1300"), differentZoneClock,
                NextCommand.MESSAGE_NON_FUTURE_VISIT_DATE);
    }

    @Test
    public void execute_timeAdvancedAfterCreation_validatesAtExecution() {
        CountingClock clock = new CountingClock(FIXED_CLOCK);
        NextCommand command = new NextCommand(INDEX_FIRST_PERSON, new VisitDate(NEW_VISIT_DATE), clock);
        assertEquals(0, clock.readCount);
        clock.delegate = Clock.offset(FIXED_CLOCK, Duration.ofDays(30));

        assertThrows(CommandException.class, NextCommand.MESSAGE_NON_FUTURE_VISIT_DATE, () -> command.execute(model));
        assertEquals(1, clock.readCount);
        assertEquals(List.of(AMY, BENSON, CARL), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_validAppointment_readsClockOnce() throws Exception {
        CountingClock clock = new CountingClock(FIXED_CLOCK);
        new NextCommand(INDEX_FIRST_PERSON, new VisitDate(NEW_VISIT_DATE), clock).execute(model);
        assertEquals(1, clock.readCount);
    }

    @Test
    public void execute_outOfRangeDisplayedIndex_preservesDataAndDisplay() {
        model.updateFilteredPersonList(person -> person.equals(BENSON));
        assertScheduleFailure(INDEX_SECOND_PERSON, new VisitDate(NEW_VISIT_DATE), FIXED_CLOCK,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertScheduleFailure(Index.fromOneBased(Integer.MAX_VALUE), new VisitDate(NEW_VISIT_DATE), FIXED_CLOCK,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyList_rejectsIndex() {
        NextCommand command = new NextCommand(INDEX_FIRST_PERSON, new VisitDate(NEW_VISIT_DATE), FIXED_CLOCK);
        String expectedMessage = Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
        assertThrows(CommandException.class, expectedMessage, () -> command.execute(new ModelManager()));
    }

    @Test
    public void equals() {
        NextCommand command = new NextCommand(INDEX_FIRST_PERSON, new VisitDate(NEW_VISIT_DATE), FIXED_CLOCK);
        NextCommand equivalent = new NextCommand(Index.fromOneBased(1), new VisitDate("01/11/2026 0930"), FIXED_CLOCK);
        assertEquals(command, command);
        assertEquals(command, equivalent);
        assertEquals(command.hashCode(), equivalent.hashCode());
        HashSet<NextCommand> commands = new HashSet<>();
        commands.add(command);
        commands.add(equivalent);
        assertEquals(1, commands.size());
        assertNotEquals(command, null);
        assertNotEquals(command, new ListCommand());
        assertNotEquals(command, new NextCommand(INDEX_SECOND_PERSON, new VisitDate(NEW_VISIT_DATE), FIXED_CLOCK));
        assertNotEquals(command, new NextCommand(INDEX_FIRST_PERSON, new VisitDate("1/11/2026 0931"), FIXED_CLOCK));
        assertNotEquals(command, new NextCommand(INDEX_FIRST_PERSON, new VisitDate(NEW_VISIT_DATE),
                Clock.offset(FIXED_CLOCK, Duration.ofMinutes(1))));
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        VisitDate visitDate = new VisitDate(NEW_VISIT_DATE);
        assertThrows(NullPointerException.class, () -> new NextCommand(null, visitDate, FIXED_CLOCK));
        assertThrows(NullPointerException.class, () -> new NextCommand(INDEX_FIRST_PERSON, null, FIXED_CLOCK));
        assertThrows(NullPointerException.class, () -> new NextCommand(INDEX_FIRST_PERSON, visitDate, null));
        NextCommand command = new NextCommand(INDEX_FIRST_PERSON, visitDate, FIXED_CLOCK);
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void toStringMethod() {
        NextCommand command = new NextCommand(INDEX_FIRST_PERSON, new VisitDate(NEW_VISIT_DATE), FIXED_CLOCK);
        assertEquals(NextCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", visitDate=" + NEW_VISIT_DATE + "}", command.toString());
    }

    private void assertScheduleSuccess(Index index, Person originalPerson, String dateTime) throws Exception {
        Person expectedPerson = new PersonBuilder(originalPerson).withVisitDate(dateTime).build();
        AddressBook expectedAddressBook = new AddressBook(model.getAddressBook());
        expectedAddressBook.setPerson(originalPerson, expectedPerson);

        CommandResult result = new NextCommand(index, new VisitDate(dateTime), FIXED_CLOCK).execute(model);

        String expectedMessage = String.format(NextCommand.MESSAGE_SUCCESS,
                expectedPerson.getName(), expectedPerson.getVisitDate());
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedAddressBook, model.getAddressBook());
        assertEquals(expectedAddressBook.getPersonList(), model.getFilteredPersonList());
        assertTrue(result.shouldSaveAddressBook());
        assertTrue(result.getInputRequest().isEmpty());
    }

    private void assertScheduleFailure(Index index, VisitDate visitDate, Clock clock, String expectedMessage) {
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedPersons = new ArrayList<>(model.getFilteredPersonList());

        NextCommand command = new NextCommand(index, visitDate, clock);
        assertThrows(CommandException.class, expectedMessage, () -> command.execute(model));

        assertEquals(before, model.getAddressBook());
        assertEquals(displayedPersons, model.getFilteredPersonList());
    }

    private static Model createModel(Person... persons) {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(persons));
        return new ModelManager(addressBook, new UserPrefs());
    }

    /**
     * Counts execution-time clock reads and allows time to advance within one test.
     */
    private static class CountingClock extends Clock {
        private Clock delegate;
        private int readCount;

        CountingClock(Clock delegate) {
            this.delegate = delegate;
        }

        @Override
        public ZoneId getZone() {
            return delegate.getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new CountingClock(delegate.withZone(zone));
        }

        /**
         * Returns the delegated instant and counts this read.
         */
        @Override
        public Instant instant() {
            readCount++;
            return delegate.instant();
        }
    }
}
