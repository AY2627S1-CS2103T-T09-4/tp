package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

/**
 * Represents one of the eight blood types, or an unrecorded value.
 */
public enum BloodType {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-"),
    UNKNOWN("");

    public static final String MESSAGE_CONSTRAINTS =
            "Blood type must be A+, A-, B+, B-, AB+, AB-, O+, or O-.";

    private final String displayValue;

    BloodType(String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * Returns the blood type after trimming whitespace and normalizing letter case.
     * An empty value means the blood type has not been recorded.
     *
     * @throws IllegalArgumentException If the value is neither empty nor one of the eight blood types.
     */
    public static BloodType fromString(String bloodType) {
        requireNonNull(bloodType);
        String normalizedBloodType = bloodType.trim().toUpperCase(Locale.ROOT);
        for (BloodType candidate : values()) {
            if (candidate.displayValue.equals(normalizedBloodType)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    /**
     * Returns whether the blood type has not been recorded.
     */
    public boolean isEmpty() {
        return this == UNKNOWN;
    }

    /**
     * Returns the blood type for display and storage, or an empty string when unrecorded.
     */
    @Override
    public String toString() {
        return displayValue;
    }
}
