package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.PrevCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses a displayed patient index into a rollback command.
 */
public class PrevCommandParser implements Parser<PrevCommand> {

    /**
     * Parses a single positive index into a command that restores the latest previous visit date.
     *
     * @throws ParseException If the arguments are not a single positive integer within the supported range.
     * @throws NullPointerException If {@code args} is null.
     */
    @Override
    public PrevCommand parse(String args) throws ParseException {
        requireNonNull(args);
        try {
            return new PrevCommand(ParserUtil.parseIndex(args));
        } catch (ParseException pe) {
            if (ParserUtil.isPositiveInteger(args.trim())) {
                throw new ParseException(ParserUtil.MESSAGE_INDEX_TOO_LARGE + "\n" + PrevCommand.MESSAGE_USAGE, pe);
            }
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, PrevCommand.MESSAGE_USAGE), pe);
        }
    }
}
