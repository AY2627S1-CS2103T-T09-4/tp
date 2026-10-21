package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.NameContainsKeywordsPredicate;

/**
 * Finds and lists all persons in the address book with a name word that starts with any of the argument keywords.
 * Keyword matching is case insensitive.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds all patients with a word in their name that "
            + "starts with any of the specified keywords (case-insensitive) "
            + "and displays them as a list with index numbers.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " george priya";

    public static final String MESSAGE_PATIENTS_FOUND = "%1$d patients found.";
    public static final String MESSAGE_ONE_PATIENT_FOUND = "1 patient found.";
    public static final String MESSAGE_NO_PATIENTS_FOUND = "No patients found. "
            + "find matches the start of any word in a name, e.g. \"find g\" matches \"George Miller\".";

    private final NameContainsKeywordsPredicate predicate;

    /**
     * Creates a command that finds patients matching the given name predicate.
     */
    public FindCommand(NameContainsKeywordsPredicate predicate) {
        this.predicate = predicate;
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
