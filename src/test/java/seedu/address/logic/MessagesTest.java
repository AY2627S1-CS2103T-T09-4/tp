package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains tests for user-visible message formatting.
 */
public class MessagesTest {

    @Test
    public void format_personWithEmail_includesEmail() {
        Person person = new PersonBuilder().withEmail("amy@example.com").build();

        String formattedPerson = Messages.format(person);

        assertEquals("Amy Bee; Phone: 85355255; Email: amy@example.com; "
                + "Address: 123, Jurong West Ave 6, #08-111; Visit date: 18/9/2026 1000", formattedPerson);
    }

    @Test
    public void format_personWithoutOptionalFields_omitsUnrecordedFields() {
        Person person = new PersonBuilder().withEmail("").build();

        String formattedPerson = Messages.format(person);

        // no empty "Email: ", "Tags: ", or health-field labels
        assertEquals("Amy Bee; Phone: 85355255; Address: 123, Jurong West Ave 6, #08-111; Visit date: 18/9/2026 1000",
                formattedPerson);
    }

    @Test
    public void format_personWithEveryField_listsVisitAndHealthDetails() {
        Person person = new PersonBuilder().withDateOfBirth("15/6/1990").withBloodType("A+")
                .withAllergies("Penicillin").withAlcoholHistory("Does not drink").withSmokingStatus("former")
                .withEmergencyContact("Mary Tan, 98765432").withNote("Review medication")
                .withTags("morning", "Diabetes").build();

        String formattedPerson = Messages.format(person);

        // the age depends on today's date, so it is taken from the same display method the patient card uses
        String dateOfBirthWithAge = person.getDateOfBirth().toDisplayString();
        assertTrue(dateOfBirthWithAge.matches("15/6/1990 \\(\\d+ years old\\)"), dateOfBirthWithAge);
        assertEquals("Amy Bee; Phone: 85355255; Email: amy@gmail.com; Address: 123, Jurong West Ave 6, #08-111; "
                + "Visit date: 18/9/2026 1000; Date of birth: " + dateOfBirthWithAge + "; Blood type: A+; "
                + "Allergies: Penicillin; Alcohol history: Does not drink; Smoking status: former; "
                + "Emergency contact: 98765432 (Mary Tan); Note: Review medication; Tags: [Diabetes][morning]",
                formattedPerson);
    }
}
