package seedu.address.model.person;

import java.util.List;

/**
 * Tests that a {@code Person}'s {@code Phone} contains any of the keywords given anywhere in the number,
 * so that a remembered part of a number, such as its last four digits, is enough.
 * Only digits are compared, so {@code 91234567}, {@code 9123 4567}, and {@code +65 9123-4567} match each other.
 */
public class PhoneContainsKeywordsPredicate extends FieldContainsKeywordsPredicate {

    /**
     * Creates a predicate that matches a person whose phone number matches any of {@code keywords}.
     */
    public PhoneContainsKeywordsPredicate(List<String> keywords) {
        super(keywords);
    }

    /**
     * Returns true if the digits of {@code keyword} appear together in the digits of the person's phone number.
     * A keyword without digits never matches, since it would otherwise match every phone number.
     */
    @Override
    protected boolean matches(Person person, String keyword) {
        String keywordDigits = digitsOf(keyword);
        return !keywordDigits.isEmpty() && digitsOf(person.getPhone().value).contains(keywordDigits);
    }

    /**
     * Returns {@code text} with every character except the digits 0 to 9 removed.
     */
    private static String digitsOf(String text) {
        return text.replaceAll("[^0-9]", "");
    }
}
