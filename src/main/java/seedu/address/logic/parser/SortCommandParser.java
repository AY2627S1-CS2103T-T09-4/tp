package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

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
     * @throws ParseException If the field, cutoff date, or number of arguments is invalid.
     */
    @Override
    public SortCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        String[] arguments = trimmedArgs.isEmpty() ? new String[0] : trimmedArgs.split("\\s+");
        if (arguments.length < 1 || arguments.length > 2 || !DATE_FIELD.equals(arguments[0])) {
            throw invalidCommandFormat();
        }
        if (arguments.length == 1) {
            return new SortCommand();
        }

        try {
            LocalDate cutoffDate = LocalDate.parse(arguments[1], SortCommand.CUTOFF_DATE_FORMATTER);
            return new SortCommand(cutoffDate);
        } catch (DateTimeParseException exception) {
            throw invalidCommandFormat();
        }
    }

    private static ParseException invalidCommandFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE));
    }
}
