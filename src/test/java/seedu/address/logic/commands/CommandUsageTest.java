package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Verifies that command usage messages use the terminology shown in the Help window.
 */
public class CommandUsageTest {

    @Test
    public void addCommandUsage_matchesHelpWindowTerminology() {
        assertTrue(AddCommand.MESSAGE_USAGE.contains(
                "n/NAME p/PHONE a/ADDRESS vd/DATETIME [e/EMAIL] [ec/[NAME, ]PHONE_NUMBER] "
                        + "[dob/DATE] [bt/BLOOD_TYPE] [all/ALLERGIES] [alc/ALCOHOL_HISTORY] "
                        + "[smk/STATUS] [note/NOTES] [t/TAG]..."));
    }

    @Test
    public void editCommandUsage_matchesHelpWindowTerminology() {
        assertTrue(EditCommand.MESSAGE_USAGE.contains(
                "[n/NAME] [p/PHONE] [a/ADDRESS] [vd/DATETIME] [e/EMAIL] [ec/[NAME, ]PHONE_NUMBER] "
                        + "[dob/DATE] [bt/BLOOD_TYPE] [all/ALLERGIES] [alc/ALCOHOL_HISTORY] "
                        + "[smk/STATUS] [note/NOTES] [t/TAG]..."));
    }
}
