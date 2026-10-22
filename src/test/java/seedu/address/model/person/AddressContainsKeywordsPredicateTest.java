package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class AddressContainsKeywordsPredicateTest {

    private final Person person = new PersonBuilder().withAddress("311, Clementi Ave 2, #02-25").build();

    @Test
    public void test_addressWordStartsWithKeyword_returnsTrue() {
        // whole word, partial word and mixed case
        assertTrue(new AddressContainsKeywordsPredicate(List.of("Clementi")).test(person));
        assertTrue(new AddressContainsKeywordsPredicate(List.of("clem")).test(person));
        assertTrue(new AddressContainsKeywordsPredicate(List.of("AVE")).test(person));

        // only one keyword matches
        assertTrue(new AddressContainsKeywordsPredicate(List.of("tampines", "clementi")).test(person));
    }

    @Test
    public void test_addressWordDoesNotStartWithKeyword_returnsFalse() {
        // zero keywords
        assertFalse(new AddressContainsKeywordsPredicate(List.of()).test(person));

        // keyword in the middle of a word
        assertFalse(new AddressContainsKeywordsPredicate(List.of("menti")).test(person));

        // keyword matches the name but not the address
        Person personWithNameKeyword = new PersonBuilder().withName("Tampines Tan").withAddress("Clementi").build();
        assertFalse(new AddressContainsKeywordsPredicate(List.of("tampines")).test(personWithNameKeyword));
    }

    @Test
    public void equals() {
        AddressContainsKeywordsPredicate predicate = new AddressContainsKeywordsPredicate(List.of("clementi"));

        assertTrue(predicate.equals(new AddressContainsKeywordsPredicate(List.of("clementi"))));
        assertFalse(predicate.equals(new AddressContainsKeywordsPredicate(List.of("tampines"))));
        assertFalse(predicate.equals(new NameContainsKeywordsPredicate(List.of("clementi"))));
    }
}
