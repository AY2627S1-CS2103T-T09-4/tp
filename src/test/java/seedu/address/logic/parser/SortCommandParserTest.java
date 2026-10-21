package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand;

/**
 * Contains tests for {@code SortCommandParser}.
 */
public class SortCommandParserTest {

    private final SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_date_returnsSortCommand() throws Exception {
        assertEquals(new SortCommand(), parser.parse("date"));
        assertEquals(new SortCommand(), parser.parse("  date  "));
    }

    @Test
    public void parse_dateWithCutoff_returnsSortCommand() throws Exception {
        SortCommand expectedCommand = new SortCommand(LocalDate.of(2026, 9, 18));

        assertEquals(expectedCommand, parser.parse("date 18/9/2026"));
        assertEquals(expectedCommand, parser.parse("  date   18/9/2026  "));
        assertEquals(new SortCommand(LocalDate.of(2024, 2, 29)), parser.parse("date 29/2/2024"));
    }

    @Test
    public void parse_invalidField_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "name", expectedMessage);
        assertParseFailure(parser, "Date", expectedMessage);
        assertParseFailure(parser, "date name", expectedMessage);
        assertParseFailure(parser, "date 18/9/2026 extra", expectedMessage);
    }

    @Test
    public void parse_invalidCutoffDate_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "date 31/2/2026", expectedMessage);
        assertParseFailure(parser, "date 29/2/2025", expectedMessage);
        assertParseFailure(parser, "date 2026-09-18", expectedMessage);
        assertParseFailure(parser, "date 18/9/26", expectedMessage);
        assertParseFailure(parser, "date 18/9/2026 1000", expectedMessage);
        assertParseFailure(parser, "date vd/18/9/2026", expectedMessage);
    }
}
