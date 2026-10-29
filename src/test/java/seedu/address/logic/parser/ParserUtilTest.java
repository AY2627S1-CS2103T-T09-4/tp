package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INDEX_TOO_LARGE;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.VisitDate;
import seedu.address.model.tag.Tag;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "911a";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";
    private static final String INVALID_VISIT_DATE = "31/2/2026 1000";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "123456";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_VISIT_DATE = "18/9/2026 1000";
    private static final String VALID_TAG_1 = "friend";
    private static final String VALID_TAG_2 = "neighbour";

    private static final String WHITESPACE = " \t\r\n";
    private static final String COMMAND_USAGE = "command: Example usage";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseIndexWithUsage_validInput_success() throws Exception {
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex(" 1 ", COMMAND_USAGE));
    }

    @Test
    public void parseIndexWithUsage_missingIndex_throwsInvalidFormat() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, COMMAND_USAGE);
        for (String index : List.of("", WHITESPACE)) {
            assertThrows(ParseException.class, expectedMessage, () -> ParserUtil.parseIndex(index, COMMAND_USAGE));
        }
    }

    @Test
    public void parseIndexWithUsage_nonPositiveOrNonNumericIndex_throwsInvalidIndex() {
        String expectedMessage = MESSAGE_INVALID_INDEX + "\n" + COMMAND_USAGE;
        for (String index : List.of("0", "-1", "+1", "abc", "1.5", "0 1", "0".repeat(20))) {
            assertThrows(ParseException.class, expectedMessage, () -> ParserUtil.parseIndex(index, COMMAND_USAGE));
        }
    }

    @Test
    public void parseIndexWithUsage_validIndexWithExtraArguments_throwsUnexpectedArguments() {
        String expectedMessage = ParserUtil.MESSAGE_EXTRA_INDEX_ARGUMENTS + "\n" + COMMAND_USAGE;
        for (String index : List.of("1 abc", "1 2", "  3   extra words  ")) {
            assertThrows(ParseException.class, expectedMessage, () -> ParserUtil.parseIndex(index, COMMAND_USAGE));
        }

        // an oversized first word is still reported as too large
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INDEX_TOO_LARGE + "\n" + COMMAND_USAGE, () ->
                ParserUtil.parseIndex("2147483648 2", COMMAND_USAGE));
    }

    @Test
    public void parseIndexWithUsage_oversizedIndex_throwsIndexTooLarge() {
        // Boundary: 2147483648 is the first value above Integer.MAX_VALUE.
        String expectedMessage = MESSAGE_INDEX_TOO_LARGE + "\n" + COMMAND_USAGE;
        for (String index : List.of("2147483648", "99999999999", "0002147483648")) {
            assertThrows(ParseException.class, expectedMessage, () -> ParserUtil.parseIndex(index, COMMAND_USAGE));
        }
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));

        // the phone prefix is mentioned only for a name that contains it
        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseName("!!!"));
        assertThrows(ParseException.class, Name.MESSAGE_CONTAINS_PHONE_PREFIX, () -> ParserUtil.parseName("Ap/B"));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        String addressWithWhitespace = WHITESPACE + VALID_ADDRESS + WHITESPACE;
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseVisitDate_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseVisitDate((String) null));
    }

    @Test
    public void parseVisitDate_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseVisitDate(INVALID_VISIT_DATE));
    }

    @Test
    public void parseVisitDate_validValueWithoutWhitespace_returnsVisitDate() throws Exception {
        VisitDate expectedVisitDate = new VisitDate(VALID_VISIT_DATE);
        assertEquals(expectedVisitDate, ParserUtil.parseVisitDate(VALID_VISIT_DATE));
    }

    @Test
    public void parseVisitDate_validValueWithWhitespace_returnsTrimmedVisitDate() throws Exception {
        String visitDateWithWhitespace = WHITESPACE + VALID_VISIT_DATE + WHITESPACE;
        VisitDate expectedVisitDate = new VisitDate(VALID_VISIT_DATE);
        assertEquals(expectedVisitDate, ParserUtil.parseVisitDate(visitDateWithWhitespace));
    }

    @Test
    public void parseVisitDate_repeatedWhitespaceBetweenDateAndTime_returnsVisitDate() throws Exception {
        VisitDate expectedVisitDate = new VisitDate(VALID_VISIT_DATE);
        for (String visitDate : new String[] {"18/9/2026  1000", "18/9/2026\t1000", " 18/9/2026 \t  1000 "}) {
            assertEquals(expectedVisitDate, ParserUtil.parseVisitDate(visitDate));
        }
    }

    @Test
    public void parseVisitDate_whitespaceInsideDate_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseVisitDate("18/ 9/2026 1000"));
        assertThrows(ParseException.class, () -> ParserUtil.parseVisitDate("18/9/2026 10 00"));
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        String tagWithWhitespace = WHITESPACE + VALID_TAG_1 + WHITESPACE;
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(tagWithWhitespace));
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));

        assertEquals(expectedTagSet, actualTagSet);
    }

    @Test
    public void parseTags_caseVariants_keepsFirstSpellingOnce() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of("FollowUp", "followup", "FOLLOWUP"));

        assertEquals(1, actualTagSet.size());
        assertEquals("FollowUp", actualTagSet.iterator().next().tagName);
    }
}
