package seedu.address.ui;

import java.time.LocalDate;
import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final String PAST_VISIT_STYLE_CLASS = "past-visit-card";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label bloodType;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private Label visitDate;
    @FXML
    private Label note;
    @FXML
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        this(person, displayedIndex, LocalDate.now());
    }

    /**
     * Creates a {@code PersonCard} using {@code currentDate} to determine whether the visit is in the past.
     */
    PersonCard(Person person, int displayedIndex, LocalDate currentDate) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        boolean hasBloodType = !person.getBloodType().isEmpty();
        bloodType.setText(hasBloodType ? "Blood type: " + person.getBloodType().toString() : "");
        bloodType.setVisible(hasBloodType);
        bloodType.setManaged(hasBloodType);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);
        visitDate.setText(person.getVisitDate().toString());
        note.setText(person.getNote().value);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));

        if (person.getVisitDate().value.toLocalDate().isBefore(currentDate)) {
            cardPane.getStyleClass().add(PAST_VISIT_STYLE_CLASS);
        }
    }
}
