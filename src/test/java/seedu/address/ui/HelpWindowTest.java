package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
            HelpWindow helpWindow = new HelpWindow();
            Stage stage = helpWindow.getRoot();
            helpWindow.show();

            try {
                assertTrue(stage.isResizable());
                assertEquals(Modality.NONE, stage.getModality());

                ScrollPane scrollPane = (ScrollPane) stage.getScene().getRoot();
                assertTrue(scrollPane.isFitToWidth());
                assertTrue(scrollPane.isPannable());
                assertEquals(ScrollBarPolicy.NEVER, scrollPane.getHbarPolicy());
                assertEquals(ScrollBarPolicy.AS_NEEDED, scrollPane.getVbarPolicy());

                Label message = (Label) scrollPane.lookup("#helpMessage");
                assertNotNull(message);
                assertTrue(message.isWrapText());
                assertEquals(HelpWindow.HELP_MESSAGE, message.getText());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void helpWindow_closeButton_closesWindow() {
        runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow();
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
            HelpWindow helpWindow = new HelpWindow();
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

    private Button findButton(Stage stage, String selector) {
        Node button = stage.getScene().lookup(selector);
        assertNotNull(button);
        return (Button) button;
    }
}
