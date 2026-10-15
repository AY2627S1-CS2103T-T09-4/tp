package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxExtension.runOnFxThread;

import java.nio.file.Path;
import java.util.ArrayList;
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

    private static final String EXPECTED_URL = "https://ay2627s1-cs2103t-t09-4.github.io/tp/UserGuide.html";

    @TempDir
    Path testDirectory;

    @AfterEach
    public void tearDown() {
        runOnFxThread(() -> List.copyOf(Window.getWindows()).forEach(Window::hide));
    }

    @Test
    public void start_helpLinkActivated_requestsGuideThroughBrowserOpener() {
        runOnFxThread(() -> {
            Path addressBookPath = testDirectory.resolve("addressbook.json");
            StorageManager storage = createStorage(addressBookPath);
            List<String> requestedUrls = new ArrayList<>();
            UiManager uiManager = new UiManager(new LogicManager(new ModelManager(), storage), addressBookPath,
                    requestedUrls::add);
            Stage primaryStage = new Stage();

            uiManager.start(primaryStage);

            assertTrue(primaryStage.isShowing());
            assertTrue(requestedUrls.isEmpty());
            fireKey(primaryStage.getScene().lookup("#commandTextField"), KeyCode.F1);
            Hyperlink userGuideLink = (Hyperlink) getHelpStage().getScene().lookup("#userGuideLink");
            userGuideLink.fire();

            assertEquals(List.of(EXPECTED_URL), requestedUrls);
        });
    }

    @Test
    public void start_withoutBrowserOpener_showsMainWindow() {
        runOnFxThread(() -> {
            Path addressBookPath = testDirectory.resolve("compatibility-addressbook.json");
            StorageManager storage = createStorage(addressBookPath);
            UiManager uiManager = new UiManager(new LogicManager(new ModelManager(), storage), addressBookPath);
            Stage primaryStage = new Stage();

            uiManager.start(primaryStage);

            assertTrue(primaryStage.isShowing());
            Node commandField = primaryStage.getScene().lookup("#commandTextField");
            fireKey(commandField, KeyCode.F1);
            assertTrue(getHelpStage().isShowing());
        });
    }

    private StorageManager createStorage(Path addressBookPath) {
        return new StorageManager(new JsonAddressBookStorage(addressBookPath),
                new JsonUserPrefsStorage(testDirectory.resolve("preferences.json")));
    }

    private static void fireKey(Node target, KeyCode keyCode) {
        target.fireEvent(new KeyEvent(target, target, KeyEvent.KEY_PRESSED, "", "", keyCode,
                false, false, false, false));
    }

    private static Stage getHelpStage() {
        return Window.getWindows().stream()
                .filter(window -> window instanceof Stage stage && "Help".equals(stage.getTitle()))
                .map(window -> (Stage) window)
                .findFirst().orElseThrow(() -> new AssertionError("Help window was not shown"));
    }
}
