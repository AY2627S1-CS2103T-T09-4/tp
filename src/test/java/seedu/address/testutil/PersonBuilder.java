package seedu.address.testutil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.model.person.Address;
import seedu.address.model.person.AlcoholHistory;
import seedu.address.model.person.Allergies;
import seedu.address.model.person.BloodType;
import seedu.address.model.person.DateOfBirth;
import seedu.address.model.person.Email;
import seedu.address.model.person.EmergencyContact;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.SmokingStatus;
import seedu.address.model.person.VisitDate;
import seedu.address.model.tag.Tag;
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final String DEFAULT_ADDRESS = "123, Jurong West Ave 6, #08-111";
    public static final String DEFAULT_VISIT_DATE = "18/9/2026 1000";
    public static final String DEFAULT_NOTE = "";

    private Name name;
    private DateOfBirth dateOfBirth;
    private BloodType bloodType;
    private Allergies allergies;
    private AlcoholHistory alcoholHistory;
    private SmokingStatus smokingStatus;
    private Phone phone;
    private Email email;
    private Address address;
    private VisitDate visitDate;
    private List<VisitDate> previousVisitDates;
    private Note note;
    private EmergencyContact emergencyContact;
    private Set<Tag> tags;

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        name = new Name(DEFAULT_NAME);
        dateOfBirth = new DateOfBirth("");
        bloodType = BloodType.UNKNOWN;
        allergies = new Allergies("");
        alcoholHistory = new AlcoholHistory("");
        smokingStatus = SmokingStatus.UNRECORDED;
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        address = new Address(DEFAULT_ADDRESS);
        visitDate = new VisitDate(DEFAULT_VISIT_DATE);
        previousVisitDates = List.of();
        note = new Note(DEFAULT_NOTE);
        emergencyContact = new EmergencyContact("");
        tags = new HashSet<>();
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        name = personToCopy.getName();
        dateOfBirth = personToCopy.getDateOfBirth();
        bloodType = personToCopy.getBloodType();
        allergies = personToCopy.getAllergies();
        alcoholHistory = personToCopy.getAlcoholHistory();
        smokingStatus = personToCopy.getSmokingStatus();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        address = personToCopy.getAddress();
        visitDate = personToCopy.getVisitDate();
        previousVisitDates = List.copyOf(personToCopy.getPreviousVisitDates());
        note = personToCopy.getNote();
        emergencyContact = personToCopy.getEmergencyContact();
        tags = new HashSet<>(personToCopy.getTags());
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Sets the optional date of birth of the patient being built.
     */
    public PersonBuilder withDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = new DateOfBirth(dateOfBirth);
        return this;
    }

    /**
     * Sets the optional blood type of the patient being built.
     */
    public PersonBuilder withBloodType(String bloodType) {
        this.bloodType = BloodType.fromString(bloodType);
        return this;
    }

    /**
     * Sets the optional allergy information of the patient being built.
     */
    public PersonBuilder withAllergies(String allergies) {
        this.allergies = new Allergies(allergies);
        return this;
    }

    /**
     * Sets the optional alcohol history of the patient being built.
     */
    public PersonBuilder withAlcoholHistory(String alcoholHistory) {
        this.alcoholHistory = new AlcoholHistory(alcoholHistory);
        return this;
    }

    /**
     * Sets the optional smoking status of the patient being built.
     */
    public PersonBuilder withSmokingStatus(String smokingStatus) {
        this.smokingStatus = SmokingStatus.fromString(smokingStatus);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the {@code Person} that we are building.
     */
    public PersonBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Address} of the {@code Person} that we are building.
     */
    public PersonBuilder withAddress(String address) {
        this.address = new Address(address);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /**
     * Sets the {@code VisitDate} of the {@code Person} that we are building.
     */
    public PersonBuilder withVisitDate(String visitDate) {
        this.visitDate = new VisitDate(visitDate);
        return this;
    }

    /**
     * Sets the previous visit dates in the supplied order for the {@code Person} being built.
     */
    public PersonBuilder withPreviousVisitDates(String... previousVisitDates) {
        this.previousVisitDates = Arrays.stream(previousVisitDates).map(VisitDate::new).toList();
        return this;
    }

    /**
     * Sets the {@code Note} of the {@code Person} that we are building.
     */
    public PersonBuilder withNote(String note) {
        this.note = new Note(note);
        return this;
    }

    /**
     * Sets the optional emergency contact of the patient being built.
     */
    public PersonBuilder withEmergencyContact(String emergencyContact) {
        this.emergencyContact = new EmergencyContact(emergencyContact);
        return this;
    }

    /**
     * Returns a patient with the configured details.
     */
    public Person build() {
        return new Person(name, dateOfBirth, bloodType, allergies, alcoholHistory, smokingStatus, phone, email,
                address, visitDate, previousVisitDates, note, emergencyContact, tags);
    }

}
