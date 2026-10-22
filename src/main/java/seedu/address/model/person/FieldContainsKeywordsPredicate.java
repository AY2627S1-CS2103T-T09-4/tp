package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that one field of a {@code Person} matches any of the keywords given.
 * Subclasses decide which field is searched and how a single keyword matches it.
 */
public abstract class FieldContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    /**
     * Creates a predicate that matches a person when any of {@code keywords} matches the searched field.
     */
    protected FieldContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = requireNonNull(keywords);
    }

    @Override
    public boolean test(Person person) {
        return keywords.stream().anyMatch(keyword -> matches(person, keyword));
    }

    /**
     * Returns true if {@code keyword} matches the searched field of {@code person}.
     */
    protected abstract boolean matches(Person person, String keyword);

    /**
     * Returns true if {@code other} is the same kind of predicate with the same keywords.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // Compare exact classes so that, e.g., name and address predicates with equal keywords differ
        if (other == null || other.getClass() != getClass()) {
            return false;
        }

        return keywords.equals(((FieldContainsKeywordsPredicate) other).keywords);
    }

    @Override
    public int hashCode() {
        return keywords.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
