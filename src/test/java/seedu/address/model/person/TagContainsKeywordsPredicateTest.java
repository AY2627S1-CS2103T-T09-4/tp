package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class TagContainsKeywordsPredicateTest {

    private final Person person = new PersonBuilder().withTags("diabetic", "elderly").build();

    @Test
    public void test_tagStartsWithKeyword_returnsTrue() {
        // whole tag, partial tag and mixed case
        assertTrue(new TagContainsKeywordsPredicate(List.of("diabetic")).test(person));
        assertTrue(new TagContainsKeywordsPredicate(List.of("diab")).test(person));
        assertTrue(new TagContainsKeywordsPredicate(List.of("ELD")).test(person));

        // only one keyword matches
        assertTrue(new TagContainsKeywordsPredicate(List.of("asthma", "elderly")).test(person));
    }

    @Test
    public void test_tagDoesNotStartWithKeyword_returnsFalse() {
        // zero keywords
        assertFalse(new TagContainsKeywordsPredicate(List.of()).test(person));

        // keyword in the middle of a tag
        assertFalse(new TagContainsKeywordsPredicate(List.of("betic")).test(person));

        // person without tags
        assertFalse(new TagContainsKeywordsPredicate(List.of("diabetic")).test(new PersonBuilder().build()));
    }

    @Test
    public void equals() {
        TagContainsKeywordsPredicate predicate = new TagContainsKeywordsPredicate(List.of("diabetic"));

        assertTrue(predicate.equals(new TagContainsKeywordsPredicate(List.of("diabetic"))));
        assertFalse(predicate.equals(new TagContainsKeywordsPredicate(List.of("elderly"))));
        assertFalse(predicate.equals(new AddressContainsKeywordsPredicate(List.of("diabetic"))));
    }
}
