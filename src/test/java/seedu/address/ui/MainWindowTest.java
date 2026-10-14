package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxExtension.runOnFxThread;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxExtension;

@ExtendWith(JavaFxExtension.class)
public class MainWindowTest {

    @TempDir
    Path testDirectory;

    private MainWindow mainWindow;
    private List<String> requestedUrls;

    @BeforeEach
    public void setUp() {
        runOnFxThread(() -> {
            Path addressBookPath = testDirectory.resolve("addressbook.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(addressBookPath),
                    new JsonUserPrefsStorage(testDirectory.resolve("preferences.json")));
            requestedUrls = new ArrayList<>();
            mainWindow = new MainWindow(new Stage(), new LogicManager(new ModelManager(), storage),
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
    public void handleHelp_minimizedWindow_restoresExistingWindow() {
        runOnFxThread(() -> {
            mainWindow.handleHelp();
            Stage helpStage = getHelpStage();
            helpStage.setIconified(true);

            mainWindow.handleHelp();

            assertFalse(helpStage.isIconified());
            assertSame(helpStage, getHelpStage());
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
}
