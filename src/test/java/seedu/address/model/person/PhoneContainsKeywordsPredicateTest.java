package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PhoneContainsKeywordsPredicateTest {

    private final Person person = new PersonBuilder().withPhone("91234567").build();

    @Test
    public void test_phoneContainsKeyword_returnsTrue() {
        // start, middle and end of the number
        assertTrue(new PhoneContainsKeywordsPredicate(List.of("9123")).test(person));
        assertTrue(new PhoneContainsKeywordsPredicate(List.of("345")).test(person));
        assertTrue(new PhoneContainsKeywordsPredicate(List.of("4567")).test(person));

        // whole number
        assertTrue(new PhoneContainsKeywordsPredicate(List.of("91234567")).test(person));

        // only one keyword matches
        assertTrue(new PhoneContainsKeywordsPredicate(List.of("0000", "4567")).test(person));
    }

    @Test
    public void test_phoneDoesNotContainKeyword_returnsFalse() {
        // zero keywords
        assertFalse(new PhoneContainsKeywordsPredicate(List.of()).test(person));

        // digits not in the number, and a keyword longer than the number
        assertFalse(new PhoneContainsKeywordsPredicate(List.of("0000")).test(person));
        assertFalse(new PhoneContainsKeywordsPredicate(List.of("912345678")).test(person));

        // matching digits that are not consecutive in the number
        assertFalse(new PhoneContainsKeywordsPredicate(List.of("9134")).test(person));
    }

    @Test
    public void equals() {
        PhoneContainsKeywordsPredicate predicate = new PhoneContainsKeywordsPredicate(List.of("9123"));

        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new PhoneContainsKeywordsPredicate(List.of("9123"))));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals(new PhoneContainsKeywordsPredicate(List.of("4567"))));

        // a predicate on a different field with the same keywords -> returns false
        assertFalse(predicate.equals(new NameContainsKeywordsPredicate(List.of("9123"))));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("9123");
        String expected = PhoneContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, new PhoneContainsKeywordsPredicate(keywords).toString());
    }
}
