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
    private final DateOfBirth dateOfBirth;
    private final BloodType bloodType;
    private final Allergies allergies;
    private final AlcoholHistory alcoholHistory;
    private final SmokingStatus smokingStatus;
    private final Email email;
    private final Address address;
    private final VisitDate visitDate;
    private final List<VisitDate> previousVisitDates;
    private final Note note;
    private final EmergencyContact emergencyContact;
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Creates a patient with no previous visit dates. Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, VisitDate visitDate, Note note,
            Set<Tag> tags) {
        this(name, phone, email, address, visitDate, List.of(), note, new EmergencyContact(""), tags);
    }

    /**
     * Creates a patient with an optional emergency contact and no previous visit dates.
     */
    public Person(Name name, Phone phone, Email email, Address address, VisitDate visitDate, Note note,
            EmergencyContact emergencyContact, Set<Tag> tags) {
        this(name, phone, email, address, visitDate, List.of(), note, emergencyContact, tags);
    }

    /**
     * Creates a patient with previous visit dates and no emergency contact.
     */
    public Person(Name name, Phone phone, Email email, Address address, VisitDate visitDate,
            List<VisitDate> previousVisitDates, Note note, Set<Tag> tags) {
        this(name, phone, email, address, visitDate, previousVisitDates, note, new EmergencyContact(""), tags);
    }

    /**
     * Creates a patient with an immutable copy of previous visit dates and an optional emergency contact.
     * Previous dates are ordered oldest first.
     *
     * @throws NullPointerException If any field or previous visit date is null.
     * @throws IllegalArgumentException If previous dates are not strictly increasing or any is at or after
     *     the current visit date.
     */
    public Person(Name name, Phone phone, Email email, Address address, VisitDate visitDate,
            List<VisitDate> previousVisitDates, Note note, EmergencyContact emergencyContact, Set<Tag> tags) {
        this(name, BloodType.UNKNOWN, phone, email, address, visitDate, previousVisitDates, note,
                emergencyContact, tags);
    }

    /**
     * Creates a patient with an optional blood type, no previous visits, and no emergency contact.
     */
    public Person(Name name, BloodType bloodType, Phone phone, Email email, Address address,
            VisitDate visitDate, Note note, Set<Tag> tags) {
        this(name, bloodType, phone, email, address, visitDate, List.of(), note, new EmergencyContact(""), tags);
    }

    /**
     * Creates a patient with an optional date of birth and no other optional details.
     */
    public Person(Name name, DateOfBirth dateOfBirth, Phone phone, Email email, Address address,
            VisitDate visitDate, Note note, Set<Tag> tags) {
        this(name, dateOfBirth, BloodType.UNKNOWN, phone, email, address, visitDate, List.of(), note,
                new EmergencyContact(""), tags);
    }

    /**
     * Creates a patient with blood type, visit history, and an emergency contact but no date of birth.
     */
    public Person(Name name, BloodType bloodType, Phone phone, Email email, Address address, VisitDate visitDate,
            List<VisitDate> previousVisitDates, Note note, EmergencyContact emergencyContact, Set<Tag> tags) {
        this(name, new DateOfBirth(""), bloodType, phone, email, address, visitDate, previousVisitDates, note,
                emergencyContact, tags);
    }

    /**
     * Creates a patient without recorded alcohol history.
     */
    public Person(Name name, DateOfBirth dateOfBirth, BloodType bloodType, Phone phone, Email email,
            Address address, VisitDate visitDate, List<VisitDate> previousVisitDates, Note note,
            EmergencyContact emergencyContact, Set<Tag> tags) {
        this(name, dateOfBirth, bloodType, new AlcoholHistory(""), phone, email, address, visitDate,
                previousVisitDates, note, emergencyContact, tags);
    }

    /**
     * Creates a patient without recorded smoking status.
     */
    public Person(Name name, DateOfBirth dateOfBirth, BloodType bloodType, AlcoholHistory alcoholHistory,
            Phone phone, Email email, Address address, VisitDate visitDate, List<VisitDate> previousVisitDates,
            Note note, EmergencyContact emergencyContact, Set<Tag> tags) {
        this(name, dateOfBirth, bloodType, alcoholHistory, SmokingStatus.UNRECORDED, phone, email, address,
                visitDate, previousVisitDates, note, emergencyContact, tags);
    }

    /**
     * Creates a patient without recorded allergy information.
     */
    public Person(Name name, DateOfBirth dateOfBirth, BloodType bloodType, AlcoholHistory alcoholHistory,
            SmokingStatus smokingStatus, Phone phone, Email email, Address address, VisitDate visitDate,
            List<VisitDate> previousVisitDates, Note note, EmergencyContact emergencyContact, Set<Tag> tags) {
        this(name, dateOfBirth, bloodType, new Allergies(""), alcoholHistory, smokingStatus, phone, email,
                address, visitDate, previousVisitDates, note, emergencyContact, tags);
    }

    /**
     * Creates a patient with all optional details and an immutable oldest-first visit history.
     *
     * @throws NullPointerException If any field or previous visit date is null.
     * @throws IllegalArgumentException If previous dates are not strictly increasing or any is at or after
     *     the current visit date.
     */
    public Person(Name name, DateOfBirth dateOfBirth, BloodType bloodType, Allergies allergies,
            AlcoholHistory alcoholHistory, SmokingStatus smokingStatus, Phone phone, Email email, Address address,
            VisitDate visitDate, List<VisitDate> previousVisitDates, Note note, EmergencyContact emergencyContact,
            Set<Tag> tags) {
        requireAllNonNull(name, dateOfBirth, bloodType, allergies, alcoholHistory, smokingStatus, phone, email,
                address, visitDate, previousVisitDates, note, emergencyContact, tags);
        this.dateOfBirth = dateOfBirth;
        this.bloodType = bloodType;
        this.allergies = allergies;
        this.alcoholHistory = alcoholHistory;
        this.smokingStatus = smokingStatus;
        List<VisitDate> copiedPreviousVisitDates = List.copyOf(previousVisitDates);
        validatePreviousVisitDates(copiedPreviousVisitDates, visitDate);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.visitDate = visitDate;
        this.previousVisitDates = copiedPreviousVisitDates;
        this.note = note;
        this.emergencyContact = emergencyContact;
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

    public DateOfBirth getDateOfBirth() {
        return dateOfBirth;
    }

    public BloodType getBloodType() {
        return bloodType;
    }

    public Allergies getAllergies() {
        return allergies;
    }

    public AlcoholHistory getAlcoholHistory() {
        return alcoholHistory;
    }

    public SmokingStatus getSmokingStatus() {
        return smokingStatus;
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
                && dateOfBirth.equals(otherPerson.dateOfBirth)
                && bloodType.equals(otherPerson.bloodType)
                && allergies.equals(otherPerson.allergies)
                && alcoholHistory.equals(otherPerson.alcoholHistory)
                && smokingStatus.equals(otherPerson.smokingStatus)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && visitDate.equals(otherPerson.visitDate)
                && previousVisitDates.equals(otherPerson.previousVisitDates)
                && note.equals(otherPerson.note)
                && emergencyContact.equals(otherPerson.emergencyContact)
                && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, dateOfBirth, bloodType, allergies, alcoholHistory, smokingStatus, phone, email,
                address, visitDate, previousVisitDates, note, emergencyContact, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("dateOfBirth", dateOfBirth)
                .add("bloodType", bloodType)
                .add("allergies", allergies)
                .add("alcoholHistory", alcoholHistory)
                .add("smokingStatus", smokingStatus)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("visitDate", visitDate)
                .add("previousVisitDates", previousVisitDates)
                .add("note", note)
                .add("emergencyContact", emergencyContact)
                .add("tags", tags)
                .toString();
    }

}
