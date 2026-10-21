package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class VisitDateOnDaysPredicateTest {

    private static final LocalDate VISIT_DAY = LocalDate.of(2026, 9, 18);

    private final Person person = new PersonBuilder().withVisitDate("18/9/2026 1000")
            .withPreviousVisitDates("17/9/2026 1000").build();

    @Test
    public void test_visitOnGivenDay_returnsTrue() {
        assertTrue(new VisitDateOnDaysPredicate(List.of(VISIT_DAY)).test(person));

        // only one day matches
        assertTrue(new VisitDateOnDaysPredicate(List.of(VISIT_DAY.plusDays(1), VISIT_DAY)).test(person));

        // first and last minute of the day
        Person earlyVisit = new PersonBuilder().withVisitDate("18/9/2026 0000").build();
        Person lateVisit = new PersonBuilder().withVisitDate("18/9/2026 2359").build();
        assertTrue(new VisitDateOnDaysPredicate(List.of(VISIT_DAY)).test(earlyVisit));
        assertTrue(new VisitDateOnDaysPredicate(List.of(VISIT_DAY)).test(lateVisit));
    }

    @Test
    public void test_visitNotOnGivenDay_returnsFalse() {
        // zero days
        assertFalse(new VisitDateOnDaysPredicate(List.of()).test(person));

        // neighboring days and the same day in another month or year
        assertFalse(new VisitDateOnDaysPredicate(List.of(VISIT_DAY.plusDays(1))).test(person));
        assertFalse(new VisitDateOnDaysPredicate(List.of(VISIT_DAY.plusMonths(1))).test(person));
        assertFalse(new VisitDateOnDaysPredicate(List.of(VISIT_DAY.plusYears(1))).test(person));

        // previous visit dates are not searched
        assertFalse(new VisitDateOnDaysPredicate(List.of(VISIT_DAY.minusDays(1))).test(person));
    }

    @Test
    public void constructor_daysChangedAfterCreation_keepsOriginalDays() {
        List<LocalDate> days = new ArrayList<>(List.of(VISIT_DAY));
        VisitDateOnDaysPredicate predicate = new VisitDateOnDaysPredicate(days);
        days.clear();

        assertTrue(predicate.test(person));
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new VisitDateOnDaysPredicate(null));
    }

    @Test
    public void equals() {
        VisitDateOnDaysPredicate predicate = new VisitDateOnDaysPredicate(List.of(VISIT_DAY));

        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new VisitDateOnDaysPredicate(List.of(VISIT_DAY))));
        assertEquals(predicate.hashCode(), new VisitDateOnDaysPredicate(List.of(VISIT_DAY)).hashCode());
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals(new VisitDateOnDaysPredicate(List.of(VISIT_DAY.plusDays(1)))));
        assertFalse(predicate.equals(new NameContainsKeywordsPredicate(List.of("18/9/2026"))));
    }

    @Test
    public void toStringMethod() {
        String expected = VisitDateOnDaysPredicate.class.getCanonicalName() + "{days=" + List.of(VISIT_DAY) + "}";
        assertEquals(expected, new VisitDateOnDaysPredicate(List.of(VISIT_DAY)).toString());
    }
}
