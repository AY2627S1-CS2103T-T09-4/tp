package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NOTE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_VISIT_DATE_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.VisitDate;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests edit parsing, command execution, and persistence through the real logic and storage components.
 */
public class EditCommandIntegrationTest {

    private static final String REPLACEMENT_NOTE = "Review medication";

    @TempDir
    private Path testFolder;

    private Model model;
    private LogicManager logic;
    private JsonAddressBookStorage addressBookStorage;
    private Person originalPerson;

    @BeforeEach
    public void setUp() throws IOException {
        originalPerson = new PersonBuilder().withNote(VALID_NOTE_AMY).withTags("existing").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(originalPerson);
        model = new ModelManager(addressBook, new UserPrefs());
        addressBookStorage = new JsonAddressBookStorage(testFolder.resolve("addressBook.json"));
        addressBookStorage.saveAddressBook(model.getAddressBook());
        StorageManager storage = new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json")));
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_visitDateOnly_persistsUpdatedRecord() throws Exception {
        Person expectedPerson = new PersonBuilder(originalPerson).withVisitDate(VALID_VISIT_DATE_BOB).build();

        CommandResult result = logic.execute("edit 1 vd/" + VALID_VISIT_DATE_BOB);

        assertEquals(String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(expectedPerson)),
                result.getFeedbackToUser());
        assertStoredPerson(expectedPerson);
    }

    @Test
    public void execute_visitDateAndEmptyEmail_persistsUpdatedRecord() throws Exception {
        Person expectedPerson = new PersonBuilder(originalPerson)
                .withVisitDate(VALID_VISIT_DATE_BOB).withEmail("").build();

        logic.execute("edit 1 e/ vd/" + VALID_VISIT_DATE_BOB);

        assertStoredPerson(expectedPerson);
    }

    @Test
    public void execute_emailOnly_preservesVisitDateInSavedRecord() throws Exception {
        Person expectedPerson = new PersonBuilder(originalPerson).withEmail("new@example.com").build();

        logic.execute("edit 1 e/new@example.com");

        assertStoredPerson(expectedPerson);
    }

    @Test
    public void execute_emptyEmailAndTags_preservesVisitDateInSavedRecord() throws Exception {
        Person expectedPerson = new PersonBuilder(originalPerson).withEmail("").withTags().build();

        logic.execute("edit 1 e/ t/");

        assertStoredPerson(expectedPerson);
    }

