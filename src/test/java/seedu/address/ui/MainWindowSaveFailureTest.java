package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.JavaFxTestUtil.callOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class MainWindowSaveFailureTest {

    private static final Clock VALIDATION_CLOCK = Clock.fixed(Instant.parse("2026-10-29T00:00:00Z"), ZoneOffset.UTC);
    private static final Person ALPHA = new PersonBuilder().withName("Alpha")
            .withVisitDate("1/10/2026 0900").withPreviousVisitDates().build();
    private static final Person BETA = new PersonBuilder().withName("Beta")
            .withPhone("90000002").withVisitDate("2/10/2026 0900").withPreviousVisitDates().build();

    @TempDir
    private Path temporaryFolder;

    private SaveFailureFixture fixture;

    @BeforeAll
    public static void setUpJavaFx() throws InterruptedException {
        startJavaFx();
    }

    @BeforeEach
    public void setUp() {
        fixture = callOnFxThread(() -> new SaveFailureFixture(temporaryFolder));
        runOnFxThread(() -> fixture.submit("view 1"));
    }

    @AfterEach
    public void tearDown() {
        runOnFxThread(() -> fixture.stage.close());
    }

    @Test
    public void execute_nextAccessDeniedThenDelete_refreshesAndClearsDetails() {
        assertNextFailureThenDelete(new AccessDeniedException(fixture.storage.getAddressBookFilePath().toString()));
    }

    @Test
    public void execute_nextIoFailureThenDelete_refreshesAndClearsDetails() {
        assertNextFailureThenDelete(new IOException("Disk unavailable"));
    }

    @Test
    public void execute_addSaveFailure_showsAddedPatient() {
        runOnFxThread(() -> {
            Person added = new PersonBuilder().withName("Gamma").withPhone("90000003")
                    .withVisitDate("3/11/2026 0900").withPreviousVisitDates().build();
            fixture.storage.failure = new IOException("Disk unavailable");

            fixture.submit(PersonUtil.getAddCommand(added));

            fixture.assertSaveError();
            assertEquals(List.of(ALPHA, BETA, added), fixture.model.getAddressBook().getPersonList());
            fixture.assertDisplayedPerson(added);
            fixture.assertSavedPatients(ALPHA, BETA);
        });
    }

    @Test
    public void execute_editSaveFailure_showsUpdatedPatient() {
        runOnFxThread(() -> {
            Person edited = new PersonBuilder(ALPHA).withName("Renamed Alpha").build();
            fixture.storage.failure = new IOException("Disk unavailable");

            fixture.submit("edit 1 n/Renamed Alpha");

            fixture.assertSaveError();
            assertEquals(List.of(edited, BETA), fixture.model.getAddressBook().getPersonList());
            fixture.assertDisplayedPerson(edited);
            fixture.assertSavedPatients(ALPHA, BETA);
        });
    }

    @Test
    public void execute_prevSaveFailure_showsRestoredVisit() {
        runOnFxThread(() -> {
            fixture.submit("next 1 1/11/2026 1000");
            Person advanced = fixture.model.getAddressBook().getPersonList().getFirst();
            fixture.storage.failure = new IOException("Disk unavailable");

            fixture.submit("prev 1");
            fixture.submit("y");

            fixture.assertSaveError();
            assertEquals(List.of(ALPHA, BETA), fixture.model.getAddressBook().getPersonList());
            fixture.assertDisplayedPerson(ALPHA);
            fixture.assertSavedPatients(advanced, BETA);
        });
    }

    @Test
    public void execute_deleteSaveFailure_clearsOnlyDeletedPatientDetails() {
        runOnFxThread(() -> {
            fixture.storage.failure = new IOException("Disk unavailable");
            fixture.submit("delete 2");
            fixture.submit("y");

            fixture.assertSaveError();
            assertEquals(List.of(ALPHA), fixture.model.getAddressBook().getPersonList());
            fixture.assertDisplayedPerson(ALPHA);
            fixture.assertSavedPatients(ALPHA, BETA);

            fixture.submit("delete 1");
            fixture.submit("y");

            fixture.assertSaveError();
            assertTrue(fixture.model.getAddressBook().getPersonList().isEmpty());
            fixture.assertDetailsCleared();
            fixture.assertSavedPatients(ALPHA, BETA);
        });
    }

    @Test
    public void execute_clearSaveFailure_clearsDetails() {
        runOnFxThread(() -> {
            fixture.storage.failure = new IOException("Disk unavailable");
            fixture.submit("clear");
            fixture.submit("y");

            fixture.assertSaveError();
            assertTrue(fixture.model.getAddressBook().getPersonList().isEmpty());
            fixture.assertDetailsCleared();
            fixture.assertSavedPatients(ALPHA, BETA);
        });
    }

    @Test
    public void execute_listSaveFailure_keepsExistingDetailsAndScroll() {
        runOnFxThread(() -> {
            ScrollPane detailsPanel = (ScrollPane) fixture.mainWindow.getPatientDetailsPanel().getRoot();
            detailsPanel.setVvalue(detailsPanel.getVmax());
            fixture.storage.failure = new IOException("Disk unavailable");

            fixture.submit("list");

            fixture.assertSaveError();
            fixture.assertDisplayedPerson(ALPHA);
            assertEquals(detailsPanel.getVmax(), detailsPanel.getVvalue());
            fixture.assertSavedPatients(ALPHA, BETA);
        });
    }

    private void assertNextFailureThenDelete(IOException failure) {
        runOnFxThread(() -> {
            Person advanced = new PersonBuilder(ALPHA).withVisitDate("1/11/2026 1000")
                    .withPreviousVisitDates("1/10/2026 0900").build();
            fixture.storage.failure = failure;

            fixture.submit("next 1 1/11/2026 1000");

            fixture.assertSaveError();
            assertEquals(List.of(advanced, BETA), fixture.model.getAddressBook().getPersonList());
            fixture.assertDisplayedPerson(advanced);
            fixture.assertSavedPatients(ALPHA, BETA);

            fixture.storage.failure = null;
            fixture.submit("delete 1");
            fixture.submit("y");

            assertFalse(fixture.commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            assertEquals(List.of(BETA), fixture.model.getAddressBook().getPersonList());
            fixture.assertDetailsCleared();
            fixture.assertSavedPatients(BETA);
        });
    }

    /** Uses real command entry, logic, model, and JSON storage with controllable write failures. */
    private static class SaveFailureFixture {
        private final ModelManager model = new ModelManager();
        private final Stage stage = new Stage();
        private final FailingStorage storage;
        private final LogicManager logic;
        private final MainWindow mainWindow;
        private final TextField commandTextField;
        private final VBox detailsContainer;

        private SaveFailureFixture(Path testFolder) {
            model.addPerson(ALPHA);
            model.addPerson(BETA);
            storage = new FailingStorage(testFolder.resolve("addressbook.json"));
            try {
                storage.saveAddressBook(model.getAddressBook());
            } catch (IOException e) {
                throw new AssertionError("Could not save initial test data", e);
            }
            StorageManager storageManager = new StorageManager(storage,
                    new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
            logic = new LogicManager(model, storageManager, new AddressBookParser(VALIDATION_CLOCK));
            mainWindow = new MainWindow(stage, logic, storage.getAddressBookFilePath(), url -> { });
            mainWindow.fillInnerParts();
            commandTextField = (TextField) stage.getScene().lookup("#commandTextField");
            detailsContainer = (VBox) ((ScrollPane) mainWindow.getPatientDetailsPanel().getRoot()).getContent();
        }

        private void submit(String commandText) {
            commandTextField.setText(commandText);
            commandTextField.getOnAction().handle(new ActionEvent(commandTextField, commandTextField));
        }

        private void assertSaveError() {
            String expectedMessage = storage.failure instanceof AccessDeniedException
                    ? String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, storage.failure.getMessage())
                    : String.format(LogicManager.FILE_OPS_ERROR_FORMAT, storage.failure.getMessage());
            TextArea resultDisplay = (TextArea) stage.getScene().lookup("#resultDisplay");
            assertEquals(expectedMessage, resultDisplay.getText());
            assertTrue(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            assertFalse(logic.isInputRequestPending());
        }

        private void assertDisplayedPerson(Person person) {
            assertTrue(detailsContainer.isVisible());
            assertTrue(detailsContainer.isManaged());
            assertEquals(person.getName().fullName, getLabelText("#name"));
            assertTrue(getLabelText("#visitDate").startsWith("Visit date: " + person.getVisitDate() + " ("));
            String expectedHistory = person.getPreviousVisitDates().isEmpty() ? "No previous visit dates"
                    : person.getPreviousVisitDates().reversed().stream()
                            .map(VisitDate::toString).collect(Collectors.joining("\n"));
            assertEquals(expectedHistory, getLabelText("#previousVisitDates"));
        }

        private String getLabelText(String selector) {
            return ((Label) detailsContainer.lookup(selector)).getText();
        }

        private void assertDetailsCleared() {
            assertFalse(detailsContainer.isVisible());
            assertFalse(detailsContainer.isManaged());
        }

        private void assertSavedPatients(Person... persons) {
            ReadOnlyAddressBook saved = assertDoesNotThrow(() -> storage.readAddressBook().orElseThrow());
            assertEquals(List.of(persons), saved.getPersonList());
        }
    }

    /** Writes actual JSON unless a test requests a storage exception. */
    private static class FailingStorage extends JsonAddressBookStorage {
        private IOException failure;

        private FailingStorage(Path filePath) {
            super(filePath);
        }

        /**
         * Saves patient data or throws the configured failure before writing.
         */
        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }
}
