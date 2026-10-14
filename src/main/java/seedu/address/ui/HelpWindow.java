package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;

/**
 * Provides access to Doc's online User Guide and a clipboard fallback.
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-t09-4.github.io/tp/UserGuide.html";
    public static final String HELP_MESSAGE = "Refer to Doc's online User Guide:";
    public static final String MESSAGE_BROWSER_FAILURE =
            "Could not open the browser. Use Copy URL and paste the link into your browser.";

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";

    private final Consumer<String> browserOpener;

    @FXML
    private Hyperlink userGuideLink;

    @FXML
    private Label helpMessage;

    @FXML
    private Label browserError;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     * @param browserOpener Capability to request opening a URL in the default browser.
     */
    public HelpWindow(Stage root, Consumer<String> browserOpener) {
        super(FXML, root);
        this.browserOpener = requireNonNull(browserOpener);
        helpMessage.setText(HELP_MESSAGE);
        userGuideLink.setText(USERGUIDE_URL);
        getRoot().getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                hide();
                event.consume();
            }
        });
    }

    /**
     * Creates a new HelpWindow with the given browser-opening capability.
     */
    public HelpWindow(Consumer<String> browserOpener) {
        this(new Stage(), browserOpener);
    }

    /**
     * Shows and focuses the help window, initially focusing the guide link.
     * Must be called on the JavaFX application thread.
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        getRoot().centerOnScreen();
        focus();
        userGuideLink.requestFocus();
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
     * Restores and focuses the existing help window.
     */
    public void focus() {
        getRoot().setIconified(false);
        getRoot().toFront();
        getRoot().requestFocus();
    }

    /**
     * Requests the online guide in the default browser, keeping a copy fallback available on failure.
     */
    @FXML
    private void openUserGuide() {
        browserError.setVisible(false);
        browserError.setManaged(false);
        try {
            browserOpener.accept(USERGUIDE_URL);
        } catch (RuntimeException e) {
            // HostServices has no success result; only failures propagated by the launch call are observable here.
            logger.log(Level.WARNING, "Could not request the User Guide in the browser", e);
            browserError.setText(MESSAGE_BROWSER_FAILURE);
            browserError.setManaged(true);
            browserError.setVisible(true);
            getRoot().sizeToScene();
        }
    }

    /**
     * Copies the URL to the user guide to the clipboard.
     */
    @FXML
    private void copyUrl() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent url = new ClipboardContent();
        url.putString(USERGUIDE_URL);
        clipboard.setContent(url);
    }
}
