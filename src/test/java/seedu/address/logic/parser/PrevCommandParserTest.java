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
        assertParseSuccess(parser, "1", new PrevCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " \t1\t ", new PrevCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "2147483647", new PrevCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, PrevCommand.MESSAGE_USAGE);
        for (String args : List.of("", " ", "0", "-1", "+1", "a", "1.5", "2147483648",
                "999999999999999999999999", "1 2", "1 vd/1/11/2026 1000")) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
