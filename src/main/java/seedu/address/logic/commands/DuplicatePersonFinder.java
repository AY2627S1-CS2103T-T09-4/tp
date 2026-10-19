package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Optional;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;

/**
 * Finds existing patients that conflict with a proposed patient record or visit date.
 */
final class DuplicatePersonFinder {

    private DuplicatePersonFinder() {}

    /**
     * Returns the first patient with the same identity as {@code candidate}, if one exists.
     */
    static Optional<Person> findIdentityDuplicate(ReadOnlyAddressBook addressBook, Person candidate) {
        requireAllNonNull(addressBook, candidate);
        return addressBook.getPersonList().stream()
                .filter(candidate::isSamePerson)
                .findFirst();
    }

    /**
     * Returns all patients with the same normalized name as {@code candidate} in stored order.
     */
    static List<Person> findNameDuplicates(ReadOnlyAddressBook addressBook, Person candidate) {
        requireAllNonNull(addressBook, candidate);
        return addressBook.getPersonList().stream()
                .filter(candidate::hasSameName)
                .toList();
    }

    /**
     * Returns the first patient scheduled at {@code visitDate}, if one exists.
     */
    static Optional<Person> findVisitDateDuplicate(ReadOnlyAddressBook addressBook, VisitDate visitDate) {
        requireAllNonNull(addressBook, visitDate);
        return addressBook.getPersonList().stream()
                .filter(person -> visitDate.equals(person.getVisitDate()))
                .findFirst();
    }
}
