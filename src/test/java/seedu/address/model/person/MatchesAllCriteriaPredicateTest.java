package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class MatchesAllCriteriaPredicateTest {

    private final Predicate<Person> nameCriterion = new NameContainsKeywordsPredicate(List.of("alex"));
    private final Predicate<Person> addressCriterion = new AddressContainsKeywordsPredicate(List.of("clementi"));

    @Test
    public void test_allCriteriaMatch_returnsTrue() {
        Person person = new PersonBuilder().withName("Alex Yeoh").withAddress("Clementi Ave 2").build();
        assertTrue(new MatchesAllCriteriaPredicate(List.of(nameCriterion, addressCriterion)).test(person));
    }

    @Test
    public void test_someCriteriaDoNotMatch_returnsFalse() {
        Person nameOnly = new PersonBuilder().withName("Alex Yeoh").withAddress("Tampines St 1").build();
        Person addressOnly = new PersonBuilder().withName("Ben Tan").withAddress("Clementi Ave 2").build();
        MatchesAllCriteriaPredicate predicate = new MatchesAllCriteriaPredicate(List.of(nameCriterion,
                addressCriterion));

        assertFalse(predicate.test(nameOnly));
        assertFalse(predicate.test(addressOnly));
    }

    @Test
    public void equals() {
        MatchesAllCriteriaPredicate predicate = new MatchesAllCriteriaPredicate(List.of(nameCriterion));

        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new MatchesAllCriteriaPredicate(
                List.of(new NameContainsKeywordsPredicate(List.of("alex"))))));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals(nameCriterion));
        assertFalse(predicate.equals(new MatchesAllCriteriaPredicate(List.of(nameCriterion, addressCriterion))));
    }

    @Test
    public void toStringMethod() {
        MatchesAllCriteriaPredicate predicate = new MatchesAllCriteriaPredicate(List.of(nameCriterion));
        String expected = MatchesAllCriteriaPredicate.class.getCanonicalName() + "{criteria=[" + nameCriterion + "]}";
        assertEquals(expected, predicate.toString());
    }
}
