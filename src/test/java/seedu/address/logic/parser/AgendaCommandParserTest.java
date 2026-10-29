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
    public void parse_invalidDate_namesTheInvalidDate() {
        for (String date : new String[] {"31/2/2026", "29/2/2025", "2026-10-27", "27/10/26", "today", "7"}) {
            assertParseFailure(parser, date, usageError(
                    String.format(AgendaCommandParser.MESSAGE_INVALID_START_DATE_FORMAT, date)));
        }
    }

    @Test
    public void parse_startBeforeFirstSupportedDate_throwsParseException() {
        String expectedError = usageError(AgendaCommand.MESSAGE_INVALID_START_DATE);

        assertParseFailure(parser, "31/12/1899", expectedError);
        assertParseFailure(parser, "1/1/1800 365", expectedError);
    }

    @Test
    public void parse_startOnFirstSupportedDate_returnsAgenda() throws Exception {
        assertEquals(new AgendaCommand(LocalDate.of(1900, 1, 1)), parser.parse("1/1/1900"));
    }

    @Test
    public void parse_overflowingDateRange_throwsParseException() {
        String expectedError = usageError(AgendaCommand.MESSAGE_INVALID_DATE_RANGE);

        assertParseFailure(parser, "2/1/2099 365", expectedError);
        assertParseFailure(parser, "26/12/2099", expectedError);
        assertParseFailure(parser, "28/10/2099 365", expectedError);
        assertParseFailure(parser, "31/12/2099 2", expectedError);
        assertParseFailure(parser, "31/12/+999999999 1", expectedError);
    }

    @Test
    public void parse_rangeEndingOnLastSupportedDate_returnsAgenda() throws Exception {
        assertEquals(new AgendaCommand(LocalDate.of(2099, 1, 1), 365), parser.parse("1/1/2099 365"));
        assertEquals(new AgendaCommand(LocalDate.of(2099, 12, 25)), parser.parse("25/12/2099"));
        assertEquals(new AgendaCommand(LocalDate.of(2099, 12, 31), 1), parser.parse("31/12/2099 1"));
    }

    @Test
    public void parse_invalidDays_explainsAllowedRange() {
        String expectedError = usageError(AgendaCommand.MESSAGE_INVALID_DAYS);
        for (String days : new String[] {"0", "-1", "1.5", "seven", String.valueOf(AgendaCommand.MAX_DAYS + 1),
            "999999999999999999"}) {
            assertParseFailure(parser, "27/10/2026 " + days, expectedError);
        }
    }

    @Test
    public void parse_additionalArguments_throwsParseException() {
        assertParseFailure(parser, "27/10/2026 7 extra", usageError(AgendaCommandParser.MESSAGE_EXTRA_ARGUMENTS));
    }

    private static String usageError(String reason) {
        return reason + "\n" + AgendaCommand.MESSAGE_USAGE;
    }
}
