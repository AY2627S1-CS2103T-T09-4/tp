package seedu.address.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.VisitDate;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    /**
     * Returns the patients shown when Doc starts without an existing data file.
     */
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("George Miller"), new Phone("88765432"),
                new Email("george.miller@example.com"), new Address("8 Maple Grove"),
                new VisitDate("5/10/2026 1530"), new Note("Review mobility exercises"),
                getTagSet("routine")),
            new Person(new Name("Priya Nair"), new Phone("81234567"),
                new Email("priya.nair@example.com"), new Address("21 Bukit Timah Road, #05-12"),
                new VisitDate("15/10/2026 0900"), new Note("Check blood pressure"),
                getTagSet("morning")),
            new Person(new Name("Aisha Rahman"), new Phone("82345678"),
                new Email("aisha.rahman@example.com"), new Address("12 Cedar Close"),
                new VisitDate("12/10/2026 1000"), new Note("Discuss glucose readings"),
                getTagSet("followup")),
            new Person(new Name("Marcus Lim"), new Phone("87654321"),
                new Email("marcus.lim@example.com"), new Address("46 Tampines Street 21, #08-03"),
                new VisitDate("16/10/2026 1400"), new Note("Review medication dosage"),
                getTagSet("afternoon")),
            new Person(new Name("Mei Lin Tan"), new Phone("83456789"),
                new Email("meilin.tan@example.com"), new Address("77 Ang Mo Kio Avenue 4, #03-18"),
                new VisitDate("14/10/2026 0800"), new Note("Routine wound dressing"),
                getTagSet("today")),
            new Person(new Name("Daniel Wong"), new Phone("84567890"),
                new Email("daniel.wong@example.com"), new Address("17 Sunset Way"),
                new VisitDate("20/10/2026 1100"), new Note("Check recovery progress"),
                getTagSet("followup")),
            new Person(new Name("Noor Aziz"), new Phone("85678901"),
                new Email("noor.aziz@example.com"), new Address("30 Bedok North Street 3, #10-22"),
                new VisitDate("1/10/2026 1330"), new Note("Confirm next prescription"),
                getTagSet("routine"))
        };
    }

    /**
     * Returns an address book populated with the sample patients.
     */
    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

}
