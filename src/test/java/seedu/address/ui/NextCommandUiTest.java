package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.LogicManagerIntegrationFixture;
import seedu.address.testutil.PersonBuilder;

public class NextCommandUiTest {

    @TempDir
    private Path testFolder;

    @BeforeAll
    public static void setUpJavaFx() throws InterruptedException {
        startJavaFx();
    }

    @Test
    public void execute_pastVisitCancellationAndConfirmation_keepsThenRefreshesDetails() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                verifyPastVisitFlow(stage);
            } catch (Exception e) {
                throw new AssertionError("Past-visit command flow failed", e);
            } finally {
                stage.close();
            }
        });
    }

    private void verifyPastVisitFlow(Stage stage) throws Exception {
        Person original = new PersonBuilder().withName("Alpha").withVisitDate("1/10/2026 0900")
                .withPreviousVisitDates("1/9/2026 0900").build();
        ModelManager model = new ModelManager();
        model.addPerson(original);
        Clock clock = Clock.fixed(Instant.parse("2026-10-29T00:00:00Z"), ZoneOffset.UTC);
        LogicManagerIntegrationFixture fixture = new LogicManagerIntegrationFixture(model, testFolder, clock);
        MainWindow window = new MainWindow(stage, fixture.getLogic(),
                fixture.getAddressBookStorage().getAddressBookFilePath(), url -> { });
        window.fillInnerParts();
        TextField commandBox = (TextField) stage.getScene().lookup("#commandTextField");
        VBox details = (VBox) ((ScrollPane) window.getPatientDetailsPanel().getRoot()).getContent();
        submit(commandBox, "view 1");
        List<String> before = getVisitDetails(details);

        assertPendingAndCancel(stage, commandBox, details, before, fixture, original);
        submit(commandBox, "next 1 15/10/2026 1000");
        submit(commandBox, " Y ");

        assertFalse(fixture.getLogic().isInputRequestPending());
        assertFalse(commandBox.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        assertTrue(getVisitDetails(details).getFirst().startsWith("Visit date: 15/10/2026 1000 ("));
        assertEquals("1/10/2026 0900\n1/9/2026 0900", getVisitDetails(details).get(1));
        Person expected = new PersonBuilder(original).withVisitDate("15/10/2026 1000")
                .withPreviousVisitDates("1/9/2026 0900", "1/10/2026 0900").build();
        fixture.assertPersistedPatients(expected);
        assertEquals(1, fixture.getSaveCount());
    }

    private void assertPendingAndCancel(Stage stage, TextField commandBox, VBox details, List<String> before,
            LogicManagerIntegrationFixture fixture, Person original) throws Exception {
        submit(commandBox, "next 1 15/10/2026 1000");
        String feedback = ((TextArea) stage.getScene().lookup("#resultDisplay")).getText();
        assertTrue(feedback.contains("not in the future: 15/10/2026 1000"));
        assertTrue(fixture.getLogic().isInputRequestPending());
        assertEquals(before, getVisitDetails(details));
        fixture.assertPersistedPatients(original);

        submit(commandBox, "n");

        assertFalse(fixture.getLogic().isInputRequestPending());
        assertEquals(before, getVisitDetails(details));
        fixture.assertPersistedPatients(original);
        assertEquals(0, fixture.getSaveCount());
    }

    private List<String> getVisitDetails(VBox details) {
        Label visit = (Label) details.lookup("#visitDate");
        Label history = (Label) details.lookup("#previousVisitDates");
        return List.of(visit.getText(), history.getText());
    }

    private void submit(TextField commandBox, String text) {
        commandBox.setText(text);
        commandBox.getOnAction().handle(new ActionEvent(commandBox, commandBox));
    }
}
