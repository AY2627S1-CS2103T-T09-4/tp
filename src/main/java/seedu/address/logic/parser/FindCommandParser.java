package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VISIT_DATE;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.AddressContainsKeywordsPredicate;
import seedu.address.model.person.MatchesAllCriteriaPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.NoteContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.PhoneContainsKeywordsPredicate;
import seedu.address.model.person.TagContainsKeywordsPredicate;
import seedu.address.model.person.VisitDate;
import seedu.address.model.person.VisitDateOnDaysPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    public static final String MESSAGE_EMPTY_KEYWORDS = "Give at least one keyword after %1$s.";
    public static final String MESSAGE_INVALID_PHONE_KEYWORD = "Phone keywords should only contain digits.";
    public static final String MESSAGE_INVALID_VISIT_DAY = "\"%1$s\" is not a valid visit date. "
            + "Give dates without a time in the format d/M/yyyy, e.g. " + PREFIX_VISIT_DATE + "18/9/2026.";

    private static final String PHONE_KEYWORD_REGEX = "\\d+";

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * Any whitespace, such as a tab, separates fields, so no field is silently read as another field's keyword.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_ADDRESS, PREFIX_TAG,
                        PREFIX_VISIT_DATE, PREFIX_NOTE);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    FindCommand.MESSAGE_KEYWORDS_NEED_PREFIX + "\n" + FindCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_ADDRESS, PREFIX_TAG,
                PREFIX_VISIT_DATE, PREFIX_NOTE);

        List<Predicate<Person>> criteria = new ArrayList<>();
        parseKeywords(argMultimap, PREFIX_NAME).map(NameContainsKeywordsPredicate::new).ifPresent(criteria::add);
        parsePhoneKeywords(argMultimap).map(PhoneContainsKeywordsPredicate::new).ifPresent(criteria::add);
        parseKeywords(argMultimap, PREFIX_ADDRESS).map(AddressContainsKeywordsPredicate::new).ifPresent(criteria::add);
        parseKeywords(argMultimap, PREFIX_TAG).map(TagContainsKeywordsPredicate::new).ifPresent(criteria::add);
        parseVisitDays(argMultimap).map(VisitDateOnDaysPredicate::new).ifPresent(criteria::add);
        parseKeywords(argMultimap, PREFIX_NOTE).map(NoteContainsKeywordsPredicate::new).ifPresent(criteria::add);

        if (criteria.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        return new FindCommand(new MatchesAllCriteriaPredicate(criteria));
    }

    /**
     * Returns the space-separated keywords given after {@code prefix}, or an empty {@code Optional}
     * if the prefix is absent.
     *
     * @throws ParseException If the prefix is present without any keywords.
     */
    private static Optional<List<String>> parseKeywords(ArgumentMultimap argMultimap, Prefix prefix)
            throws ParseException {
        Optional<String> value = argMultimap.getValue(prefix).map(String::trim);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        if (value.get().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_EMPTY_KEYWORDS, prefix));
        }
        return value.map(keywords -> List.of(keywords.split("\\s+")));
    }

    /**
     * Returns the keywords given after the phone prefix, or an empty {@code Optional} if the prefix is absent.
     *
     * @throws ParseException If the prefix is present without keywords or any keyword contains a non-digit.
     */
    private static Optional<List<String>> parsePhoneKeywords(ArgumentMultimap argMultimap) throws ParseException {
        Optional<List<String>> phoneKeywords = parseKeywords(argMultimap, PREFIX_PHONE);
        boolean hasInvalidKeyword = phoneKeywords.orElse(List.of()).stream()
                .anyMatch(keyword -> !keyword.matches(PHONE_KEYWORD_REGEX));
        if (hasInvalidKeyword) {
            throw new ParseException(MESSAGE_INVALID_PHONE_KEYWORD);
        }
        return phoneKeywords;
    }

    /**
     * Returns the calendar days given after the visit date prefix, or an empty {@code Optional} if the prefix is
     * absent.
     *
     * @throws ParseException If the prefix is present without dates or any date is not a valid d/M/yyyy date.
     */
    private static Optional<List<LocalDate>> parseVisitDays(ArgumentMultimap argMultimap) throws ParseException {
        Optional<List<String>> dayKeywords = parseKeywords(argMultimap, PREFIX_VISIT_DATE);
        if (dayKeywords.isEmpty()) {
            return Optional.empty();
        }

        List<LocalDate> days = new ArrayList<>();
        try {
            for (String dayKeyword : dayKeywords.get()) {
                days.add(parseVisitDay(dayKeyword));
            }
        } catch (ParseException exception) {
            String visitDays = String.join(" ", dayKeywords.get());
            throw new ParseException(String.format(MESSAGE_INVALID_VISIT_DAY, visitDays), exception);
        }
        return Optional.of(days);
    }

    /**
     * Parses one d/M/yyyy date, rejecting impossible dates such as 31/2/2026.
     *
     * @throws ParseException If {@code dayKeyword} is not a valid date in that format.
     */
    private static LocalDate parseVisitDay(String dayKeyword) throws ParseException {
        try {
            return LocalDate.parse(dayKeyword, VisitDate.DAY_INPUT_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new ParseException(String.format(MESSAGE_INVALID_VISIT_DAY, dayKeyword), exception);
        }
    }
}
