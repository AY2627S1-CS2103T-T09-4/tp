package seedu.address;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V0.5.1";

    static final String MESSAGE_DATA_NOT_LOADED = "Doc could not read the patient data file %1$s, so it has "
            + "started with an empty patient list.\nReason: %2$s\n";
    static final String MESSAGE_BACKUP_SAVED = "A copy of the unreadable file was saved as %1$s, so no patient data "
            + "is lost. To recover it, close Doc, fix the problem in the copy, and use it to replace %2$s.";
    static final String MESSAGE_BACKUP_FAILED = "The file could not be backed up (%1$s). To avoid losing it, close "
            + "Doc now without entering any command, and copy %2$s somewhere safe.";
    static final String MESSAGE_DATA_FILE_MIGRATION_FAILED = "Doc could not rename the existing patient data file "
            + "from %1$s to %2$s, so this run will continue using %1$s.\nReason: %3$s";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path PATIENT_DATA_FILE_PATH = Paths.get("data", "doc.json");
    private static final Path LEGACY_PATIENT_DATA_FILE_PATH = Paths.get("data", "addressbook.json");
    /** Longest load-failure reason shown to the user; the full details are in the log. */
    private static final int MAX_REASON_LENGTH = 200;

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    /** Warning to show once the main window is open if patient data startup requires the user's attention. */
    private Optional<String> startupWarning = Optional.empty();

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing AddressBook ]===========================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        DataFileResolution dataFileResolution = resolveDataFilePath(
                PATIENT_DATA_FILE_PATH, LEGACY_PATIENT_DATA_FILE_PATH);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(dataFileResolution.dataFilePath());
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);
        startupWarning = combineWarnings(dataFileResolution.warning(), startupWarning);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getAddressBookFilePath(), getHostServices()::showDocument);
    }

    /**
     * Returns the patient data file to use, moving a legacy file to the preferred path when needed.
     * If migration fails, the legacy file remains active and the result carries a warning for the user.
     */
    static DataFileResolution resolveDataFilePath(Path preferredDataFilePath, Path legacyDataFilePath) {
        if (Files.exists(preferredDataFilePath)) {
            if (Files.exists(legacyDataFilePath)) {
                logger.warning("Both patient data files exist; using " + preferredDataFilePath
                        + " and leaving " + legacyDataFilePath + " unchanged.");
            }
            return new DataFileResolution(preferredDataFilePath, Optional.empty());
        }

        if (Files.notExists(legacyDataFilePath)) {
            return new DataFileResolution(preferredDataFilePath, Optional.empty());
        }

        try {
            Files.move(legacyDataFilePath, preferredDataFilePath);
            logger.info("Renamed patient data file from " + legacyDataFilePath + " to " + preferredDataFilePath);
            return new DataFileResolution(preferredDataFilePath, Optional.empty());
        } catch (IOException e) {
            logger.warning("Patient data file could not be renamed from " + legacyDataFilePath
                    + " to " + preferredDataFilePath + ": " + e);
            String warning = String.format(MESSAGE_DATA_FILE_MIGRATION_FAILED, legacyDataFilePath,
                    preferredDataFilePath, String.valueOf(e.getMessage()));
            return new DataFileResolution(legacyDataFilePath, Optional.of(warning));
        }
    }

    /** Combines startup warnings in display order. */
    private static Optional<String> combineWarnings(Optional<String> firstWarning, Optional<String> secondWarning) {
        if (firstWarning.isPresent() && secondWarning.isPresent()) {
            return Optional.of(firstWarning.get() + "\n\n" + secondWarning.get());
        }
        return firstWarning.or(() -> secondWarning);
    }

    /**
     * Returns a {@code ModelManager} with the initial patient data from {@code storage} and {@code userPrefs},
     * keeping any warning about that data to show when the UI starts.
     */
    Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        InitialData initialData = loadInitialData(storage, LocalDateTime.now());
        startupWarning = initialData.warning();
        return new ModelManager(initialData.addressBook(), userPrefs);
    }

    /**
     * Returns the patient data to start with: the saved data, the sample data when no data file exists, or an empty
     * list when the data file cannot be loaded. In the last case the unreadable file is first backed up, because the
     * next save would otherwise overwrite it, and the result carries a warning that says where the copy is.
     *
     * @param storage Storage holding the patient data file.
     * @param now Time used to name the backup file.
     * @return Patient data to start with and, if the data file could not be loaded, a warning for the user.
     */
    static InitialData loadInitialData(Storage storage, LocalDateTime now) {
        Path dataFilePath = storage.getAddressBookFilePath();
        logger.info("Using data file : " + dataFilePath);

        try {
            Optional<ReadOnlyAddressBook> addressBookOptional = storage.readAddressBook();
            if (addressBookOptional.isEmpty()) {
                logger.info("Creating a new data file " + dataFilePath + " populated with a sample AddressBook.");
            }
            return new InitialData(addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook),
                    Optional.empty());
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + dataFilePath + " could not be loaded."
                    + " Will be starting with an empty AddressBook.");
            return new InitialData(new AddressBook(), Optional.of(backUpAndDescribeFailure(storage, e, now)));
        }
    }

    /**
     * Backs up the unreadable data file and returns a warning explaining the failure and where the copy is.
     * If the backup itself fails, the warning tells the user how to keep the file safe instead.
     */
    private static String backUpAndDescribeFailure(Storage storage, DataLoadingException loadingException,
            LocalDateTime now) {
        Path dataFilePath = storage.getAddressBookFilePath();
        String warning = String.format(MESSAGE_DATA_NOT_LOADED, dataFilePath, describeFailure(loadingException));
        try {
            Path backupPath = storage.backupAddressBook(now);
            return warning + String.format(MESSAGE_BACKUP_SAVED, backupPath, dataFilePath);
        } catch (IOException ioe) {
            logger.warning("Data file " + dataFilePath + " could not be backed up: " + ioe);
            return warning + String.format(MESSAGE_BACKUP_FAILED, ioe, dataFilePath);
        }
    }

    /**
     * Returns the first line of the reason the data file could not be loaded, shortened to
     * {@value #MAX_REASON_LENGTH} characters, since parser messages can include the file's contents.
     * The cause's message is used because the exception's own message also names the cause's class.
     */
    private static String describeFailure(DataLoadingException loadingException) {
        String message = String.valueOf(loadingException.getCause().getMessage());
        String firstLine = message.lines().findFirst().orElse("").strip();
        return (firstLine.length() <= MAX_REASON_LENGTH)
                ? firstLine
                : firstLine.substring(0, MAX_REASON_LENGTH) + "...";
    }

    /**
     * Holds the patient data to start with and, if the data file could not be loaded, a warning for the user.
     */
    record InitialData(ReadOnlyAddressBook addressBook, Optional<String> warning) {}

    /** Holds the active patient data path and an optional migration warning. */
    record DataFileResolution(Path dataFilePath, Optional<String> warning) {}

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting AddressBook " + MainApp.VERSION);
        ui.start(primaryStage);
        startupWarning.ifPresent(ui::showStartupWarning);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping AddressBook ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
