package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALCOHOL_HISTORY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALLERGIES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BLOOD_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE_OF_BIRTH;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMERGENCY_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SMOKING_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VISIT_DATE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
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
            + "[" + PREFIX_DATE_OF_BIRTH + "DATE] "
            + "[" + PREFIX_BLOOD_TYPE + "BLOOD_TYPE] "
            + "[" + PREFIX_ALLERGIES + "ALLERGIES] "
            + "[" + PREFIX_ALCOHOL_HISTORY + "ALCOHOL_HISTORY] "
            + "[" + PREFIX_SMOKING_STATUS + "STATUS] "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_ADDRESS + "ADDRESS "
            + PREFIX_VISIT_DATE + "VISIT_DATE "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_NOTE + "NOTES] "
            + "[" + PREFIX_EMERGENCY_CONTACT + "[NAME, ]PHONE_NUMBER] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_DATE_OF_BIRTH + "15/6/1990 "
            + PREFIX_BLOOD_TYPE + "A+ "
            + PREFIX_ALLERGIES + "Penicillin; latex "
            + PREFIX_ALCOHOL_HISTORY + "Occasional drinking "
            + PREFIX_SMOKING_STATUS + "former "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_ADDRESS + "311, Clementi Ave 2, #02-25 "
            + PREFIX_VISIT_DATE + "18/9/2026 1000 "
            + PREFIX_TAG + "followup ";

    public static final String MESSAGE_SUCCESS = "Added patient %1$s\nVisit date: %2$s";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "A patient with the same name and phone number already exists in the address book. "
                    + "The existing patient is shown in the patient details panel.";
    public static final String MESSAGE_DUPLICATE_VISIT_DATE =
            "Another patient already has a current or previous visit at %1$s. "
                    + "The patient is shown in the patient details panel. Choose a different date and time.";
    public static final String MESSAGE_WARNING_DUPLICATE_NAME =
            "A patient named %1$s already exists. The matching patient is shown in the patient details panel.";
    public static final String MESSAGE_WARNING_DUPLICATE_NAMES =
            "%2$d patients named %1$s already exist. Only one matching patient is shown in the patient details panel.";
    public static final String MESSAGE_WARNING_FULL_DAY = "Heads up: you already have %1$d visits on %2$s.";
    public static final String MESSAGE_CONFIRM_ADD = "Add %1$s anyway? Type y to confirm. Any other input cancels.";
    public static final String MESSAGE_ADD_CANCELLED = "Addition cancelled.";
    public static final String MESSAGE_NOT_ADDED = "The patient is not added due to: %1$s";

    private static final Logger logger = LogsCenter.getLogger(AddCommand.class);

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    /**
     * {@inheritDoc}
     * Asks for a single confirmation listing every warning when the name matches another patient or the visit day
     * already has {@value Model#MAX_VISITS_PER_DAY} or more visits, so the user never confirms twice in a row.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        validateNoConflicts(model);

        List<Person> nameDuplicates = DuplicatePersonFinder.findNameDuplicates(model.getAddressBook(), toAdd);
        List<String> warnings = collectWarnings(model, nameDuplicates);
        if (warnings.isEmpty()) {
            return addPerson(model);
        }

        String prompt = String.join(" ", warnings) + " " + String.format(MESSAGE_CONFIRM_ADD, toAdd.getName());
        CommandInputRequest inputRequest =
                CommandInputRequest.createConfirmation(prompt, MESSAGE_ADD_CANCELLED, this::addPerson);
        // Previews one matching patient so the user can tell whether the new patient is really someone else
        return nameDuplicates.isEmpty()
                ? CommandResult.requestInput(inputRequest)
                : CommandResult.requestInput(inputRequest, nameDuplicates.getFirst());
    }

    /**
     * Returns the warnings the user must accept before the person is added, in the order they are shown.
     * An empty list means the person can be added without confirmation.
     */
    private List<String> collectWarnings(Model model, List<Person> nameDuplicates) {
        List<String> warnings = new ArrayList<>();
        if (!nameDuplicates.isEmpty()) {
            warnings.add(formatDuplicateNameWarning(nameDuplicates.size()));
        }

        int visitsOnSameDay = model.countPersonsWithSameVisitDay(toAdd);
        if (visitsOnSameDay >= Model.MAX_VISITS_PER_DAY) {
            warnings.add(String.format(MESSAGE_WARNING_FULL_DAY, visitsOnSameDay,
                    toAdd.getVisitDate().toDayString()));
        }

        if (warnings.size() > 1) {
            logger.info("Combining " + warnings.size() + " add warnings into one confirmation for " + toAdd.getName());
        }
        return warnings;
    }

    /**
     * Adds the person captured when this command was created.
     *
     * @throws CommandException If a true duplicate or visit-date conflict now exists in the address book.
     */
    private CommandResult addPerson(Model model) throws CommandException {
        validateNoConflicts(model);

        model.addPerson(toAdd);
        logger.fine(() -> "Added patient to model; optional fields recorded: dateOfBirth="
                + !toAdd.getDateOfBirth().isEmpty() + ", bloodType=" + !toAdd.getBloodType().isEmpty()
                + ", allergies=" + !toAdd.getAllergies().isEmpty()
                + ", alcoholHistory=" + !toAdd.getAlcoholHistory().isEmpty()
                + ", smokingStatus=" + !toAdd.getSmokingStatus().isEmpty()
                + ", emergencyContact=" + !toAdd.getEmergencyContact().isEmpty());
        return CommandResult.showUpdatedPerson(formatSuccessMessage(toAdd), toAdd);
    }

    /**
     * Returns the message shown after {@code person} is added, listing their contact details, tags, and visit date
     * so that the doctor can check what was recorded.
     */
    public static String formatSuccessMessage(Person person) {
        requireNonNull(person);
        return String.format(MESSAGE_SUCCESS, Messages.format(person), person.getVisitDate());
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
     * Returns a grammatically correct warning for the duplicate-name matches.
     */
    private String formatDuplicateNameWarning(int duplicateCount) {
        assert duplicateCount > 0;

        return (duplicateCount == 1)
                ? String.format(MESSAGE_WARNING_DUPLICATE_NAME, toAdd.getName())
                : String.format(MESSAGE_WARNING_DUPLICATE_NAMES, toAdd.getName(), duplicateCount);
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
