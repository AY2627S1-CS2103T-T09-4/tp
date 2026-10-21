package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
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

    private static VBox getDetailsContainer(MainWindow mainWindow) {
        ScrollPane detailsPanel = (ScrollPane) mainWindow.getPatientDetailsPanel().getRoot();
        return (VBox) detailsPanel.getContent();
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
        COMMAND_ERROR_WITH_PREVIEW,
        COMMAND_ERROR,
        PARSE_ERROR
    }

    /** Logic stub that returns or throws the configured execution outcome. */
    private static class TestLogic implements Logic {

        private final Person personToView;
        private final ObservableList<Person> persons;
        private ExecutionOutcome executionOutcome;
        private String lastCommandText;

        TestLogic(Person personToView) {
            this(personToView, ExecutionOutcome.SUCCESS);
        }

        TestLogic(Person personToView, ExecutionOutcome executionOutcome) {
            this.personToView = personToView;
            this.executionOutcome = executionOutcome;
            persons = FXCollections.observableArrayList(personToView);
        }

        @Override
        public CommandResult execute(String commandText) throws CommandException, ParseException {
            lastCommandText = commandText;
            return switch (executionOutcome) {
                case SUCCESS -> CommandResult.viewPerson(
                        "Viewing person: " + personToView.getName(), personToView);
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
    }
}
