package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class VisitDateTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new VisitDate(null));
    }

    @Test
    public void constructor_invalidVisitDate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new VisitDate(""));
    }

    @Test
    public void isValidVisitDate() {
        // null visit date
        assertThrows(NullPointerException.class, () -> VisitDate.isValidVisitDate(null));

        // invalid visit dates
        assertFalse(VisitDate.isValidVisitDate("")); // empty string
        assertFalse(VisitDate.isValidVisitDate(" ")); // spaces only
        assertFalse(VisitDate.isValidVisitDate("18/9/2026")); // missing time
        assertFalse(VisitDate.isValidVisitDate("1000")); // missing date
        assertFalse(VisitDate.isValidVisitDate("2026-09-18 1000")); // wrong date format
        assertFalse(VisitDate.isValidVisitDate("18/9/2026 10:00")); // wrong time format
        assertFalse(VisitDate.isValidVisitDate("31/2/2026 1000")); // date does not exist
        assertFalse(VisitDate.isValidVisitDate("29/2/2026 1000")); // not a leap year
        assertFalse(VisitDate.isValidVisitDate("18/13/2026 1000")); // invalid month
        assertFalse(VisitDate.isValidVisitDate("18/9/2026 2400")); // invalid hour
        assertFalse(VisitDate.isValidVisitDate("18/9/2026 1060")); // invalid minute

        // valid visit dates
        assertTrue(VisitDate.isValidVisitDate("18/9/2026 1000"));
        assertTrue(VisitDate.isValidVisitDate("18/09/2026 1000")); // leading zero in month
        assertTrue(VisitDate.isValidVisitDate("1/1/2020 0000")); // past date
        assertTrue(VisitDate.isValidVisitDate("29/2/2028 2359")); // leap year
    }

    @Test
    public void toStringMethod() {
        assertEquals("18/9/2026 1000", new VisitDate("18/09/2026 1000").toString());
    }

    @Test
    public void equals() {
        VisitDate visitDate = new VisitDate("18/9/2026 1000");

        // same values -> returns true
        assertTrue(visitDate.equals(new VisitDate("18/9/2026 1000")));
        assertTrue(visitDate.equals(new VisitDate("18/09/2026 1000")));

        // same object -> returns true
        assertTrue(visitDate.equals(visitDate));

        // null -> returns false
        assertFalse(visitDate.equals(null));

        // different types -> returns false
        assertFalse(visitDate.equals(5.0f));

        // different values -> returns false
        assertFalse(visitDate.equals(new VisitDate("18/9/2026 1100")));
    }
}
