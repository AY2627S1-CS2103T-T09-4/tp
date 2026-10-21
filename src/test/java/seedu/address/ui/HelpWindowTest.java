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

public class HelpWindowTest {

    @BeforeAll
    public static void setUpJavaFx() throws InterruptedException {
        startJavaFx();
    }

    @Test
    public void helpWindow_layoutIsResizableAndScrollable() {
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
                assertCommandGroup(root, "#patientCommands",
                        List.of("add", "list", "find", "view", "edit", "delete", "clear"));
                assertCommandGroup(root, "#visitCommands", List.of("next", "prev", "sort"));
                assertCommandGroup(root, "#applicationCommands", List.of("help", "exit"));
                assertEquals(12, root.lookupAll(".helpCommand").size());
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
    public void helpWindow_closeButton_closesWindow() {
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
