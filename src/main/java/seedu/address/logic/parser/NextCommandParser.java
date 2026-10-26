package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.time.Clock;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NextCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.VisitDate;

/**
 * Parses the displayed index, date, and time for a {@code NextCommand}.
 */
public class NextCommandParser implements Parser<NextCommand> {

    public static final String MESSAGE_MISSING_ARGUMENTS = "Provide an index, date, and time in that order.";
    public static final String MESSAGE_EXTRA_ARGUMENTS = "Unexpected arguments after the visit time.";
    public static final String MESSAGE_PREFIX_NOT_SUPPORTED = "Use next INDEX DATETIME without the vd/ prefix.";
    public static final String MESSAGE_INDEX_TOO_LARGE = ParserUtil.MESSAGE_INDEX_TOO_LARGE;

    private static final int ARGUMENT_COUNT = 3;

    private final Clock clock;

    /**
     * Creates a parser whose commands use the computer's local clock.
     */
    public NextCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates a parser whose commands validate appointments using the supplied clock.
     */
    public NextCommandParser(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    /**
     * Parses exactly three positional arguments, normalizing whitespace between the date and time.
     *
     * @throws ParseException If arguments are missing, extra, prefixed, or invalid.
     */
    @Override
    public NextCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NextCommand.MESSAGE_USAGE));
        }

        String[] arguments = trimmedArgs.split("\\s+");
        for (String argument : arguments) {
            if (argument.contains(CliSyntax.PREFIX_VISIT_DATE.toString())) {
                throw createUsageError(MESSAGE_PREFIX_NOT_SUPPORTED);
            }
        }
        if (arguments.length < ARGUMENT_COUNT) {
            throw createUsageError(MESSAGE_MISSING_ARGUMENTS);
        }
        if (arguments.length > ARGUMENT_COUNT) {
            throw createUsageError(MESSAGE_EXTRA_ARGUMENTS);
        }

        Index index = parseIndex(arguments[0]);
        VisitDate visitDate = ParserUtil.parseVisitDate(arguments[1] + " " + arguments[2]);
        return new NextCommand(index, visitDate, clock);
    }

    private static Index parseIndex(String argument) throws ParseException {
        try {
            return ParserUtil.parseIndex(argument);
        } catch (ParseException exception) {
            // A positive integer rejected by the shared parser exceeds the supported index range.
            if (ParserUtil.isPositiveInteger(argument)) {
                throw createUsageError(MESSAGE_INDEX_TOO_LARGE);
            }
            throw createUsageError(exception.getMessage());
        }
    }

    private static ParseException createUsageError(String reason) {
        return new ParseException(reason + "\n" + NextCommand.MESSAGE_USAGE);
    }
}
