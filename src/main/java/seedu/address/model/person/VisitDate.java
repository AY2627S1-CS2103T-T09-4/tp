package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Represents a Person's visit date and time in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidVisitDate(String)}
 */
public class VisitDate {

    /** Earliest accepted visit year, so that mistyped years such as 0000 or 0026 are rejected. */
    public static final int MIN_YEAR = 1900;
    /** Latest accepted visit year, so that mistyped years such as 9999 or 2206 are rejected. */
    public static final int MAX_YEAR = 2099;

    public static final String MESSAGE_CONSTRAINTS = "Visit dates should be a valid date and time "
            + "in the format d/M/yyyy HHmm, e.g. 18/9/2026 1000, with a year from " + MIN_YEAR + " to " + MAX_YEAR
            + ".";

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d/M/uuuu HHmm")
            .withResolverStyle(ResolverStyle.STRICT);

    /** Format of a calendar day without a time, e.g. {@code 18/9/2026}, used when searching by visit day. */
    public static final DateTimeFormatter DAY_INPUT_FORMATTER = DateTimeFormatter.ofPattern("d/M/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    // English is fixed so that weekday and month names do not change with the user's system language
    private static final DateTimeFormatter DAY_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, d MMM uuuu", Locale.ENGLISH);

    public final LocalDateTime value;

    /**
     * Constructs a {@code VisitDate}.
     *
     * @param visitDate A valid visit date.
     */
    public VisitDate(String visitDate) {
        requireNonNull(visitDate);
        checkArgument(isValidVisitDate(visitDate), MESSAGE_CONSTRAINTS);
        value = LocalDateTime.parse(visitDate, FORMATTER);
    }

    /**
     * Returns true if a given string is an existing date and time in {@link #FORMATTER}'s format with a year from
     * {@value #MIN_YEAR} to {@value #MAX_YEAR}.
     */
    public static boolean isValidVisitDate(String test) {
        try {
            int year = LocalDateTime.parse(test, FORMATTER).getYear();
            return year >= MIN_YEAR && year <= MAX_YEAR;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns true if {@code other} falls on the same calendar day as this visit date, regardless of time.
     */
    public boolean isSameDay(VisitDate other) {
        requireNonNull(other);
        return value.toLocalDate().equals(other.value.toLocalDate());
    }

    /**
     * Returns the weekday and calendar day of this visit date without the time, e.g. {@code Fri, 18 Sep 2026}.
     */
    public String toDayString() {
        return value.format(DAY_FORMATTER);
    }

    @Override
    public String toString() {
        return value.format(FORMATTER);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof VisitDate otherVisitDate)) {
            return false;
        }

        return value.equals(otherVisitDate.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
