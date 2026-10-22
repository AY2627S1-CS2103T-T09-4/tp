package seedu.address.model.util;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.AlcoholHistory;
import seedu.address.model.person.Allergies;
import seedu.address.model.person.BloodType;
import seedu.address.model.person.DateOfBirth;
import seedu.address.model.person.Email;
import seedu.address.model.person.EmergencyContact;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.SmokingStatus;
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
            new Person(new Name("Amelia Tan"), new DateOfBirth("14/3/1958"), BloodType.A_POSITIVE,
                    new Allergies("Penicillin"), new AlcoholHistory("Does not drink"), SmokingStatus.NEVER,
                    new Phone("81234567"), new Email("amelia.tan@example.com"),
                    new Address("18 Tampines Street 21, #04-12"), new VisitDate("20/10/2026 0900"),
                    List.of(new VisitDate("1/9/2026 0900"), new VisitDate("15/9/2026 0930")),
                    new Note("Review insulin dosage and glucose readings"),
                    new EmergencyContact("Wei Ming Tan, 91234567"), getTagSet("diabetes", "homevisit")),
            new Person(new Name("Benjamin Lim"), new DateOfBirth("2/11/1949"), BloodType.O_POSITIVE,
                    new Allergies("No known allergies"), new AlcoholHistory("Occasional drinking"),
                    SmokingStatus.FORMER, new Phone("82345678"), new Email(""),
                    new Address("7 Bedok North Avenue 2, #08-31"), new VisitDate("22/10/2026 1100"),
                    List.of(new VisitDate("5/10/2026 1030")),
                    new Note("Check blood pressure before medication review"), new EmergencyContact("92345678"),
                    getTagSet("cardiac", "morning")),
            new Person(new Name("Chloe Ng"), new DateOfBirth("28/7/1972"), BloodType.B_NEGATIVE,
                    new Allergies("Latex"), new AlcoholHistory(""), SmokingStatus.UNKNOWN,
                    new Phone("83456789"), new Email("chloe.ng@example.com"),
                    new Address("61 Clementi Avenue 3, #06-08"), new VisitDate("24/10/2026 1430"),
                    List.of(new VisitDate("10/10/2026 1400")),
                    new Note("Inspect wound healing and replace dressing"),
                    new EmergencyContact("Aaron Ng, 93456789"), getTagSet("woundcare", "followup")),
            new Person(new Name("David Koh"), new DateOfBirth("9/1/1963"), BloodType.AB_POSITIVE,
                    new Allergies("Sulfonamides"), new AlcoholHistory("Does not drink"), SmokingStatus.NEVER,
                    new Phone("84567890"), new Email("david.koh@example.com"),
                    new Address("4 Hougang Avenue 5, #02-17"), new VisitDate("30/10/2026 0800"), List.of(),
                    new Note("Assess mobility after physiotherapy"), new EmergencyContact("94567890"),
                    getTagSet("mobility", "morning")),
            new Person(new Name("Farah Aziz"), new DateOfBirth("18/6/1980"), BloodType.O_NEGATIVE,
                    new Allergies("No known allergies"), new AlcoholHistory("Does not drink"), SmokingStatus.NEVER,
                    new Phone("85678901"), new Email("farah.aziz@example.com"),
                    new Address("29 Pasir Ris Drive 6, #10-05"), new VisitDate("30/10/2026 0900"), List.of(),
                    new Note("Review asthma action plan"), new EmergencyContact("Nur Aziz, 95678901"),
                    getTagSet("respiratory", "morning")),
            new Person(new Name("Grace Wong"), new DateOfBirth("30/12/1955"), BloodType.A_NEGATIVE,
                    new Allergies("Aspirin"), new AlcoholHistory("Former regular drinker"), SmokingStatus.FORMER,
                    new Phone("86789012"), new Email("grace.wong@example.com"),
                    new Address("103 Yishun Ring Road, #11-44"), new VisitDate("30/10/2026 1000"), List.of(),
                    new Note("Review fall risk and home safety"), new EmergencyContact("96789012"),
                    getTagSet("falls", "homevisit")),
            new Person(new Name("Jordan Lee"), new DateOfBirth("6/5/1991"), BloodType.B_POSITIVE,
                    new Allergies("Peanuts"), new AlcoholHistory("Social drinking"), SmokingStatus.CURRENT,
                    new Phone("87890123"), new Email("jordan.lee.one@example.com"),
                    new Address("52 Sengkang East Way, #03-19"), new VisitDate("30/10/2026 1100"), List.of(),
                    new Note("Discuss smoking cessation options"), new EmergencyContact("97890123"),
                    getTagSet("smoking", "counselling")),
            new Person(new Name("Jordan Lee"), new DateOfBirth("21/8/1986"), BloodType.AB_NEGATIVE,
                    new Allergies("No known allergies"), new AlcoholHistory("Occasional drinking"),
                    SmokingStatus.NEVER, new Phone("88901234"), new Email("jordan.lee.two@example.com"),
                    new Address("14 Jurong West Street 41, #07-22"), new VisitDate("30/10/2026 1200"), List.of(),
                    new Note("Follow up on migraine frequency"), new EmergencyContact("98901234"),
                    getTagSet("neurology", "followup")),
            new Person(new Name("Kavita Rao"), new DateOfBirth("11/4/1975"), BloodType.UNKNOWN,
                    new Allergies(""), new AlcoholHistory(""), SmokingStatus.UNRECORDED, new Phone("89012345"),
                    new Email(""), new Address("88 Serangoon Central, #05-09"),
                    new VisitDate("30/10/2026 1300"), List.of(), new Note("Collect baseline observations"),
                    new EmergencyContact(""), getTagSet("newpatient")),
            new Person(new Name("Lucas Chen"), new DateOfBirth("17/9/1968"), BloodType.O_POSITIVE,
                    new Allergies("Shellfish"), new AlcoholHistory("Does not drink"), SmokingStatus.FORMER,
                    new Phone("90123456"), new Email("lucas.chen@example.com"),
                    new Address("35 Toa Payoh Lorong 4, #09-16"), new VisitDate("30/10/2026 1400"), List.of(),
                    new Note("Check recovery after hospital discharge"),
                    new EmergencyContact("Mei Chen, 91123456"), getTagSet("postdischarge", "afternoon"))
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
