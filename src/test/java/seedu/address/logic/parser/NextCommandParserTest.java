package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NextCommand;
import seedu.address.model.person.VisitDate;

/**
 * Tests positional next arguments, whitespace normalization, and malformed input feedback.
 */
public class NextCommandParserTest {

    private final NextCommandParser parser = new NextCommandParser();

    @Test
    public void parse_validArguments_success() {
        // EP: Valid input combines a positive index with a valid minute-precision datetime.
        // Boundary: Index 1 and Integer.MAX_VALUE are the lower and upper valid endpoints.
        NextCommand expectedCommand = new NextCommand(INDEX_FIRST_PERSON, new VisitDate("1/11/2026 0930"));
        assertParseSuccess(parser, "1 1/11/2026 0930", expectedCommand);
        assertParseSuccess(parser, "  \t1\t 01/11/2026 \t0930\t ", expectedCommand);
        assertParseSuccess(parser, "2147483647 29/2/2028 0000",
                new NextCommand(Index.fromOneBased(Integer.MAX_VALUE), new VisitDate("29/2/2028 0000")));
        // Representation: Leading zeroes preserve the same valid index.
        assertParseSuccess(parser, "0002147483647 29/2/2028 0000",
                new NextCommand(Index.fromOneBased(Integer.MAX_VALUE), new VisitDate("29/2/2028 0000")));
        // Stress sample: This long spelling of one is not a specified maximum input length.
        assertParseSuccess(parser, "0".repeat(1000) + "1 1/11/2026 0930", expectedCommand);
        assertParseSuccess(parser, "1 31/12/2026 2359",
                new NextCommand(INDEX_FIRST_PERSON, new VisitDate("31/12/2026 2359")));
    }

    @Test
    public void parse_emptyArguments_showsNextUsage() {
        // EP: Empty and whitespace-only input is the missing-argument partition.
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, NextCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, " \t ", expectedMessage);
    }

    @Test
    public void parse_missingArguments_showsReasonAndNextUsage() {
        // EP: Missing index, date, or time is rejected before command construction.
        for (String input : List.of("1", "1 1/11/2026", "11/11/20260930", "1/11/2026 0930")) {
            assertParseFailure(parser, input,
                    NextCommandParser.MESSAGE_MISSING_ARGUMENTS + "\n" + NextCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_extraArguments_showsReasonAndNextUsage() {
        // EP: Extra positional tokens are rejected even when the first appointment is valid.
        for (String input : List.of("1 1/11/2026 0930 extra", "1 1/11/2026 0930 2/11/2026 1000")) {
            assertParseFailure(parser, input,
                    NextCommandParser.MESSAGE_EXTRA_ARGUMENTS + "\n" + NextCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_prefixMisuse_showsReasonAndNextUsage() {
        // EP: Unsupported prefixes are distinct from malformed positional values.
        for (String input : List.of("1 vd/1/11/2026 0930", "1 vd/ 1/11/2026 0930", "1 vd/", "vd/")) {
            assertParseFailure(parser, input,
                    NextCommandParser.MESSAGE_PREFIX_NOT_SUPPORTED + "\n" + NextCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_invalidIndex_showsReasonAndNextUsage() {
        // EP: Non-positive, signed, nonnumeric, and malformed numeric spellings are invalid partitions.
        for (String index : List.of("0", "-1", "+1", "abc", "1.0", "-2147483649", "+2147483648",
                "2147483648abc", "2147483648.0", "0".repeat(1000))) {
            assertParseFailure(parser, index + " 1/11/2026 0930",
                    ParserUtil.MESSAGE_INVALID_INDEX + "\n" + NextCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_oversizedIndex_showsSpecificReasonAndNextUsage() {
        // Boundary: Integer.MAX_VALUE is valid; the decimal string 2147483648 is the first overflow value.
        for (String index : List.of("2147483648", "0002147483648", "999999999999999999999999",
                "9".repeat(1000), "٢١٤٧٤٨٣٦٤٨")) {
            assertParseFailure(parser, index + " 1/11/2026 0930",
                    NextCommandParser.MESSAGE_INDEX_TOO_LARGE + "\n" + NextCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_invalidDateTime_showsVisitDateConstraints() {
        // EPs: Impossible calendar dates and malformed datetime spellings.
        // Boundaries: Day 0, month 13, hour 24, minute 60, and years 1899 and 2100 are outside their valid ranges.
        for (String dateTime : List.of("31/2/2026 0930", "29/2/2027 0930", "0/11/2026 0930", "1/13/2026 0930",
                "31/12/1899 0930", "1/1/2100 0930",
                "1/11/2026 2400", "1/11/2026 1260", "1/11/2026 930", "1/11/2026 09:30", "1/11/2026 09300",
                "1/11/2026 0930extra", "\"1/11/2026 0930\"", "1/11/20260930 0930")) {
            assertParseFailure(parser, "1 " + dateTime, VisitDate.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_pastDateTime_leavesChronologyAndWarningsToExecution() {
        assertParseSuccess(parser, "1 1/1/2025 1000",
                new NextCommand(INDEX_FIRST_PERSON, new VisitDate("1/1/2025 1000")));
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        // API preconditions, distinct from malformed CLI input.
        assertThrows(NullPointerException.class, () -> parser.parse(null));
        assertThrows(NullPointerException.class, () -> new NextCommandParser(null));
    }
}
