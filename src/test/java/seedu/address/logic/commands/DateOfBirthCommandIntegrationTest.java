package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE_OF_BIRTH;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

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
import seedu.address.model.person.DateOfBirth;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class DateOfBirthCommandIntegrationTest {

    private static final String ADD_PATIENT = "add n/Amy Bee dob/15/6/1990 p/85355255 "
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
    public void execute_addEditViewAndNext_preservesDateOfBirthInStorage() throws Exception {
        logic.execute(ADD_PATIENT);
        assertStoredPatient(new PersonBuilder().withDateOfBirth("15/6/1990").build());

        logic.execute("edit 1 n/Amy Tan dob/20/7/1985");
        Person edited = new PersonBuilder().withName("Amy Tan").withDateOfBirth("20/7/1985").build();
        assertStoredPatient(edited);
        logic.execute("view 1");
        assertEquals(edited, model.getFilteredPersonList().getFirst());

        logic.execute("edit 1 note/Review medication");
        logic.execute("next 1 1/1/2099 1000");
        assertStoredPatient(new PersonBuilder(edited).withNote("Review medication")
                .withVisitDate("1/1/2099 1000").build());
    }

    @Test
    public void execute_reorderedPrefixesAndWhitespace_acceptsDateOfBirth() throws Exception {
        logic.execute(ADD_PATIENT.replace(" dob/15/6/1990", "") + " dob/  15/06/1990  ");
        assertStoredPatient(new PersonBuilder().withDateOfBirth("15/6/1990").build());
        logic.execute("edit 1 dob/ 20/07/1985 n/Amy Tan");
        assertStoredPatient(new PersonBuilder().withName("Amy Tan").withDateOfBirth("20/7/1985").build());
    }

    @Test
    public void execute_omittedDateOfBirth_supportsExistingCommandsAndRecords() throws Exception {
        logic.execute(ADD_PATIENT.replace(" dob/15/6/1990", ""));
        assertStoredPatient(new PersonBuilder().build());
        logic.execute("edit 1 dob/15/6/1990");
        assertStoredPatient(new PersonBuilder().withDateOfBirth("15/6/1990").build());

        // Emulate a saved file from before date of birth was introduced.
        String json = Files.readString(storage.getAddressBookFilePath());
        String legacyJson = json.replaceAll("(?m)^\\s*\"dateOfBirth\"[^\\r\\n]*\\R", "");
        assertFalse(legacyJson.contains("dateOfBirth"));
        Files.writeString(storage.getAddressBookFilePath(), legacyJson);
        assertEquals(new PersonBuilder().build(), storage.readAddressBook().orElseThrow().getPersonList().getFirst());
    }

    @Test
    public void execute_emptyDateOfBirth_clearsSavedDateAndPreservesOtherFields() throws Exception {
        logic.execute(ADD_PATIENT);
        Person cleared = new PersonBuilder().build();
        for (String emptyValue : List.of("", "   ", "  \t  ")) {
            logic.execute("edit 1 dob/15/6/1990");
            logic.execute("edit 1 dob/" + emptyValue);
            assertStoredPatient(cleared);
            logic.execute("view 1");
            assertTrue(model.getFilteredPersonList().getFirst().getDateOfBirth().isEmpty());
        }
        logic.execute("edit 1 dob/");
        assertStoredPatient(cleared);
        logic.execute("edit 1 dob/1/1/2000");
        assertStoredPatient(new PersonBuilder().withDateOfBirth("1/1/2000").build());
    }

    @Test
    public void execute_clearDateOfBirthWithOtherFields_updatesBothFields() throws Exception {
        logic.execute(ADD_PATIENT);
        logic.execute("edit 1 dob/ note/Review medication");
        assertStoredPatient(new PersonBuilder().withNote("Review medication").build());
        logic.execute("edit 1 dob/15/6/1990");
        logic.execute("edit 1 note/Keep this note dob/");
        assertStoredPatient(new PersonBuilder().withNote("Keep this note").build());
    }

    @Test
    public void execute_invalidOrRepeatedDateOfBirth_leavesPatientAndFileUnchanged() throws Exception {
        logic.execute(ADD_PATIENT);
        String originalJson = Files.readString(storage.getAddressBookFilePath());
        Person originalPerson = model.getFilteredPersonList().getFirst();
        for (String date : List.of("31/2/2000", "1/1/9999", "1990-06-15")) {
            assertThrows(ParseException.class, DateOfBirth.MESSAGE_CONSTRAINTS, () ->
                    logic.execute("edit 1 dob/" + date));
            assertThrows(ParseException.class,
                    String.format(AddCommand.MESSAGE_NOT_ADDED, DateOfBirth.MESSAGE_CONSTRAINTS), () ->
                    logic.execute(ADD_PATIENT.replace("dob/15/6/1990", "dob/" + date)));
        }
        assertThrows(ParseException.class,
                String.format(AddCommand.MESSAGE_NOT_ADDED, DateOfBirth.MESSAGE_CONSTRAINTS), () ->
                logic.execute(ADD_PATIENT.replace("dob/15/6/1990", "dob/")));
        String duplicateMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DATE_OF_BIRTH);
        for (String repeated : List.of("dob/ dob/", "dob/ dob/15/6/1990", "dob/15/6/1990 dob/")) {
            assertThrows(ParseException.class, duplicateMessage, () -> logic.execute("edit 1 " + repeated));
        }
        assertThrows(ParseException.class, duplicateMessage, () ->
                logic.execute("edit 1 dob/15/6/1990 dob/15/6/1990"));
        assertThrows(ParseException.class, String.format(AddCommand.MESSAGE_NOT_ADDED, duplicateMessage), () ->
                logic.execute(ADD_PATIENT + " dob/1/1/2000"));
        assertEquals(List.of(originalPerson), model.getAddressBook().getPersonList());
        assertEquals(originalJson, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void parseDateOfBirth_whitespaceAndFutureDate_validatesInput() throws Exception {
        assertEquals(new DateOfBirth("15/6/1990"), ParserUtil.parseDateOfBirth(" 15/06/1990 \t"));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseDateOfBirth(null));
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String futureDate = tomorrow.getDayOfMonth() + "/" + tomorrow.getMonthValue() + "/" + tomorrow.getYear();
        assertThrows(ParseException.class, DateOfBirth.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseDateOfBirth(futureDate));
    }

    @Test
    public void descriptor_dateOfBirthOnly_copiesAndComparesField() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        assertFalse(descriptor.isAnyFieldEdited());
        descriptor.setDateOfBirth(new DateOfBirth("15/6/1990"));
        assertTrue(descriptor.isAnyFieldEdited());
        EditPersonDescriptor copy = new EditPersonDescriptor(descriptor);
        assertEquals(descriptor, copy);
        descriptor.setDateOfBirth(new DateOfBirth("1/1/2000"));
        assertFalse(descriptor.equals(copy));
        assertEquals(new DateOfBirth("15/6/1990"), copy.getDateOfBirth().orElseThrow());
    }

    private void assertStoredPatient(Person expected) throws Exception {
        assertEquals(List.of(expected), model.getAddressBook().getPersonList());
        assertEquals(List.of(expected), storage.readAddressBook().orElseThrow().getPersonList());
    }
}
