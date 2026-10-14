package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand;

/**
 * Contains tests for {@code SortCommandParser}.
 */
public class SortCommandParserTest {

    private final SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_date_returnsSortCommand() throws Exception {
        assertInstanceOf(SortCommand.class, parser.parse("date"));
        assertInstanceOf(SortCommand.class, parser.parse("  date  "));
    }

    @Test
    public void parse_invalidField_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "name", expectedMessage);
        assertParseFailure(parser, "Date", expectedMessage);
        assertParseFailure(parser, "date name", expectedMessage);
    }
}
