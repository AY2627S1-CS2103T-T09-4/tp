package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HelpWindowTest {

    private static final List<String> EXPECTED_KEYBOARD_HINT_PARTS = List.of("↑", "↓", "Scroll ·", "Tab", "/", "Shift",
            "+", "Tab", "Move focus ·", "Esc", "Close");
    private static final String NOTATION_LEGEND = "UPPERCASE: replace with your values · [ ]: optional · "
            + "...: repeatable";

    private static final List<List<String>> EXPECTED_PATIENT_AND_VISIT_ENTRIES = List.of(
            List.of("add — Add a patient.", "add n/NAME p/PHONE a/ADDRESS vd/DATETIME",
                    "[e/EMAIL] [ec/[NAME, ]PHONE_NUMBER] [dob/DATE] [bt/BLOOD_TYPE] [all/ALLERGIES] "
                            + "[alc/ALCOHOL_HISTORY] [smk/STATUS] [note/NOTES] [t/TAG]..."),
            List.of("edit — Update a patient's details. Provide at least one field.", "edit INDEX",
                    "[n/NAME] [p/PHONE] [a/ADDRESS] [vd/DATETIME] [e/EMAIL] [ec/[NAME, ]PHONE_NUMBER] "
                            + "[dob/DATE] [bt/BLOOD_TYPE] [all/ALLERGIES] [alc/ALCOHOL_HISTORY] "
                            + "[smk/STATUS] [note/NOTES] [t/TAG]..."),
            List.of("next — Schedule a patient's next visit.", "next INDEX DATETIME"),
            List.of("prev — Restore the latest previous visit date.", "prev INDEX"),
            List.of("delete — Delete a patient.", "delete INDEX"),
            List.of("clear — Remove all patients.", "clear"));

    private static final List<List<String>> EXPECTED_FINDING_AND_VIEWING_ENTRIES = List.of(
            List.of("list — Show all patients.", "list"),
            List.of("find — Find patients. Provide at least one field.", "find",
                    "[n/NAME_KEYWORD...] [p/PHONE_KEYWORD...] [a/ADDRESS_KEYWORD...] [t/TAG_KEYWORD...] "
                            + "[vd/DATE...] [note/NOTE_KEYWORD...]"),
            List.of("view — Show a patient's details.", "view INDEX"),
            List.of("sort — Sort displayed patients by visit date.", "sort date [START_DATE]"),
            List.of("agenda — Show visits chronologically for a date range.", "agenda START_DATE [DAYS]"));

    private static final List<List<String>> EXPECTED_APPLICATION_ENTRIES = List.of(
            List.of("help — Open this help window.", "help"),
            List.of("exit — Close Doc.", "exit"));

    @BeforeAll
    public static void setUpJavaFx() throws InterruptedException {
        startJavaFx();
    }

    @Test
    public void helpWindow_minimumSize_isResizableAndScrollable() {
        withHelpWindow(helpWindow -> {
            showAtMinimumSize(helpWindow);
            Stage stage = helpWindow.getRoot();
            assertTrue(stage.isResizable());
            assertEquals(Modality.NONE, stage.getModality());

            ScrollPane scrollPane = (ScrollPane) stage.getScene().lookup("#helpScrollPane");
            assertTrue(scrollPane.isFitToWidth());
            assertTrue(scrollPane.isPannable());
            assertTrue(scrollPane.isFocusTraversable());
            assertEquals(ScrollBarPolicy.NEVER, scrollPane.getHbarPolicy());
            assertEquals(ScrollBarPolicy.AS_NEEDED, scrollPane.getVbarPolicy());

            Label message = (Label) stage.getScene().lookup("#helpMessage");
            assertNotNull(message);
            assertTrue(message.isWrapText());
            assertEquals(HelpWindow.HELP_MESSAGE, message.getText());

            assertMinimumLayout(stage, scrollPane);
            findButton(stage, "#copyButton").fire();
            assertMinimumLayout(stage, scrollPane);
        });
    }

    @Test
    public void helpWindow_availableCommands_listsEachCommandOnceInGroups() {
        withHelpWindow(helpWindow -> {
            helpWindow.show();
            Parent root = helpWindow.getRoot().getScene().getRoot();
            assertEquals("Available commands", ((Label) root.lookup(".helpTitle")).getText());
            List<String> groupSelectors = List.of("#patientAndVisitCommands", "#findingAndViewingCommands",
                    "#applicationCommands");
            List<String> expectedHeadings = List.of("Manage patients and visits", "Find and view patients",
                    "Application controls");
            List<List<List<String>>> expectedEntries = List.of(EXPECTED_PATIENT_AND_VISIT_ENTRIES,
                    EXPECTED_FINDING_AND_VIEWING_ENTRIES, EXPECTED_APPLICATION_ENTRIES);
            for (int i = 0; i < groupSelectors.size(); i++) {
                VBox commandGroup = (VBox) root.lookup(groupSelectors.get(i));
                assertNotNull(commandGroup);
                assertEquals(expectedHeadings.get(i), ((Label) commandGroup.getChildren().getFirst()).getText());
                assertCommandGroup(commandGroup, expectedEntries.get(i));
            }

            int expectedCommandCount = EXPECTED_PATIENT_AND_VISIT_ENTRIES.size()
                    + EXPECTED_FINDING_AND_VIEWING_ENTRIES.size() + EXPECTED_APPLICATION_ENTRIES.size();
            assertEquals(expectedCommandCount, root.lookupAll(".helpCommand").size());
        });
    }

    @Test
    public void helpWindow_keyboardHints_displaysNonInteractiveKeyCaps() {
        withHelpWindow(helpWindow -> {
            helpWindow.show();
            FlowPane keyboardHint = (FlowPane) helpWindow.getRoot().getScene().lookup("#keyboardHint");
            List<Label> keyboardHintLabels = keyboardHint.getChildren().stream()
                    .map(Label.class::cast)
                    .toList();

            assertEquals(EXPECTED_KEYBOARD_HINT_PARTS, keyboardHintLabels.stream().map(Label::getText).toList());
            assertFalse(keyboardHint.isFocusTraversable());
            assertTrue(keyboardHintLabels.stream()
                    .filter(label -> label.getStyleClass().contains("helpKeyCap"))
                    .allMatch(label -> !label.isFocusTraversable() && label.isMouseTransparent()));
        });
    }

    @Test
    public void helpWindow_notationLegend_explainsSyntaxBeforeCommandGroups() {
        withHelpWindow(helpWindow -> {
            helpWindow.show();
            Parent root = helpWindow.getRoot().getScene().getRoot();
            ScrollPane scrollPane = (ScrollPane) root.lookup("#helpScrollPane");
            VBox content = (VBox) scrollPane.getContent();
            FlowPane keyboardHint = (FlowPane) content.lookup("#keyboardHint");
            TextFlow notationLegend = (TextFlow) content.lookup("#notationLegend");

            assertEquals(NOTATION_LEGEND, getTextFlowText(notationLegend));
            assertTrue(notationLegend.getChildren().stream().allMatch(Text.class::isInstance));
            assertEquals(content.getChildren().indexOf(keyboardHint) + 1,
                    content.getChildren().indexOf(notationLegend));
            assertTrue(content.getChildren().indexOf(notationLegend)
                    < content.getChildren().indexOf(root.lookup("#patientAndVisitCommands")));
        });
    }

    @Test
    public void helpWindow_defaultSize_focusesScrollableCommands() {
        withHelpWindow(helpWindow -> {
            helpWindow.show();
            Stage stage = helpWindow.getRoot();
            Parent root = stage.getScene().getRoot();
            ScrollPane scrollPane = (ScrollPane) root.lookup("#helpScrollPane");

            assertSame(scrollPane, stage.getScene().getFocusOwner());
            root.applyCss();
            root.layout();
            assertTrue(scrollPane.getContent().getBoundsInLocal().getHeight()
                    > scrollPane.getViewportBounds().getHeight(), "The default window should allow scrolling");
            assertEquals(ScrollBarPolicy.NEVER, scrollPane.getHbarPolicy());
        });
    }

    @Test
    public void helpWindow_minimumSize_arrowKeysScrollCommands() {
        withHelpWindow(helpWindow -> {
            showAtMinimumSize(helpWindow);
            Stage stage = helpWindow.getRoot();
            ScrollPane scrollPane = (ScrollPane) stage.getScene().lookup("#helpScrollPane");
            assertMinimumLayout(stage, scrollPane);
            assertSame(scrollPane, stage.getScene().getFocusOwner());

            double positionBeforeDown = scrollPane.getVvalue();
            fireKeyPressed(scrollPane, KeyCode.DOWN);
            assertTrue(scrollPane.getVvalue() > positionBeforeDown, "Down should scroll towards later commands");

            double positionBeforeUp = scrollPane.getVvalue();
            fireKeyPressed(scrollPane, KeyCode.UP);
            assertTrue(scrollPane.getVvalue() < positionBeforeUp, "Up should scroll towards earlier commands");
        });
    }

    @Test
    public void helpWindow_minimumSize_wrapsTemplatesAndReachesFinalCommand() {
        withHelpWindow(helpWindow -> {
            showAtMinimumSize(helpWindow);
            Parent root = helpWindow.getRoot().getScene().getRoot();
            ScrollPane scrollPane = (ScrollPane) root.lookup("#helpScrollPane");
            root.applyCss();
            root.layout();

            List<Label> syntaxLabels = root.lookupAll(".helpCommandSyntax").stream()
                    .map(Label.class::cast)
                    .toList();
            List<FlowPane> continuationPanes = root.lookupAll(".helpCommandContinuation").stream()
                    .map(FlowPane.class::cast)
                    .toList();
            assertEquals(13, syntaxLabels.size());
            assertEquals(3, continuationPanes.size());
            assertTrue(syntaxLabels.stream().allMatch(Label::isWrapText));
            assertTrue(continuationPanes.stream().allMatch(pane -> pane.getChildren().stream()
                    .allMatch(Label.class::isInstance)));
            double availableWidth = scrollPane.getViewportBounds().getWidth();
            assertTemplateLabelsFit(syntaxLabels, availableWidth);
            assertTemplatePanesFit(continuationPanes, availableWidth);
            assertFinalCommandVisible(root, scrollPane);
        });
    }

    @Test
    public void helpWindow_closeButton_closesWindow() {
        // EP: A shown Help stage transitions to hidden through its real Close button.
        withHelpWindow(helpWindow -> {
            helpWindow.show();
            Button closeButton = findButton(helpWindow.getRoot(), "#closeButton");
            assertTrue(closeButton.isFocusTraversable());
            closeButton.fire();
            assertFalse(helpWindow.isShowing());
        });
    }

    @Test
    public void helpWindow_escapeKey_closesWindow() {
        withHelpWindow(helpWindow -> {
            helpWindow.show();
            fireKeyPressed(helpWindow.getRoot().getScene().getRoot(), KeyCode.ESCAPE);
            assertFalse(helpWindow.isShowing());
        });
    }

    @Test
    public void show_minimizedWindow_restoresWindow() {
        // EP: Showing an iconified Help stage must restore it, rather than leave it minimized.
        withHelpWindow(helpWindow -> {
            Stage stage = helpWindow.getRoot();
            helpWindow.show();
            stage.setIconified(true);
            assertTrue(stage.isIconified());

            helpWindow.show();

            assertTrue(helpWindow.isShowing());
            assertFalse(stage.isIconified());
            assertSame(stage.getScene().lookup("#helpScrollPane"), stage.getScene().getFocusOwner());
        });
    }

    /** Runs assertions with a help window whose stage is always closed, including after assertion failures. */
    private void withHelpWindow(Consumer<HelpWindow> assertions) {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                HelpWindow helpWindow = new HelpWindow(stage, url -> { }, () -> false);
                assertions.accept(helpWindow);
            } finally {
                stage.close();
            }
        });
    }

    /** Shows the help window at its minimum supported dimensions. */
    private void showAtMinimumSize(HelpWindow helpWindow) {
        Stage stage = helpWindow.getRoot();
        stage.setWidth(stage.getMinWidth());
        stage.setHeight(stage.getMinHeight());
        helpWindow.show();
    }

    /** Checks that scrolling to the bottom makes the last command reachable. */
    private void assertFinalCommandVisible(Parent root, ScrollPane scrollPane) {
        scrollPane.setVvalue(1.0);
        root.layout();
        Node finalCommand = findCommandEntry((VBox) root.lookup("#applicationCommands"), "exit");
        Bounds viewportBounds = scrollPane.localToScene(scrollPane.getBoundsInLocal());
        Bounds finalCommandBounds = finalCommand.localToScene(finalCommand.getBoundsInLocal());
        String visibilityMessage = "The final command should be reachable at the minimum size: final="
                + finalCommandBounds + ", viewport=" + viewportBounds;
        assertTrue(finalCommandBounds.getMaxY() >= viewportBounds.getMinY()
                        && finalCommandBounds.getMinY() <= viewportBounds.getMaxY(), visibilityMessage);
    }

    private void fireKeyPressed(Node target, KeyCode keyCode) {
        target.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode, false, false, false, false));
    }

    private Button findButton(Stage stage, String selector) {
        Node button = stage.getScene().lookup(selector);
        assertNotNull(button);
        return (Button) button;
    }

    /** Checks that commands retain most of the small window, including when action feedback is shown. */
    private void assertMinimumLayout(Stage stage, ScrollPane scrollPane) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        assertTrue(scrollPane.getViewportBounds().getHeight() >= stage.getScene().getHeight() / 2,
                "Commands should occupy at least half of the minimum-size window");
        assertTrue(scrollPane.getContent().getBoundsInLocal().getHeight()
                > scrollPane.getViewportBounds().getHeight());
        Button closeButton = findButton(stage, "#closeButton");
        assertTrue(closeButton.localToScene(closeButton.getBoundsInLocal()).getMaxY()
                <= stage.getScene().getHeight());
        assertTrue(closeButton.localToScene(closeButton.getBoundsInLocal()).getMinY()
                >= scrollPane.localToScene(scrollPane.getBoundsInLocal()).getMaxY());
    }

    /** Checks displayed command entries, their order, descriptions, and syntax templates. */
    private void assertCommandGroup(VBox group, List<List<String>> expectedEntries) {
        assertNotNull(group);
        List<VBox> actualEntries = group.getChildren().stream()
                .filter(VBox.class::isInstance)
                .map(VBox.class::cast)
                .filter(entry -> entry.getStyleClass().contains("helpCommand"))
                .toList();
        assertEquals(expectedEntries.size(), actualEntries.size());
        List<String> commandNames = new ArrayList<>();
        for (int i = 0; i < expectedEntries.size(); i++) {
            commandNames.add(assertCommandEntry(actualEntries.get(i), expectedEntries.get(i)));
        }
        assertEquals(expectedEntries.stream().map(entry -> entry.getFirst().split(" — ", 2)[0]).toList(), commandNames);
    }

    /** Checks one command's displayed text and styles, returning its command word. */
    private String assertCommandEntry(VBox entry, List<String> expectedTextParts) {
        TextFlow description = (TextFlow) entry.getChildren().getFirst();
        List<Label> syntaxLabels = entry.getChildren().stream()
                .skip(1)
                .filter(Label.class::isInstance)
                .map(Label.class::cast)
                .toList();
        List<FlowPane> continuationPanes = entry.getChildren().stream()
                .skip(1)
                .filter(FlowPane.class::isInstance)
                .map(FlowPane.class::cast)
                .toList();
        List<String> actualTextParts = new ArrayList<>();
        actualTextParts.add(getTextFlowText(description));
        actualTextParts.addAll(syntaxLabels.stream().map(Label::getText).toList());
        actualTextParts.addAll(continuationPanes.stream().map(this::getFlowPaneText).toList());
        assertEquals(expectedTextParts, actualTextParts);
        assertCommandSyntax(syntaxLabels, continuationPanes);
        return assertCommandDescription(description);
    }

    /** Checks a styled, nonempty command description and returns its command word. */
    private String assertCommandDescription(TextFlow description) {
        assertTrue(description.getStyleClass().contains("helpCommandDescription"));
        assertTrue(description.getChildren().stream().allMatch(Text.class::isInstance));
        assertTrue(((Text) description.getChildren().getFirst()).getStyleClass().contains("helpCommandName"));
        String[] commandParts = getTextFlowText(description).split(" — ", 2);
        assertEquals(2, commandParts.length);
        assertFalse(commandParts[1].isBlank());
        return commandParts[0];
    }

    /** Checks syntax wrapping, continuation containers, and non-wrapping tokens. */
    private void assertCommandSyntax(List<Label> syntaxLabels, List<FlowPane> continuationPanes) {
        assertTrue(syntaxLabels.stream().allMatch(label -> label.getStyleClass().contains("helpCommandSyntax")
                && label.isWrapText()));
        assertTrue(continuationPanes.stream().allMatch(pane -> pane.getStyleClass()
                .contains("helpCommandContinuation")));
        assertTrue(continuationPanes.stream().flatMap(pane -> pane.getChildren().stream())
                .map(Label.class::cast)
                .allMatch(label -> label.getStyleClass().contains("helpCommandToken") && !label.isWrapText()));
    }

    private Node findCommandEntry(VBox group, String commandWord) {
        return group.getChildren().stream()
                .filter(VBox.class::isInstance)
                .map(VBox.class::cast)
                .filter(entry -> entry.getStyleClass().contains("helpCommand"))
                .filter(entry -> getTextFlowText((TextFlow) entry.getChildren().getFirst())
                        .startsWith(commandWord + " — "))
                .findFirst()
                .orElseThrow();
    }

    private void assertTemplateLabelsFit(List<Label> labels, double availableWidth) {
        assertTrue(labels.stream().allMatch(label -> label.getWidth() > 0
                && label.getWidth() <= availableWidth));
    }

    private void assertTemplatePanesFit(List<FlowPane> panes, double availableWidth) {
        assertTrue(panes.stream().allMatch(pane -> pane.getWidth() > 0 && pane.getWidth() <= availableWidth));
        assertTrue(panes.stream().flatMap(pane -> pane.getChildren().stream())
                .map(Label.class::cast)
                .allMatch(label -> label.getWidth() > 0 && label.getWidth() <= availableWidth));
    }

    private String getFlowPaneText(FlowPane pane) {
        return String.join(" ", pane.getChildren().stream()
                .map(Label.class::cast)
                .map(Label::getText)
                .toList());
    }

    private String getTextFlowText(TextFlow textFlow) {
        return textFlow.getChildren().stream()
                .map(Text.class::cast)
                .map(Text::getText)
                .reduce("", String::concat);
    }
}
