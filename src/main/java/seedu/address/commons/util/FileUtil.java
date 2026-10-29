package seedu.address.commons.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Writes and reads files.
 */
public class FileUtil {

    /** Writes content to a specified path. */
    @FunctionalInterface
    interface ContentWriter {
        /**
         * Writes {@code contents} to {@code file}.
         *
         * @throws IOException If the content cannot be written.
         */
        void write(Path file, byte[] contents) throws IOException;
    }

    /**
     * Reads the contents of an existing file.
     *
     * @throws IOException If the file cannot be read.
     */
    public static String readFromFile(Path file) throws IOException {
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    /**
     * Writes the given string without exposing a partially written destination file.
     * Creates the destination file and its parent directories if they do not exist.
     *
     * @throws IOException If the content cannot be written or the destination cannot be replaced atomically.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        writeToFile(file, content, (temporaryFile, bytes) -> Files.write(temporaryFile, bytes));
    }

    /**
     * Writes the given string using {@code contentWriter} before replacing the destination file.
     * This overload allows tests to simulate a failure during the temporary-file write.
     */
    static void writeToFile(Path file, String content, ContentWriter contentWriter) throws IOException {
        Path targetFile = file.toAbsolutePath();
        Path parentDirectory = targetFile.getParent();
        Files.createDirectories(parentDirectory);

        Path temporaryFile = Files.createTempFile(parentDirectory, "tmp-" + targetFile.getFileName(), ".tmp");
        try {
            contentWriter.write(temporaryFile, content.getBytes(StandardCharsets.UTF_8));
            Files.move(temporaryFile, targetFile, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

}
