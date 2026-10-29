package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;
import seedu.address.testutil.PersonBuilder;

public class AgendaCommandTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 10, 27);

    private final Person beforeRange = personWithVisit("Before", START_DATE.minusDays(1), 23);
    private final Person firstDayLate = personWithVisit("First late", START_DATE, 18);
    private final Person firstDayEarly = personWithVisit("First early", START_DATE, 9);
    private final Person middleDay = personWithVisit("Middle", START_DATE.plusDays(3), 12);
    private final Person lastDay = personWithVisit("Last", START_DATE.plusDays(6), 23);
    private final Person afterRange = personWithVisit("After", START_DATE.plusDays(7), 0);
    private final Model model = new ModelManager();

    @Test
    public void execute_startDateOnly_showsSevenDaysChronologically() {
        addPersons(afterRange, firstDayLate, beforeRange, lastDay, middleDay, firstDayEarly);

        CommandResult result = new AgendaCommand(START_DATE).execute(model);

        assertEquals(List.of(firstDayEarly, firstDayLate, middleDay, lastDay), model.getFilteredPersonList());
        assertEquals(String.format(AgendaCommand.MESSAGE_MULTIPLE_VISITS, 4, "27/10/2026", "2/11/2026"),
                result.getFeedbackToUser());
        assertFalse(result.shouldSaveAddressBook());
        assertEquals(List.of(afterRange, firstDayLate, beforeRange, lastDay, middleDay, firstDayEarly),
                model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_customRange_usesInclusiveStartAndEndDay() {
        addPersons(afterRange, firstDayLate, firstDayEarly, middleDay);

        CommandResult result = new AgendaCommand(START_DATE, 1).execute(model);

        assertEquals(List.of(firstDayEarly, firstDayLate), model.getFilteredPersonList());
        assertEquals(String.format(AgendaCommand.MESSAGE_MULTIPLE_VISITS, 2, "27/10/2026", "27/10/2026"),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_existingFilter_replacesItWithAgendaRange() {
        addPersons(afterRange, firstDayEarly, middleDay);
        model.updateFilteredPersonList(person -> person.equals(afterRange));

        new AgendaCommand(START_DATE, 7).execute(model);

        assertEquals(List.of(firstDayEarly, middleDay), model.getFilteredPersonList());
    }

    @Test
    public void execute_zeroOrOneVisit_usesSpecificFeedback() {
        CommandResult emptyResult = new AgendaCommand(START_DATE, 1).execute(model);
        assertEquals(String.format(AgendaCommand.MESSAGE_NO_VISITS, "27/10/2026", "27/10/2026"),
                emptyResult.getFeedbackToUser());

        addPersons(firstDayEarly);
        CommandResult oneVisitResult = new AgendaCommand(START_DATE, 1).execute(model);
        assertEquals(String.format(AgendaCommand.MESSAGE_ONE_VISIT, "27/10/2026", "27/10/2026"),
                oneVisitResult.getFeedbackToUser());
    }

    @Test
    public void execute_rangeEndingOnLastSupportedDate_includesLastDay() {
        Person finalDayVisit = personWithVisit("Final day", LocalDate.of(2099, 12, 31), 23);
        addPersons(finalDayVisit);

        CommandResult result = new AgendaCommand(LocalDate.of(2099, 12, 31), 1).execute(model);

        assertEquals(List.of(finalDayVisit), model.getFilteredPersonList());
        assertEquals(String.format(AgendaCommand.MESSAGE_ONE_VISIT, "31/12/2099", "31/12/2099"),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_listAfterAgenda_restoresStoredOrder() {
        addPersons(middleDay, firstDayEarly);
        new AgendaCommand(START_DATE, 7).execute(model);

        new ListCommand().execute(model);

        assertEquals(List.of(middleDay, firstDayEarly), model.getFilteredPersonList());
    }

    @Test
    public void constructor_invalidArguments_throwsException() {
        assertThrows(NullPointerException.class, () -> new AgendaCommand((LocalDate) null));
        assertThrows(IllegalArgumentException.class, () -> new AgendaCommand(START_DATE, 0));
        assertThrows(IllegalArgumentException.class, () ->
                new AgendaCommand(START_DATE, AgendaCommand.MAX_DAYS + 1));
        assertThrows(IllegalArgumentException.class, () -> new AgendaCommand(LocalDate.of(2099, 12, 31), 2));
        assertThrows(IllegalArgumentException.class, () -> new AgendaCommand(LocalDate.MAX, 1));
        assertThrows(NullPointerException.class, () -> new AgendaCommand(START_DATE).execute(null));
    }

    @Test
    public void equals() {
        AgendaCommand command = new AgendaCommand(START_DATE, 7);
        AgendaCommand equivalentCommand = new AgendaCommand(LocalDate.of(2026, 10, 27), 7);

        assertEquals(command, command);
        assertEquals(command, equivalentCommand);
        assertEquals(command.hashCode(), equivalentCommand.hashCode());
        assertNotEquals(command, new AgendaCommand(START_DATE.plusDays(1), 7));
        assertNotEquals(command, new AgendaCommand(START_DATE, 8));
        assertNotEquals(command, null);
        assertNotEquals(command, new ListCommand());
    }

    private void addPersons(Person... persons) {
        for (Person person : persons) {
            model.addPerson(person);
        }
    }

    private static Person personWithVisit(String name, LocalDate date, int hour) {
        return new PersonBuilder().withName(name)
                .withVisitDate(date.atTime(hour, 0).format(VisitDate.FORMATTER)).build();
    }
}
