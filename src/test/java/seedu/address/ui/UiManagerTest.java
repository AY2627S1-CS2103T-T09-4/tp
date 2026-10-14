package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxExtension.runOnFxThread;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
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
public class UiManagerTest {

    @TempDir
    Path testDirectory;

    @AfterEach
    public void tearDown() {
        runOnFxThread(() -> List.copyOf(Window.getWindows()).forEach(Window::hide));
    }

    @Test
    public void start_withBrowserOpener_showsMainWindow() {
        runOnFxThread(() -> {
            Path addressBookPath = testDirectory.resolve("addressbook.json");
            StorageManager storage = createStorage(addressBookPath);
            UiManager uiManager = new UiManager(new LogicManager(new ModelManager(), storage), addressBookPath,
                    url -> assertTrue(url.startsWith("https://")));
            Stage primaryStage = new Stage();

            uiManager.start(primaryStage);

            assertTrue(primaryStage.isShowing());
        });
    }

    @Test
    public void start_withoutBrowserOpener_showsMainWindow() {
        runWithHeadlessDesktop(() -> runOnFxThread(() -> {
            Path addressBookPath = testDirectory.resolve("compatibility-addressbook.json");
            StorageManager storage = createStorage(addressBookPath);
            UiManager uiManager = new UiManager(new LogicManager(new ModelManager(), storage), addressBookPath);
            Stage primaryStage = new Stage();

            uiManager.start(primaryStage);

            assertTrue(primaryStage.isShowing());
            Node commandField = primaryStage.getScene().lookup("#commandTextField");
            fireKey(commandField, KeyCode.F1);
            Stage helpStage = Window.getWindows().stream()
                    .filter(window -> window instanceof Stage stage && "Help".equals(stage.getTitle()))
                    .map(window -> (Stage) window)
                    .findFirst().orElseThrow(() -> new AssertionError("Help window was not shown"));
            Hyperlink userGuideLink = (Hyperlink) helpStage.getScene().lookup("#userGuideLink");
            userGuideLink.fire();
            assertTrue(helpStage.getScene().lookup("#browserError").isVisible());
        }));
    }

    private StorageManager createStorage(Path addressBookPath) {
        return new StorageManager(new JsonAddressBookStorage(addressBookPath),
                new JsonUserPrefsStorage(testDirectory.resolve("preferences.json")));
    }

    private static void fireKey(Node target, KeyCode keyCode) {
        target.fireEvent(new KeyEvent(target, target, KeyEvent.KEY_PRESSED, "", "", keyCode,
                false, false, false, false));
    }

    private static void runWithHeadlessDesktop(Runnable action) {
        String previousHeadlessSetting = System.getProperty("java.awt.headless");
        System.setProperty("java.awt.headless", "true");
        try {
            action.run();
        } finally {
            if (previousHeadlessSetting == null) {
                System.clearProperty("java.awt.headless");
            } else {
                System.setProperty("java.awt.headless", previousHeadlessSetting);
            }
        }
    }
}
