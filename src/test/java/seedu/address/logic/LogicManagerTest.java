package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_VISIT_DATE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VISIT_DATE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ParserUtil;
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
import seedu.address.testutil.PersonUtil;

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

    @Test
    public void execute_editVisitDateWithPreviousDates_savesCompleteRecord() throws Exception {
        Person original = new PersonBuilder().withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000", "1/10/2026 1100").build();
        model.addPerson(original);
        Person expected = new PersonBuilder(original).withVisitDate("2/10/2026 1000").build();

        CommandResult result = logic.execute("edit 1 vd/2/10/2026 1000");

        assertTrue(result.shouldSaveAddressBook());
        assertEquals(expected, result.getPersonToView().orElseThrow());
        assertEquals(1, addressBookSaveCount);
        ReadOnlyAddressBook reloaded = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"))
                .readAddressBook().orElseThrow();
        assertEquals(List.of(expected), reloaded.getPersonList());
    }

    @Test
    public void execute_invalidVisitRecordEdit_preservesSavedDataFilterAndSort() throws Exception {
        Person original = new PersonBuilder().withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000").build();
        Person hidden = new PersonBuilder().withName("Hidden Patient").withVisitDate("12/10/2026 1000")
                .withPreviousVisitDates("2/10/2026 1000").build();
        model.addPerson(original);
        model.addPerson(hidden);
        logic.execute("list");
        Path savedFile = temporaryFolder.resolve("addressBook.json");
        String savedBefore = Files.readString(savedFile);
        model.updateFilteredPersonList(person -> person.equals(original));
        model.sortFilteredPersonList(Comparator.comparing((Person person) -> person.getVisitDate().value).reversed());

        assertThrows(CommandException.class,
                String.format(EditCommand.MESSAGE_VISIT_DATE_NOT_AFTER_PREVIOUS, "1/10/2026 1000"), () ->
                logic.execute("edit 1 vd/1/10/2026 1000 note/Changed"));
        assertThrows(CommandException.class,
                String.format(EditCommand.MESSAGE_DUPLICATE_VISIT_DATE, "2/10/2026 1000"), () ->
                logic.execute("edit 1 vd/02/10/2026 1000 note/Changed"));

        assertEquals(1, addressBookSaveCount);
        assertEquals(savedBefore, Files.readString(savedFile));
        assertEquals(List.of(original, hidden), model.getAddressBook().getPersonList());
        assertEquals(List.of(original), model.getFilteredPersonList());
    }

    @Test
    public void execute_cancelCombinedVisitRecordEdit_preservesSavedData() throws Exception {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person original = new PersonBuilder().withName("Other Patient").withVisitDate("2/1/2026 1000")
                .withPreviousVisitDates("1/1/2024 1000").build();
        model.addPerson(original);
        logic = createLogic(model);
        logic.execute("list");
        Path savedFile = temporaryFolder.resolve("addressBook.json");
        String savedBefore = Files.readString(savedFile);
        List<Person> recordsBefore = List.copyOf(model.getAddressBook().getPersonList());
        String duplicateName = recordsBefore.getFirst().getName().fullName;
        int targetPosition = model.getFilteredPersonList().indexOf(original);
        assertTrue(targetPosition >= 0);
        int displayedIndex = targetPosition + 1;

        CommandResult requestResult =
                logic.execute("edit " + displayedIndex + " n/" + duplicateName + " vd/1/1/2026 1500");
        assertTrue(logic.isInputRequestPending());
        CommandResult result = logic.execute("n");

        assertTrue(requestResult.getFeedbackToUser().contains("already exists"));
        assertTrue(requestResult.getFeedbackToUser().contains("already have 7 visits"));
        assertEquals(EditCommand.MESSAGE_EDIT_CANCELLED, result.getFeedbackToUser());
        assertFalse(logic.isInputRequestPending());
        assertEquals(1, addressBookSaveCount);
        assertEquals(savedBefore, Files.readString(savedFile));
        assertEquals(recordsBefore, model.getAddressBook().getPersonList());
        assertEquals(recordsBefore, model.getFilteredPersonList());
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
    public void execute_addDuplicateNameThenConfirm_addsAndSavesOnce() throws Exception {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        logic = createLogic(model);
        Person existingPerson = model.getFilteredPersonList().get(0);
        Person personToAdd = new PersonBuilder(existingPerson).withPhone("91234567")
                .withVisitDate(VALID_VISIT_DATE_BOB).build();

        CommandResult requestResult = logic.execute(PersonUtil.getAddCommand(personToAdd));

        assertEquals(String.format(AddCommand.MESSAGE_WARNING_DUPLICATE_NAME, personToAdd.getName()) + " "
                + String.format(AddCommand.MESSAGE_CONFIRM_ADD, personToAdd.getName()),
                requestResult.getFeedbackToUser());
        assertEquals(existingPerson, requestResult.getPersonToView().orElseThrow());
        assertTrue(logic.isInputRequestPending());
        assertFalse(model.getAddressBook().getPersonList().contains(personToAdd));
        assertEquals(0, addressBookSaveCount);

        CommandResult confirmedResult = logic.execute("y");

        assertEquals(AddCommand.formatSuccessMessage(personToAdd),
                confirmedResult.getFeedbackToUser());
        assertFalse(logic.isInputRequestPending());
        assertTrue(model.getAddressBook().getPersonList().contains(personToAdd));
        assertEquals(1, addressBookSaveCount);
    }

    @Test
    public void execute_editDuplicateNameThenConfirm_editsAndSavesOnce() throws Exception {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        logic = createLogic(model);
        Person firstPerson = model.getFilteredPersonList().get(0);
        Person secondPerson = model.getFilteredPersonList().get(1);
        Person editedPerson = new PersonBuilder(secondPerson).withName(firstPerson.getName().fullName).build();

        CommandResult requestResult = logic.execute("edit 2 n/" + firstPerson.getName());

        String expectedPrompt = String.format(EditCommand.MESSAGE_WARNING_DUPLICATE_NAME, editedPerson.getName())
                + " " + String.format(EditCommand.MESSAGE_CONFIRM_EDIT, editedPerson.getName());
        assertEquals(expectedPrompt, requestResult.getFeedbackToUser());
        assertEquals(firstPerson, requestResult.getPersonToView().orElseThrow());
        assertTrue(logic.isInputRequestPending());
        assertTrue(model.getAddressBook().getPersonList().contains(secondPerson));
        assertEquals(0, addressBookSaveCount);

        CommandResult confirmedResult = logic.execute("Y");

        assertEquals(String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)),
                confirmedResult.getFeedbackToUser());
        assertFalse(logic.isInputRequestPending());
        assertTrue(model.getAddressBook().getPersonList().contains(editedPerson));
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
        String expectedMessage = ParserUtil.MESSAGE_INVALID_INDEX + "\n" + DeleteCommand.MESSAGE_USAGE;

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
    public void execute_confirmedDeleteSaveFailure_restoresPatientDataAndDisplay() throws Exception {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person firstPerson = model.getAddressBook().getPersonList().getFirst();
        Person secondPerson = model.getAddressBook().getPersonList().get(1);
        model.updateFilteredPersonList(person -> person.equals(firstPerson) || person.equals(secondPerson));
        model.sortFilteredPersonList(Comparator.comparing((Person person) -> person.getName().fullName).reversed());
        List<Person> storedPersonsBefore = List.copyOf(model.getAddressBook().getPersonList());
        List<Person> displayedPersonsBefore = List.copyOf(model.getFilteredPersonList());
        logic = createLogicWithFailingStorage(DUMMY_IO_EXCEPTION);
        logic.execute("delete 1");
        String expectedMessage = String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage());

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("y"));

        assertEquals(storedPersonsBefore, model.getAddressBook().getPersonList());
        assertEquals(displayedPersonsBefore, model.getFilteredPersonList());
        assertFalse(logic.isInputRequestPending());
    }

    @Test
    public void execute_confirmedClearSaveFailure_restoresPatientDataAndDisplay() throws Exception {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        List<Person> storedPersonsBefore = List.copyOf(model.getAddressBook().getPersonList());
        List<Person> displayedPersonsBefore = List.copyOf(model.getFilteredPersonList());
        logic = createLogicWithFailingStorage(DUMMY_IO_EXCEPTION);
        logic.execute("clear");
        String expectedMessage = String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage());

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("y"));

        assertEquals(storedPersonsBefore, model.getAddressBook().getPersonList());
        assertEquals(displayedPersonsBefore, model.getFilteredPersonList());
        assertFalse(logic.isInputRequestPending());
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
        logic = createLogicWithFailingStorage(e);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + VISIT_DATE_DESC_AMY;
        ModelManager expectedModel = new ModelManager();
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    /**
     * Creates a logic manager whose patient-data save always throws {@code exception}.
     */
    private Logic createLogicWithFailingStorage(IOException exception) {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("ExceptionAddressBook.json")) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                        throw exception;
                    }
                };
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        return new LogicManager(model, storage);
    }
}
