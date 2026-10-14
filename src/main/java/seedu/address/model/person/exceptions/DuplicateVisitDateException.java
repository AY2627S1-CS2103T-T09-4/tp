package seedu.address.model.person.exceptions;

/**
 * Signals that the operation will assign the same visit date and time to multiple patients.
 */
public class DuplicateVisitDateException extends RuntimeException {
    public DuplicateVisitDateException() {
        super("Operation would result in duplicate visit dates");
    }
}
