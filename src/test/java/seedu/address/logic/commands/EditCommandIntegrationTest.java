package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NOTE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_VISIT_DATE_BOB;
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
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests edit parsing, command execution, and persistence through the real logic and storage components.
 */
public class EditCommandIntegrationTest {

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
