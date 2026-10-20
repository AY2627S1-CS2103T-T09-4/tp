package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;

import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Sorts displayed persons by visit date around a cutoff date.
 */
public class SortCommand extends Command {

    public static final String COMMAND_WORD = "sort";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Sorts the displayed patients by the specified field.\n"
            + "Parameters: FIELD (must be date)\n"
            + "Example: " + COMMAND_WORD + " date";
    public static final String MESSAGE_SUCCESS = "Sorted displayed visits by date, with upcoming visits first.";

    private final Optional<LocalDate> specifiedCutoffDate;

    /**
     * Creates a command that uses the computer's local date at execution time as the cutoff date.
     */
    public SortCommand() {
        specifiedCutoffDate = Optional.empty();
    }

    /**
     * Creates a command that uses the supplied cutoff date.
     */
    public SortCommand(LocalDate cutoffDate) {
        specifiedCutoffDate = Optional.of(requireNonNull(cutoffDate));
    }

    /**
     * Sorts displayed visits so that visits on or after the cutoff precede visits before it.
     */
    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate cutoffDate = specifiedCutoffDate.orElseGet(LocalDate::now);
        Comparator<Person> visitDateComparator = Comparator
                .comparing((Person person) -> isBeforeCutoff(person, cutoffDate))
                .thenComparing(person -> person.getVisitDate().value);
        model.sortFilteredPersonList(visitDateComparator);
        return new CommandResult(MESSAGE_SUCCESS);
    }

    private static boolean isBeforeCutoff(Person person, LocalDate cutoffDate) {
        return person.getVisitDate().value.toLocalDate().isBefore(cutoffDate);
    }
}
