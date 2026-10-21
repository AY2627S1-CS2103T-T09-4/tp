package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Sorts displayed persons by visit date around a cutoff date.
 */
public class SortCommand extends Command {

    private static final Logger logger = LogsCenter.getLogger(SortCommand.class);

    public static final String COMMAND_WORD = "sort";
    /** Format used to parse and display cutoff dates. */
    public static final DateTimeFormatter CUTOFF_DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Sorts displayed patients by visit date using an optional cutoff date.\n"
            + "Parameters: date [START_DATE (d/M/yyyy)]\n"
            + "Examples:\n"
            + COMMAND_WORD + " date\n"
            + COMMAND_WORD + " date 18/9/2026";
    public static final String MESSAGE_SUCCESS =
            "Sorted displayed visits by date, with visits on or after %1$s first.";

    private final Optional<LocalDate> specifiedCutoffDate;
    private final Clock clock;

    /**
     * Creates a command that uses the computer's local date at execution time as the cutoff date.
     */
    public SortCommand() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates a command that uses the supplied cutoff date.
     */
    public SortCommand(LocalDate cutoffDate) {
        this(Optional.of(requireNonNull(cutoffDate)), Clock.systemDefaultZone());
    }

    /**
     * Creates a command that uses the supplied clock to determine the cutoff date at execution time.
     */
    SortCommand(Clock clock) {
        this(Optional.empty(), clock);
    }

    private SortCommand(Optional<LocalDate> specifiedCutoffDate, Clock clock) {
        this.specifiedCutoffDate = requireNonNull(specifiedCutoffDate);
        this.clock = requireNonNull(clock);
    }

    /**
     * Sorts displayed visits so that visits on or after the cutoff precede visits before it.
     */
    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate cutoffDate = specifiedCutoffDate.orElseGet(() -> LocalDate.now(clock));
        logger.fine(() -> String.format("Sorting %d displayed patients using cutoff date %s",
                model.getFilteredPersonList().size(), cutoffDate.format(CUTOFF_DATE_FORMATTER)));

        Comparator<Person> visitDateComparator = Comparator
                .comparing((Person person) -> isBeforeCutoff(person, cutoffDate))
                .thenComparing(person -> person.getVisitDate().value);
        model.sortFilteredPersonList(visitDateComparator);
        return new CommandResult(String.format(MESSAGE_SUCCESS, cutoffDate.format(CUTOFF_DATE_FORMATTER)));
    }

    private static boolean isBeforeCutoff(Person person, LocalDate cutoffDate) {
        return person.getVisitDate().value.toLocalDate().isBefore(cutoffDate);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof SortCommand otherSortCommand)) {
            return false;
        }
        return specifiedCutoffDate.equals(otherSortCommand.specifiedCutoffDate)
                && clock.equals(otherSortCommand.clock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(specifiedCutoffDate, clock);
    }
}
