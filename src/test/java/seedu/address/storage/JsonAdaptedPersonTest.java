package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Address;
import seedu.address.model.person.BloodType;
import seedu.address.model.person.DateOfBirth;
import seedu.address.model.person.Email;
import seedu.address.model.person.EmergencyContact;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.SmokingStatus;
import seedu.address.model.person.VisitDate;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "911a";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";
    private static final String INVALID_VISIT_DATE = "31/2/2026 1000";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final String VALID_VISIT_DATE = BENSON.getVisitDate().toString();
    private static final String VALID_NOTE = BENSON.getNote().toString();
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_unsupportedSmokingStatus_throwsIllegalValueException() {
        for (String status : List.of("daily", "yes", "unrecorded")) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, null, null, null, status,
                    VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_VISIT_DATE,
                    null, VALID_NOTE, null, VALID_TAGS);
            assertThrows(IllegalValueException.class, SmokingStatus.MESSAGE_CONSTRAINTS, person::toModelType);
        }
    }

    @Test
    public void toModelType_invalidDateOfBirth_throwsIllegalValueException() {
        for (String date : List.of("31/2/2000", "1/1/9999", " ")) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, date, null, null, null, null,
                    VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_VISIT_DATE,
                    null, VALID_NOTE, null, VALID_TAGS);
            assertThrows(IllegalValueException.class, DateOfBirth.MESSAGE_CONSTRAINTS, person::toModelType);
        }
    }

    @Test
    public void toModelType_dateOfBirth_roundTripsPatient() throws Exception {
        Person patient = new PersonBuilder(BENSON).withDateOfBirth("15/6/1990").build();
        assertEquals(patient, new JsonAdaptedPerson(patient).toModelType());
    }

    @Test
    public void toModelType_previousVisitDates_preservesOrderAndOtherFields() throws Exception {
        Person original = new PersonBuilder(BENSON).withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000", "1/10/2026 1100")
                .withDateOfBirth("15/6/1990").withEmergencyContact("Mary Tan, 91234567").withBloodType("AB-")
                .withAllergies("Penicillin; latex").withAlcoholHistory("Occasional drinking")
                .withSmokingStatus("unknown").build();

        assertEquals(original, new JsonAdaptedPerson(original).toModelType());
    }

    @Test
    public void toModelType_nullPreviousVisitDates_defaultsToEmpty() throws Exception {
        assertEquals(BENSON, withPreviousVisitDates(null).toModelType());
        assertEquals(BENSON, withPreviousVisitDates(List.of()).toModelType());
    }

    @Test
    public void toModelType_malformedOrNullPreviousDate_throwsIllegalValueException() {
        for (List<String> previousDates : List.of(List.of(INVALID_VISIT_DATE), List.of(""),
                Arrays.asList("1/1/2024 1000", null))) {
            assertThrows(IllegalValueException.class, () -> withPreviousVisitDates(previousDates).toModelType());
        }
    }

    @Test
    public void toModelType_invalidPreviousDateChronology_preservesCause() {
        for (List<String> previousDates : List.of(List.of("2/1/2024 1000", "1/1/2024 1000"),
                List.of("1/1/2024 1000", "01/01/2024 1000"), List.of(VALID_VISIT_DATE),
                List.of("1/1/2027 1000"))) {
            IllegalValueException exception = Assertions.assertThrows(
                    IllegalValueException.class, () -> withPreviousVisitDates(previousDates).toModelType());
            assertEquals(Person.MESSAGE_PREVIOUS_VISIT_DATES_CONSTRAINTS, exception.getMessage());
            assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        }
    }

    @Test
    public void constructor_mutablePreviousVisitDates_defensivelyCopiesList() throws Exception {
        List<String> previousDates = new ArrayList<>(List.of("1/1/2024 1000"));
        JsonAdaptedPerson adapted = withPreviousVisitDates(previousDates);
        previousDates.clear();

        assertEquals(List.of(new VisitDate("1/1/2024 1000")), adapted.toModelType().getPreviousVisitDates());
    }

    @Test
    public void toModelType_caseVariantTags_keepsFirstSpellingOnce() throws Exception {
        List<JsonAdaptedTag> caseVariantTags = List.of(new JsonAdaptedTag("FollowUp"), new JsonAdaptedTag("followup"));
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null,
                VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_VISIT_DATE, null, VALID_NOTE, null, caseVariantTags);

        Set<Tag> tags = person.toModelType().getTags();

        assertEquals(1, tags.size());
        assertEquals("FollowUp", tags.iterator().next().tagName);
    }

    private JsonAdaptedPerson withPreviousVisitDates(List<String> previousDates) {
        return new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null,
                VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_VISIT_DATE,
                previousDates, VALID_NOTE, null, VALID_TAGS);
    }

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_bloodType_roundTripsPatient() throws Exception {
        Person patient = new PersonBuilder(BENSON).withBloodType("A+").build();
        assertEquals(patient, new JsonAdaptedPerson(patient).toModelType());
    }

    @Test
    public void toModelType_invalidBloodType_preservesCause() {
        for (String bloodType : List.of("C+", "A", "UNKNOWN")) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, bloodType, null, null, null,
                    VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_VISIT_DATE,
                    null, VALID_NOTE, null, VALID_TAGS);
            IllegalValueException exception = Assertions.assertThrows(IllegalValueException.class, person::toModelType);
            assertEquals(BloodType.MESSAGE_CONSTRAINTS, exception.getMessage());
            assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
            assertEquals(BloodType.MESSAGE_CONSTRAINTS, exception.getCause().getMessage());
        }
    }

    @Test
    public void fromJson_missingOrNullOptionalFields_loadsOlderRecord() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(BENSON));
        for (boolean shouldUseNull : List.of(false, true)) {
            ObjectNode record = JsonUtil.fromJsonString(json, ObjectNode.class);
            for (String field : List.of("dateOfBirth", "bloodType", "allergies", "alcoholHistory", "smokingStatus",
                    "previousVisitDates", "note", "emergencyContact")) {
                if (shouldUseNull) {
                    record.putNull(field);
                } else {
                    record.remove(field);
                }
            }
            Person expected = new PersonBuilder(BENSON).withNote("").build();
            JsonAdaptedPerson adapted = JsonUtil.fromJsonString(record.toString(), JsonAdaptedPerson.class);
            assertEquals(expected, adapted.toModelType());
        }
    }

    @Test
    public void toModelType_multipleInvalidFields_preservesValidationOrder() throws Exception {
        ObjectNode record = JsonUtil.fromJsonString(JsonUtil.toJsonString(new JsonAdaptedPerson(BENSON)),
                ObjectNode.class);
        record.put("dateOfBirth", "31/2/2000");
        record.put("bloodType", "C+");
        record.put("email", INVALID_EMAIL);

        JsonAdaptedPerson invalidBirthDate = JsonUtil.fromJsonString(record.toString(), JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, DateOfBirth.MESSAGE_CONSTRAINTS, invalidBirthDate::toModelType);

        record.put("dateOfBirth", "");
        JsonAdaptedPerson invalidBloodType = JsonUtil.fromJsonString(record.toString(), JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, BloodType.MESSAGE_CONSTRAINTS, invalidBloodType::toModelType);

        record.put("bloodType", "");
        JsonAdaptedPerson invalidEmail = JsonUtil.fromJsonString(record.toString(), JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, Email.MESSAGE_CONSTRAINTS, invalidEmail::toModelType);
    }

    @Test
    public void fromJson_recordedOptionalFields_preservesCompleteRecord() throws Exception {
        Person patient = new PersonBuilder(BENSON).withDateOfBirth("15/6/1990").withBloodType("AB-")
                .withAllergies("Penicillin; latex").withAlcoholHistory("Does not drink").withSmokingStatus("former")
                .withEmergencyContact("Mary Tan, 91234567").withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000").withNote("Review medication").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(patient));

        JsonAdaptedPerson adapted = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);

        assertEquals(patient, adapted.toModelType());
    }

    @Test
    public void toModelType_invalidEmergencyContact_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null,
                VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_VISIT_DATE,
                null, VALID_NOTE, "Mary Tan, invalid", VALID_TAGS);
        assertThrows(IllegalValueException.class, EmergencyContact.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(INVALID_NAME, null, null, null, null, null,
                VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_VISIT_DATE,
                null, VALID_NOTE, null, VALID_TAGS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nameContainsPhonePrefix_throwsSpecificMessage() {
        JsonAdaptedPerson person = new JsonAdaptedPerson("Rachelp/91234567", null, null, null, null, null,
                VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_VISIT_DATE,
                null, VALID_NOTE, null, VALID_TAGS);
        assertThrows(IllegalValueException.class, Name.MESSAGE_CONTAINS_PHONE_PREFIX, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, null, null, null, null, null,
                VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_VISIT_DATE,
                null, VALID_NOTE, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null,
                INVALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_VISIT_DATE,
                null, VALID_NOTE, null, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null,
                null, VALID_EMAIL, VALID_ADDRESS, VALID_VISIT_DATE,
                null, VALID_NOTE, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null,
                VALID_PHONE, INVALID_EMAIL, VALID_ADDRESS, VALID_VISIT_DATE,
                null, VALID_NOTE, null, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null,
                VALID_PHONE, null, VALID_ADDRESS, VALID_VISIT_DATE,
                null, VALID_NOTE, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null,
                VALID_PHONE, VALID_EMAIL, INVALID_ADDRESS, VALID_VISIT_DATE,
                null, VALID_NOTE, null, VALID_TAGS);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null,
                VALID_PHONE, VALID_EMAIL, null, VALID_VISIT_DATE,
                null, VALID_NOTE, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidVisitDate_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null,
                VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, INVALID_VISIT_DATE,
                null, VALID_NOTE, null, VALID_TAGS);
        String expectedMessage = VisitDate.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_visitDateYearOutOfRange_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null,
                VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, "1/1/2100 1000",
                null, VALID_NOTE, null, VALID_TAGS);
        assertThrows(IllegalValueException.class, VisitDate.MESSAGE_CONSTRAINTS, person::toModelType);

        JsonAdaptedPerson withOldPreviousDate = withPreviousVisitDates(List.of("1/1/1899 1000"));
        assertThrows(IllegalValueException.class, "Invalid previous visit date. " + VisitDate.MESSAGE_CONSTRAINTS,
                withOldPreviousDate::toModelType);
    }

    @Test
    public void toModelType_nullVisitDate_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null,
                VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                null, VALID_NOTE, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, VisitDate.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null,
                VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_VISIT_DATE,
                null, VALID_NOTE, null, invalidTags);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

}
