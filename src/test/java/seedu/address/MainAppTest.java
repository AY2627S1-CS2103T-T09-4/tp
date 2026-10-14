package seedu.address;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.ui.UiManager;

public class MainAppTest {

    @Test
    public void init_createsUiManagerWithBrowserOpener() throws Exception {
        TestMainApp mainApp = new TestMainApp();

        mainApp.init();

        assertTrue(mainApp.ui instanceof UiManager);
    }

    private static class TestMainApp extends MainApp {

        @Override
        protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
            return new UserPrefs();
        }
    }
}
