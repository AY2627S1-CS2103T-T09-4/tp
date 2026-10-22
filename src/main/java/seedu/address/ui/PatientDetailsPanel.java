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

    @FXML
    private VBox detailsContainer;

    @FXML
    private Label name;

    @FXML
    private Label dateOfBirth;

    @FXML
    private Label bloodType;

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
        boolean hasDateOfBirth = !person.getDateOfBirth().isEmpty();
        dateOfBirth.setText(hasDateOfBirth ? "Date of birth: " + person.getDateOfBirth().toDisplayString() : "");
        dateOfBirth.setVisible(hasDateOfBirth);
        dateOfBirth.setManaged(hasDateOfBirth);
        boolean hasBloodType = !person.getBloodType().isEmpty();
        bloodType.setText(hasBloodType ? "Blood type: " + person.getBloodType().toString() : "");
        bloodType.setVisible(hasBloodType);
        bloodType.setManaged(hasBloodType);
        phone.setText("Phone: " + person.getPhone().value);
        email.setText("Email: " + person.getEmail().value);
        address.setText("Address: " + person.getAddress().value);
        visitDate.setText("Visit date: " + person.getVisitDate()
                + " (" + getVisitDateLabel(person.getVisitDate()) + ")");
        previousVisitDates.setText(person.getPreviousVisitDates().isEmpty()
                ? "No previous visit dates"
                : person.getPreviousVisitDates().reversed().stream()
                        .map(VisitDate::toString)
                        .collect(Collectors.joining("\n")));
        note.setText("Note: " + person.getNote().value);
        emergencyContact.setText("Emergency Contact: " + person.getEmergencyContact().toDisplayString());
        boolean hasEmergencyContact = !person.getEmergencyContact().isEmpty();
        emergencyContact.setVisible(hasEmergencyContact);
        emergencyContact.setManaged(hasEmergencyContact);

        String tagNames = person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .map(tag -> tag.tagName)
                .collect(Collectors.joining(", "));
        tags.setText("Tags: " + tagNames);

        detailsContainer.setManaged(true);
        detailsContainer.setVisible(true);
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
