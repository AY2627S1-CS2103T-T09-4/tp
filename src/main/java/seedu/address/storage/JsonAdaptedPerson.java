package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
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

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Patient's %s field is missing!";

    private final String name;
    private final String dateOfBirth;
    private final String bloodType;
    private final String allergies;
    private final String alcoholHistory;
    private final String smokingStatus;
    private final String phone;
    private final String email;
    private final String address;
    private final String visitDate;
    private final List<String> previousVisitDates = new ArrayList<>();
    private final String note;
    private final String emergencyContact;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Constructs an adapted patient from JSON properties, accepting missing optional fields in older files.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name,
            @JsonProperty("dateOfBirth") String dateOfBirth, @JsonProperty("bloodType") String bloodType,
            @JsonProperty("allergies") String allergies, @JsonProperty("alcoholHistory") String alcoholHistory,
            @JsonProperty("smokingStatus") String smokingStatus,
            @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("visitDate") String visitDate,
            @JsonProperty("previousVisitDates") List<String> previousVisitDates,
            @JsonProperty("note") String note, @JsonProperty("emergencyContact") String emergencyContact,
            @JsonProperty("tags") List<JsonAdaptedTag> tags) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.bloodType = bloodType;
        this.allergies = allergies;
        this.alcoholHistory = alcoholHistory;
        this.smokingStatus = smokingStatus;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.visitDate = visitDate;
        if (previousVisitDates != null) {
            this.previousVisitDates.addAll(previousVisitDates);
        }
        this.note = note;
        this.emergencyContact = emergencyContact;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        dateOfBirth = source.getDateOfBirth().toString();
        bloodType = source.getBloodType().toString();
        allergies = source.getAllergies().toString();
        alcoholHistory = source.getAlcoholHistory().toString();
        smokingStatus = source.getSmokingStatus().toString();
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        visitDate = source.getVisitDate().toString();
        previousVisitDates.addAll(source.getPreviousVisitDates().stream()
                .map(VisitDate::toString).toList());
        note = source.getNote().value;
        emergencyContact = source.getEmergencyContact().toString();
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        String storedDateOfBirth = dateOfBirth == null ? "" : dateOfBirth;
        if (!DateOfBirth.isValidDateOfBirth(storedDateOfBirth)) {
            throw new IllegalValueException(DateOfBirth.MESSAGE_CONSTRAINTS);
        }
        final DateOfBirth modelDateOfBirth = new DateOfBirth(storedDateOfBirth);

        final BloodType modelBloodType;
        try {
            modelBloodType = BloodType.fromString(bloodType == null ? "" : bloodType);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(BloodType.MESSAGE_CONSTRAINTS);
        }

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        if (visitDate == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT, VisitDate.class.getSimpleName()));
        }
        if (!VisitDate.isValidVisitDate(visitDate)) {
            throw new IllegalValueException(VisitDate.MESSAGE_CONSTRAINTS);
        }
        final VisitDate modelVisitDate = new VisitDate(visitDate);

        // notes are optional, so a missing note is treated as no notes
        final Note modelNote = new Note(note == null ? "" : note);

        String storedContact = emergencyContact == null ? "" : emergencyContact;
        if (!EmergencyContact.isValidEmergencyContact(storedContact)) {
            throw new IllegalValueException(EmergencyContact.MESSAGE_CONSTRAINTS);
        }
        final EmergencyContact modelEmergencyContact = new EmergencyContact(storedContact);

        final Allergies modelAllergies = new Allergies(allergies == null ? "" : allergies);
        final AlcoholHistory modelAlcoholHistory = new AlcoholHistory(alcoholHistory == null ? "" : alcoholHistory);
        final SmokingStatus modelSmokingStatus;
        try {
            modelSmokingStatus = SmokingStatus.fromString(smokingStatus == null ? "" : smokingStatus);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(SmokingStatus.MESSAGE_CONSTRAINTS, exception);
        }
        final Set<Tag> modelTags = new HashSet<>(personTags);
        final List<VisitDate> modelPreviousVisitDates = parsePreviousVisitDates();
        try {
            return new Person(modelName, modelDateOfBirth, modelBloodType, modelAllergies, modelAlcoholHistory,
                    modelSmokingStatus, modelPhone, modelEmail, modelAddress, modelVisitDate, modelPreviousVisitDates,
                    modelNote, modelEmergencyContact, modelTags);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(Person.MESSAGE_PREVIOUS_VISIT_DATES_CONSTRAINTS, e);
        }
    }

    /**
     * Converts the stored previous-date strings into {@code VisitDate} objects.
     *
     * @throws IllegalValueException If any previous date is null or malformed.
     */
    private List<VisitDate> parsePreviousVisitDates() throws IllegalValueException {
        List<VisitDate> modelPreviousVisitDates = new ArrayList<>();
        for (String previousVisitDate : previousVisitDates) {
            if (previousVisitDate == null || !VisitDate.isValidVisitDate(previousVisitDate)) {
                throw new IllegalValueException("Invalid previous visit date. " + VisitDate.MESSAGE_CONSTRAINTS);
            }
            modelPreviousVisitDates.add(new VisitDate(previousVisitDate));
        }
        return modelPreviousVisitDates;
    }
}
