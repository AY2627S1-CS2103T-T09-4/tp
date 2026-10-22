package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class SampleDataUtilTest {

    private static final Person[] EXPECTED_SAMPLE_PERSONS = {
        new PersonBuilder().withName("Amelia Tan").withDateOfBirth("14/3/1958").withBloodType("A+")
                .withAllergies("Penicillin").withAlcoholHistory("Does not drink").withSmokingStatus("never")
                .withPhone("81234567").withEmail("amelia.tan@example.com")
                .withAddress("18 Tampines Street 21, #04-12").withVisitDate("20/10/2026 0900")
                .withPreviousVisitDates("1/9/2026 0900", "15/9/2026 0930")
                .withNote("Review insulin dosage and glucose readings")
                .withEmergencyContact("Wei Ming Tan, 91234567").withTags("diabetes", "homevisit").build(),
        new PersonBuilder().withName("Benjamin Lim").withDateOfBirth("2/11/1949").withBloodType("O+")
                .withAllergies("No known allergies").withAlcoholHistory("Occasional drinking")
                .withSmokingStatus("former").withPhone("82345678").withEmail("")
                .withAddress("7 Bedok North Avenue 2, #08-31").withVisitDate("22/10/2026 1100")
                .withPreviousVisitDates("5/10/2026 1030")
                .withNote("Check blood pressure before medication review").withEmergencyContact("92345678")
                .withTags("cardiac", "morning").build(),
        new PersonBuilder().withName("Chloe Ng").withDateOfBirth("28/7/1972").withBloodType("B-")
                .withAllergies("Latex").withSmokingStatus("unknown").withPhone("83456789")
                .withEmail("chloe.ng@example.com").withAddress("61 Clementi Avenue 3, #06-08")
                .withVisitDate("24/10/2026 1430").withPreviousVisitDates("10/10/2026 1400")
                .withNote("Inspect wound healing and replace dressing")
                .withEmergencyContact("Aaron Ng, 93456789").withTags("woundcare", "followup").build(),
        new PersonBuilder().withName("David Koh").withDateOfBirth("9/1/1963").withBloodType("AB+")
                .withAllergies("Sulfonamides").withAlcoholHistory("Does not drink").withSmokingStatus("never")
                .withPhone("84567890").withEmail("david.koh@example.com")
                .withAddress("4 Hougang Avenue 5, #02-17").withVisitDate("30/10/2026 0800")
                .withNote("Assess mobility after physiotherapy").withEmergencyContact("94567890")
                .withTags("mobility", "morning").build(),
        new PersonBuilder().withName("Farah Aziz").withDateOfBirth("18/6/1980").withBloodType("O-")
                .withAllergies("No known allergies").withAlcoholHistory("Does not drink")
                .withSmokingStatus("never").withPhone("85678901").withEmail("farah.aziz@example.com")
                .withAddress("29 Pasir Ris Drive 6, #10-05").withVisitDate("30/10/2026 0900")
                .withNote("Review asthma action plan").withEmergencyContact("Nur Aziz, 95678901")
                .withTags("respiratory", "morning").build(),
        new PersonBuilder().withName("Grace Wong").withDateOfBirth("30/12/1955").withBloodType("A-")
                .withAllergies("Aspirin").withAlcoholHistory("Former regular drinker").withSmokingStatus("former")
                .withPhone("86789012").withEmail("grace.wong@example.com")
                .withAddress("103 Yishun Ring Road, #11-44").withVisitDate("30/10/2026 1000")
                .withNote("Review fall risk and home safety").withEmergencyContact("96789012")
                .withTags("falls", "homevisit").build(),
        new PersonBuilder().withName("Jordan Lee").withDateOfBirth("6/5/1991").withBloodType("B+")
                .withAllergies("Peanuts").withAlcoholHistory("Social drinking").withSmokingStatus("current")
                .withPhone("87890123").withEmail("jordan.lee.one@example.com")
                .withAddress("52 Sengkang East Way, #03-19").withVisitDate("30/10/2026 1100")
                .withNote("Discuss smoking cessation options").withEmergencyContact("97890123")
                .withTags("smoking", "counselling").build(),
        new PersonBuilder().withName("Jordan Lee").withDateOfBirth("21/8/1986").withBloodType("AB-")
                .withAllergies("No known allergies").withAlcoholHistory("Occasional drinking")
                .withSmokingStatus("never").withPhone("88901234").withEmail("jordan.lee.two@example.com")
                .withAddress("14 Jurong West Street 41, #07-22").withVisitDate("30/10/2026 1200")
                .withNote("Follow up on migraine frequency").withEmergencyContact("98901234")
                .withTags("neurology", "followup").build(),
        new PersonBuilder().withName("Kavita Rao").withDateOfBirth("11/4/1975").withPhone("89012345")
                .withEmail("").withAddress("88 Serangoon Central, #05-09").withVisitDate("30/10/2026 1300")
                .withNote("Collect baseline observations").withTags("newpatient").build(),
        new PersonBuilder().withName("Lucas Chen").withDateOfBirth("17/9/1968").withBloodType("O+")
                .withAllergies("Shellfish").withAlcoholHistory("Does not drink").withSmokingStatus("former")
                .withPhone("90123456").withEmail("lucas.chen@example.com")
                .withAddress("35 Toa Payoh Lorong 4, #09-16").withVisitDate("30/10/2026 1400")
                .withNote("Check recovery after hospital discharge").withEmergencyContact("Mei Chen, 91123456")
                .withTags("postdischarge", "afternoon").build()
    };

    @Test
    public void getSamplePersons_returnsDefaultSamplePersons() {
        assertArrayEquals(EXPECTED_SAMPLE_PERSONS, SampleDataUtil.getSamplePersons());
    }

    @Test
    public void getSampleAddressBook_returnsDefaultSamplePersonsInOrder() {
        assertEquals(List.of(EXPECTED_SAMPLE_PERSONS), SampleDataUtil.getSampleAddressBook().getPersonList());
    }
}
