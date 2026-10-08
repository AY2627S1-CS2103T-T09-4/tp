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
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Comparator;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
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

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
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

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
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

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
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
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)), expectedModel);
    }

    @Test
    public void execute_sameVisitDate_success() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withVisitDate(original.getVisitDate().toString()).build());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess(command, model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(original)), expectedModel);
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
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)), expectedModel);
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
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)), expectedModel);
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
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)), expectedModel);
    }

    @Test
    public void execute_noteOnly_preservesOtherFields() {
        Person original = setExistingNote(INDEX_FIRST_PERSON);
        Person editedPerson = new PersonBuilder(original).withNote(REPLACEMENT_NOTE).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withNote(REPLACEMENT_NOTE).build();

        assertEditSuccess(INDEX_FIRST_PERSON, original, editedPerson, descriptor);
    }

    @Test
    public void execute_emptyNote_preservesOtherFields() {
        Person original = setExistingNote(INDEX_FIRST_PERSON);
        Person editedPerson = new PersonBuilder(original).withNote("").build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withNote("").build();

        assertEditSuccess(INDEX_FIRST_PERSON, original, editedPerson, descriptor);
    }

    @Test
    public void execute_emptyNoteAlreadyEmpty_success() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(new Note(""), original.getNote());

        assertEditSuccess(INDEX_FIRST_PERSON, original, original,
                new EditPersonDescriptorBuilder().withNote("").build());
    }

    @Test
    public void execute_sameNote_success() {
        Person original = setExistingNote(INDEX_FIRST_PERSON);

        assertEditSuccess(INDEX_FIRST_PERSON, original, original,
                new EditPersonDescriptorBuilder().withNote(VALID_NOTE_AMY).build());
    }

    @Test
    public void execute_noteOmitted_preservesExistingNote() {
        Person original = setExistingNote(INDEX_FIRST_PERSON);
        Person editedPerson = new PersonBuilder(original).withPhone(VALID_PHONE_BOB).build();

        assertEditSuccess(INDEX_FIRST_PERSON, original, editedPerson,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build());
    }

    @Test
    public void execute_visitDateEmailAndNote_success() {
        Person original = setExistingNote(INDEX_FIRST_PERSON);
        Person editedPerson = new PersonBuilder(original).withVisitDate(VALID_VISIT_DATE_BOB)
                .withEmail(VALID_EMAIL_BOB).withNote(REPLACEMENT_NOTE).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate(VALID_VISIT_DATE_BOB)
                .withEmail(VALID_EMAIL_BOB).withNote(REPLACEMENT_NOTE).build();

        assertEditSuccess(INDEX_FIRST_PERSON, original, editedPerson, descriptor);
    }

    @Test
    public void execute_emptyEmailAndNote_successWithAndWithoutVisitDate() {
        Person original = setExistingNote(INDEX_FIRST_PERSON);
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
        Person original = setExistingNote(INDEX_SECOND_PERSON);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person editedPerson = new PersonBuilder(original).withNote(REPLACEMENT_NOTE).build();

        assertEditSuccess(INDEX_FIRST_PERSON, original, editedPerson,
                new EditPersonDescriptorBuilder().withNote(REPLACEMENT_NOTE).build());
        assertEquals(editedPerson, model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased()));
    }

    @Test
    public void execute_noteSortedList_editsDisplayedPersonAndRestoresStoredOrder() {
        Person original = setExistingNote(INDEX_SECOND_PERSON);
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
    public void execute_duplicateNameAndNote_preservesState() {
        Person firstPerson = setExistingNote(INDEX_FIRST_PERSON);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(firstPerson.getName().fullName).withNote(REPLACEMENT_NOTE).build();

        assertCommandFailure(new EditCommand(INDEX_SECOND_PERSON, descriptor), model,
                EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_noteInvalidDisplayedIndex_preservesState() {
        setExistingNote(INDEX_FIRST_PERSON);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withNote(REPLACEMENT_NOTE).build();

        assertCommandFailure(new EditCommand(INDEX_SECOND_PERSON, descriptor), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_descriptorChangedAfterConstruction_preservesCopiedNote() {
        Person original = setExistingNote(INDEX_FIRST_PERSON);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withNote(REPLACEMENT_NOTE).build();
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON, descriptor);
        descriptor.setNote(new Note(""));
        Person editedPerson = new PersonBuilder(original).withNote(REPLACEMENT_NOTE).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, editedPerson);

        assertCommandSuccess(command, model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)), expectedModel);
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
     * Gives the patient at the displayed index a nonempty note before testing an edit.
     */
    private Person setExistingNote(Index displayedIndex) {
        Person original = model.getFilteredPersonList().get(displayedIndex.getZeroBased());
        Person personWithNote = new PersonBuilder(original).withNote(VALID_NOTE_AMY).build();
        model.setPerson(original, personWithNote);
        return personWithNote;
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
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)), expectedModel);
    }
}
