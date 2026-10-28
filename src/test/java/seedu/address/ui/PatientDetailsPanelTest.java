package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;
import seedu.address.testutil.CountingClock;
import seedu.address.testutil.PersonBuilder;

public class PatientDetailsPanelTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-21T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));

    @BeforeAll
    public static void setUpJavaFx() throws InterruptedException {
        startJavaFx();
    }

    @Test
    public void setPerson_allergies_displaysInMedicalProfileAndClearsStaleText() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel();
            Person recorded = new PersonBuilder().withBloodType("A+").withAllergies("Penicillin; latex")
                    .withAlcoholHistory("Does not drink").withSmokingStatus("former").build();
            panel.setPerson(recorded);
            Label allergies = getLabel(panel, "allergies");
            assertEquals("Allergies: Penicillin; latex", allergies.getText());
            assertFieldVisible(panel, "allergies", true);
            assertTrue(allergies.isWrapText());
            VBox medicalSection = getSection(panel, "medicalSection");
            int allergyIndex = medicalSection.getChildren().indexOf(allergies);
            assertEquals(allergyIndex + 1,
                    medicalSection.getChildren().indexOf(getLabel(panel, "bloodType")));
            assertEquals(allergyIndex + 3,
                    medicalSection.getChildren().indexOf(getLabel(panel, "smokingStatus")));
            assertEquals(allergyIndex + 4,
                    medicalSection.getChildren().indexOf(getLabel(panel, "alcoholHistory")));

            panel.setPerson(new PersonBuilder(recorded).withAllergies("").build());
            assertFieldVisible(panel, "allergies", false);
            panel.setPerson(new PersonBuilder().withAllergies("No known allergies").build());
            assertFieldVisible(panel, "allergies", true);
            assertEquals("Allergies: No known allergies", allergies.getText());
        });
    }

    @Test
    public void setPerson_longAllergies_wrapsWithinScrollableDetails() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel();
            String allergies = "Penicillin; latex; recorded reaction details. ".repeat(30).strip();
            panel.setPerson(new PersonBuilder().withAllergies(allergies).build());
            assertEquals("Allergies: " + allergies, getLabel(panel, "allergies").getText());
            assertRecordCanScrollToBottom(panel);
        });
    }

    @Test
    public void setPerson_smokingStatus_displaysInMedicalProfileAndHidesWhenCleared() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel();
            Person recorded = new PersonBuilder().withAlcoholHistory("Does not drink")
                    .withSmokingStatus("former").build();
            panel.setPerson(recorded);
            Label status = getLabel(panel, "smokingStatus");
            assertEquals("Smoking status: former", status.getText());
            assertFieldVisible(panel, "smokingStatus", true);
            assertTrue(status.isWrapText());
            VBox medicalSection = getSection(panel, "medicalSection");
            assertEquals(medicalSection.getChildren().indexOf(getLabel(panel, "dateOfBirth")) + 1,
                    medicalSection.getChildren().indexOf(status));

            panel.setPerson(new PersonBuilder(recorded).withSmokingStatus("").build());
            assertFieldVisible(panel, "smokingStatus", false);
            panel.setPerson(new PersonBuilder().withSmokingStatus("unknown").build());
            assertFieldVisible(panel, "smokingStatus", true);
            assertEquals("Smoking status: unknown", status.getText());
            panel.setPerson(new PersonBuilder().build());
            assertFieldVisible(panel, "smokingStatus", false);
        });
    }

    @Test
    public void setPerson_alcoholHistory_displaysInMedicalProfileAndClearsStaleText() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel();
            Person recorded = new PersonBuilder().withBloodType("A+").withAllergies("Penicillin")
                    .withAlcoholHistory("Occasional drinking").build();
            panel.setPerson(recorded);
            Label history = getLabel(panel, "alcoholHistory");
            assertEquals("Alcohol history: Occasional drinking", history.getText());
            assertFieldVisible(panel, "alcoholHistory", true);
            assertTrue(history.isWrapText());
            VBox medicalSection = getSection(panel, "medicalSection");
            assertEquals(medicalSection.getChildren().indexOf(getLabel(panel, "smokingStatus")) + 1,
                    medicalSection.getChildren().indexOf(history));

            panel.setPerson(new PersonBuilder(recorded).withAlcoholHistory("").build());
            assertFieldVisible(panel, "alcoholHistory", false);
            panel.setPerson(new PersonBuilder().withAlcoholHistory("Does not drink").build());
            assertFieldVisible(panel, "alcoholHistory", true);
            assertEquals("Alcohol history: Does not drink", history.getText());
        });
    }

    @Test
    public void setPerson_longAlcoholHistory_wrapsWithinScrollableDetails() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel();
            String history = "Occasional drinking with meals. ".repeat(30).strip();
            panel.setPerson(new PersonBuilder().withAlcoholHistory(history).build());
            assertEquals("Alcohol history: " + history, getLabel(panel, "alcoholHistory").getText());
            assertRecordCanScrollToBottom(panel);
        });
    }

    @Test
    public void setPerson_dateOfBirth_displaysAgeInMedicalProfileAndHidesWhenAbsent() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel();
            panel.setPerson(new PersonBuilder().withDateOfBirth("15/6/1990").build());
            Label birthDate = getLabel(panel, "dateOfBirth");
            int age = Period.between(LocalDate.of(1990, 6, 15), LocalDate.now()).getYears();
            assertEquals("Date of birth: 15/6/1990 (" + age + " years old)", birthDate.getText());
            assertFieldVisible(panel, "dateOfBirth", true);
            assertTrue(birthDate.isWrapText());
            VBox medicalSection = getSection(panel, "medicalSection");
            assertEquals(medicalSection.getChildren().indexOf(getLabel(panel, "bloodType")) + 1,
                    medicalSection.getChildren().indexOf(birthDate));

            panel.setPerson(new PersonBuilder().build());
            assertFieldVisible(panel, "dateOfBirth", false);
            panel.setPerson(new PersonBuilder().withDateOfBirth("1/1/2000").build());
            assertFieldVisible(panel, "dateOfBirth", true);
            assertTrue(birthDate.getText().startsWith("Date of birth: 1/1/2000 ("));
        });
    }

    @Test
    public void constructor_noPersonSelected_hidesDetails() {
        runOnFxThread(() -> {
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(FIXED_CLOCK);
            VBox detailsContainer = getDetailsContainer(patientDetailsPanel);

            assertFalse(detailsContainer.isManaged());
            assertFalse(detailsContainer.isVisible());
        });
    }

    @Test
    public void setPerson_validPerson_displaysAllDetails() {
        runOnFxThread(() -> {
            Person person = new PersonBuilder()
                    .withName("Alex Yeoh")
                    .withPhone("87438807")
                    .withEmail("alex@example.com")
                    .withAddress("123, Jurong West Ave 6, #08-111")
                    .withVisitDate("18/9/2026 1000")
                    .withNote("Monitor blood pressure before the next visit")
                    .withTags("family", "friend")
                    .build();
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(FIXED_CLOCK);

            patientDetailsPanel.setPerson(person);

            assertEquals("Alex Yeoh", getLabel(patientDetailsPanel, "name").getText());
            assertEquals("Phone: 87438807", getLabel(patientDetailsPanel, "phone").getText());
            assertEquals("Email: alex@example.com", getLabel(patientDetailsPanel, "email").getText());
            assertEquals("Address: 123, Jurong West Ave 6, #08-111",
                    getLabel(patientDetailsPanel, "address").getText());
            assertEquals("Visit date: 18/9/2026 1000 (Past)",
                    getLabel(patientDetailsPanel, "visitDate").getText());
            assertEquals("Note: Monitor blood pressure before the next visit",
                    getLabel(patientDetailsPanel, "note").getText());
            assertEquals("Previous visit dates", getLabel(patientDetailsPanel, "previousVisitDatesHeader").getText());
            assertEquals("No previous visit dates", getLabel(patientDetailsPanel, "previousVisitDates").getText());
            assertEquals("Tags: family, friend", getLabel(patientDetailsPanel, "tags").getText());
            assertTrue(getLabel(patientDetailsPanel, "address").isWrapText());
            assertTrue(getLabel(patientDetailsPanel, "note").isWrapText());

            VBox detailsContainer = getDetailsContainer(patientDetailsPanel);
            assertTrue(detailsContainer.isManaged());
            assertTrue(detailsContainer.isVisible());
        });
    }

    @Test
    public void constructor_sections_followClinicalScanningOrder() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel(FIXED_CLOCK);
            VBox detailsContainer = getDetailsContainer(panel);

            assertEquals(List.of(
                    getLabel(panel, "name"),
                    getSection(panel, "visitOverviewSection"),
                    getSection(panel, "medicalSection"),
                    getSection(panel, "contactSection"),
                    getSection(panel, "additionalNotesSection"),
                    getSection(panel, "visitHistorySection")),
                    detailsContainer.getChildren());
            assertEquals("Visit details", getSectionHeading(panel, "visitOverviewSection").getText());
            assertEquals("Medical profile", getSectionHeading(panel, "medicalSection").getText());
            assertEquals("Contact information", getSectionHeading(panel, "contactSection").getText());
            assertEquals("Additional notes", getSectionHeading(panel, "additionalNotesSection").getText());
            assertEquals("Visit history", getSectionHeading(panel, "visitHistorySection").getText());
