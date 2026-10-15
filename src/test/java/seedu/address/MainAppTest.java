package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxExtension.runOnFxThread;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.testutil.JavaFxExtension;

@ExtendWith(JavaFxExtension.class)
public class MainAppTest {

    private static final String EXPECTED_URL = "https://ay2627s1-cs2103t-t09-4.github.io/tp/UserGuide.html";

    @AfterEach
    public void tearDown() {
        runOnFxThread(() -> List.copyOf(Window.getWindows()).forEach(Window::hide));
    }

    @Test
    public void init_helpLinkActivated_requestsGuideThroughBrowserOpener() throws Exception {
        TestMainApp mainApp = new TestMainApp();

        mainApp.init();

        runOnFxThread(() -> {
            Stage primaryStage = new Stage();
            mainApp.start(primaryStage);
            assertTrue(primaryStage.isShowing());
            assertTrue(mainApp.requestedUrls.isEmpty());
            Node commandField = primaryStage.getScene().lookup("#commandTextField");
            commandField.fireEvent(new KeyEvent(commandField, commandField, KeyEvent.KEY_PRESSED, "", "", KeyCode.F1,
                    false, false, false, false));
            Stage helpStage = Window.getWindows().stream()
                    .filter(window -> window instanceof Stage stage && "Help".equals(stage.getTitle()))
                    .map(window -> (Stage) window)
                    .findFirst().orElseThrow(() -> new AssertionError("Help window was not shown"));
            Hyperlink userGuideLink = (Hyperlink) helpStage.getScene().lookup("#userGuideLink");
            userGuideLink.fire();

            assertEquals(List.of(EXPECTED_URL), mainApp.requestedUrls);
        });
    }

    private static class TestMainApp extends MainApp {

        private final List<String> requestedUrls = new ArrayList<>();

        @Override
        protected void openInBrowser(String url) {
            requestedUrls.add(url);
        }

        @Override
        protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
            return new UserPrefs();
        }
    }
}
