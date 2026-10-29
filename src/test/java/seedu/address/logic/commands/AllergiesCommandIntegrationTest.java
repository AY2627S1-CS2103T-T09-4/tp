package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALLERGIES;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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
import seedu.address.model.person.Allergies;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.LogicManagerIntegrationFixture;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AllergiesCommandIntegrationTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-21T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));

    private static final String ADD_PATIENT = "add n/Amy Bee dob/15/6/1990 bt/A+ alc/Occasional drinking smk/former "
            + "p/85355255 ec/Mary Tan, 91234567 e/amy@gmail.com a/123, Jurong West Ave 6, #08-111 "
            + "vd/1/1/2000 1000 note/Review medication t/family";

    @TempDir
    private Path testFolder;

    private Model model;
    private LogicManager logic;
    private LogicManagerIntegrationFixture fixture;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws IOException {
        model = new ModelManager();
        fixture = new LogicManagerIntegrationFixture(model, testFolder, FIXED_CLOCK);
        logic = fixture.getLogic();
        storage = fixture.getAddressBookStorage();
    }

    @Test
    public void execute_addEditViewAndNext_preservesCompleteRecord() throws Exception {
        logic.execute(ADD_PATIENT + " all/  Penicillin; latex  ");
        Person original = expectedPatient().withAllergies("Penicillin; latex").build();
        fixture.assertPersistedPatients(original);
        assertEquals(original, logic.execute("view 1").getPersonToView().orElseThrow());

        logic.execute("edit 1 all/ No known allergies n/Amy Tan");
        Person edited = new PersonBuilder(original).withName("Amy Tan").withAllergies("No known allergies").build();
        fixture.assertPersistedPatients(edited);
        logic.execute("edit 1 note/Review next visit alc/Does not drink smk/current");
        logic.execute("next 1 1/1/2099 1000");
        Person scheduled = new PersonBuilder(edited).withNote("Review next visit")
                .withAlcoholHistory("Does not drink").withSmokingStatus("current")
                .withPreviousVisitDates("1/1/2000 1000").withVisitDate("1/1/2099 1000").build();
        fixture.assertPersistedPatients(scheduled);
        assertEquals(scheduled, logic.execute("view 1").getPersonToView().orElseThrow());

        logic.execute("edit 1 all/");
        fixture.assertPersistedPatients(new PersonBuilder(scheduled).withAllergies("").build());
    }

    @Test
    public void execute_blankEdit_clearsAllergiesAndPreservesOtherFields() throws Exception {
        logic.execute(ADD_PATIENT + " all/Penicillin; latex");
        for (String blank : List.of("", "   ", " \t ", "\u2003")) {
            logic.execute("edit 1 all/No known allergies");
            logic.execute("edit 1 all/" + blank);
            fixture.assertPersistedPatients(expectedPatient().build());
        }
        logic.execute("edit 1 all/");
        fixture.assertPersistedPatients(expectedPatient().build());
        logic.execute("edit 1 all/Penicillin; latex");
        fixture.assertPersistedPatients(expectedPatient().withAllergies("Penicillin; latex").build());
    }

    @Test
    public void execute_omittedOrLegacyAllergies_loadsAsUnrecorded() throws Exception {
        logic.execute(ADD_PATIENT);
        fixture.assertPersistedPatients(expectedPatient().build());
        logic.execute("edit 1 all/No known allergies");
        String json = Files.readString(storage.getAddressBookFilePath());
        String legacyJson = json.replaceAll("(?m)^\\s*\"allergies\"[^\\r\\n]*\\R", "");
        assertFalse(legacyJson.contains("allergies"));
        Files.writeString(storage.getAddressBookFilePath(), legacyJson);
        assertEquals(expectedPatient().build(), storage.readAddressBook().orElseThrow().getPersonList().getFirst());

        String nullAllergiesJson = json.replace("\"allergies\" : \"No known allergies\"", "\"allergies\" : null");
        assertTrue(nullAllergiesJson.contains("\"allergies\" : null"));
        Files.writeString(storage.getAddressBookFilePath(), nullAllergiesJson);
        assertEquals(expectedPatient().build(), storage.readAddressBook().orElseThrow().getPersonList().getFirst());
    }

    @Test
    public void execute_blankAddAndRepeatedPrefixes_leavesPatientAndFileUnchanged() throws Exception {
        logic.execute(ADD_PATIENT + " all/No known allergies");
        for (String blank : List.of("", "   ", " \t ", "\u2003")) {
            fixture.assertRejectedCommand(ADD_PATIENT + " all/" + blank, ParseException.class,
                    String.format(AddCommand.MESSAGE_NOT_ADDED, Allergies.MESSAGE_CONSTRAINTS));
        }
        String duplicateMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ALLERGIES);
        for (String repeated : List.of("all/ all/", "all/No known allergies all/", "all/ all/Penicillin; latex",
                "all/No known allergies all/No known allergies")) {
            fixture.assertRejectedCommand("edit 1 " + repeated, ParseException.class, duplicateMessage);
            fixture.assertRejectedCommand(ADD_PATIENT + " " + repeated, ParseException.class,
                    String.format(AddCommand.MESSAGE_NOT_ADDED, duplicateMessage));
        }
        fixture.assertPersistedPatients(expectedPatient().withAllergies("No known allergies").build());
    }

    @Test
    public void execute_freeTextAndReorderedPrefixes_preservesText() throws Exception {
        logic.execute("add all/Penicillin;  latex (rash) " + ADD_PATIENT.substring("add ".length()));
        fixture.assertPersistedPatients(expectedPatient().withAllergies("Penicillin;  latex (rash)").build());
        logic.execute("edit 1 bt/O- all/No known allergies ec/98765432");
        fixture.assertPersistedPatients(expectedPatient().withBloodType("O-").withAllergies("No known allergies")
                .withEmergencyContact("98765432").build());
    }

    @Test
    public void execute_confirmationOnlyAppliesAllergiesAfterApproval() throws Exception {
        logic.execute(ADD_PATIENT + " all/No known allergies");
        Person original = expectedPatient().withAllergies("No known allergies").build();
        logic.execute("add n/Other Patient p/87654321 a/Test Road vd/2/1/2000 1000");
        logic.execute("edit 1 n/Other Patient all/Penicillin; latex");
        assertTrue(logic.isInputRequestPending());
        assertEquals(original, model.getAddressBook().getPersonList().getFirst());
        logic.execute("n");
        assertEquals(original, storage.readAddressBook().orElseThrow().getPersonList().getFirst());
        logic.execute("edit 1 n/Other Patient all/Penicillin; latex");
        logic.execute("y");
        Person edited = new PersonBuilder(original).withName("Other Patient")
                .withAllergies("Penicillin; latex").build();
        assertEquals(edited, storage.readAddressBook().orElseThrow().getPersonList().getFirst());
    }

    @Test
    public void parseAllergies_blankAndNullInput_validatesRecordedAllergies() throws Exception {
        assertEquals(new Allergies("Penicillin; latex"),
                ParserUtil.parseAllergies(" Penicillin; latex "));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAllergies(null));
        assertThrows(ParseException.class, Allergies.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseAllergies(" \t\u2003"));
    }

    @Test
    public void descriptor_allergiesOnly_copiesAndSerializesField() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        assertFalse(descriptor.isAnyFieldEdited());
        descriptor.setAllergies(new Allergies("No known allergies"));
        assertTrue(descriptor.isAnyFieldEdited());
        EditPersonDescriptor copy = new EditPersonDescriptor(descriptor);
        assertEquals(descriptor, copy);
        assertEquals("all/No known allergies ", PersonUtil.getEditPersonDescriptorDetails(copy));
        descriptor.setAllergies(new Allergies(""));
        assertFalse(descriptor.equals(copy));
        assertEquals(new Allergies("No known allergies"), copy.getAllergies().orElseThrow());
    }

    @Test
    public void help_allergies_precedesAlcoholAndSmokingInAddAndEdit() {
        for (String usage : List.of(AddCommand.MESSAGE_USAGE, EditCommand.MESSAGE_USAGE)) {
            assertTrue(usage.contains("[bt/BLOOD_TYPE] [all/ALLERGIES] [alc/ALCOHOL_HISTORY] [smk/STATUS]"));
        }
    }

    private PersonBuilder expectedPatient() {
        return new PersonBuilder().withDateOfBirth("15/6/1990").withBloodType("A+")
                .withAlcoholHistory("Occasional drinking").withSmokingStatus("former")
                .withVisitDate("1/1/2000 1000").withNote("Review medication")
                .withEmergencyContact("Mary Tan, 91234567").withTags("family");
    }
}
