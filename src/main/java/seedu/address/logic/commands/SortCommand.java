package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Comparator;

import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Sorts displayed persons by visit date, with current and future visits before past visits.
 */
public class SortCommand extends Command {

    public static final String COMMAND_WORD = "sort";
    public static final String MESSAGE_SUCCESS = "Sorted displayed visits by date, with upcoming visits first.";

    /**
     * Sorts the displayed visits using the computer's local date at execution time.
     */
    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate today = LocalDate.now();
        Comparator<Person> visitDateComparator = Comparator
                .comparing((Person person) -> isPastVisit(person, today))
                .thenComparing(person -> person.getVisitDate().value);
        model.sortFilteredPersonList(visitDateComparator);
        return new CommandResult(MESSAGE_SUCCESS);
    }

    private static boolean isPastVisit(Person person, LocalDate today) {
        return person.getVisitDate().value.toLocalDate().isBefore(today);
    }
}
