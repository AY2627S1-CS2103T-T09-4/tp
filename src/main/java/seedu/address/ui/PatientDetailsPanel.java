package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;

/**
 * Panel containing all details of a person selected by the view command.
 */
public class PatientDetailsPanel extends UiPart<Region> {

    private static final String FXML = "PatientDetailsPanel.fxml";
    private static final double SMALL_SCROLL_PIXELS = 40.0;
    private static final double PAGE_SCROLL_RATIO = 0.9;

    private final Clock clock;
    private Person displayedPerson;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox detailsContainer;

    @FXML
    private Label name;

    @FXML
    private Label dateOfBirth;

    @FXML
    private Label bloodType;

    @FXML
    private Label allergies;

    @FXML
    private Label medicalPlaceholder;

    @FXML
    private Label alcoholHistory;

    @FXML
    private Label smokingStatus;

    @FXML
    private Label phone;

    @FXML
    private Label email;

    @FXML
    private Label address;

    @FXML
    private Label visitDate;

    @FXML
    private Label previousVisitDates;

    @FXML
    private Label note;

    @FXML
    private Label emergencyContact;

    @FXML
    private Label tags;

    /**
     * Creates an empty {@code PatientDetailsPanel} using the computer's local clock for calendar labels.
     */
    public PatientDetailsPanel() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates an empty panel using the supplied clock to determine the calendar day when details are rendered.
     *
     * @throws NullPointerException If {@code clock} is null.
     */
    PatientDetailsPanel(Clock clock) {
        super(FXML);
        this.clock = requireNonNull(clock);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    /**
     * Displays all details of the specified person.
     * Labels the current visit Past, Today, or Upcoming by calendar date at the time of rendering.
     * Shows previous visit dates newest first without changing their stored order.
     *
     * @param person Person whose details should be displayed.
     * @throws NullPointerException If {@code person} is null.
     */
    public void setPerson(Person person) {
        requireNonNull(person);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        name.setText(person.getName().fullName);
        setMedicalDetails(person);
        setContactDetails(person);
        setVisitDetails(person);
        setAdditionalDetails(person);

        detailsContainer.setManaged(true);
        detailsContainer.setVisible(true);
        displayedPerson = person;
        resetScrollPositionAfterLayout();
    }

    /** Hides the displayed patient details. */
    public void clear() {
        displayedPerson = null;
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        detailsContainer.setManaged(false);
        detailsContainer.setVisible(false);
    }

    /**
     * Hides the details only when they belong to {@code person}.
     */
    public void clearIfShowing(Person person) {
        requireNonNull(person);
        if (person.equals(displayedPerson)) {
            clear();
        }
    }

    /**
     * Displays recorded medical fields or the empty medical profile placeholder.
     */
    private void setMedicalDetails(Person person) {
        boolean hasDateOfBirth = !person.getDateOfBirth().isEmpty();
        setOptionalLabel(dateOfBirth,
                hasDateOfBirth ? "Date of birth: " + person.getDateOfBirth().toDisplayString() : "",
                hasDateOfBirth);
        boolean hasBloodType = !person.getBloodType().isEmpty();
        setOptionalLabel(bloodType, hasBloodType ? "Blood type: " + person.getBloodType().toString() : "",
                hasBloodType);
        boolean hasAllergies = !person.getAllergies().isEmpty();
        setOptionalLabel(allergies, hasAllergies ? "Allergies: " + person.getAllergies() : "", hasAllergies);
        boolean hasAlcoholHistory = !person.getAlcoholHistory().isEmpty();
        setOptionalLabel(alcoholHistory, hasAlcoholHistory ? "Alcohol history: " + person.getAlcoholHistory() : "",
                hasAlcoholHistory);
        boolean hasSmokingStatus = !person.getSmokingStatus().isEmpty();
        setOptionalLabel(smokingStatus, hasSmokingStatus ? "Smoking status: " + person.getSmokingStatus() : "",
                hasSmokingStatus);
        boolean hasMedicalInformation = hasDateOfBirth || hasBloodType || hasAllergies
                || hasAlcoholHistory || hasSmokingStatus;
        medicalPlaceholder.setVisible(!hasMedicalInformation);
        medicalPlaceholder.setManaged(!hasMedicalInformation);
    }

    /**
     * Displays contact information, hiding optional fields when absent.
     */
    private void setContactDetails(Person person) {
        phone.setText("Phone: " + person.getPhone().value);
        boolean hasEmail = !person.getEmail().value.isEmpty();
        setOptionalLabel(email, hasEmail ? "Email: " + person.getEmail().value : "", hasEmail);
        address.setText("Address: " + person.getAddress().value);
        boolean hasEmergencyContact = !person.getEmergencyContact().isEmpty();
        setOptionalLabel(emergencyContact,
                hasEmergencyContact ? "Emergency Contact: " + person.getEmergencyContact().toDisplayString() : "",
                hasEmergencyContact);
    }

    /**
     * Displays the current visit and previous visit history.
     */
    private void setVisitDetails(Person person) {
        visitDate.setText("Visit date: " + person.getVisitDate()
                + " (" + getVisitDateLabel(person.getVisitDate()) + ")");
        previousVisitDates.setText(formatPreviousVisitDates(person));
    }

    /**
     * Displays the optional note and sorted tags.
     */
    private void setAdditionalDetails(Person person) {
        boolean hasNote = !person.getNote().value.isEmpty();
        setOptionalLabel(note, hasNote ? "Note: " + person.getNote().value : "", hasNote);

        String tagNames = person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName, String.CASE_INSENSITIVE_ORDER))
                .map(tag -> tag.tagName)
                .collect(Collectors.joining(", "));
        tags.setText("Tags: " + tagNames);
    }

    /**
     * Sets an optional label's text and keeps its visibility and layout participation in sync.
     */
    private void setOptionalLabel(Label label, String text, boolean hasValue) {
        label.setText(text);
        label.setVisible(hasValue);
        label.setManaged(hasValue);
    }

    /**
     * Formats previous visit dates newest first without changing the patient's stored order.
     */
    private String formatPreviousVisitDates(Person person) {
        if (person.getPreviousVisitDates().isEmpty()) {
            return "No previous visit dates";
        }
        return person.getPreviousVisitDates().reversed().stream()
                .map(VisitDate::toString)
                .collect(Collectors.joining("\n"));
    }

    /** Handles scrolling keys while the details panel owns keyboard focus. */
    private void handleKeyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case UP -> scrollByPixels(-SMALL_SCROLL_PIXELS);
            case DOWN -> scrollByPixels(SMALL_SCROLL_PIXELS);
            case PAGE_UP -> scrollByPixels(-getPageScrollPixels());
            case PAGE_DOWN -> scrollByPixels(getPageScrollPixels());
            case HOME -> scrollToBoundary(scrollPane.getVmin());
            case END -> scrollToBoundary(scrollPane.getVmax());
            default -> {
                return;
            }
        }
        keyEvent.consume();
    }

    /** Scrolls the panel by the given number of content pixels within its valid bounds. */
    private void scrollByPixels(double pixels) {
        if (!canScroll()) {
            return;
        }

        double scrollableHeight = getScrollableHeight();
        double valueRange = scrollPane.getVmax() - scrollPane.getVmin();
        double currentPixels = (scrollPane.getVvalue() - scrollPane.getVmin()) / valueRange * scrollableHeight;
        double targetPixels = Math.max(0, Math.min(scrollableHeight, currentPixels + pixels));
        double targetValue = scrollPane.getVmin() + targetPixels / scrollableHeight * valueRange;
        scrollPane.setVvalue(targetValue);
    }

    /** Moves to a boundary only when the panel has content that can scroll. */
    private void scrollToBoundary(double value) {
        if (canScroll()) {
            scrollPane.setVvalue(value);
        }
    }

    /** Returns true when the details content exceeds the available viewport. */
    private boolean canScroll() {
        return detailsContainer.isManaged() && detailsContainer.isVisible()
                && getScrollableHeight() > 0 && scrollPane.getVmax() > scrollPane.getVmin();
    }

    /** Returns the number of content pixels outside the details viewport. */
    private double getScrollableHeight() {
        return scrollPane.getContent().getBoundsInLocal().getHeight()
                - scrollPane.getViewportBounds().getHeight();
    }

    /** Returns the number of pixels used by one page scroll. */
    private double getPageScrollPixels() {
        return scrollPane.getViewportBounds().getHeight() * PAGE_SCROLL_RATIO;
    }

    /** Resets the scroll position immediately and again after the updated content is laid out. */
    private void resetScrollPositionAfterLayout() {
        scrollPane.setVvalue(scrollPane.getVmin());
        if (scrollPane.getScene() == null) {
            return;
        }

        Platform.runLater(() -> {
            scrollPane.applyCss();
            scrollPane.layout();
            scrollPane.setVvalue(scrollPane.getVmin());
        });
    }

    /**
     * Returns a calendar-day label using one clock read, independent of the appointment's time of day.
     */
    private String getVisitDateLabel(VisitDate currentVisitDate) {
        LocalDate today = LocalDate.now(clock);
        LocalDate visitDay = currentVisitDate.value.toLocalDate();
        if (visitDay.isBefore(today)) {
            return "Past";
        }
        if (visitDay.isAfter(today)) {
            return "Upcoming";
        }
        return "Today";
    }
}
