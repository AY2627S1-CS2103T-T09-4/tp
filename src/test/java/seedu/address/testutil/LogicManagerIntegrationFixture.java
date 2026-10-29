package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.LogicManager;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Composes the counted-storage and state-verification support shared by command integration tests.
 */
public class LogicManagerIntegrationFixture {

    private final Model model;
    private final Path testFolder;
    private final Clock validationClock;
    private JsonAddressBookStorage addressBookStorage;
    private LogicManager logic;
    private int saveCount;

    /**
     * Creates a fixture and persists the model's initial state without counting that setup write.
     *
     * @param model Model under test.
     * @param testFolder Temporary folder for address-book and preferences files.
     * @param validationClock Clock used by command parsers.
     * @throws IOException If the initial address book cannot be saved.
     */
    public LogicManagerIntegrationFixture(Model model, Path testFolder, Clock validationClock) throws IOException {
        this.model = model;
        this.testFolder = testFolder;
        this.validationClock = validationClock;
        addressBookStorage = createCountingStorage();
        addressBookStorage.saveAddressBook(model.getAddressBook());
        saveCount = 0;
        logic = createLogic(addressBookStorage);
    }

    /**
     * Returns the logic manager under test.
     */
    public LogicManager getLogic() {
        return logic;
    }

    /**
     * Returns the storage used by the fixture.
     */
    public JsonAddressBookStorage getAddressBookStorage() {
        return addressBookStorage;
    }

    /**
     * Returns the number of counted address-book saves after setup.
     */
    public int getSaveCount() {
        return saveCount;
    }

    /**
     * Replaces the storage with one that fails every save using the supplied exception.
     *
     * @param failure Exception that the replacement storage should throw.
     */
    public void useFailingStorage(IOException failure) {
        Path storagePath = addressBookStorage.getAddressBookFilePath();
        addressBookStorage = new JsonAddressBookStorage(storagePath) {
            /**
             * Simulates a storage write failure.
             */
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw failure;
            }
        };
        logic = createLogic(addressBookStorage);
    }

    /**
     * Creates another logic manager using the fixture storage and explicitly supplied model and validation clock.
     *
     * @param targetModel Model that the returned logic manager should use.
     * @param targetValidationClock Clock used by the returned command parser.
     * @return A logic manager configured with the supplied model and clock.
     */
    public LogicManager createLogic(Model targetModel, Clock targetValidationClock) {
        return createLogic(targetModel, addressBookStorage, targetValidationClock);
    }

    private LogicManager createLogic(JsonAddressBookStorage storage) {
        return createLogic(model, storage, validationClock);
    }

    private LogicManager createLogic(Model targetModel, JsonAddressBookStorage storage, Clock targetValidationClock) {
        StorageManager storageManager = new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("userPrefs.json")));
        return new LogicManager(targetModel, storageManager, new AddressBookParser(targetValidationClock));
    }

    /**
     * Asserts that the model and reloaded file contain the expected patients.
     *
     * @param expectedPersons Patients expected in stored order.
     * @throws DataLoadingException If the persisted address book cannot be read.
     */
    public void assertPersistedPatients(Person... expectedPersons) throws DataLoadingException {
        List<Person> expectedPatients = List.of(expectedPersons);
        assertEquals(expectedPatients, model.getAddressBook().getPersonList());
        assertEquals(expectedPatients, addressBookStorage.readAddressBook().orElseThrow().getPersonList());
    }

    /**
     * Executes a command expected to fail and verifies that the model, display, file, and save count remain unchanged.
     * Also verifies that no input request remains pending.
     *
     * @param commandText Command to execute.
     * @param exceptionClass Expected exception type.
     * @param expectedMessage Expected exception message.
     * @return The caught exception for scenario-specific assertions.
     * @throws IOException If the saved address book cannot be read.
     */
    public <T extends Exception> T assertRejectedCommand(String commandText, Class<T> exceptionClass,
            String expectedMessage) throws IOException {
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedPersons = List.copyOf(model.getFilteredPersonList());
        String savedData = Files.readString(addressBookStorage.getAddressBookFilePath());
        int savesBefore = saveCount;

        T exception = assertThrows(exceptionClass, expectedMessage, () -> logic.execute(commandText));

        assertEquals(before, model.getAddressBook());
        assertEquals(displayedPersons, model.getFilteredPersonList());
        assertEquals(savedData, Files.readString(addressBookStorage.getAddressBookFilePath()));
        assertEquals(savesBefore, saveCount);
        assertFalse(logic.isInputRequestPending());
        return exception;
    }

    private JsonAddressBookStorage createCountingStorage() {
        return new JsonAddressBookStorage(testFolder.resolve("addressBook.json")) {
            /**
             * Saves the address book and counts this write.
             */
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                saveCount++;
                super.saveAddressBook(addressBook);
            }
        };
    }

}
