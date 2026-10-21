package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class HelpWindowIntegrationTest {

    private static final String DRAFT_COMMAND = "find Be";

    @TempDir
    Path temporaryFolder;

    private Path patientDataPath;
    private Model model;
    private LogicManager logic;
    private MainWindow mainWindow;
    private Stage primaryStage;
    private TextField commandTextField;
    private TextArea resultDisplay;
    private List<String> requestedUrls;

    @BeforeAll
    public static void setUpJavaFx() throws InterruptedException {
        startJavaFx();
    }

    @BeforeEach
    public void setUp() {
        runOnFxThread(() -> {
            patientDataPath = temporaryFolder.resolve("patients.json");
            model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(patientDataPath),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
            logic = new LogicManager(model, storage);
            primaryStage = new Stage();
            requestedUrls = new ArrayList<>();
            mainWindow = new MainWindow(primaryStage, logic, patientDataPath, requestedUrls::add);
            mainWindow.show();
            mainWindow.fillInnerParts();
            primaryStage.getScene().getRoot().applyCss();
            primaryStage.getScene().getRoot().layout();
            commandTextField = (TextField) primaryStage.getScene().lookup("#commandTextField");
            resultDisplay = (TextArea) primaryStage.getScene().lookup("#resultDisplay");
        });
    }

    @AfterEach
    public void tearDown() {
        runOnFxThread(() -> {
            for (Stage helpStage : getShowingHelpStages()) {
                helpStage.close();
            }
            primaryStage.close();
        });
    }

    @Test
    public void openUserGuide_fromHelpWindow_usesSuppliedBrowserAction() {
        runOnFxThread(() -> {
            mainWindow.handleHelp();
            Stage helpStage = getOnlyShowingHelpStage();

            Button openGuideButton = (Button) helpStage.getScene().lookup("#openGuideButton");
            openGuideButton.fire();

            assertEquals(List.of(HelpWindow.USERGUIDE_URL), requestedUrls);
            assertTrue(helpStage.isShowing());
        });
    }

    @Test
    public void handleHelp_typedHelpAndMenu_reuseWindowAndPreserveBounds() {
        runOnFxThread(() -> {
            submitCommand("help");
            Stage helpStage = getOnlyShowingHelpStage();
            helpStage.setWidth(580);
            helpStage.setHeight(320);
            helpStage.setX(40);
            helpStage.setY(60);

            for (String command : List.of("help", "help add", "help 123")) {
                submitCommand(command);
                assertSame(helpStage, getOnlyShowingHelpStage());
            }
            getHelpMenuItem().fire();

            assertSame(helpStage, getOnlyShowingHelpStage());
            assertEquals(580, helpStage.getWidth());
            assertEquals(320, helpStage.getHeight());
            assertEquals(40, helpStage.getX());
            assertEquals(60, helpStage.getY());
            assertEquals("", commandTextField.getText());
        });
    }

    @Test
    public void handleHelp_minimizedWindow_restoresExistingWindow() throws InterruptedException {
        Stage[] helpStages = new Stage[1];
        runOnFxThread(() -> {
            mainWindow.handleHelp();
            Stage helpStage = getOnlyShowingHelpStage();
            helpStages[0] = helpStage;
            assertNull(helpStage.getOwner(), "Help must remain independently minimizable");
        });
        Stage helpStage = helpStages[0];
        waitForFxCondition(helpStage::isFocused, "Help to receive focus after opening");

        runOnFxThread(() -> helpStage.setIconified(true));
        // isIconified changes before the window manager finishes; focus loss confirms the transition.
        waitForFxCondition(() -> helpStage.isIconified() && !helpStage.isFocused(), "Help to finish minimizing");
        runOnFxThread(() -> {
            assertFalse(primaryStage.isIconified());
            mainWindow.handleHelp();
        });
        waitForFxCondition(() -> !helpStage.isIconified() && helpStage.isFocused(),
                "Help to restore and receive focus");
        runOnFxThread(() -> {
            assertSame(helpStage, getOnlyShowingHelpStage());
            assertFalse(primaryStage.isIconified());
        });
    }

    @Test
    public void handleHelp_closeButton_returnsToDraftAndReopens() {
        assertDismissalPreservesDraft(commandTextField,
                stage -> ((Button) stage.getScene().lookup("#closeButton")).fire());
    }

    @Test
    public void handleHelp_escape_returnsToDraftAndReopens() {
        assertDismissalPreservesDraft(resultDisplay, stage -> fireKey(stage.getScene().getRoot(), KeyCode.ESCAPE));
    }

    @Test
    public void handleHelp_nativeClose_returnsToDraftAndReopens() {
        assertDismissalPreservesDraft(commandTextField, stage -> stage.fireEvent(
                new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
    }

    @Test
    public void handleHelp_pendingConfirmation_keepsRequestAndDraft() {
        runOnFxThread(() -> {
            submitCommand("delete 1");
            String prompt = resultDisplay.getText();
            int patientCount = model.getAddressBook().getPersonList().size();
            commandTextField.setText("y");

            fireKey(commandTextField, KeyCode.F1);
            Stage helpStage = getOnlyShowingHelpStage();
            Button closeButton = (Button) helpStage.getScene().lookup("#closeButton");
            closeButton.fire();
            getHelpMenuItem().fire();
            assertSame(helpStage, getOnlyShowingHelpStage());
            fireKey(helpStage.getScene().getRoot(), KeyCode.ESCAPE);

            assertTrue(logic.isInputRequestPending());
            assertEquals(prompt, resultDisplay.getText());
            assertEquals("y", commandTextField.getText());
            assertSame(commandTextField, primaryStage.getScene().getFocusOwner());
            assertEquals(patientCount, model.getAddressBook().getPersonList().size());
            assertFalse(Files.exists(patientDataPath));
            submitCommand("n");
            assertFalse(logic.isInputRequestPending());
        });
    }

    @Test
    public void commandEntry_helpDuringConfirmation_remainsCancellationResponse() {
        runOnFxThread(() -> {
            int patientCount = model.getAddressBook().getPersonList().size();
            submitCommand("delete 1");
            assertTrue(logic.isInputRequestPending());

            submitCommand("help");

            assertTrue(getShowingHelpStages().isEmpty());
            assertFalse(logic.isInputRequestPending());
            assertEquals(DeleteCommand.MESSAGE_DELETE_CANCELLED, resultDisplay.getText());
            assertEquals(patientCount, model.getAddressBook().getPersonList().size());
            assertFalse(Files.exists(patientDataPath));
        });
    }

    @Test
    public void handleExit_minimizedHelp_hidesBothWindows() {
        runOnFxThread(() -> {
            mainWindow.handleHelp();
            Stage helpStage = getOnlyShowingHelpStage();
            helpStage.setIconified(true);

            submitCommand("exit");

            assertFalse(primaryStage.isShowing());
            assertFalse(helpStage.isShowing());
        });
    }

    @Test
    public void handleExit_nativeMainWindowClose_hidesHelp() {
        runOnFxThread(() -> {
            mainWindow.handleHelp();
            Stage helpStage = getOnlyShowingHelpStage();

            primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));

            assertFalse(primaryStage.isShowing());
            assertFalse(helpStage.isShowing());
        });
    }

    private void assertDismissalPreservesDraft(Node helpSource, Consumer<Stage> dismissWindow) {
        runOnFxThread(() -> {
            commandTextField.requestFocus();
            commandTextField.setText(DRAFT_COMMAND);
            commandTextField.selectRange(2, 5);
            helpSource.requestFocus();
            fireKey(helpSource, KeyCode.F1);
            Stage helpStage = getOnlyShowingHelpStage();

            dismissWindow.accept(helpStage);

            assertFalse(helpStage.isShowing());
            assertTrue(primaryStage.isShowing());
            assertEquals(DRAFT_COMMAND, commandTextField.getText());
            assertSame(commandTextField, primaryStage.getScene().getFocusOwner());
            assertEquals(2, commandTextField.getAnchor());
            assertEquals(5, commandTextField.getCaretPosition());
            assertFalse(Files.exists(patientDataPath));
            fireKey(commandTextField, KeyCode.F1);
            assertSame(helpStage, getOnlyShowingHelpStage());
            assertSame(helpStage.getScene().lookup("#helpScrollPane"), helpStage.getScene().getFocusOwner());
        });
    }

    /** Waits off the FX thread so native window events can finish, reporting the transition on timeout. */
    private void waitForFxCondition(BooleanSupplier condition, String transition) throws InterruptedException {
        CountDownLatch conditionMet = new CountDownLatch(1);
        AnimationTimer[] timerHolder = new AnimationTimer[1];
        runOnFxThread(() -> {
            timerHolder[0] = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (condition.getAsBoolean()) {
                        stop();
                        conditionMet.countDown();
                    }
                }
            };
            timerHolder[0].start();
        });

        try {
            assertTrue(conditionMet.await(10, TimeUnit.SECONDS), "Timed out waiting for " + transition);
        } finally {
            runOnFxThread(() -> timerHolder[0].stop());
        }
    }

    private Stage getOnlyShowingHelpStage() {
        List<Stage> helpStages = getShowingHelpStages();
        assertEquals(1, helpStages.size());
        return helpStages.getFirst();
    }

    private List<Stage> getShowingHelpStages() {
        return Window.getWindows().stream().filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(stage -> stage.getScene() != null && stage.getScene().lookup("#helpMessage") != null).toList();
    }

    private MenuItem getHelpMenuItem() {
        MenuBar menuBar = (MenuBar) primaryStage.getScene().lookup("#menuBar");
        return menuBar.getMenus().stream().flatMap(menu -> menu.getItems().stream())
                .filter(menuItem -> "helpMenuItem".equals(menuItem.getId())).findFirst().orElseThrow();
    }

    private void submitCommand(String commandText) {
        commandTextField.setText(commandText);
        commandTextField.getOnAction().handle(new ActionEvent(commandTextField, commandTextField));
    }

    private void fireKey(Node target, KeyCode keyCode) {
        target.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode, false, false, false, false));
    }
}
