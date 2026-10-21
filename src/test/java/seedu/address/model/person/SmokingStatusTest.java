package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class SmokingStatusTest {

    @Test
    public void fromString_supportedStatuses_normalizesCaseAndWhitespace() {
        for (SmokingStatus status : List.of(SmokingStatus.NEVER, SmokingStatus.FORMER,
                SmokingStatus.CURRENT, SmokingStatus.UNKNOWN)) {
            assertEquals(status,
                    SmokingStatus.fromString(" \t" + status.toString().toUpperCase(Locale.ROOT) + "\u2003"));
            assertFalse(status.isEmpty());
        }
        assertEquals("never", SmokingStatus.NEVER.toString());
        assertEquals("former", SmokingStatus.FORMER.toString());
        assertEquals("current", SmokingStatus.CURRENT.toString());
        assertEquals("unknown", SmokingStatus.UNKNOWN.toString());
    }

    @Test
    public void fromString_blankStatus_distinguishesUnrecordedAndUnknown() {
        for (String blank : List.of("", "   ", " \t\n\u2003")) {
            assertEquals(SmokingStatus.UNRECORDED, SmokingStatus.fromString(blank));
        }
        assertTrue(SmokingStatus.UNRECORDED.isEmpty());
        assertEquals("", SmokingStatus.UNRECORDED.toString());
        assertNotEquals(SmokingStatus.UNRECORDED, SmokingStatus.UNKNOWN);
        assertFalse(SmokingStatus.UNKNOWN.isEmpty());
    }

    @Test
    public void fromString_unsupportedOrNullStatus_throwsException() {
        assertThrows(NullPointerException.class, () -> SmokingStatus.fromString(null));
        for (String invalid : List.of("daily", "yes", "no", "never smoker", "unrecorded", "cur rent")) {
            assertThrows(IllegalArgumentException.class, SmokingStatus.MESSAGE_CONSTRAINTS, () ->
                    SmokingStatus.fromString(invalid));
        }
    }
}
