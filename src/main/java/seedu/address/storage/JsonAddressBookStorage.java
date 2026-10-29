package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    /** Time stamp in backup file names, e.g. {@code 20261029-153000}, so that backups sort by time. */
    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss");
    private static final String JSON_EXTENSION = ".json";

    private Path filePath;

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableAddressBook> jsonAddressBook = JsonUtil.readJsonFile(
                filePath, JsonSerializableAddressBook.class);
        if (!jsonAddressBook.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonAddressBook.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Copies the data file to a new file in the same folder, named after the data file and {@code backupTime}, e.g.
     * {@code addressbook-invalid-20261029-153000.json}. Used when the data file cannot be loaded, so that the next
     * save does not destroy the only copy of the patient data.
     *
     * @param backupTime Time used in the backup file name.
     * @return Path of the backup file.
     * @throws IOException If the data file cannot be read or the backup cannot be written, including when a backup
     *     with the same name already exists.
     */
    public Path backupAddressBook(LocalDateTime backupTime) throws IOException {
        requireNonNull(backupTime);

        Path backupPath = filePath.resolveSibling(getBackupFileName(backupTime));
        Files.copy(filePath, backupPath);
        logger.info("Backed up data file " + filePath + " to " + backupPath);
        return backupPath;
    }

    /**
     * Returns the backup file name for the data file, keeping its {@code .json} extension at the end.
     */
    private String getBackupFileName(LocalDateTime backupTime) {
        String dataFileName = filePath.getFileName().toString();
        String baseName = dataFileName.endsWith(JSON_EXTENSION)
                ? dataFileName.substring(0, dataFileName.length() - JSON_EXTENSION.length())
                : dataFileName;
        return baseName + "-invalid-" + backupTime.format(BACKUP_TIMESTAMP_FORMATTER) + JSON_EXTENSION;
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), filePath);
    }

}
