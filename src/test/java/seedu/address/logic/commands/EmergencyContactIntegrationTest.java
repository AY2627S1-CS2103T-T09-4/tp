package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMERGENCY_CONTACT;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.EmergencyContact;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests emergency contact input, command behavior, viewing, and persistence through the application logic.
 */
public class EmergencyContactIntegrationTest {

    private static final String CONTACT = "Mary Tan, 98765432";
    private static final String REPLACEMENT_CONTACT = "David Tan, 87654321";
    private static final String ADD_COMMAND =
            "add n/John Tan p/91234567 a/123 Clementi Road vd/22/10/2026 1000";

    @TempDir
    private Path testFolder;

    private Model model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(testFolder.resolve("patients.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_addWithContact_persistsAndViewsContact() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        Person expected = new PersonBuilder().withName("John Tan").withPhone("91234567")
                .withEmail("").withAddress("123 Clementi Road").withVisitDate("22/10/2026 1000")
                .withEmergencyContact(CONTACT).build();
        assertStoredPerson(expected);

        CommandResult result = logic.execute("view 1");
        assertEquals(expected, result.getPersonToView().orElseThrow());
        assertFalse(result.shouldSaveAddressBook());
    }

    @Test
    public void execute_addWithoutContact_keepsFieldOptional() throws Exception {
        logic.execute(ADD_COMMAND);
        Person patient = model.getFilteredPersonList().getFirst();
        assertTrue(patient.getEmergencyContact().isEmpty());
        assertStoredPerson(patient);
    }

    @Test
    public void execute_contactBeforeOtherFields_parsesBoundariesAndWhitespace() throws Exception {
        logic.execute("add ec/ Mary Tan ,98765432 n/John Tan p/91234567"
                + " a/123 Clementi Road vd/22/10/2026 1000 note/Follow up t/priority");
        Person patient = model.getFilteredPersonList().getFirst();
        assertEquals(CONTACT, patient.getEmergencyContact().toString());
        assertEquals("Follow up", patient.getNote().toString());
        assertStoredPerson(patient);
    }

    @Test
    public void execute_editContact_addsAndReplacesContact() throws Exception {
        logic.execute(ADD_COMMAND);
        Person original = model.getFilteredPersonList().getFirst();

        logic.execute("edit 1 ec/" + CONTACT);
        assertStoredPerson(new PersonBuilder(original).withEmergencyContact(CONTACT).build());

        logic.execute("edit 1 ec/" + REPLACEMENT_CONTACT);
        assertStoredPerson(new PersonBuilder(original).withEmergencyContact(REPLACEMENT_CONTACT).build());
    }

    @Test
    public void execute_unrelatedEditAndNext_preservesContact() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        Person original = model.getFilteredPersonList().getFirst();
        logic.execute("edit 1 note/Review medication");
        Person edited = new PersonBuilder(original).withNote("Review medication").build();
        assertStoredPerson(edited);

        logic.execute("next 1 1/1/2099 1000");
        assertStoredPerson(new PersonBuilder(edited).withVisitDate("1/1/2099 1000").build());
    }

    @Test
    public void execute_findListAndSort_preservesContact() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        Person original = model.getFilteredPersonList().getFirst();
        for (String command : List.of("find John", "sort date", "list")) {
            logic.execute(command);
            assertEquals(List.of(original), model.getFilteredPersonList());
            assertStoredPerson(original);
        }
    }

    @Test
    public void execute_invalidContact_preservesModelAndFile() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        for (String invalid : List.of("", " ", "Mary Tan", "Mary Tan 98765432", "Mary Tan,",
                ",98765432", "Mary Tan, 12", "Mary Tan, abc", "Mary@Tan, 98765432",
                "Mary Tan, 98765432, 87654321")) {
            assertRejectedCommand("edit 1 ec/" + invalid, EmergencyContact.MESSAGE_CONSTRAINTS);
            assertRejectedCommand("add n/Jane Tan p/82345678 a/Other Road vd/23/10/2026 1000 ec/" + invalid,
                    String.format(AddCommand.MESSAGE_NOT_ADDED, EmergencyContact.MESSAGE_CONSTRAINTS));
        }
    }

    @Test
    public void execute_repeatedContact_rejectsDuplicatePrefixes() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        String reason = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMERGENCY_CONTACT);
        assertRejectedCommand("edit 1 ec/" + CONTACT + " ec/" + REPLACEMENT_CONTACT, reason);
        assertRejectedCommand(ADD_COMMAND + " ec/invalid ec/" + CONTACT,
                String.format(AddCommand.MESSAGE_NOT_ADDED, reason));
    }

    @Test
    public void execute_invalidIndex_preservesContactAndFile() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        assertThrows(CommandException.class, () -> logic.execute("edit 2 ec/" + REPLACEMENT_CONTACT));
        assertEquals(before, model.getAddressBook());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_duplicatePatientWithDifferentContact_rejectsDuplicateIdentity() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        assertThrows(CommandException.class, () -> logic.execute(ADD_COMMAND + " ec/" + REPLACEMENT_CONTACT));
        assertEquals(CONTACT, model.getFilteredPersonList().getFirst().getEmergencyContact().toString());
    }

    @Test
    public void execute_filteredEdit_updatesDisplayedPatient() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        logic.execute("add n/Jane Tan p/82345678 a/Other Road vd/23/10/2026 1000");
        logic.execute("find Jane");
        logic.execute("edit 1 ec/" + REPLACEMENT_CONTACT);

        List<Person> patients = storage.readAddressBook().orElseThrow().getPersonList();
        assertEquals(CONTACT, patients.get(0).getEmergencyContact().toString());
        assertEquals(REPLACEMENT_CONTACT, patients.get(1).getEmergencyContact().toString());
    }

    @Test
    public void copy_contactDescriptor_preservesReplacementAndEquality() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        assertFalse(descriptor.isAnyFieldEdited());
        descriptor.setEmergencyContact(new EmergencyContact(CONTACT));
        assertTrue(descriptor.isAnyFieldEdited());
        EditPersonDescriptor copy = new EditPersonDescriptor(descriptor);
        assertEquals(descriptor, copy);
        descriptor.setEmergencyContact(new EmergencyContact(REPLACEMENT_CONTACT));
        assertFalse(descriptor.equals(copy));
        assertEquals(CONTACT, copy.getEmergencyContact().orElseThrow().toString());
    }

    private void assertStoredPerson(Person expected) throws Exception {
        assertEquals(List.of(expected), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    private void assertRejectedCommand(String command, String expectedMessage) throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        ParseException exception = assertThrows(ParseException.class, () -> logic.execute(command));
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(before, model.getAddressBook());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
    }
}
