package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.PrevCommand;

public class PrevCommandParserTest {

    private final PrevCommandParser parser = new PrevCommandParser();

    @Test
    public void parse_validArgs_returnsPrevCommand() {
        // EP: Positive indices form a valid-value partition, including small and large values.
        // Boundary: Index 1 and Integer.MAX_VALUE are the lower and upper valid endpoints.
        assertParseSuccess(parser, "1", new PrevCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " \t1\t ", new PrevCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "2147483647", new PrevCommand(Index.fromOneBased(Integer.MAX_VALUE)));
        // Representation: Leading zeroes preserve the same valid index.
        assertParseSuccess(parser, "0002147483647", new PrevCommand(Index.fromOneBased(Integer.MAX_VALUE)));
        // Stress sample: This long spelling of one is not a specified maximum input length.
        assertParseSuccess(parser, "0".repeat(1000) + "1", new PrevCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_missingArgs_showsInvalidFormat() {
        // EP: Missing and whitespace-only input.
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, PrevCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, " ", expectedMessage);
    }

    @Test
    public void parse_invalidArgs_showsReasonAndPrevUsage() {
        String expectedMessage = ParserUtil.MESSAGE_INVALID_INDEX + "\n" + PrevCommand.MESSAGE_USAGE;
        String[] invalidArguments = {
            // Boundary: Zero is immediately below the smallest valid displayed index, one.
            "0",
            // EP: A negative signed index is a representative non-positive input.
            "-1",
            // EP: Signed, nonnumeric, and non-integral spellings form invalid numeric partitions.
            "+1", "a", "1.5", "-2147483649", "+2147483648", "2147483648abc", "2147483648.0",
            // Stress sample: A long spelling of zero is still non-positive.
            "0".repeat(1000),
            // EP: Extra tokens and unsupported prefixes.
            "1 2", "1 vd/1/11/2026 1000"
        };
        for (String args : invalidArguments) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }

    @Test
    public void parse_oversizedIndex_showsSpecificReasonAndPrevUsage() {
        // Boundary: The first decimal value above Integer.MAX_VALUE gets the overflow-specific message.
        String expectedMessage = ParserUtil.MESSAGE_INDEX_TOO_LARGE + "\n" + PrevCommand.MESSAGE_USAGE;
        for (String index : List.of("2147483648", "0002147483648", "999999999999999999999999",
                "9".repeat(1000), "٢١٤٧٤٨٣٦٤٨", " \t2147483648\t ")) {
            assertParseFailure(parser, index, expectedMessage);
        }
    }

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        // API precondition: null cannot arrive as text from the command box.
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
