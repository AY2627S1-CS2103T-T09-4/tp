package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALCOHOL_HISTORY;
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

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.AlcoholHistory;
import seedu.address.model.person.BloodType;
import seedu.address.model.person.DateOfBirth;
import seedu.address.model.person.EmergencyContact;
import seedu.address.model.person.Note;
import seedu.address.model.person.SmokingStatus;
import seedu.address.model.tag.Tag;

/**
 * Parses optional replacement fields into an EditCommand.
 */
public class EditCommandParser implements Parser<EditCommand> {

    /**
     * Parses the given arguments into an edit command with at least one replacement field.
     *
     * @throws ParseException If the index, prefixes, or field values are invalid, or no field is supplied.
     */
    @Override
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_DATE_OF_BIRTH,
                PREFIX_BLOOD_TYPE, PREFIX_ALCOHOL_HISTORY, PREFIX_SMOKING_STATUS, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_ADDRESS, PREFIX_VISIT_DATE, PREFIX_NOTE, PREFIX_EMERGENCY_CONTACT, PREFIX_TAG);
        Index index = parseIndex(argMultimap.getPreamble());

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_DATE_OF_BIRTH, PREFIX_BLOOD_TYPE,
                PREFIX_ALCOHOL_HISTORY, PREFIX_SMOKING_STATUS, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                PREFIX_VISIT_DATE, PREFIX_NOTE, PREFIX_EMERGENCY_CONTACT);
        EditPersonDescriptor editPersonDescriptor = parsePersonDescriptor(argMultimap);
        if (!editPersonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCommand(index, editPersonDescriptor);
    }

    /**
     * Parses the displayed index and includes edit usage when the index is invalid.
     */
    private Index parseIndex(String preamble) throws ParseException {
        try {
            return ParserUtil.parseIndex(preamble);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE), pe);
        }
    }

    /**
     * Parses the supplied fields into a descriptor after duplicate prefixes have been checked.
     */
    private EditPersonDescriptor parsePersonDescriptor(ArgumentMultimap argMultimap) throws ParseException {
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            editPersonDescriptor.setName(ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_DATE_OF_BIRTH).isPresent()) {
            String dateOfBirth = argMultimap.getValue(PREFIX_DATE_OF_BIRTH).get();
            editPersonDescriptor.setDateOfBirth(dateOfBirth.trim().isEmpty()
                    ? new DateOfBirth("") : ParserUtil.parseDateOfBirth(dateOfBirth));
        }
        if (argMultimap.getValue(PREFIX_BLOOD_TYPE).isPresent()) {
            String bloodType = argMultimap.getValue(PREFIX_BLOOD_TYPE).get().trim();
            editPersonDescriptor.setBloodType(bloodType.isEmpty()
                    ? BloodType.UNKNOWN : ParserUtil.parseBloodType(bloodType));
        }
        if (argMultimap.getValue(PREFIX_ALCOHOL_HISTORY).isPresent()) {
            editPersonDescriptor.setAlcoholHistory(
                    new AlcoholHistory(argMultimap.getValue(PREFIX_ALCOHOL_HISTORY).get()));
        }
        if (argMultimap.getValue(PREFIX_SMOKING_STATUS).isPresent()) {
            String smokingStatus = argMultimap.getValue(PREFIX_SMOKING_STATUS).get();
            editPersonDescriptor.setSmokingStatus(smokingStatus.isBlank()
                    ? SmokingStatus.UNRECORDED : ParserUtil.parseSmokingStatus(smokingStatus));
        }
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            editPersonDescriptor.setPhone(ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get()));
        }
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            editPersonDescriptor.setEmail(ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get()));
        }
        if (argMultimap.getValue(PREFIX_ADDRESS).isPresent()) {
            editPersonDescriptor.setAddress(ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get()));
        }
        if (argMultimap.getValue(PREFIX_VISIT_DATE).isPresent()) {
            editPersonDescriptor.setVisitDate(ParserUtil.parseVisitDate(
                    argMultimap.getValue(PREFIX_VISIT_DATE).get()));
        }
        if (argMultimap.getValue(PREFIX_NOTE).isPresent()) {
            editPersonDescriptor.setNote(new Note(argMultimap.getValue(PREFIX_NOTE).get()));
        }
        if (argMultimap.getValue(PREFIX_EMERGENCY_CONTACT).isPresent()) {
            String emergencyContact = argMultimap.getValue(PREFIX_EMERGENCY_CONTACT).get().trim();
            editPersonDescriptor.setEmergencyContact(emergencyContact.isEmpty()
                    ? new EmergencyContact("") : ParserUtil.parseEmergencyContact(emergencyContact));
        }
        parseTagsForEdit(argMultimap.getAllValues(PREFIX_TAG)).ifPresent(editPersonDescriptor::setTags);

        return editPersonDescriptor;
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>} if {@code tags} is non-empty.
     * If {@code tags} contains only one element which is an empty string, it will be parsed into a
     * {@code Set<Tag>} containing zero tags.
     */
    private Optional<Set<Tag>> parseTagsForEdit(Collection<String> tags) throws ParseException {
        assert tags != null;

        if (tags.isEmpty()) {
            return Optional.empty();
        }
        Collection<String> tagSet = tags.size() == 1 && tags.contains("") ? Collections.emptySet() : tags;
        return Optional.of(ParserUtil.parseTags(tagSet));
    }

}
