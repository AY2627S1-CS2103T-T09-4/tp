package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests rollback selection, date restoration, field preservation, and rejected-command state.
 */
public class PrevCommandTest {

    private static final Person AMY = new PersonBuilder().withVisitDate("30/10/2026 1000")
            .withPreviousVisitDates("10/10/2026 1000", "20/10/2026 1000")
            .withDateOfBirth("15/6/1990").withBloodType("A+")
            .withAllergies("Penicillin; latex").withAlcoholHistory("Does not drink").withSmokingStatus("former")
            .withEmergencyContact("Mary Tan, 91234567")
            .withNote("Review  medication").withTags("priority", "friend").build();
    private static final Person BENSON = new PersonBuilder().withName("Benson Meier").withPhone("91234567")
            .withVisitDate("1/10/2026 1000").withEmail("").withNote("").withTags().build();
    private static final Person CHARLIE = new PersonBuilder().withName("Charlie Tan").withPhone("81234567")
            .withVisitDate("5/10/2026 1000").withEmail("").withNote("").withTags().build();

    private final Model model = createModel(AMY, BENSON);

    @Test
    public void execute_severalPreviousDates_restoresLatestAndPreservesOtherFields() throws Exception {
        // EP: Several retained dates restore only the latest and preserve earlier entries.
        Person expectedPerson = new PersonBuilder(AMY).withVisitDate("20/10/2026 1000")
                .withPreviousVisitDates("10/10/2026 1000").build();

        assertRollbackSuccess(INDEX_FIRST_PERSON, AMY, expectedPerson);
        assertEquals(2, AMY.getPreviousVisitDates().size());
        assertEquals("30/10/2026 1000", AMY.getVisitDate().toString());
    }

    @Test
    public void execute_onePreviousDate_restoresDateAndLeavesEmptyHistory() throws Exception {
        // Boundary: One retained date becomes the current visit and leaves no previous dates.
        Person original = new PersonBuilder(BENSON).withPreviousVisitDates("1/9/2026 1000").build();
        model.setPerson(BENSON, original);
        Person expectedPerson = new PersonBuilder(original).withVisitDate("1/9/2026 1000")
                .withPreviousVisitDates().build();

        assertRollbackSuccess(INDEX_SECOND_PERSON, original, expectedPerson);
    }

    @Test
    public void execute_futureCurrentAndPreviousDates_allowsRollback() throws Exception {
        // EP: Rollback is independent of whether the retained or current visit is in the future.
        Person original = new PersonBuilder(AMY).withVisitDate("30/10/9999 1000")
                .withPreviousVisitDates("20/10/9999 1000").build();
        model.setPerson(AMY, original);
        Person expectedPerson = new PersonBuilder(original).withVisitDate("20/10/9999 1000")
                .withPreviousVisitDates().build();

        assertRollbackSuccess(INDEX_FIRST_PERSON, original, expectedPerson);
    }

    @Test
    public void execute_filteredSortedIndex_targetsPatientAndResetsList() throws Exception {
        // EP: The restrictive filter hides one patient, and sorting changes the displayed order.
        model.addPerson(CHARLIE);
        model.updateFilteredPersonList(person -> person.equals(AMY) || person.equals(CHARLIE));
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        assertEquals(List.of(CHARLIE, AMY), model.getFilteredPersonList());
        Person expectedPerson = new PersonBuilder(AMY).withVisitDate("20/10/2026 1000")
                .withPreviousVisitDates("10/10/2026 1000").build();

        assertRollbackSuccess(INDEX_SECOND_PERSON, AMY, expectedPerson);
        assertEquals(BENSON, model.getAddressBook().getPersonList().get(1));
        assertEquals(CHARLIE, model.getAddressBook().getPersonList().get(2));
    }

    @Test
    public void execute_sortedIndex_updatesDisplayedPatient() throws Exception {
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        Person expectedPerson = new PersonBuilder(AMY).withVisitDate("20/10/2026 1000")
                .withPreviousVisitDates("10/10/2026 1000").build();

        assertRollbackSuccess(INDEX_SECOND_PERSON, AMY, expectedPerson);
    }

