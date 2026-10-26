package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NextCommand;
import seedu.address.logic.commands.PrevCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.ViewCommand;

public class HelpWindowTest {

    private static final List<String> EXPECTED_PATIENT_COMMANDS = List.of(AddCommand.COMMAND_WORD,
            ListCommand.COMMAND_WORD, FindCommand.COMMAND_WORD, ViewCommand.COMMAND_WORD,
            EditCommand.COMMAND_WORD, DeleteCommand.COMMAND_WORD, ClearCommand.COMMAND_WORD);
    private static final List<String> EXPECTED_VISIT_COMMANDS = List.of(NextCommand.COMMAND_WORD,
            PrevCommand.COMMAND_WORD, SortCommand.COMMAND_WORD);
    private static final List<String> EXPECTED_APPLICATION_COMMANDS = List.of(HelpCommand.COMMAND_WORD,
            ExitCommand.COMMAND_WORD);

    @BeforeAll
    public static void setUpJavaFx() throws InterruptedException {
        startJavaFx();
    }

    @Test
    public void helpWindow_minimumSize_isResizableAndScrollable() {
        runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow(new Stage(), url -> { }, () -> false);
            Stage stage = helpWindow.getRoot();
            stage.setWidth(stage.getMinWidth());
            stage.setHeight(stage.getMinHeight());
            helpWindow.show();

            try {
                assertTrue(stage.isResizable());
                assertEquals(Modality.NONE, stage.getModality());

                ScrollPane scrollPane = (ScrollPane) stage.getScene().lookup("#helpScrollPane");
                assertTrue(scrollPane.isFitToWidth());
                assertTrue(scrollPane.isPannable());
                assertTrue(scrollPane.isFocusTraversable());
                assertEquals(ScrollBarPolicy.NEVER, scrollPane.getHbarPolicy());
                assertEquals(ScrollBarPolicy.AS_NEEDED, scrollPane.getVbarPolicy());

                Label message = (Label) stage.getScene().lookup("#helpMessage");
                assertNotNull(message);
                assertTrue(message.isWrapText());
                assertEquals(HelpWindow.HELP_MESSAGE, message.getText());

                assertMinimumLayout(stage, scrollPane);
                findButton(stage, "#copyButton").fire();
                assertMinimumLayout(stage, scrollPane);
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void helpWindow_availableCommands_listsEachCommandOnceInGroups() {
        runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow(url -> { });
            Stage stage = helpWindow.getRoot();
            try {
                helpWindow.show();
                Parent root = stage.getScene().getRoot();
                assertEquals("Available commands", ((Label) root.lookup(".helpTitle")).getText());
                assertCommandGroup(root, "#patientCommands", EXPECTED_PATIENT_COMMANDS);
                assertCommandGroup(root, "#visitCommands", EXPECTED_VISIT_COMMANDS);
                assertCommandGroup(root, "#applicationCommands", EXPECTED_APPLICATION_COMMANDS);
                int expectedCommandCount = EXPECTED_PATIENT_COMMANDS.size() + EXPECTED_VISIT_COMMANDS.size()
                        + EXPECTED_APPLICATION_COMMANDS.size();
                assertEquals(expectedCommandCount, root.lookupAll(".helpCommand").size());
                ScrollPane scrollPane = (ScrollPane) root.lookup("#helpScrollPane");
                Label keyboardHint = (Label) scrollPane.getContent().lookup("#keyboardHint");
                assertEquals("↑/↓ Scroll · Tab/Shift+Tab Move focus · Esc Close", keyboardHint.getText());
                assertTrue(keyboardHint.isWrapText());
                assertSame(scrollPane, stage.getScene().getFocusOwner());
                root.applyCss();
                root.layout();
                assertTrue(scrollPane.getContent().getBoundsInLocal().getHeight()
                        <= scrollPane.getViewportBounds().getHeight(),
                        "All commands should fit in the default window");
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void helpWindow_minimumSize_arrowKeysScrollCommands() {
        runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow(url -> { });
            Stage stage = helpWindow.getRoot();
            try {
                stage.setWidth(stage.getMinWidth());
                stage.setHeight(stage.getMinHeight());
                helpWindow.show();
                ScrollPane scrollPane = (ScrollPane) stage.getScene().lookup("#helpScrollPane");
                assertMinimumLayout(stage, scrollPane);
                assertSame(scrollPane, stage.getScene().getFocusOwner());

                double positionBeforeDown = scrollPane.getVvalue();
                fireKeyPressed(scrollPane, KeyCode.DOWN);
                assertTrue(scrollPane.getVvalue() > positionBeforeDown, "Down should scroll towards later commands");

                double positionBeforeUp = scrollPane.getVvalue();
                fireKeyPressed(scrollPane, KeyCode.UP);
                assertTrue(scrollPane.getVvalue() < positionBeforeUp, "Up should scroll towards earlier commands");
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void helpWindow_closeButton_closesWindow() {
        // EP: A shown Help stage transitions to hidden through its real Close button.
        runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow(url -> { });
            Stage stage = helpWindow.getRoot();
            helpWindow.show();

            try {
                Button closeButton = findButton(stage, "#closeButton");
                assertTrue(closeButton.isFocusTraversable());
                closeButton.fire();
                assertFalse(helpWindow.isShowing());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void helpWindow_escapeKey_closesWindow() {
        runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow(url -> { });
            Stage stage = helpWindow.getRoot();
            helpWindow.show();

            try {
                KeyEvent escape = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE,
                        false, false, false, false);
                stage.getScene().getRoot().fireEvent(escape);
                assertFalse(helpWindow.isShowing());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void show_minimizedWindow_restoresWindow() {
        // EP: Showing an iconified Help stage must restore it, rather than leave it minimized.
        runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow(url -> { });
            Stage stage = helpWindow.getRoot();
            try {
                helpWindow.show();
                stage.setIconified(true);
                assertTrue(stage.isIconified());

                helpWindow.show();

                assertTrue(helpWindow.isShowing());
                assertFalse(stage.isIconified());
                assertSame(stage.getScene().lookup("#helpScrollPane"), stage.getScene().getFocusOwner());
            } finally {
                stage.close();
            }
        });
    }

    private void fireKeyPressed(Node target, KeyCode keyCode) {
        target.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode, false, false, false, false));
    }

    private Button findButton(Stage stage, String selector) {
        Node button = stage.getScene().lookup(selector);
        assertNotNull(button);
        return (Button) button;
    }

    /** Checks that commands retain most of the small window, including when action feedback is shown. */
    private void assertMinimumLayout(Stage stage, ScrollPane scrollPane) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        assertTrue(scrollPane.getViewportBounds().getHeight() >= stage.getScene().getHeight() / 2,
                "Commands should occupy at least half of the minimum-size window");
        assertTrue(scrollPane.getContent().getBoundsInLocal().getHeight()
                > scrollPane.getViewportBounds().getHeight());
        Button closeButton = findButton(stage, "#closeButton");
        assertTrue(closeButton.localToScene(closeButton.getBoundsInLocal()).getMaxY()
                <= stage.getScene().getHeight());
        assertTrue(closeButton.localToScene(closeButton.getBoundsInLocal()).getMinY()
                >= scrollPane.localToScene(scrollPane.getBoundsInLocal()).getMaxY());
    }

    /** Checks displayed command names, their order, and the presence of readable purposes. */
    private void assertCommandGroup(Node root, String selector, List<String> expectedCommands) {
        VBox group = (VBox) root.lookup(selector);
        assertNotNull(group);
        List<String> commandNames = new ArrayList<>();
        for (Node child : group.getChildren()) {
            if (child.getStyleClass().contains("helpCommand")) {
                Label entry = (Label) child;
                String[] commandParts = entry.getText().split(" — ", 2);
                assertEquals(2, commandParts.length);
                assertFalse(commandParts[1].isBlank());
                assertTrue(entry.isWrapText());
                commandNames.add(commandParts[0]);
            }
        }
        assertEquals(expectedCommands, commandNames);
    }
}
