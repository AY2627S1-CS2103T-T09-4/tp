package seedu.address.ui;

import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldIndex, newIndex) ->
                scrollToSelectedPatient(newIndex.intValue()));
    }

    /** Keeps the selected patient card visible after the selected cell has been laid out. */
    private void scrollToSelectedPatient(int selectedIndex) {
        if (selectedIndex < 0) {
            return;
        }

        Platform.runLater(() -> keepSelectedPatientVisible(selectedIndex));
    }

    /** Adjusts the list only when the selected patient card is clipped by the viewport. */
    private void keepSelectedPatientVisible(int selectedIndex) {
        if (personListView.getScene() == null
                || personListView.getSelectionModel().getSelectedIndex() != selectedIndex) {
            return;
        }

        personListView.applyCss();
        personListView.layout();
        if (personListView.lookup(".virtual-flow") instanceof VirtualFlow<?> flow) {
            // Unlike ListView.scrollTo, this keeps fully visible cells in their current position.
            flow.scrollTo(selectedIndex);
        }
    }

    /** Returns the list view used as the patient-list focus stop. */
    Node getFocusTarget() {
        return personListView;
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1).getRoot());
            }
        }
    }

}
