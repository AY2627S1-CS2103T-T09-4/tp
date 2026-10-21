package seedu.address.ui;

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

    @FXML
    private VBox detailsContainer;

    @FXML
    private Label name;

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
     * Creates an empty {@code PatientDetailsPanel}.
     */
    public PatientDetailsPanel() {
        super(FXML);
    }

    /**
     * Displays all details of the specified person.
     * Shows previous visit dates newest first without changing their stored order.
     *
     * @param person Person whose details should be displayed.
     */
    public void setPerson(Person person) {
        name.setText(person.getName().fullName);
        phone.setText("Phone: " + person.getPhone().value);
        email.setText("Email: " + person.getEmail().value);
        address.setText("Address: " + person.getAddress().value);
        visitDate.setText("Visit date: " + person.getVisitDate());
        previousVisitDates.setText(person.getPreviousVisitDates().isEmpty()
                ? "No previous visit dates"
                : person.getPreviousVisitDates().reversed().stream()
                        .map(VisitDate::toString)
                        .collect(Collectors.joining("\n")));
        note.setText("Note: " + person.getNote().value);
        emergencyContact.setText("Emergency Contact : " + person.getEmergencyContact().toDisplayString());
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
}
