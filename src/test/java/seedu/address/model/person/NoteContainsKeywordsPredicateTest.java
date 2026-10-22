package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NoteContainsKeywordsPredicateTest {

    private final Person person = new PersonBuilder().withNote("Review insulin dose after meals").build();

    @Test
    public void test_noteWordStartsWithKeyword_returnsTrue() {
        // whole word, partial word and mixed case
        assertTrue(new NoteContainsKeywordsPredicate(List.of("insulin")).test(person));
        assertTrue(new NoteContainsKeywordsPredicate(List.of("insul")).test(person));
        assertTrue(new NoteContainsKeywordsPredicate(List.of("DOSE")).test(person));

        // only one keyword matches
        assertTrue(new NoteContainsKeywordsPredicate(List.of("wound", "meals")).test(person));
    }

    @Test
    public void test_noteWordDoesNotStartWithKeyword_returnsFalse() {
        // zero keywords
        assertFalse(new NoteContainsKeywordsPredicate(List.of()).test(person));

        // keyword in the middle of a word
        assertFalse(new NoteContainsKeywordsPredicate(List.of("sulin")).test(person));

        // person with no notes
        Person personWithoutNote = new PersonBuilder().withNote("").build();
        assertFalse(new NoteContainsKeywordsPredicate(List.of("insulin")).test(personWithoutNote));

        // keyword matches the name but not the notes
        Person personWithNameKeyword = new PersonBuilder().withName("Insulin Tan").withNote("Check wound").build();
        assertFalse(new NoteContainsKeywordsPredicate(List.of("insulin")).test(personWithNameKeyword));
    }

    @Test
    public void equals() {
        NoteContainsKeywordsPredicate predicate = new NoteContainsKeywordsPredicate(List.of("insulin"));

        assertTrue(predicate.equals(new NoteContainsKeywordsPredicate(List.of("insulin"))));
        assertFalse(predicate.equals(new NoteContainsKeywordsPredicate(List.of("wound"))));
        assertFalse(predicate.equals(new NameContainsKeywordsPredicate(List.of("insulin"))));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("insulin");
        String expected = NoteContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, new NoteContainsKeywordsPredicate(keywords).toString());
    }
}
