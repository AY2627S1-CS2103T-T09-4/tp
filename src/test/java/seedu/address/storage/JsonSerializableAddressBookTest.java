package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");
    private static final Path DUPLICATE_NAME_FILE = TEST_DATA_FOLDER.resolve("duplicateNameAddressBook.json");
    private static final Path DUPLICATE_VISIT_DATE_FILE =
            TEST_DATA_FOLDER.resolve("duplicateVisitDateAddressBook.json");

    @Test
    public void toModelType_conflictingVisitRecords_rejectsEitherRecordOrder() throws Exception {
        Person existing = new PersonBuilder().withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000").build();
        for (Person conflicting : List.of(
                new PersonBuilder().withName("Other Patient").withVisitDate("01/10/2026 1000").build(),
                new PersonBuilder().withName("Other Patient").withVisitDate("20/10/2026 1000")
                        .withPreviousVisitDates("10/10/2026 1000").build(),
                new PersonBuilder().withName("Other Patient").withVisitDate("20/10/2026 1000")
                        .withPreviousVisitDates("01/10/2026 1000").build())) {
            for (List<Person> persons : List.of(List.of(existing, conflicting), List.of(conflicting, existing))) {
                JsonSerializableAddressBook adapted = new JsonSerializableAddressBook(
                        persons.stream().map(JsonAdaptedPerson::new).toList());
                String json = JsonUtil.toJsonString(adapted);
                JsonSerializableAddressBook reloaded = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);

                assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_VISIT_DATE,
                        reloaded::toModelType);
            }
        }
    }

    @Test
    public void toModelType_sameDayDifferentTimes_success() throws Exception {
        Person existing = new PersonBuilder().withVisitDate("10/10/2026 1000")
                .withPreviousVisitDates("1/10/2026 1000").build();
        Person other = new PersonBuilder().withName("Other Patient").withVisitDate("10/10/2026 1100")
                .withPreviousVisitDates("1/10/2026 1100").build();
        JsonSerializableAddressBook adapted = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(existing), new JsonAdaptedPerson(other)));

        assertEquals(List.of(existing, other), adapted.toModelType().getPersonList());
    }

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicateNamesWithDifferentPhones_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_NAME_FILE,
                JsonSerializableAddressBook.class).get();

        AddressBook addressBookFromFile = dataFromFile.toModelType();

        assertEquals(2, addressBookFromFile.getPersonList().size());
    }

    @Test
    public void toModelType_duplicateVisitDates_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_VISIT_DATE_FILE,
                JsonSerializableAddressBook.class).get();

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_VISIT_DATE,
                dataFromFile::toModelType);
    }

}
