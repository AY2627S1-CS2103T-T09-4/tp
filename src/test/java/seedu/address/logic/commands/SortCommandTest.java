package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private final LocalDate today = LocalDate.now();
    private final Person past = personWithVisit("Past", today.minusDays(1), 23);
    private final Person early = personWithVisit("Early", today, 0);
    private final Person late = personWithVisit("Late", today, 23);
    private final Person future = personWithVisit("Future", today.plusDays(1), 0);
    private final Model model = new ModelManager();

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SortCommand().execute(null));
    }

    @Test
    public void execute_mixedDates_showsTodayAndFutureChronologically() {
        addPersons(future, late, past, early);
        ObservableList<Person> displayedPersons = model.getFilteredPersonList();

        CommandResult result = new SortCommand().execute(model);

        assertEquals(SortCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(List.of(early, late, future), displayedPersons);
        assertSame(displayedPersons, model.getFilteredPersonList());
        assertEquals(List.of(future, late, past, early), model.getAddressBook().getPersonList());
        assertThrows(UnsupportedOperationException.class, () -> displayedPersons.remove(0));
    }

    @Test
    public void execute_filteredList_includesVisitsOutsidePreviousFilter() {
        addPersons(future, early);
        model.updateFilteredPersonList(person -> person.equals(future));

        new SortCommand().execute(model);

        assertEquals(List.of(early, future), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyOrPastOnlyList_showsEmptyList() {
        new SortCommand().execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());

        addPersons(past);
        new SortCommand().execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(List.of(past), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_equalDatesAndRepeatedSort_preservesTieOrder() {
        Person sameTime = personWithVisit("Same time", today, 0);
        addPersons(future, sameTime, early);

        new SortCommand().execute(model);
        new SortCommand().execute(model);

        assertEquals(List.of(sameTime, early, future), model.getFilteredPersonList());
    }

    @Test
    public void execute_listAfterSort_restoresAllPersonsInStoredOrder() {
        addPersons(future, past, early);
        new SortCommand().execute(model);

        new ListCommand().execute(model);

        assertEquals(List.of(future, past, early), model.getFilteredPersonList());
    }

    @Test
    public void execute_findAfterSort_restoresMatchingPersonsInStoredOrder() {
        addPersons(future, past, early);
        new SortCommand().execute(model);

        new FindCommand(new NameContainsKeywordsPredicate(List.of("Future", "Past", "Early"))).execute(model);

        assertEquals(List.of(future, past, early), model.getFilteredPersonList());
    }

    @Test
    public void execute_deleteAfterSort_deletesDisplayedPerson() throws Exception {
        addPersons(future, past, early, late);
        new SortCommand().execute(model);

        CommandResult requestResult = new DeleteCommand(INDEX_FIRST_PERSON).execute(model);
        requestResult.getInputRequest().orElseThrow().respond("y", model);

        assertEquals(List.of(late, future), model.getFilteredPersonList());
        assertEquals(List.of(future, past, late), model.getAddressBook().getPersonList());
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
