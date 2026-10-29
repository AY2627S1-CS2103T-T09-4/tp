package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainAppTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");
    private static final LocalDateTime START_TIME = LocalDateTime.of(2026, 10, 29, 15, 30, 5);

    @TempDir
    public Path testFolder;

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

    /**
     * Checks that an unreadable data file starts an empty list, is copied byte for byte to a timestamped backup,
     * stays unchanged itself, and is explained in a warning naming the backup.
     */
    private void assertInvalidFileBackedUp(String invalidFileName, String expectedReasonText) throws Exception {
        Path dataFile = dataFile();
        Files.copy(TEST_DATA_FOLDER.resolve(invalidFileName), dataFile);
        byte[] originalBytes = Files.readAllBytes(dataFile);

        MainApp.InitialData initialData = MainApp.loadInitialData(createStorage(dataFile), START_TIME);

        Path backupPath = testFolder.resolve("addressbook-invalid-20261029-153005.json");
        assertEquals(new AddressBook(), initialData.addressBook());
        assertArrayEquals(originalBytes, Files.readAllBytes(backupPath));
        assertArrayEquals(originalBytes, Files.readAllBytes(dataFile));
        String warning = initialData.warning().orElseThrow();
        assertTrue(warning.contains("could not read the patient data file " + dataFile), warning);
        assertTrue(warning.contains(expectedReasonText), warning);
        assertTrue(warning.contains("saved as " + backupPath), warning);
    }

    private Path dataFile() {
        return testFolder.resolve("addressbook.json");
    }

    private StorageManager createStorage(Path dataFile) {
        return new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
    }
}
