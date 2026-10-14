package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    private static final LocalDate CURRENT_DATE = LocalDate.of(2026, 10, 14);

    @BeforeAll
    public static void setUpJavaFx() throws InterruptedException {
        startJavaFx();
    }

    @Test
    public void constructor_visitBeforeCurrentDate_addsPastVisitStyle() {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withVisitDate("13/10/2026 1000").build();

            PersonCard personCard = new PersonCard(person, 1, CURRENT_DATE);

            assertTrue(personCard.getRoot().getStyleClass().contains("past-visit-card"));
        });
    }

    @Test
    public void constructor_visitOnCurrentDate_doesNotAddPastVisitStyle() {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withVisitDate("14/10/2026 0000").build();

            PersonCard personCard = new PersonCard(person, 1, CURRENT_DATE);

            assertFalse(personCard.getRoot().getStyleClass().contains("past-visit-card"));
        });
    }

    @Test
    public void constructor_visitAfterCurrentDate_doesNotAddPastVisitStyle() {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withVisitDate("15/10/2026 1000").build();

            PersonCard personCard = new PersonCard(person, 1, CURRENT_DATE);

            assertFalse(personCard.getRoot().getStyleClass().contains("past-visit-card"));
        });
    }
}
