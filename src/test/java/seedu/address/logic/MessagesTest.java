package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
                + "Address: 123, Jurong West Ave 6, #08-111; Tags: ", formattedPerson);
    }

    @Test
    public void format_personWithoutEmail_omitsEmail() {
        Person person = new PersonBuilder().withEmail("").build();

        String formattedPerson = Messages.format(person);

        assertEquals("Amy Bee; Phone: 85355255; Address: 123, Jurong West Ave 6, #08-111; Tags: ",
                formattedPerson);
    }
}
