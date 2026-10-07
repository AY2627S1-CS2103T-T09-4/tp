package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.exceptions.ParseException;

public class CommandBoxTest {

    private static final long FX_TIMEOUT_SECONDS = 10;

    @BeforeAll
    public static void startJavaFx() throws InterruptedException {
        CountDownLatch startupLatch = new CountDownLatch(1);
        Platform.startup(startupLatch::countDown);
        assertTrue(startupLatch.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    @AfterAll
    public static void stopJavaFx() {
        Platform.exit();
    }

    @Test
    public void shouldSubmit_emptyTextWithoutRequest_returnsFalse() {
        assertFalse(CommandBox.shouldSubmit("", false));
    }

    @Test
    public void shouldSubmit_emptyTextWithRequest_returnsTrue() {
        assertTrue(CommandBox.shouldSubmit("", true));
    }

    @Test
    public void shouldSubmit_nonEmptyText_returnsTrue() {
        assertTrue(CommandBox.shouldSubmit("list", false));
    }

    @Test
    public void commandEntry_successfulCommand_recordsAndClearsInput() {
        runOnFxThread(() -> {
            List<String> executedCommands = new ArrayList<>();
            CommandBox commandBox = new CommandBox(command -> {
                executedCommands.add(command);
                return new CommandResult("success");
            });
            TextField commandTextField = getCommandTextField(commandBox);

            commandTextField.setText("list");
            fireCommandEntered(commandTextField);

            assertEquals(List.of("list"), executedCommands);
            assertEquals("", commandTextField.getText());

            commandTextField.setText("unfinished command");
            KeyEvent upEvent = fireKeyPressed(commandTextField, KeyCode.UP);
            assertEquals("list", commandTextField.getText());
            assertEquals("list".length(), commandTextField.getCaretPosition());
            assertTrue(upEvent.isConsumed());

            KeyEvent downEvent = fireKeyPressed(commandTextField, KeyCode.DOWN);
            assertEquals("unfinished command", commandTextField.getText());
            assertEquals("unfinished command".length(), commandTextField.getCaretPosition());
            assertTrue(downEvent.isConsumed());
        });
    }

    @Test
    public void commandEntry_emptyCommandWithoutRequest_doesNotExecute() {
        runOnFxThread(() -> {
            List<String> executedCommands = new ArrayList<>();
            CommandBox commandBox = new CommandBox(command -> {
                executedCommands.add(command);
                return new CommandResult("success");
            });
            TextField commandTextField = getCommandTextField(commandBox);

            fireCommandEntered(commandTextField);

            assertTrue(executedCommands.isEmpty());
            assertEquals("", commandTextField.getText());
        });
    }

    @Test
    public void commandEntry_failedCommand_recordsAndRetainsInput() {
        runOnFxThread(() -> {
            CommandBox commandBox = new CommandBox(command -> {
                throw new ParseException("invalid command");
            });
            TextField commandTextField = getCommandTextField(commandBox);

            commandTextField.setText("invalid");
            fireCommandEntered(commandTextField);

            assertEquals("invalid", commandTextField.getText());
            assertTrue(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

            fireCommandEntered(commandTextField);
            long errorStyleCount = commandTextField.getStyleClass().stream()
                    .filter(CommandBox.ERROR_STYLE_CLASS::equals)
                    .count();
            assertEquals(1, errorStyleCount);

            commandTextField.setText("");
            fireKeyPressed(commandTextField, KeyCode.UP);
            assertEquals("invalid", commandTextField.getText());
        });
    }

    @Test
    public void commandEntry_followUpInput_excludesResponseFromHistory() {
        runOnFxThread(() -> {
            AtomicBoolean isInputRequestPending = new AtomicBoolean();
            List<String> executedInputs = new ArrayList<>();
            CommandBox commandBox = new CommandBox(command -> {
                executedInputs.add(command);
                return new CommandResult("success");
            }, isInputRequestPending::get);
            TextField commandTextField = getCommandTextField(commandBox);

            commandTextField.setText("delete 1");
            fireCommandEntered(commandTextField);
            isInputRequestPending.set(true);
            commandTextField.setText("y");
            fireCommandEntered(commandTextField);
            isInputRequestPending.set(false);

            fireKeyPressed(commandTextField, KeyCode.UP);
            assertEquals(List.of("delete 1", "y"), executedInputs);
            assertEquals("delete 1", commandTextField.getText());
        });
    }

    @Test
    public void keyPressed_nonHistoryKey_preservesInputAndEvent() {
        runOnFxThread(() -> {
            CommandBox commandBox = new CommandBox(command -> new CommandResult("success"));
            TextField commandTextField = getCommandTextField(commandBox);
            commandTextField.setText("draft");

            KeyEvent leftEvent = fireKeyPressed(commandTextField, KeyCode.LEFT);

            assertEquals("draft", commandTextField.getText());
            assertFalse(leftEvent.isConsumed());
        });
    }

    /**
     * Returns the command text field loaded from the production FXML file.
     */
    private static TextField getCommandTextField(CommandBox commandBox) {
        TextField commandTextField = (TextField) commandBox.getRoot().lookup("#commandTextField");
        assertNotNull(commandTextField);
        assertNotNull(commandTextField.getOnAction());
        assertNotNull(commandTextField.getOnKeyPressed());
        return commandTextField;
    }

    /**
     * Fires the action handler installed by the production FXML file.
     */
    private static void fireCommandEntered(TextField commandTextField) {
        commandTextField.getOnAction().handle(new ActionEvent(commandTextField, commandTextField));
    }

    /**
     * Fires the key handler installed by the production FXML file and returns the event for consumption checks.
     */
    private static KeyEvent fireKeyPressed(TextField commandTextField, KeyCode keyCode) {
        KeyEvent keyEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode, false, false, false, false);
        commandTextField.getOnKeyPressed().handle(keyEvent);
        return keyEvent;
    }

    /**
     * Runs an assertion block on the JavaFX application thread and propagates its failure to JUnit.
     */
    private static void runOnFxThread(Runnable action) {
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
