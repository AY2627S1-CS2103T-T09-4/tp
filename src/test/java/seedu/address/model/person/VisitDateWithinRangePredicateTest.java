package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class VisitDateWithinRangePredicateTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 10, 27);
    private static final LocalDate END_DATE = START_DATE.plusDays(7);

    private final VisitDateWithinRangePredicate predicate =
            new VisitDateWithinRangePredicate(START_DATE, END_DATE);

    @Test
    public void test_visitWithinRange_returnsTrue() {
        assertTrue(predicate.test(personWithVisit(START_DATE, 0)));
        assertTrue(predicate.test(personWithVisit(START_DATE.plusDays(3), 12)));
        assertTrue(predicate.test(personWithVisit(END_DATE.minusDays(1), 23)));
    }

    @Test
    public void test_visitOutsideRange_returnsFalse() {
        assertFalse(predicate.test(personWithVisit(START_DATE.minusDays(1), 23)));
        assertFalse(predicate.test(personWithVisit(END_DATE, 0)));
    }

    @Test
    public void constructor_invalidArguments_throwsException() {
        assertThrows(NullPointerException.class,
                () -> new VisitDateWithinRangePredicate(null, END_DATE));
        assertThrows(NullPointerException.class,
                () -> new VisitDateWithinRangePredicate(START_DATE, null));
        assertThrows(IllegalArgumentException.class,
                () -> new VisitDateWithinRangePredicate(START_DATE, START_DATE));
        assertThrows(IllegalArgumentException.class,
                () -> new VisitDateWithinRangePredicate(START_DATE, START_DATE.minusDays(1)));
        assertThrows(NullPointerException.class, () -> predicate.test(null));
    }

    @Test
    public void equals() {
        VisitDateWithinRangePredicate equivalentPredicate =
                new VisitDateWithinRangePredicate(START_DATE, END_DATE);

        assertEquals(predicate, predicate);
        assertEquals(predicate, equivalentPredicate);
        assertEquals(predicate.hashCode(), equivalentPredicate.hashCode());
        assertNotEquals(predicate,
                new VisitDateWithinRangePredicate(START_DATE.plusDays(1), END_DATE));
        assertNotEquals(predicate,
                new VisitDateWithinRangePredicate(START_DATE, END_DATE.plusDays(1)));
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, "not a predicate");
    }

    @Test
    public void toStringMethod() {
        String expected = VisitDateWithinRangePredicate.class.getCanonicalName()
                + "{startDate=" + START_DATE + ", endDate=" + END_DATE + "}";

        assertEquals(expected, predicate.toString());
    }

    private static Person personWithVisit(LocalDate date, int hour) {
        return new PersonBuilder()
                .withVisitDate(date.atTime(hour, 0).format(VisitDate.FORMATTER))
                .build();
    }
}
