package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class BloodTypeTest {

    @Test
    public void fromString_validBloodTypes_normalizesValues() {
        for (BloodType bloodType : BloodType.values()) {
            assertEquals(bloodType, BloodType.fromString(bloodType.toString()));
            assertEquals(bloodType, BloodType.fromString(" " + bloodType.toString().toLowerCase(Locale.ROOT) + "\t"));
        }
        assertEquals(BloodType.AB_NEGATIVE, BloodType.fromString("ab-"));
        assertEquals("AB-", BloodType.AB_NEGATIVE.toString());
    }

    @Test
    public void fromString_invalidBloodTypes_throwsException() {
        assertThrows(NullPointerException.class, () -> BloodType.fromString(null));
        for (String invalid : List.of("A", "B", "AB", "O", "C+", "A++", "A +", "A_POSITIVE", "UNKNOWN", "0-")) {
            assertThrows(IllegalArgumentException.class, BloodType.MESSAGE_CONSTRAINTS, () ->
                    BloodType.fromString(invalid));
        }
    }

    @Test
    public void isEmpty_unrecordedBloodType_hasNoDisplayValue() {
        assertEquals(BloodType.UNKNOWN, BloodType.fromString(""));
        assertEquals(BloodType.UNKNOWN, BloodType.fromString(" \t "));
        assertTrue(BloodType.UNKNOWN.isEmpty());
        assertEquals("", BloodType.UNKNOWN.toString());
        for (BloodType bloodType : BloodType.values()) {
            if (bloodType != BloodType.UNKNOWN) {
                assertFalse(bloodType.isEmpty());
            }
        }
    }
}
