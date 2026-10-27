package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import seedu.address.logic.commands.AgendaCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.VisitDate;

/**
 * Parses input arguments and creates an {@code AgendaCommand}.
 */
public class AgendaCommandParser implements Parser<AgendaCommand> {

    /**
     * Parses a required start date and optional number of days into an agenda command.
     *
     * @throws ParseException If an argument is invalid or supplied in the wrong position.
     */
    @Override
    public AgendaCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw invalidCommandFormat();
        }

        String[] arguments = trimmedArgs.split("\\s+");
        if (arguments.length > 2) {
            throw invalidCommandFormat();
        }

        LocalDate startDate = parseStartDate(arguments[0]);
        if (arguments.length == 1) {
            return new AgendaCommand(startDate);
        }
        return new AgendaCommand(startDate, parseDays(arguments[1]));
    }

    private static LocalDate parseStartDate(String argument) throws ParseException {
        try {
            return LocalDate.parse(argument, VisitDate.DAY_INPUT_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw invalidCommandFormat();
        }
    }

    private static int parseDays(String argument) throws ParseException {
        try {
            int days = Integer.parseInt(argument);
            if (days < 1 || days > AgendaCommand.MAX_DAYS) {
                throw invalidCommandFormat();
            }
            return days;
        } catch (NumberFormatException exception) {
            throw invalidCommandFormat();
        }
    }

    private static ParseException invalidCommandFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AgendaCommand.MESSAGE_USAGE));
    }
}
