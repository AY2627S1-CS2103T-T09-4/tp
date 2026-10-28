package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class EmergencyContactTest {

    @Test
    public void constructor_validContact_normalizesSurroundingWhitespace() {
        EmergencyContact contact = new EmergencyContact("  Mary Tan  ,  91234567  ");
        assertEquals("Mary Tan, 91234567", contact.toString());
        assertFalse(contact.isEmpty());
        assertEquals("91234567 (Mary Tan)", contact.toDisplayString());
        assertEquals("Mary Tan, 123", new EmergencyContact("Mary Tan,123").toString());
    }

    @Test
    public void constructor_emptyContact_representsAbsentContact() {
        EmergencyContact contact = new EmergencyContact("");
        assertTrue(contact.isEmpty());
        assertEquals("", contact.toString());
        assertEquals("", contact.toDisplayString());
    }

    @Test
    public void constructor_phoneWithoutName_normalizesAndDisplaysPhone() {
        for (String phone : List.of("123", "91234567", "+6591234567", "9123 4567")) {
            EmergencyContact contact = new EmergencyContact("  " + phone + "  ");
            assertTrue(EmergencyContact.isValidEmergencyContact(phone));
            assertFalse(contact.isEmpty());
            assertEquals(phone, contact.toString());
            assertEquals(phone, contact.toDisplayString());
            assertEquals(new EmergencyContact(phone), contact);
            assertEquals(new EmergencyContact(phone).hashCode(), contact.hashCode());
            assertNotEquals(new EmergencyContact("Mary Tan, " + phone), contact);
        }
    }

    @Test
    public void constructor_invalidContact_throwsIllegalArgumentException() {
        for (String invalidContact : List.of(" ", "Mary Tan", "12", "abc", "Mary Tan 91234567",
                "Mary Tan,", ",91234567", "Mary Tan, 12", "Mary Tan, 911a",
                "Mary Tan, 9123abcd", "Mary@Tan, 91234567", "Mary Tan, 91234567, 98765432", ",")) {
            assertFalse(EmergencyContact.isValidEmergencyContact(invalidContact));
            assertThrows(IllegalArgumentException.class, () -> new EmergencyContact(invalidContact));
        }
    }

    @Test
    public void constructor_nullContact_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EmergencyContact(null));
        assertThrows(NullPointerException.class, () -> EmergencyContact.isValidEmergencyContact(null));
    }

    @Test
    public void equals_contactDetails_comparesNameAndPhone() {
        EmergencyContact contact = new EmergencyContact("Mary Tan, 91234567");
        EmergencyContact equivalent = new EmergencyContact(" Mary Tan ,91234567 ");
        assertEquals(contact, contact);
        assertEquals(contact, equivalent);
        assertEquals(contact.hashCode(), equivalent.hashCode());
        assertNotEquals(contact, null);
        assertNotEquals(contact, "Mary Tan, 91234567");
        assertNotEquals(contact, new EmergencyContact("John Tan, 91234567"));
        assertNotEquals(contact, new EmergencyContact("Mary Tan, 98765432"));
        assertNotEquals(contact, new EmergencyContact(""));
    }

    @Test
    public void equals_patientContact_preservesPatientIdentity() {
        Person original = new PersonBuilder().build();
        Person withContact = new PersonBuilder(original).withEmergencyContact("Mary Tan, 91234567").build();
        Person copy = new PersonBuilder(withContact).build();

        assertNotEquals(original, withContact);
        assertTrue(original.isSamePerson(withContact));
        assertEquals(withContact, copy);
        assertEquals(withContact.hashCode(), copy.hashCode());
    }
}
