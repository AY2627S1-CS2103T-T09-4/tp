package seedu.address.logic.commands.exceptions;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.logic.commands.Command;
import seedu.address.model.person.Person;

/**
 * Represents an error which occurs during execution of a {@link Command}.
 */
public class CommandException extends Exception {

    private final Person personToView;

    /**
     * Constructs an exception with the specified detail {@code message}.
     */
    public CommandException(String message) {
        super(message);
        personToView = null;
    }

    /**
     * Constructs an exception that also displays {@code personToView} in the patient details panel.
     */
    public CommandException(String message, Person personToView) {
        super(message);
        this.personToView = requireNonNull(personToView);
    }

    /**
     * Constructs a new {@code CommandException} with the specified detail {@code message} and {@code cause}.
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
        personToView = null;
    }

    /**
     * Returns the person whose details should accompany this error, if one was supplied.
     */
    public Optional<Person> getPersonToView() {
        return Optional.ofNullable(personToView);
    }
}
