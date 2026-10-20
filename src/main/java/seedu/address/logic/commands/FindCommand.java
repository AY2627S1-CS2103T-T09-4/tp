package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Finds and lists all persons in the address book that match the given search criteria.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds all patients matching every given field "
            + "and displays them as a list with index numbers. Name and address keywords match the start of any word, "
            + "phone keywords match any part of the number, and tag keywords match the start of a tag "
            + "(all case-insensitive). Several keywords for one field match any of them.\n"
            + "Parameters (at least one): "
            + "[" + PREFIX_NAME + "NAME_KEYWORD...] "
            + "[" + PREFIX_PHONE + "PHONE_KEYWORD...] "
            + "[" + PREFIX_ADDRESS + "ADDRESS_KEYWORD...] "
            + "[" + PREFIX_TAG + "TAG_KEYWORD...]\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_NAME + "alex ben " + PREFIX_ADDRESS + "clementi";
    public static final String MESSAGE_KEYWORDS_NEED_PREFIX = "Put a prefix before search keywords, e.g. "
            + COMMAND_WORD + " " + PREFIX_NAME + "alex for names or " + COMMAND_WORD + " " + PREFIX_PHONE
            + "9123 for phone numbers.";

    public static final String MESSAGE_PATIENTS_FOUND = "%1$d patients found.";
    public static final String MESSAGE_ONE_PATIENT_FOUND = "1 patient found.";
    public static final String MESSAGE_NO_PATIENTS_FOUND = "No patients found. "
            + "Name keywords match the start of any word in a name, e.g. \"find n/b\" matches \"Bernice Yu\".";

    private final Predicate<Person> predicate;

    /**
     * Creates a command that lists the persons satisfying {@code predicate}.
     */
    public FindCommand(Predicate<Person> predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        return new CommandResult(formatResultMessage(model.getFilteredPersonList().size()));
    }

    /**
     * Returns the feedback for {@code patientCount} matches, using the singular for one match
     * and a search hint for none.
     */
    private static String formatResultMessage(int patientCount) {
        return switch (patientCount) {
            case 0 -> MESSAGE_NO_PATIENTS_FOUND;
            case 1 -> MESSAGE_ONE_PATIENT_FOUND;
            default -> String.format(MESSAGE_PATIENTS_FOUND, patientCount);
        };
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
