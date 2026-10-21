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

import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.FindCommandParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.AddressContainsKeywordsPredicate;
import seedu.address.model.person.MatchesAllCriteriaPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.PhoneContainsKeywordsPredicate;
import seedu.address.model.person.TagContainsKeywordsPredicate;

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
                + "Search with n/ for names, p/ for phone numbers, a/ for addresses, or t/ for tags.";
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
    public void execute_noMatchInOtherFields_showsGenericNoMatchMessage() {
        // the hint must suit every field, not just names
        assertFindSuccess(new PhoneContainsKeywordsPredicate(List.of("0000")), FindCommand.MESSAGE_NO_PATIENTS_FOUND,
                List.of());
        assertFindSuccess(new AddressContainsKeywordsPredicate(List.of("tampines")),
                FindCommand.MESSAGE_NO_PATIENTS_FOUND, List.of());
        assertFindSuccess(new TagContainsKeywordsPredicate(List.of("diabetic")), FindCommand.MESSAGE_NO_PATIENTS_FOUND,
                List.of());
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
    public void execute_tabBetweenFields_phoneCriterionStillApplied() throws Exception {
        // BENSON's name matches but his phone does not, so the parsed search must find nobody
        FindCommand command = new FindCommandParser().parse(" n/Benson\tp/0000");
        expectedModel.updateFilteredPersonList(person -> false);

        assertCommandSuccess(command, model, FindCommand.MESSAGE_NO_PATIENTS_FOUND, expectedModel);
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
