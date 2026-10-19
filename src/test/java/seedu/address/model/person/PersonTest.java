package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    private static final VisitDate CURRENT_VISIT_DATE = new VisitDate("10/10/2026 1000");
    private static final VisitDate EARLIER_VISIT_DATE = new VisitDate("1/10/2026 1000");
    private static final VisitDate LATER_VISIT_DATE = new VisitDate("1/10/2026 1100");

    @Test
    public void hasCommonVisitDate_currentAndPreviousDates_comparesParsedDatetimes() {
        Person original = createPerson(CURRENT_VISIT_DATE, List.of(EARLIER_VISIT_DATE));
        Person sameCurrent = createPerson(CURRENT_VISIT_DATE, List.of());
        Person currentInHistory = createPerson(new VisitDate("01/10/2026 1000"), List.of());
        Person historyContainsCurrent = createPerson(new VisitDate("11/10/2026 1000"), List.of(CURRENT_VISIT_DATE));
        Person sameHistory = createPerson(new VisitDate("12/10/2026 1000"), List.of(EARLIER_VISIT_DATE));
        Person differentTimes = createPerson(LATER_VISIT_DATE, List.of());

        for (Person conflicting : List.of(original, sameCurrent, currentInHistory, historyContainsCurrent,
                sameHistory)) {
            assertTrue(original.hasCommonVisitDate(conflicting));
            assertTrue(conflicting.hasCommonVisitDate(original));
        }
        assertFalse(original.hasCommonVisitDate(differentTimes));
        assertFalse(differentTimes.hasCommonVisitDate(original));
        assertThrows(NullPointerException.class, () -> original.hasCommonVisitDate(null));
    }

    @Test
    public void constructor_emptyPreviousVisitDates_success() {
        assertEquals(List.of(), new PersonBuilder().build().getPreviousVisitDates());
        Person person = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                ALICE.getVisitDate(), ALICE.getNote(), ALICE.getTags());
        assertEquals(List.of(), person.getPreviousVisitDates());
    }

    @Test
    public void constructor_orderedPreviousVisitDates_preservesOrderAndOtherFields() {
        List<VisitDate> previousVisitDates = List.of(EARLIER_VISIT_DATE, LATER_VISIT_DATE);
        Person person = createPerson(CURRENT_VISIT_DATE, previousVisitDates);

        assertEquals(previousVisitDates, person.getPreviousVisitDates());
        assertEquals(CURRENT_VISIT_DATE, person.getVisitDate());
        assertEquals(ALICE.getName(), person.getName());
        assertEquals(ALICE.getPhone(), person.getPhone());
        assertEquals(ALICE.getEmail(), person.getEmail());
        assertEquals(ALICE.getAddress(), person.getAddress());
        assertEquals(ALICE.getNote(), person.getNote());
        assertEquals(ALICE.getTags(), person.getTags());
    }

    @Test
    public void constructor_nullVisitRecord_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> createPerson(CURRENT_VISIT_DATE, null));
        assertThrows(NullPointerException.class, () -> createPerson(CURRENT_VISIT_DATE,
                Arrays.asList(EARLIER_VISIT_DATE, null)));
        assertThrows(NullPointerException.class, () -> createPerson(null, List.of()));
        assertThrows(NullPointerException.class, () -> new Person(ALICE.getName(), ALICE.getPhone(),
                ALICE.getEmail(), ALICE.getAddress(), null, ALICE.getNote(), ALICE.getTags()));
    }

    @Test
    public void constructor_unorderedOrDuplicatePreviousVisitDates_throwsIllegalArgumentException() {
        for (List<VisitDate> previousVisitDates : List.of(
                List.of(LATER_VISIT_DATE, EARLIER_VISIT_DATE),
                List.of(EARLIER_VISIT_DATE, new VisitDate("01/10/2026 1000")))) {
            assertThrows(IllegalArgumentException.class, Person.MESSAGE_PREVIOUS_VISIT_DATES_CONSTRAINTS, () ->
                    createPerson(CURRENT_VISIT_DATE, previousVisitDates));
        }
    }

    @Test
    public void constructor_previousDateAtOrAfterCurrent_throwsIllegalArgumentException() {
        for (VisitDate previousVisitDate : List.of(new VisitDate("10/10/2026 1001"),
                new VisitDate("10/10/2026 1000"))) {
            assertThrows(IllegalArgumentException.class, Person.MESSAGE_PREVIOUS_VISIT_DATES_CONSTRAINTS, () ->
                    createPerson(CURRENT_VISIT_DATE, List.of(previousVisitDate)));
        }
    }

    @Test
    public void constructor_sameDayDifferentTimes_success() {
        Person person = createPerson(LATER_VISIT_DATE, List.of(EARLIER_VISIT_DATE));
        assertEquals(List.of(EARLIER_VISIT_DATE), person.getPreviousVisitDates());
        assertEquals(LATER_VISIT_DATE, person.getVisitDate());
    }

    @Test
    public void constructor_mutablePreviousVisitDates_defensivelyCopiesList() {
        List<VisitDate> previousVisitDates = new ArrayList<>(List.of(EARLIER_VISIT_DATE, LATER_VISIT_DATE));
        Person person = createPerson(CURRENT_VISIT_DATE, previousVisitDates);
        previousVisitDates.clear();
        previousVisitDates.add(CURRENT_VISIT_DATE);

        assertEquals(List.of(EARLIER_VISIT_DATE, LATER_VISIT_DATE), person.getPreviousVisitDates());
    }

    @Test
    public void getPreviousVisitDates_modifyList_throwsUnsupportedOperationException() {
        Person person = createPerson(CURRENT_VISIT_DATE, List.of(EARLIER_VISIT_DATE, LATER_VISIT_DATE));
        List<VisitDate> previousVisitDates = person.getPreviousVisitDates();
        assertThrows(UnsupportedOperationException.class, () -> previousVisitDates.add(CURRENT_VISIT_DATE));
        assertThrows(UnsupportedOperationException.class, () -> previousVisitDates.remove(0));
        assertThrows(UnsupportedOperationException.class, () -> previousVisitDates.set(0, CURRENT_VISIT_DATE));
        assertThrows(UnsupportedOperationException.class, previousVisitDates::clear);
        assertEquals(List.of(EARLIER_VISIT_DATE, LATER_VISIT_DATE), person.getPreviousVisitDates());
    }

    @Test
    public void equalsAndHashCode_previousVisitDates_comparesVisitRecord() {
        Person person = createPerson(CURRENT_VISIT_DATE, List.of(EARLIER_VISIT_DATE, LATER_VISIT_DATE));
        Person equivalent = createPerson(new VisitDate("10/10/2026 1000"),
                List.of(new VisitDate("01/10/2026 1000"), new VisitDate("01/10/2026 1100")));
        Person differentHistory = createPerson(CURRENT_VISIT_DATE, List.of(EARLIER_VISIT_DATE));

        assertEquals(person, equivalent);
        assertEquals(person.hashCode(), equivalent.hashCode());
        assertNotEquals(person, differentHistory);
        assertNotEquals(person.hashCode(), differentHistory.hashCode());
        assertTrue(person.isSamePerson(differentHistory));
        assertTrue(person.hasSameName(differentHistory));
    }

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name and phone, all data fields different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // same name, different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, all other attributes same -> returns true
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase(Locale.ROOT)).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has trailing spaces, all other attributes same -> returns true
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has repeated spaces, all other attributes same -> returns true
        String nameWithRepeatedSpaces = VALID_NAME_BOB.replace(" ", "   ");
        editedBob = new PersonBuilder(BOB).withName(nameWithRepeatedSpaces).build();
        assertTrue(BOB.isSamePerson(editedBob));
    }

    @Test
    public void hasSameName() {
        assertTrue(ALICE.hasSameName(ALICE));
        assertFalse(ALICE.hasSameName(null));

        Person sameNameDifferentPhone = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertTrue(ALICE.hasSameName(sameNameDifferentPhone));

        Person normalizedNameDifferentPhone = new PersonBuilder(ALICE)
                .withName(ALICE.getName().fullName.toLowerCase(Locale.ROOT).replace(" ", "   ") + " ")
                .withPhone(VALID_PHONE_BOB).build();
        assertTrue(ALICE.hasSameName(normalizedNameDifferentPhone));

        Person differentName = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.hasSameName(differentName));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", address=" + ALICE.getAddress()
                + ", visitDate=" + ALICE.getVisitDate() + ", previousVisitDates=" + ALICE.getPreviousVisitDates()
                + ", note=" + ALICE.getNote()
                + ", tags=" + ALICE.getTags() + "}";
        assertEquals(expected, ALICE.toString());
    }

    private static Person createPerson(VisitDate visitDate, List<VisitDate> previousVisitDates) {
        return new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                visitDate, previousVisitDates, ALICE.getNote(), ALICE.getTags());
    }
}
