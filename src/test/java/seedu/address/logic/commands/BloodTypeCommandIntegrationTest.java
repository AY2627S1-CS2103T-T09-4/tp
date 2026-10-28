package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BLOOD_TYPE;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
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
import seedu.address.testutil.LogicManagerIntegrationFixture;
import seedu.address.testutil.PersonBuilder;

public class BloodTypeCommandIntegrationTest {

    private static final String ADD_PATIENT = "add n/Amy Bee bt/A+ p/85355255 "
            + "e/amy@gmail.com a/123, Jurong West Ave 6, #08-111 vd/18/9/2026 1000";

    @TempDir
    private Path testFolder;

    private Model model;
    private LogicManager logic;
    private LogicManagerIntegrationFixture fixture;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws IOException {
        model = new ModelManager();
        fixture = new LogicManagerIntegrationFixture(model, testFolder, Clock.systemDefaultZone());
        logic = fixture.getLogic();
        storage = fixture.getAddressBookStorage();
    }

    @Test
    public void execute_addEditViewAndNext_preservesBloodTypeInStorage() throws Exception {
        logic.execute(ADD_PATIENT);
        fixture.assertPersistedPatients(new PersonBuilder().withBloodType("A+").build());

        logic.execute("edit 1 n/Amy Tan bt/O-");
        Person edited = new PersonBuilder().withName("Amy Tan").withBloodType("O-").build();
        fixture.assertPersistedPatients(edited);
        logic.execute("view 1");
        assertEquals(edited, model.getFilteredPersonList().getFirst());

        logic.execute("edit 1 note/Review medication");
        logic.execute("next 1 1/1/2099 1000");
        fixture.assertPersistedPatients(new PersonBuilder(edited).withNote("Review medication")
                .withVisitDate("1/1/2099 1000").withPreviousVisitDates(edited.getVisitDate().toString()).build());
    }

    @Test
    public void execute_bloodTypeContactAndHistory_preservesCombinedRecord() throws Exception {
        logic.execute(ADD_PATIENT + " ec/91234567");
        Person original = new PersonBuilder().withBloodType("A+").withEmergencyContact("91234567").build();
        fixture.assertPersistedPatients(original);

        logic.execute("next 1 1/1/2099 1000");
        Person scheduled = new PersonBuilder(original).withVisitDate("1/1/2099 1000")
                .withPreviousVisitDates(original.getVisitDate().toString()).build();
        fixture.assertPersistedPatients(scheduled);

        logic.execute("edit 1 bt/O- ec/Mary Tan, 98765432");
        Person edited = new PersonBuilder(scheduled).withBloodType("O-")
                .withEmergencyContact("Mary Tan, 98765432").build();
        fixture.assertPersistedPatients(edited);
        assertEquals(edited, logic.execute("view 1").getPersonToView().orElseThrow());

        logic.execute("edit 1 bt/");
        Person withoutBloodType = new PersonBuilder(edited).withBloodType("").build();
        fixture.assertPersistedPatients(withoutBloodType);
        logic.execute("edit 1 ec/");
        fixture.assertPersistedPatients(new PersonBuilder(withoutBloodType).withEmergencyContact("").build());
    }

    @Test
    public void execute_reorderedPrefixesAndWhitespace_acceptsBloodType() throws Exception {
        logic.execute(ADD_PATIENT.replace(" bt/A+", "") + " bt/  a+  ");
        fixture.assertPersistedPatients(new PersonBuilder().withBloodType("A+").build());
        logic.execute("edit 1 bt/ o- n/Amy Tan");
        fixture.assertPersistedPatients(new PersonBuilder().withName("Amy Tan").withBloodType("O-").build());
    }

    @Test
    public void execute_omittedBloodType_supportsExistingCommandsAndRecords() throws Exception {
        logic.execute(ADD_PATIENT.replace(" bt/A+", ""));
        fixture.assertPersistedPatients(new PersonBuilder().build());
        logic.execute("edit 1 bt/A+");
        fixture.assertPersistedPatients(new PersonBuilder().withBloodType("A+").build());

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
            fixture.assertPersistedPatients(cleared);
            logic.execute("view 1");
            assertTrue(model.getFilteredPersonList().getFirst().getBloodType().isEmpty());
        }
        logic.execute("edit 1 bt/");
        fixture.assertPersistedPatients(cleared);
        logic.execute("edit 1 bt/AB+");
        fixture.assertPersistedPatients(new PersonBuilder().withBloodType("AB+").build());
    }

    @Test
    public void execute_clearBloodTypeWithOtherFields_updatesBothFields() throws Exception {
        logic.execute(ADD_PATIENT);
        logic.execute("edit 1 bt/ note/Review medication");
        fixture.assertPersistedPatients(new PersonBuilder().withNote("Review medication").build());
        logic.execute("edit 1 bt/A+");
        logic.execute("edit 1 note/Keep this note bt/");
        fixture.assertPersistedPatients(new PersonBuilder().withNote("Keep this note").build());
    }

    @Test
    public void execute_invalidOrRepeatedBloodType_leavesPatientAndFileUnchanged() throws Exception {
        logic.execute(ADD_PATIENT);
        Person originalPerson = model.getFilteredPersonList().getFirst();
        for (String invalidBloodType : List.of("C+", "A", "A_POSITIVE", "UNKNOWN", "A +")) {
            fixture.assertRejectedCommand("edit 1 bt/" + invalidBloodType, ParseException.class,
                    BloodType.MESSAGE_CONSTRAINTS);
            fixture.assertRejectedCommand(ADD_PATIENT.replace("bt/A+", "bt/" + invalidBloodType), ParseException.class,
                    String.format(AddCommand.MESSAGE_NOT_ADDED, BloodType.MESSAGE_CONSTRAINTS));
        }
        fixture.assertRejectedCommand(ADD_PATIENT.replace("bt/A+", "bt/"), ParseException.class,
                String.format(AddCommand.MESSAGE_NOT_ADDED, BloodType.MESSAGE_CONSTRAINTS));
        String duplicateMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_BLOOD_TYPE);
        for (String repeated : List.of("bt/ bt/", "bt/ bt/A+", "bt/A+ bt/")) {
            fixture.assertRejectedCommand("edit 1 " + repeated, ParseException.class, duplicateMessage);
        }
        fixture.assertRejectedCommand("edit 1 bt/A+ bt/A+", ParseException.class, duplicateMessage);
        fixture.assertRejectedCommand(ADD_PATIENT + " bt/AB+", ParseException.class,
                String.format(AddCommand.MESSAGE_NOT_ADDED, duplicateMessage));
        assertEquals(List.of(originalPerson), model.getAddressBook().getPersonList());
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
            fixture.assertPersistedPatients(new PersonBuilder().withBloodType(bloodType).build());
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
}
