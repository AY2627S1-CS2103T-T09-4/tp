package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

/**
 * Tests the counting and delegation contract of {@link CountingClock}.
 */
public class CountingClockTest {

    private static final Instant INITIAL_INSTANT = Instant.parse("2026-10-14T04:00:30Z");
    private static final ZoneId INITIAL_ZONE = ZoneId.of("Asia/Singapore");

    @Test
    public void instant_initialRead_returnsInstantAndIncrementsCounter() {
        CountingClock clock = new CountingClock(Clock.fixed(INITIAL_INSTANT, INITIAL_ZONE));

        assertEquals(INITIAL_ZONE, clock.getZone());
        assertEquals(0, clock.getReadCount());
        assertEquals(INITIAL_INSTANT, clock.instant());
        assertEquals(1, clock.getReadCount());
    }

    @Test
    public void withZone_existingRead_returnsIndependentCounter() {
        CountingClock clock = new CountingClock(Clock.fixed(INITIAL_INSTANT, INITIAL_ZONE));
        clock.instant();

        CountingClock zonedClock = (CountingClock) clock.withZone(ZoneId.of("UTC"));

        assertEquals(1, clock.getReadCount());
        assertEquals(0, zonedClock.getReadCount());
        assertEquals(ZoneId.of("UTC"), zonedClock.getZone());
    }

    @Test
    public void setDelegate_existingRead_preservesAccumulatedCount() {
        CountingClock clock = new CountingClock(Clock.fixed(INITIAL_INSTANT, INITIAL_ZONE));
        clock.instant();
        clock.setDelegate(Clock.offset(Clock.fixed(INITIAL_INSTANT, INITIAL_ZONE), Duration.ofDays(1)));

        assertEquals(1, clock.getReadCount());
        assertEquals(INITIAL_INSTANT.plus(Duration.ofDays(1)), clock.instant());
        assertEquals(2, clock.getReadCount());
    }
}
