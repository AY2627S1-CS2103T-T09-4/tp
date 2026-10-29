package seedu.address.logic;

import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Container for user visible messages.
 */
public class Messages {

    /**
     * Locale for numbers in user-visible messages. The interface is in English, so counts are always shown with
     * the digits 0 to 9, whatever the computer's language setting.
     */
    public static final Locale DISPLAY_LOCALE = Locale.ENGLISH;

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX =
            "The patient index provided is invalid. Enter an index shown in the displayed patient list.";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     * Lists the name, phone, address, and visit date, followed by every optional field that is recorded.
     * Unrecorded fields are left out, and tags are listed alphabetically, ignoring case.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName());
        appendField(builder, "Phone", person.getPhone().value);
        appendField(builder, "Email", person.getEmail().value);
        appendField(builder, "Address", person.getAddress().value);
        appendField(builder, "Visit date", person.getVisitDate().toString());
        appendField(builder, "Date of birth", person.getDateOfBirth().toDisplayString());
        appendField(builder, "Blood type", person.getBloodType().toString());
        appendField(builder, "Allergies", person.getAllergies().toString());
        appendField(builder, "Alcohol history", person.getAlcoholHistory().toString());
        appendField(builder, "Smoking status", person.getSmokingStatus().toString());
        appendField(builder, "Emergency contact", person.getEmergencyContact().toDisplayString());
        appendField(builder, "Note", person.getNote().value);
        appendField(builder, "Tags", person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName, String.CASE_INSENSITIVE_ORDER))
                .map(Tag::toString)
                .collect(Collectors.joining()));
        return builder.toString();
    }

    /**
     * Appends {@code "; LABEL: VALUE"} to {@code builder}, or nothing if {@code value} is empty (unrecorded).
     */
    private static void appendField(StringBuilder builder, String label, String value) {
        if (!value.isEmpty()) {
            builder.append("; ").append(label).append(": ").append(value);
        }
    }

}
