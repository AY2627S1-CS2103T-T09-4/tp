package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.JavaFxExtension.runOnFxThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.testutil.JavaFxExtension;

@ExtendWith(JavaFxExtension.class)
public class HelpWindowTest {

    private static final String EXPECTED_URL = "https://ay2627s1-cs2103t-t09-4.github.io/tp/UserGuide.html";

    private HelpWindow helpWindow;
    private List<String> requestedUrls;

    @BeforeEach
    public void setUp() {
        runOnFxThread(() -> {
            requestedUrls = new ArrayList<>();
            helpWindow = new HelpWindow(requestedUrls::add);
            helpWindow.show();
        });
    }

    @AfterEach
    public void tearDown() {
        runOnFxThread(() -> helpWindow.hide());
    }

    @Test
    public void show_guideAndHintsVisible_doesNotOpenBrowser() {
        runOnFxThread(() -> {
            assertEquals(EXPECTED_URL, getGuideLink().getText());
            assertEquals("Press Esc to close", getLabel("#closeHint").getText());
            assertTrue(getLabel("#internetHint").getText().contains("Internet access"));
            assertFalse(getLabel("#browserError").isVisible());
            assertFalse(getLabel("#browserError").isManaged());
            assertSame(getGuideLink(), helpWindow.getRoot().getScene().getFocusOwner());
            assertTrue(requestedUrls.isEmpty());
        });
    }

    @Test
    public void openUserGuide_repeatedActivation_requestsCorrectUrlAndKeepsCopyAvailable() {
        runOnFxThread(() -> {
            getGuideLink().fire();
            getGuideLink().fire();

            assertEquals(List.of(EXPECTED_URL, EXPECTED_URL), requestedUrls);
            assertTrue(helpWindow.isShowing());
            assertCopyUrl();
        });
    }

    @Test
    public void keyPressed_tabAndSpace_navigatesAndActivatesControls() {
        runOnFxThread(() -> {
            fireKey(getGuideLink(), KeyCode.TAB, false);
            assertSame(getCopyButton(), helpWindow.getRoot().getScene().getFocusOwner());
            fireKey(getCopyButton(), KeyCode.TAB, true);
            assertSame(getGuideLink(), helpWindow.getRoot().getScene().getFocusOwner());

            pressSpace(getGuideLink());
            assertEquals(List.of(EXPECTED_URL), requestedUrls);
            getCopyButton().requestFocus();
            assertCopyUrlWithKeyboard();
        });
    }

    @Test
    public void keyPressed_escapeFromEachControl_hidesAndAllowsReopening() {
        runOnFxThread(() -> {
            Stage originalStage = helpWindow.getRoot();
            for (Node target : List.of(getGuideLink(), getCopyButton(), originalStage.getScene().getRoot())) {
                helpWindow.show();
                target.requestFocus();
                AtomicBoolean wasKeyDelivered = new AtomicBoolean();
                target.setOnKeyPressed(event -> wasKeyDelivered.set(true));

                fireKey(target, KeyCode.ESCAPE, false);

                assertFalse(helpWindow.isShowing());
                assertFalse(wasKeyDelivered.get());
                helpWindow.show();
                assertTrue(helpWindow.isShowing());
                assertSame(originalStage, helpWindow.getRoot());
                target.setOnKeyPressed(null);
            }
            assertTrue(requestedUrls.isEmpty());
        });
    }

    @Test
    public void keyPressed_unrelatedKey_preservesWindowAndEvent() {
        runOnFxThread(() -> {
            AtomicBoolean wasKeyDelivered = new AtomicBoolean();
            getGuideLink().setOnKeyPressed(event -> wasKeyDelivered.set(true));

            fireKey(getGuideLink(), KeyCode.F2, false);

            assertTrue(wasKeyDelivered.get());
            assertTrue(helpWindow.isShowing());
            assertTrue(requestedUrls.isEmpty());
        });
    }

    @Test
    public void openUserGuide_launchThrows_showsFallbackAndKeepsCopyAndEscapeWorking() {
        runOnFxThread(() -> {
            helpWindow.hide();
            helpWindow = new HelpWindow(url -> {
                throw new IllegalStateException("Browser unavailable");
            });
            helpWindow.show();
        });
        runOnFxThread(() -> {
            getGuideLink().fire();

            assertTrue(helpWindow.isShowing());
            assertEquals(HelpWindow.MESSAGE_BROWSER_FAILURE, getLabel("#browserError").getText());
            assertTrue(getLabel("#browserError").isVisible());
            assertTrue(getLabel("#browserError").isManaged());
            assertCopyUrl();
            fireKey(getCopyButton(), KeyCode.ESCAPE, false);
            assertFalse(helpWindow.isShowing());
        });
    }

    @Test
    public void openUserGuide_retryAfterFailure_clearsStaleError() {
        runOnFxThread(() -> {
            helpWindow.hide();
            AtomicInteger launchAttempts = new AtomicInteger();
            helpWindow = new HelpWindow(url -> {
                if (launchAttempts.getAndIncrement() == 0) {
                    throw new SecurityException("Browser launch denied");
                }
                requestedUrls.add(url);
            });
            helpWindow.show();
        });
        runOnFxThread(() -> {
            getGuideLink().fire();
            assertTrue(getLabel("#browserError").isVisible());

            getGuideLink().fire();

            assertFalse(getLabel("#browserError").isVisible());
            assertFalse(getLabel("#browserError").isManaged());
            assertEquals(List.of(EXPECTED_URL), requestedUrls);
            assertTrue(helpWindow.isShowing());
        });
    }

    private Hyperlink getGuideLink() {
        return (Hyperlink) helpWindow.getRoot().getScene().lookup("#userGuideLink");
    }

    private Button getCopyButton() {
        return (Button) helpWindow.getRoot().getScene().lookup("#copyButton");
    }

    private Label getLabel(String selector) {
        return (Label) helpWindow.getRoot().getScene().lookup(selector);
    }

    private void assertCopyUrl() {
        assertClipboardAction(() -> getCopyButton().fire());
    }

    private void assertCopyUrlWithKeyboard() {
        assertClipboardAction(() -> pressSpace(getCopyButton()));
    }

    /**
     * Verifies copying and pasting, then restores the readable clipboard contents.
     */
    private void assertClipboardAction(Runnable copyAction) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        Map<DataFormat, Object> previousContents = new HashMap<>();
        for (DataFormat format : clipboard.getContentTypes()) {
            // Native clipboards can advertise formats whose contents JavaFX cannot read.
            Object content = clipboard.getContent(format);
            if (content != null) {
                previousContents.put(format, content);
            }
        }
        try {
            ClipboardContent initialContents = new ClipboardContent();
            initialContents.putString("Clipboard text before copying the guide");
            assertTrue(clipboard.setContent(initialContents));
            copyAction.run();
            assertEquals(EXPECTED_URL, clipboard.getString());
            TextField pasteTarget = new TextField();
            pasteTarget.paste();
            assertEquals(EXPECTED_URL, pasteTarget.getText());
        } finally {
            clipboard.setContent(previousContents);
        }
    }

    private static void pressSpace(Node target) {
        fireKey(target, KeyCode.SPACE, false);
        target.fireEvent(new KeyEvent(target, target, KeyEvent.KEY_RELEASED, "", "", KeyCode.SPACE,
                false, false, false, false));
    }

    private static void fireKey(Node target, KeyCode keyCode, boolean isShiftDown) {
        target.fireEvent(new KeyEvent(target, target, KeyEvent.KEY_PRESSED, "", "", keyCode,
                isShiftDown, false, false, false));
    }
}
