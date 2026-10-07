package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Comparator;

import seedu.address.model.Model;

/**
 * Lists persons with visits today or later in chronological order.
 */
public class SortCommand extends Command {

    public static final String COMMAND_WORD = "sort";
    public static final String MESSAGE_SUCCESS = "Listed visits from today onward, earliest first.";

    /**
     * Shows all visits from today onward, using the computer's local date at execution time.
     */
    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate today = LocalDate.now();
        model.updateFilteredPersonList(person -> !person.getVisitDate().value.toLocalDate().isBefore(today));
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
