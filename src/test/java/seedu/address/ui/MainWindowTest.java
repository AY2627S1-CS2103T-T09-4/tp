package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.ui.JavaFxTestUtil.callOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;
import static seedu.address.ui.JavaFxTestUtil.waitForNextPulse;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MainWindowTest {

    private static final Path TEST_DATA_FILE_PATH = Path.of("data", "test-addressbook.json");
    private static final double FOCUS_BORDER_WIDTH = 2.0;
    private static final Color FOCUS_BORDER_COLOR = Color.web("#909090");
    private static final PseudoClass KEYBOARD_FOCUSED_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("keyboard-focused");

    @BeforeAll
    public static void setUpJavaFx() throws InterruptedException {
        startJavaFx();
    }

    @Test
    public void executeCommand_viewCommand_displaysPersonInDetailsPanel() {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Alex Yeoh").build();
            TestLogic logic = new TestLogic(person);
            Stage stage = new Stage();

            try {
                MainWindow mainWindow = new MainWindow(stage, logic, TEST_DATA_FILE_PATH, url -> { });
                mainWindow.fillInnerParts();

                try {
                    mainWindow.executeCommand("view 1");
                } catch (CommandException | ParseException e) {
                    throw new AssertionError("View command should execute successfully", e);
                }

                assertEquals("view 1", logic.getLastCommandText());
                PatientDetailsPanel patientDetailsPanel = mainWindow.getPatientDetailsPanel();
                assertNotNull(patientDetailsPanel);
                ScrollPane detailsPanel = (ScrollPane) patientDetailsPanel.getRoot();
                VBox detailsContainer = (VBox) detailsPanel.getContent();
                Label name = (Label) detailsContainer.lookup("#name");
                assertNotNull(name);
                assertEquals("Alex Yeoh", name.getText());
                assertTrue(detailsContainer.isVisible());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void keyboardNavigation_emptyDetails_cyclesForwardAndReverse() {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Keyboard Patient").build();
            TestLogic logic = new TestLogic(person, FXCollections.observableArrayList());
            Stage stage = new Stage();

            try {
                MainWindow mainWindow = new MainWindow(stage, logic, TEST_DATA_FILE_PATH, url -> { });
                mainWindow.fillInnerParts();
                mainWindow.show();
                Scene scene = stage.getScene();
                scene.getRoot().applyCss();
                scene.getRoot().layout();

                TextField commandTextField = (TextField) scene.lookup("#commandTextField");
                TextArea resultDisplay = (TextArea) scene.lookup("#resultDisplay");
                ListView<?> personListView = (ListView<?>) scene.lookup("#personListView");
                ScrollPane detailsPanel = (ScrollPane) mainWindow.getPatientDetailsPanel().getRoot();
                List<Node> focusSections = getFocusSections(scene);

                commandTextField.requestFocus();
                assertSame(commandTextField, scene.getFocusOwner());
                assertFocusIndicator(scene, focusSections, 0);
                fireTab(scene, false);
                assertSame(resultDisplay, scene.getFocusOwner());
                assertFocusIndicator(scene, focusSections, 1);
                fireTab(scene, false);
                assertSame(personListView, scene.getFocusOwner());
                assertFocusIndicator(scene, focusSections, 2);
                fireTab(scene, false);
                assertSame(detailsPanel, scene.getFocusOwner());
                assertFocusIndicator(scene, focusSections, 3);
                fireTab(scene, false);
                assertSame(commandTextField, scene.getFocusOwner());
                assertFocusIndicator(scene, focusSections, 0);

                fireTab(scene, true);
                assertSame(detailsPanel, scene.getFocusOwner());
                assertFocusIndicator(scene, focusSections, 3);
                fireTab(scene, true);
                assertSame(personListView, scene.getFocusOwner());
                assertFocusIndicator(scene, focusSections, 2);
                fireTab(scene, true);
                assertSame(resultDisplay, scene.getFocusOwner());
                assertFocusIndicator(scene, focusSections, 1);
                fireTab(scene, true);
                assertSame(commandTextField, scene.getFocusOwner());
                assertFocusIndicator(scene, focusSections, 0);

                logic.setExecutionOutcome(ExecutionOutcome.CLEAR_DETAILS);
                assertDoesNotThrow(() -> mainWindow.executeCommand("clear"));
                commandTextField.requestFocus();
                fireTab(scene, false);
                fireTab(scene, false);
                fireTab(scene, false);
                assertSame(detailsPanel, scene.getFocusOwner());
                assertFocusIndicator(scene, focusSections, 3);
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void focusIndicators_preserveCommandErrorAndPatientSelection() {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Selected Patient").build();
            ObservableList<Person> persons = FXCollections.observableArrayList(person);
            TestLogic logic = new TestLogic(person, ExecutionOutcome.COMMAND_ERROR, persons);
            Stage stage = new Stage();

            try {
                MainWindow mainWindow = new MainWindow(stage, logic, TEST_DATA_FILE_PATH, url -> { });
                mainWindow.fillInnerParts();
                mainWindow.show();
                Scene scene = stage.getScene();
                scene.getRoot().applyCss();
                scene.getRoot().layout();

                List<Node> focusSections = getFocusSections(scene);
                TextField commandTextField = getCommandTextField(scene);
                ListView<?> personListView = (ListView<?>) scene.lookup("#personListView");

                commandTextField.setText("invalid command");
                commandTextField.getOnAction().handle(new ActionEvent(commandTextField, commandTextField));
                assertTrue(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                commandTextField.requestFocus();
                assertFocusIndicator(scene, focusSections, 0);

                personListView.getSelectionModel().select(0);
                personListView.requestFocus();
                assertEquals(0, personListView.getSelectionModel().getSelectedIndex());
                assertTrue(getSelectedListCell(personListView).getPseudoClassStates()
                        .contains(PseudoClass.getPseudoClass("selected")));
                assertFocusIndicator(scene, focusSections, 2);
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void patientList_arrowNavigation_keepsSelectedPatientVisibleAndStable() {
        ObservableList<Person> persons = FXCollections.observableArrayList();
        persons.add(new PersonBuilder().withName("Patient 1").build());
        for (int i = 2; i <= 4; i++) {
            persons.add(new PersonBuilder().withName("Patient " + i)
                    .withNote("A long note that makes this patient card tall. ".repeat(10)).build());
        }
        Stage[] stageHolder = new Stage[1];
        ListView<?>[] personListViewHolder = new ListView<?>[1];

        runOnFxThread(() -> {
            TestLogic logic = new TestLogic(persons.get(0), persons);
            Stage stage = new Stage();
            stageHolder[0] = stage;
            MainWindow mainWindow = new MainWindow(stage, logic, TEST_DATA_FILE_PATH, url -> { });
            mainWindow.fillInnerParts();
            mainWindow.show();
            Scene scene = stage.getScene();
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            ListView<?> personListView = (ListView<?>) scene.lookup("#personListView");
            personListViewHolder[0] = personListView;
            personListView.getSelectionModel().select(0);
            personListView.requestFocus();
        });

        for (int i = 0; i < persons.size() - 1; i++) {
            runOnFxThread(() -> fireKeyPressed(personListViewHolder[0], KeyCode.DOWN));
            waitForNextPulse();
            runOnFxThread(() -> assertSelectedPatientFullyVisible(personListViewHolder[0]));
        }

        double scrollValueAtLastPatient = callOnFxThread(() ->
                getVerticalScrollBar(personListViewHolder[0]).getValue());
        runOnFxThread(() -> fireKeyPressed(personListViewHolder[0], KeyCode.UP));
        waitForNextPulse();
        runOnFxThread(() -> {
            ListView<?> personListView = personListViewHolder[0];
            assertEquals(persons.size() - 2, personListView.getSelectionModel().getSelectedIndex());
            assertSelectedPatientFullyVisible(personListView);
            assertEquals(scrollValueAtLastPatient, getVerticalScrollBar(personListView).getValue(), 1.0e-6,
                    "Selecting a fully visible patient should not move the list");
            stageHolder[0].close();
        });
    }

    @Test
    public void commandEntry_view_keepsFocusAndResetsDetails() {
        Person longRecord = new PersonBuilder().withName("Long Record")
                .withNote("A long note that should overflow the details panel. ".repeat(200)).build();
        Person shortRecord = new PersonBuilder().withName("Short Record").build();
        MainWindowFixture fixture = callOnFxThread(() -> new MainWindowFixture(longRecord));

        try {
            runOnFxThread(fixture::submitView);
            waitForNextPulse();
            runOnFxThread(() -> {
                fixture.assertCommandFocus();
                fixture.assertDetailsAtTop();
                fixture.scrollToBottom();
                fixture.setPersonToView(shortRecord);
                fixture.submitView();
            });
            waitForNextPulse();
            runOnFxThread(() -> {
                fixture.assertCommandFocus();
                fixture.assertDetailsAtTop();
                fixture.assertDisplayedName("Short Record");
                fixture.setPersonToView(longRecord);
                fixture.submitView();
            });
            waitForNextPulse();
            runOnFxThread(() -> {
                fixture.assertCommandFocus();
                fixture.assertDetailsAtTop();
            });
        } finally {
            runOnFxThread(fixture::close);
        }
    }

    @Test
    public void executeCommand_duplicateError_displaysPersonInDetailsPanel() {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Duplicate Patient").build();
            TestLogic logic = new TestLogic(person, ExecutionOutcome.COMMAND_ERROR_WITH_PREVIEW);
            Stage stage = new Stage();

            try {
                MainWindow mainWindow = new MainWindow(stage, logic, TEST_DATA_FILE_PATH, url -> { });
                mainWindow.fillInnerParts();

                CommandException exception = assertThrows(
                        CommandException.class, () -> mainWindow.executeCommand("add duplicate"));

                assertEquals("Duplicate patient", exception.getMessage());
                assertEquals(person, exception.getPersonToView().orElseThrow());
                PatientDetailsPanel patientDetailsPanel = mainWindow.getPatientDetailsPanel();
                ScrollPane detailsPanel = (ScrollPane) patientDetailsPanel.getRoot();
                VBox detailsContainer = (VBox) detailsPanel.getContent();
                Label name = (Label) detailsContainer.lookup("#name");
                assertEquals("Duplicate Patient", name.getText());
                assertTrue(detailsContainer.isVisible());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void executeCommand_commandErrorWithoutPreview_keepsExistingDetails() {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Existing Patient")
                    .withVisitDate("2/1/2026 1000").withPreviousVisitDates("1/1/2026 1000").build();
            TestLogic logic = new TestLogic(person);
            Stage stage = new Stage();

            try {
                MainWindow mainWindow = new MainWindow(stage, logic, TEST_DATA_FILE_PATH, url -> { });
                mainWindow.fillInnerParts();
                assertDoesNotThrow(() -> mainWindow.executeCommand("view 1"));
                VBox detailsContainer = getDetailsContainer(mainWindow);
                List<String> existingDetails = getDisplayedDetails(detailsContainer);

                logic.setExecutionOutcome(ExecutionOutcome.COMMAND_ERROR);
                CommandException exception = assertThrows(
                        CommandException.class, () -> mainWindow.executeCommand("invalid command"));

                assertEquals("Command error", exception.getMessage());
                assertTrue(detailsContainer.isVisible());
                assertEquals(existingDetails, getDisplayedDetails(detailsContainer));
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void executeCommand_parseError_keepsExistingDetails() {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Existing Patient")
                    .withVisitDate("2/1/2026 1000").withPreviousVisitDates("1/1/2026 1000").build();
            TestLogic logic = new TestLogic(person);
            Stage stage = new Stage();

            try {
                MainWindow mainWindow = new MainWindow(stage, logic, TEST_DATA_FILE_PATH, url -> { });
                mainWindow.fillInnerParts();
                assertDoesNotThrow(() -> mainWindow.executeCommand("view 1"));
                VBox detailsContainer = getDetailsContainer(mainWindow);
                List<String> existingDetails = getDisplayedDetails(detailsContainer);

                logic.setExecutionOutcome(ExecutionOutcome.PARSE_ERROR);
                ParseException exception = assertThrows(
                        ParseException.class, () -> mainWindow.executeCommand("invalid syntax"));

                assertEquals("Parse error", exception.getMessage());
                assertTrue(detailsContainer.isVisible());
                assertEquals(existingDetails, getDisplayedDetails(detailsContainer));
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void executeCommand_clearDetailsAction_hidesExistingDetails() {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Existing Patient").build();
            TestLogic logic = new TestLogic(person);
            Stage stage = new Stage();

            try {
                MainWindow mainWindow = new MainWindow(stage, logic, TEST_DATA_FILE_PATH, url -> { });
                mainWindow.fillInnerParts();
                assertDoesNotThrow(() -> mainWindow.executeCommand("view 1"));

                logic.setExecutionOutcome(ExecutionOutcome.CLEAR_DETAILS);
                assertDoesNotThrow(() -> mainWindow.executeCommand("clear"));

                VBox detailsContainer = getDetailsContainer(mainWindow);
                assertFalse(detailsContainer.isManaged());
                assertFalse(detailsContainer.isVisible());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void executeCommand_clearMatchingDetailsAction_hidesOnlyMatchingPerson() {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Existing Patient").build();
            TestLogic logic = new TestLogic(person);
            Stage stage = new Stage();

            try {
                MainWindow mainWindow = new MainWindow(stage, logic, TEST_DATA_FILE_PATH, url -> { });
                mainWindow.fillInnerParts();
                assertDoesNotThrow(() -> mainWindow.executeCommand("view 1"));
                VBox detailsContainer = getDetailsContainer(mainWindow);

                logic.setExecutionOutcome(ExecutionOutcome.CLEAR_OTHER_PERSON);
                assertDoesNotThrow(() -> mainWindow.executeCommand("delete 2"));
                assertTrue(detailsContainer.isVisible());
                assertEquals("Existing Patient", getLabelText(detailsContainer, "#name"));

                logic.setExecutionOutcome(ExecutionOutcome.CLEAR_MATCHING_PERSON);
                assertDoesNotThrow(() -> mainWindow.executeCommand("delete 1"));
                assertFalse(detailsContainer.isManaged());
                assertFalse(detailsContainer.isVisible());
            } finally {
                stage.close();
            }
        });
    }

    private static VBox getDetailsContainer(MainWindow mainWindow) {
        ScrollPane detailsPanel = (ScrollPane) mainWindow.getPatientDetailsPanel().getRoot();
        return (VBox) detailsPanel.getContent();
    }

    private static ScrollPane getDetailsPanel(MainWindow mainWindow) {
        return (ScrollPane) mainWindow.getPatientDetailsPanel().getRoot();
    }

    private static List<Node> getFocusSections(Scene scene) {
        return List.of(
                getNode(scene, "#commandBoxPlaceholder"),
                getNode(scene, "#resultDisplayPlaceholder"),
                getNode(scene, "#personList"),
                getNode(scene, "#patientDetailsPanelPlaceholder"));
    }

    private static Node getNode(Scene scene, String selector) {
        Node node = scene.lookup(selector);
        assertNotNull(node);
        return node;
    }

    private static void assertFocusIndicator(Scene scene, List<Node> focusSections, int focusedSectionIndex) {
        scene.getRoot().applyCss();
        for (int i = 0; i < focusSections.size(); i++) {
            Region section = (Region) focusSections.get(i);
            boolean isFocusedSection = i == focusedSectionIndex;
            assertEquals(isFocusedSection,
                    section.getPseudoClassStates().contains(KEYBOARD_FOCUSED_PSEUDO_CLASS));
            Border border = section.getBorder();
            assertNotNull(border);
            BorderStroke borderStroke = border.getStrokes().get(0);
            assertEquals(FOCUS_BORDER_WIDTH, borderStroke.getWidths().getTop());
            assertEquals(FOCUS_BORDER_WIDTH, borderStroke.getWidths().getRight());
            assertEquals(FOCUS_BORDER_WIDTH, borderStroke.getWidths().getBottom());
            assertEquals(FOCUS_BORDER_WIDTH, borderStroke.getWidths().getLeft());
            if (isFocusedSection) {
                assertEquals(FOCUS_BORDER_COLOR, borderStroke.getTopStroke());
            } else {
                assertNotEquals(FOCUS_BORDER_COLOR, borderStroke.getTopStroke());
            }
        }
    }

    private static ListCell<?> getSelectedListCell(ListView<?> personListView) {
        return personListView.lookupAll(".list-cell").stream()
                .map(ListCell.class::cast)
                .filter(ListCell::isSelected)
                .findFirst()
                .orElseThrow();
    }

    private static TextField getCommandTextField(Scene scene) {
        TextField commandTextField = (TextField) scene.lookup("#commandTextField");
        assertNotNull(commandTextField);
        return commandTextField;
    }

    private static void fireTab(Scene scene, boolean isShiftDown) {
        Node focusOwner = scene.getFocusOwner();
        assertNotNull(focusOwner);
        focusOwner.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB,
                isShiftDown, false, false, false));
    }

    private static void fireKeyPressed(Node target, KeyCode keyCode) {
        target.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode,
                false, false, false, false));
    }

    private static void assertSelectedPatientFullyVisible(ListView<?> personListView) {
        ListCell<?> selectedCell = getSelectedListCell(personListView);
        Node selectedPatient = selectedCell.getGraphic();
        Bounds selectedBounds = selectedPatient.localToScene(selectedPatient.getBoundsInLocal());
        Bounds viewportBounds = personListView.localToScene(personListView.getBoundsInLocal());
        assertTrue(selectedBounds.getMinY() >= viewportBounds.getMinY(),
                "The selected patient should start within the visible list viewport");
        assertTrue(selectedBounds.getMaxY() <= viewportBounds.getMaxY(),
                "The selected patient should be fully visible in the list viewport");
    }

    private static ScrollBar getVerticalScrollBar(ListView<?> personListView) {
        return personListView.lookupAll(".scroll-bar").stream()
                .map(ScrollBar.class::cast)
                .filter(scrollBar -> scrollBar.getOrientation() == Orientation.VERTICAL)
                .findFirst()
                .orElseThrow();
    }

    private static void submitCommand(TextField commandTextField, String commandText) {
        commandTextField.setText(commandText);
        commandTextField.getOnAction().handle(new ActionEvent(commandTextField, commandTextField));
    }

    private static List<String> getDisplayedDetails(VBox detailsContainer) {
        return List.of(
                getLabelText(detailsContainer, "#name"),
                getLabelText(detailsContainer, "#phone"),
                getLabelText(detailsContainer, "#email"),
                getLabelText(detailsContainer, "#address"),
                getLabelText(detailsContainer, "#visitDate"),
                getLabelText(detailsContainer, "#previousVisitDatesHeader"),
                getLabelText(detailsContainer, "#previousVisitDates"),
                getLabelText(detailsContainer, "#note"),
                getLabelText(detailsContainer, "#tags"));
    }

    private static String getLabelText(VBox detailsContainer, String labelId) {
        Label label = (Label) detailsContainer.lookup(labelId);
        return label.getText();
    }

    /** Outcomes that the test logic can return or throw. */
    private enum ExecutionOutcome {
        SUCCESS,
        CLEAR_DETAILS,
        CLEAR_MATCHING_PERSON,
        CLEAR_OTHER_PERSON,
        COMMAND_ERROR_WITH_PREVIEW,
        COMMAND_ERROR,
        PARSE_ERROR
    }

    /** Provides a displayed main window and command-entry actions for view tests. */
    private static final class MainWindowFixture {

        private final Stage stage;
        private final TestLogic logic;
        private final TextField commandTextField;
        private final ScrollPane detailsPanel;
        private final List<Node> focusSections;

        private MainWindowFixture(Person initialPerson) {
            stage = new Stage();
            logic = new TestLogic(initialPerson);
            MainWindow mainWindow = new MainWindow(stage, logic, TEST_DATA_FILE_PATH, url -> { });
            mainWindow.fillInnerParts();
            mainWindow.show();
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
            commandTextField = getCommandTextField(stage.getScene());
            detailsPanel = getDetailsPanel(mainWindow);
            focusSections = getFocusSections(stage.getScene());
        }

        private void submitView() {
            commandTextField.requestFocus();
            submitCommand(commandTextField, "view 1");
        }

        private void setPersonToView(Person person) {
            logic.setPersonToView(person);
        }

        private void scrollToBottom() {
            detailsPanel.requestFocus();
            detailsPanel.setVvalue(detailsPanel.getVmax());
        }

        private void assertCommandFocus() {
            assertSame(commandTextField, stage.getScene().getFocusOwner());
            assertFocusIndicator(stage.getScene(), focusSections, 0);
        }

        private void assertDetailsAtTop() {
            assertEquals(detailsPanel.getVmin(), detailsPanel.getVvalue());
        }

        private void assertDisplayedName(String name) {
            assertEquals(name, getLabelText((VBox) detailsPanel.getContent(), "#name"));
        }

        private void close() {
            stage.close();
        }
    }

    /** Logic stub that returns or throws the configured execution outcome. */
    private static class TestLogic implements Logic {

        private Person personToView;
        private final ObservableList<Person> persons;
        private ExecutionOutcome executionOutcome;
        private String lastCommandText;

        TestLogic(Person personToView) {
            this(personToView, ExecutionOutcome.SUCCESS);
        }

        TestLogic(Person personToView, ExecutionOutcome executionOutcome) {
            this(personToView, executionOutcome, FXCollections.observableArrayList(personToView));
        }

        TestLogic(Person personToView, ObservableList<Person> persons) {
            this(personToView, ExecutionOutcome.SUCCESS, persons);
        }

        TestLogic(Person personToView, ExecutionOutcome executionOutcome, ObservableList<Person> persons) {
            this.personToView = personToView;
            this.executionOutcome = executionOutcome;
            this.persons = persons;
        }

        @Override
        public CommandResult execute(String commandText) throws CommandException, ParseException {
            lastCommandText = commandText;
            return switch (executionOutcome) {
                case SUCCESS -> CommandResult.viewPerson(
                        "Viewing person: " + personToView.getName(), personToView);
                case CLEAR_DETAILS -> CommandResult.clearDetailsPanel("Cleared all patients");
                case CLEAR_MATCHING_PERSON -> CommandResult.clearPersonIfShowing(
                        "Deleted patient", personToView);
                case CLEAR_OTHER_PERSON -> CommandResult.clearPersonIfShowing(
                        "Deleted another patient", new PersonBuilder().withName("Another Patient").build());
                case COMMAND_ERROR_WITH_PREVIEW -> throw new CommandException(
                        "Duplicate patient", personToView);
                case COMMAND_ERROR -> throw new CommandException("Command error");
                case PARSE_ERROR -> throw new ParseException("Parse error");
                default -> throw new AssertionError("Unexpected execution outcome: " + executionOutcome);
            };
        }

        @Override
        public boolean isInputRequestPending() {
            return false;
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return persons;
        }

        @Override
        public GuiSettings getGuiSettings() {
            return new GuiSettings();
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            // This test stub does not persist GUI settings.
        }

        String getLastCommandText() {
            return lastCommandText;
        }

        void setExecutionOutcome(ExecutionOutcome executionOutcome) {
            this.executionOutcome = executionOutcome;
        }

        void setPersonToView(Person personToView) {
            this.personToView = personToView;
        }
    }
}
