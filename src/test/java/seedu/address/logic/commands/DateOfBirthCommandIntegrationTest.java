package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE_OF_BIRTH;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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
import seedu.address.model.person.DateOfBirth;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.LogicManagerIntegrationFixture;
import seedu.address.testutil.PersonBuilder;

public class DateOfBirthCommandIntegrationTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-21T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));

    private static final String ADD_PATIENT = "add n/Amy Bee dob/15/6/1990 p/85355255 "
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
        fixture = new LogicManagerIntegrationFixture(model, testFolder, FIXED_CLOCK);
        logic = fixture.getLogic();
        storage = fixture.getAddressBookStorage();
    }

    @Test
    public void execute_addEditViewAndNext_preservesDateOfBirthInStorage() throws Exception {
        logic.execute(ADD_PATIENT);
        fixture.assertPersistedPatients(new PersonBuilder().withDateOfBirth("15/6/1990").build());

        logic.execute("edit 1 n/Amy Tan dob/20/7/1985");
        Person edited = new PersonBuilder().withName("Amy Tan").withDateOfBirth("20/7/1985").build();
        fixture.assertPersistedPatients(edited);
        CommandResult viewResult = logic.execute("view 1");
        assertEquals(edited, viewResult.getPersonToView().orElseThrow());

        logic.execute("edit 1 note/Review medication");
        logic.execute("next 1 1/1/2099 1000");
        fixture.assertPersistedPatients(new PersonBuilder(edited).withNote("Review medication")
                .withVisitDate("1/1/2099 1000").withPreviousVisitDates(edited.getVisitDate().toString()).build());
    }

    @Test
    public void execute_combinedOptionalDetails_preservesFieldsAndHistory() throws Exception {
        logic.execute(ADD_PATIENT + " bt/A+ ec/91234567");
        Person original = new PersonBuilder().withDateOfBirth("15/6/1990")
                .withBloodType("A+").withEmergencyContact("91234567").build();
        fixture.assertPersistedPatients(original);

        logic.execute("next 1 1/1/2099 1000");
        Person scheduled = new PersonBuilder(original).withVisitDate("1/1/2099 1000")
                .withPreviousVisitDates(original.getVisitDate().toString()).build();
        fixture.assertPersistedPatients(scheduled);

        logic.execute("edit 1 dob/20/7/1985 bt/O- ec/Mary Tan, 98765432");
        Person edited = new PersonBuilder(scheduled).withDateOfBirth("20/7/1985")
                .withBloodType("O-").withEmergencyContact("Mary Tan, 98765432").build();
        fixture.assertPersistedPatients(edited);
        assertEquals(edited, logic.execute("view 1").getPersonToView().orElseThrow());

        logic.execute("edit 1 dob/");
        Person withoutBirthDate = new PersonBuilder(edited).withDateOfBirth("").build();
        fixture.assertPersistedPatients(withoutBirthDate);
        logic.execute("edit 1 bt/ ec/");
        fixture.assertPersistedPatients(
                new PersonBuilder(withoutBirthDate).withBloodType("").withEmergencyContact("").build());
    }

    @Test
    public void execute_reorderedPrefixesAndWhitespace_acceptsDateOfBirth() throws Exception {
        logic.execute(ADD_PATIENT.replace(" dob/15/6/1990", "") + " dob/  15/06/1990  ");
        fixture.assertPersistedPatients(new PersonBuilder().withDateOfBirth("15/6/1990").build());
        logic.execute("edit 1 dob/ 20/07/1985 n/Amy Tan");
        fixture.assertPersistedPatients(new PersonBuilder().withName("Amy Tan").withDateOfBirth("20/7/1985").build());
    }

    @Test
    public void execute_omittedDateOfBirth_supportsExistingCommandsAndRecords() throws Exception {
        logic.execute(ADD_PATIENT.replace(" dob/15/6/1990", ""));
        fixture.assertPersistedPatients(new PersonBuilder().build());
        logic.execute("edit 1 dob/15/6/1990");
        fixture.assertPersistedPatients(new PersonBuilder().withDateOfBirth("15/6/1990").build());

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
            fixture.assertPersistedPatients(cleared);
            CommandResult viewResult = logic.execute("view 1");
            assertEquals(cleared, viewResult.getPersonToView().orElseThrow());
        }
        logic.execute("edit 1 dob/");
        fixture.assertPersistedPatients(cleared);
        logic.execute("edit 1 dob/1/1/2000");
        fixture.assertPersistedPatients(new PersonBuilder().withDateOfBirth("1/1/2000").build());
    }

    @Test
    public void execute_clearDateOfBirthWithOtherFields_updatesBothFields() throws Exception {
        logic.execute(ADD_PATIENT);
        logic.execute("edit 1 dob/ note/Review medication");
        fixture.assertPersistedPatients(new PersonBuilder().withNote("Review medication").build());
        logic.execute("edit 1 dob/15/6/1990");
        logic.execute("edit 1 note/Keep this note dob/");
        fixture.assertPersistedPatients(new PersonBuilder().withNote("Keep this note").build());
    }

    @Test
    public void execute_invalidOrRepeatedDateOfBirth_leavesPatientAndFileUnchanged() throws Exception {
        logic.execute(ADD_PATIENT);
        Person originalPerson = model.getFilteredPersonList().getFirst();
        for (String date : List.of("31/2/2000", "1/1/9999", "1990-06-15")) {
            fixture.assertRejectedCommand("edit 1 dob/" + date, ParseException.class,
                    DateOfBirth.MESSAGE_CONSTRAINTS);
            fixture.assertRejectedCommand(ADD_PATIENT.replace("dob/15/6/1990", "dob/" + date), ParseException.class,
                    String.format(AddCommand.MESSAGE_NOT_ADDED, DateOfBirth.MESSAGE_CONSTRAINTS));
        }
        fixture.assertRejectedCommand(ADD_PATIENT.replace("dob/15/6/1990", "dob/"), ParseException.class,
                String.format(AddCommand.MESSAGE_NOT_ADDED, DateOfBirth.MESSAGE_CONSTRAINTS));
        String duplicateMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DATE_OF_BIRTH);
        for (String repeated : List.of("dob/ dob/", "dob/ dob/15/6/1990", "dob/15/6/1990 dob/")) {
            fixture.assertRejectedCommand("edit 1 " + repeated, ParseException.class, duplicateMessage);
        }
        fixture.assertRejectedCommand("edit 1 dob/15/6/1990 dob/15/6/1990", ParseException.class, duplicateMessage);
        fixture.assertRejectedCommand(ADD_PATIENT + " dob/1/1/2000", ParseException.class,
                String.format(AddCommand.MESSAGE_NOT_ADDED, duplicateMessage));
        assertEquals(List.of(originalPerson), model.getAddressBook().getPersonList());
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
}
