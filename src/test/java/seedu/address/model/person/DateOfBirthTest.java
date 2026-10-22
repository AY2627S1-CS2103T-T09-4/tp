package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

public class DateOfBirthTest {

    @Test
    public void constructor_invalidDate_throwsException() {
        assertThrows(NullPointerException.class, () -> new DateOfBirth(null));
        for (String invalid : List.of(" ", "31/4/2000", "29/2/1900", "1/13/2000", "0/1/2000",
                "1990-06-15", "15/6/90", "1/1/0000", "1/1/-001", "1/1/10000", "15/6/1990 1000")) {
            assertFalse(DateOfBirth.isValidDateOfBirth(invalid), invalid);
            assertThrows(IllegalArgumentException.class, () -> new DateOfBirth(invalid));
        }
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String futureDate = tomorrow.getDayOfMonth() + "/" + tomorrow.getMonthValue() + "/" + tomorrow.getYear();
        assertThrows(IllegalArgumentException.class, () -> new DateOfBirth(futureDate));
    }

    @Test
    public void constructor_validDate_normalizesDate() {
        assertEquals("15/6/1990", new DateOfBirth("15/06/1990").toString());
        assertTrue(DateOfBirth.isValidDateOfBirth("29/2/2000"));
        assertTrue(DateOfBirth.isValidDateOfBirth("1/1/0001"));
        LocalDate today = LocalDate.now();
        DateOfBirth newborn = new DateOfBirth(today.getDayOfMonth() + "/" + today.getMonthValue()
                + "/" + today.getYear());
        assertEquals(0, newborn.getAge(today));
        assertEquals(newborn + " (0 years old)", newborn.toDisplayString());
    }

    @Test
    public void getAge_beforeOnAndAfterBirthday_countsCompletedYears() {
        DateOfBirth dateOfBirth = new DateOfBirth("15/6/1990");
        assertEquals(35, dateOfBirth.getAge(LocalDate.of(2026, 6, 14)));
        assertEquals(36, dateOfBirth.getAge(LocalDate.of(2026, 6, 15)));
        assertEquals(36, dateOfBirth.getAge(LocalDate.of(2026, 6, 16)));
        assertThrows(IllegalArgumentException.class, () -> dateOfBirth.getAge(LocalDate.of(1989, 1, 1)));
        assertThrows(NullPointerException.class, () -> dateOfBirth.getAge(null));
    }

    @Test
    public void getAge_leapDayBirthday_countsCompletedYears() {
        DateOfBirth dateOfBirth = new DateOfBirth("29/2/2000");
        assertEquals(24, dateOfBirth.getAge(LocalDate.of(2025, 2, 28)));
        assertEquals(25, dateOfBirth.getAge(LocalDate.of(2025, 3, 1)));
        assertEquals(24, dateOfBirth.getAge(LocalDate.of(2024, 2, 29)));
    }

    @Test
    public void emptyDate_hasNoDisplayOrAge() {
        DateOfBirth empty = new DateOfBirth("");
        assertTrue(empty.isEmpty());
        assertEquals("", empty.toString());
        assertEquals("", empty.toDisplayString());
        assertThrows(IllegalArgumentException.class, () -> empty.getAge(LocalDate.now()));
    }

    @Test
    public void equals_normalizedDates_comparesDateValues() {
        DateOfBirth dateOfBirth = new DateOfBirth("15/6/1990");
        DateOfBirth sameDate = new DateOfBirth("15/06/1990");
        assertEquals(dateOfBirth, dateOfBirth);
        assertEquals(dateOfBirth, sameDate);
        assertEquals(dateOfBirth.hashCode(), sameDate.hashCode());
        assertNotEquals(dateOfBirth, new DateOfBirth("16/6/1990"));
        assertNotEquals(dateOfBirth, new DateOfBirth(""));
        assertNotEquals(dateOfBirth, null);
        assertNotEquals(dateOfBirth, "15/6/1990");
    }
}
