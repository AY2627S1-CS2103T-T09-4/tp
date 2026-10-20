package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_VISIT_DATE_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.DuplicateVisitDateException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.testutil.PersonBuilder;

public class UniquePersonListTest {

    private final UniquePersonList uniquePersonList = new UniquePersonList();

    @Test
    public void add_conflictingVisitRecords_rejectsWithoutChangingList() {
        Person existing = new PersonBuilder(ALICE).withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000").build();
        uniquePersonList.add(existing);
        for (Person conflicting : conflictingVisitRecords()) {
            assertTrue(uniquePersonList.containsSameVisitDate(conflicting));
            assertThrows(DuplicateVisitDateException.class, () -> uniquePersonList.add(conflicting));
            assertEquals(List.of(existing), uniquePersonList.asUnmodifiableObservableList());
        }
    }

    @Test
    public void setPerson_conflictingVisitRecords_rejectsCompleteCandidateWithoutChangingList() {
        Person existing = new PersonBuilder(ALICE).withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000").build();
        uniquePersonList.setPersons(List.of(existing, BOB));
        for (Person conflicting : conflictingVisitRecords()) {
            assertTrue(uniquePersonList.containsSameVisitDate(conflicting, BOB));
            assertThrows(DuplicateVisitDateException.class, () -> uniquePersonList.setPerson(BOB, conflicting));
            assertEquals(List.of(existing, BOB), uniquePersonList.asUnmodifiableObservableList());
        }
    }

    @Test
    public void setPerson_unchangedCurrentWithConflictingHistory_rejectsWithoutChangingList() {
        Person existing = new PersonBuilder(ALICE).withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000").build();
        Person target = new PersonBuilder(BOB).withVisitDate("20/10/2026 1000").build();
        uniquePersonList.setPersons(List.of(existing, target));
        Person unchangedCurrentWithConflictingHistory = new PersonBuilder(target)
                .withPreviousVisitDates("1/10/2026 1000").build();

        assertThrows(DuplicateVisitDateException.class, () ->
                uniquePersonList.setPerson(target, unchangedCurrentWithConflictingHistory));
        assertEquals(List.of(existing, target), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void setPersons_conflictingVisitRecords_preservesOriginalList() {
        uniquePersonList.add(ALICE);
        Person existing = new PersonBuilder(ALICE).withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000").build();
        for (Person conflicting : conflictingVisitRecords()) {
            assertThrows(DuplicateVisitDateException.class, () ->
                    uniquePersonList.setPersons(List.of(existing, conflicting)));
            assertEquals(List.of(ALICE), uniquePersonList.asUnmodifiableObservableList());
        }
    }

    @Test
    public void setPerson_ownDatesMovedToHistory_excludesOriginalTarget() {
        Person original = new PersonBuilder(ALICE).withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000").build();
        uniquePersonList.add(original);
        Person replacement = new PersonBuilder(original).withVisitDate("20/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000", "10/10/2026 1000").build();

        assertFalse(uniquePersonList.containsSameVisitDate(replacement, original));
        uniquePersonList.setPerson(original, replacement);
        uniquePersonList.setPerson(replacement, replacement);

        assertEquals(List.of(replacement), uniquePersonList.asUnmodifiableObservableList());
        assertThrows(NullPointerException.class, () -> uniquePersonList.containsSameVisitDate(null, original));
        assertThrows(NullPointerException.class, () -> uniquePersonList.containsSameVisitDate(replacement, null));
    }

    @Test
    public void countSameVisitDay_previousDates_doNotCountTowardsCapacity() {
        Person existing = new PersonBuilder(ALICE).withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000", "1/10/2026 1100").build();
        uniquePersonList.add(existing);
        Person sameDayDifferentTime = new PersonBuilder(BOB).withVisitDate("1/10/2026 1200").build();

        assertFalse(uniquePersonList.containsSameVisitDate(sameDayDifferentTime));
        assertEquals(0, uniquePersonList.countSameVisitDay(sameDayDifferentTime));
        uniquePersonList.add(sameDayDifferentTime);
        assertEquals(1, uniquePersonList.countSameVisitDay(sameDayDifferentTime));
    }

    /**
     * Returns distinct patients exercising all current/previous datetime conflict combinations.
     */
    private List<Person> conflictingVisitRecords() {
        return List.of(new PersonBuilder(BOB).withVisitDate("01/10/2026 1000").build(),
                new PersonBuilder(BOB).withVisitDate("10/10/2026 1000").build(),
                new PersonBuilder(BOB).withVisitDate("20/10/2026 1000")
                        .withPreviousVisitDates("10/10/2026 1000").build(),
                new PersonBuilder(BOB).withVisitDate("20/10/2026 1000")
                        .withPreviousVisitDates("01/10/2026 1000").build());
    }

    @Test
    public void contains_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.contains(null));
    }

    @Test
    public void contains_personNotInList_returnsFalse() {
        assertFalse(uniquePersonList.contains(ALICE));
    }

    @Test
    public void contains_personInList_returnsTrue() {
        uniquePersonList.add(ALICE);
        assertTrue(uniquePersonList.contains(ALICE));
    }

    @Test
    public void contains_personWithSameIdentityFieldsInList_returnsTrue() {
        uniquePersonList.add(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(uniquePersonList.contains(editedAlice));
    }

    @Test
    public void contains_personWithSameNameAndDifferentPhone_returnsFalse() {
        uniquePersonList.add(ALICE);
        Person sameNameDifferentPhone = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB)
                .withVisitDate(VALID_VISIT_DATE_BOB).build();

        assertFalse(uniquePersonList.contains(sameNameDifferentPhone));
        assertTrue(uniquePersonList.containsSameName(sameNameDifferentPhone));
    }

