package seedu.address.model;

import java.util.Comparator;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** Opaque snapshot of a model's patient data and displayed-list state. */
    interface ModelState {
    }

    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /** Number of visits a doctor can comfortably make in one day; scheduling beyond it requires confirmation. */
    int MAX_VISITS_PER_DAY = 7;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Returns a snapshot of the patient data and displayed-list state.
     */
    ModelState createStateSnapshot();

    /**
     * Restores patient data and displayed-list state from {@code modelState}.
     *
     * @throws IllegalArgumentException If the snapshot was created by an incompatible model implementation.
     */
    void restoreStateSnapshot(ModelState modelState);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Returns true if a person with the same normalized name as {@code person} exists in the address book.
     */
    boolean hasPersonWithSameName(Person person);

    /**
     * Returns true if any stored patient shares a current or previous datetime with {@code person}.
     */
    boolean hasPersonWithSameVisitDate(Person person);

    /**
     * Returns true if any stored patient other than {@code excludedPerson} shares a visit datetime with {@code person}.
     * Includes patients hidden by the active filter.
     *
     * @throws NullPointerException If either argument is null.
     */
    boolean hasPersonWithSameVisitDate(Person person, Person excludedPerson);

    /**
     * Returns the number of persons whose visit falls on the same day as {@code person}'s visit.
     */
    int countPersonsWithSameVisitDay(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} and its visit date must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The identity and visit date of {@code editedPerson} must not match another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter by the given {@code predicate} and restores the stored person order.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Sorts the displayed person list using {@code comparator}, without changing stored order.
     *
     * @throws NullPointerException If {@code comparator} is null.
     */
    void sortFilteredPersonList(Comparator<Person> comparator);
}
