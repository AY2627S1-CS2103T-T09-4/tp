package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains unsupported punctuation
        assertFalse(Name.isValidName("p/Smith")); // conflicts with the phone prefix
        assertFalse(Name.isValidName("Mary--Ann")); // repeated punctuation
        assertFalse(Name.isValidName("/Smith")); // punctuation at the start
        assertFalse(Name.isValidName("Smith/")); // punctuation at the end

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(Name.isValidName("s/o Rajan")); // slash in a name component
        assertTrue(Name.isValidName("d/o Priya")); // slash in a name component
        assertTrue(Name.isValidName("O'Brien")); // apostrophe in a name component
        assertTrue(Name.isValidName("José")); // accented character
        assertTrue(Name.isValidName("Mary-Ann")); // hyphen in a name component
        assertTrue(Name.isValidName("Mr. Smith")); // title abbreviation
    }

    @Test
    public void isSameName() {
        Name name = new Name("Valid Name");

        assertTrue(name.isSameName(new Name("valid name")));
        assertTrue(name.isSameName(new Name("Valid   Name")));
        assertTrue(name.isSameName(new Name("Valid Name   ")));
        assertFalse(name.isSameName(new Name("Other Valid Name")));
        assertFalse(name.isSameName(null));
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));

        // normalized match with different stored value -> returns false
        assertFalse(name.equals(new Name("valid  name")));
    }
}
