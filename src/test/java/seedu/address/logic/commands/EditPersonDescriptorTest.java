package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NOTE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_VISIT_DATE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_VISIT_DATE_BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Note;
import seedu.address.model.person.VisitDate;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

public class EditPersonDescriptorTest {

    @Test
    public void equals() {
        // same values -> returns true
        EditPersonDescriptor descriptorWithSameValues = new EditPersonDescriptor(DESC_AMY);
        assertTrue(DESC_AMY.equals(descriptorWithSameValues));

        // same object -> returns true
        assertTrue(DESC_AMY.equals(DESC_AMY));

        // null -> returns false
        assertFalse(DESC_AMY.equals(null));

        // different types -> returns false
        assertFalse(DESC_AMY.equals(5));

        // different values -> returns false
        assertFalse(DESC_AMY.equals(DESC_BOB));

        // different name -> returns false
        EditPersonDescriptor editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withName(VALID_NAME_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different phone -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withPhone(VALID_PHONE_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different email -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different address -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different tags -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(DESC_AMY.equals(editedAmy));
    }

    @Test
    public void equals_differentVisitDate_returnsFalse() {
        EditPersonDescriptor editedAmy = new EditPersonDescriptorBuilder(DESC_AMY)
                .withVisitDate(VALID_VISIT_DATE_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));
    }

    @Test
    public void copy_visitDateOnly_preservesField() {
        EditPersonDescriptor original = new EditPersonDescriptorBuilder().withVisitDate(VALID_VISIT_DATE_AMY).build();
        EditPersonDescriptor copy = new EditPersonDescriptor(original);
        assertEquals(original, copy);

        original.setVisitDate(new VisitDate(VALID_VISIT_DATE_BOB));
        assertEquals(new VisitDate(VALID_VISIT_DATE_AMY), copy.getVisitDate().orElseThrow());
        assertFalse(original.equals(copy));
    }

    @Test
    public void isAnyFieldEdited_visitDateOnly_returnsTrue() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        assertFalse(descriptor.isAnyFieldEdited());

        descriptor.setVisitDate(new VisitDate(VALID_VISIT_DATE_AMY));
        assertTrue(descriptor.isAnyFieldEdited());
        assertFalse(descriptor.equals(new EditPersonDescriptor()));
    }

    @Test
    public void equals_noteValues_distinguishesOmittedEmptyAndReplacement() {
        EditPersonDescriptor omitted = new EditPersonDescriptor();
        EditPersonDescriptor empty = new EditPersonDescriptorBuilder().withNote("").build();
        EditPersonDescriptor replacement = new EditPersonDescriptorBuilder().withNote(VALID_NOTE_AMY).build();

        assertFalse(omitted.equals(empty));
        assertFalse(empty.equals(omitted));
        assertFalse(empty.equals(replacement));
        assertFalse(replacement.equals(new EditPersonDescriptorBuilder().withNote("Review medication").build()));
        assertEquals(empty, new EditPersonDescriptorBuilder().withNote("").build());
        assertEquals(replacement, new EditPersonDescriptorBuilder().withNote(VALID_NOTE_AMY).build());
    }

    @Test
    public void copy_note_preservesOmittedEmptyAndReplacement() {
        EditPersonDescriptor omitted = new EditPersonDescriptor();
        EditPersonDescriptor empty = new EditPersonDescriptorBuilder().withNote("").build();
        EditPersonDescriptor replacement = new EditPersonDescriptorBuilder().withNote(VALID_NOTE_AMY).build();

        assertEquals(omitted, new EditPersonDescriptor(omitted));
        assertTrue(new EditPersonDescriptor(omitted).getNote().isEmpty());
        assertEquals(empty, new EditPersonDescriptor(empty));
        assertEquals(new Note(""), new EditPersonDescriptor(empty).getNote().orElseThrow());
        EditPersonDescriptor copy = new EditPersonDescriptor(replacement);
        assertEquals(replacement, copy);
        replacement.setNote(new Note(""));
        assertEquals(new Note(VALID_NOTE_AMY), copy.getNote().orElseThrow());
    }

    @Test
    public void isAnyFieldEdited_noteOnly_returnsTrueForEmptyAndReplacement() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        assertFalse(descriptor.isAnyFieldEdited());
        descriptor.setNote(new Note(""));
        assertTrue(descriptor.isAnyFieldEdited());
        descriptor.setNote(new Note(VALID_NOTE_AMY));
        assertTrue(descriptor.isAnyFieldEdited());
    }

    @Test
    public void builder_note_preservesOmittedAndExplicitValues() {
        assertTrue(new EditPersonDescriptorBuilder().build().getNote().isEmpty());
        EditPersonDescriptor empty = new EditPersonDescriptorBuilder().withNote("").build();
        assertEquals(empty, new EditPersonDescriptorBuilder(empty).build());
        EditPersonDescriptor fromPerson = new EditPersonDescriptorBuilder(
                new PersonBuilder().withNote(VALID_NOTE_AMY).build()).build();
        assertEquals(new Note(VALID_NOTE_AMY), fromPerson.getNote().orElseThrow());
    }

    @Test
    public void toString_note_includesOmittedEmptyAndReplacement() {
        assertTrue(new EditPersonDescriptor().toString().contains("note=null"));
        assertTrue(new EditPersonDescriptorBuilder().withNote("").build().toString().contains("note=, tags="));
        assertTrue(new EditPersonDescriptorBuilder().withNote(VALID_NOTE_AMY).build().toString()
                .contains("note=" + VALID_NOTE_AMY));
    }

    @Test
    public void toStringMethod() {
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptorBuilder()
                .withVisitDate(VALID_VISIT_DATE_AMY).build();
        String expected = EditPersonDescriptor.class.getCanonicalName() + "{name="
                + editPersonDescriptor.getName().orElse(null) + ", phone="
                + editPersonDescriptor.getPhone().orElse(null) + ", email="
                + editPersonDescriptor.getEmail().orElse(null) + ", address="
                + editPersonDescriptor.getAddress().orElse(null) + ", visitDate="
                + editPersonDescriptor.getVisitDate().orElse(null) + ", note="
                + editPersonDescriptor.getNote().orElse(null) + ", tags="
                + editPersonDescriptor.getTags().orElse(null) + "}";
        assertEquals(expected, editPersonDescriptor.toString());
    }
}
