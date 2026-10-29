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
        TextField commandTextField = (TextField) stage.getScene().lookup("#commandTextField");
        VBox detailsContainer = (VBox) ((ScrollPane) window.getPatientDetailsPanel().getRoot()).getContent();
        submit(commandTextField, "view 1");
        List<String> visitDetailTextsBefore = getVisitDetails(detailsContainer);

        assertPendingAndCancel(stage, commandTextField, detailsContainer, visitDetailTextsBefore, fixture, original);
        submit(commandTextField, "next 1 15/10/2026 1000");
        submit(commandTextField, " Y ");

        assertFalse(fixture.getLogic().isInputRequestPending());
        assertFalse(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        assertTrue(getVisitDetails(detailsContainer).getFirst().startsWith("Visit date: 15/10/2026 1000 ("));
        assertEquals("1/10/2026 0900\n1/9/2026 0900", getVisitDetails(detailsContainer).get(1));
        Person expected = new PersonBuilder(original).withVisitDate("15/10/2026 1000")
                .withPreviousVisitDates("1/9/2026 0900", "1/10/2026 0900").build();
        fixture.assertPersistedPatients(expected);
        assertEquals(1, fixture.getSaveCount());
    }

    private void assertPendingAndCancel(Stage stage, TextField commandTextField, VBox detailsContainer,
            List<String> visitDetailTextsBefore, LogicManagerIntegrationFixture fixture, Person original)
            throws Exception {
        submit(commandTextField, "next 1 15/10/2026 1000");
        String feedback = ((TextArea) stage.getScene().lookup("#resultDisplay")).getText();
        assertTrue(feedback.contains("not in the future: 15/10/2026 1000"));
        assertTrue(fixture.getLogic().isInputRequestPending());
        assertEquals(visitDetailTextsBefore, getVisitDetails(detailsContainer));
        fixture.assertPersistedPatients(original);

        submit(commandTextField, "n");

        assertFalse(fixture.getLogic().isInputRequestPending());
        assertEquals(visitDetailTextsBefore, getVisitDetails(detailsContainer));
        fixture.assertPersistedPatients(original);
        assertEquals(0, fixture.getSaveCount());
    }

    private List<String> getVisitDetails(VBox detailsContainer) {
        Label visit = (Label) detailsContainer.lookup("#visitDate");
        Label history = (Label) detailsContainer.lookup("#previousVisitDates");
        return List.of(visit.getText(), history.getText());
    }

    private void submit(TextField commandTextField, String text) {
        commandTextField.setText(text);
        commandTextField.getOnAction().handle(new ActionEvent(commandTextField, commandTextField));
    }
}
