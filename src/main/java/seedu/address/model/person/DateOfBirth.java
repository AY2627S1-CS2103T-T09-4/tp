package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Represents an optional patient date of birth. An empty value means it has not been recorded.
 * Guarantees: immutable; nonempty dates are real dates between year 1 and today.
 */
public class DateOfBirth {

    public static final String MESSAGE_CONSTRAINTS = "Date of birth must be a valid date in the format "
            + "d/M/yyyy, e.g. 15/6/1990, with a year from 0001 onwards, and must not be in the future.";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d/M/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private final String value;

    /**
     * Creates a date of birth, or an unrecorded value when the supplied string is empty.
     *
     * @throws IllegalArgumentException If the nonempty date is invalid or in the future.
     */
    public DateOfBirth(String dateOfBirth) {
        requireNonNull(dateOfBirth);
        checkArgument(isValidDateOfBirth(dateOfBirth), MESSAGE_CONSTRAINTS);
        value = dateOfBirth.isEmpty() ? "" : LocalDate.parse(dateOfBirth, FORMATTER).format(FORMATTER);
    }

    /**
     * Returns whether the value is empty or a real date in years 0001 to 9999 no later than today.
     */
    public static boolean isValidDateOfBirth(String dateOfBirth) {
        requireNonNull(dateOfBirth);
        if (dateOfBirth.isEmpty()) {
            return true;
        }
        if (!dateOfBirth.matches("[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}")) {
            return false;
        }
        try {
            LocalDate date = LocalDate.parse(dateOfBirth, FORMATTER);
            return date.getYear() >= 1 && !date.isAfter(LocalDate.now());
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    /**
     * Returns whether the date of birth has not been recorded.
     */
    public boolean isEmpty() {
        return value.isEmpty();
    }

    /**
     * Returns the age in completed years on the supplied date.
     * A leap-day birthday completes another year on March 1 in non-leap years.
     *
     * @throws IllegalArgumentException If no birth date is recorded or the supplied date precedes it.
     */
    public int getAge(LocalDate currentDate) {
        requireNonNull(currentDate);
        checkArgument(!isEmpty(), "An unrecorded date of birth has no age.");
        LocalDate birthDate = LocalDate.parse(value, FORMATTER);
        checkArgument(!currentDate.isBefore(birthDate), "The age date must not precede the date of birth.");
        return Period.between(birthDate, currentDate).getYears();
    }

    /**
     * Returns the date and current age for display, or an empty string when unrecorded.
     */
    public String toDisplayString() {
        return isEmpty() ? "" : value + " (" + getAge(LocalDate.now()) + " years old)";
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof DateOfBirth otherDateOfBirth && value.equals(otherDateOfBirth.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
