package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;

/**
 * Panel containing all details of a person selected by the view command.
 */
public class PatientDetailsPanel extends UiPart<Region> {

    private static final String FXML = "PatientDetailsPanel.fxml";

    private final Clock clock;
    private Person displayedPerson;

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
        name.setText(person.getName().fullName);
        setMedicalDetails(person);
        setContactDetails(person);
        setVisitDetails(person);
        setAdditionalDetails(person);

        detailsContainer.setManaged(true);
        detailsContainer.setVisible(true);
        displayedPerson = person;
    }

    /** Hides the displayed patient details. */
    public void clear() {
        displayedPerson = null;
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