    @Test
    public void containsSameVisitDate() {
        uniquePersonList.add(ALICE);
        Person personWithSameVisitDate = new PersonBuilder(ALICE).withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).build();
        Person personWithDifferentVisitDate = new PersonBuilder(personWithSameVisitDate)
                .withVisitDate(VALID_VISIT_DATE_BOB).build();

        assertTrue(uniquePersonList.containsSameVisitDate(personWithSameVisitDate));
        assertFalse(uniquePersonList.containsSameVisitDate(personWithDifferentVisitDate));
        assertThrows(NullPointerException.class, () -> uniquePersonList.containsSameVisitDate(null));
    }

    @Test
    public void countSameVisitDay() {
        // ALICE and BENSON visit on the same day at different times
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BENSON);
        Person personLaterOnSameDay = new PersonBuilder().withVisitDate("1/1/2026 1500").build();
        Person personOnDifferentDay = new PersonBuilder().withVisitDate(VALID_VISIT_DATE_BOB).build();

        assertEquals(2, uniquePersonList.countSameVisitDay(personLaterOnSameDay));
        assertEquals(0, uniquePersonList.countSameVisitDay(personOnDifferentDay));
        assertThrows(NullPointerException.class, () -> uniquePersonList.countSameVisitDay(null));
    }

    @Test
    public void add_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.add(null));
    }

    @Test
    public void add_duplicatePerson_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(ALICE));
    }

    @Test
    public void add_sameNameAndDifferentPhone_addsPerson() {
        Person sameNameDifferentPhone = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB)
                .withVisitDate(VALID_VISIT_DATE_BOB).build();

        uniquePersonList.add(ALICE);
        uniquePersonList.add(sameNameDifferentPhone);

        assertEquals(List.of(ALICE, sameNameDifferentPhone), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void add_differentNameAndSamePhone_addsPerson() {
        Person samePhoneDifferentName = new PersonBuilder(ALICE).withName(VALID_NAME_BOB)
                .withVisitDate(VALID_VISIT_DATE_BOB).build();

        uniquePersonList.add(ALICE);
        uniquePersonList.add(samePhoneDifferentName);

        assertEquals(List.of(ALICE, samePhoneDifferentName), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void add_duplicateVisitDate_throwsDuplicateVisitDateException() {
        Person personWithSameVisitDate = new PersonBuilder(ALICE).withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).build();

        uniquePersonList.add(ALICE);

        assertThrows(DuplicateVisitDateException.class, () -> uniquePersonList.add(personWithSameVisitDate));
    }

    @Test
    public void setPerson_nullTargetPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPerson(null, ALICE));
    }

    @Test
    public void setPerson_nullEditedPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPerson(ALICE, null));
    }

    @Test
    public void setPerson_targetPersonNotInList_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.setPerson(ALICE, ALICE));
    }

    @Test
    public void setPerson_editedPersonIsSamePerson_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.setPerson(ALICE, ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(ALICE);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasSameIdentity_success() {
        uniquePersonList.add(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        uniquePersonList.setPerson(ALICE, editedAlice);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(editedAlice);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasDifferentIdentity_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.setPerson(ALICE, BOB);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasNonUniqueIdentity_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(ALICE, BOB));
    }

    @Test
    public void setPerson_editedPersonHasNonUniqueVisitDate_throwsDuplicateVisitDateException() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        Person editedAlice = new PersonBuilder(ALICE).withVisitDate(BOB.getVisitDate().toString()).build();

        assertThrows(DuplicateVisitDateException.class, () -> uniquePersonList.setPerson(ALICE, editedAlice));
    }

    @Test
    public void remove_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.remove(null));
    }

    @Test
    public void remove_personDoesNotExist_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.remove(ALICE));
    }

    @Test
    public void remove_existingPerson_removesPerson() {
        uniquePersonList.add(ALICE);
        uniquePersonList.remove(ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_nullUniquePersonList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersons((UniquePersonList) null));
    }

    @Test
    public void setPersons_uniquePersonList_replacesOwnListWithProvidedUniquePersonList() {
        uniquePersonList.add(ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        uniquePersonList.setPersons(expectedUniquePersonList);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersons((List<Person>) null));
    }

    @Test
    public void setPersons_list_replacesOwnListWithProvidedList() {
        uniquePersonList.add(ALICE);
        List<Person> personList = List.of(BOB);
        uniquePersonList.setPersons(personList);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_listWithDuplicatePersons_throwsDuplicatePersonException() {
        List<Person> listWithDuplicatePersons = List.of(ALICE, ALICE);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPersons(listWithDuplicatePersons));
    }

    @Test
    public void setPersons_listWithDuplicateVisitDates_throwsDuplicateVisitDateException() {
        Person personWithSameVisitDate = new PersonBuilder(ALICE).withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).build();
        List<Person> personsWithDuplicateVisitDates = List.of(ALICE, personWithSameVisitDate);

        assertThrows(DuplicateVisitDateException.class, () ->
                uniquePersonList.setPersons(personsWithDuplicateVisitDates));
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () ->
                uniquePersonList.asUnmodifiableObservableList().remove(0));
    }

    @Test
    public void toStringMethod() {
        assertEquals(uniquePersonList.asUnmodifiableObservableList().toString(), uniquePersonList.toString());
    }
}
