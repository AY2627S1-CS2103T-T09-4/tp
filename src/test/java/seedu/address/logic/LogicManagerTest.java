package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VISIT_DATE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;
    private int addressBookSaveCount;

    @BeforeEach
    public void setUp() {
        logic = createLogic(model);
    }

    /**
     * Creates a logic manager that records the number of address book saves.
     */
    private Logic createLogic(Model model) {
        addressBookSaveCount = 0;
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                        addressBookSaveCount++;
                        super.saveAddressBook(addressBook);
                    }
                };
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        return new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_deleteThenConfirm_deletesAndSavesOnce() throws Exception {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        logic = createLogic(model);
        Person personToDelete = model.getFilteredPersonList().get(0);

        CommandResult requestResult = logic.execute("delete 1");

        assertEquals(String.format(DeleteCommand.MESSAGE_CONFIRM_DELETE, personToDelete.getName()),
                requestResult.getFeedbackToUser());
        assertTrue(logic.isInputRequestPending());
        assertTrue(model.getAddressBook().getPersonList().contains(personToDelete));
        assertEquals(0, addressBookSaveCount);

        CommandResult confirmedResult = logic.execute("  y  ");

        assertEquals(String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)),
                confirmedResult.getFeedbackToUser());
        assertFalse(logic.isInputRequestPending());
        assertFalse(model.getAddressBook().getPersonList().contains(personToDelete));
        assertEquals(1, addressBookSaveCount);
    }

    @Test
    public void execute_deleteThenValidCommand_cancelsAndConsumesCommand() throws Exception {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        logic = createLogic(model);
        int initialPersonCount = model.getAddressBook().getPersonList().size();
        logic.execute("delete 1");

        CommandResult cancelledResult = logic.execute("list");

        assertEquals(DeleteCommand.MESSAGE_DELETE_CANCELLED, cancelledResult.getFeedbackToUser());
        assertEquals(initialPersonCount, model.getAddressBook().getPersonList().size());
        assertFalse(logic.isInputRequestPending());
        assertEquals(0, addressBookSaveCount);

        CommandResult listResult = logic.execute("list");
        assertEquals(ListCommand.MESSAGE_SUCCESS, listResult.getFeedbackToUser());
        assertEquals(1, addressBookSaveCount);
    }

    @Test
    public void execute_deleteThenBlankInput_cancelsDeletion() throws Exception {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        logic = createLogic(model);
        int initialPersonCount = model.getAddressBook().getPersonList().size();
        logic.execute("delete 1");

        CommandResult result = logic.execute("");

        assertEquals(DeleteCommand.MESSAGE_DELETE_CANCELLED, result.getFeedbackToUser());
        assertEquals(initialPersonCount, model.getAddressBook().getPersonList().size());
        assertEquals(0, addressBookSaveCount);
    }

    @Test
    public void execute_deleteTargetChangedThenConfirm_reportsErrorWithoutSaving() throws Exception {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        logic = createLogic(model);
        Person originalPerson = model.getFilteredPersonList().get(0);
        logic.execute("delete 1");
        Person editedPerson = new PersonBuilder(originalPerson).withPhone("91234567").build();
        model.setPerson(originalPerson, editedPerson);

        assertThrows(
                CommandException.class, DeleteCommand.MESSAGE_PERSON_NO_LONGER_EXISTS, () -> logic.execute("y"));

        assertFalse(logic.isInputRequestPending());
        assertTrue(model.getAddressBook().getPersonList().contains(editedPerson));
        assertEquals(0, addressBookSaveCount);
    }

    @Test
    public void execute_invalidDelete_usesExistingErrorWithoutRequestOrSave() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);

        assertParseException("delete invalid", expectedMessage);

        assertFalse(logic.isInputRequestPending());
        assertEquals(0, addressBookSaveCount);
    }

    @Test
    public void execute_clearEmptyAddressBook_returnsAlreadyEmptyWithoutRequestOrSave() throws Exception {
        CommandResult result = logic.execute("clear ignored arguments");

        assertEquals(ClearCommand.MESSAGE_ALREADY_EMPTY, result.getFeedbackToUser());
        assertFalse(logic.isInputRequestPending());
        assertEquals(0, addressBookSaveCount);
    }

    @Test
    public void execute_clearThenConfirm_clearsAndSavesOnce() throws Exception {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        logic = createLogic(model);
        int entryCount = model.getAddressBook().getPersonList().size();

        CommandResult requestResult = logic.execute("clear ignored arguments");

        assertEquals(String.format(ClearCommand.MESSAGE_CONFIRM_CLEAR_MULTIPLE, entryCount),
                requestResult.getFeedbackToUser());
        assertEquals(entryCount, model.getAddressBook().getPersonList().size());
        assertEquals(0, addressBookSaveCount);

        CommandResult confirmedResult = logic.execute("Y");

        assertEquals(ClearCommand.MESSAGE_SUCCESS, confirmedResult.getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(1, addressBookSaveCount);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + VISIT_DATE_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
