package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class UiManagerIntegrationTest {

    @TempDir
    private Path temporaryFolder;

    @BeforeAll
    public static void setUpJavaFx() throws InterruptedException {
        startJavaFx();
    }

    @Test
    public void start_helpMenuAndUserGuide_forwardActionAndShowFeedback() {
        runOnFxThread(() -> {
            List<Window> existingWindows = List.copyOf(Window.getWindows());
            Stage primaryStage = new Stage();
            try {
                Path patientDataPath = temporaryFolder.resolve("patients.json");
                List<String> requestedUrls = new ArrayList<>();
                startUi(primaryStage, patientDataPath, requestedUrls);
                assertGuideAction(primaryStage, existingWindows, requestedUrls);
                assertFalse(Files.exists(patientDataPath));
            } finally {
                closeTestStages(primaryStage, existingWindows);
            }
        });
    }

    private void startUi(Stage primaryStage, Path patientDataPath, List<String> requestedUrls) {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(patientDataPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        LogicManager logic = new LogicManager(model, storage);
        new UiManager(logic, patientDataPath, requestedUrls::add).start(primaryStage);
        primaryStage.getScene().getRoot().applyCss();
        primaryStage.getScene().getRoot().layout();
    }

    /** Closes only windows created by this test, retaining unrelated Help stages. */
    private void closeTestStages(Stage primaryStage, List<Window> existingWindows) {
        List<Stage> helpStages = Window.getWindows().stream()
                .filter(window -> !existingWindows.contains(window) && window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(UiManagerIntegrationTest::isHelpStage).toList();
        try {
            for (Stage helpStage : helpStages) {
                helpStage.close();
            }
        } finally {
            primaryStage.close();
        }
    }

    private void assertGuideAction(Stage primaryStage, List<Window> existingWindows, List<String> requestedUrls) {
        // The recording consumer verifies the UI flow without opening an external browser.
        getHelpMenuItem(primaryStage).fire();
        Stage helpStage = getOnlyShowingHelpStage(existingWindows);
        Button openGuideButton = (Button) helpStage.getScene().lookup("#openGuideButton");
        Label guideStatus = (Label) helpStage.getScene().lookup("#guideStatus");

        assertNotNull(openGuideButton);
        assertNotNull(guideStatus);
        openGuideButton.fire();

        assertEquals(List.of(HelpWindow.USER_GUIDE_URL), requestedUrls);
        assertTrue(primaryStage.isShowing());
        assertTrue(helpStage.isShowing());
        assertTrue(guideStatus.isVisible());
        assertEquals(HelpWindow.MESSAGE_BROWSER_REQUESTED, guideStatus.getText());
    }

    private MenuItem getHelpMenuItem(Stage primaryStage) {
        MenuBar menuBar = (MenuBar) primaryStage.getScene().lookup("#menuBar");
        return menuBar.getMenus().stream().flatMap(menu -> menu.getItems().stream())
                .filter(menuItem -> "helpMenuItem".equals(menuItem.getId())).findFirst().orElseThrow();
    }

    private Stage getOnlyShowingHelpStage(List<Window> existingWindows) {
        List<Stage> helpStages = Window.getWindows().stream()
                .filter(window -> !existingWindows.contains(window) && window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(stage -> stage.isShowing() && isHelpStage(stage))
                .toList();
        assertEquals(1, helpStages.size());
        return helpStages.getFirst();
    }

    private static boolean isHelpStage(Stage stage) {
        return stage.getScene() != null && stage.getScene().lookup("#helpMessage") != null;
    }
}
