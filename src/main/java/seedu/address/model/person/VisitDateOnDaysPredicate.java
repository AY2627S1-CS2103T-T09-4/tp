package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s current visit falls on any of the given calendar days, at any time of day,
 * so that a doctor can list one day's visits to plan the route. Previous visit dates are not searched.
 */
public class VisitDateOnDaysPredicate implements Predicate<Person> {
    private final List<LocalDate> days;

    /**
     * Creates a predicate that matches a person whose current visit is on any of {@code days}.
     */
    public VisitDateOnDaysPredicate(List<LocalDate> days) {
        this.days = List.copyOf(requireNonNull(days));
    }

    @Override
    public boolean test(Person person) {
        LocalDate visitDay = person.getVisitDate().value.toLocalDate();
        return days.contains(visitDay);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof VisitDateOnDaysPredicate otherPredicate)) {
            return false;
        }

        return days.equals(otherPredicate.days);
    }

    @Override
    public int hashCode() {
        return days.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("days", days).toString();
    }
}
