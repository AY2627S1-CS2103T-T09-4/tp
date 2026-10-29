package seedu.address.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a Tag in the address book.
 * Tags that differ only in letter case are equal, so a patient cannot hold both {@code followup} and
 * {@code FOLLOWUP}. The spelling given when the tag was created is kept for display.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 */
public class Tag {

    public static final String MESSAGE_CONSTRAINTS = "Tag names should be alphanumeric";
    public static final String VALIDATION_REGEX = "\\p{Alnum}+";

    public final String tagName;

    /** Lower-case form of {@code tagName}, used so that equality and hashing ignore letter case. */
    private final String caseInsensitiveName;

    /**
     * Constructs a {@code Tag}.
     *
     * @param tagName A valid tag name.
     */
    public Tag(String tagName) {
        requireNonNull(tagName);
        checkArgument(isValidTagName(tagName), MESSAGE_CONSTRAINTS);
        this.tagName = tagName;
        this.caseInsensitiveName = tagName.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns true if a given string is a valid tag name.
     */
    public static boolean isValidTagName(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns true if {@code other} is a tag with the same name, ignoring letter case.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Tag otherTag)) {
            return false;
        }

        return caseInsensitiveName.equals(otherTag.caseInsensitiveName);
    }

    @Override
    public int hashCode() {
        return caseInsensitiveName.hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
