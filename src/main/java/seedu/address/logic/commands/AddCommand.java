package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VISIT_DATE;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a person to the address book. "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_ADDRESS + "ADDRESS "
            + PREFIX_VISIT_DATE + "VISIT_DATE "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_NOTE + "NOTES] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_ADDRESS + "311, Clementi Ave 2, #02-25 "
            + PREFIX_VISIT_DATE + "18/9/2026 1000 "
            + PREFIX_TAG + "friends "
            + PREFIX_TAG + "owesMoney";

    public static final String MESSAGE_SUCCESS = "Added patient %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "A patient with the same name and phone number already exists in the address book.";
    public static final String MESSAGE_DUPLICATE_VISIT_DATE =
            "Another patient already has a visit scheduled for %1$s. Choose a different date and time.";
    public static final String MESSAGE_CONFIRM_DUPLICATE_NAME =
            "A patient named %1$s already exists. Add this patient anyway? "
                    + "Type y to confirm. Any other input cancels.";
    public static final String MESSAGE_ADD_CANCELLED = "Addition cancelled.";
    public static final String MESSAGE_NOT_ADDED = "The patient is not added due to: %1$s";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        validateNoConflicts(model);

        if (model.hasPersonWithSameName(toAdd)) {
            String prompt = String.format(MESSAGE_CONFIRM_DUPLICATE_NAME, toAdd.getName());
            CommandInputRequest inputRequest = CommandInputRequest.createConfirmation(
                    prompt, MESSAGE_ADD_CANCELLED, confirmedModel -> addPerson(confirmedModel));
            return CommandResult.requestInput(inputRequest);
        }

        return addPerson(model);
    }

    /**
     * Adds the person captured when this command was created.
     *
     * @throws CommandException If a true duplicate or visit-date conflict now exists in the address book.
     */
    private CommandResult addPerson(Model model) throws CommandException {
        validateNoConflicts(model);

        model.addPerson(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd.getName()));
    }

    /**
     * Checks that the captured person has neither an identity nor a visit-date conflict.
     */
    private void validateNoConflicts(Model model) throws CommandException {
        if (model.hasPerson(toAdd)) {
            throw new CommandException(String.format(MESSAGE_NOT_ADDED, MESSAGE_DUPLICATE_PERSON));
        }
        if (model.hasPersonWithSameVisitDate(toAdd)) {
            String conflictMessage = String.format(MESSAGE_DUPLICATE_VISIT_DATE, toAdd.getVisitDate());
            throw new CommandException(String.format(MESSAGE_NOT_ADDED, conflictMessage));
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
