package seedu.address.testutil;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALCOHOL_HISTORY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALLERGIES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BLOOD_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE_OF_BIRTH;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMERGENCY_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SMOKING_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VISIT_DATE;

import java.util.Set;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * A utility class for Person.
 */
public class PersonUtil {

    /**
     * Returns an add command string for adding the {@code person}.
     */
    public static String getAddCommand(Person person) {
        return AddCommand.COMMAND_WORD + " " + getPersonDetails(person);
    }

    /**
     * Returns the part of command string for the given {@code person}'s details.
     */
    public static String getPersonDetails(Person person) {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX_NAME + person.getName().fullName + " ");
        if (!person.getDateOfBirth().isEmpty()) {
            sb.append(PREFIX_DATE_OF_BIRTH).append(person.getDateOfBirth()).append(" ");
        }
        if (!person.getBloodType().isEmpty()) {
            sb.append(PREFIX_BLOOD_TYPE).append(person.getBloodType()).append(" ");
        }
        if (!person.getAllergies().isEmpty()) {
            sb.append(PREFIX_ALLERGIES).append(person.getAllergies()).append(" ");
        }
        if (!person.getAlcoholHistory().isEmpty()) {
            sb.append(PREFIX_ALCOHOL_HISTORY).append(person.getAlcoholHistory()).append(" ");
        }
        if (!person.getSmokingStatus().isEmpty()) {
            sb.append(PREFIX_SMOKING_STATUS).append(person.getSmokingStatus()).append(" ");
        }
        sb.append(PREFIX_PHONE + person.getPhone().value + " ");
        if (!person.getEmergencyContact().isEmpty()) {
            sb.append(PREFIX_EMERGENCY_CONTACT).append(person.getEmergencyContact()).append(" ");
        }
        sb.append(PREFIX_EMAIL + person.getEmail().value + " ");
        sb.append(PREFIX_ADDRESS + person.getAddress().value + " ");
        sb.append(PREFIX_VISIT_DATE + person.getVisitDate().toString() + " ");
        person.getTags().stream().forEach(
            s -> sb.append(PREFIX_TAG + s.tagName + " ")
        );
        return sb.toString();
    }

    /**
     * Returns the part of command string for the given {@code EditPersonDescriptor}'s details.
     */
    public static String getEditPersonDescriptorDetails(EditPersonDescriptor descriptor) {
        StringBuilder sb = new StringBuilder();
        descriptor.getName().ifPresent(name -> sb.append(PREFIX_NAME).append(name.fullName).append(" "));
        descriptor.getDateOfBirth().ifPresent(dateOfBirth ->
                sb.append(PREFIX_DATE_OF_BIRTH).append(dateOfBirth).append(" "));
        descriptor.getBloodType().ifPresent(bloodType ->
                sb.append(PREFIX_BLOOD_TYPE).append(bloodType).append(" "));
        descriptor.getAllergies().ifPresent(allergies ->
                sb.append(PREFIX_ALLERGIES).append(allergies).append(" "));
        descriptor.getAlcoholHistory().ifPresent(alcoholHistory ->
                sb.append(PREFIX_ALCOHOL_HISTORY).append(alcoholHistory).append(" "));
        descriptor.getSmokingStatus().ifPresent(smokingStatus ->
                sb.append(PREFIX_SMOKING_STATUS).append(smokingStatus).append(" "));
        descriptor.getPhone().ifPresent(phone -> sb.append(PREFIX_PHONE).append(phone.value).append(" "));
        descriptor.getEmergencyContact().ifPresent(emergencyContact ->
                sb.append(PREFIX_EMERGENCY_CONTACT).append(emergencyContact).append(" "));
        descriptor.getAddress().ifPresent(address -> sb.append(PREFIX_ADDRESS).append(address.value).append(" "));
        descriptor.getVisitDate().ifPresent(visitDate ->
                sb.append(PREFIX_VISIT_DATE).append(visitDate).append(" "));
        descriptor.getEmail().ifPresent(email -> sb.append(PREFIX_EMAIL).append(email.value).append(" "));
        descriptor.getNote().ifPresent(note -> sb.append(PREFIX_NOTE).append(note.value).append(" "));
        if (descriptor.getTags().isPresent()) {
            Set<Tag> tags = descriptor.getTags().get();
            if (tags.isEmpty()) {
                sb.append(PREFIX_TAG);
            } else {
                tags.forEach(s -> sb.append(PREFIX_TAG).append(s.tagName).append(" "));
            }
        }
        return sb.toString();
    }
}
