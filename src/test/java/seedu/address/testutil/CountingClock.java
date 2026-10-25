package seedu.address.testutil;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * A test clock that counts calls to {@link #instant()} while delegating time to another clock.
 */
public class CountingClock extends Clock {

    private Clock delegate;
    private int readCount;

    /**
     * Creates a counting clock backed by the supplied delegate.
     *
     * @param delegate Clock that supplies the current instant.
     */
    public CountingClock(Clock delegate) {
        this.delegate = requireNonNull(delegate);
    }

    /**
     * Replaces the delegate without resetting the accumulated read count.
     *
     * @param delegate Clock that should supply subsequent instants.
     */
    public void setDelegate(Clock delegate) {
        this.delegate = requireNonNull(delegate);
    }

    /**
     * Returns the number of calls made to {@link #instant()}.
     */
    public int getReadCount() {
        return readCount;
    }

    @Override
    public ZoneId getZone() {
        return delegate.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new CountingClock(delegate.withZone(zone));
    }

    /**
     * Returns the delegated instant and increments the read count exactly once.
     */
    @Override
    public Instant instant() {
        readCount++;
        return delegate.instant();
    }
}
