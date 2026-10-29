package seedu.address.logic;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandInputRequest;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private CommandInputRequest pendingInputRequest;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, new AddressBookParser());
    }

    /**
     * Constructs a logic manager with the supplied parser, model, and storage.
     * Allows command parsing dependencies to be controlled in integration tests.
     */
    public LogicManager(Model model, Storage storage, AddressBookParser addressBookParser) {
        requireAllNonNull(model, storage, addressBookParser);
        this.model = model;
        this.storage = storage;
        this.addressBookParser = addressBookParser;
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");
        Model.ModelState modelState = model.createStateSnapshot();

        CommandResult commandResult = hasPendingInputRequest()
                ? handleRequestedInput(commandText)
                : executeParsedCommand(commandText);

        if (commandResult.shouldSaveAddressBook()) {
            saveAddressBook(modelState);
        }

        pendingInputRequest = commandResult.getInputRequest().orElse(null);
        return commandResult;
    }

    @Override
    public boolean isInputRequestPending() {
        return hasPendingInputRequest();
    }

    /**
     * Parses and executes a regular command.
     */
    private CommandResult executeParsedCommand(String commandText) throws CommandException, ParseException {
        Command command = addressBookParser.parseCommand(commandText);
        return command.execute(model);
    }

    /**
     * Consumes input requested by the previous command without passing it to the command parser.
     */
    private CommandResult handleRequestedInput(String input) throws CommandException {
        CommandInputRequest inputRequest = pendingInputRequest;
        pendingInputRequest = null;
        return inputRequest.respond(input, model);
    }

    /**
     * Returns true when a command has requested another command-line input.
     */
    private boolean hasPendingInputRequest() {
        return pendingInputRequest != null;
    }

    /**
     * Saves the current address book state and restores the previous model state if storage fails.
     */
    private void saveAddressBook(Model.ModelState modelState) throws CommandException {
        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            model.restoreStateSnapshot(modelState);
            logger.log(Level.WARNING, "Patient data could not be saved: access denied", e);
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            model.restoreStateSnapshot(modelState);
            logger.log(Level.WARNING, "Patient data could not be saved: I/O failure", ioe);
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
