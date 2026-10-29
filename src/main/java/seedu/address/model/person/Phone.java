package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers should contain at least 3 digits and be at most 30 characters long. They may start with "
                    + "'+' and may use single spaces or dashes between groups of digits, e.g. +65 9123-4567";
    /**
     * An optional leading '+', then groups of digits joined by a single space or dash, so that formats such as
     * {@code +65 9123-4567} are accepted while {@code ------123} or {@code 123+456} are not.
     */
    public static final String VALIDATION_REGEX = "\\+?\\d+([ -]\\d+)*";

    private static final int MINIMUM_DIGIT_COUNT = 3;
    private static final int MAX_PHONE_LENGTH = 30;
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone;
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        String digitOnlyPhone = test.replaceAll("[^\\d]", "");
        return test.length() <= MAX_PHONE_LENGTH
                && test.matches(VALIDATION_REGEX)
                && digitOnlyPhone.length() >= MINIMUM_DIGIT_COUNT;
    }

    /**
     * Returns true if {@code other} is the same number written with the same digits, ignoring spaces, dashes, and
     * '+', e.g. {@code 8123 4567} and {@code 8123-4567}. Numbers with and without a country code, such as
     * {@code +65 8123 4567} and {@code 81234567}, have different digits and are not the same number.
     * Unlike {@link #equals(Object)}, this ignores formatting, so it is used to identify the same patient.
     */
    public boolean isSameNumber(Phone other) {
        requireNonNull(other);
        return digitsOf(value).equals(digitsOf(other.value));
    }

    /**
     * Returns {@code text} with every character except the digits 0 to 9 removed.
     */
    private static String digitsOf(String text) {
        return text.replaceAll("[^0-9]", "");
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
