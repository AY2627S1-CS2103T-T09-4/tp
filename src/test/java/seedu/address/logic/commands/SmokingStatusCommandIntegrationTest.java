package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SMOKING_STATUS;
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
import seedu.address.model.person.Person;
import seedu.address.model.person.SmokingStatus;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.LogicManagerIntegrationFixture;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class SmokingStatusCommandIntegrationTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-21T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));

    private static final String ADD_PATIENT = "add n/Amy Bee dob/15/6/1990 bt/A+ alc/Occasional drinking "
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
        logic.execute(ADD_PATIENT + " smk/  FoRmEr  ");
        Person original = expectedPatient().withSmokingStatus("former").build();
        fixture.assertPersistedPatients(original);
        assertEquals(original, logic.execute("view 1").getPersonToView().orElseThrow());

        logic.execute("edit 1 smk/current n/Amy Tan");
        Person edited = new PersonBuilder(original).withName("Amy Tan").withSmokingStatus("current").build();
        fixture.assertPersistedPatients(edited);
        logic.execute("edit 1 alc/Does not drink");
        logic.execute("next 1 1/1/2099 1000");
        Person scheduled = new PersonBuilder(edited).withAlcoholHistory("Does not drink")
                .withPreviousVisitDates("1/1/2000 1000").withVisitDate("1/1/2099 1000").build();
        fixture.assertPersistedPatients(scheduled);
        assertEquals(scheduled, logic.execute("view 1").getPersonToView().orElseThrow());
        logic.execute("edit 1 smk/");
        fixture.assertPersistedPatients(new PersonBuilder(scheduled).withSmokingStatus("").build());
    }

    @Test
    public void execute_blankEdit_clearsStatusWhileUnknownRemainsRecorded() throws Exception {
        logic.execute(ADD_PATIENT + " smk/unknown");
        fixture.assertPersistedPatients(expectedPatient().withSmokingStatus("unknown").build());
        for (String blank : List.of("", "   ", " \t ", "\u2003")) {
            logic.execute("edit 1 smk/unknown");
            logic.execute("edit 1 smk/" + blank);
            fixture.assertPersistedPatients(expectedPatient().build());
        }
        logic.execute("edit 1 smk/");
        fixture.assertPersistedPatients(expectedPatient().build());
        logic.execute("edit 1 smk/never");
        fixture.assertPersistedPatients(expectedPatient().withSmokingStatus("never").build());
    }

    @Test
    public void execute_omittedOrLegacyStatus_loadsAsUnrecorded() throws Exception {
        logic.execute(ADD_PATIENT);
        fixture.assertPersistedPatients(expectedPatient().build());
        logic.execute("edit 1 smk/unknown");
        String json = Files.readString(storage.getAddressBookFilePath());
        String legacyJson = json.replaceAll("(?m)^\\s*\"smokingStatus\"[^\\r\\n]*\\R", "");
        assertFalse(legacyJson.contains("smokingStatus"));
        Files.writeString(storage.getAddressBookFilePath(), legacyJson);
        assertEquals(expectedPatient().build(), storage.readAddressBook().orElseThrow().getPersonList().getFirst());

        String nullStatusJson = json.replace("\"smokingStatus\" : \"unknown\"", "\"smokingStatus\" : null");
        assertTrue(nullStatusJson.contains("\"smokingStatus\" : null"));
        Files.writeString(storage.getAddressBookFilePath(), nullStatusJson);
        assertEquals(expectedPatient().build(), storage.readAddressBook().orElseThrow().getPersonList().getFirst());
    }

    @Test
    public void execute_unsupportedOrRepeatedStatus_leavesPatientAndFileUnchanged() throws Exception {
        logic.execute(ADD_PATIENT + " smk/former");
        for (String invalid : List.of("daily", "yes", "no", "never smoker", "unrecorded")) {
            fixture.assertRejectedCommand("edit 1 smk/" + invalid, ParseException.class,
                    SmokingStatus.MESSAGE_CONSTRAINTS);
            fixture.assertRejectedCommand(ADD_PATIENT + " smk/" + invalid, ParseException.class,
                    String.format(AddCommand.MESSAGE_NOT_ADDED, SmokingStatus.MESSAGE_CONSTRAINTS));
        }
        for (String blank : List.of("", "   ", " \t ", "\u2003")) {
            fixture.assertRejectedCommand(ADD_PATIENT + " smk/" + blank, ParseException.class,
                    String.format(AddCommand.MESSAGE_NOT_ADDED, SmokingStatus.MESSAGE_CONSTRAINTS));
        }
        String duplicateMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SMOKING_STATUS);
        for (String repeated : List.of("smk/ smk/", "smk/former smk/", "smk/ smk/current", "smk/never smk/never")) {
            fixture.assertRejectedCommand("edit 1 " + repeated, ParseException.class, duplicateMessage);
            fixture.assertRejectedCommand(ADD_PATIENT + " " + repeated, ParseException.class,
                    String.format(AddCommand.MESSAGE_NOT_ADDED, duplicateMessage));
        }
        fixture.assertPersistedPatients(expectedPatient().withSmokingStatus("former").build());
    }

    @Test
    public void execute_allStatusesAndReorderedPrefixes_normalizesAndPersistsStatus() throws Exception {
        logic.execute("add smk/CURRENT " + ADD_PATIENT.substring("add ".length()));
        fixture.assertPersistedPatients(expectedPatient().withSmokingStatus("current").build());
        for (String status : List.of(" NEVER ", "FoRmEr", " CURRENT\t", "UNKNOWN")) {
            logic.execute("edit 1 alc/Does not drink smk/" + status + " bt/O-");
            fixture.assertPersistedPatients(expectedPatient().withAlcoholHistory("Does not drink").withBloodType("O-")
                    .withSmokingStatus(status).build());
        }
    }

    @Test
    public void execute_confirmationOnlyAppliesStatusAfterApproval() throws Exception {
        logic.execute(ADD_PATIENT + " smk/never");
        Person original = expectedPatient().withSmokingStatus("never").build();
        logic.execute("add n/Other Patient p/87654321 a/Test Road vd/2/1/2000 1000");
        logic.execute("edit 1 n/Other Patient smk/former");
        assertTrue(logic.isInputRequestPending());
        assertEquals(original, model.getAddressBook().getPersonList().getFirst());
        logic.execute("n");
        assertEquals(original, storage.readAddressBook().orElseThrow().getPersonList().getFirst());
        logic.execute("edit 1 n/Other Patient smk/former");
        logic.execute("y");
        Person edited = new PersonBuilder(original).withName("Other Patient").withSmokingStatus("former").build();
        assertEquals(edited, storage.readAddressBook().orElseThrow().getPersonList().getFirst());
    }

    @Test
    public void parseSmokingStatus_blankAndNullInput_validatesRecordedStatus() throws Exception {
        assertEquals(SmokingStatus.FORMER, ParserUtil.parseSmokingStatus(" FoRmEr "));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseSmokingStatus(null));
        assertThrows(ParseException.class, SmokingStatus.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseSmokingStatus(" \t\u2003"));
    }

    @Test
    public void descriptor_smokingStatusOnly_copiesAndSerializesField() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        assertFalse(descriptor.isAnyFieldEdited());
        descriptor.setSmokingStatus(SmokingStatus.UNKNOWN);
        assertTrue(descriptor.isAnyFieldEdited());
        EditPersonDescriptor copy = new EditPersonDescriptor(descriptor);
        assertEquals(descriptor, copy);
        assertEquals("smk/unknown ", PersonUtil.getEditPersonDescriptorDetails(copy));
        descriptor.setSmokingStatus(SmokingStatus.UNRECORDED);
        assertFalse(descriptor.equals(copy));
        assertEquals(SmokingStatus.UNKNOWN, copy.getSmokingStatus().orElseThrow());
    }

    @Test
    public void help_smokingStatus_followsAlcoholHistoryInAddAndEdit() {
        for (String usage : List.of(AddCommand.MESSAGE_USAGE, EditCommand.MESSAGE_USAGE)) {
            assertTrue(usage.contains("[alc/ALCOHOL_HISTORY] [smk/STATUS]"));
        }
    }

    private PersonBuilder expectedPatient() {
        return new PersonBuilder().withDateOfBirth("15/6/1990").withBloodType("A+")
                .withAlcoholHistory("Occasional drinking").withVisitDate("1/1/2000 1000")
                .withNote("Review medication").withEmergencyContact("Mary Tan, 91234567").withTags("family");
    }
}
