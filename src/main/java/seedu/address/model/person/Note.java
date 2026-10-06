package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents a Person's notes in the address book.
 * Guarantees: immutable; can take any value. An empty value represents a person with no notes.
 */
public class Note {

    public final String value;

    /**
     * Constructs a {@code Note}.
     *
     * @param note Any note.
     */
    public Note(String note) {
        requireNonNull(note);
        value = note;
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
        if (!(other instanceof Note otherNote)) {
            return false;
        }

        return value.equals(otherNote.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
