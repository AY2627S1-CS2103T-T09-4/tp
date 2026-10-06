package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Represents a Person's visit date and time in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidVisitDate(String)}
 */
public class VisitDate {

    public static final String MESSAGE_CONSTRAINTS = "Visit dates should be a valid date and time "
            + "in the format d/M/yyyy HHmm, e.g. 18/9/2026 1000";

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d/M/uuuu HHmm")
            .withResolverStyle(ResolverStyle.STRICT);

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
     * Returns true if a given string is a valid visit date.
     */
    public static boolean isValidVisitDate(String test) {
        try {
            LocalDateTime.parse(test, FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
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
