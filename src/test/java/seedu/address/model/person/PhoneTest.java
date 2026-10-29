package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("91")); // less than 3 numbers
        assertFalse(Phone.isValidPhone("phone")); // non-numeric
        assertFalse(Phone.isValidPhone("9011p041")); // alphabets within digits
        assertFalse(Phone.isValidPhone("---")); // formatting characters without digits
        assertFalse(Phone.isValidPhone("++ ")); // formatting characters without digits
        assertFalse(Phone.isValidPhone("1".repeat(31))); // exceeds the raw string length limit
        assertFalse(Phone.isValidPhone("------123")); // separators before the first digit
        assertFalse(Phone.isValidPhone("123+456")); // '+' after the start
        assertFalse(Phone.isValidPhone("++65 91234567")); // more than one '+'
        assertFalse(Phone.isValidPhone("9123  4567")); // two spaces in a row
        assertFalse(Phone.isValidPhone("9123--4567")); // two dashes in a row
        assertFalse(Phone.isValidPhone("9123 -4567")); // mixed separators in a row
        assertFalse(Phone.isValidPhone("91234567-")); // trailing separator
        assertFalse(Phone.isValidPhone("+ 6591234567")); // separator straight after '+'

        // valid phone numbers
        assertTrue(Phone.isValidPhone("911")); // exactly 3 numbers
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("9312 1534")); // spaces within digits
        assertTrue(Phone.isValidPhone("+65 9312-1534")); // country code, spaces, and dashes
        assertTrue(Phone.isValidPhone("124293842033123")); // long phone numbers
        assertTrue(Phone.isValidPhone("1".repeat(30))); // exactly the raw string length limit
        assertTrue(Phone.isValidPhone("+6591234567")); // country code without separators
        assertTrue(Phone.isValidPhone("9123-4567")); // single dash
        assertTrue(Phone.isValidPhone("1-2-3")); // three digits in single-digit groups
    }

    @Test
    public void equals() {
        Phone phone = new Phone("999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("995")));
    }

    @Test
    public void isSameNumber() {
        Phone phone = new Phone("81234567");

        // same digits with different formatting -> same number
        assertTrue(phone.isSameNumber(new Phone("81234567")));
        assertTrue(phone.isSameNumber(new Phone("8123 4567")));
        assertTrue(phone.isSameNumber(new Phone("8123-4567")));
        assertTrue(new Phone("+65 8123 4567").isSameNumber(new Phone("+65-8123-4567")));

        // different digits, including a country code -> different number
        assertFalse(phone.isSameNumber(new Phone("81234568")));
        assertFalse(phone.isSameNumber(new Phone("+65 8123 4567")));

        // formatting still matters for equality, so a reformatted number is a real edit
        assertFalse(phone.equals(new Phone("8123 4567")));

        assertThrows(NullPointerException.class, () -> phone.isSameNumber(null));
    }
}
