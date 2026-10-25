package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAndSaveAddressBook_previousDates_roundTripsFullRecord() throws Exception {
        Path filePath = testFolder.resolve("visit-record.json");
        Person patient = new PersonBuilder(ALICE).withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000", "1/10/2026 1100").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(patient);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(addressBook);

        assertTrue(Files.readString(filePath).contains("\"previousVisitDates\""));
        assertEquals(addressBook, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void readAddressBook_missingNullOrEmptyPreviousDates_loadsEmptyHistory() throws Exception {
        Path filePath = testFolder.resolve("legacy.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        for (String property : List.of("", "\"previousVisitDates\": null,", "\"previousVisitDates\": [],")) {
            Files.writeString(filePath, "{\"persons\":[" + createPatientJson(property) + "]}");
            Person loaded = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
            assertEquals(List.of(), loaded.getPreviousVisitDates());
            assertEquals("10/10/2026 1000", loaded.getVisitDate().toString());
            storage.saveAddressBook(storage.readAddressBook().orElseThrow());
            assertTrue(Files.readString(filePath).contains("\"previousVisitDates\" : [ ]"));
        }
    }

    @Test
    public void readAddressBook_invalidPreviousDates_rejectsWithoutRepairingFile() throws Exception {
        Path filePath = testFolder.resolve("invalid-history.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        for (String previousDates : List.of("\"1/10/2026 1000\"", "{}", "1", "true", "[null]",
                "[\"31/2/2026 1000\"]", "[\"\"]", "[{}]", "[[]]", "[false]", "[1]",
                "[\"2/10/2026 1000\",\"1/10/2026 1000\"]",
                "[\"1/10/2026 1000\",\"01/10/2026 1000\"]",
                "[\"10/10/2026 1000\"]", "[\"11/10/2026 1000\"]")) {
            String json = "{\"persons\":["
                    + createPatientJson("\"previousVisitDates\":" + previousDates + ",") + "]}";
            Files.writeString(filePath, json);

            assertThrows(DataLoadingException.class, () -> storage.readAddressBook());
            assertEquals(json, Files.readString(filePath));
        }
    }

    @Test
    public void readAddressBook_missingOrNullCurrentWithHistory_stillRejectsRequiredField() throws Exception {
        Path filePath = testFolder.resolve("missing-current.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        String patient = createPatientJson("\"previousVisitDates\":[\"1/10/2026 1000\"],");
        for (String replacement : List.of("", "\"visitDate\":null,")) {
            String json = "{\"persons\":[" + patient.replace("\"visitDate\":\"10/10/2026 1000\",", replacement)
                    + "]}";
            Files.writeString(filePath, json);

            assertThrows(DataLoadingException.class, () -> storage.readAddressBook());
        }
    }

    @Test
    public void readAddressBook_crossPatientHistoryConflict_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("duplicate-history.json");
        String firstPatient = createPatientJson("\"previousVisitDates\":[\"1/10/2026 1000\"],");
        String secondPatient = createPatientJson("").replace("Test Patient", "Other Patient")
                .replace("10/10/2026 1000", "01/10/2026 1000");
        String json = "{\"persons\":[" + firstPatient + "," + secondPatient + "]}";
        Files.writeString(filePath, json);

        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(filePath).readAddressBook());
        assertEquals(json, Files.readString(filePath));
    }

    /**
     * Returns an otherwise valid patient JSON object with the supplied previous-date property.
     */
    private String createPatientJson(String previousDatesProperty) {
        return "{\"name\":\"Test Patient\",\"phone\":\"91234567\",\"email\":\"\",\"address\":\"Test Road\","
                + "\"visitDate\":\"10/10/2026 1000\"," + previousDatesProperty + "\"note\":\"\",\"tags\":[]}";
    }

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
