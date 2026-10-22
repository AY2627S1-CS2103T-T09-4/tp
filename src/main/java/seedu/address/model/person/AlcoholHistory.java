package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents optional free-text alcohol history. An empty value means it has not been recorded.
 * Guarantees: immutable; surrounding whitespace is trimmed.
 */
public class AlcoholHistory {

    public static final String MESSAGE_CONSTRAINTS = "Alcohol history must not be blank when supplied.";

    private final String value;

    /**
     * Creates alcohol history, or an unrecorded value when the supplied text is blank.
     *
     * @throws NullPointerException If the supplied text is null.
     */
    public AlcoholHistory(String alcoholHistory) {
        requireNonNull(alcoholHistory);
        value = alcoholHistory.strip();
    }

    /**
     * Returns whether alcohol history has not been recorded.
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
                || (other instanceof AlcoholHistory otherHistory && value.equals(otherHistory.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
