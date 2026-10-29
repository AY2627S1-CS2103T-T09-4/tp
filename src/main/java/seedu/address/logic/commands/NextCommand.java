package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.Messages.DISPLAY_LOCALE;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;

/**
 * Retains a patient's current visit date and time before scheduling an available future appointment.
 * The current visit must have arrived or passed; retained dates do not verify attendance or completion.
 */
public class NextCommand extends Command {

    public static final String COMMAND_WORD = "next";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Schedules the next visit for the patient identified by the displayed index.\n"
            + "The current visit must have arrived or passed. Use edit to reschedule a future current visit.\n"
            + "Parameters: INDEX (must be a positive integer) DATETIME (d/M/yyyy HHmm, must be in the future)\n"
            + "Example: " + COMMAND_WORD + " 1 1/11/2026 0930";
    public static final String MESSAGE_SUCCESS = "Next visit scheduled for patient %1$s: %2$s";
    public static final String MESSAGE_UNCHANGED_VISIT_DATE = "Patient %1$s already has an appointment at %2$s.";
    public static final String MESSAGE_NON_FUTURE_VISIT_DATE =
            "The next visit must be later than the current time (%1$s). Choose a later date and time.";
    public static final String MESSAGE_CURRENT_VISIT_IN_FUTURE =
            "Patient %1$s has a current visit at %2$s that is still in the future. Use edit to reschedule it.";
    public static final String MESSAGE_DUPLICATE_VISIT_DATE =
            "Patient %1$s already has a current or previous visit at %2$s and is shown in the patient details panel. "
                    + "Choose a different date and time.";
    public static final String MESSAGE_CONFIRM_FULL_DAY =
            "Heads up: you already have %1$d visits on %2$s. Schedule %3$s's next visit on that day anyway? "
                    + "Type y to confirm. Any other input cancels.";
    public static final String MESSAGE_NEXT_CANCELLED = "Scheduling cancelled.";
    public static final String MESSAGE_PERSON_NO_LONGER_EXISTS =
            "The patient selected for scheduling no longer exists.";

    private static final Logger logger = LogsCenter.getLogger(NextCommand.class);

    private final Index index;
    private final VisitDate visitDate;
    private final Clock clock;

    /**
     * Creates a command using the computer's local clock.
     */
    public NextCommand(Index index, VisitDate visitDate) {
        this(index, visitDate, Clock.systemDefaultZone());
    }

    /**
     * Creates a command using the supplied clock to validate the appointment at execution time.
     */
    public NextCommand(Index index, VisitDate visitDate, Clock clock) {
        requireAllNonNull(index, visitDate, clock);
        this.index = index;
        this.visitDate = visitDate;
        this.clock = clock;
    }

    /**
     * {@inheritDoc}
     * Appends the current visit to the previous dates, replaces it with the requested future visit,
     * and restores the full displayed list in stored order after validation.
     * Asks for confirmation first if the requested day already has {@value Model#MAX_VISITS_PER_DAY} or more visits.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUpdate = displayedPersons.get(index.getZeroBased());
        validateVisitDate(personToUpdate);

        Person updatedPerson = createUpdatedPerson(personToUpdate);
        validateNoConflicts(model, personToUpdate, updatedPerson);
        return scheduleVisitAfterVisitDayCheck(model, personToUpdate, updatedPerson);
    }

    /**
     * Returns a replacement patient with the current date appended to the previous dates.
     * All fields other than the current and previous visit dates are preserved.
     */
    private Person createUpdatedPerson(Person personToUpdate) {
        List<VisitDate> previousVisitDates = new ArrayList<>(personToUpdate.getPreviousVisitDates());
        previousVisitDates.add(personToUpdate.getVisitDate());
        Person updatedPerson = personToUpdate.copyWithVisitDates(visitDate, previousVisitDates);

        assert updatedPerson.getPreviousVisitDates().size() == personToUpdate.getPreviousVisitDates().size() + 1
                : "Exactly one visit date should be retained";
        return updatedPerson;
    }

