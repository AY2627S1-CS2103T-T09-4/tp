package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NOTE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_VISIT_DATE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_VISIT_DATE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private static final String REPLACEMENT_NOTE = "Review medication";
    /** A free time slot on 1/1/2026, the day on which all seven typical persons are already visited. */
    private static final String FULL_DAY_VISIT_DATE = "1/1/2026 1500";

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_hiddenPreviousDateConflict_preservesDataFilterAndSort() {
        Person hiddenPatient = setExistingNoteAndPreviousDates(INDEX_SECOND_PERSON);
        Person original = model.getFilteredPersonList().getFirst();
        model.updateFilteredPersonList(person -> !person.equals(hiddenPatient));
        model.sortFilteredPersonList(Comparator.comparing((Person person) -> person.getVisitDate().value).reversed());
        Index displayedIndex = Index.fromZeroBased(model.getFilteredPersonList().indexOf(original));
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate("01/01/2024 0900")
                .withNote(REPLACEMENT_NOTE).build();
        String expectedMessage = String.format(EditCommand.MESSAGE_DUPLICATE_VISIT_DATE, "1/1/2024 0900");

        assertCommandFailure(new EditCommand(displayedIndex, descriptor), model, expectedMessage);
        Person changedOriginal = new PersonBuilder(original).withVisitDate("1/1/2027 1000").build();
        model.setPerson(original, changedOriginal);
        assertEquals(changedOriginal, model.getFilteredPersonList().getFirst());
        assertFalse(model.getFilteredPersonList().contains(hiddenPatient));
    }

    @Test
    public void confirmation_previousDateConflictAppeared_preservesDataFilterAndSort() throws Exception {
        Person firstPerson = model.getFilteredPersonList().getFirst();
        Person original = setExistingNoteAndPreviousDates(INDEX_SECOND_PERSON);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(firstPerson.getName().fullName)
                .withVisitDate("1/10/2026 1000").build();
        CommandResult requestResult = new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);
        Person conflicting = new PersonBuilder().withName("Conflicting Patient").withVisitDate("2/10/2026 1000")
                .withPreviousVisitDates("01/10/2026 1000").build();
        model.addPerson(conflicting);
        model.updateFilteredPersonList(person -> person.equals(original) || person.equals(firstPerson));
        model.sortFilteredPersonList(Comparator.comparing((Person person) -> person.getVisitDate().value).reversed());
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedPersonsBefore = List.copyOf(model.getFilteredPersonList());

        assertThrows(CommandException.class,
                String.format(EditCommand.MESSAGE_DUPLICATE_VISIT_DATE, "1/10/2026 1000"), () ->
                requestResult.getInputRequest().orElseThrow().respond("y", model));

        assertEquals(before, model.getAddressBook());
        assertEquals(displayedPersonsBefore, model.getFilteredPersonList());
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model,
                CommandResult.showUpdatedPerson(expectedMessage, editedPerson), expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withTags(VALID_TAG_HUSBAND).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model,
                CommandResult.showUpdatedPerson(expectedMessage, editedPerson), expectedModel);
    }

    @Test
    public void execute_duplicatePhoneWithDifferentName_success() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(firstPerson).withPhone(secondPerson.getPhone().value).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withPhone(secondPerson.getPhone().value).build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(firstPerson, editedPerson);

        assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                CommandResult.showUpdatedPerson(
                        String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)),
                        editedPerson),
                expectedModel);
    }

    @Test
    public void execute_nameCaseAndSpacingOnlyChange_editsWithoutConfirmation() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String normalizedNameVariant = firstPerson.getName().fullName.toLowerCase(Locale.ROOT)
                .replace(" ", "   ") + " ";
        Person editedPerson = new PersonBuilder(firstPerson).withName(normalizedNameVariant).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(normalizedNameVariant).build();

        CommandResult result = new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        assertEquals(String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)),
                result.getFeedbackToUser());
        assertEquals(editedPerson, result.getPersonToView().orElseThrow());
        assertTrue(result.getInputRequest().isEmpty());
        assertTrue(model.getAddressBook().getPersonList().contains(editedPerson));
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model,
                CommandResult.showUpdatedPerson(expectedMessage, editedPerson), expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model,
                CommandResult.showUpdatedPerson(expectedMessage, editedPerson), expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        List<Person> displayedPersons = List.copyOf(model.getFilteredPersonList());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        CommandException exception = assertThrows(
                CommandException.class, EditCommand.MESSAGE_DUPLICATE_PERSON, () -> editCommand.execute(model));

        assertEquals(firstPerson, exception.getPersonToView().orElseThrow());
        assertEquals(displayedPersons, model.getFilteredPersonList());
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        List<Person> displayedPersons = List.copyOf(model.getFilteredPersonList());

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        CommandException exception = assertThrows(
                CommandException.class, EditCommand.MESSAGE_DUPLICATE_PERSON, () -> editCommand.execute(model));

        assertEquals(personInList, exception.getPersonToView().orElseThrow());
        assertEquals(displayedPersons, model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Checks an index outside the displayed list but inside the stored address book.
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_visitDateOnly_preservesOtherFields() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personWithNotes = new PersonBuilder(original).withNote(VALID_NOTE_AMY).build();
        model.setPerson(original, personWithNotes);
        Person editedPerson = new PersonBuilder(personWithNotes).withVisitDate(VALID_VISIT_DATE_BOB).build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withVisitDate(VALID_VISIT_DATE_BOB).build());

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(personWithNotes, editedPerson);
        assertCommandSuccess(command, model,
                CommandResult.showUpdatedPerson(
                        String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)),
                        editedPerson),
                expectedModel);
    }

    @Test
    public void execute_otherFieldsWithPreviousDates_preservesVisitRecord() {
        Person original = setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);
        Person editedPerson = new PersonBuilder(original).withName("Updated Patient").withPhone("92345678")
                .withEmail(VALID_EMAIL_BOB).withAddress("Updated address").withNote(REPLACEMENT_NOTE)
                .withTags(VALID_TAG_HUSBAND).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        descriptor.setVisitDate(null);

        assertEditSuccess(INDEX_FIRST_PERSON, original, editedPerson, descriptor);
        assertEquals(original.getVisitDate(), model.getFilteredPersonList().getFirst().getVisitDate());
        assertEquals(original.getPreviousVisitDates(),
                model.getFilteredPersonList().getFirst().getPreviousVisitDates());
    }

    @Test
    public void execute_pastDateAfterPreviousDates_preservesPreviousDates() {
        Person original = setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);
        Person editedPerson = new PersonBuilder(original).withVisitDate(VALID_VISIT_DATE_BOB).build();

        assertEditSuccess(INDEX_FIRST_PERSON, original, editedPerson,
                new EditPersonDescriptorBuilder().withVisitDate(VALID_VISIT_DATE_BOB).build());
        assertEquals(original.getPreviousVisitDates(),
                model.getFilteredPersonList().getFirst().getPreviousVisitDates());
    }

    @Test
    public void execute_dateAtOrBeforeLatestPreviousDate_preservesDataFilterAndSort() {
        // EP: A replacement current visit before or equal to the latest history entry is invalid.
        // Boundary: The one-minute neighbor below and exact equality distinguish strict chronology.
        Person original = setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);
        model.updateFilteredPersonList(person -> person.equals(original));
        model.sortFilteredPersonList(Comparator.comparing((Person person) -> person.getVisitDate().value).reversed());

        for (String visitDate : List.of("1/1/2024 0900", "1/1/2024 0901", "2/1/2024 0859",
                "2/1/2024 0900", "1/1/2023 0900")) {
            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate(visitDate)
                    .withNote(REPLACEMENT_NOTE).build();
            assertCommandFailure(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                    String.format(EditCommand.MESSAGE_VISIT_DATE_NOT_AFTER_PREVIOUS, "2/1/2024 0900"));
        }
    }

    @Test
    public void execute_dateBeforePreviousDate_reportsLatestDate() {
        setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withVisitDate("1/1/2024 0900").build());
        String expectedMessage = "The current visit date and time must be later than the latest previous visit "
                + "(2/1/2024 0900).";

        assertThrows(CommandException.class, expectedMessage, () -> command.execute(model));
    }

    @Test
    public void execute_sameDayAfterLatestPreviousDate_success() {
        // Boundary: One minute after the latest history entry is valid, even on the same calendar day.
        Person original = setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);
        Person editedPerson = new PersonBuilder(original).withVisitDate("2/1/2024 0901").build();

        assertEditSuccess(INDEX_FIRST_PERSON, original, editedPerson,
                new EditPersonDescriptorBuilder().withVisitDate("2/1/2024 0901").build());
    }

    @Test
    public void execute_sameVisitDate_success() {
        Person original = setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withVisitDate(original.getVisitDate().toString()).build());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess(command, model,
                CommandResult.showUpdatedPerson(
                        String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(original)), original),
                expectedModel);
    }

    @Test
    public void execute_duplicateVisitDate_throwsCommandException() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withVisitDate(firstPerson.getVisitDate().toString()).build();
        String expectedMessage = String.format(EditCommand.MESSAGE_DUPLICATE_VISIT_DATE,
                firstPerson.getVisitDate());

        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        List<Person> displayedPersons = List.copyOf(model.getFilteredPersonList());
        CommandException exception = assertThrows(CommandException.class, expectedMessage, () ->
                new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model));
        assertEquals(originalAddressBook, model.getAddressBook());
        assertEquals(firstPerson, exception.getPersonToView().orElseThrow());
        assertEquals(displayedPersons, model.getFilteredPersonList());
        assertTrue(model.getAddressBook().getPersonList().contains(secondPerson));
    }

    @Test
    public void execute_visitDateAndEmptyEmail_success() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(original).withVisitDate(VALID_VISIT_DATE_BOB).withEmail("").build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withVisitDate(VALID_VISIT_DATE_BOB).withEmail("").build());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, editedPerson);

        assertCommandSuccess(command, model,
                CommandResult.showUpdatedPerson(
                        String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)),
                        editedPerson),
                expectedModel);
    }

    @Test
    public void execute_sortedList_editsDisplayedPerson() {
        Person storedSecondPerson = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person earlierPerson = new PersonBuilder(storedSecondPerson).withVisitDate(VALID_VISIT_DATE_BOB).build();
        model.setPerson(storedSecondPerson, earlierPerson);
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        assertEquals(earlierPerson, model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));

        Person editedPerson = new PersonBuilder(earlierPerson).withVisitDate(VALID_VISIT_DATE_AMY).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(earlierPerson, editedPerson);
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withVisitDate(VALID_VISIT_DATE_AMY).build());

        assertCommandSuccess(command, model,
                CommandResult.showUpdatedPerson(
                        String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)),
                        editedPerson),
                expectedModel);
        assertEquals(editedPerson, model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased()));
    }

    @Test
    public void execute_descriptorChangedAfterConstruction_preservesCopiedVisitDate() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate(VALID_VISIT_DATE_BOB).build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON, descriptor);
        descriptor.setVisitDate(new VisitDate(VALID_VISIT_DATE_AMY));
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(original).withVisitDate(VALID_VISIT_DATE_BOB).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, editedPerson);

        assertCommandSuccess(command, model,
                CommandResult.showUpdatedPerson(
                        String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)),
                        editedPerson),
                expectedModel);
    }

    @Test
    public void execute_noteOnly_preservesOtherFields() {
        // EP: A nonempty note is replaced while the other fields and history remain unchanged.
        Person original = setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);
        Person editedPerson = new PersonBuilder(original).withNote(REPLACEMENT_NOTE).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withNote(REPLACEMENT_NOTE).build();

        assertEditSuccess(INDEX_FIRST_PERSON, original, editedPerson, descriptor);
    }

    @Test
    public void execute_emptyNote_preservesOtherFields() {
        Person original = setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);
        Person editedPerson = new PersonBuilder(original).withNote("").build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withNote("").build();

        assertEditSuccess(INDEX_FIRST_PERSON, original, editedPerson, descriptor);
    }

    @Test
    public void execute_emptyNoteAlreadyEmpty_success() {
        // EP: An explicitly empty note is a valid replacement for an already empty note.
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(new Note(""), original.getNote());

        assertEditSuccess(INDEX_FIRST_PERSON, original, original,
                new EditPersonDescriptorBuilder().withNote("").build());
    }

    @Test
    public void execute_sameNote_success() {
        Person original = setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);

        assertEditSuccess(INDEX_FIRST_PERSON, original, original,
                new EditPersonDescriptorBuilder().withNote(VALID_NOTE_AMY).build());
    }

    @Test
    public void execute_noteOmitted_preservesExistingNote() {
        Person original = setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);
        Person editedPerson = new PersonBuilder(original).withPhone(VALID_PHONE_BOB).build();

        assertEditSuccess(INDEX_FIRST_PERSON, original, editedPerson,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build());
    }

    @Test
    public void execute_visitDateEmailAndNote_success() {
        Person original = setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);
        Person editedPerson = new PersonBuilder(original).withVisitDate(VALID_VISIT_DATE_BOB)
                .withEmail(VALID_EMAIL_BOB).withNote(REPLACEMENT_NOTE).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate(VALID_VISIT_DATE_BOB)
                .withEmail(VALID_EMAIL_BOB).withNote(REPLACEMENT_NOTE).build();

        assertEditSuccess(INDEX_FIRST_PERSON, original, editedPerson, descriptor);
    }

    @Test
    public void execute_emptyEmailAndNote_successWithAndWithoutVisitDate() {
        Person original = setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);
        Person clearedPerson = new PersonBuilder(original).withEmail("").withNote("").build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withEmail("").withNote("").build();
        assertEditSuccess(INDEX_FIRST_PERSON, original, clearedPerson, descriptor);

        model.setPerson(clearedPerson, original);
        descriptor.setVisitDate(new VisitDate(VALID_VISIT_DATE_BOB));
        Person rescheduledPerson = new PersonBuilder(clearedPerson).withVisitDate(VALID_VISIT_DATE_BOB).build();
        assertEditSuccess(INDEX_FIRST_PERSON, original, rescheduledPerson, descriptor);
    }

    @Test
    public void execute_noteFilteredList_editsDisplayedPersonAndRestoresFullList() {
        Person original = setExistingNoteAndPreviousDates(INDEX_SECOND_PERSON);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person editedPerson = new PersonBuilder(original).withNote(REPLACEMENT_NOTE).build();

        assertEditSuccess(INDEX_FIRST_PERSON, original, editedPerson,
                new EditPersonDescriptorBuilder().withNote(REPLACEMENT_NOTE).build());
        assertEquals(editedPerson, model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased()));
    }

    @Test
    public void execute_noteSortedList_editsDisplayedPersonAndRestoresStoredOrder() {
        Person original = setExistingNoteAndPreviousDates(INDEX_SECOND_PERSON);
        Person earlierPerson = new PersonBuilder(original).withVisitDate(VALID_VISIT_DATE_BOB).build();
        model.setPerson(original, earlierPerson);
        model.sortFilteredPersonList(Comparator.comparing(person -> person.getVisitDate().value));
        assertEquals(earlierPerson, model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
        Person editedPerson = new PersonBuilder(earlierPerson).withNote(REPLACEMENT_NOTE).build();

        assertEditSuccess(INDEX_FIRST_PERSON, earlierPerson, editedPerson,
                new EditPersonDescriptorBuilder().withNote(REPLACEMENT_NOTE).build());
        assertEquals(editedPerson, model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased()));
    }

    @Test
    public void execute_duplicateName_requestsConfirmationWithoutEditing() throws Exception {
        Person firstPerson = setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(firstPerson.getName().fullName).withNote(REPLACEMENT_NOTE).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        CommandResult result = new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);

        String expectedPrompt = String.format(EditCommand.MESSAGE_WARNING_DUPLICATE_NAME, firstPerson.getName())
                + " " + String.format(EditCommand.MESSAGE_CONFIRM_EDIT, firstPerson.getName());
        assertEquals(expectedPrompt, result.getFeedbackToUser());
        assertTrue(result.getInputRequest().isPresent());
        assertEquals(firstPerson, result.getPersonToView().orElseThrow());
        assertFalse(result.shouldSaveAddressBook());
        assertEquals(expectedModel.getAddressBook(), model.getAddressBook());
        assertEquals(expectedModel.getFilteredPersonList(), model.getFilteredPersonList());
        assertTrue(model.getAddressBook().getPersonList().contains(secondPerson));
    }

    @Test
    public void execute_normalizedDuplicateName_requestsConfirmationWithoutEditing() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        String normalizedNameVariant = firstPerson.getName().fullName.toLowerCase(Locale.ROOT)
                .replace(" ", "   ") + " ";
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(normalizedNameVariant).build();

        CommandResult result = new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);

        String expectedPrompt = String.format(EditCommand.MESSAGE_WARNING_DUPLICATE_NAME, normalizedNameVariant)
                + " " + String.format(EditCommand.MESSAGE_CONFIRM_EDIT, normalizedNameVariant);
        assertEquals(expectedPrompt, result.getFeedbackToUser());
        assertTrue(result.getInputRequest().isPresent());
        assertEquals(firstPerson, result.getPersonToView().orElseThrow());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
        assertTrue(model.getAddressBook().getPersonList().contains(secondPerson));
    }

    @Test
    public void execute_duplicateNameHiddenByFilter_previewsMatchWithoutChangingList() throws Exception {
        Person hiddenMatch = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personToEdit = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        List<Person> displayedPersons = List.copyOf(model.getFilteredPersonList());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(hiddenMatch.getName().fullName).build();

        CommandResult result = new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        assertEquals(hiddenMatch, result.getPersonToView().orElseThrow());
        assertEquals(displayedPersons, model.getFilteredPersonList());
        assertTrue(model.getAddressBook().getPersonList().contains(personToEdit));
    }

    @Test
    public void execute_multipleDuplicateNames_displaysFirstMatchWithoutChangingList() throws Exception {
        Person firstMatch = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personToEdit = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person secondMatch = new PersonBuilder(firstMatch).withPhone("91234567")
                .withVisitDate(VALID_VISIT_DATE_BOB).build();
        model.addPerson(secondMatch);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(firstMatch.getName().fullName).build();

        CommandResult result = new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);

        String expectedPrompt = String.format(EditCommand.MESSAGE_WARNING_DUPLICATE_NAMES,
                firstMatch.getName(), 2) + " " + String.format(EditCommand.MESSAGE_CONFIRM_EDIT,
                firstMatch.getName());
        assertEquals(expectedPrompt, result.getFeedbackToUser());
        assertTrue(result.getFeedbackToUser().contains("Only one matching patient is shown"));
        assertEquals(firstMatch, result.getPersonToView().orElseThrow());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
        assertTrue(model.getAddressBook().getPersonList().contains(personToEdit));
    }

    @Test
    public void confirmation_duplicateNameConfirmed_editsPerson() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = setExistingNoteAndPreviousDates(INDEX_SECOND_PERSON);
        Person editedPerson = new PersonBuilder(secondPerson).withName(firstPerson.getName().fullName).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(firstPerson.getName().fullName).build();
        CommandResult requestResult = new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);

        CommandResult confirmedResult = requestResult.getInputRequest().orElseThrow().respond("Y", model);

        assertEquals(String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)),
                confirmedResult.getFeedbackToUser());
        assertEquals(editedPerson, confirmedResult.getPersonToView().orElseThrow());
        assertTrue(confirmedResult.shouldSaveAddressBook());
        assertTrue(model.getAddressBook().getPersonList().contains(editedPerson));
        assertFalse(model.getAddressBook().getPersonList().contains(secondPerson));
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void confirmation_duplicateNameNotConfirmed_cancelsEdit() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = setExistingNoteAndPreviousDates(INDEX_SECOND_PERSON);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(firstPerson.getName().fullName).build();
        CommandResult requestResult = new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);

        CommandResult cancelledResult = requestResult.getInputRequest().orElseThrow().respond("n", model);

        assertEquals(EditCommand.MESSAGE_EDIT_CANCELLED, cancelledResult.getFeedbackToUser());
        assertFalse(cancelledResult.shouldSaveAddressBook());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
        assertTrue(model.getAddressBook().getPersonList().contains(secondPerson));
    }

    @Test
    public void execute_existingDuplicateNameWithoutNameChange_editsWithoutConfirmation() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person sameNameDifferentPhone = new PersonBuilder(firstPerson).withPhone("91234567")
                .withVisitDate(VALID_VISIT_DATE_BOB).build();
        model.addPerson(sameNameDifferentPhone);
        Index duplicateNameIndex = Index.fromOneBased(model.getFilteredPersonList().size());
        Person editedPerson = new PersonBuilder(sameNameDifferentPhone).withNote(REPLACEMENT_NOTE).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withNote(REPLACEMENT_NOTE).build();

        CommandResult result = new EditCommand(duplicateNameIndex, descriptor).execute(model);

        assertEquals(String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)),
                result.getFeedbackToUser());
        assertTrue(result.getInputRequest().isEmpty());
        assertTrue(model.getAddressBook().getPersonList().contains(editedPerson));
    }

    @Test
    public void confirmation_capturedPersonChanged_throwsCommandExceptionWithoutEditingReplacement() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(firstPerson.getName().fullName).build();
        CommandResult requestResult = new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);
        Person replacement = new PersonBuilder(secondPerson).withNote(REPLACEMENT_NOTE).build();
        model.setPerson(secondPerson, replacement);

        assertThrows(CommandException.class, EditCommand.MESSAGE_PERSON_NO_LONGER_EXISTS, () ->
                requestResult.getInputRequest()
                        .orElseThrow()
                        .respond("y", model));

        assertTrue(model.getAddressBook().getPersonList().contains(replacement));
    }

    @Test
    public void confirmation_trueDuplicateAppeared_throwsCommandExceptionWithoutEditing() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(firstPerson.getName().fullName).build();
        CommandResult requestResult = new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);
        Person trueDuplicate = new PersonBuilder(secondPerson).withName(firstPerson.getName().fullName)
                .withVisitDate(VALID_VISIT_DATE_BOB).build();
        model.addPerson(trueDuplicate);

        CommandException exception = assertThrows(CommandException.class, EditCommand.MESSAGE_DUPLICATE_PERSON, () ->
                requestResult.getInputRequest()
                        .orElseThrow()
                        .respond("y", model));

        assertTrue(model.getAddressBook().getPersonList().contains(secondPerson));
        assertTrue(model.getAddressBook().getPersonList().contains(trueDuplicate));
        assertEquals(trueDuplicate, exception.getPersonToView().orElseThrow());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void confirmation_visitDateConflictAppeared_throwsCommandExceptionWithoutEditing() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(firstPerson.getName().fullName).withVisitDate(VALID_VISIT_DATE_AMY).build();
        CommandResult requestResult = new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);
        Person conflictingVisit = new PersonBuilder().withName("Visit Conflict").withPhone("92345678")
                .withVisitDate(VALID_VISIT_DATE_AMY).build();
        model.addPerson(conflictingVisit);

        String expectedMessage = String.format(EditCommand.MESSAGE_DUPLICATE_VISIT_DATE,
                VALID_VISIT_DATE_AMY);
        CommandException exception = assertThrows(CommandException.class, expectedMessage, () ->
                requestResult.getInputRequest()
                        .orElseThrow()
                        .respond("y", model));

        assertTrue(model.getAddressBook().getPersonList().contains(secondPerson));
        assertTrue(model.getAddressBook().getPersonList().contains(conflictingVisit));
        assertEquals(conflictingVisit, exception.getPersonToView().orElseThrow());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_visitMovedToFullDay_requestsConfirmationWithoutEditing() throws Exception {
        Index otherDayIndex = addPersonOnOtherDay();
        Person otherDayPerson = model.getFilteredPersonList().get(otherDayIndex.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate(FULL_DAY_VISIT_DATE).build();

        CommandResult result = new EditCommand(otherDayIndex, descriptor).execute(model);

        String expectedPrompt = String.format(EditCommand.MESSAGE_WARNING_FULL_DAY, Model.MAX_VISITS_PER_DAY,
                "Thu, 1 Jan 2026") + " " + String.format(EditCommand.MESSAGE_CONFIRM_EDIT,
                otherDayPerson.getName());
        assertEquals(expectedPrompt, result.getFeedbackToUser());
        assertTrue(result.getInputRequest().isPresent());
        assertFalse(result.shouldSaveAddressBook());
        assertTrue(model.getAddressBook().getPersonList().contains(otherDayPerson));
    }

    @Test
    public void confirmation_visitMovedToFullDayConfirmed_editsPerson() throws Exception {
        Index otherDayIndex = addPersonOnOtherDay();
        Person otherDayPerson = model.getFilteredPersonList().get(otherDayIndex.getZeroBased());
        Person editedPerson = new PersonBuilder(otherDayPerson).withVisitDate(FULL_DAY_VISIT_DATE).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate(FULL_DAY_VISIT_DATE).build();
        CommandResult requestResult = new EditCommand(otherDayIndex, descriptor).execute(model);

        CommandResult confirmedResult = requestResult.getInputRequest().orElseThrow().respond("y", model);

        assertEquals(String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)),
                confirmedResult.getFeedbackToUser());
        assertEquals(editedPerson, confirmedResult.getPersonToView().orElseThrow());
        assertTrue(confirmedResult.shouldSaveAddressBook());
        assertTrue(model.getAddressBook().getPersonList().contains(editedPerson));
        assertFalse(model.getAddressBook().getPersonList().contains(otherDayPerson));
    }

    @Test
    public void confirmation_visitMovedToFullDayNotConfirmed_cancelsEdit() throws Exception {
        Index otherDayIndex = addPersonOnOtherDay();
        Person otherDayPerson = model.getFilteredPersonList().get(otherDayIndex.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate(FULL_DAY_VISIT_DATE).build();
        CommandResult requestResult = new EditCommand(otherDayIndex, descriptor).execute(model);

        CommandResult cancelledResult = requestResult.getInputRequest().orElseThrow().respond("n", model);

        assertEquals(EditCommand.MESSAGE_EDIT_CANCELLED, cancelledResult.getFeedbackToUser());
        assertFalse(cancelledResult.shouldSaveAddressBook());
        assertTrue(model.getAddressBook().getPersonList().contains(otherDayPerson));
    }

    @Test
    public void execute_visitMovedToDayBelowLimit_editsWithoutConfirmation() throws Exception {
        model.deletePerson(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
        Index otherDayIndex = addPersonOnOtherDay();
        Person otherDayPerson = model.getFilteredPersonList().get(otherDayIndex.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate(FULL_DAY_VISIT_DATE).build();

        CommandResult result = new EditCommand(otherDayIndex, descriptor).execute(model);

        assertTrue(result.getInputRequest().isEmpty());
        assertFalse(model.getAddressBook().getPersonList().contains(otherDayPerson));
    }

    @Test
    public void execute_visitTimeChangedWithinFullDay_editsWithoutConfirmation() throws Exception {
        // The first person is already one of the seven visits on the full day, so the day's total does not grow
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate(FULL_DAY_VISIT_DATE).build();

        CommandResult result = new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        assertTrue(result.getInputRequest().isEmpty());
        assertTrue(result.shouldSaveAddressBook());
    }

    @Test
    public void confirmation_duplicateNameOnFullDay_requestsSingleConfirmation() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Index otherDayIndex = addPersonOnOtherDay();
        Person otherDayPerson = model.getFilteredPersonList().get(otherDayIndex.getZeroBased());
        Person editedPerson = new PersonBuilder(otherDayPerson).withName(firstPerson.getName().fullName)
                .withVisitDate(FULL_DAY_VISIT_DATE).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(firstPerson.getName().fullName)
                .withVisitDate(FULL_DAY_VISIT_DATE).build();
        CommandResult requestResult = new EditCommand(otherDayIndex, descriptor).execute(model);

        String expectedPrompt = String.format(EditCommand.MESSAGE_WARNING_DUPLICATE_NAME, editedPerson.getName())
                + " " + String.format(EditCommand.MESSAGE_WARNING_FULL_DAY, Model.MAX_VISITS_PER_DAY,
                "Thu, 1 Jan 2026") + " " + String.format(EditCommand.MESSAGE_CONFIRM_EDIT, editedPerson.getName());
        assertEquals(expectedPrompt, requestResult.getFeedbackToUser());
        assertTrue(requestResult.getInputRequest().isPresent());
        assertTrue(model.getAddressBook().getPersonList().contains(otherDayPerson));

        CommandResult confirmedResult = requestResult.getInputRequest().orElseThrow().respond("y", model);

        assertEquals(String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)),
                confirmedResult.getFeedbackToUser());
        assertEquals(editedPerson, confirmedResult.getPersonToView().orElseThrow());
        assertTrue(confirmedResult.getInputRequest().isEmpty());
        assertTrue(model.getAddressBook().getPersonList().contains(editedPerson));
    }

    @Test
    public void confirmation_duplicateNameOnFullDayNotConfirmed_cancelsEdit() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Index otherDayIndex = addPersonOnOtherDay();
        Person otherDayPerson = model.getFilteredPersonList().get(otherDayIndex.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(firstPerson.getName().fullName)
                .withVisitDate(FULL_DAY_VISIT_DATE).build();
        CommandResult requestResult = new EditCommand(otherDayIndex, descriptor).execute(model);

        CommandResult cancelledResult = requestResult.getInputRequest().orElseThrow().respond("n", model);

        assertEquals(EditCommand.MESSAGE_EDIT_CANCELLED, cancelledResult.getFeedbackToUser());
        assertFalse(cancelledResult.shouldSaveAddressBook());
        assertTrue(model.getAddressBook().getPersonList().contains(otherDayPerson));
    }

    @Test
    public void execute_noteInvalidDisplayedIndex_preservesState() {
        setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withNote(REPLACEMENT_NOTE).build();

        assertCommandFailure(new EditCommand(INDEX_SECOND_PERSON, descriptor), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_descriptorChangedAfterConstruction_preservesCopiedNote() {
        Person original = setExistingNoteAndPreviousDates(INDEX_FIRST_PERSON);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withNote(REPLACEMENT_NOTE).build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON, descriptor);
        descriptor.setNote(new Note(""));
        Person editedPerson = new PersonBuilder(original).withNote(REPLACEMENT_NOTE).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, editedPerson);

        assertCommandSuccess(command, model,
                CommandResult.showUpdatedPerson(
                        String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)),
                        editedPerson),
                expectedModel);
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

    /**
     * Gives the patient a nonempty note and previous dates before testing their preservation during an edit.
     */
    private Person setExistingNoteAndPreviousDates(Index displayedIndex) {
        Person original = model.getFilteredPersonList().get(displayedIndex.getZeroBased());
        Person personWithNote = new PersonBuilder(original).withNote(VALID_NOTE_AMY)
                .withPreviousVisitDates("1/1/2024 0900", "2/1/2024 0900").build();
        model.setPerson(original, personWithNote);
        return personWithNote;
    }

    /**
     * Adds a person visited on a day other than the typical persons' full day and returns its displayed index.
     */
    private Index addPersonOnOtherDay() {
        model.addPerson(new PersonBuilder().withVisitDate("2/1/2026 0900").build());
        return Index.fromOneBased(model.getFilteredPersonList().size());
    }

    /**
     * Checks the complete edited record, success feedback, and restoration of the full displayed list.
     */
    private void assertEditSuccess(Index displayedIndex, Person original, Person editedPerson,
            EditPersonDescriptor descriptor) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, editedPerson);
        EditCommand command = new EditCommand(displayedIndex, descriptor);

        assertCommandSuccess(command, model,
                CommandResult.showUpdatedPerson(
                        String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)),
                        editedPerson),
                expectedModel);
    }
}
