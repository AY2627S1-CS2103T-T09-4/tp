package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALCOHOL_HISTORY;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
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
import seedu.address.model.person.AlcoholHistory;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AlcoholHistoryCommandIntegrationTest {

    private static final String ADD_PATIENT = "add n/Amy Bee dob/15/6/1990 bt/A+ "
            + "p/85355255 ec/Mary Tan, 91234567 e/amy@gmail.com a/123, Jurong West Ave 6, #08-111 "
            + "vd/1/1/2000 1000 note/Review medication t/family";

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
    public void execute_addEditViewAndNext_preservesCompleteRecord() throws Exception {
        logic.execute(ADD_PATIENT + " alc/  Occasional drinking  ");
        Person original = expectedPatient().withAlcoholHistory("Occasional drinking").build();
        assertStoredPatient(original);
        assertEquals(original, logic.execute("view 1").getPersonToView().orElseThrow());

        logic.execute("edit 1 alc/ Does not drink n/Amy Tan");
        Person edited = new PersonBuilder(original).withName("Amy Tan").withAlcoholHistory("Does not drink").build();
        assertStoredPatient(edited);
        logic.execute("edit 1 note/Review next visit");
        logic.execute("next 1 1/1/2099 1000");
        Person scheduled = new PersonBuilder(edited).withNote("Review next visit")
                .withPreviousVisitDates("1/1/2000 1000").withVisitDate("1/1/2099 1000").build();
        assertStoredPatient(scheduled);
        assertEquals(scheduled, logic.execute("view 1").getPersonToView().orElseThrow());

        logic.execute("edit 1 alc/");
        assertStoredPatient(new PersonBuilder(scheduled).withAlcoholHistory("").build());
    }

    @Test
    public void execute_blankEdit_clearsHistoryAndPreservesOtherFields() throws Exception {
        logic.execute(ADD_PATIENT + " alc/Occasional drinking");
        for (String blank : List.of("", "   ", " \t ", "\u2003")) {
            logic.execute("edit 1 alc/Does not drink");
            logic.execute("edit 1 alc/" + blank);
            assertStoredPatient(expectedPatient().build());
        }
        logic.execute("edit 1 alc/");
        assertStoredPatient(expectedPatient().build());
        logic.execute("edit 1 alc/Occasional drinking");
        assertStoredPatient(expectedPatient().withAlcoholHistory("Occasional drinking").build());
    }

    @Test
    public void execute_omittedOrLegacyHistory_loadsAsUnrecorded() throws Exception {
        logic.execute(ADD_PATIENT);
        assertStoredPatient(expectedPatient().build());
        logic.execute("edit 1 alc/Does not drink");
        String json = Files.readString(storage.getAddressBookFilePath());
        String legacyJson = json.replaceAll("(?m)^\\s*\"alcoholHistory\"[^\\r\\n]*\\R", "");
        assertFalse(legacyJson.contains("alcoholHistory"));
        Files.writeString(storage.getAddressBookFilePath(), legacyJson);
        assertEquals(expectedPatient().build(), storage.readAddressBook().orElseThrow().getPersonList().getFirst());

        String nullHistoryJson = json.replace("\"alcoholHistory\" : \"Does not drink\"", "\"alcoholHistory\" : null");
        assertTrue(nullHistoryJson.contains("\"alcoholHistory\" : null"));
        Files.writeString(storage.getAddressBookFilePath(), nullHistoryJson);
        assertEquals(expectedPatient().build(), storage.readAddressBook().orElseThrow().getPersonList().getFirst());
    }

    @Test
    public void execute_blankAddAndRepeatedPrefixes_leavesPatientAndFileUnchanged() throws Exception {
        logic.execute(ADD_PATIENT + " alc/Does not drink");
        String originalJson = Files.readString(storage.getAddressBookFilePath());
        for (String blank : List.of("", "   ", " \t ", "\u2003")) {
            assertThrows(ParseException.class,
                    String.format(AddCommand.MESSAGE_NOT_ADDED, AlcoholHistory.MESSAGE_CONSTRAINTS), () ->
                    logic.execute(ADD_PATIENT + " alc/" + blank));
        }
        String duplicateMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ALCOHOL_HISTORY);
        for (String repeated : List.of("alc/ alc/", "alc/Does not drink alc/", "alc/ alc/Occasional drinking",
                "alc/Does not drink alc/Does not drink")) {
            assertThrows(ParseException.class, duplicateMessage, () -> logic.execute("edit 1 " + repeated));
            assertThrows(ParseException.class, String.format(AddCommand.MESSAGE_NOT_ADDED, duplicateMessage), () ->
                    logic.execute(ADD_PATIENT + " " + repeated));
        }
        assertStoredPatient(expectedPatient().withAlcoholHistory("Does not drink").build());
        assertEquals(originalJson, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_freeTextAndReorderedPrefixes_preservesText() throws Exception {
        logic.execute("add alc/Occasional drinking;  1–2 glasses " + ADD_PATIENT.substring("add ".length()));
        assertStoredPatient(expectedPatient().withAlcoholHistory("Occasional drinking;  1–2 glasses").build());
        logic.execute("edit 1 bt/O- alc/Does not drink ec/98765432");
        assertStoredPatient(expectedPatient().withBloodType("O-").withAlcoholHistory("Does not drink")
                .withEmergencyContact("98765432").build());
    }

    @Test
    public void execute_confirmationOnlyAppliesHistoryAfterApproval() throws Exception {
        logic.execute(ADD_PATIENT + " alc/Does not drink");
        Person original = expectedPatient().withAlcoholHistory("Does not drink").build();
        logic.execute("add n/Other Patient p/87654321 a/Test Road vd/2/1/2000 1000");
        logic.execute("edit 1 n/Other Patient alc/Occasional drinking");
        assertTrue(logic.isInputRequestPending());
        assertEquals(original, model.getAddressBook().getPersonList().getFirst());
        logic.execute("n");
        assertEquals(original, storage.readAddressBook().orElseThrow().getPersonList().getFirst());
        logic.execute("edit 1 n/Other Patient alc/Occasional drinking");
        logic.execute("y");
        Person edited = new PersonBuilder(original).withName("Other Patient")
                .withAlcoholHistory("Occasional drinking").build();
        assertEquals(edited, storage.readAddressBook().orElseThrow().getPersonList().getFirst());
    }

    @Test
    public void parseAlcoholHistory_blankAndNullInput_validatesRecordedHistory() throws Exception {
        assertEquals(new AlcoholHistory("Occasional drinking"),
                ParserUtil.parseAlcoholHistory(" Occasional drinking "));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAlcoholHistory(null));
        assertThrows(ParseException.class, AlcoholHistory.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseAlcoholHistory(" \t\u2003"));
    }

    @Test
    public void descriptor_alcoholHistoryOnly_copiesAndSerializesField() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        assertFalse(descriptor.isAnyFieldEdited());
        descriptor.setAlcoholHistory(new AlcoholHistory("Does not drink"));
        assertTrue(descriptor.isAnyFieldEdited());
        EditPersonDescriptor copy = new EditPersonDescriptor(descriptor);
        assertEquals(descriptor, copy);
        assertEquals("alc/Does not drink ", PersonUtil.getEditPersonDescriptorDetails(copy));
        descriptor.setAlcoholHistory(new AlcoholHistory(""));
        assertFalse(descriptor.equals(copy));
        assertEquals(new AlcoholHistory("Does not drink"), copy.getAlcoholHistory().orElseThrow());
    }

    @Test
    public void help_alcoholHistory_followsBloodTypeInAddAndEdit() {
        for (String usage : List.of(AddCommand.MESSAGE_USAGE, EditCommand.MESSAGE_USAGE)) {
            assertTrue(usage.contains("[bt/BLOOD_TYPE] [alc/ALCOHOL_HISTORY]"));
        }
    }

    private PersonBuilder expectedPatient() {
        return new PersonBuilder().withDateOfBirth("15/6/1990").withBloodType("A+")
                .withVisitDate("1/1/2000 1000").withNote("Review medication")
                .withEmergencyContact("Mary Tan, 91234567").withTags("family");
    }

    private void assertStoredPatient(Person expected) throws Exception {
        assertEquals(List.of(expected), model.getAddressBook().getPersonList());
        assertEquals(List.of(expected), storage.readAddressBook().orElseThrow().getPersonList());
    }
}
