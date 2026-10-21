package seedu.address.model.person;

import java.util.List;

import seedu.address.commons.util.StringUtil;

/**
 * Tests that any word in a {@code Person}'s {@code Name} starts with any of the keywords given, ignoring case.
 */
public class NameContainsKeywordsPredicate extends FieldContainsKeywordsPredicate {

    /**
     * Creates a predicate that matches a person whose name matches any of {@code keywords}.
     */
    public NameContainsKeywordsPredicate(List<String> keywords) {
        super(keywords);
    }

    @Override
    protected boolean matches(Person person, String keyword) {
        return StringUtil.containsWordStartingWithIgnoreCase(person.getName().fullName, keyword);
    }
}