    /**
     * Schedules the visit, first asking for confirmation if it moves to another day that already has
     * {@value Model#MAX_VISITS_PER_DAY} or more visits. A next visit later on the same day as the current one
     * never asks, since it replaces that day's visit instead of adding another.
     *
     * @throws CommandException If the visit cannot be scheduled.
     */
    private CommandResult scheduleVisitAfterVisitDayCheck(Model model, Person personToUpdate, Person updatedPerson)
            throws CommandException {
        boolean isVisitDayChanged = !personToUpdate.getVisitDate().isSameDay(visitDate);
        int visitsOnNewDay = model.countPersonsWithSameVisitDay(updatedPerson);
        if (!isVisitDayChanged || visitsOnNewDay < Model.MAX_VISITS_PER_DAY) {
            return scheduleVisit(model, personToUpdate, updatedPerson);
        }

        logger.info("Requesting confirmation to schedule a visit on " + visitDate.toDayString()
                + ", which already has " + visitsOnNewDay + " visits");
        String prompt = String.format(DISPLAY_LOCALE, MESSAGE_CONFIRM_FULL_DAY, visitsOnNewDay,
                visitDate.toDayString(), updatedPerson.getName());
        CommandInputRequest inputRequest = CommandInputRequest.createConfirmation(prompt, MESSAGE_NEXT_CANCELLED,
                confirmedModel -> scheduleConfirmedVisit(confirmedModel, personToUpdate, updatedPerson));
        return CommandResult.requestInput(inputRequest);
    }

    /**
     * Schedules the visit after the user confirms the full-day prompt.
     * Rechecks the times first, since the requested appointment may have passed while the prompt was open.
     *
     * @throws CommandException If the appointment is no longer in the future or can no longer be scheduled.
     */
    private CommandResult scheduleConfirmedVisit(Model model, Person personToUpdate, Person updatedPerson)
            throws CommandException {
        validateVisitDate(personToUpdate);
        return scheduleVisit(model, personToUpdate, updatedPerson);
    }

    /**
     * Replaces the patient captured when this command was first executed while preserving the current list context.
     * Checks again for conflicts, since the address book may have changed while confirmation was pending.
     *
     * @throws CommandException If the captured patient no longer exists or the visit now conflicts.
     */
    private CommandResult scheduleVisit(Model model, Person personToUpdate, Person updatedPerson)
            throws CommandException {
        if (!model.getAddressBook().getPersonList().contains(personToUpdate)) {
            throw new CommandException(MESSAGE_PERSON_NO_LONGER_EXISTS);
        }
        validateNoConflicts(model, personToUpdate, updatedPerson);

        model.setPerson(personToUpdate, updatedPerson);
        logger.fine("Scheduled next visit for " + updatedPerson.getName() + " at " + visitDate);
        return CommandResult.showUpdatedPerson(
                String.format(MESSAGE_SUCCESS, updatedPerson.getName(), visitDate), updatedPerson);
    }

    /**
     * Requires the current visit to be at or before now and the requested visit to be strictly after now.
     * Reads the execution-time clock once for both comparisons, retaining its full precision.
     */
    private void validateVisitDate(Person personToUpdate) throws CommandException {
        if (visitDate.equals(personToUpdate.getVisitDate())) {
            throw new CommandException(String.format(MESSAGE_UNCHANGED_VISIT_DATE,
                    personToUpdate.getName(), visitDate));
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (personToUpdate.getVisitDate().value.isAfter(now)) {
            throw new CommandException(String.format(MESSAGE_CURRENT_VISIT_IN_FUTURE,
                    personToUpdate.getName(), personToUpdate.getVisitDate()));
        }
        if (!visitDate.value.isAfter(now)) {
            throw new CommandException(String.format(MESSAGE_NON_FUTURE_VISIT_DATE, now.format(VisitDate.FORMATTER)));
        }
    }

    /**
     * Checks the full replacement record against every other stored patient's visit record.
     */
    private void validateNoConflicts(Model model, Person personToUpdate, Person updatedPerson) throws CommandException {
        Optional<Person> visitDateDuplicate = DuplicatePersonFinder.findVisitDateDuplicate(
                model.getAddressBook(), updatedPerson, personToUpdate);
        if (visitDateDuplicate.isPresent()) {
            Person conflictingPerson = visitDateDuplicate.get();
            throw new CommandException(
                    String.format(MESSAGE_DUPLICATE_VISIT_DATE, conflictingPerson.getName(), visitDate),
                    conflictingPerson);
        }
    }

    /**
     * Returns whether both commands have the same target, appointment, and validation clock.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof NextCommand otherNextCommand)) {
            return false;
        }
        return index.equals(otherNextCommand.index)
                && visitDate.equals(otherNextCommand.visitDate)
                && clock.equals(otherNextCommand.clock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), visitDate, clock);
    }

    /**
     * Returns a description of the target index and requested appointment.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("visitDate", visitDate).toString();
    }
}
