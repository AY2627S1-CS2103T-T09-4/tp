package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.DISPLAY_LOCALE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VISIT_DATE;

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
            + "and displays them as a list with index numbers. Name, address and note keywords match the start of "
            + "any word, phone keywords match any part of the number, tag keywords match the start of a tag "
            + "(all case-insensitive), and visit dates (d/M/yyyy) match current visits on that day. "
            + "Several keywords for one field match any of them.\n"
            + "Parameters (at least one): "
            + "[" + PREFIX_NAME + "NAME_KEYWORD...] "
            + "[" + PREFIX_PHONE + "PHONE_KEYWORD...] "
            + "[" + PREFIX_ADDRESS + "ADDRESS_KEYWORD...] "
            + "[" + PREFIX_TAG + "TAG_KEYWORD...] "
            + "[" + PREFIX_VISIT_DATE + "DATE...] "
            + "[" + PREFIX_NOTE + "NOTE_KEYWORD...]\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_NAME + "george priya " + PREFIX_TAG + "morning";
    public static final String MESSAGE_KEYWORDS_NEED_PREFIX = "Search keywords must use a supported prefix: "
            + PREFIX_NAME + ", " + PREFIX_PHONE + ", " + PREFIX_ADDRESS + ", " + PREFIX_TAG + ", "
            + PREFIX_VISIT_DATE + ", or " + PREFIX_NOTE + ".";

    public static final String MESSAGE_PATIENTS_FOUND = "%1$d patients found.";
    public static final String MESSAGE_ONE_PATIENT_FOUND = "1 patient found.";
    public static final String MESSAGE_NO_PATIENTS_FOUND = "No patients found. Search with "
            + PREFIX_NAME + " for names, " + PREFIX_PHONE + " for phone numbers, "
            + PREFIX_ADDRESS + " for addresses, " + PREFIX_TAG + " for tags, " + PREFIX_VISIT_DATE
            + " for visit dates, or " + PREFIX_NOTE + " for notes.";

    private final Predicate<Person> predicate;

    /**
     * Creates a command that lists the patients satisfying {@code predicate}.
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
            default -> String.format(DISPLAY_LOCALE, MESSAGE_PATIENTS_FOUND, patientCount);
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
