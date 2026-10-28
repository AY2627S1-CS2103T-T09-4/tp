package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;

import java.time.LocalDate;
import java.time.Period;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    private static final LocalDate CURRENT_DATE = LocalDate.of(2026, 10, 14);

    @BeforeAll
    public static void setUpJavaFx() throws InterruptedException {
        startJavaFx();
    }

    @Test
    public void constructor_clearedDateOfBirth_hidesRowWithoutLeavingSpace() {
        runOnFxThread(() -> {
            Person recorded = new PersonBuilder().withDateOfBirth("15/6/1990").build();
            Person cleared = new PersonBuilder(recorded).withDateOfBirth("").build();
            PersonCard card = new PersonCard(cleared, 1);
            Label dateOfBirth = (Label) card.getRoot().lookup("#dateOfBirth");

            assertEquals("", dateOfBirth.getText());
            assertFalse(dateOfBirth.isVisible());
            assertFalse(dateOfBirth.isManaged());
        });
    }

    @Test
    public void constructor_recordedDateOfBirth_displaysDateAndAgeBelowName() {
        runOnFxThread(() -> {
            Person patient = new PersonBuilder().withDateOfBirth("15/6/1990").build();
            PersonCard card = new PersonCard(patient, 1);
            Label dateOfBirth = (Label) card.getRoot().lookup("#dateOfBirth");
            int age = Period.between(LocalDate.of(1990, 6, 15), LocalDate.now()).getYears();

            assertEquals("Date of birth: 15/6/1990 (" + age + " years old)", dateOfBirth.getText());
            assertTrue(dateOfBirth.isVisible());
            assertTrue(dateOfBirth.isManaged());
            assertTrue(dateOfBirth.isWrapText());
            VBox fields = (VBox) dateOfBirth.getParent();
            assertEquals(fields.getChildren().indexOf(card.getRoot().lookup("#name").getParent()) + 1,
                    fields.getChildren().indexOf(dateOfBirth));
        });
    }

    @Test
    public void constructor_recordedBloodType_displaysBloodTypeBelowDateOfBirth() {
        runOnFxThread(() -> {
            Person patient = new PersonBuilder().withDateOfBirth("15/6/1990").withBloodType("A+").build();
            PersonCard card = new PersonCard(patient, 1);
            Label bloodType = (Label) card.getRoot().lookup("#bloodType");

            assertEquals("Blood type: A+", bloodType.getText());
            assertTrue(bloodType.isVisible());
            assertTrue(bloodType.isManaged());
            assertTrue(bloodType.isWrapText());
            VBox fields = (VBox) bloodType.getParent();
            assertEquals(fields.getChildren().indexOf(card.getRoot().lookup("#dateOfBirth")) + 1,
                    fields.getChildren().indexOf(bloodType));
        });
    }

    @Test
    public void constructor_clearedBloodType_hidesRowWithoutLeavingSpace() {
        runOnFxThread(() -> {
            Person recorded = new PersonBuilder().withDateOfBirth("15/6/1990").withBloodType("A+").build();
            Person cleared = new PersonBuilder(recorded).withBloodType("").build();
            PersonCard card = new PersonCard(cleared, 1);
            Label bloodType = (Label) card.getRoot().lookup("#bloodType");

            assertEquals("", bloodType.getText());
            assertFalse(bloodType.isVisible());
            assertFalse(bloodType.isManaged());
        });
    }

    @Test
    public void constructor_emptyEmailAndNote_hidesRowsWithoutLeavingSpace() {
        runOnFxThread(() -> {
            Person patient = new PersonBuilder().withEmail("").withNote("").build();
            PersonCard card = new PersonCard(patient, 1);
            Label email = (Label) card.getRoot().lookup("#email");
            Label note = (Label) card.getRoot().lookup("#note");

            assertEquals("", email.getText());
            assertFalse(email.isVisible());
            assertFalse(email.isManaged());
            assertEquals("", note.getText());
            assertFalse(note.isVisible());
            assertFalse(note.isManaged());
        });
    }

    @Test
    public void constructor_recordedEmailAndNote_displaysRows() {
        runOnFxThread(() -> {
            Person patient = new PersonBuilder().withEmail("amy@example.com")
                    .withNote("Call before the next visit").build();
            PersonCard card = new PersonCard(patient, 1);
            Label email = (Label) card.getRoot().lookup("#email");
            Label note = (Label) card.getRoot().lookup("#note");

            assertEquals("amy@example.com", email.getText());
            assertTrue(email.isVisible());
            assertTrue(email.isManaged());
            assertEquals("Call before the next visit", note.getText());
            assertTrue(note.isVisible());
            assertTrue(note.isManaged());
        });
    }

    @Test
    public void publicConstructor_pastVisit_addsPastVisitStyle() {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withVisitDate("1/1/2000 1000").build();

            PersonCard personCard = new PersonCard(person, 1);

            assertTrue(personCard.getRoot().getStyleClass().contains("past-visit-card"));
        });
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
