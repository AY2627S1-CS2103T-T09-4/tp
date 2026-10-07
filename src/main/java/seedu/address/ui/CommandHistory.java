package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores commands submitted during the current application session and tracks navigation through them.
 */
class CommandHistory {

    private final List<String> commands = new ArrayList<>();
    private int position;
    private String draft = "";

    /**
     * Records a submitted command and resets navigation to the newest end of the history.
     * Follow-up input requested by a command and empty input are not stored.
     *
     * @param input Submitted command-line input.
     * @param isFollowUpInput Whether the input answers a pending request instead of representing a command.
     */
    void recordSubmission(String input, boolean isFollowUpInput) {
        requireNonNull(input);
        if (!isFollowUpInput && !input.isEmpty()) {
            commands.add(input);
        }
        resetNavigation();
    }

    /**
     * Returns the previous command, or {@code currentInput} when the history is empty.
     * The current input is retained as a draft when navigation starts.
     */
    String getPrevious(String currentInput) {
        requireNonNull(currentInput);
        if (commands.isEmpty()) {
            return currentInput;
        }

        if (position == commands.size()) {
            draft = currentInput;
        }
        if (position > 0) {
            position--;
        }
        return commands.get(position);
    }

    /**
     * Returns the next command, the retained draft after the newest command, or {@code currentInput} when already at
     * the newest end of the history.
     */
    String getNext(String currentInput) {
        requireNonNull(currentInput);
        if (position >= commands.size()) {
            return currentInput;
        }

        position++;
        return position == commands.size() ? draft : commands.get(position);
    }

    /**
     * Resets navigation to the newest end of the history and discards the retained draft.
     */
    private void resetNavigation() {
        position = commands.size();
        draft = "";
    }
}
