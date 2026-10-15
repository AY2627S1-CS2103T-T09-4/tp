package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

public class StringUtilTest {

    //---------------- Tests for isNonZeroUnsignedInteger --------------------------------------

    @Test
    public void isNonZeroUnsignedInteger() {

        // EP: empty strings
        assertFalse(StringUtil.isNonZeroUnsignedInteger("")); // Boundary value
        assertFalse(StringUtil.isNonZeroUnsignedInteger("  "));

        // EP: not a number
        assertFalse(StringUtil.isNonZeroUnsignedInteger("a"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("aaa"));

        // EP: zero
        assertFalse(StringUtil.isNonZeroUnsignedInteger("0"));

        // EP: zero as prefix
        assertTrue(StringUtil.isNonZeroUnsignedInteger("01"));

        // EP: signed numbers
        assertFalse(StringUtil.isNonZeroUnsignedInteger("-1"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("+1"));

        // EP: numbers with white space
        assertFalse(StringUtil.isNonZeroUnsignedInteger(" 10 ")); // Leading/trailing spaces
        assertFalse(StringUtil.isNonZeroUnsignedInteger("1 0")); // Spaces in the middle

        // EP: number larger than Integer.MAX_VALUE
        assertFalse(StringUtil.isNonZeroUnsignedInteger(Long.toString(Integer.MAX_VALUE + 1)));

        // EP: valid numbers, should return true
        assertTrue(StringUtil.isNonZeroUnsignedInteger("1")); // Boundary value
        assertTrue(StringUtil.isNonZeroUnsignedInteger("10"));
    }


    //---------------- Tests for containsWordStartingWithIgnoreCase --------------------------------------

    /*
     * Invalid equivalence partitions for prefix: null, empty, multiple words
     * Invalid equivalence partitions for sentence: null
     * The four test cases below test one invalid input at a time.
     */

    @Test
    public void containsWordStartingWithIgnoreCase_nullPrefix_throwsNullPointerException() {
        assertThrows(NullPointerException.class, ()
            -> StringUtil.containsWordStartingWithIgnoreCase("typical sentence", null));
    }

    @Test
    public void containsWordStartingWithIgnoreCase_emptyPrefix_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, "Prefix parameter cannot be empty", ()
            -> StringUtil.containsWordStartingWithIgnoreCase("typical sentence", "  "));
    }

    @Test
    public void containsWordStartingWithIgnoreCase_multipleWords_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, "Prefix parameter should be a single word", ()
            -> StringUtil.containsWordStartingWithIgnoreCase("typical sentence", "aaa BBB"));
    }

    @Test
    public void containsWordStartingWithIgnoreCase_nullSentence_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsWordStartingWithIgnoreCase(null, "abc"));
    }

    /*
     * Valid equivalence partitions for prefix:
     *   - single character
     *   - part of a word
     *   - whole word
     *   - prefix containing symbols/numbers
     *   - prefix with leading/trailing spaces
     *
     * Valid equivalence partitions for sentence:
     *   - empty string
     *   - one word
     *   - multiple words
     *   - sentence with extra spaces
     *
     * Possible scenarios returning true:
     *   - prefix starts the first, middle or last word in sentence
     *   - prefix starts multiple words
     *
     * Possible scenarios returning false:
     *   - prefix appears only in the middle or at the end of a word
     *   - prefix is longer than every word it starts
     *
     * The test method below tries to verify all above with a reasonably low number of test cases.
     */

    @Test
    public void containsWordStartingWithIgnoreCase_validInputs_correctResult() {

        // Empty sentence
        assertFalse(StringUtil.containsWordStartingWithIgnoreCase("", "abc")); // Boundary case
        assertFalse(StringUtil.containsWordStartingWithIgnoreCase("    ", "123"));

        // Prefix does not start any word
        assertFalse(StringUtil.containsWordStartingWithIgnoreCase("abc def", "bc")); // Middle of a word
        assertFalse(StringUtil.containsWordStartingWithIgnoreCase("abc def", "ef")); // End of a word
        assertFalse(StringUtil.containsWordStartingWithIgnoreCase("abc def", "abcd")); // Prefix longer than word

        // Prefix starts a word in the sentence, different upper/lower case letters
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("aaa bBb ccc", "A")); // Single character
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("aaa bBb ccc", "Bb")); // Middle word, partial
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("aaa bBb ccc@1", "CCc@1")); // Last word, whole
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("  AAA   bBb   ccc  ", "aa")); // Extra spaces
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("Aaa", "aaa")); // Only one word (boundary case)
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("aaa bbb ccc", "  cc  ")); // Leading/trailing spaces

        // Prefix starts multiple words in sentence
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("AAA bBb ccc  bbb", "b"));
    }

    //---------------- Tests for getDetails --------------------------------------

    /*
     * Equivalence Partitions: null, valid throwable object
     */

    @Test
    public void getDetails_exceptionGiven() {
        assertTrue(StringUtil.getDetails(new FileNotFoundException("file not found"))
            .contains("java.io.FileNotFoundException: file not found"));
    }

    @Test
    public void getDetails_nullGiven_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.getDetails(null));
    }

}
