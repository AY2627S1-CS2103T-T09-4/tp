package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AllergiesTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Allergies(null));
    }

    @Test
    public void constructor_freeText_trimsOnlySurroundingWhitespace() {
        Allergies allergies = new Allergies(" \tPenicillin;  latex (rash)\n");
        assertEquals("Penicillin;  latex (rash)", allergies.toString());
        assertFalse(allergies.isEmpty());
    }

    @Test
    public void constructor_blankAllergies_representsUnrecordedValue() {
        assertTrue(new Allergies("").isEmpty());
        assertTrue(new Allergies(" \t\n\u2003").isEmpty());
        assertEquals("", new Allergies(" \t ").toString());
        assertNotEquals(new Allergies(""), new Allergies("No known allergies"));
    }

    @Test
    public void equals_normalizedAllergies_comparesRecordedText() {
        Allergies allergies = new Allergies("Penicillin; latex");
        Allergies sameAllergies = new Allergies(" Penicillin; latex ");
        assertEquals(allergies, allergies);
        assertEquals(allergies, sameAllergies);
        assertEquals(allergies.hashCode(), sameAllergies.hashCode());
        assertNotEquals(allergies, new Allergies("No known allergies"));
        assertNotEquals(allergies, "Penicillin; latex");
        assertNotEquals(allergies, null);
    }
}
