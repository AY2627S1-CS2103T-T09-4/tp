package seedu.address.model.person;

import java.util.List;

import seedu.address.commons.util.StringUtil;

/**
 * Tests that any of a {@code Person}'s tags starts with any of the keywords given, ignoring case.
 */
public class TagContainsKeywordsPredicate extends FieldContainsKeywordsPredicate {

    /**
     * Creates a predicate that matches a person with a tag that matches any of {@code keywords}.
     */
    public TagContainsKeywordsPredicate(List<String> keywords) {
        super(keywords);
    }

    @Override
    protected boolean matches(Person person, String keyword) {
        return person.getTags().stream()
                .anyMatch(tag -> StringUtil.containsWordStartingWithIgnoreCase(tag.tagName, keyword));
    }
}
