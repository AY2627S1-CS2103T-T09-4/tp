package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.AlcoholHistory;
import seedu.address.model.person.Allergies;
import seedu.address.model.person.BloodType;
import seedu.address.model.person.DateOfBirth;
import seedu.address.model.person.Email;
import seedu.address.model.person.EmergencyContact;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.SmokingStatus;
import seedu.address.model.person.VisitDate;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";
    public static final String MESSAGE_INDEX_TOO_LARGE =
            "Index is too large. Enter an index shown in the displayed patient list.";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Returns whether the input is a positive decimal integer, regardless of its size.
     * Uses the same digit interpretation as Integer.parseInt without converting the numeric value.
     */
    static boolean isPositiveInteger(String argument) {
        return argument.chars().allMatch(character -> Character.digit(character, 10) >= 0)
                && argument.chars().anyMatch(character -> Character.digit(character, 10) > 0);
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a supplied date of birth, trimming surrounding whitespace and rejecting empty input.
     *
     * @throws ParseException If the date is empty, invalid, or in the future.
     */
    public static DateOfBirth parseDateOfBirth(String dateOfBirth) throws ParseException {
        requireNonNull(dateOfBirth);
        String trimmedDate = dateOfBirth.trim();
        if (trimmedDate.isEmpty() || !DateOfBirth.isValidDateOfBirth(trimmedDate)) {
            throw new ParseException(DateOfBirth.MESSAGE_CONSTRAINTS);
        }
        return new DateOfBirth(trimmedDate);
    }

    /**
     * Parses recorded allergy information, trimming surrounding whitespace and rejecting blank input.
     *
     * @throws ParseException If the supplied allergy information is blank.
     * @throws NullPointerException If the supplied allergy information is null.
     */
    public static Allergies parseAllergies(String allergies) throws ParseException {
        Allergies parsedAllergies = new Allergies(allergies);
        if (parsedAllergies.isEmpty()) {
            throw new ParseException(Allergies.MESSAGE_CONSTRAINTS);
        }
        return parsedAllergies;
    }

    /**
     * Parses recorded alcohol history, trimming surrounding whitespace and rejecting blank input.
     *
     * @throws ParseException If the supplied history is blank.
     * @throws NullPointerException If the supplied history is null.
     */
    public static AlcoholHistory parseAlcoholHistory(String alcoholHistory) throws ParseException {
        AlcoholHistory parsedHistory = new AlcoholHistory(alcoholHistory);
        if (parsedHistory.isEmpty()) {
            throw new ParseException(AlcoholHistory.MESSAGE_CONSTRAINTS);
        }
        return parsedHistory;
    }

    /**
     * Parses a recorded smoking status, rejecting blank or unsupported values.
     *
     * @throws ParseException If the supplied status is blank or unsupported.
     * @throws NullPointerException If the supplied status is null.
     */
    public static SmokingStatus parseSmokingStatus(String smokingStatus) throws ParseException {
        try {
            SmokingStatus parsedStatus = SmokingStatus.fromString(smokingStatus);
            if (parsedStatus.isEmpty()) {
                throw new ParseException(SmokingStatus.MESSAGE_CONSTRAINTS);
            }
            return parsedStatus;
        } catch (IllegalArgumentException exception) {
            throw new ParseException(SmokingStatus.MESSAGE_CONSTRAINTS, exception);
        }
    }

    /**
     * Parses a recorded blood type, rejecting empty input.
     *
     * @throws ParseException If the supplied blood type is empty or invalid.
     */
    public static BloodType parseBloodType(String bloodType) throws ParseException {
        requireNonNull(bloodType);
        try {
            BloodType parsedBloodType = BloodType.fromString(bloodType);
            if (parsedBloodType.isEmpty()) {
                throw new ParseException(BloodType.MESSAGE_CONSTRAINTS);
            }
            return parsedBloodType;
        } catch (IllegalArgumentException exception) {
            throw new ParseException(BloodType.MESSAGE_CONSTRAINTS, exception);
        }
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String visitDate} into a {@code VisitDate}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code visitDate} is invalid.
     */
    public static VisitDate parseVisitDate(String visitDate) throws ParseException {
        requireNonNull(visitDate);
        String trimmedVisitDate = visitDate.trim();
        if (!VisitDate.isValidVisitDate(trimmedVisitDate)) {
            throw new ParseException(VisitDate.MESSAGE_CONSTRAINTS);
        }
        return new VisitDate(trimmedVisitDate);
    }

    /**
     * Parses a supplied emergency contact, rejecting blank or incomplete details.
     *
     * @throws ParseException If the contact lacks a valid phone number or supplies an invalid name.
     */
    public static EmergencyContact parseEmergencyContact(String emergencyContact) throws ParseException {
        requireNonNull(emergencyContact);
        String trimmedContact = emergencyContact.trim();
        if (trimmedContact.isEmpty() || !EmergencyContact.isValidEmergencyContact(trimmedContact)) {
            throw new ParseException(EmergencyContact.MESSAGE_CONSTRAINTS);
        }
        return new EmergencyContact(trimmedContact);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }
}
