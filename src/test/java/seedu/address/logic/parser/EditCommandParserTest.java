package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_VISIT_DATE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_VISIT_DATE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_VISIT_DATE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VISIT_DATE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VISIT_DATE_DESC_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VISIT_DATE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.VisitDate;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {

    private static final String TAG_EMPTY = " " + PREFIX_TAG;

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);

    private static final String INDEX_AND_VISIT_DATE = "1" + VISIT_DATE_DESC_AMY;

    private final EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, VALID_NAME_AMY, MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);
        assertParseFailure(parser, "1   ", EditCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // negative index
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // zero index
        assertParseFailure(parser, "0" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // invalid arguments being parsed as preamble
        assertParseFailure(parser, "1 some random string", MESSAGE_INVALID_FORMAT);

        // invalid prefix being parsed as preamble
        assertParseFailure(parser, "1 i/ string", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, INDEX_AND_VISIT_DATE + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, INDEX_AND_VISIT_DATE + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, INDEX_AND_VISIT_DATE + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, INDEX_AND_VISIT_DATE + INVALID_ADDRESS_DESC, Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, INDEX_AND_VISIT_DATE + INVALID_TAG_DESC, Tag.MESSAGE_CONSTRAINTS);

        // invalid phone followed by valid email
        assertParseFailure(parser, INDEX_AND_VISIT_DATE + INVALID_PHONE_DESC + EMAIL_DESC_AMY,
                Phone.MESSAGE_CONSTRAINTS);

        // while parsing {@code PREFIX_TAG} alone will reset the tags of the {@code Person} being edited,
        // parsing it together with a valid tag results in error
        assertParseFailure(parser, INDEX_AND_VISIT_DATE + TAG_DESC_FRIEND + TAG_DESC_HUSBAND + TAG_EMPTY,
                Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, INDEX_AND_VISIT_DATE + TAG_DESC_FRIEND + TAG_EMPTY + TAG_DESC_HUSBAND,
                Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, INDEX_AND_VISIT_DATE + TAG_EMPTY + TAG_DESC_FRIEND + TAG_DESC_HUSBAND,
                Tag.MESSAGE_CONSTRAINTS);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser, INDEX_AND_VISIT_DATE + INVALID_NAME_DESC + INVALID_EMAIL_DESC
                + VALID_ADDRESS_AMY + VALID_PHONE_AMY,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + VISIT_DATE_DESC_AMY
                + PHONE_DESC_BOB + TAG_DESC_HUSBAND + EMAIL_DESC_AMY + ADDRESS_DESC_AMY
                + NAME_DESC_AMY + TAG_DESC_FRIEND;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate(VALID_VISIT_DATE_AMY)
                .withName(VALID_NAME_AMY).withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).withAddress(VALID_ADDRESS_AMY)
                .withTags(VALID_TAG_HUSBAND, VALID_TAG_FRIEND).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + VISIT_DATE_DESC_AMY
                + PHONE_DESC_BOB + EMAIL_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate(VALID_VISIT_DATE_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_oneOptionalFieldSpecified_success() {
        assertOptionalFieldParsed(NAME_DESC_AMY, new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build());
        assertOptionalFieldParsed(PHONE_DESC_AMY, new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build());
        assertOptionalFieldParsed(EMAIL_DESC_AMY, new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build());
        assertOptionalFieldParsed(ADDRESS_DESC_AMY,
                new EditPersonDescriptorBuilder().withAddress(VALID_ADDRESS_AMY).build());
        assertOptionalFieldParsed(TAG_DESC_FRIEND,
                new EditPersonDescriptorBuilder().withTags(VALID_TAG_FRIEND).build());
    }

    @Test
    public void parse_visitDateOnly_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate(VALID_VISIT_DATE_AMY).build();
        assertParseSuccess(parser, INDEX_AND_VISIT_DATE, new EditCommand(INDEX_FIRST_PERSON, descriptor));

        EditPersonDescriptor pastDateDescriptor = new EditPersonDescriptorBuilder()
                .withVisitDate(VALID_VISIT_DATE_BOB).build();
        assertParseSuccess(parser, "1" + VISIT_DATE_DESC_BOB, new EditCommand(INDEX_FIRST_PERSON, pastDateDescriptor));
    }

    @Test
    public void parse_visitDateWhitespace_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate(VALID_VISIT_DATE_AMY).build();
        assertParseSuccess(parser, " 1 vd/  18/09/2026 1000  ", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_allExistingFieldsWithoutVisitDate_success() {
        String userInput = "1" + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY
                + TAG_DESC_FRIEND + TAG_DESC_HUSBAND;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_AMY).withEmail(VALID_EMAIL_AMY).withAddress(VALID_ADDRESS_AMY)
                .withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND).build();

        assertParseSuccess(parser, userInput, new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_invalidVisitDate_failure() {
        String[] invalidDates = {"", "  ", "31/2/2026 1000", "8/10/2026 2500", "8/10/2026", "2026-10-08 1000"};
        for (String invalidDate : invalidDates) {
            assertParseFailure(parser, "1 vd/" + invalidDate, VisitDate.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_repeatedVisitDate_failure() {
        String[] repeatedDates = {VISIT_DATE_DESC_AMY + VISIT_DATE_DESC_BOB,
            VISIT_DATE_DESC_AMY + INVALID_VISIT_DATE_DESC, INVALID_VISIT_DATE_DESC + VISIT_DATE_DESC_AMY,
            VISIT_DATE_DESC_AMY + VISIT_DATE_DESC_AMY};
        for (String repeatedDate : repeatedDates) {
            assertParseFailure(parser, "1" + repeatedDate,
                    Messages.getErrorMessageForDuplicatePrefixes(PREFIX_VISIT_DATE));
        }
    }

    @Test
    public void parse_validationOrder_failure() {
        assertParseFailure(parser, "0" + EMAIL_DESC_AMY + EMAIL_DESC_BOB, MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1" + EMAIL_DESC_AMY + EMAIL_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));
        assertParseFailure(parser, "1" + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_emptyEmail_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withEmail("").build();
        assertParseSuccess(parser, "1 e/", new EditCommand(INDEX_FIRST_PERSON, descriptor));
        descriptor.setVisitDate(new VisitDate(VALID_VISIT_DATE_AMY));
        assertParseSuccess(parser, INDEX_AND_VISIT_DATE + " e/", new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseSuccess(parser, "1 e/" + VISIT_DATE_DESC_AMY, new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        // More extensive testing of duplicate parameter detections is done in
        // AddCommandParserTest#parse_repeatedNonTagValue_failure()

        // valid followed by invalid
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + VISIT_DATE_DESC_AMY
                + INVALID_PHONE_DESC + PHONE_DESC_BOB;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid followed by valid
        userInput = targetIndex.getOneBased() + VISIT_DATE_DESC_AMY
                + PHONE_DESC_BOB + INVALID_PHONE_DESC;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple valid fields repeated
        userInput = targetIndex.getOneBased() + VISIT_DATE_DESC_AMY
                + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY
                + TAG_DESC_FRIEND + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY + TAG_DESC_FRIEND
                + PHONE_DESC_BOB + ADDRESS_DESC_BOB + EMAIL_DESC_BOB + TAG_DESC_HUSBAND;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS));

        // multiple invalid values
        userInput = targetIndex.getOneBased() + VISIT_DATE_DESC_AMY
                + INVALID_PHONE_DESC + INVALID_ADDRESS_DESC + INVALID_EMAIL_DESC
                + INVALID_PHONE_DESC + INVALID_ADDRESS_DESC + INVALID_EMAIL_DESC;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS));
    }

    @Test
    public void parse_resetTags_success() {
        Index targetIndex = INDEX_THIRD_PERSON;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags().build();
        assertParseSuccess(parser, targetIndex.getOneBased() + TAG_EMPTY, new EditCommand(targetIndex, descriptor));

        descriptor.setVisitDate(new VisitDate(VALID_VISIT_DATE_AMY));
        assertParseSuccess(parser, targetIndex.getOneBased() + VISIT_DATE_DESC_AMY + TAG_EMPTY,
                new EditCommand(targetIndex, descriptor));
    }

    /**
     * Checks an optional field both with and without a supplied visit date and time.
     */
    private void assertOptionalFieldParsed(String optionalField, EditPersonDescriptor descriptor) {
        assertParseSuccess(parser, "1" + optionalField, new EditCommand(INDEX_FIRST_PERSON, descriptor));
        descriptor.setVisitDate(new VisitDate(VALID_VISIT_DATE_AMY));
        assertParseSuccess(parser, INDEX_AND_VISIT_DATE + optionalField,
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }
}
