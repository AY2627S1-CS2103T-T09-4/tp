package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BLOOD_TYPE;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.BloodType;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class BloodTypeCommandIntegrationTest {

    private static final String ADD_PATIENT = "add n/Amy Bee bt/A+ p/85355255 "
            + "e/amy@gmail.com a/123, Jurong West Ave 6, #08-111 vd/18/9/2026 1000";

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
    public void execute_addEditViewAndNext_preservesBloodTypeInStorage() throws Exception {
        logic.execute(ADD_PATIENT);
        assertStoredPatient(new PersonBuilder().withBloodType("A+").build());

        logic.execute("edit 1 n/Amy Tan bt/O-");
        Person edited = new PersonBuilder().withName("Amy Tan").withBloodType("O-").build();
        assertStoredPatient(edited);
        logic.execute("view 1");
        assertEquals(edited, model.getFilteredPersonList().getFirst());

        logic.execute("edit 1 note/Review medication");
        logic.execute("next 1 1/1/2099 1000");
        assertStoredPatient(new PersonBuilder(edited).withNote("Review medication")
                .withVisitDate("1/1/2099 1000").build());
    }

    @Test
    public void execute_reorderedPrefixesAndWhitespace_acceptsBloodType() throws Exception {
        logic.execute(ADD_PATIENT.replace(" bt/A+", "") + " bt/  a+  ");
        assertStoredPatient(new PersonBuilder().withBloodType("A+").build());
        logic.execute("edit 1 bt/ o- n/Amy Tan");
        assertStoredPatient(new PersonBuilder().withName("Amy Tan").withBloodType("O-").build());
    }

    @Test
    public void execute_omittedBloodType_supportsExistingCommandsAndRecords() throws Exception {
        logic.execute(ADD_PATIENT.replace(" bt/A+", ""));
        assertStoredPatient(new PersonBuilder().build());
        logic.execute("edit 1 bt/A+");
        assertStoredPatient(new PersonBuilder().withBloodType("A+").build());

        // Emulate a saved file from before blood type was introduced.
        String json = Files.readString(storage.getAddressBookFilePath());
        String legacyJson = json.replaceAll("(?m)^\\s*\"bloodType\"[^\\r\\n]*\\R", "");
        assertFalse(legacyJson.contains("bloodType"));
        Files.writeString(storage.getAddressBookFilePath(), legacyJson);
        assertEquals(new PersonBuilder().build(), storage.readAddressBook().orElseThrow().getPersonList().getFirst());
    }

    @Test
    public void execute_emptyBloodType_clearsSavedValueAndPreservesOtherFields() throws Exception {
        logic.execute(ADD_PATIENT);
        Person cleared = new PersonBuilder().build();
        for (String emptyValue : List.of("", "   ", "  \t  ")) {
            logic.execute("edit 1 bt/A+");
            logic.execute("edit 1 bt/" + emptyValue);
            assertStoredPatient(cleared);
            logic.execute("view 1");
            assertTrue(model.getFilteredPersonList().getFirst().getBloodType().isEmpty());
        }
        logic.execute("edit 1 bt/");
        assertStoredPatient(cleared);
        logic.execute("edit 1 bt/AB+");
        assertStoredPatient(new PersonBuilder().withBloodType("AB+").build());
    }

    @Test
    public void execute_clearBloodTypeWithOtherFields_updatesBothFields() throws Exception {
        logic.execute(ADD_PATIENT);
        logic.execute("edit 1 bt/ note/Review medication");
        assertStoredPatient(new PersonBuilder().withNote("Review medication").build());
        logic.execute("edit 1 bt/A+");
        logic.execute("edit 1 note/Keep this note bt/");
        assertStoredPatient(new PersonBuilder().withNote("Keep this note").build());
    }

    @Test
    public void execute_invalidOrRepeatedBloodType_leavesPatientAndFileUnchanged() throws Exception {
        logic.execute(ADD_PATIENT);
        String originalJson = Files.readString(storage.getAddressBookFilePath());
        Person originalPerson = model.getFilteredPersonList().getFirst();
        for (String invalidBloodType : List.of("C+", "A", "A_POSITIVE", "UNKNOWN", "A +")) {
            assertThrows(ParseException.class, BloodType.MESSAGE_CONSTRAINTS, () ->
                    logic.execute("edit 1 bt/" + invalidBloodType));
            assertThrows(ParseException.class,
                    String.format(AddCommand.MESSAGE_NOT_ADDED, BloodType.MESSAGE_CONSTRAINTS), () ->
                    logic.execute(ADD_PATIENT.replace("bt/A+", "bt/" + invalidBloodType)));
        }
        assertThrows(ParseException.class,
                String.format(AddCommand.MESSAGE_NOT_ADDED, BloodType.MESSAGE_CONSTRAINTS), () ->
                logic.execute(ADD_PATIENT.replace("bt/A+", "bt/")));
        String duplicateMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_BLOOD_TYPE);
        for (String repeated : List.of("bt/ bt/", "bt/ bt/A+", "bt/A+ bt/")) {
            assertThrows(ParseException.class, duplicateMessage, () -> logic.execute("edit 1 " + repeated));
        }
        assertThrows(ParseException.class, duplicateMessage, () ->
                logic.execute("edit 1 bt/A+ bt/A+"));
        assertThrows(ParseException.class, String.format(AddCommand.MESSAGE_NOT_ADDED, duplicateMessage), () ->
                logic.execute(ADD_PATIENT + " bt/AB+"));
        assertEquals(List.of(originalPerson), model.getAddressBook().getPersonList());
        assertEquals(originalJson, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void parseBloodType_whitespaceAndEmptyInput_validatesInput() throws Exception {
        assertEquals(BloodType.A_POSITIVE, ParserUtil.parseBloodType(" a+ \t"));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseBloodType(null));
        assertThrows(ParseException.class, BloodType.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseBloodType(" "));
    }

    @Test
    public void execute_allBloodTypes_normalizesAndPersistsValues() throws Exception {
        logic.execute(ADD_PATIENT);
        for (String bloodType : List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")) {
            logic.execute("edit 1 bt/ " + bloodType.toLowerCase(Locale.ROOT) + "  ");
            assertStoredPatient(new PersonBuilder().withBloodType(bloodType).build());
            assertTrue(Files.readString(storage.getAddressBookFilePath()).contains(
                    "\"bloodType\" : \"" + bloodType + "\""));
        }
    }

    @Test
    public void descriptor_bloodTypeOnly_copiesAndComparesField() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        assertFalse(descriptor.isAnyFieldEdited());
        descriptor.setBloodType(BloodType.A_POSITIVE);
        assertTrue(descriptor.isAnyFieldEdited());
        EditPersonDescriptor copy = new EditPersonDescriptor(descriptor);
        assertEquals(descriptor, copy);
        descriptor.setBloodType(BloodType.AB_POSITIVE);
        assertFalse(descriptor.equals(copy));
        assertEquals(BloodType.A_POSITIVE, copy.getBloodType().orElseThrow());
    }

    private void assertStoredPatient(Person expected) throws Exception {
        assertEquals(List.of(expected), model.getAddressBook().getPersonList());
        assertEquals(List.of(expected), storage.readAddressBook().orElseThrow().getPersonList());
    }
}
