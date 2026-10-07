package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandBoxTest {

    @Test
    public void shouldSubmit_emptyTextWithoutRequest_returnsFalse() {
        assertFalse(CommandBox.shouldSubmit("", false));
    }

    @Test
    public void shouldSubmit_emptyTextWithRequest_returnsTrue() {
        assertTrue(CommandBox.shouldSubmit("", true));
    }

    @Test
    public void shouldSubmit_nonEmptyText_returnsTrue() {
        assertTrue(CommandBox.shouldSubmit("list", false));
    }
}
