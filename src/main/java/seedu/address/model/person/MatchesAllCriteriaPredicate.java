package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person} satisfies every one of the given search criteria, e.g. both a name and an address
 * criterion, so that each additional criterion narrows the search.
 */
public class MatchesAllCriteriaPredicate implements Predicate<Person> {
    private final List<Predicate<Person>> criteria;

    /**
     * Creates a predicate that requires every predicate in {@code criteria} to match.
     */
    public MatchesAllCriteriaPredicate(List<Predicate<Person>> criteria) {
        this.criteria = requireNonNull(criteria);
    }

    @Override
    public boolean test(Person person) {
        return criteria.stream().allMatch(criterion -> criterion.test(person));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof MatchesAllCriteriaPredicate otherPredicate)) {
            return false;
        }

        return criteria.equals(otherPredicate.criteria);
    }

    @Override
    public int hashCode() {
        return criteria.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("criteria", criteria).toString();
    }
}
