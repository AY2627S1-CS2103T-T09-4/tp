package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class SampleDataUtilTest {

    private static final Person[] EXPECTED_SAMPLE_PERSONS = {
        new PersonBuilder().withName("George Miller").withPhone("88765432")
                .withEmail("george.miller@example.com").withAddress("8 Maple Grove")
                .withVisitDate("5/10/2026 1530").withNote("Review mobility exercises")
                .withTags("routine").build(),
        new PersonBuilder().withName("Priya Nair").withPhone("81234567")
                .withEmail("priya.nair@example.com").withAddress("21 Bukit Timah Road, #05-12")
                .withVisitDate("15/10/2026 0900").withNote("Check blood pressure")
                .withTags("morning").build(),
        new PersonBuilder().withName("Aisha Rahman").withPhone("82345678")
                .withEmail("aisha.rahman@example.com").withAddress("12 Cedar Close")
                .withVisitDate("12/10/2026 1000").withNote("Discuss glucose readings")
                .withTags("followup").build(),
        new PersonBuilder().withName("Marcus Lim").withPhone("87654321")
                .withEmail("marcus.lim@example.com").withAddress("46 Tampines Street 21, #08-03")
                .withVisitDate("16/10/2026 1400").withNote("Review medication dosage")
                .withTags("afternoon").build(),
        new PersonBuilder().withName("Mei Lin Tan").withPhone("83456789")
                .withEmail("meilin.tan@example.com").withAddress("77 Ang Mo Kio Avenue 4, #03-18")
                .withVisitDate("14/10/2026 0800").withNote("Routine wound dressing")
                .withTags("morning").build(),
        new PersonBuilder().withName("Daniel Wong").withPhone("84567890")
                .withEmail("daniel.wong@example.com").withAddress("17 Sunset Way")
                .withVisitDate("20/10/2026 1100").withNote("Check recovery progress")
                .withTags("followup").build(),
        new PersonBuilder().withName("Noor Aziz").withPhone("85678901")
                .withEmail("noor.aziz@example.com").withAddress("30 Bedok North Street 3, #10-22")
                .withVisitDate("1/10/2026 1330").withNote("Confirm next prescription")
                .withTags("routine").build()
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
