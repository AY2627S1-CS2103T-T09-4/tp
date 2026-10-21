package seedu.address.model.person;

import java.util.List;

/**
 * Tests that a {@code Person}'s {@code Phone} contains any of the keywords given anywhere in the number,
 * so that a remembered part of a number, such as its last four digits, is enough.
 */
public class PhoneContainsKeywordsPredicate extends FieldContainsKeywordsPredicate {

    /**
     * Creates a predicate that matches a person whose phone number matches any of {@code keywords}.
     */
    public PhoneContainsKeywordsPredicate(List<String> keywords) {
        super(keywords);
    }

    @Override
    protected boolean matches(Person person, String keyword) {
        return person.getPhone().value.contains(keyword);
    }
}
