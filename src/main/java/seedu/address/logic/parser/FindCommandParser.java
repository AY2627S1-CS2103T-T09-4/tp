package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.AddressContainsKeywordsPredicate;
import seedu.address.model.person.MatchesAllCriteriaPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.PhoneContainsKeywordsPredicate;
import seedu.address.model.person.TagContainsKeywordsPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    public static final String MESSAGE_EMPTY_KEYWORDS = "Give at least one keyword after %1$s.";
    public static final String MESSAGE_INVALID_PHONE_KEYWORD = "Phone keywords should only contain digits.";

    private static final String PHONE_KEYWORD_REGEX = "\\d+";

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_ADDRESS, PREFIX_TAG);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    FindCommand.MESSAGE_KEYWORDS_NEED_PREFIX + "\n" + FindCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_ADDRESS, PREFIX_TAG);

        List<Predicate<Person>> criteria = new ArrayList<>();
        parseKeywords(argMultimap, PREFIX_NAME).map(NameContainsKeywordsPredicate::new).ifPresent(criteria::add);
        parsePhoneKeywords(argMultimap).map(PhoneContainsKeywordsPredicate::new).ifPresent(criteria::add);
        parseKeywords(argMultimap, PREFIX_ADDRESS).map(AddressContainsKeywordsPredicate::new).ifPresent(criteria::add);
        parseKeywords(argMultimap, PREFIX_TAG).map(TagContainsKeywordsPredicate::new).ifPresent(criteria::add);

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
}
