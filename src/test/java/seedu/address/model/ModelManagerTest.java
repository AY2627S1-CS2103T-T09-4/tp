package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_VISIT_DATE_BOB;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void hasPersonWithSameVisitDate_hiddenPreviousDate_checksFullCollectionAndExcludesTarget() {
        // EP: Stored historical conflicts remain relevant when their patient is hidden from the displayed list.
        // EP: The replaced target is excluded from its own conflict check.
        Person existing = new PersonBuilder(ALICE).withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000").build();
        modelManager.addPerson(existing);
        modelManager.addPerson(BENSON);
        modelManager.updateFilteredPersonList(person -> person.equals(BENSON));
        Person candidate = new PersonBuilder(BENSON).withVisitDate("01/10/2026 1000").build();

        assertTrue(modelManager.hasPersonWithSameVisitDate(candidate));
        assertTrue(modelManager.hasPersonWithSameVisitDate(candidate, BENSON));
        assertFalse(modelManager.hasPersonWithSameVisitDate(existing, existing));
        assertFalse(modelManager.hasPersonWithSameVisitDate(BENSON, BENSON));
        assertEquals(List.of(BENSON), modelManager.getFilteredPersonList());
        // API preconditions: Both the candidate and excluded target must be non-null.
        assertThrows(NullPointerException.class, () -> modelManager.hasPersonWithSameVisitDate(null, BENSON));
        assertThrows(NullPointerException.class, () -> modelManager.hasPersonWithSameVisitDate(candidate, null));
    }

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void restoreStateSnapshot_afterDataAndDisplayChanges_restoresCompleteState() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        modelManager = new ModelManager(addressBook, new UserPrefs());
        Comparator<Person> reverseNameComparator = Comparator.comparing(
                person -> person.getName().fullName, Comparator.reverseOrder());
        modelManager.updateFilteredPersonList(person -> person.getName().fullName.contains("Alice"));
        modelManager.sortFilteredPersonList(reverseNameComparator);
        Model.ModelState snapshot = modelManager.createStateSnapshot();

        modelManager.deletePerson(ALICE);
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        modelManager.restoreStateSnapshot(snapshot);

        assertEquals(List.of(ALICE, BENSON), modelManager.getAddressBook().getPersonList());
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());

        Person renamedBenson = new PersonBuilder(BENSON).withName("Alice Benson").build();
        modelManager.setPerson(BENSON, renamedBenson);
        assertEquals(List.of(ALICE, renamedBenson), modelManager.getFilteredPersonList());
    }

    @Test
    public void restoreStateSnapshot_nullSnapshot_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.restoreStateSnapshot(null));
    }

    @Test
    public void restoreStateSnapshot_incompatibleSnapshot_throwsIllegalArgumentException() {
        Model.ModelState incompatibleSnapshot = new Model.ModelState() {
        };

        assertThrows(IllegalArgumentException.class,
                () -> modelManager.restoreStateSnapshot(incompatibleSnapshot));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPersonWithSameName_sameNameAndDifferentPhone_returnsTrue() {
        modelManager.addPerson(ALICE);
        Person sameNameDifferentPhone = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB)
                .withVisitDate(VALID_VISIT_DATE_BOB).build();

        assertFalse(modelManager.hasPerson(sameNameDifferentPhone));
        assertTrue(modelManager.hasPersonWithSameName(sameNameDifferentPhone));
    }

    @Test
    public void hasPersonWithSameVisitDate_sameVisitDate_returnsTrue() {
        modelManager.addPerson(ALICE);
        Person personWithSameVisitDate = new PersonBuilder(ALICE).withName("Different Patient")
                .withPhone(VALID_PHONE_BOB).build();

        assertTrue(modelManager.hasPersonWithSameVisitDate(personWithSameVisitDate));
    }

    @Test
    public void countPersonsWithSameVisitDay() {
        // ALICE and BENSON visit on the same day at different times
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        Person personLaterOnSameDay = new PersonBuilder().withVisitDate("1/1/2026 1500").build();
        Person personOnDifferentDay = new PersonBuilder().withVisitDate(VALID_VISIT_DATE_BOB).build();

        assertEquals(2, modelManager.countPersonsWithSameVisitDay(personLaterOnSameDay));
        assertEquals(0, modelManager.countPersonsWithSameVisitDay(personOnDifferentDay));
        assertThrows(NullPointerException.class, () -> modelManager.countPersonsWithSameVisitDay(null));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void sortFilteredPersonList_nullComparator_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.sortFilteredPersonList(null));
    }

    @Test
    public void sortFilteredPersonList_validComparator_sortsDisplayedListOnly() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        modelManager = new ModelManager(addressBook, new UserPrefs());
        Comparator<Person> reverseNameComparator = Comparator.comparing(
                person -> person.getName().fullName, Comparator.reverseOrder());

        modelManager.sortFilteredPersonList(reverseNameComparator);

        assertEquals(List.of(BENSON, ALICE), modelManager.getFilteredPersonList());
        assertEquals(List.of(ALICE, BENSON), modelManager.getAddressBook().getPersonList());
    }

    @Test
    public void updateFilteredPersonList_afterSorting_restoresStoredOrder() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        modelManager = new ModelManager(addressBook, new UserPrefs());
        Comparator<Person> reverseNameComparator = Comparator.comparing(
                person -> person.getName().fullName, Comparator.reverseOrder());
        modelManager.sortFilteredPersonList(reverseNameComparator);

        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        assertEquals(List.of(ALICE, BENSON), modelManager.getFilteredPersonList());
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }
}
