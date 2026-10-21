package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents optional free-text allergy information. An empty value means it has not been recorded.
 * Guarantees: immutable; surrounding whitespace is trimmed.
 */
public class Allergies {

    public static final String MESSAGE_CONSTRAINTS = "Allergies must not be blank when supplied.";

    private final String value;

    /**
     * Creates allergy information, or an unrecorded value when the supplied text is blank.
     *
     * @throws NullPointerException If the supplied text is null.
     */
    public Allergies(String allergies) {
        requireNonNull(allergies);
        value = allergies.strip();
    }

    /**
     * Returns whether allergy information has not been recorded.
     */
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof Allergies otherAllergies && value.equals(otherAllergies.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
