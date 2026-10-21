package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Desktop;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javafx.application.Platform;

/**
 * Provides shared JavaFX setup and execution helpers for UI tests.
 */
final class JavaFxTestUtil {

    private static final long FX_TIMEOUT_SECONDS = 10;

    private static boolean isJavaFxStarted;

    private JavaFxTestUtil() {
    }

    /**
     * Starts the JavaFX application thread once for all UI tests.
     */
    static synchronized void startJavaFx() throws InterruptedException {
        if (isJavaFxStarted) {
            return;
        }

        CountDownLatch startupLatch = new CountDownLatch(1);
        Platform.startup(startupLatch::countDown);
        assertTrue(startupLatch.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
        isJavaFxStarted = true;
    }

    /**
     * Requests native foreground access for focus-dependent tests when supported by the desktop.
     */
    static void requestForeground() {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.APP_REQUEST_FOREGROUND)) {
            desktop.requestForeground(true);
        }
    }

    /**
     * Runs an assertion block on the JavaFX application thread and propagates its failure to JUnit.
     */
    static void runOnFxThread(Runnable action) {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        try {
            task.get(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for the JavaFX application thread", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            if (e.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new AssertionError("JavaFX test action failed", e.getCause());
        } catch (TimeoutException e) {
            throw new AssertionError("JavaFX application thread did not respond", e);
        }
    }
}