    @Test
    public void execute_emptyHistory_preservesDataFilterAndSort() {
        // Boundary: Zero retained dates cannot be rolled back; the arranged view must remain intact.
        model.updateFilteredPersonList(person -> person.getName().fullName.contains("e"));
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));

        assertRollbackFailure(INDEX_FIRST_PERSON,
                String.format(PrevCommand.MESSAGE_NO_PREVIOUS_VISIT_DATES, BENSON.getName()));

        Person laterBenson = new PersonBuilder(BENSON).withVisitDate("1/11/2026 1000").build();
        model.setPerson(BENSON, laterBenson);
        assertEquals(List.of(AMY, laterBenson), model.getFilteredPersonList());
        model.setPerson(laterBenson, new PersonBuilder(laterBenson).withName("Carl Kurz").build());
        assertEquals(List.of(AMY), model.getFilteredPersonList());
    }

    @Test
    public void execute_validIndex_returnsPromptWithoutMutatingPatient() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandResult result = new PrevCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(String.format(PrevCommand.MESSAGE_CONFIRM_PREV, AMY.getName(), AMY.getPhone(),
                "20/10/2026 1000", AMY.getVisitDate()), result.getFeedbackToUser());
        assertFalse(result.shouldSaveAddressBook());
        assertTrue(result.getInputRequest().isPresent());
        assertEquals(before, model.getAddressBook());
        assertEquals(before.getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_cancelledConfirmation_preservesDataAndDisplayedOrder() throws Exception {
        model.updateFilteredPersonList(person -> person.getName().equals(AMY.getName()));
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedPersons = new ArrayList<>(model.getFilteredPersonList());

        CommandResult requestResult = new PrevCommand(INDEX_FIRST_PERSON).execute(model);
        CommandResult result = requestResult.getInputRequest().orElseThrow().respond("list", model);

        assertEquals(PrevCommand.MESSAGE_PREV_CANCELLED, result.getFeedbackToUser());
        assertFalse(result.shouldSaveAddressBook());
        assertEquals(before, model.getAddressBook());
        assertEquals(displayedPersons, model.getFilteredPersonList());
    }

    @Test
    public void execute_confirmationAfterDisplayedOrderChanges_restoresCapturedPatient() throws Exception {
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        CommandResult requestResult = new PrevCommand(INDEX_SECOND_PERSON).execute(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        CommandResult result = requestResult.getInputRequest().orElseThrow().respond("Y", model);

        Person expectedPerson = new PersonBuilder(AMY).withVisitDate("20/10/2026 1000")
                .withPreviousVisitDates("10/10/2026 1000").build();
        assertEquals(expectedPerson, model.getAddressBook().getPersonList().getFirst());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
        assertTrue(result.shouldSaveAddressBook());
    }

    @Test
    public void execute_confirmationAfterCapturedPatientChanges_rejectsStaleRequest() throws Exception {
        CommandResult requestResult = new PrevCommand(INDEX_FIRST_PERSON).execute(model);
        Person replacementPerson = new PersonBuilder(AMY).withName("Amy Updated").build();
        model.setPerson(AMY, replacementPerson);

        assertThrows(CommandException.class, PrevCommand.MESSAGE_PERSON_NO_LONGER_EXISTS, () ->
                requestResult.getInputRequest().orElseThrow().respond("y", model));
        assertEquals(replacementPerson, model.getAddressBook().getPersonList().getFirst());
    }

    @Test
    public void execute_outOfRangeDisplayedIndex_preservesDataAndDisplay() {
        model.updateFilteredPersonList(person -> person.getName().equals(AMY.getName()));
        assertRollbackFailure(INDEX_SECOND_PERSON, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertRollbackFailure(Index.fromOneBased(Integer.MAX_VALUE), Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyList_rejectsIndex() {
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () ->
                new PrevCommand(INDEX_FIRST_PERSON).execute(new ModelManager()));
    }

    @Test
    public void constructorAndExecute_nullArguments_throwsNullPointerException() {
        // API preconditions, distinct from CLI argument validation.
        assertThrows(NullPointerException.class, () -> new PrevCommand(null));
        assertThrows(NullPointerException.class, () -> new PrevCommand(INDEX_FIRST_PERSON).execute(null));
    }

    @Test
    public void equals() {
        PrevCommand command = new PrevCommand(INDEX_FIRST_PERSON);
        PrevCommand equivalent = new PrevCommand(Index.fromOneBased(1));
        assertEquals(command, command);
        assertEquals(command, equivalent);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertNotEquals(command, new PrevCommand(INDEX_SECOND_PERSON));
        assertNotEquals(command, new ListCommand());
        assertNotEquals(command, null);
    }

    @Test
    public void toStringMethod() {
        assertEquals(PrevCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON + "}",
                new PrevCommand(INDEX_FIRST_PERSON).toString());
    }

    private void assertRollbackSuccess(Index index, Person originalPerson, Person expectedPerson) throws Exception {
        AddressBook expectedAddressBook = new AddressBook(model.getAddressBook());
        expectedAddressBook.setPerson(originalPerson, expectedPerson);

        CommandResult requestResult = new PrevCommand(index).execute(model);
        assertFalse(requestResult.shouldSaveAddressBook());
        assertTrue(requestResult.getInputRequest().isPresent());
        CommandResult result = requestResult.getInputRequest().orElseThrow().respond("y", model);

        String expectedMessage = String.format(PrevCommand.MESSAGE_SUCCESS,
                expectedPerson.getName(), expectedPerson.getVisitDate(), originalPerson.getVisitDate());
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedAddressBook, model.getAddressBook());
        assertEquals(expectedAddressBook.getPersonList(), model.getFilteredPersonList());
        assertTrue(result.shouldSaveAddressBook());
        assertTrue(result.getPersonToView().isEmpty());
        assertTrue(result.getInputRequest().isEmpty());
    }

    private void assertRollbackFailure(Index index, String expectedMessage) {
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedPersons = new ArrayList<>(model.getFilteredPersonList());

        assertThrows(CommandException.class, expectedMessage, () -> new PrevCommand(index).execute(model));

        assertEquals(before, model.getAddressBook());
        assertEquals(displayedPersons, model.getFilteredPersonList());
    }

    private static Model createModel(Person... persons) {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(persons));
        return new ModelManager(addressBook, new UserPrefs());
    }
}
