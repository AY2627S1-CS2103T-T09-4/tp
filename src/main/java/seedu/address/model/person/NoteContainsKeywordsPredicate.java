package seedu.address.model.person;

import java.util.List;

import seedu.address.commons.util.StringUtil;

/**
 * Tests that any word in a {@code Person}'s {@code Note} starts with any of the keywords given, ignoring case.
 * A person with no notes never matches.
 */
public class NoteContainsKeywordsPredicate extends FieldContainsKeywordsPredicate {

    /**
     * Creates a predicate that matches a person whose notes match any of {@code keywords}.
     */
    public NoteContainsKeywordsPredicate(List<String> keywords) {
        super(keywords);
    }

    @Override
    protected boolean matches(Person person, String keyword) {
        return StringUtil.containsWordStartingWithIgnoreCase(person.getNote().value, keyword);
    }
}
