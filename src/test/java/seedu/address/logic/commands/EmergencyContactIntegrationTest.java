package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMERGENCY_CONTACT;

import java.io.IOException;
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
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.EmergencyContact;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.LogicManagerIntegrationFixture;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests emergency contact input, command behavior, viewing, and persistence through the application logic.
 */
public class EmergencyContactIntegrationTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-21T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));

    private static final String CONTACT = "Mary Tan, 98765432";
    private static final String REPLACEMENT_CONTACT = "David Tan, 87654321";
    private static final String ADD_COMMAND =
            "add n/John Tan p/91234567 a/123 Clementi Road vd/22/10/2026 1000";

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
    public void execute_addWithContact_persistsAndViewsContact() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        Person expected = new PersonBuilder().withName("John Tan").withPhone("91234567")
                .withEmail("").withAddress("123 Clementi Road").withVisitDate("22/10/2026 1000")
                .withEmergencyContact(CONTACT).build();
        fixture.assertPersistedPatients(expected);

        CommandResult result = logic.execute("view 1");
        assertEquals(expected, result.getPersonToView().orElseThrow());
        assertFalse(result.shouldSaveAddressBook());
    }

    @Test
    public void execute_addWithoutContact_keepsFieldOptional() throws Exception {
        logic.execute(ADD_COMMAND);
        Person patient = model.getFilteredPersonList().getFirst();
        assertTrue(patient.getEmergencyContact().isEmpty());
        fixture.assertPersistedPatients(patient);
    }

    @Test
    public void execute_phoneOnlyContact_addsReplacesAndClearsWithoutLosingOtherDetails() throws Exception {
        logic.execute(ADD_COMMAND + " ec/  98765432  note/Follow up");
        Person original = model.getFilteredPersonList().getFirst();
        assertEquals("98765432", original.getEmergencyContact().toString());
        fixture.assertPersistedPatients(original);
        assertEquals(original, logic.execute("view 1").getPersonToView().orElseThrow());

        for (String contact : List.of(CONTACT, "87654321", "")) {
            logic.execute("edit 1 ec/" + contact);
            fixture.assertPersistedPatients(new PersonBuilder(original).withEmergencyContact(contact).build());
        }
    }

    @Test
    public void execute_contactBeforeOtherFields_parsesBoundariesAndWhitespace() throws Exception {
        logic.execute("add ec/ Mary Tan ,98765432 n/John Tan p/91234567"
                + " a/123 Clementi Road vd/22/10/2026 1000 note/Follow up t/priority");
        Person patient = model.getFilteredPersonList().getFirst();
        assertEquals(CONTACT, patient.getEmergencyContact().toString());
        assertEquals("Follow up", patient.getNote().toString());
        fixture.assertPersistedPatients(patient);
    }

    @Test
    public void execute_editContact_addsAndReplacesContact() throws Exception {
        logic.execute(ADD_COMMAND);
        Person original = model.getFilteredPersonList().getFirst();

        logic.execute("edit 1 ec/" + CONTACT);
        fixture.assertPersistedPatients(new PersonBuilder(original).withEmergencyContact(CONTACT).build());

        logic.execute("edit 1 ec/" + REPLACEMENT_CONTACT);
        fixture.assertPersistedPatients(new PersonBuilder(original).withEmergencyContact(REPLACEMENT_CONTACT).build());
    }

    @Test
    public void execute_emptyContactEdit_clearsAndPersistsContact() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        Person expected = new PersonBuilder(model.getFilteredPersonList().getFirst())
                .withEmergencyContact("").build();

        for (String emptyContact : List.of("", "   ", " \t ")) {
            logic.execute("edit 1 ec/" + CONTACT);
            logic.execute("edit 1 ec/" + emptyContact);

            fixture.assertPersistedPatients(expected);
            CommandResult result = logic.execute("view 1");
            assertTrue(result.getPersonToView().orElseThrow().getEmergencyContact().isEmpty());
        }
    }

    @Test
    public void execute_emptyContactWithOtherFields_clearsContactAndUpdatesNote() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        Person expected = new PersonBuilder(model.getFilteredPersonList().getFirst())
                .withEmergencyContact("").withNote("Review medication").build();

        logic.execute("edit 1 ec/ note/Review medication");
        fixture.assertPersistedPatients(expected);

        logic.execute("edit 1 ec/");
        fixture.assertPersistedPatients(expected);
    }

    @Test
    public void execute_emptyContactAdd_rejectsWithoutChangingData() throws Exception {
        logic.execute(ADD_COMMAND);
        for (String emptyContact : List.of("", "   ", " \t ")) {
            fixture.assertRejectedCommand(
                    "add n/Jane Tan p/82345678 a/Other Road vd/23/10/2026 1000 ec/" + emptyContact,
                    ParseException.class,
                    String.format(AddCommand.MESSAGE_NOT_ADDED, EmergencyContact.MESSAGE_CONSTRAINTS));
        }
    }

    @Test
    public void execute_unrelatedEditAndNext_preservesContact() throws Exception {
        logic.execute(ADD_COMMAND.replace("22/10/2026 1000", "1/1/2000 1000") + " ec/" + CONTACT);
        Person original = model.getFilteredPersonList().getFirst();
        logic.execute("edit 1 note/Review medication");
        Person edited = new PersonBuilder(original).withNote("Review medication").build();
        fixture.assertPersistedPatients(edited);

        logic.execute("next 1 1/1/2099 1000");
        fixture.assertPersistedPatients(new PersonBuilder(edited).withVisitDate("1/1/2099 1000")
                .withPreviousVisitDates(original.getVisitDate().toString()).build());
    }

    @Test
    public void execute_editContactWithVisitHistory_preservesHistoryAndOtherDetails() throws Exception {
        Person original = new PersonBuilder().withVisitDate("3/1/2000 1000")
                .withPreviousVisitDates("1/1/2000 1000", "2/1/2000 1000")
                .withEmergencyContact(CONTACT).build();
        model.addPerson(original);

        logic.execute("edit 1 ec/" + REPLACEMENT_CONTACT);
        fixture.assertPersistedPatients(new PersonBuilder(original).withEmergencyContact(REPLACEMENT_CONTACT).build());

        logic.execute("edit 1 ec/");
        fixture.assertPersistedPatients(new PersonBuilder(original).withEmergencyContact("").build());
    }

    @Test
    public void execute_findListAndSort_preservesContact() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        Person original = model.getFilteredPersonList().getFirst();
        for (String command : List.of("find n/John", "sort date", "list")) {
            logic.execute(command);
            assertEquals(List.of(original), model.getFilteredPersonList());
            fixture.assertPersistedPatients(original);
        }
    }

    @Test
    public void execute_invalidContact_preservesModelAndFile() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        for (String invalid : List.of("12", "abc", "Mary Tan", "Mary Tan 98765432", "Mary Tan,",
                ",98765432", "Mary Tan, 12", "Mary Tan, abc", "Mary@Tan, 98765432",
                "Mary Tan, 98765432, 87654321")) {
            fixture.assertRejectedCommand("edit 1 ec/" + invalid, ParseException.class,
                    EmergencyContact.MESSAGE_CONSTRAINTS);
            fixture.assertRejectedCommand("add n/Jane Tan p/82345678 a/Other Road vd/23/10/2026 1000 ec/" + invalid,
                    ParseException.class,
                    String.format(AddCommand.MESSAGE_NOT_ADDED, EmergencyContact.MESSAGE_CONSTRAINTS));
        }
    }

    @Test
    public void execute_repeatedContact_rejectsDuplicatePrefixes() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        String reason = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMERGENCY_CONTACT);
        fixture.assertRejectedCommand("edit 1 ec/" + CONTACT + " ec/" + REPLACEMENT_CONTACT, ParseException.class,
                reason);
        fixture.assertRejectedCommand("edit 1 ec/ ec/", ParseException.class, reason);
        fixture.assertRejectedCommand("edit 1 ec/" + CONTACT + " ec/", ParseException.class, reason);
        fixture.assertRejectedCommand("edit 1 ec/ ec/" + CONTACT, ParseException.class, reason);
        fixture.assertRejectedCommand(ADD_COMMAND + " ec/invalid ec/" + CONTACT, ParseException.class,
                String.format(AddCommand.MESSAGE_NOT_ADDED, reason));
    }

    @Test
    public void execute_invalidIndex_preservesContactAndFile() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        fixture.assertRejectedCommand("edit 2 ec/" + REPLACEMENT_CONTACT, CommandException.class,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        fixture.assertRejectedCommand("edit 2 ec/", CommandException.class,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_duplicatePatientWithDifferentContact_rejectsDuplicateIdentity() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        fixture.assertRejectedCommand(ADD_COMMAND + " ec/" + REPLACEMENT_CONTACT, CommandException.class,
                String.format(AddCommand.MESSAGE_NOT_ADDED, AddCommand.MESSAGE_DUPLICATE_PERSON));
        assertEquals(CONTACT, model.getFilteredPersonList().getFirst().getEmergencyContact().toString());
    }

    @Test
    public void execute_filteredEdit_updatesDisplayedPatient() throws Exception {
        logic.execute(ADD_COMMAND + " ec/" + CONTACT);
        logic.execute("add n/Jane Tan p/82345678 a/Other Road vd/23/10/2026 1000");
        logic.execute("find n/Jane");
        logic.execute("edit 1 ec/" + REPLACEMENT_CONTACT);

        List<Person> patients = storage.readAddressBook().orElseThrow().getPersonList();
        assertEquals(CONTACT, patients.get(0).getEmergencyContact().toString());
        assertEquals(REPLACEMENT_CONTACT, patients.get(1).getEmergencyContact().toString());

        logic.execute("find n/Jane");
        logic.execute("edit 1 ec/");
        patients = storage.readAddressBook().orElseThrow().getPersonList();
        assertEquals(CONTACT, patients.get(0).getEmergencyContact().toString());
        assertTrue(patients.get(1).getEmergencyContact().isEmpty());
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
}
