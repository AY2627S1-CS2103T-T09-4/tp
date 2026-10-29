package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.logic.Messages.DISPLAY_LOCALE;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;
import seedu.address.model.person.VisitDateWithinRangePredicate;

/**
 * Displays visits in a date range as a chronological agenda.
 */
public class AgendaCommand extends Command {

    public static final String COMMAND_WORD = "agenda";
    public static final int DEFAULT_DAYS = 7;
    public static final int MAX_DAYS = 365;
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows visits in chronological order over a range of calendar days.\n"
            + "Parameters: START_DATE (d/M/yyyy) [DAYS (1-" + MAX_DAYS + ", default: " + DEFAULT_DAYS + ")]\n"
            + "Examples:\n"
            + COMMAND_WORD + " 27/10/2026\n"
            + COMMAND_WORD + " 27/10/2026 14";
    public static final String MESSAGE_NO_VISITS = "No visits scheduled from %1$s to %2$s.";
    public static final String MESSAGE_ONE_VISIT = "Showing 1 visit from %1$s to %2$s.";
    public static final String MESSAGE_MULTIPLE_VISITS = "Showing %1$d visits from %2$s to %3$s.";
    public static final String MESSAGE_INVALID_DATE_RANGE =
            "The agenda date range must end on or before 31/12/" + VisitDate.MAX_YEAR + ".";
    public static final String MESSAGE_INVALID_START_DATE =
            "The agenda must start on or after 1/1/" + VisitDate.MIN_YEAR + ".";
    public static final String MESSAGE_INVALID_DAYS = "Days must be between 1 and " + MAX_DAYS + ".";

    /** First day that can hold a visit, since visit dates are limited to {@link VisitDate#MIN_YEAR}. */
    private static final LocalDate FIRST_SUPPORTED_DATE = LocalDate.of(VisitDate.MIN_YEAR, 1, 1);
    /** Last day that can hold a visit, since visit dates are limited to {@link VisitDate#MAX_YEAR}. */
    private static final LocalDate LAST_SUPPORTED_DATE = LocalDate.of(VisitDate.MAX_YEAR, 12, 31);

    private final LocalDate startDate;
    private final int days;

    /**
     * Creates a seven-day agenda starting on {@code startDate}.
     */
    public AgendaCommand(LocalDate startDate) {
        this(startDate, DEFAULT_DAYS);
    }

    /**
     * Creates an agenda starting on {@code startDate} and spanning {@code days} calendar days.
     */
    public AgendaCommand(LocalDate startDate, int days) {
        this.startDate = requireNonNull(startDate);
        checkArgument(days >= 1 && days <= MAX_DAYS, MESSAGE_INVALID_DAYS);
        checkArgument(!startDate.isBefore(FIRST_SUPPORTED_DATE), MESSAGE_INVALID_START_DATE);
        checkArgument(!startDate.isAfter(LAST_SUPPORTED_DATE.minusDays(days - 1)), MESSAGE_INVALID_DATE_RANGE);
        this.days = days;
    }

    /**
     * Shows visits in the agenda range, ordered by their date and time.
     */
    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate endDate = startDate.plusDays(days);
        VisitDateWithinRangePredicate predicate = new VisitDateWithinRangePredicate(startDate, endDate);

        model.updateFilteredPersonList(predicate);
        model.sortFilteredPersonList(Comparator.comparing((Person person) -> person.getVisitDate().value));

        int visitCount = model.getFilteredPersonList().size();
        String formattedStartDate = startDate.format(VisitDate.DAY_INPUT_FORMATTER);
        String formattedEndDate = endDate.minusDays(1).format(VisitDate.DAY_INPUT_FORMATTER);
        return CommandResult.withoutSaving(formatResultMessage(visitCount, formattedStartDate, formattedEndDate));
    }

    private static String formatResultMessage(int visitCount, String startDate, String endDate) {
        return switch (visitCount) {
            case 0 -> String.format(MESSAGE_NO_VISITS, startDate, endDate);
            case 1 -> String.format(MESSAGE_ONE_VISIT, startDate, endDate);
            default -> String.format(DISPLAY_LOCALE, MESSAGE_MULTIPLE_VISITS, visitCount, startDate, endDate);
        };
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AgendaCommand otherAgendaCommand)) {
            return false;
        }
        return startDate.equals(otherAgendaCommand.startDate) && days == otherAgendaCommand.days;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, days);
    }
}
