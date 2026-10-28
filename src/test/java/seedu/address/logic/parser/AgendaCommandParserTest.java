package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AgendaCommand;

public class AgendaCommandParserTest {

    private static final String EXPECTED_ERROR =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, AgendaCommand.MESSAGE_USAGE);

    private final AgendaCommandParser parser = new AgendaCommandParser();

    @Test
    public void parse_noArguments_throwsParseException() {
        assertParseFailure(parser, "", EXPECTED_ERROR);
        assertParseFailure(parser, "  \t  ", EXPECTED_ERROR);
    }

    @Test
    public void parse_startDate_returnsSevenDayAgenda() throws Exception {
        AgendaCommand expectedCommand = new AgendaCommand(LocalDate.of(2026, 10, 27));

        assertEquals(expectedCommand, parser.parse("27/10/2026"));
        assertEquals(expectedCommand, parser.parse("  27/10/2026  "));
        assertEquals(new AgendaCommand(LocalDate.of(2024, 2, 29)), parser.parse("29/2/2024"));
    }

    @Test
    public void parse_startDateAndDays_returnsConfiguredAgenda() throws Exception {
        AgendaCommand expectedCommand = new AgendaCommand(LocalDate.of(2026, 10, 27), 14);

        assertEquals(expectedCommand, parser.parse("27/10/2026 14"));
        assertEquals(expectedCommand, parser.parse("  27/10/2026   14  "));
        assertEquals(new AgendaCommand(LocalDate.of(2026, 10, 27), AgendaCommand.MAX_DAYS),
                parser.parse("27/10/2026 " + AgendaCommand.MAX_DAYS));
    }

    @Test
    public void parse_invalidDate_throwsParseException() {
        assertParseFailure(parser, "31/2/2026", EXPECTED_ERROR);
        assertParseFailure(parser, "29/2/2025", EXPECTED_ERROR);
        assertParseFailure(parser, "2026-10-27", EXPECTED_ERROR);
        assertParseFailure(parser, "27/10/26", EXPECTED_ERROR);
    }

    @Test
    public void parse_overflowingDateRange_throwsParseException() {
        String expectedError = AgendaCommand.MESSAGE_INVALID_DATE_RANGE + "\n" + AgendaCommand.MESSAGE_USAGE;

        assertParseFailure(parser, "2/1/9999 365", expectedError);
        assertParseFailure(parser, "26/12/9999", expectedError);
        assertParseFailure(parser, "28/10/9999 365", expectedError);
        assertParseFailure(parser, "31/12/9999 2", expectedError);
        assertParseFailure(parser, "31/12/+999999999 1", expectedError);
    }

    @Test
    public void parse_rangeEndingOnLastSupportedDate_returnsAgenda() throws Exception {
        assertEquals(new AgendaCommand(LocalDate.of(9999, 1, 1), 365), parser.parse("1/1/9999 365"));
        assertEquals(new AgendaCommand(LocalDate.of(9999, 12, 25)), parser.parse("25/12/9999"));
        assertEquals(new AgendaCommand(LocalDate.of(9999, 12, 31), 1), parser.parse("31/12/9999 1"));
    }

    @Test
    public void parse_invalidDays_throwsParseException() {
        assertParseFailure(parser, "27/10/2026 0", EXPECTED_ERROR);
        assertParseFailure(parser, "27/10/2026 -1", EXPECTED_ERROR);
        assertParseFailure(parser, "27/10/2026 1.5", EXPECTED_ERROR);
        assertParseFailure(parser, "27/10/2026 seven", EXPECTED_ERROR);
        assertParseFailure(parser, "27/10/2026 " + (AgendaCommand.MAX_DAYS + 1), EXPECTED_ERROR);
        assertParseFailure(parser, "27/10/2026 999999999999999999", EXPECTED_ERROR);
    }

    @Test
    public void parse_additionalArguments_throwsParseException() {
        assertParseFailure(parser, "27/10/2026 7 extra", EXPECTED_ERROR);
        assertParseFailure(parser, "7", EXPECTED_ERROR);
    }
}
