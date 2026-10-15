package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a {@code SortCommand}.
 */
public class SortCommandParser implements Parser<SortCommand> {

    private static final String DATE_FIELD = "date";

    /**
     * Parses the given arguments and returns a {@code SortCommand}.
     *
     * @throws ParseException If the user input is not the supported sort field.
     */
    @Override
    public SortCommand parse(String args) throws ParseException {
        if (!DATE_FIELD.equals(args.trim())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE));
        }

        return new SortCommand();
    }
}
