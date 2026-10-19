package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;

    // Data fields
    private final Email email;
    private final Address address;
    private final VisitDate visitDate;
    private final Note note;
    private final EmergencyContact emergencyContact;
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, VisitDate visitDate, Note note,
            Set<Tag> tags) {
        this(name, phone, email, address, visitDate, note, new EmergencyContact(""), tags);
    }

    /**
     * Creates a patient with an optional emergency contact. Every field must be non-null.
     */
    public Person(Name name, Phone phone, Email email, Address address, VisitDate visitDate, Note note,
            EmergencyContact emergencyContact, Set<Tag> tags) {
        requireAllNonNull(name, phone, email, address, visitDate, note, emergencyContact, tags);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.visitDate = visitDate;
        this.note = note;
        this.emergencyContact = emergencyContact;
        this.tags.addAll(tags);
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

    public EmergencyContact getEmergencyContact() {
        return emergencyContact;
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
                && note.equals(otherPerson.note)
                && emergencyContact.equals(otherPerson.emergencyContact)
                && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, visitDate, note, emergencyContact, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("visitDate", visitDate)
                .add("note", note)
                .add("emergencyContact", emergencyContact)
                .add("tags", tags)
                .toString();
    }

}
