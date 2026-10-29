package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.ui.JavaFxTestUtil.callOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;
import static seedu.address.ui.JavaFxTestUtil.waitForNextPulse;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;
import seedu.address.testutil.CountingClock;
import seedu.address.testutil.PersonBuilder;

public class PatientDetailsPanelTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-21T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final int LONG_RECORD_DATE_COUNT = 400;
    private static final String LONG_RECORD_NOTE =
            "A long note that wraps within the patient details panel. ".repeat(20);
    private static final double MINIMUM_PAGE_SCROLL_RATIO = 0.75;
    private static final double MAXIMUM_PAGE_SCROLL_RATIO = 1.1;
    private static final double MINIMUM_PAGE_TO_ARROW_RATIO = 2.0;
    private static final List<String> LONG_RECORD_PREVIOUS_DATES = createLongRecordPreviousDates();

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
    public void setPerson_noMedicalInformation_showsPlaceholder() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel();
            Label placeholder = getLabel(panel, "medicalPlaceholder");

            panel.setPerson(new PersonBuilder().build());
            assertEquals("No medical information recorded", placeholder.getText());
            assertTrue(placeholder.isVisible());
            assertTrue(placeholder.isManaged());

            panel.setPerson(new PersonBuilder().withBloodType("A+").build());
            assertFalse(placeholder.isVisible());
            assertFalse(placeholder.isManaged());

            panel.setPerson(new PersonBuilder().build());
            assertTrue(placeholder.isVisible());
            assertTrue(placeholder.isManaged());
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
    public void clear_displayedPerson_hidesDetails() {
        runOnFxThread(() -> {
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(FIXED_CLOCK);
            patientDetailsPanel.setPerson(new PersonBuilder().build());

            patientDetailsPanel.clear();

            VBox detailsContainer = getDetailsContainer(patientDetailsPanel);
            assertFalse(detailsContainer.isManaged());
            assertFalse(detailsContainer.isVisible());
        });
    }

    @Test
    public void clearIfShowing_matchingPerson_hidesDetails() {
        runOnFxThread(() -> {
            Person displayedPerson = new PersonBuilder().build();
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(FIXED_CLOCK);
            patientDetailsPanel.setPerson(displayedPerson);

            patientDetailsPanel.clearIfShowing(displayedPerson);

            VBox detailsContainer = getDetailsContainer(patientDetailsPanel);
            assertFalse(detailsContainer.isManaged());
            assertFalse(detailsContainer.isVisible());
        });
    }

    @Test
    public void clearIfShowing_differentPerson_keepsDetails() {
        runOnFxThread(() -> {
            Person displayedPerson = new PersonBuilder().withName("Displayed Patient").build();
            Person otherPerson = new PersonBuilder().withName("Other Patient").build();
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(FIXED_CLOCK);
            patientDetailsPanel.setPerson(displayedPerson);

            patientDetailsPanel.clearIfShowing(otherPerson);

            VBox detailsContainer = getDetailsContainer(patientDetailsPanel);
            assertTrue(detailsContainer.isManaged());
            assertTrue(detailsContainer.isVisible());
            assertEquals("Displayed Patient", getLabel(patientDetailsPanel, "name").getText());
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

            // tags are listed alphabetically regardless of letter case
            patientDetailsPanel.setPerson(new PersonBuilder(person).withTags("Zeta", "alpha", "Beta").build());
            assertEquals("Tags: alpha, Beta, Zeta", getLabel(patientDetailsPanel, "tags").getText());
            patientDetailsPanel.setPerson(person);
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
        });
    }

    @Test
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
            assertFieldVisible(panel, "emergencyContact", false);

            panel.setPerson(patient);
            assertEquals("Emergency Contact: 91234567 (Mary Tan)", contact.getText());
            assertTrue(contact.isVisible());
            assertTrue(contact.isManaged());
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
            Person person = createLongRecord();
            Person originalPerson = new PersonBuilder(person).build();
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(FIXED_CLOCK);

            patientDetailsPanel.setPerson(person);

            Label previousVisitDates = getLabel(patientDetailsPanel, "previousVisitDates");
            assertEquals(LONG_RECORD_PREVIOUS_DATES.reversed(), previousVisitDates.getText().lines().toList());
            assertTrue(previousVisitDates.isWrapText());
            assertRecordCanScrollToBottom(patientDetailsPanel);
            assertEquals(originalPerson, person);
        });
    }

    @Test
    public void keyboardScrolling_overflowingDetails_handlesAllKeysAndBounds() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel(FIXED_CLOCK);
            panel.setPerson(createLongRecord());
            ScrollPane scrollPane = getScrollPane(panel);
            Scene scene = new Scene(scrollPane, 320, 240);
            scene.getStylesheets().add(PatientDetailsPanel.class.getResource("/view/DarkTheme.css").toExternalForm());
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            assertTrue(getDetailsContainer(panel).getHeight() > scrollPane.getViewportBounds().getHeight());
            double top = scrollPane.getVmin();
            double bottom = scrollPane.getVmax();

            fireKeyPressed(scrollPane, KeyCode.UP);
            assertEquals(top, scrollPane.getVvalue());
            fireKeyPressed(scrollPane, KeyCode.DOWN);
            double arrowMovement = getScrollPositionPixels(scrollPane);
            assertTrue(arrowMovement > 0);
            fireKeyPressed(scrollPane, KeyCode.UP);
            assertEquals(top, scrollPane.getVvalue());

            fireKeyPressed(scrollPane, KeyCode.PAGE_DOWN);
            double pageDownMovement = getScrollPositionPixels(scrollPane);
            assertPageMovementIsApproximatelyOneViewport(scrollPane, pageDownMovement);
            assertTrue(pageDownMovement >= arrowMovement * MINIMUM_PAGE_TO_ARROW_RATIO);
            fireKeyPressed(scrollPane, KeyCode.PAGE_UP);
            assertEquals(0, getScrollPositionPixels(scrollPane), 0.001);

            fireKeyPressed(scrollPane, KeyCode.END);
            assertEquals(bottom, scrollPane.getVvalue());
            double bottomPosition = getScrollPositionPixels(scrollPane);
            fireKeyPressed(scrollPane, KeyCode.PAGE_UP);
            double pageUpMovement = bottomPosition - getScrollPositionPixels(scrollPane);
            assertPageMovementIsApproximatelyOneViewport(scrollPane, pageUpMovement);
            fireKeyPressed(scrollPane, KeyCode.DOWN);
            assertTrue(scrollPane.getVvalue() < bottom);
            fireKeyPressed(scrollPane, KeyCode.END);
            fireKeyPressed(scrollPane, KeyCode.DOWN);
            assertEquals(bottom, scrollPane.getVvalue());
            fireKeyPressed(scrollPane, KeyCode.HOME);
            assertEquals(top, scrollPane.getVvalue());
        });
    }

    @Test
    public void keyboardScrolling_emptyOrFittingDetails_isSafe() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel(FIXED_CLOCK);
            ScrollPane scrollPane = getScrollPane(panel);
            Scene scene = new Scene(scrollPane, 320, 240);
            scene.getStylesheets().add(PatientDetailsPanel.class.getResource("/view/DarkTheme.css").toExternalForm());
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            assertScrollingKeysHaveNoEffect(scrollPane);

            PatientDetailsPanel fittingPanel = new PatientDetailsPanel(FIXED_CLOCK);
            ScrollPane fittingScrollPane = getScrollPane(fittingPanel);
            Scene fittingScene = new Scene(fittingScrollPane, 320, 2_000);
            fittingScene.getStylesheets().add(
                    PatientDetailsPanel.class.getResource("/view/DarkTheme.css").toExternalForm());
            fittingPanel.setPerson(new PersonBuilder().build());
            fittingScene.getRoot().applyCss();
            fittingScene.getRoot().layout();
            assertTrue(getDetailsContainer(fittingPanel).getHeight()
                    <= fittingScrollPane.getViewportBounds().getHeight());

            assertScrollingKeysHaveNoEffect(fittingScrollPane);
        });
    }

    @Test
    public void keyboardScrolling_unhandledKeys_reachOtherHandlersWithoutScrolling() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel(FIXED_CLOCK);
            panel.setPerson(createLongRecord());
            ScrollPane scrollPane = getScrollPane(panel);
            Scene scene = new Scene(scrollPane, 320, 240);
            scene.getStylesheets().add(PatientDetailsPanel.class.getResource("/view/DarkTheme.css").toExternalForm());
            scrollPane.applyCss();
            scrollPane.layout();
            fireKeyPressed(scrollPane, KeyCode.DOWN);
            double initialValue = scrollPane.getVvalue();
            assertTrue(initialValue > scrollPane.getVmin());
            List<KeyCode> unhandledKeys = new ArrayList<>();
            scrollPane.addEventHandler(KeyEvent.KEY_PRESSED, event -> unhandledKeys.add(event.getCode()));

            for (KeyCode keyCode : List.of(KeyCode.A, KeyCode.ENTER, KeyCode.F1)) {
                fireKeyPressed(scrollPane, keyCode);
                assertEquals(initialValue, scrollPane.getVvalue());
            }
            assertEquals(List.of(KeyCode.A, KeyCode.ENTER, KeyCode.F1), unhandledKeys);
        });
    }

    @Test
    public void keyboardScrolling_hiddenDetailsOrFixedRange_keepsScrollPosition() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel(FIXED_CLOCK);
            panel.setPerson(createLongRecord());
            ScrollPane scrollPane = getScrollPane(panel);
            Scene scene = new Scene(scrollPane, 320, 240);
            scene.getStylesheets().add(PatientDetailsPanel.class.getResource("/view/DarkTheme.css").toExternalForm());
            scrollPane.applyCss();
            scrollPane.layout();
            assertTrue(getScrollableHeight(scrollPane) > 0);

            getDetailsContainer(panel).setVisible(false);
            assertScrollingKeysHaveNoEffect(scrollPane);

            getDetailsContainer(panel).setVisible(true);
            scrollPane.setVmax(scrollPane.getVmin());
            assertScrollingKeysHaveNoEffect(scrollPane);
        });
    }

    @Test
    public void scrollBar_emptyDetails_isHiddenUntilPatientViewed() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel(FIXED_CLOCK);
            ScrollPane scrollPane = getScrollPane(panel);

            assertEquals(ScrollPane.ScrollBarPolicy.NEVER, scrollPane.getVbarPolicy());
            panel.setPerson(new PersonBuilder().build());
            assertEquals(ScrollPane.ScrollBarPolicy.AS_NEEDED, scrollPane.getVbarPolicy());
            panel.clear();
            assertEquals(ScrollPane.ScrollBarPolicy.NEVER, scrollPane.getVbarPolicy());
        });
    }

    @Test
    public void setPerson_differentPatient_resetsScrollAfterLayout() {
        PatientDetailsPanelFixture fixture = callOnFxThread(PatientDetailsPanelFixture::new);
        try {
            Person shortRecord = new PersonBuilder().withName("Short Record").build();
            runOnFxThread(() -> fixture.setPerson(createLongRecord()));
            waitForNextPulse();
            callOnFxThread(() -> {
                fixture.scrollToBottom();
                assertTrue(fixture.isScrolledAwayFromTop());
                fixture.setPerson(shortRecord);
                return null;
            });
            waitForNextPulse();
            callOnFxThread(() -> {
                fixture.assertAtTop();
                return null;
            });
        } finally {
            runOnFxThread(fixture::close);
        }
    }

    @Test
    public void setPerson_samePatient_resetsScrollAfterLayout() {
        PatientDetailsPanelFixture fixture = callOnFxThread(PatientDetailsPanelFixture::new);
        Person longRecord = createLongRecord();
        try {
            runOnFxThread(() -> fixture.setPerson(longRecord));
            waitForNextPulse();
            callOnFxThread(() -> {
                fixture.scrollToBottom();
                assertTrue(fixture.isScrolledAwayFromTop());
                return null;
            });

            runOnFxThread(() -> fixture.setPerson(longRecord));
            waitForNextPulse();
            callOnFxThread(() -> {
                fixture.assertAtTop();
                return null;
            });
        } finally {
            runOnFxThread(fixture::close);
        }
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
        ScrollPane scrollPane = getScrollPane(patientDetailsPanel);
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

    private static List<String> createLongRecordPreviousDates() {
        LocalDateTime firstVisit = LocalDateTime.of(2025, 1, 1, 9, 30);
        return IntStream.range(0, LONG_RECORD_DATE_COUNT)
                .mapToObj(day -> firstVisit.plusDays(day).format(VisitDate.FORMATTER))
                .toList();
    }

    private static Person createLongRecord() {
        return new PersonBuilder().withPreviousVisitDates(LONG_RECORD_PREVIOUS_DATES.toArray(String[]::new))
                .withNote(LONG_RECORD_NOTE).build();
    }

    private static void assertPageMovementIsApproximatelyOneViewport(ScrollPane scrollPane, double movement) {
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        assertTrue(movement >= viewportHeight * MINIMUM_PAGE_SCROLL_RATIO);
        assertTrue(movement <= viewportHeight * MAXIMUM_PAGE_SCROLL_RATIO);
    }

    private static double getScrollPositionPixels(ScrollPane scrollPane) {
        double scrollableHeight = getScrollableHeight(scrollPane);
        double valueRange = scrollPane.getVmax() - scrollPane.getVmin();
        if (scrollableHeight <= 0 || valueRange <= 0) {
            return 0;
        }
        return (scrollPane.getVvalue() - scrollPane.getVmin()) / valueRange * scrollableHeight;
    }

    private static double getScrollableHeight(ScrollPane scrollPane) {
        return scrollPane.getContent().getBoundsInLocal().getHeight()
                - scrollPane.getViewportBounds().getHeight();
    }

    private static void assertScrollingKeysHaveNoEffect(ScrollPane scrollPane) {
        for (KeyCode keyCode : List.of(KeyCode.UP, KeyCode.DOWN, KeyCode.PAGE_UP, KeyCode.PAGE_DOWN,
                KeyCode.HOME, KeyCode.END)) {
            double previousValue = scrollPane.getVvalue();
            fireKeyPressed(scrollPane, keyCode);
            assertEquals(previousValue, scrollPane.getVvalue(), 0.001, keyCode.toString());
        }
    }

    private static void fireKeyPressed(Node target, KeyCode keyCode) {
        target.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode,
                false, false, false, false));
    }

    private static ScrollPane getScrollPane(PatientDetailsPanel patientDetailsPanel) {
        return (ScrollPane) patientDetailsPanel.getRoot();
    }

    /** Provides a displayed details panel and its stage for reset tests. */
    private static final class PatientDetailsPanelFixture {

        private final Stage stage;
        private final PatientDetailsPanel panel;
        private final ScrollPane scrollPane;

        private PatientDetailsPanelFixture() {
            stage = new Stage();
            panel = new PatientDetailsPanel(FIXED_CLOCK);
            scrollPane = getScrollPane(panel);
            stage.setScene(new Scene(scrollPane, 320, 240));
            stage.getScene().getStylesheets().add(
                    PatientDetailsPanel.class.getResource("/view/DarkTheme.css").toExternalForm());
            stage.show();
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
        }

        private void setPerson(Person person) {
            panel.setPerson(person);
        }

        private void scrollToBottom() {
            scrollPane.setVvalue(scrollPane.getVmax());
        }

        private boolean isScrolledAwayFromTop() {
            return scrollPane.getVvalue() > scrollPane.getVmin();
        }

        private void assertAtTop() {
            assertEquals(scrollPane.getVmin(), scrollPane.getVvalue());
        }

        private void close() {
            stage.close();
        }
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
