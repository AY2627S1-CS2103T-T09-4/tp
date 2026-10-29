package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS = "Names should contain only letters, numbers, spaces, and the "
            + "punctuation . ' - /, and should not be blank.";
    public static final String MESSAGE_CONTAINS_PHONE_PREFIX = "Names cannot contain " + Name.PHONE_PREFIX
            + ", which starts the phone number. Check that there is a space before " + Name.PHONE_PREFIX + ".";

    /** Phone prefix that a name cannot contain, since names may otherwise use '/' as in "s/o". */
    private static final String PHONE_PREFIX = "p/";

    /*
     * The first character of the name must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    private static final String NAME_PART_REGEX = "[\\p{L}\\p{M}\\p{N}]+(?:[.'/-][\\p{L}\\p{M}\\p{N}]+)*\\.?";

    public static final String VALIDATION_REGEX = "(?!.*p/)" + NAME_PART_REGEX + "(?: +" + NAME_PART_REGEX + ")* *";

    private static final String SPACE_SEQUENCE_REGEX = " +";
    private static final String NORMALIZED_SPACE = " ";

    public final String fullName;
    private final String normalizedName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), getConstraintMessage(name));
        fullName = name;
        normalizedName = normalize(name);
    }

    /**
     * Returns the reason {@code invalidName} is not a valid name, mentioning the phone prefix only when the name
     * contains it, so that other mistakes are not explained as a phone-prefix problem.
     */
    public static String getConstraintMessage(String invalidName) {
        requireNonNull(invalidName);
        return invalidName.contains(PHONE_PREFIX) ? MESSAGE_CONTAINS_PHONE_PREFIX : MESSAGE_CONSTRAINTS;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns true if both names match after trimming surrounding whitespace, collapsing repeated spaces,
     * and converting letters to lowercase without using a locale-specific rule.
     */
    public boolean isSameName(Name otherName) {
        return otherName != null && normalizedName.equals(otherName.normalizedName);
    }

    /**
     * Returns the normalized form used only for duplicate-name comparison.
     */
    private static String normalize(String name) {
        return name.trim()
                .replaceAll(SPACE_SEQUENCE_REGEX, NORMALIZED_SPACE)
                .toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
