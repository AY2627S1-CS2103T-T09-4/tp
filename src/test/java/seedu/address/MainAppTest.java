package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.stage.Stage;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;

public class MainAppTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");
    private static final LocalDateTime START_TIME = LocalDateTime.of(2026, 10, 29, 15, 30, 5);

    @TempDir
    public Path testFolder;

    @Test
    public void resolveDataFilePath_filesMissing_usesDocPath() {
        Path docDataFile = testFolder.resolve("doc.json");
        Path legacyDataFile = testFolder.resolve("addressbook.json");

        MainApp.DataFileResolution resolution = MainApp.resolveDataFilePath(docDataFile, legacyDataFile);

        assertEquals(docDataFile, resolution.dataFilePath());
        assertTrue(resolution.warning().isEmpty());
    }

    @Test
    public void resolveDataFilePath_legacyFileOnly_movesToDocPath() throws Exception {
        Path docDataFile = testFolder.resolve("doc.json");
        Path legacyDataFile = testFolder.resolve("addressbook.json");
        Files.writeString(legacyDataFile, "legacy patient data");

        MainApp.DataFileResolution resolution = MainApp.resolveDataFilePath(docDataFile, legacyDataFile);

        assertEquals(docDataFile, resolution.dataFilePath());
        assertTrue(resolution.warning().isEmpty());
        assertEquals("legacy patient data", Files.readString(docDataFile));
        assertFalse(Files.exists(legacyDataFile));
    }

    @Test
    public void resolveDataFilePath_bothFilesExist_usesDocAndPreservesLegacy() throws Exception {
        Path docDataFile = testFolder.resolve("doc.json");
        Path legacyDataFile = testFolder.resolve("addressbook.json");
        Files.writeString(docDataFile, "current patient data");
        Files.writeString(legacyDataFile, "legacy patient data");

        MainApp.DataFileResolution resolution = MainApp.resolveDataFilePath(docDataFile, legacyDataFile);

        assertEquals(docDataFile, resolution.dataFilePath());
        assertTrue(resolution.warning().isEmpty());
        assertEquals("current patient data", Files.readString(docDataFile));
        assertEquals("legacy patient data", Files.readString(legacyDataFile));
    }

    @Test
    public void resolveDataFilePath_migrationFails_usesLegacyAndWarns() throws Exception {
        Path legacyDataFile = testFolder.resolve("addressbook.json");
        Path invalidParent = testFolder.resolve("not-a-directory");
        Path docDataFile = invalidParent.resolve("doc.json");
        Files.writeString(legacyDataFile, "legacy patient data");
        Files.writeString(invalidParent, "blocks the destination directory");

        MainApp.DataFileResolution resolution = MainApp.resolveDataFilePath(docDataFile, legacyDataFile);

        assertEquals(legacyDataFile, resolution.dataFilePath());
        assertEquals("legacy patient data", Files.readString(legacyDataFile));
        String warning = resolution.warning().orElseThrow();
        assertTrue(warning.contains("could not rename"), warning);
        assertTrue(warning.contains(legacyDataFile.toString()), warning);
        assertTrue(warning.contains(docDataFile.toString()), warning);
    }

    @Test
    public void loadInitialData_missingFile_usesSampleDataWithoutWarning() {
        MainApp.InitialData initialData = MainApp.loadInitialData(createStorage(dataFile()), START_TIME);

        assertEquals(new AddressBook(SampleDataUtil.getSampleAddressBook()), initialData.addressBook());
        assertTrue(initialData.warning().isEmpty());
    }

    @Test
    public void loadInitialData_validFile_loadsDataWithoutWarning() throws Exception {
        StorageManager storage = createStorage(dataFile());
        storage.saveAddressBook(getTypicalAddressBook());

        MainApp.InitialData initialData = MainApp.loadInitialData(storage, START_TIME);

        assertEquals(getTypicalAddressBook(), initialData.addressBook());
        assertTrue(initialData.warning().isEmpty());
    }

    @Test
    public void loadInitialData_invalidPerson_backsUpFileAndWarns() throws Exception {
        assertInvalidFileBackedUp("invalidPersonAddressBook.json", "Names should");
    }

    @Test
    public void loadInitialData_malformedJson_backsUpFileAndWarns() throws Exception {
        assertInvalidFileBackedUp("notJsonFormatAddressBook.json", "Reason: ");
    }

    @Test
    public void loadInitialData_backupFails_warnsUserToKeepFileSafe() throws Exception {
        Path dataFile = dataFile();
        Files.copy(TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json"), dataFile);
        StorageManager storageWithFailingBackup = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))) {
            @Override
            public Path backupAddressBook(LocalDateTime backupTime) throws IOException {
                throw new IOException("disk full");
            }
        };

        MainApp.InitialData initialData = MainApp.loadInitialData(storageWithFailingBackup, START_TIME);

        assertEquals(new AddressBook(), initialData.addressBook());
        String warning = initialData.warning().orElseThrow();
        assertTrue(warning.contains("could not be backed up"), warning);
        assertTrue(warning.contains("disk full"), warning);
        assertTrue(warning.contains("without entering any command"), warning);
    }

    @Test
    public void loadInitialData_longMultilineReason_showsShortenedFirstLine() {
        String longFirstLine = "x".repeat(250);
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile()),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))) {
            @Override
            public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
                throw new DataLoadingException(new IllegalValueException(longFirstLine + "\nsecond line"));
            }

            @Override
            public Path backupAddressBook(LocalDateTime backupTime) {
                return testFolder.resolve("backup.json");
            }
        };

        String warning = MainApp.loadInitialData(storage, START_TIME).warning().orElseThrow();

        assertTrue(warning.contains("Reason: " + "x".repeat(200) + "...\n"), warning);
        assertFalse(warning.contains("x".repeat(201)), warning);
        assertFalse(warning.contains("second line"), warning);
    }

    @Test
    public void start_unreadableDataFile_showsWarningAfterWindowOpens() throws Exception {
        Path dataFile = dataFile();
        Files.copy(TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json"), dataFile);
        MainApp mainApp = new MainApp();
        UiStub uiStub = new UiStub();
        mainApp.ui = uiStub;

        Model model = mainApp.initModelManager(createStorage(dataFile), new UserPrefs());
        mainApp.start(null);

        assertEquals(new AddressBook(), model.getAddressBook());
        assertEquals(2, uiStub.events.size(), uiStub.events.toString());
        assertEquals(UiStub.STARTED, uiStub.events.get(0));
        assertTrue(uiStub.events.get(1).contains("could not read the patient data file " + dataFile),
                uiStub.events.get(1));
    }

    @Test
    public void start_validDataFile_showsNoWarning() throws Exception {
        StorageManager storage = createStorage(dataFile());
        storage.saveAddressBook(getTypicalAddressBook());
        MainApp mainApp = new MainApp();
        UiStub uiStub = new UiStub();
        mainApp.ui = uiStub;

        Model model = mainApp.initModelManager(storage, new UserPrefs());
        mainApp.start(null);

        assertEquals(getTypicalAddressBook(), model.getAddressBook());
        assertEquals(List.of(UiStub.STARTED), uiStub.events);
    }

    /**
     * Checks that an unreadable data file starts an empty list, is copied byte for byte to a timestamped backup,
     * stays unchanged itself, and is explained in a warning naming the backup.
     */
    private void assertInvalidFileBackedUp(String invalidFileName, String expectedReasonText) throws Exception {
        Path dataFile = dataFile();
        Files.copy(TEST_DATA_FOLDER.resolve(invalidFileName), dataFile);
        byte[] originalBytes = Files.readAllBytes(dataFile);

        MainApp.InitialData initialData = MainApp.loadInitialData(createStorage(dataFile), START_TIME);

        Path backupPath = testFolder.resolve("doc-invalid-20261029-153005.json");
        assertEquals(new AddressBook(), initialData.addressBook());
        assertArrayEquals(originalBytes, Files.readAllBytes(backupPath));
        assertArrayEquals(originalBytes, Files.readAllBytes(dataFile));
        String warning = initialData.warning().orElseThrow();
        assertTrue(warning.contains("could not read the patient data file " + dataFile), warning);
        assertTrue(warning.contains(expectedReasonText), warning);
        assertTrue(warning.contains("saved as " + backupPath), warning);
    }

    private Path dataFile() {
        return testFolder.resolve("doc.json");
    }

    private StorageManager createStorage(Path dataFile) {
        return new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
    }

    /**
     * A UI that records, in order, that it was started and each start-up warning it was asked to show, without
     * opening any window.
     */
    private static class UiStub implements Ui {
        static final String STARTED = "started";

        final List<String> events = new ArrayList<>();

        @Override
        public void start(Stage primaryStage) {
            events.add(STARTED);
        }

        @Override
        public void showStartupWarning(String message) {
            events.add(message);
        }
    }
}
