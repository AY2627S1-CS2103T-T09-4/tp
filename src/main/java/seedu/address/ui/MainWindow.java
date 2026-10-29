package seedu.address.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.commands.exceptions.CommandSaveException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final PseudoClass KEYBOARD_FOCUSED_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("keyboard-focused");

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private PatientDetailsPanel patientDetailsPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;
    private CommandBox commandBox;
    private List<Node> mainSectionRoots = List.of();
    private List<Node> mainSectionFocusTargets = List.of();

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane patientDetailsPanelPlaceholder;

    @FXML
    private VBox personList;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * the data file path to show in the status bar, and a browser-opening action.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath, Consumer<String> openDocument) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow(openDocument);
        primaryStage.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> helpWindow.hide());
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add the following event filter to capture such key events and open
         * the help window purposely so as to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        patientDetailsPanel = new PatientDetailsPanel();
        patientDetailsPanelPlaceholder.getChildren().add(patientDetailsPanel.getRoot());

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        commandBox = new CommandBox(this::executeCommand, logic::isInputRequestPending);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
        configureKeyboardNavigation();
        helpWindow.getRoot().addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> {
            if (primaryStage.isShowing()) {
                primaryStage.requestFocus();
                commandBox.focus();
            }
        });
    }

    /** Configures the four-section keyboard cycle for the main window. */
    private void configureKeyboardNavigation() {
        mainSectionRoots = List.of(commandBoxPlaceholder, resultDisplayPlaceholder,
                personList, patientDetailsPanelPlaceholder);
        mainSectionFocusTargets = List.of(commandBox.getFocusTarget(), resultDisplay.getFocusTarget(),
                personListPanel.getFocusTarget(), patientDetailsPanel.getRoot());
        primaryStage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::handleTabNavigation);
        primaryStage.getScene().focusOwnerProperty().addListener((observable, oldFocusOwner, newFocusOwner) ->
                updateFocusedSection(newFocusOwner));
        updateFocusedSection(primaryStage.getScene().getFocusOwner());
    }

    /** Moves focus between the four main window sections when Tab or Shift+Tab is pressed. */
    private void handleTabNavigation(KeyEvent keyEvent) {
        if (keyEvent.getCode() != KeyCode.TAB) {
            return;
        }

        int currentSectionIndex = findFocusedSectionIndex();
        if (currentSectionIndex < 0) {
            return;
        }

        int direction = keyEvent.isShiftDown() ? -1 : 1;
        int nextSectionIndex = (currentSectionIndex + direction + mainSectionFocusTargets.size())
                % mainSectionFocusTargets.size();
        mainSectionFocusTargets.get(nextSectionIndex).requestFocus();
        keyEvent.consume();
    }

    /** Returns the index of the section containing the current focus owner, or -1 if there is none. */
    private int findFocusedSectionIndex() {
        Node focusOwner = primaryStage.getScene().getFocusOwner();
        for (int i = 0; i < mainSectionRoots.size(); i++) {
            if (isDescendantOf(focusOwner, mainSectionRoots.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /** Updates the focus outline on the one main-window section containing {@code focusOwner}. */
    private void updateFocusedSection(Node focusOwner) {
        for (int i = 0; i < mainSectionRoots.size(); i++) {
            boolean isFocused = isDescendantOf(focusOwner, mainSectionRoots.get(i));
            mainSectionRoots.get(i).pseudoClassStateChanged(KEYBOARD_FOCUSED_PSEUDO_CLASS, isFocused);
        }
    }

    /** Returns true if {@code node} is the given root or one of its descendants. */
    private boolean isDescendantOf(Node node, Node root) {
        Node currentNode = node;
        while (currentNode != null) {
            if (currentNode == root) {
                return true;
            }
            currentNode = currentNode.getParent();
        }
        return false;
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        commandBox.saveSelection();
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    PatientDetailsPanel getPatientDetailsPanel() {
        return patientDetailsPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            updatePatientDetailsPanel(commandResult);

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException e) {
            resultDisplay.setFeedbackToUser(e.getMessage());
            if (e instanceof CommandSaveException saveException) {
                updatePatientDetailsPanel(saveException.getCommandResult());
            }
            e.getPersonToView().ifPresent(patientDetailsPanel::setPerson);
            logger.info("An error occurred while executing command: " + commandText
                    + "; reason: " + e.getMessage()
                    + "; related patient displayed: " + e.getPersonToView().isPresent());
            throw e;
        } catch (ParseException e) {
            logger.info("An error occurred while parsing command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            throw e;
        }
    }

    /** Applies the details-panel action after an in-memory operation, even if saving it failed. */
    private void updatePatientDetailsPanel(CommandResult commandResult) {
        switch (commandResult.getDetailsPanelAction()) {
            case KEEP:
                break;
            case SHOW:
                patientDetailsPanel.setPerson(commandResult.getPersonToView().orElseThrow());
                break;
            case CLEAR:
                patientDetailsPanel.clear();
                break;
            case CLEAR_IF_SHOWING:
                commandResult.getPersonToClearFromView().ifPresent(patientDetailsPanel::clearIfShowing);
                break;
            default:
                throw new AssertionError("Unknown details-panel action: " + commandResult.getDetailsPanelAction());
        }
    }
}
