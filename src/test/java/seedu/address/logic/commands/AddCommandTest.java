package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class AddCommandTest {

    @Test
    public void execute_hiddenPreviousDateConflict_preservesModel() {
        Model model = new ModelManager();
        Person existing = new PersonBuilder().withName("Existing Patient").withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000").build();
        model.addPerson(existing);
        model.updateFilteredPersonList(person -> false);
        Person candidate = new PersonBuilder().withVisitDate("01/10/2026 1000").build();
        String conflictMessage = String.format(AddCommand.MESSAGE_DUPLICATE_VISIT_DATE, candidate.getVisitDate());

        CommandTestUtil.assertCommandFailure(new AddCommand(candidate), model,
                String.format(AddCommand.MESSAGE_NOT_ADDED, conflictMessage));
    }

    @Test
    public void constructor_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddCommand(null));
    }

    @Test
    public void formatSuccessMessage_listsDetailsAndVisitDate() {
        Person person = new PersonBuilder().withTags("followup").build();
        assertEquals("Added patient Amy Bee; Phone: 85355255; Email: amy@gmail.com; "
                + "Address: 123, Jurong West Ave 6, #08-111; Visit date: 18/9/2026 1000; Tags: [followup]",
                AddCommand.formatSuccessMessage(person));

        // an empty email and an empty tag list are left out
        Person personWithoutEmail = new PersonBuilder().withEmail("").build();
        assertEquals("Added patient Amy Bee; Phone: 85355255; Address: 123, Jurong West Ave 6, #08-111; "
                + "Visit date: 18/9/2026 1000", AddCommand.formatSuccessMessage(personWithoutEmail));
    }

    @Test
    public void execute_personAcceptedByModel_addSuccessful() throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
        Person validPerson = new PersonBuilder().build();

        CommandResult commandResult = new AddCommand(validPerson).execute(modelStub);

        assertEquals(AddCommand.formatSuccessMessage(validPerson),
                commandResult.getFeedbackToUser());
        assertEquals(List.of(validPerson), modelStub.personsAdded);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person validPerson = new PersonBuilder().build();
        AddCommand addCommand = new AddCommand(validPerson);
        ModelStub modelStub = new ModelStubWithPerson(validPerson);

        String expectedMessage = String.format(AddCommand.MESSAGE_NOT_ADDED, AddCommand.MESSAGE_DUPLICATE_PERSON);
        CommandException exception =
                assertThrows(CommandException.class, expectedMessage, () -> addCommand.execute(modelStub));

        assertEquals(validPerson, exception.getPersonToView().orElseThrow());
    }

    @Test
    public void execute_duplicateVisitDate_throwsCommandException() {
        Person existingPerson = new PersonBuilder().withName("Existing Person").build();
        Person personWithConflictingVisit = new PersonBuilder().build();
        AddCommand addCommand = new AddCommand(personWithConflictingVisit);
        ModelStub modelStub = new ModelStubWithPerson(existingPerson);

        String conflictMessage = String.format(AddCommand.MESSAGE_DUPLICATE_VISIT_DATE,
                personWithConflictingVisit.getVisitDate());
        String expectedMessage = String.format(AddCommand.MESSAGE_NOT_ADDED, conflictMessage);
        CommandException exception =
                assertThrows(CommandException.class, expectedMessage, () -> addCommand.execute(modelStub));

        assertEquals(existingPerson, exception.getPersonToView().orElseThrow());
        assertEquals(List.of(existingPerson), modelStub.getFilteredPersonList());
    }

    @Test
    public void equals() {
        Person alice = new PersonBuilder().withName("Alice").build();
        Person bob = new PersonBuilder().withName("Bob").build();
        AddCommand addAliceCommand = new AddCommand(alice);
        AddCommand addBobCommand = new AddCommand(bob);

        // same object -> returns true
        assertTrue(addAliceCommand.equals(addAliceCommand));

        // same values -> returns true
        AddCommand addAliceCommandCopy = new AddCommand(alice);
        assertTrue(addAliceCommand.equals(addAliceCommandCopy));

        // different types -> returns false
        assertFalse(addAliceCommand.equals(1));

        // null -> returns false
        assertFalse(addAliceCommand.equals(null));

        // different person -> returns false
        assertFalse(addAliceCommand.equals(addBobCommand));
    }

    @Test
    public void toStringMethod() {
        AddCommand addCommand = new AddCommand(ALICE);
        String expected = AddCommand.class.getCanonicalName() + "{toAdd=" + ALICE + "}";
        assertEquals(expected, addCommand.toString());
    }

    /**
     * A default model stub that has all of the methods failing.
     */
    private class ModelStub implements Model {
        @Override
        public ReadOnlyUserPrefs getUserPrefs() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public GuiSettings getGuiSettings() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void addPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setAddressBook(ReadOnlyAddressBook newData) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasPersonWithSameName(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasPersonWithSameVisitDate(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        /**
         * Fails if the replacement-only conflict check is called while testing an add command.
         *
         * @throws AssertionError Whenever this method is called.
         */
        @Override
        public boolean hasPersonWithSameVisitDate(Person person, Person excludedPerson) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public int countPersonsWithSameVisitDay(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void deletePerson(Person target) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setPerson(Person target, Person editedPerson) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void sortFilteredPersonList(Comparator<Person> comparator) {
            throw new AssertionError("This method should not be called.");
        }
    }

    /**
     * A Model stub that contains a single person.
     */
    private class ModelStubWithPerson extends ModelStub {
        private final Person person;
        private final ObservableList<Person> filteredPersons = FXCollections.observableArrayList();

        ModelStubWithPerson(Person person) {
            requireNonNull(person);
            this.person = person;
            filteredPersons.add(person);
        }

        @Override
        public boolean hasPerson(Person person) {
            requireNonNull(person);
            return this.person.isSamePerson(person);
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            AddressBook addressBook = new AddressBook();
            addressBook.addPerson(person);
            return addressBook;
        }

        @Override
        public boolean hasPersonWithSameName(Person person) {
            requireNonNull(person);
            return this.person.hasSameName(person);
        }

        @Override
        public boolean hasPersonWithSameVisitDate(Person person) {
            requireNonNull(person);
            return this.person.hasCommonVisitDate(person);
        }

        @Override
        public int countPersonsWithSameVisitDay(Person person) {
            requireNonNull(person);
            return this.person.getVisitDate().isSameDay(person.getVisitDate()) ? 1 : 0;
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return filteredPersons;
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            requireNonNull(predicate);
            filteredPersons.setAll(predicate.test(person) ? List.of(person) : List.of());
        }
    }

    /**
     * A Model stub that always accepts the person being added.
     */
    private class ModelStubAcceptingPersonAdded extends ModelStub {
        final ArrayList<Person> personsAdded = new ArrayList<>();

        @Override
        public boolean hasPerson(Person person) {
            requireNonNull(person);
            return personsAdded.stream().anyMatch(person::isSamePerson);
        }

        @Override
        public boolean hasPersonWithSameName(Person person) {
            requireNonNull(person);
            return personsAdded.stream().anyMatch(person::hasSameName);
        }

        @Override
        public boolean hasPersonWithSameVisitDate(Person person) {
            requireNonNull(person);
            return personsAdded.stream()
                    .anyMatch(person::hasCommonVisitDate);
        }

        @Override
        public int countPersonsWithSameVisitDay(Person person) {
            requireNonNull(person);
            return (int) personsAdded.stream()
                    .filter(existingPerson -> existingPerson.getVisitDate().isSameDay(person.getVisitDate()))
                    .count();
        }

        @Override
        public void addPerson(Person person) {
            requireNonNull(person);
            personsAdded.add(person);
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return new AddressBook();
        }
    }

}
