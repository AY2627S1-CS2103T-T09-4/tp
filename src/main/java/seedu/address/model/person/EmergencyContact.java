package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents an immutable emergency contact. An empty value represents no emergency contact.
 * Nonempty values contain a valid phone number, optionally preceded by a name and comma.
 */
public class EmergencyContact {

    public static final String MESSAGE_CONSTRAINTS =
            "Emergency contacts must use ec/PHONE_NUMBER or ec/NAME, PHONE_NUMBER. "
                    + "The phone number is required; the name is optional. "
                    + Name.MESSAGE_CONSTRAINTS + ". " + Phone.MESSAGE_CONSTRAINTS + ".";

    private final String value;

    /**
     * Constructs a contact, trimming surrounding whitespace from each detail.
     * An empty string represents an absent contact in the model and saved records.
     *
     * @throws NullPointerException If emergencyContact is null.
     * @throws IllegalArgumentException If a nonempty contact is incomplete or invalid.
     */
    public EmergencyContact(String emergencyContact) {
        requireNonNull(emergencyContact);
        checkArgument(isValidEmergencyContact(emergencyContact), MESSAGE_CONSTRAINTS);
        if (emergencyContact.isEmpty()) {
            value = "";
        } else if (!emergencyContact.contains(",")) {
            value = emergencyContact.trim();
        } else {
            String[] details = emergencyContact.split(",", -1);
            value = details[0].trim() + ", " + details[1].trim();
        }
    }

    /**
     * Returns whether the value is empty or contains one valid phone number with an optional name.
     */
    public static boolean isValidEmergencyContact(String emergencyContact) {
        requireNonNull(emergencyContact);
        if (emergencyContact.isEmpty()) {
            return true;
        }
        String[] details = emergencyContact.split(",", -1);
        if (details.length == 1) {
            return Phone.isValidPhone(details[0].trim());
        }
        return details.length == 2
                && Name.isValidName(details[0].trim())
                && Phone.isValidPhone(details[1].trim());
    }

    /**
     * Returns whether this patient has no emergency contact.
     */
    public boolean isEmpty() {
        return value.isEmpty();
    }

    /**
     * Returns the phone number, followed by the name in parentheses when supplied, or empty if absent.
     */
    public String toDisplayString() {
        if (!value.contains(",")) {
            return value;
        }
        String[] details = value.split(", ", -1);
        return details[1] + " (" + details[0] + ")";
    }

    /**
     * Returns the phone number with any supplied name, or an empty string for an absent contact.
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Returns whether both emergency contacts contain the same details.
     */
    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof EmergencyContact otherContact && value.equals(otherContact.value));
    }

    /**
     * Returns a hash code based on the contact details.
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
