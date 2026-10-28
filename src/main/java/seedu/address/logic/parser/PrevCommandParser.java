package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

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
        return new PrevCommand(ParserUtil.parseIndex(args, PrevCommand.MESSAGE_USAGE));
    }
}
