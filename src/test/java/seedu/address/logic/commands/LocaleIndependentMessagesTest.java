package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests that counts in user-visible messages use the digits 0 to 9 even when the computer's locale uses other digits.
 */
public class LocaleIndependentMessagesTest {

    /** A locale whose default number formatting uses Arabic-Indic digits. */
    private static final Locale ARABIC_DIGITS_LOCALE = Locale.forLanguageTag("ar-SA");
    /** A free slot on 1/1/2026, the day on which all seven typical persons are already visited. */
    private static final String FULL_DAY_VISIT_DATE = "1/1/2026 1500";
    private static final String OTHER_DAY_VISIT_DATE = "31/12/2025 1000";
    /** Noon on 31/12/2025, after the other-day visit and before every visit on the full day. */
    private static final Clock CLOCK_BEFORE_FULL_DAY = Clock.fixed(Instant.parse("2025-12-31T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));

    private Locale originalLocale;
    private Model model;

    @BeforeEach
    public void setUp() {
        originalLocale = Locale.getDefault();
        Locale.setDefault(ARABIC_DIGITS_LOCALE);
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @AfterEach
    public void tearDown() {
        Locale.setDefault(originalLocale);
    }

    @Test
    public void testLocale_formatsCountsWithOtherDigits() {
        // guards the other tests: without an explicit locale, this locale would not print 7 as "7"
        assertNotEquals("7", String.format("%d", 7));
    }

    @Test
    public void find_patientCount_usesWesternDigits() {
        CommandResult result = new FindCommand(person -> true).execute(model);

        assertEquals("7 patients found.", result.getFeedbackToUser());
    }

    @Test
    public void agenda_visitCount_usesWesternDigits() {
        CommandResult result = new AgendaCommand(LocalDate.of(2026, 1, 1)).execute(model);

        assertTrue(result.getFeedbackToUser().startsWith("Showing 7 visits from"), result.getFeedbackToUser());
    }

    @Test
    public void clear_entryCount_usesWesternDigits() {
        CommandResult result = new ClearCommand().execute(model);

        assertEquals("Clear 7 entries? Type y to confirm. Any other input cancels.", result.getFeedbackToUser());
    }

    @Test
    public void add_fullDayAndDuplicateNameCounts_useWesternDigits() throws Exception {
        model.addPerson(new PersonBuilder(ALICE).withPhone("91234567").withVisitDate(OTHER_DAY_VISIT_DATE).build());
        Person thirdAlice = new PersonBuilder(ALICE).withPhone("92345678").withVisitDate(FULL_DAY_VISIT_DATE).build();

        String feedback = new AddCommand(thirdAlice).execute(model).getFeedbackToUser();

        assertTrue(feedback.startsWith("2 patients named Alice Pauline already exist."), feedback);
        assertTrue(feedback.contains("you already have 7 visits on"), feedback);
    }

    @Test
    public void edit_fullDayCount_usesWesternDigits() throws Exception {
        model.addPerson(new PersonBuilder().withName("Other Day").withPhone("93456789")
                .withVisitDate(OTHER_DAY_VISIT_DATE).build());
        Index otherDayIndex = Index.fromOneBased(model.getFilteredPersonList().size());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withVisitDate(FULL_DAY_VISIT_DATE).build();

        String feedback = new EditCommand(otherDayIndex, descriptor).execute(model).getFeedbackToUser();

        assertTrue(feedback.startsWith("Heads up: you already have 7 visits on"), feedback);
    }

    @Test
    public void next_fullDayCount_usesWesternDigits() throws Exception {
        model.addPerson(new PersonBuilder().withName("Other Day").withPhone("93456789")
                .withVisitDate(OTHER_DAY_VISIT_DATE).build());
        Index otherDayIndex = Index.fromOneBased(model.getFilteredPersonList().size());

        String feedback = new NextCommand(otherDayIndex, new VisitDate(FULL_DAY_VISIT_DATE), CLOCK_BEFORE_FULL_DAY)
                .execute(model).getFeedbackToUser();

        assertTrue(feedback.startsWith("Heads up: you already have 7 visits on"), feedback);
    }
}
