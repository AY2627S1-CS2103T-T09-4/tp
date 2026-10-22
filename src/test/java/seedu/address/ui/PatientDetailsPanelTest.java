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
import seedu.address.testutil.PersonBuilder;

public class PatientDetailsPanelTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-21T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));

    @BeforeAll
    public static void setUpJavaFx() throws InterruptedException {
        startJavaFx();
    }

    @Test
    public void setPerson_allergies_displaysBeforeAlcoholAndSmokingAndClearsStaleText() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel();
            Person recorded = new PersonBuilder().withBloodType("A+").withAllergies("Penicillin; latex")
                    .withAlcoholHistory("Does not drink").withSmokingStatus("former").build();
            panel.setPerson(recorded);
            Label allergies = getLabel(panel, "allergies");
            assertEquals("Allergies: Penicillin; latex", allergies.getText());
            assertFieldVisible(panel, "allergies", true);
            assertTrue(allergies.isWrapText());
            VBox container = getDetailsContainer(panel);
            int allergyIndex = container.getChildren().indexOf(allergies);
            assertEquals(container.getChildren().indexOf(getLabel(panel, "bloodType")) + 1, allergyIndex);
            assertEquals(allergyIndex + 1, container.getChildren().indexOf(getLabel(panel, "alcoholHistory")));
            assertEquals(allergyIndex + 2, container.getChildren().indexOf(getLabel(panel, "smokingStatus")));

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
    public void setPerson_smokingStatus_displaysAfterAlcoholHistoryAndHidesWhenCleared() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel();
            Person recorded = new PersonBuilder().withAlcoholHistory("Does not drink")
                    .withSmokingStatus("former").build();
            panel.setPerson(recorded);
            Label status = getLabel(panel, "smokingStatus");
            assertEquals("Smoking status: former", status.getText());
            assertFieldVisible(panel, "smokingStatus", true);
            assertTrue(status.isWrapText());
            VBox container = getDetailsContainer(panel);
            assertEquals(container.getChildren().indexOf(getLabel(panel, "alcoholHistory")) + 1,
                    container.getChildren().indexOf(status));

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
    public void setPerson_alcoholHistory_displaysAfterAllergiesAndClearsStaleText() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel();
            Person recorded = new PersonBuilder().withBloodType("A+").withAllergies("Penicillin")
                    .withAlcoholHistory("Occasional drinking").build();
            panel.setPerson(recorded);
            Label history = getLabel(panel, "alcoholHistory");
            assertEquals("Alcohol history: Occasional drinking", history.getText());
            assertFieldVisible(panel, "alcoholHistory", true);
            assertTrue(history.isWrapText());
            VBox container = getDetailsContainer(panel);
            assertEquals(container.getChildren().indexOf(getLabel(panel, "allergies")) + 1,
                    container.getChildren().indexOf(history));

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
    public void setPerson_dateOfBirth_displaysAgeBelowNameAndHidesWhenAbsent() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel();
            panel.setPerson(new PersonBuilder().withDateOfBirth("15/6/1990").build());
            Label birthDate = getLabel(panel, "dateOfBirth");
            int age = Period.between(LocalDate.of(1990, 6, 15), LocalDate.now()).getYears();
            assertEquals("Date of birth: 15/6/1990 (" + age + " years old)", birthDate.getText());
            assertFieldVisible(panel, "dateOfBirth", true);
            assertTrue(birthDate.isWrapText());
            VBox container = getDetailsContainer(panel);
            assertEquals(container.getChildren().indexOf(getLabel(panel, "name")) + 1,
                    container.getChildren().indexOf(birthDate));

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
            assertEquals("Previous visit dates:", getLabel(patientDetailsPanel, "previousVisitDatesHeader").getText());
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
    public void setPerson_bloodType_displaysBelowDateOfBirthAndHidesWhenAbsent() {
        runOnFxThread(() -> {
            PatientDetailsPanel panel = new PatientDetailsPanel();
            panel.setPerson(new PersonBuilder().withDateOfBirth("15/6/1990").withBloodType("A+").build());
            Label bloodTypeLabel = getLabel(panel, "bloodType");
            assertEquals("Blood type: A+", bloodTypeLabel.getText());
            assertFieldVisible(panel, "bloodType", true);
            assertTrue(bloodTypeLabel.isWrapText());
            VBox container = getDetailsContainer(panel);
            assertEquals(container.getChildren().indexOf(getLabel(panel, "dateOfBirth")) + 1,
                    container.getChildren().indexOf(bloodTypeLabel));

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
            VBox container = getDetailsContainer(panel);
            assertEquals(container.getChildren().indexOf(getLabel(panel, "phone")) + 1,
                    container.getChildren().indexOf(contact));

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

            List<Node> details = getDetailsContainer(patientDetailsPanel).getChildren();
            int visitDateIndex = details.indexOf(getLabel(patientDetailsPanel, "visitDate"));
            assertEquals(getLabel(patientDetailsPanel, "previousVisitDatesHeader"), details.get(visitDateIndex + 1));
            assertEquals(getLabel(patientDetailsPanel, "previousVisitDates"), details.get(visitDateIndex + 2));
            assertEquals(getLabel(patientDetailsPanel, "note"), details.get(visitDateIndex + 3));
        });
    }

    @Test
    public void setPerson_differentPatients_replacesPreviousDatesAndEmptyState() {
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
        runOnFxThread(() -> {
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(FIXED_CLOCK);
            Person person = new PersonBuilder().withVisitDate("20/10/2026 2359").build();

            patientDetailsPanel.setPerson(person);

            assertEquals("Visit date: 20/10/2026 2359 (Past)", getLabel(patientDetailsPanel, "visitDate").getText());
        });
    }

    @Test
    public void setPerson_visitTodayAtAnyTime_displaysToday() {
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
        runOnFxThread(() -> {
            Clock beforeMidnight = Clock.fixed(Instant.parse("2026-12-31T15:59:59.999999999Z"), FIXED_CLOCK.getZone());
            CountingClock clock = new CountingClock(beforeMidnight);
            PatientDetailsPanel patientDetailsPanel = new PatientDetailsPanel(clock);
            Person person = new PersonBuilder().withVisitDate("31/12/2026 0000").build();
            assertEquals(0, clock.readCount);

            patientDetailsPanel.setPerson(person);
            assertEquals("Visit date: 31/12/2026 0000 (Today)", getLabel(patientDetailsPanel, "visitDate").getText());
            assertEquals(1, clock.readCount);

            clock.delegate = Clock.offset(beforeMidnight, Duration.ofNanos(1));
            assertEquals("Visit date: 31/12/2026 0000 (Today)", getLabel(patientDetailsPanel, "visitDate").getText());
            patientDetailsPanel.setPerson(person);

            assertEquals("Visit date: 31/12/2026 0000 (Past)", getLabel(patientDetailsPanel, "visitDate").getText());
            assertEquals(2, clock.readCount);
        });
    }

    @Test
    public void constructorAndSetPerson_nullArguments_throwsNullPointerException() {
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
        Label tags = getLabel(patientDetailsPanel, "tags");
        Bounds tagsBounds = tags.localToScene(tags.getBoundsInLocal());
        assertTrue(tagsBounds.getMaxY() <= viewportBounds.getMaxY());
        assertTrue(tagsBounds.getMinY() >= viewportBounds.getMinY());
    }

    private static VBox getDetailsContainer(PatientDetailsPanel patientDetailsPanel) {
        ScrollPane scrollPane = (ScrollPane) patientDetailsPanel.getRoot();
        VBox detailsContainer = (VBox) scrollPane.getContent();
        assertNotNull(detailsContainer);
        return detailsContainer;
    }

    private static Label getLabel(PatientDetailsPanel patientDetailsPanel, String labelId) {
        Label label = (Label) getDetailsContainer(patientDetailsPanel).lookup("#" + labelId);
        assertNotNull(label);
        return label;
    }

    /**
     * Counts calendar-label clock reads and permits deterministic advancement across midnight.
     */
    private static class CountingClock extends Clock {
        private Clock delegate;
        private int readCount;

        CountingClock(Clock delegate) {
            this.delegate = delegate;
        }

        @Override
        public ZoneId getZone() {
            return delegate.getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new CountingClock(delegate.withZone(zone));
        }

        /**
         * Returns the delegated instant and counts this read.
         */
        @Override
        public Instant instant() {
            readCount++;
            return delegate.instant();
        }
    }
}
