package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies the AWT browser adapter in a separate JVM configured as headless at startup.
 */
public class HelpWindowDesktopTest {

    private static final long PROCESS_TIMEOUT_SECONDS = 30;

    @TempDir
    Path testDirectory;

    @Test
    public void openInDefaultBrowser_headlessJvm_wrapsFailure() throws Exception {
        Path outputPath = testDirectory.resolve("desktop-browser-probe.log");
        Process process = new ProcessBuilder(createProbeCommand())
                .redirectErrorStream(true)
                .redirectOutput(outputPath.toFile())
                .start();

        try {
            assertTrue(process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "The isolated browser test did not finish");
            assertEquals(0, process.exitValue(), Files.readString(outputPath));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                assertTrue(process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                        "The isolated browser test did not stop");
            }
        }
    }

    private static List<String> createProbeCommand() throws URISyntaxException {
        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        // Reuse JaCoCo's agent so the child contributes to the existing execution-data file.
        ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .filter(argument -> argument.startsWith("-javaagent:") && argument.contains("jacoco"))
                .forEach(command::add);
        command.add("-Djava.awt.headless=true");
        command.add("-cp");
        command.add(getTestClasspath());
        command.add(DesktopBrowserProbe.class.getName());
        return command;
    }

    /**
     * Includes Gradle's isolated test classpath as well as the ordinary JVM classpath used by IDE runners.
     */
    private static String getTestClasspath() throws URISyntaxException {
        List<String> classpathEntries = new ArrayList<>();
        classpathEntries.add(System.getProperty("java.class.path"));
        ClassLoader testClassLoader = HelpWindowDesktopTest.class.getClassLoader();
        if (testClassLoader instanceof URLClassLoader urlClassLoader) {
            for (URL classpathUrl : urlClassLoader.getURLs()) {
                classpathEntries.add(Path.of(classpathUrl.toURI()).toString());
            }
        }
        return String.join(File.pathSeparator, classpathEntries);
    }

    /**
     * Checks the real Desktop adapter in a child JVM without starting JavaFX or opening a browser.
     */
    public static class DesktopBrowserProbe {

        /**
         * Verifies that the adapter wraps the headless Desktop failure.
         *
         * @param args Unused command-line arguments.
         */
        public static void main(String[] args) {
            if (!GraphicsEnvironment.isHeadless()) {
                throw new AssertionError("The browser probe requires headless mode at JVM startup");
            }

            try {
                HelpWindow.openInDefaultBrowser(HelpWindow.USERGUIDE_URL);
            } catch (IllegalStateException exception) {
                if (exception.getCause() instanceof HeadlessException) {
                    return;
                }
                throw new AssertionError("Expected a wrapped HeadlessException", exception);
            }
            throw new AssertionError("Expected the Desktop browser request to fail in headless mode");
        }
    }
}
