package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    public static final String MESSAGE_PREVIOUS_VISIT_DATES_CONSTRAINTS =
            "Previous visit dates must be strictly increasing and earlier than the current visit date.";

    // Identity fields
    private final Name name;
    private final Phone phone;

    // Data fields
    private final Email email;
    private final Address address;
    private final VisitDate visitDate;
    private final List<VisitDate> previousVisitDates;
    private final Note note;
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Creates a patient with no previous visit dates. Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, VisitDate visitDate, Note note,
            Set<Tag> tags) {
        this(name, phone, email, address, visitDate, List.of(), note, tags);
    }

    /**
     * Creates a patient with an immutable copy of the previous visit dates, ordered oldest first.
     *
     * @throws NullPointerException If any field or previous visit date is null.
     * @throws IllegalArgumentException If previous dates are not strictly increasing or any is at or after
     *     the current visit date.
     */
    public Person(Name name, Phone phone, Email email, Address address, VisitDate visitDate,
            List<VisitDate> previousVisitDates, Note note, Set<Tag> tags) {
        requireAllNonNull(name, phone, email, address, visitDate, previousVisitDates, note, tags);
        List<VisitDate> copiedPreviousVisitDates = List.copyOf(previousVisitDates);
        validatePreviousVisitDates(copiedPreviousVisitDates, visitDate);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.visitDate = visitDate;
        this.previousVisitDates = copiedPreviousVisitDates;
        this.note = note;
        this.tags.addAll(tags);
    }

    /**
     * Checks each previous date against its successor, ending with the required current visit date.
     */
    private static void validatePreviousVisitDates(List<VisitDate> previousVisitDates, VisitDate visitDate) {
        VisitDate laterVisitDate = visitDate;
        for (int i = previousVisitDates.size() - 1; i >= 0; i--) {
            VisitDate previousVisitDate = previousVisitDates.get(i);
            checkArgument(previousVisitDate.value.isBefore(laterVisitDate.value),
                    MESSAGE_PREVIOUS_VISIT_DATES_CONSTRAINTS);
            laterVisitDate = previousVisitDate;
        }
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public VisitDate getVisitDate() {
        return visitDate;
    }

    /**
     * Returns the immutable previous visit dates, ordered oldest first.
     */
    public List<VisitDate> getPreviousVisitDates() {
        return previousVisitDates;
    }

    /**
     * Returns true if the two patients share any current or previous visit datetime.
     *
     * @throws NullPointerException If {@code other} is null.
     */
    public boolean hasCommonVisitDate(Person other) {
        requireNonNull(other);
        if (visitDate.equals(other.visitDate) || previousVisitDates.contains(other.visitDate)
                || other.previousVisitDates.contains(visitDate)) {
            return true;
        }
        return previousVisitDates.stream().anyMatch(other.previousVisitDates::contains);
    }

    public Note getNote() {
        return note;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true if both persons have the same normalized name and phone number.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().isSameName(getName())
                && otherPerson.getPhone().equals(getPhone());
    }

    /**
     * Returns true if both persons have the same normalized name.
     */
    public boolean hasSameName(Person otherPerson) {
        return otherPerson != null && otherPerson.getName().isSameName(getName());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && visitDate.equals(otherPerson.visitDate)
                && previousVisitDates.equals(otherPerson.previousVisitDates)
                && note.equals(otherPerson.note)
                && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, visitDate, previousVisitDates, note, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("visitDate", visitDate)
                .add("previousVisitDates", previousVisitDates)
                .add("note", note)
                .add("tags", tags)
                .toString();
    }

}
