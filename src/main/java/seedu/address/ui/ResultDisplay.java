package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final double SMALL_SCROLL_PIXELS = 40.0;

    @FXML
    private TextArea resultDisplay;

    /** Creates a response panel that supports immediate keyboard scrolling. */
    public ResultDisplay() {
        super(FXML);
        resultDisplay.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    /** Returns the text area used as the response-panel focus stop. */
    Node getFocusTarget() {
        return resultDisplay;
    }

    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        resultDisplay.setText(feedbackToUser);
    }

    /** Scrolls response text immediately when an unmodified Up or Down key is pressed. */
    private void handleKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.isShiftDown() || keyEvent.isControlDown()
                || keyEvent.isAltDown() || keyEvent.isMetaDown()) {
            return;
        }

        switch (keyEvent.getCode()) {
            case UP -> scrollByPixels(-SMALL_SCROLL_PIXELS);
            case DOWN -> scrollByPixels(SMALL_SCROLL_PIXELS);
            default -> {
                return;
            }
        }
        keyEvent.consume();
    }

    /** Scrolls response text vertically by the specified number of pixels. */
    private void scrollByPixels(double pixels) {
        double targetScrollTop = Math.max(0, resultDisplay.getScrollTop() + pixels);
        resultDisplay.setScrollTop(targetScrollTop);
    }

}
