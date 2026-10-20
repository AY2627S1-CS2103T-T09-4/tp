package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;

import java.nio.file.Path;

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

    private static class TestLogic implements Logic {

        private final Person personToView;
        private final ObservableList<Person> persons;
        private String lastCommandText;

        TestLogic(Person personToView) {
            this.personToView = personToView;
            persons = FXCollections.observableArrayList(personToView);
        }

        @Override
        public CommandResult execute(String commandText) {
            lastCommandText = commandText;
            return CommandResult.viewPerson("Viewing person: " + personToView.getName(), personToView);
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
    }
}
