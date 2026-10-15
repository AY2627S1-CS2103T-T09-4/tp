package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * Helper functions for handling strings.
 */
public class StringUtil {

    /**
     * Returns true if any word in the {@code sentence} starts with the {@code prefix}, ignoring case.
     *   <br>examples:<pre>
     *       containsWordStartingWithIgnoreCase("ABc def", "abc") == true
     *       containsWordStartingWithIgnoreCase("ABc def", "a") == true
     *       containsWordStartingWithIgnoreCase("ABc def", "DE") == true
     *       containsWordStartingWithIgnoreCase("ABc def", "bc") == false //not at the start of a word
     *       </pre>
     * @param sentence cannot be null
     * @param prefix cannot be null, cannot be empty, must be a single word
     */
    public static boolean containsWordStartingWithIgnoreCase(String sentence, String prefix) {
        requireNonNull(sentence);
        requireNonNull(prefix);

        String preppedPrefix = prefix.trim();
        checkArgument(!preppedPrefix.isEmpty(), "Prefix parameter cannot be empty");
        checkArgument(preppedPrefix.split("\\s+").length == 1, "Prefix parameter should be a single word");

        String[] wordsInSentence = sentence.split("\\s+");

        // regionMatches with ignoreCase = true acts as a case-insensitive startsWith
        return Arrays.stream(wordsInSentence)
                .anyMatch(word -> word.regionMatches(true, 0, preppedPrefix, 0, preppedPrefix.length()));
    }

    /**
     * Returns a detailed message of {@code t}, including the stack trace.
     */
    public static String getDetails(Throwable t) {
        requireNonNull(t);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return t.getMessage() + "\n" + sw.toString();
    }

    /**
     * Returns true if {@code s} represents a non-zero unsigned integer
     * e.g. 1, 2, 3, ..., {@code Integer.MAX_VALUE} <br>
     * Will return false for any other non-null string input
     * e.g. empty string, "-1", "0", "+1", and " 2 " (untrimmed), "3 0" (contains whitespace), "1 a" (contains letters)
     * @throws NullPointerException if {@code s} is null.
     */
    public static boolean isNonZeroUnsignedInteger(String s) {
        requireNonNull(s);

        try {
            int value = Integer.parseInt(s);
            return value > 0 && !s.startsWith("+"); // "+1" is successfully parsed by Integer#parseInt(String)
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
