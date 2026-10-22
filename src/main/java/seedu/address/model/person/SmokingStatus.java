package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

/**
 * Represents optional smoking status, keeping unrecorded information distinct from an explicit unknown status.
 */
public enum SmokingStatus {
    NEVER("never"),
    FORMER("former"),
    CURRENT("current"),
    UNKNOWN("unknown"),
    UNRECORDED("");

    public static final String MESSAGE_CONSTRAINTS =
            "Smoking status must be never, former, current, or unknown.";

    private final String value;

    SmokingStatus(String value) {
        this.value = value;
    }

    /**
     * Returns the status after trimming surrounding whitespace and normalizing letter case.
     * Blank text represents an unrecorded value.
     *
     * @throws NullPointerException If the supplied status is null.
     * @throws IllegalArgumentException If the status is neither blank nor one of the supported values.
     */
    public static SmokingStatus fromString(String smokingStatus) {
        requireNonNull(smokingStatus);
        String normalizedStatus = smokingStatus.strip().toLowerCase(Locale.ROOT);
        for (SmokingStatus candidate : values()) {
            if (candidate.value.equals(normalizedStatus)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    /**
     * Returns whether smoking status has not been recorded.
     */
    public boolean isEmpty() {
        return this == UNRECORDED;
    }

    /**
     * Returns the normalized status for display and storage, or an empty string when unrecorded.
     */
    @Override
    public String toString() {
        return value;
    }
}
