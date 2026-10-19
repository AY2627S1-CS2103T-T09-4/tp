package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;
import seedu.address.testutil.PersonBuilder;

public class SortCommandTest {

    private static final LocalDate CUTOFF_DATE = LocalDate.of(2026, 10, 19);

    private final Person olderBeforeCutoff = personWithVisit("Older before", CUTOFF_DATE.minusDays(2), 23);
    private final Person beforeCutoff = personWithVisit("Before", CUTOFF_DATE.minusDays(1), 23);
    private final Person earlyOnCutoff = personWithVisit("Early on cutoff", CUTOFF_DATE, 0);
    private final Person lateOnCutoff = personWithVisit("Late on cutoff", CUTOFF_DATE, 23);
    private final Person afterCutoff = personWithVisit("After cutoff", CUTOFF_DATE.plusDays(1), 0);
    private final Model model = new ModelManager();

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SortCommand().execute(null));
    }

    @Test
    public void constructor_nullCutoffDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SortCommand(null));
    }

    @Test
    public void execute_unspecifiedCutoffDate_usesCurrentDate() {
        LocalDate today = LocalDate.now();
        Person beforeToday = personWithVisit("Before today", today.minusDays(1), 23);
        Person onToday = personWithVisit("On today", today, 0);
        addPersons(beforeToday, onToday);

        CommandResult result = new SortCommand().execute(model);

        assertEquals(List.of(onToday, beforeToday), model.getFilteredPersonList());
        assertEquals(String.format(SortCommand.MESSAGE_SUCCESS, today.format(SortCommand.CUTOFF_DATE_FORMATTER)),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_mixedDates_showsOnOrAfterCutoffThenBeforeCutoffChronologically() {
        addPersons(afterCutoff, beforeCutoff, lateOnCutoff, olderBeforeCutoff, earlyOnCutoff);
        ObservableList<Person> displayedPersons = model.getFilteredPersonList();

        CommandResult result = new SortCommand(CUTOFF_DATE).execute(model);

        assertEquals(String.format(SortCommand.MESSAGE_SUCCESS,
                CUTOFF_DATE.format(SortCommand.CUTOFF_DATE_FORMATTER)), result.getFeedbackToUser());
        assertEquals(List.of(earlyOnCutoff, lateOnCutoff, afterCutoff, olderBeforeCutoff, beforeCutoff),
                displayedPersons);
        assertSame(displayedPersons, model.getFilteredPersonList());
        assertEquals(List.of(afterCutoff, beforeCutoff, lateOnCutoff, olderBeforeCutoff, earlyOnCutoff),
                model.getAddressBook().getPersonList());
        assertThrows(UnsupportedOperationException.class, () -> displayedPersons.remove(0));
    }

    @Test
    public void execute_filteredList_preservesPreviousFilter() {
        addPersons(afterCutoff, earlyOnCutoff);
        model.updateFilteredPersonList(person -> person.equals(afterCutoff));

        new SortCommand(CUTOFF_DATE).execute(model);

        assertEquals(List.of(afterCutoff), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyOrBeforeCutoffOnlyList_sortsDisplayedVisits() {
        new SortCommand(CUTOFF_DATE).execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());

        addPersons(beforeCutoff, olderBeforeCutoff);
        new SortCommand(CUTOFF_DATE).execute(model);
        assertEquals(List.of(olderBeforeCutoff, beforeCutoff), model.getFilteredPersonList());
        assertEquals(List.of(beforeCutoff, olderBeforeCutoff), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_repeatedSort_preservesChronologicalOrder() {
        addPersons(afterCutoff, lateOnCutoff, earlyOnCutoff);

        new SortCommand(CUTOFF_DATE).execute(model);
        new SortCommand(CUTOFF_DATE).execute(model);

        assertEquals(List.of(earlyOnCutoff, lateOnCutoff, afterCutoff), model.getFilteredPersonList());
    }

    @Test
    public void execute_listAfterSort_restoresAllPersonsInStoredOrder() {
        addPersons(afterCutoff, beforeCutoff, earlyOnCutoff);
        new SortCommand(CUTOFF_DATE).execute(model);

        new ListCommand().execute(model);

        assertEquals(List.of(afterCutoff, beforeCutoff, earlyOnCutoff), model.getFilteredPersonList());
    }

    @Test
    public void execute_findAfterSort_restoresMatchingPersonsInStoredOrder() {
        addPersons(afterCutoff, beforeCutoff, earlyOnCutoff);
        new SortCommand(CUTOFF_DATE).execute(model);

        new FindCommand(new NameContainsKeywordsPredicate(List.of("After", "Before", "Early"))).execute(model);

        assertEquals(List.of(afterCutoff, beforeCutoff, earlyOnCutoff), model.getFilteredPersonList());
    }

    @Test
    public void execute_deleteAfterSort_deletesDisplayedPerson() throws Exception {
        addPersons(afterCutoff, beforeCutoff, earlyOnCutoff, lateOnCutoff);
        new SortCommand(CUTOFF_DATE).execute(model);

        CommandResult requestResult = new DeleteCommand(INDEX_FIRST_PERSON).execute(model);
        requestResult.getInputRequest().orElseThrow().respond("y", model);

        assertEquals(List.of(lateOnCutoff, afterCutoff, beforeCutoff), model.getFilteredPersonList());
        assertEquals(List.of(afterCutoff, beforeCutoff, lateOnCutoff), model.getAddressBook().getPersonList());
    }

    @Test
    public void equals() {
        SortCommand command = new SortCommand(CUTOFF_DATE);
        SortCommand equivalentCommand = new SortCommand(LocalDate.of(2026, 10, 19));

        assertEquals(command, command);
        assertEquals(command, equivalentCommand);
        assertEquals(command.hashCode(), equivalentCommand.hashCode());
        assertEquals(new SortCommand(), new SortCommand());
        assertNotEquals(command, new SortCommand());
        assertNotEquals(command, new SortCommand(CUTOFF_DATE.plusDays(1)));
        assertNotEquals(command, null);
        assertNotEquals(command, new ListCommand());
    }

    private void addPersons(Person... persons) {
        for (Person person : persons) {
            model.addPerson(person);
        }
    }

    private Person personWithVisit(String name, LocalDate date, int hour) {
        return new PersonBuilder().withName(name)
                .withVisitDate(date.atTime(hour, 0).format(VisitDate.FORMATTER)).build();
    }
}
