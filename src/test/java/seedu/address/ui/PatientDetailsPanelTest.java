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
import java.time.LocalDateTime;
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
