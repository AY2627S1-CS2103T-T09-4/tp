package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ViewCommand;

/**
 * Contains tests for {@code ViewCommandParser}.
 */
public class ViewCommandParserTest {

    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validIndex_returnsViewCommand() {
        assertParseSuccess(parser, "1", new ViewCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_missingIndex_showsInvalidFormat() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, " \t ", expectedMessage);
    }

    @Test
    public void parse_invalidIndex_showsReasonAndViewUsage() {
        String expectedMessage = ParserUtil.MESSAGE_INVALID_INDEX + "\n" + ViewCommand.MESSAGE_USAGE;

        for (String index : List.of("0", "-1", "+1", "abc", "1.5", "abc 1")) {
            assertParseFailure(parser, index, expectedMessage);
        }
    }

    @Test
    public void parse_validIndexWithExtraArguments_showsUnexpectedArguments() {
        String expectedMessage = ParserUtil.MESSAGE_EXTRA_INDEX_ARGUMENTS + "\n" + ViewCommand.MESSAGE_USAGE;

        for (String args : List.of("1 abc", "1 2", " 1   2 ", "1\t2")) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }

    @Test
    public void parse_oversizedIndex_showsReasonAndViewUsage() {
        String expectedMessage = ParserUtil.MESSAGE_INDEX_TOO_LARGE + "\n" + ViewCommand.MESSAGE_USAGE;

        assertParseFailure(parser, "2147483648", expectedMessage);
        assertParseFailure(parser, "99999999999", expectedMessage);
    }
}
