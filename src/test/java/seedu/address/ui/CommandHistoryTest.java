package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {

    private final CommandHistory history = new CommandHistory();

    @Test
    public void navigate_emptyHistory_preservesCurrentInput() {
        assertEquals("draft", history.getPrevious("draft"));
        assertEquals("draft", history.getNext("draft"));
    }

    @Test
    public void navigate_oneCommand_restoresDraftAfterNewestCommand() {
        history.recordSubmission("list", false);

        assertEquals("list", history.getPrevious("unfinished command"));
        assertEquals("unfinished command", history.getNext("list"));
    }

    @Test
    public void navigate_multipleCommands_stopsAtHistoryBoundaries() {
        history.recordSubmission("first", false);
        history.recordSubmission("second", false);

        assertEquals("second", history.getPrevious("draft"));
        assertEquals("first", history.getPrevious("second"));
        assertEquals("first", history.getPrevious("first"));
        assertEquals("second", history.getNext("first"));
        assertEquals("draft", history.getNext("second"));
        assertEquals("edited draft", history.getNext("edited draft"));
    }

    @Test
    public void recordSubmission_duplicateAndWhitespaceCommands_preservesExactInputAndOrder() {
        history.recordSubmission("  list  ", false);
        history.recordSubmission("list", false);
        history.recordSubmission("list", false);

        assertEquals("list", history.getPrevious(""));
        assertEquals("list", history.getPrevious("list"));
        assertEquals("  list  ", history.getPrevious("list"));
    }

    @Test
    public void recordSubmission_followUpAndEmptyInput_excludesBothFromHistory() {
        history.recordSubmission("delete 1", false);
        history.recordSubmission("y", true);
        history.recordSubmission("", false);

        assertEquals("delete 1", history.getPrevious(""));
        assertEquals("delete 1", history.getPrevious("delete 1"));
    }

    @Test
    public void recordSubmission_whileNavigating_resetsToNewestCommand() {
        history.recordSubmission("first", false);
        history.recordSubmission("second", false);
        history.getPrevious("");
        history.getPrevious("second");

        history.recordSubmission("third", false);

        assertEquals("third", history.getPrevious(""));
        assertEquals("second", history.getPrevious("third"));
    }
}
