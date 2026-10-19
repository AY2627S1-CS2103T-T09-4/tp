package seedu.address.ui;

import java.util.Comparator;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;

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
     * Displays the specified person's details, hiding empty optional fields and their labels.
     *
     * @param person Person whose details should be displayed.
     */
    public void setPerson(Person person) {
        name.setText(person.getName().fullName);
        phone.setText("Phone: " + person.getPhone().value);
        address.setText("Address: " + person.getAddress().value);
        visitDate.setText("Visit date: " + person.getVisitDate());
        setOptionalField(email, "Email: ", person.getEmail().value);
        setOptionalField(note, "Note: ", person.getNote().value);
        setOptionalField(emergencyContact, "Emergency Contact : ", person.getEmergencyContact().toDisplayString());

        String tagNames = person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .map(tag -> tag.tagName)
                .collect(Collectors.joining(", "));
        setOptionalField(tags, "Tags: ", tagNames);

        detailsContainer.setManaged(true);
        detailsContainer.setVisible(true);
    }

    /**
     * Displays a nonempty optional value with its label, or hides the entire row without reserving layout space.
     */
    private void setOptionalField(Label label, String prefix, String value) {
        boolean hasValue = !value.isEmpty();
        label.setText(hasValue ? prefix + value : "");
        label.setVisible(hasValue);
        label.setManaged(hasValue);
    }
}
