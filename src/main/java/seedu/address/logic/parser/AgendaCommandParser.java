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

    public static final String MESSAGE_INVALID_START_DATE_FORMAT =
            "\"%1$s\" is not a valid start date. Use d/M/yyyy, e.g. 27/10/2026.";
    public static final String MESSAGE_EXTRA_ARGUMENTS = "Give only a start date and an optional number of days.";

    /**
     * Parses a required start date and optional number of days into an agenda command.
     * Each kind of mistake is reported with its own reason, followed by the command usage.
     *
     * @throws ParseException If an argument is missing, invalid, or extra, or the range is outside visit dates.
     */
    @Override
    public AgendaCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AgendaCommand.MESSAGE_USAGE));
        }

        String[] arguments = trimmedArgs.split("\\s+");
        if (arguments.length > 2) {
            throw usageError(MESSAGE_EXTRA_ARGUMENTS);
        }

        LocalDate startDate = parseStartDate(arguments[0]);
        int days = arguments.length == 1 ? AgendaCommand.DEFAULT_DAYS : parseDays(arguments[1]);
        try {
            return new AgendaCommand(startDate, days);
        } catch (IllegalArgumentException exception) {
            // The constructor names the limit that the range breaks
            throw usageError(exception.getMessage());
        }
    }

    private static LocalDate parseStartDate(String argument) throws ParseException {
        try {
            return LocalDate.parse(argument, VisitDate.DAY_INPUT_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw usageError(String.format(MESSAGE_INVALID_START_DATE_FORMAT, argument));
        }
    }

    private static int parseDays(String argument) throws ParseException {
        try {
            int days = Integer.parseInt(argument);
            if (days < 1 || days > AgendaCommand.MAX_DAYS) {
                throw usageError(AgendaCommand.MESSAGE_INVALID_DAYS);
            }
            return days;
        } catch (NumberFormatException exception) {
            throw usageError(AgendaCommand.MESSAGE_INVALID_DAYS);
        }
    }

    /**
     * Returns a parse error giving {@code reason} followed by the command usage.
     */
    private static ParseException usageError(String reason) {
        return new ParseException(reason + "\n" + AgendaCommand.MESSAGE_USAGE);
    }
}
