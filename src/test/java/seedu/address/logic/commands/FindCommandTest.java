package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.FIONA;
import static seedu.address.testutil.TypicalPersons.GEORGE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.AddressContainsKeywordsPredicate;
import seedu.address.model.person.MatchesAllCriteriaPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.NoteContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.PhoneContainsKeywordsPredicate;
import seedu.address.model.person.TagContainsKeywordsPredicate;
import seedu.address.model.person.VisitDateOnDaysPredicate;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void equals() {
        NameContainsKeywordsPredicate firstPredicate =
                new NameContainsKeywordsPredicate(List.of("first"));
        NameContainsKeywordsPredicate secondPredicate =
                new NameContainsKeywordsPredicate(List.of("second"));

        FindCommand findFirstCommand = new FindCommand(firstPredicate);
        FindCommand findSecondCommand = new FindCommand(secondPredicate);

        // same object -> returns true
        assertTrue(findFirstCommand.equals(findFirstCommand));

        // same values -> returns true
        FindCommand findFirstCommandCopy = new FindCommand(firstPredicate);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        // different types -> returns false
        assertFalse(findFirstCommand.equals(1));

        // null -> returns false
        assertFalse(findFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(findFirstCommand.equals(findSecondCommand));
    }

    @Test
    public void execute_zeroKeywords_noPersonFound() {
        String expectedMessage = "No patients found. "
                + "Name keywords match the start of any word in a name, e.g. \"find n/g\" matches \"George Miller\".";
        NameContainsKeywordsPredicate predicate = preparePredicate(" ");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_multipleKeywords_multiplePersonsFound() {
        String expectedMessage = "3 patients found.";
        NameContainsKeywordsPredicate predicate = preparePredicate("Kurz Elle Kunz");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(CARL, ELLE, FIONA), model.getFilteredPersonList());
    }

    @Test
    public void execute_singleLetter_personsWithAnyNameWordStartingWithLetterFound() {
        // "b" starts BENSON's first name and GEORGE's last name
        String expectedMessage = "2 patients found.";
        NameContainsKeywordsPredicate predicate = preparePredicate("b");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(BENSON, GEORGE), model.getFilteredPersonList());
    }

    @Test
    public void execute_partialWord_onlyNameWordsStartingWithItFound() {
        // "Mey" starts ELLE's "Meyer" but not the "Meier" surnames
        String expectedMessage = "1 patient found.";
        NameContainsKeywordsPredicate predicate = preparePredicate("Mey");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(ELLE), model.getFilteredPersonList());
    }

    @Test
    public void execute_phoneKeyword_matchesAnyPartOfNumber() {
        assertFindSuccess(new PhoneContainsKeywordsPredicate(List.of("9482")), "3 patients found.",
                List.of(ELLE, FIONA, GEORGE));

        // the last four digits are enough
        assertFindSuccess(new PhoneContainsKeywordsPredicate(List.of("2442")), "1 patient found.",
                List.of(GEORGE));
    }

    @Test
    public void execute_addressKeyword_matchesStartOfAddressWords() {
        assertFindSuccess(new AddressContainsKeywordsPredicate(List.of("street")), "3 patients found.",
                List.of(CARL, DANIEL, GEORGE));
        assertFindSuccess(new AddressContainsKeywordsPredicate(List.of("clem")), "1 patient found.",
                List.of(BENSON));
    }

    @Test
    public void execute_tagKeyword_matchesStartOfTags() {
        assertFindSuccess(new TagContainsKeywordsPredicate(List.of("friend")), "3 patients found.",
                List.of(ALICE, BENSON, DANIEL));
    }

    @Test
    public void execute_visitDate_matchesVisitsOnThatDay() {
        // every typical person is visited on 1/1/2026, at different times
        assertFindSuccess(new VisitDateOnDaysPredicate(List.of(LocalDate.of(2026, 1, 1))), "7 patients found.",
                getTypicalAddressBook().getPersonList());
        assertFindSuccess(new VisitDateOnDaysPredicate(List.of(LocalDate.of(2026, 1, 2))),
                FindCommand.MESSAGE_NO_PATIENTS_FOUND, List.of());
    }

    @Test
    public void execute_noteKeyword_matchesStartOfNoteWords() {
        // typical persons have no notes, so give two of them notes in both models
        Person carlWithNote = new PersonBuilder(CARL).withNote("Review insulin dose").build();
        Person elleWithNote = new PersonBuilder(ELLE).withNote("Check wound").build();
        for (Model eachModel : List.of(model, expectedModel)) {
            eachModel.setPerson(CARL, carlWithNote);
            eachModel.setPerson(ELLE, elleWithNote);
        }

        assertFindSuccess(new NoteContainsKeywordsPredicate(List.of("insul")), "1 patient found.",
                List.of(carlWithNote));
        assertFindSuccess(new NoteContainsKeywordsPredicate(List.of("insulin", "wound")), "2 patients found.",
                List.of(carlWithNote, elleWithNote));
    }

    @Test
    public void execute_multipleFields_onlyPersonsMatchingAllFieldsFound() {
        // "Meier" alone matches BENSON and DANIEL; the address narrows it to BENSON
        MatchesAllCriteriaPredicate predicate = new MatchesAllCriteriaPredicate(List.of(
                new NameContainsKeywordsPredicate(List.of("Meier")),
                new AddressContainsKeywordsPredicate(List.of("clementi"))));
        assertFindSuccess(predicate, "1 patient found.", List.of(BENSON));
    }

    @Test
    public void toStringMethod() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("keyword"));
        FindCommand findCommand = new FindCommand(predicate);
        String expected = FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, findCommand.toString());
    }

    /**
     * Checks that finding with {@code predicate} shows {@code expectedMessage} and lists {@code expectedPersons}.
     */
    private void assertFindSuccess(Predicate<Person> predicate, String expectedMessage, List<Person> expectedPersons) {
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(new FindCommand(predicate), model, expectedMessage, expectedModel);
        assertEquals(expectedPersons, model.getFilteredPersonList());
    }

    /**
     * Parses {@code userInput} into a {@code NameContainsKeywordsPredicate}.
     */
    private NameContainsKeywordsPredicate preparePredicate(String userInput) {
        return new NameContainsKeywordsPredicate(List.of(userInput.split("\\s+")));
    }
}
