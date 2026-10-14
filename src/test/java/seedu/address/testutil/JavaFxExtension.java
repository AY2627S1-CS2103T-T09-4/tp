package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javafx.application.Platform;

/**
 * Shares one JavaFX toolkit across UI test classes and shuts it down after the test run.
 */
public class JavaFxExtension implements BeforeAllCallback {

    private static final long FX_TIMEOUT_SECONDS = 10;

    /**
     * Starts JavaFX once for the root JUnit context, keeping it alive between UI test classes.
     */
    @Override
    public void beforeAll(ExtensionContext context) {
        context.getRoot().getStore(ExtensionContext.Namespace.create(JavaFxExtension.class))
                .getOrComputeIfAbsent(Toolkit.class, key -> new Toolkit(), Toolkit.class);
    }

    /**
     * Runs an assertion block on the JavaFX application thread and propagates its failure to JUnit.
     */
    public static void runOnFxThread(Runnable action) {
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

    /**
     * Owns the toolkit for the lifetime of the root JUnit context.
     */
    private static class Toolkit implements AutoCloseable {

        Toolkit() {
            CountDownLatch startupLatch = new CountDownLatch(1);
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                startupLatch.countDown();
            });
            try {
                assertTrue(startupLatch.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Interrupted while starting JavaFX", e);
            }
        }

        /**
         * Shuts JavaFX down after all test classes have finished.
         */
        @Override
        public void close() {
            Platform.exit();
        }
    }
}