<<<<<<< HEAD
        });
    }

    @Test
=======
>>>>>>> 984f509c0ccd1e8db069bf6b9066fd0060457289
    public void setPerson_emailAndNote_hidesEmptyFieldsAndRestoresPopulatedFields() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel(FIXED_CLOCK);
            Person firstRecorded = new PersonBuilder().withEmail("first@example.com")
                    .withNote("First note").build();
            Person empty = new PersonBuilder().withEmail("").withNote("").build();
            Person secondRecorded = new PersonBuilder().withEmail("second@example.com")
                    .withNote("Second note").build();

            panel.setPerson(firstRecorded);
            assertEquals("Email: first@example.com", getLabel(panel, "email").getText());
            assertEquals("Note: First note", getLabel(panel, "note").getText());
            assertFieldVisible(panel, "email", true);
            assertFieldVisible(panel, "note", true);

            panel.setPerson(empty);
            assertFieldVisible(panel, "email", false);
            assertFieldVisible(panel, "note", false);

            panel.setPerson(secondRecorded);
            assertEquals("Email: second@example.com", getLabel(panel, "email").getText());
            assertEquals("Note: Second note", getLabel(panel, "note").getText());
            assertFieldVisible(panel, "email", true);
            assertFieldVisible(panel, "note", true);
        });
    }

    @Test
    public void setPerson_bloodType_displaysInMedicalProfileAndHidesWhenAbsent() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel();
            panel.setPerson(new PersonBuilder().withDateOfBirth("15/6/1990").withBloodType("A+").build());
            Label bloodTypeLabel = getLabel(panel, "bloodType");
            assertEquals("Blood type: A+", bloodTypeLabel.getText());
            assertFieldVisible(panel, "bloodType", true);
            assertTrue(bloodTypeLabel.isWrapText());
            VBox medicalSection = getSection(panel, "medicalSection");
            assertEquals(medicalSection.getChildren().indexOf(getLabel(panel, "allergies")) + 1,
                    medicalSection.getChildren().indexOf(bloodTypeLabel));

            panel.setPerson(new PersonBuilder().build());
            assertFieldVisible(panel, "bloodType", false);
            panel.setPerson(new PersonBuilder().withBloodType("O-").build());
            assertFieldVisible(panel, "bloodType", true);
            assertEquals("Blood type: O-", bloodTypeLabel.getText());
        });
    }

    private static void assertFieldVisible(PatientDetailsPanel panel, String labelId, boolean isVisible) {
        Label label = getLabel(panel, labelId);
        assertEquals(isVisible, label.isVisible(), labelId);
        assertEquals(isVisible, label.isManaged(), labelId);
        if (!isVisible) {
            assertEquals("", label.getText(), labelId);
        }
    }

    @Test
    public void setPerson_emergencyContact_displaysBelowPhoneAndHidesWhenAbsent() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel();
            Person patient = new PersonBuilder().withEmergencyContact("Mary Tan, 91234567").build();
            panel.setPerson(patient);
            Label contact = getLabel(panel, "emergencyContact");
            assertEquals("Emergency Contact: 91234567 (Mary Tan)", contact.getText());
            assertTrue(contact.isVisible());
            assertTrue(contact.isManaged());
            assertTrue(contact.isWrapText());
            VBox contactSection = getSection(panel, "contactSection");
            assertEquals(contactSection.getChildren().indexOf(getLabel(panel, "phone")) + 1,
                    contactSection.getChildren().indexOf(contact));

            panel.setPerson(new PersonBuilder(patient).withEmergencyContact("98765432").build());
            assertEquals("Emergency Contact: 98765432", contact.getText());
            assertTrue(contact.isVisible());
            assertTrue(contact.isManaged());

            panel.setPerson(new PersonBuilder().build());
            assertFalse(contact.isVisible());
            assertFalse(contact.isManaged());
            assertFalse(contact.getText().contains("Mary Tan"));
        });
    }

    @Test
    public void setPerson_populatedPreviousDates_displaysNewestFirstWithoutChangingPerson() {
        // EP: Several stored oldest-first entries render newest first without modifying the Person.
        runOnFxThread(() -> {
            Person person = new PersonBuilder()
                    .withPreviousVisitDates("01/08/2026 0930", "2/8/2026 0900", "2/8/2026 1030")
                    .build();
            Person originalPerson = new PersonBuilder(person).build();
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(FIXED_CLOCK);

            patientDetailsPanel.setPerson(person);
            patientDetailsPanel.setPerson(person);

            assertEquals("2/8/2026 1030\n2/8/2026 0900\n1/8/2026 0930",
                    getLabel(patientDetailsPanel, "previousVisitDates").getText());
            assertEquals("Visit date: 18/9/2026 1000 (Past)", getLabel(patientDetailsPanel, "visitDate").getText());
            assertEquals(originalPerson, person);

            List<Node> historyDetails = getSection(patientDetailsPanel, "visitHistorySection").getChildren();
            int historyHeaderIndex = historyDetails.indexOf(
                    getLabel(patientDetailsPanel, "previousVisitDatesHeader"));
            assertEquals(getLabel(patientDetailsPanel, "previousVisitDates"),
                    historyDetails.get(historyHeaderIndex + 1));
        });
    }

    @Test
    public void setPerson_differentPatients_replacesPreviousDatesAndEmptyState() {
        // Boundary: Switching between one and zero retained dates must replace stale history text.
        runOnFxThread(() -> {
            Person personWithPreviousDates = new PersonBuilder().withPreviousVisitDates("1/8/2026 0930").build();
            Person personWithoutPreviousDates = new PersonBuilder().withName("Another Patient").build();
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(FIXED_CLOCK);

            patientDetailsPanel.setPerson(personWithPreviousDates);
            patientDetailsPanel.setPerson(personWithoutPreviousDates);

            assertEquals("Another Patient", getLabel(patientDetailsPanel, "name").getText());
            assertEquals("No previous visit dates", getLabel(patientDetailsPanel, "previousVisitDates").getText());

            patientDetailsPanel.setPerson(personWithPreviousDates);

            assertEquals("1/8/2026 0930", getLabel(patientDetailsPanel, "previousVisitDates").getText());
            assertTrue(personWithoutPreviousDates.getPreviousVisitDates().isEmpty());
        });
    }

    @Test
    public void setPerson_longRecord_displaysEveryDateWithScrolling() {
        // Stress sample: 400 dates exercise scrolling and wrapping, not a specified maximum history length.
        runOnFxThread(() -> {
            LocalDateTime firstVisit = LocalDateTime.of(2025, 1, 1, 9, 30);
            String[] previousDates = IntStream.range(0, 400)
                    .mapToObj(day -> firstVisit.plusDays(day).format(VisitDate.FORMATTER))
                    .toArray(String[]::new);
            Person person = new PersonBuilder().withPreviousVisitDates(previousDates)
                    .withNote("A long note that wraps within the patient details panel. ".repeat(20)).build();
            Person originalPerson = new PersonBuilder(person).build();
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(FIXED_CLOCK);

            patientDetailsPanel.setPerson(person);

            Label previousVisitDates = getLabel(patientDetailsPanel, "previousVisitDates");
            assertEquals(List.of(previousDates).reversed(), previousVisitDates.getText().lines().toList());
            assertTrue(previousVisitDates.isWrapText());
            assertRecordCanScrollToBottom(patientDetailsPanel);
            assertEquals(originalPerson, person);
        });
    }

    @Test
    public void setPerson_visitOnPreviousDay_displaysPast() {
        // Boundary: 2359 on yesterday is Past; Today begins at local midnight, regardless of appointment time.
        runOnFxThread(() -> {
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(FIXED_CLOCK);
            Person person = new PersonBuilder().withVisitDate("20/10/2026 2359").build();

            patientDetailsPanel.setPerson(person);

            assertEquals("Visit date: 20/10/2026 2359 (Past)", getLabel(patientDetailsPanel, "visitDate").getText());
        });
    }

    @Test
    public void setPerson_visitTodayAtAnyTime_displaysToday() {
        // EP: All times on the clock's local date are Today, before or after its current instant.
        // Boundary: Include both day endpoints, 0000 and 2359.
        runOnFxThread(() -> {
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(FIXED_CLOCK);
            for (String dateTime : List.of("21/10/2026 0000", "21/10/2026 1159", "21/10/2026 1200",
                    "21/10/2026 2359")) {
                Person person = new PersonBuilder().withVisitDate(dateTime).build();

                patientDetailsPanel.setPerson(person);

                assertEquals("Visit date: " + dateTime + " (Today)",
                        getLabel(patientDetailsPanel, "visitDate").getText());
            }
        });
    }

    @Test
    public void setPerson_visitOnNextDay_displaysUpcoming() {
        // Boundary: Tomorrow at 0000 is Upcoming, immediately outside today's calendar partition.
        runOnFxThread(() -> {
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(FIXED_CLOCK);
            Person person = new PersonBuilder().withVisitDate("22/10/2026 0000").build();

            patientDetailsPanel.setPerson(person);

            assertEquals("Visit date: 22/10/2026 0000 (Upcoming)",
                    getLabel(patientDetailsPanel, "visitDate").getText());
        });
    }

    @Test
    public void setPerson_clockTimeZone_usesLocalCalendarDate() {
        runOnFxThread(() -> {
            Clock utcClock = Clock.fixed(Instant.parse("2026-10-21T00:30:00Z"), ZoneId.of("UTC"));
            PatientDetailsPanel utcPanel = new PatientDetailsPanel(utcClock);
            Clock losAngelesClock = utcClock.withZone(ZoneId.of("America/Los_Angeles"));
            PatientDetailsPanel losAngelesPanel = new PatientDetailsPanel(losAngelesClock);
            Person person = new PersonBuilder().withVisitDate("21/10/2026 0000").build();

            utcPanel.setPerson(person);
            losAngelesPanel.setPerson(person);

            assertEquals("Visit date: 21/10/2026 0000 (Today)", getLabel(utcPanel, "visitDate").getText());
            assertEquals("Visit date: 21/10/2026 0000 (Upcoming)", getLabel(losAngelesPanel, "visitDate").getText());
        });
    }

    @Test
    public void setPerson_clockCrossesMidnight_recomputesLabelOnlyWhenRendered() {
        // Boundary: Advance one nanosecond across local midnight; labels update on explicit rendering only.
        runOnFxThread(() -> {
            Clock beforeMidnight = Clock.fixed(Instant.parse("2026-12-31T15:59:59.999999999Z"), FIXED_CLOCK.getZone());
            CountingClock clock = new CountingClock(beforeMidnight);
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(clock);
            Person person = new PersonBuilder().withVisitDate("31/12/2026 0000").build();
            assertEquals(0, clock.getReadCount());

            patientDetailsPanel.setPerson(person);
            assertEquals("Visit date: 31/12/2026 0000 (Today)", getLabel(patientDetailsPanel, "visitDate").getText());
            assertEquals(1, clock.getReadCount());

            clock.setDelegate(Clock.offset(beforeMidnight, Duration.ofNanos(1)));
            assertEquals("Visit date: 31/12/2026 0000 (Today)", getLabel(patientDetailsPanel, "visitDate").getText());
            patientDetailsPanel.setPerson(person);

            assertEquals("Visit date: 31/12/2026 0000 (Past)", getLabel(patientDetailsPanel, "visitDate").getText());
            assertEquals(2, clock.getReadCount());
        });
    }

    @Test
    public void constructorAndSetPerson_nullArguments_throwsNullPointerException() {
        // API preconditions, distinct from patient data entered through the CLI.
        runOnFxThread(() -> {
            assertThrows(NullPointerException.class, () -> new PatientDetailsPanel(null));
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(FIXED_CLOCK);
            assertThrows(NullPointerException.class, () -> patientDetailsPanel.setPerson(null));
        });
    }

    private static void assertRecordCanScrollToBottom(PatientDetailsPanel patientDetailsPanel) {
        ScrollPane scrollPane = (ScrollPane) patientDetailsPanel.getRoot();
        Scene scene = new Scene(scrollPane, 320, 240);
        scene.getStylesheets().add(PatientDetailsPanel.class.getResource("/view/DarkTheme.css").toExternalForm());
        scrollPane.applyCss();
        scrollPane.layout();

        Label previousVisitDates = getLabel(patientDetailsPanel, "previousVisitDates");
        assertTrue(scrollPane.isFitToWidth());
        assertTrue(getDetailsContainer(patientDetailsPanel).getHeight() > scrollPane.getViewportBounds().getHeight());
        assertTrue(previousVisitDates.getHeight() >= previousVisitDates.prefHeight(previousVisitDates.getWidth()));

        scrollPane.setVvalue(scrollPane.getVmax());
        scrollPane.layout();

        Node viewport = scrollPane.lookup(".viewport");
        assertNotNull(viewport);
        Bounds viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());
        Bounds previousVisitDatesBounds = previousVisitDates.localToScene(previousVisitDates.getBoundsInLocal());
        assertTrue(previousVisitDatesBounds.getMaxY() <= viewportBounds.getMaxY());
        assertTrue(previousVisitDatesBounds.getMaxY() > viewportBounds.getMinY());
    }

    private static VBox getDetailsContainer(PatientDetailsPanel patientDetailsPanel) {
        ScrollPane scrollPane = (ScrollPane) patientDetailsPanel.getRoot();
        VBox detailsContainer = (VBox) scrollPane.getContent();
        assertNotNull(detailsContainer);
        return detailsContainer;
    }

    private static VBox getSection(PatientDetailsPanel patientDetailsPanel, String sectionId) {
        VBox section = (VBox) getDetailsContainer(patientDetailsPanel).lookup("#" + sectionId);
        assertNotNull(section);
        return section;
    }

    private static Label getSectionHeading(PatientDetailsPanel patientDetailsPanel, String sectionId) {
        return (Label) getSection(patientDetailsPanel, sectionId).getChildren().getFirst();
    }

    private static Label getLabel(PatientDetailsPanel patientDetailsPanel, String labelId) {
        Label label = (Label) getDetailsContainer(patientDetailsPanel).lookup("#" + labelId);
        assertNotNull(label);
        return label;
    }

}
