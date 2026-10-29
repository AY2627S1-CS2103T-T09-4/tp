package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMERGENCY_CONTACT;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.EmergencyContact;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;

/**
 * Tests that generated commands preserve emergency contact intentions when executed.
 */
public class PersonUtilTest {

    @Test
    public void getAddCommand_recordedContact_preservesContact() throws Exception {
        for (String contact : List.of("Mary Tan, 98765432", "98765432")) {
            Person expected = new PersonBuilder().withEmergencyContact(contact).build();
            Model model = new ModelManager();

            new AddressBookParser().parseCommand(PersonUtil.getAddCommand(expected)).execute(model);

            assertEquals(expected, model.getFilteredPersonList().getFirst());
        }
    }

    @Test
    public void getAddCommand_absentContact_omitsContact() throws Exception {
        Person expected = new PersonBuilder().build();
        String command = PersonUtil.getAddCommand(expected);
        Model model = new ModelManager();

        assertFalse(command.contains(PREFIX_EMERGENCY_CONTACT.toString()));
        new AddressBookParser().parseCommand(command).execute(model);

        assertEquals(expected, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void getEditPersonDescriptorDetails_recordedContact_replacesContact() throws Exception {
        for (String contact : List.of("David Tan, 87654321", "87654321")) {
            EditPersonDescriptor descriptor = new EditPersonDescriptor();
            descriptor.setEmergencyContact(new EmergencyContact(contact));
            Person original = new PersonBuilder().withEmergencyContact("Mary Tan, 98765432").build();
            Person expected = new PersonBuilder(original).withEmergencyContact(contact).build();

            assertGeneratedEditResult(original, descriptor, expected);
        }
    }

    @Test
    public void getEditPersonDescriptorDetails_emptyContact_clearsContact() throws Exception {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setEmergencyContact(new EmergencyContact(""));
        Person original = new PersonBuilder().withEmergencyContact("Mary Tan, 98765432").build();
        Person expected = new PersonBuilder(original).withEmergencyContact("").build();

        assertGeneratedEditResult(original, descriptor, expected);
    }

    @Test
    public void getEditPersonDescriptorDetails_omittedContact_preservesContact() throws Exception {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setName(new Name("Amy Tan"));
        Person original = new PersonBuilder().withEmergencyContact("Mary Tan, 98765432").build();
        Person expected = new PersonBuilder(original).withName("Amy Tan").build();

        assertFalse(PersonUtil.getEditPersonDescriptorDetails(descriptor)
                .contains(PREFIX_EMERGENCY_CONTACT.toString()));
        assertGeneratedEditResult(original, descriptor, expected);
    }

    private void assertGeneratedEditResult(Person original, EditPersonDescriptor descriptor, Person expected)
            throws Exception {
        Model model = new ModelManager();
        model.addPerson(original);
        String command = "edit 1 " + PersonUtil.getEditPersonDescriptorDetails(descriptor);

        new AddressBookParser().parseCommand(command).execute(model);

        assertEquals(expected, model.getFilteredPersonList().getFirst());
    }
}
