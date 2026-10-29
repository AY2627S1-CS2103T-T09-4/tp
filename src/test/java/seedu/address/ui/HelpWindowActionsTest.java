package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HelpWindowActionsTest {

    @BeforeAll
    public static void setUpJavaFx() throws InterruptedException {
        startJavaFx();
    }

    @Test
    public void openUserGuide_browserAcceptsRequest_reportsRequestWithoutClaimingPageLoaded() {
        // External browser calls are replaced with a recording action; acceptance does not prove page loading.
        List<String> requestedUrls = new ArrayList<>();
        withHelpWindow(requestedUrls::add, () -> true, stage -> {
            assertFalse(stage.getScene().lookup("#guideStatus").isVisible());

            findButton(stage, "#openGuideButton").fire();

            assertEquals(List.of(HelpWindow.USER_GUIDE_URL), requestedUrls);
            assertStatus(stage, HelpWindow.MESSAGE_BROWSER_REQUESTED);
        });
    }

    @Test
    public void openUserGuide_browserThrows_keepsCopyFallbackUsable() {
        withHelpWindow(url -> {
            throw new SecurityException("Browser access denied");
        }, () -> true, stage -> {
            findButton(stage, "#openGuideButton").fire();
            assertStatus(stage, HelpWindow.MESSAGE_BROWSER_FAILURE);

            findButton(stage, "#copyButton").fire();
            assertStatus(stage, HelpWindow.MESSAGE_COPY_SUCCESS);
        });
    }

    @Test
    public void openUserGuide_retryAfterFailure_updatesFeedback() {
        AtomicInteger attempts = new AtomicInteger();
        withHelpWindow(url -> {
            if (attempts.getAndIncrement() == 0) {
                throw new UnsupportedOperationException("Browser unavailable");
            }
        }, () -> true, stage -> {
            findButton(stage, "#openGuideButton").fire();
            assertStatus(stage, HelpWindow.MESSAGE_BROWSER_FAILURE);

            findButton(stage, "#openGuideButton").fire();
            assertStatus(stage, HelpWindow.MESSAGE_BROWSER_REQUESTED);
            assertEquals(2, attempts.get());
        });
    }

    @Test
    public void copyUrl_clipboardAccepts_reportsSuccessWithoutOpeningBrowser() {
        // Native clipboard access is replaced with a fake action; success does not prove system delivery.
        AtomicInteger copies = new AtomicInteger();
        withHelpWindow(url -> fail("Copy URL must not open the browser"), () -> {
            copies.incrementAndGet();
            return true;
        }, stage -> {
            findButton(stage, "#copyButton").fire();

            assertEquals(1, copies.get());
            assertStatus(stage, HelpWindow.MESSAGE_COPY_SUCCESS);
        });
    }

    @Test
    public void copyUrl_clipboardRejects_reportsFailureAndKeepsBrowserFallbackUsable() {
        List<String> requestedUrls = new ArrayList<>();
        withHelpWindow(requestedUrls::add, () -> false, stage -> {
            findButton(stage, "#copyButton").fire();
            assertStatus(stage, HelpWindow.MESSAGE_COPY_FAILURE);
            assertTrue(requestedUrls.isEmpty());

            findButton(stage, "#openGuideButton").fire();
            assertEquals(List.of(HelpWindow.USER_GUIDE_URL), requestedUrls);
            assertStatus(stage, HelpWindow.MESSAGE_BROWSER_REQUESTED);
        });
    }

    @Test
    public void copyUrl_clipboardThrows_allowsRetry() {
        AtomicInteger attempts = new AtomicInteger();
        withHelpWindow(url -> fail("Copy URL must not open the browser"), () -> {
            if (attempts.getAndIncrement() == 0) {
                throw new IllegalStateException("Clipboard unavailable");
            }
            return true;
        }, stage -> {
            findButton(stage, "#copyButton").fire();
            assertStatus(stage, HelpWindow.MESSAGE_COPY_FAILURE);

            findButton(stage, "#copyButton").fire();
            assertStatus(stage, HelpWindow.MESSAGE_COPY_SUCCESS);
            assertEquals(2, attempts.get());
        });
    }

    @Test
    public void constructor_nullExternalActions_rejectsDependencies() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                assertThrows(NullPointerException.class, () -> new HelpWindow(stage, null, () -> true));
                assertThrows(NullPointerException.class, () -> new HelpWindow(stage, url -> { }, null));
            } finally {
                stage.close();
            }
        });
    }

    private void withHelpWindow(Consumer<String> openDocument, BooleanSupplier copyUrl, Consumer<Stage> assertions) {
        runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow(new Stage(), openDocument, copyUrl);
            Stage stage = helpWindow.getRoot();
            try {
                helpWindow.show();
                assertions.accept(stage);
                findButton(stage, "#closeButton").fire();
                assertFalse(stage.isShowing());
            } finally {
                stage.close();
            }
        });
    }

    private void assertStatus(Stage stage, String expectedMessage) {
        Label status = (Label) stage.getScene().lookup("#guideStatus");
        assertEquals(expectedMessage, status.getText());
        assertTrue(status.isVisible());
        assertTrue(status.isManaged());
        assertTrue(status.isWrapText());
        assertTrue(stage.isShowing());
        assertEquals(HelpWindow.HELP_MESSAGE, ((Label) stage.getScene().lookup("#helpMessage")).getText());
        assertFalse(findButton(stage, "#openGuideButton").isDisabled());
        assertFalse(findButton(stage, "#copyButton").isDisabled());
    }

    private Button findButton(Stage stage, String selector) {
        return (Button) stage.getScene().lookup(selector);
    }
}
