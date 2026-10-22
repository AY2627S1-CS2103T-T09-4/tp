package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AlcoholHistoryTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AlcoholHistory(null));
    }

    @Test
    public void constructor_freeText_trimsOnlySurroundingWhitespace() {
        AlcoholHistory history = new AlcoholHistory(" \tOccasional drinking;  1–2 glasses\n");
        assertEquals("Occasional drinking;  1–2 glasses", history.toString());
        assertFalse(history.isEmpty());
    }

    @Test
    public void constructor_blankHistory_representsUnrecordedValue() {
        assertTrue(new AlcoholHistory("").isEmpty());
        assertTrue(new AlcoholHistory(" \t\n\u2003").isEmpty());
        assertEquals("", new AlcoholHistory(" \t ").toString());
        assertNotEquals(new AlcoholHistory(""), new AlcoholHistory("Does not drink"));
    }

    @Test
    public void equals_normalizedHistory_comparesRecordedText() {
        AlcoholHistory history = new AlcoholHistory("Occasional drinking");
        AlcoholHistory sameHistory = new AlcoholHistory(" Occasional drinking ");
        assertEquals(history, history);
        assertEquals(history, sameHistory);
        assertEquals(history.hashCode(), sameHistory.hashCode());
        assertNotEquals(history, new AlcoholHistory("Does not drink"));
        assertNotEquals(history, "Occasional drinking");
        assertNotEquals(history, null);
    }
}