    @Test
    public void execute_noteOnly_persistsReplacement() throws Exception {
        Person expectedPerson = new PersonBuilder(originalPerson).withNote(REPLACEMENT_NOTE).build();

        CommandResult result = logic.execute("edit 1 note/" + REPLACEMENT_NOTE);

        assertEquals(String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(expectedPerson)),
                result.getFeedbackToUser());
        assertStoredPerson(expectedPerson);
    }

    @Test
    public void execute_emptyNote_persistsClearedNote() throws Exception {
        Person expectedPerson = new PersonBuilder(originalPerson).withNote("").build();

        logic.execute("edit 1 note/");

        assertStoredPerson(expectedPerson);
        logic.execute("edit 1 note/   ");
        assertStoredPerson(expectedPerson);
    }

    @Test
    public void execute_sameNote_persistsUnchangedRecord() throws Exception {
        logic.execute("edit 1 note/" + VALID_NOTE_AMY);

        assertStoredPerson(originalPerson);
    }

    @Test
    public void execute_visitDateEmailAndNote_persistsUpdatedRecord() throws Exception {
        Person expectedPerson = new PersonBuilder(originalPerson).withVisitDate(VALID_VISIT_DATE_BOB)
                .withEmail(VALID_EMAIL_BOB).withNote(REPLACEMENT_NOTE).build();

        logic.execute("edit 1 note/" + REPLACEMENT_NOTE + " vd/" + VALID_VISIT_DATE_BOB + " e/" + VALID_EMAIL_BOB);

        assertStoredPerson(expectedPerson);
    }

    @Test
    public void execute_emptyEmailAndNote_persistsClearedFields() throws Exception {
        Person expectedPerson = new PersonBuilder(originalPerson).withEmail("").withNote("").build();

        logic.execute("edit 1 e/ note/");

        assertStoredPerson(expectedPerson);
    }

    @Test
    public void execute_visitDateWithEmptyEmailAndNote_persistsUpdatedRecord() throws Exception {
        Person expectedPerson = new PersonBuilder(originalPerson).withVisitDate(VALID_VISIT_DATE_BOB)
                .withEmail("").withNote("").build();

        logic.execute("edit 1 vd/" + VALID_VISIT_DATE_BOB + " e/ note/");

        assertStoredPerson(expectedPerson);
    }

    @Test
    public void execute_rejectedNotes_preservesModelAndStorage() throws Exception {
        String duplicateMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NOTE);
        assertRejectedEdit("edit 1 note/" + VALID_NOTE_AMY + " note/" + REPLACEMENT_NOTE,
                ParseException.class, duplicateMessage);
        assertRejectedEdit("edit 1 note/" + VALID_NOTE_AMY + " note/" + VALID_NOTE_AMY,
                ParseException.class, duplicateMessage);
        assertRejectedEdit("edit 1 note/ note/", ParseException.class, duplicateMessage);
        assertRejectedEdit("edit 1 note/ note/" + VALID_NOTE_AMY, ParseException.class, duplicateMessage);
        assertRejectedEdit("edit 2 note/" + REPLACEMENT_NOTE, CommandException.class,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertRejectedEdit("edit 0 note/", ParseException.class,
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
    }

    @Test
    public void execute_invalidFieldWithNote_preservesModelAndStorage() throws Exception {
        assertRejectedEdit("edit 1 note/Review medication n/James&", ParseException.class, Name.MESSAGE_CONSTRAINTS);
        assertRejectedEdit("edit 1 note/Review medication p/911a", ParseException.class, Phone.MESSAGE_CONSTRAINTS);
        assertRejectedEdit("edit 1 note/Review medication a/", ParseException.class, Address.MESSAGE_CONSTRAINTS);
        assertRejectedEdit("edit 1 note/Review medication e/bob!yahoo",
                ParseException.class, Email.MESSAGE_CONSTRAINTS);
        assertRejectedEdit("edit 1 note/Review medication vd/31/2/2026 1000",
                ParseException.class, VisitDate.MESSAGE_CONSTRAINTS);
        assertRejectedEdit("edit 1 note/Review medication t/hubby*", ParseException.class, Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void execute_rejectedEdits_preservesModelAndStorage() throws Exception {
        assertRejectedEdit("edit 1", ParseException.class, EditCommand.MESSAGE_NOT_EDITED);
        assertRejectedEdit("edit 1 vd/31/2/2026 1000", ParseException.class, VisitDate.MESSAGE_CONSTRAINTS);
        assertRejectedEdit("edit 1 vd/", ParseException.class, VisitDate.MESSAGE_CONSTRAINTS);
        assertRejectedEdit("edit 2 vd/" + VALID_VISIT_DATE_BOB, CommandException.class,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Checks that the model and reloaded data contain exactly the expected patient.
     */
    private void assertStoredPerson(Person expectedPerson) throws Exception {
        AddressBook expectedAddressBook = new AddressBook();
        expectedAddressBook.addPerson(expectedPerson);
        assertEquals(expectedAddressBook, model.getAddressBook());
        assertEquals(expectedAddressBook, new AddressBook(addressBookStorage.readAddressBook().orElseThrow()));
    }

    /**
     * Checks that a rejected edit preserves both model state and the saved file.
     */
    private void assertRejectedEdit(String commandText, Class<? extends Exception> exceptionClass,
            String expectedMessage) throws IOException {
        AddressBook before = new AddressBook(model.getAddressBook());
        String savedData = Files.readString(addressBookStorage.getAddressBookFilePath());

        assertThrows(exceptionClass, expectedMessage, () -> logic.execute(commandText));

        assertEquals(before, model.getAddressBook());
        assertEquals(savedData, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }
}
