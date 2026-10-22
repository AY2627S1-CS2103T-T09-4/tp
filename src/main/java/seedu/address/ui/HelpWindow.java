package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;

/**
 * Controller for a help page
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-t09-4.github.io/tp/UserGuide.html";
    public static final String HELP_MESSAGE = "Refer to the user guide: " + USERGUIDE_URL;

    static final String MESSAGE_BROWSER_REQUESTED = "Browser launch requested. If nothing opens, use Copy URL.";
    static final String MESSAGE_BROWSER_FAILURE =
            "Could not launch the browser. Use Copy URL or enter the URL manually.";
    static final String MESSAGE_COPY_SUCCESS = "User Guide URL copied to the clipboard.";
    static final String MESSAGE_COPY_FAILURE = "Could not copy the URL. Try again or enter the URL in your browser.";

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";

    private final Consumer<String> openDocument;
    private final BooleanSupplier copyUrlToClipboard;

    @FXML
    private Label helpMessage;

    @FXML
    private ScrollPane helpScrollPane;

    @FXML
    private Label guideStatus;

    /**
     * Creates a help window using the supplied browser-opening action and the system clipboard.
     */
    public HelpWindow(Consumer<String> openDocument) {
        this(new Stage(), openDocument, HelpWindow::copyGuideUrlToClipboard);
    }

    /**
     * Creates a help window with replaceable external actions for testing.
     *
     * @param root Stage to use as the root of the help window.
     * @param openDocument Requests a browser launch for the supplied URL.
     * @param copyUrlToClipboard Copies {@link #USERGUIDE_URL} and reports whether the clipboard accepted it.
     */
    HelpWindow(Stage root, Consumer<String> openDocument, BooleanSupplier copyUrlToClipboard) {
        super(FXML, root);
        this.openDocument = requireNonNull(openDocument);
        this.copyUrlToClipboard = requireNonNull(copyUrlToClipboard);
        helpMessage.setText(HELP_MESSAGE);
        getRoot().getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    /**
     * Shows and focuses the help window, restoring it if minimized.
     * Must be called on the JavaFX Application Thread.
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        getRoot().centerOnScreen();
        focus();
        helpScrollPane.requestFocus();
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Restores and focuses the help window without changing its content or size.
     * Must be called on the JavaFX Application Thread.
     */
    public void focus() {
        getRoot().setIconified(false);
        getRoot().toFront();
        getRoot().requestFocus();
    }

    /** Closes the help window. */
    @FXML
    private void closeWindow() {
        hide();
    }

    /** Closes the help window when the Escape key is pressed. */
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            closeWindow();
            event.consume();
        }
    }

    /**
     * Requests that the browser open the User Guide, retaining manual fallback after a detectable failure.
     */
    @FXML
    private void openUserGuide() {
        try {
            openDocument.accept(USERGUIDE_URL);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Could not request a browser launch for the User Guide", e);
            showGuideStatus(MESSAGE_BROWSER_FAILURE);
            return;
        }
        showGuideStatus(MESSAGE_BROWSER_REQUESTED);
    }

    /**
     * Copies the User Guide URL and reports success only when the clipboard accepts it.
     */
    @FXML
    private void copyUrl() {
        boolean wasCopied;
        try {
            wasCopied = copyUrlToClipboard.getAsBoolean();
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Could not copy the User Guide URL to the clipboard", e);
            showGuideStatus(MESSAGE_COPY_FAILURE);
            return;
        }
        if (wasCopied) {
            showGuideStatus(MESSAGE_COPY_SUCCESS);
        } else {
            logger.warning("The clipboard did not accept the User Guide URL");
            showGuideStatus(MESSAGE_COPY_FAILURE);
        }
    }

    /** Shows action feedback without hiding the guide or disabling its controls. */
    private void showGuideStatus(String message) {
        guideStatus.setText(message);
        guideStatus.setManaged(true);
        guideStatus.setVisible(true);
    }

    /** Copies the User Guide URL and returns whether the system clipboard accepted it. */
    private static boolean copyGuideUrlToClipboard() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent url = new ClipboardContent();
        url.putString(USERGUIDE_URL);
        return clipboard.setContent(url);
    }
}
