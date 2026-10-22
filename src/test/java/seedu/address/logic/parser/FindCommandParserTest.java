package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.AddressContainsKeywordsPredicate;
import seedu.address.model.person.MatchesAllCriteriaPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.NoteContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.PhoneContainsKeywordsPredicate;
import seedu.address.model.person.TagContainsKeywordsPredicate;
import seedu.address.model.person.VisitDateOnDaysPredicate;

public class FindCommandParserTest {

    private static final String MESSAGE_KEYWORDS_NEED_PREFIX = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            FindCommand.MESSAGE_KEYWORDS_NEED_PREFIX + "\n" + FindCommand.MESSAGE_USAGE);

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_keywordsWithoutPrefix_throwsParseException() {
        assertParseFailure(parser, "Alice Bob", MESSAGE_KEYWORDS_NEED_PREFIX);

        // keywords before a valid prefix are still rejected
        assertParseFailure(parser, "Alice n/Bob", MESSAGE_KEYWORDS_NEED_PREFIX);
    }

    @Test
    public void parse_nameKeywords_returnsFindCommand() {
        FindCommand expectedFindCommand = findCommandOf(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, " n/Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " n/ \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_eachOtherField_returnsFindCommand() {
        assertParseSuccess(parser, " p/9123 4567",
                findCommandOf(new PhoneContainsKeywordsPredicate(List.of("9123", "4567"))));
        assertParseSuccess(parser, " a/clementi",
                findCommandOf(new AddressContainsKeywordsPredicate(List.of("clementi"))));
        assertParseSuccess(parser, " t/diabetic",
                findCommandOf(new TagContainsKeywordsPredicate(List.of("diabetic"))));
    }

    @Test
    public void parse_multipleFields_returnsFindCommandRequiringAll() {
        FindCommand expectedFindCommand = findCommandOf(
                new NameContainsKeywordsPredicate(List.of("alex")),
                new PhoneContainsKeywordsPredicate(List.of("9123")),
                new AddressContainsKeywordsPredicate(List.of("clementi")),
                new TagContainsKeywordsPredicate(List.of("diabetic")));
        assertParseSuccess(parser, " n/alex p/9123 a/clementi t/diabetic", expectedFindCommand);

        // prefixes in a different order produce the same search
        assertParseSuccess(parser, " t/diabetic a/clementi p/9123 n/alex", expectedFindCommand);
    }

    @Test
    public void parse_nonSpaceWhitespaceBetweenFields_keepsEveryField() {
        FindCommand expectedFindCommand = findCommandOf(
                new NameContainsKeywordsPredicate(List.of("Alex")),
                new PhoneContainsKeywordsPredicate(List.of("9123")));

        // a tab, a newline or several spaces must not turn p/9123 into a name keyword
        assertParseSuccess(parser, " n/Alex\tp/9123", expectedFindCommand);
        assertParseSuccess(parser, " n/Alex\np/9123", expectedFindCommand);
        assertParseSuccess(parser, "\tn/Alex \t p/9123", expectedFindCommand);
    }

    @Test
    public void parse_emptyKeywords_throwsParseException() {
        assertParseFailure(parser, " n/", String.format(FindCommandParser.MESSAGE_EMPTY_KEYWORDS, "n/"));
        assertParseFailure(parser, " n/alex a/  ", String.format(FindCommandParser.MESSAGE_EMPTY_KEYWORDS, "a/"));
    }

    @Test
    public void parse_repeatedPrefix_throwsParseException() {
        assertParseFailure(parser, " n/alex n/ben",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NAME));
    }

    @Test
    public void parse_nonDigitPhoneKeyword_throwsParseException() {
        assertParseFailure(parser, " p/9123 abc", FindCommandParser.MESSAGE_INVALID_PHONE_KEYWORD);
        assertParseFailure(parser, " p/+65", FindCommandParser.MESSAGE_INVALID_PHONE_KEYWORD);
    }

    @Test
    public void parse_visitDates_returnsFindCommand() {
        LocalDate visitDay = LocalDate.of(2026, 9, 18);
        assertParseSuccess(parser, " vd/18/9/2026", findCommandOf(new VisitDateOnDaysPredicate(List.of(visitDay))));

        // leading zeros give the same day
        assertParseSuccess(parser, " vd/18/09/2026", findCommandOf(new VisitDateOnDaysPredicate(List.of(visitDay))));

        // several dates match any of them
        assertParseSuccess(parser, " vd/18/9/2026 19/9/2026", findCommandOf(
                new VisitDateOnDaysPredicate(List.of(visitDay, visitDay.plusDays(1)))));

        // combined with another field
        assertParseSuccess(parser, " n/alex vd/18/9/2026", findCommandOf(
                new NameContainsKeywordsPredicate(List.of("alex")),
                new VisitDateOnDaysPredicate(List.of(visitDay))));
    }

    @Test
    public void parse_invalidVisitDate_throwsParseException() {
        // impossible date, wrong format, and a time given with the date
        assertParseFailure(parser, " vd/31/2/2026", String.format(FindCommandParser.MESSAGE_INVALID_VISIT_DAY,
                "31/2/2026"));
        assertParseFailure(parser, " vd/2026-09-18", String.format(FindCommandParser.MESSAGE_INVALID_VISIT_DAY,
                "2026-09-18"));
        assertParseFailure(parser, " vd/18/9/2026 1000", String.format(FindCommandParser.MESSAGE_INVALID_VISIT_DAY,
                "1000"));
    }

    @Test
    public void parse_emptyOrRepeatedVisitDate_throwsParseException() {
        assertParseFailure(parser, " vd/", String.format(FindCommandParser.MESSAGE_EMPTY_KEYWORDS, "vd/"));
        assertParseFailure(parser, " vd/18/9/2026 vd/19/9/2026",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_VISIT_DATE));
    }

    @Test
    public void parse_noteKeywords_returnsFindCommand() {
        assertParseSuccess(parser, " note/insulin wound",
                findCommandOf(new NoteContainsKeywordsPredicate(List.of("insulin", "wound"))));

        // n/ is not mistaken for the start of note/
        assertParseSuccess(parser, " n/alex note/insulin", findCommandOf(
                new NameContainsKeywordsPredicate(List.of("alex")),
                new NoteContainsKeywordsPredicate(List.of("insulin"))));
    }

    @Test
    public void parse_emptyOrRepeatedNote_throwsParseException() {
        assertParseFailure(parser, " note/", String.format(FindCommandParser.MESSAGE_EMPTY_KEYWORDS, "note/"));
        assertParseFailure(parser, " note/insulin note/wound",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NOTE));
    }

    @SafeVarargs
    private static FindCommand findCommandOf(Predicate<Person>... criteria) {
        return new FindCommand(new MatchesAllCriteriaPredicate(List.of(criteria)));
    }
}
