package seedu.address.ui;

import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private static final double SCROLL_EPSILON = 0.5;
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

        personListView.layout();
        ListCell<?> selectedCell = findSelectedCell(selectedIndex);
        if (selectedCell == null) {
            personListView.scrollTo(selectedIndex);
            Platform.runLater(() -> keepSelectedPatientVisible(selectedIndex));
            return;
        }

        Node selectedContent = selectedCell.getGraphic() == null ? selectedCell : selectedCell.getGraphic();
        Bounds viewportBounds = personListView.localToScene(personListView.getBoundsInLocal());
        Bounds selectedBounds = selectedContent.localToScene(selectedContent.getBoundsInLocal());
        double requiredScrollPixels = getRequiredScrollPixels(selectedBounds, viewportBounds);
        if (Math.abs(requiredScrollPixels) <= SCROLL_EPSILON) {
            return;
        }

        ScrollBar verticalScrollBar = findVerticalScrollBar();
        if (verticalScrollBar == null) {
            personListView.scrollTo(selectedIndex);
            return;
        }

        double valueRange = verticalScrollBar.getMax() - verticalScrollBar.getMin();
        double visibleAmount = verticalScrollBar.getVisibleAmount();
        if (valueRange <= 0 || visibleAmount <= 0 || visibleAmount >= valueRange) {
            return;
        }

        // The scrollbar's visible amount uses the same normalized range as its value.
        double scrollableHeight = personListView.getLayoutBounds().getHeight()
                * (valueRange / visibleAmount - 1);
        if (scrollableHeight <= 0) {
            return;
        }
        double targetValue = verticalScrollBar.getValue()
                + requiredScrollPixels / scrollableHeight * valueRange;
        verticalScrollBar.setValue(Math.max(verticalScrollBar.getMin(),
                Math.min(verticalScrollBar.getMax(), targetValue)));
    }

    /** Returns the number of pixels by which the selected card lies outside the viewport. */
    private double getRequiredScrollPixels(Bounds selectedBounds, Bounds viewportBounds) {
        if (selectedBounds.getMaxY() > viewportBounds.getMaxY()) {
            return selectedBounds.getMaxY() - viewportBounds.getMaxY();
        }
        if (selectedBounds.getMinY() < viewportBounds.getMinY()) {
            return selectedBounds.getMinY() - viewportBounds.getMinY();
        }
        return 0;
    }

    /** Finds the cell currently rendering the selected patient, if it is attached to the virtualized list. */
    private ListCell<?> findSelectedCell(int selectedIndex) {
        for (Node node : personListView.lookupAll(".list-cell")) {
            if (node instanceof ListCell<?> cell && cell.getIndex() == selectedIndex) {
                return cell;
            }
        }
        return null;
    }

    /** Returns the vertical scrollbar used by the list view, if it is currently available. */
    private ScrollBar findVerticalScrollBar() {
        for (Node node : personListView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar && scrollBar.getOrientation() == Orientation.VERTICAL) {
                return scrollBar;
            }
        }
        return null;
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
