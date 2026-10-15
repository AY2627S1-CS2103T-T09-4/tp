package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxExtension.runOnFxThread;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxExtension;
import seedu.address.testutil.PersonBuilder;

@ExtendWith(JavaFxExtension.class)
public class MainWindowTest {

    private static final long WINDOW_STATE_TIMEOUT_SECONDS = 10;

    @TempDir
    Path testDirectory;

    private MainWindow mainWindow;
    private ModelManager model;
    private List<String> requestedUrls;

    @BeforeEach
    public void setUp() {
        runOnFxThread(() -> {
            Path addressBookPath = testDirectory.resolve("addressbook.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(addressBookPath),
                    new JsonUserPrefsStorage(testDirectory.resolve("preferences.json")));
            requestedUrls = new ArrayList<>();
            model = new ModelManager();
            mainWindow = new MainWindow(new Stage(), new LogicManager(model, storage),
                    addressBookPath, requestedUrls::add);
            mainWindow.show();
            mainWindow.fillInnerParts();
        });
    }

    @AfterEach
    public void tearDown() {
        runOnFxThread(() -> List.copyOf(Window.getWindows()).forEach(Window::hide));
    }

    @Test
    public void handleHelp_allEntryPoints_reuseAndReopenWindow() {
        runOnFxThread(() -> {
            TextField commandField = getCommandField();
            fireKey(commandField, KeyCode.F1);
            Stage helpStage = getHelpStage();
            assertTrue(helpStage.isShowing());
            helpStage.hide();

            commandField.setText("help");
            commandField.fireEvent(new ActionEvent());
            assertSame(helpStage, getHelpStage());
            helpStage.hide();

            MenuBar menuBar = (MenuBar) mainWindow.getRoot().getScene().lookup("#menuBar");
            menuBar.getMenus().get(1).getItems().get(0).fire();
            assertSame(helpStage, getHelpStage());
            mainWindow.handleHelp();
            assertSame(helpStage, getHelpStage());
            assertTrue(mainWindow.getRoot().isShowing());
            assertTrue(requestedUrls.isEmpty());
        });
    }

    @Test
    public void keyPressed_escape_isolatesHelpFromMainWindow() {
        runOnFxThread(() -> {
            mainWindow.handleHelp();
            Stage helpStage = getHelpStage();
            mainWindow.getRoot().requestFocus();

            fireKey(getCommandField(), KeyCode.ESCAPE);

            assertTrue(helpStage.isShowing());
            assertTrue(mainWindow.getRoot().isShowing());
            fireKey(helpStage.getScene().lookup("#copyButton"), KeyCode.ESCAPE);
            assertFalse(helpStage.isShowing());
            assertTrue(mainWindow.getRoot().isShowing());
            mainWindow.handleHelp();
            assertSame(helpStage, getHelpStage());
        });
    }

    @Test
    public void handleHelp_minimizedWindow_restoresExistingWindow() throws InterruptedException {
        AtomicReference<Stage> originalHelpStage = new AtomicReference<>();
        runOnFxThread(() -> {
            mainWindow.handleHelp();
            Stage helpStage = getHelpStage();
            originalHelpStage.set(helpStage);
            helpStage.setIconified(true);
        });

        waitForIconifiedState(originalHelpStage.get(), true);
        runOnFxThread(() -> {
            Stage helpStage = originalHelpStage.get();
            assertTrue(helpStage.isIconified(), "Help must be minimized before requesting restoration");
            mainWindow.handleHelp();
            assertSame(originalHelpStage.get(), getHelpStage());
        });

        waitForIconifiedState(originalHelpStage.get(), false);
    }

    @Test
    public void constructor_withoutBrowserOpener_createsWindow() {
        runOnFxThread(() -> {
            Path addressBookPath = testDirectory.resolve("compatibility-addressbook.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(addressBookPath),
                    new JsonUserPrefsStorage(testDirectory.resolve("compatibility-preferences.json")));
            MainWindow compatibilityWindow = new MainWindow(new Stage(), new LogicManager(new ModelManager(), storage),
                    addressBookPath);

            compatibilityWindow.show();
            assertTrue(compatibilityWindow.getRoot().isShowing());
            compatibilityWindow.handleHelp();
            assertTrue(getHelpStage().isShowing());
        });
    }

    @Test
    public void executeCommand_viewCommand_displaysPersonInDetailsPanel() {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Alex Yeoh").build();
            model.addPerson(person);

            try {
                mainWindow.executeCommand("view 1");
            } catch (CommandException | ParseException e) {
                throw new AssertionError("View command should execute successfully", e);
            }

            PatientDetailsPanel patientDetailsPanel = mainWindow.getPatientDetailsPanel();
            assertNotNull(patientDetailsPanel);
            ScrollPane detailsPanel = (ScrollPane) patientDetailsPanel.getRoot();
            VBox detailsContainer = (VBox) detailsPanel.getContent();
            Label name = (Label) detailsContainer.lookup("#name");
            assertNotNull(name);
            assertEquals("Alex Yeoh", name.getText());
            assertTrue(detailsContainer.isVisible());
        });
    }

    private TextField getCommandField() {
        return (TextField) mainWindow.getRoot().getScene().lookup("#commandTextField");
    }

    private Stage getHelpStage() {
        return Window.getWindows().stream()
                .filter(window -> window instanceof Stage stage && "Help".equals(stage.getTitle()))
                .map(window -> (Stage) window)
                .findFirst().orElseThrow(() -> new AssertionError("Help window was not shown"));
    }

    private static void fireKey(Node target, KeyCode keyCode) {
        target.fireEvent(new KeyEvent(target, target, KeyEvent.KEY_PRESSED, "", "", keyCode,
                false, false, false, false));
    }

    /**
     * Waits on the JUnit thread while JavaFX processes window-state changes.
     */
    private static void waitForIconifiedState(Stage stage, boolean shouldBeIconified) throws InterruptedException {
        CountDownLatch stateLatch = new CountDownLatch(1);
        ChangeListener<Boolean> stateListener = (observable, wasIconified, isIconified) -> {
            if (isIconified == shouldBeIconified) {
                stateLatch.countDown();
            }
        };
        runOnFxThread(() -> {
            stage.iconifiedProperty().addListener(stateListener);
            if (stage.isIconified() == shouldBeIconified) {
                stateLatch.countDown();
            }
        });
        try {
            assertTrue(stateLatch.await(WINDOW_STATE_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "The window manager did not set iconified=" + shouldBeIconified);
            runOnFxThread(() -> assertEquals(shouldBeIconified, stage.isIconified()));
        } finally {
            runOnFxThread(() -> stage.iconifiedProperty().removeListener(stateListener));
        }
    }
}
