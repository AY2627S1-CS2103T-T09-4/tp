package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s current visit falls within a range of calendar days.
 * The start is inclusive and the end is exclusive.
 */
public class VisitDateWithinRangePredicate implements Predicate<Person> {

    private static final String MESSAGE_INVALID_RANGE = "The end date must be after the start date.";

    private final LocalDate startDate;
    private final LocalDate endDate;

    /**
     * Creates a predicate for visits from {@code startDate}, inclusive, to {@code endDate}, exclusive.
     */
    public VisitDateWithinRangePredicate(LocalDate startDate, LocalDate endDate) {
        requireNonNull(startDate);
        requireNonNull(endDate);
        checkArgument(startDate.isBefore(endDate), MESSAGE_INVALID_RANGE);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        LocalDate visitDate = person.getVisitDate().value.toLocalDate();
        return !visitDate.isBefore(startDate) && visitDate.isBefore(endDate);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof VisitDateWithinRangePredicate otherPredicate)) {
            return false;
        }
        return startDate.equals(otherPredicate.startDate) && endDate.equals(otherPredicate.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("startDate", startDate)
                .add("endDate", endDate)
                .toString();
    }
}
