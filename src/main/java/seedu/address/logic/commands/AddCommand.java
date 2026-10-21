package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BLOOD_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMERGENCY_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VISIT_DATE;

import java.util.List;
import java.util.Optional;

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
            + "[" + PREFIX_BLOOD_TYPE + "BLOOD_TYPE] "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_ADDRESS + "ADDRESS "
            + PREFIX_VISIT_DATE + "VISIT_DATE "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_NOTE + "NOTES] "
            + "[" + PREFIX_EMERGENCY_CONTACT + "[NAME, ]PHONE_NUMBER] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_BLOOD_TYPE + "A+ "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_ADDRESS + "311, Clementi Ave 2, #02-25 "
            + PREFIX_VISIT_DATE + "18/9/2026 1000 "
            + PREFIX_TAG + "followup ";

    public static final String MESSAGE_SUCCESS = "Added patient %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "A patient with the same name and phone number already exists in the address book. "
                    + "The existing patient is shown in the patient details panel.";
    public static final String MESSAGE_DUPLICATE_VISIT_DATE =
            "Another patient already has a current or previous visit at %1$s. "
                    + "The patient is shown in the patient details panel. Choose a different date and time.";
    public static final String MESSAGE_CONFIRM_DUPLICATE_NAME =
            "A patient named %1$s already exists. The matching patient is shown in the patient details panel. "
                    + "Add this patient anyway? Type y to confirm. Any other input cancels.";
    public static final String MESSAGE_CONFIRM_DUPLICATE_NAMES =
            "%2$d patients named %1$s already exist. Only one matching patient is shown in the patient details "
                    + "panel. Add this patient anyway? Type y to confirm. Any other input cancels.";
    public static final String MESSAGE_CONFIRM_FULL_DAY =
            "Heads up: you already have %1$d visits on %2$s. Add %3$s anyway? "
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

        List<Person> nameDuplicates = DuplicatePersonFinder.findNameDuplicates(model.getAddressBook(), toAdd);
        if (!nameDuplicates.isEmpty()) {
            String prompt = formatDuplicateNamePrompt(nameDuplicates.size());
            return requestConfirmation(prompt, this::addPersonAfterVisitDayCheck, nameDuplicates.getFirst());
        }

        return addPersonAfterVisitDayCheck(model);
    }

    /**
     * Adds the person, first asking for confirmation if the visit day already has
     * {@value Model#MAX_VISITS_PER_DAY} or more visits.
     *
     * @throws CommandException If a true duplicate or visit-date conflict now exists in the address book.
     */
    private CommandResult addPersonAfterVisitDayCheck(Model model) throws CommandException {
        int visitsOnSameDay = model.countPersonsWithSameVisitDay(toAdd);
        if (visitsOnSameDay >= Model.MAX_VISITS_PER_DAY) {
            String prompt = String.format(MESSAGE_CONFIRM_FULL_DAY, visitsOnSameDay,
                    toAdd.getVisitDate().toDayString(), toAdd.getName());
            return requestConfirmation(prompt, this::addPerson);
        }

        return addPerson(model);
    }

    /**
     * Returns a result asking the user to confirm with {@code y} before running {@code confirmedAction}.
     */
    private static CommandResult requestConfirmation(String prompt,
            CommandInputRequest.ConfirmedAction confirmedAction) {
        CommandInputRequest inputRequest = createConfirmationRequest(prompt, confirmedAction);
        return CommandResult.requestInput(inputRequest);
    }

    /**
     * Returns a result that also displays one matching patient while requesting confirmation.
     */
    private static CommandResult requestConfirmation(String prompt,
            CommandInputRequest.ConfirmedAction confirmedAction, Person personToView) {
        CommandInputRequest inputRequest = createConfirmationRequest(prompt, confirmedAction);
        return CommandResult.requestInput(inputRequest, personToView);
    }

    /**
     * Returns a confirmation request that adds the person only after a {@code y} response.
     */
    private static CommandInputRequest createConfirmationRequest(String prompt,
            CommandInputRequest.ConfirmedAction confirmedAction) {
        return CommandInputRequest.createConfirmation(prompt, MESSAGE_ADD_CANCELLED, confirmedAction);
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
        Optional<Person> identityDuplicate =
                DuplicatePersonFinder.findIdentityDuplicate(model.getAddressBook(), toAdd);
        if (identityDuplicate.isPresent()) {
            throw new CommandException(String.format(MESSAGE_NOT_ADDED, MESSAGE_DUPLICATE_PERSON),
                    identityDuplicate.get());
        }

        Optional<Person> visitDateDuplicate =
                DuplicatePersonFinder.findVisitDateDuplicate(model.getAddressBook(), toAdd);
        if (visitDateDuplicate.isPresent()) {
            String conflictMessage = String.format(MESSAGE_DUPLICATE_VISIT_DATE, toAdd.getVisitDate());
            throw new CommandException(String.format(MESSAGE_NOT_ADDED, conflictMessage), visitDateDuplicate.get());
        }
    }

    /**
     * Returns a grammatically correct confirmation prompt for the duplicate-name matches.
     */
    private String formatDuplicateNamePrompt(int duplicateCount) {
        assert duplicateCount > 0;

        return (duplicateCount == 1)
                ? String.format(MESSAGE_CONFIRM_DUPLICATE_NAME, toAdd.getName())
                : String.format(MESSAGE_CONFIRM_DUPLICATE_NAMES, toAdd.getName(), duplicateCount);
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
