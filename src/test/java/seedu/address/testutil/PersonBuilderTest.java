package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.person.VisitDate;

public class PersonBuilderTest {

    @Test
    public void build_copyWithOtherFieldsChanged_preservesVisitRecord() {
        Person original = new PersonBuilder().withPreviousVisitDates("1/9/2026 1000", "2/9/2026 1000")
                .withEmergencyContact("Mary Tan, 91234567").build();
        Person copy = new PersonBuilder(original).build();
        Person edited = new PersonBuilder(original).withName("Changed Name").withPhone("91234567")
                .withEmail("changed@example.com").withAddress("Changed address").withNote("Changed note")
                .withTags("priority").build();

        assertEquals(original, copy);
        assertNotEquals(original, edited);
        assertEquals(original.getVisitDate(), edited.getVisitDate());
        assertEquals(original.getPreviousVisitDates(), edited.getPreviousVisitDates());
        assertEquals(original.getEmergencyContact(), edited.getEmergencyContact());
    }

    @Test
    public void withPreviousVisitDates_reusedBuilder_preservesBuiltRecords() {
        PersonBuilder builder = new PersonBuilder();
        String[] previousVisitDates = {"1/9/2026 1000"};
        builder.withPreviousVisitDates(previousVisitDates);
        previousVisitDates[0] = "2/9/2026 1000";
        Person original = builder.build();
        Person changed = builder.withPreviousVisitDates("3/9/2026 1000").build();
        Person cleared = builder.withPreviousVisitDates().build();

        assertEquals(List.of(new VisitDate("1/9/2026 1000")), original.getPreviousVisitDates());
        assertEquals(List.of(new VisitDate("3/9/2026 1000")), changed.getPreviousVisitDates());
        assertEquals(List.of(), cleared.getPreviousVisitDates());
    }
}
