package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Optional;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

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
     * Returns the first patient whose visit record conflicts with {@code candidate}, if one exists.
     */
    static Optional<Person> findVisitDateDuplicate(ReadOnlyAddressBook addressBook, Person candidate) {
        requireAllNonNull(addressBook, candidate);
        return addressBook.getPersonList().stream()
                .filter(candidate::hasCommonVisitDate)
                .findFirst();
    }

    /**
     * Returns the first patient other than {@code excludedPerson} whose visit record conflicts with
     * {@code candidate}, if one exists.
     */
    static Optional<Person> findVisitDateDuplicate(ReadOnlyAddressBook addressBook, Person candidate,
            Person excludedPerson) {
        requireAllNonNull(addressBook, candidate, excludedPerson);
        return addressBook.getPersonList().stream()
                .filter(person -> !person.equals(excludedPerson))
                .filter(candidate::hasCommonVisitDate)
                .findFirst();
    }
}
