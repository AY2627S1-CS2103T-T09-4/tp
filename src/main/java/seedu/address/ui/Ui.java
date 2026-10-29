package seedu.address.ui;

import javafx.stage.Stage;

/**
 * API of the UI component
 */
public interface Ui {

    /** Starts the UI (and the App).  */
    void start(Stage primaryStage);

    /**
     * Shows a warning that must be seen at start-up, such as patient data that could not be loaded.
     *
     * @param message Explanation of the problem and what the user can do about it.
     */
    void showStartupWarning(String message);

}
