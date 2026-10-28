package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {

    @TempDir
    public Path testFolder;

    @Test
    public void writeToFile_missingFileAndParent_createsFile() throws IOException {
        Path file = testFolder.resolve("missing").resolve("addressBook.json");

        FileUtil.writeToFile(file, "saved data");

        assertEquals("saved data", Files.readString(file));
    }

    @Test
    public void writeToFile_existingFile_replacesFile() throws IOException {
        Path file = testFolder.resolve("addressBook.json");
        Files.writeString(file, "original data");

        FileUtil.writeToFile(file, "replacement data");

        assertEquals("replacement data", Files.readString(file));
    }

    @Test
    public void writeToFile_partialTemporaryWriteFailure_preservesExistingFile() throws IOException {
        Path file = testFolder.resolve("addressBook.json");
        Files.writeString(file, "original data");

        FileUtil.ContentWriter failingWriter = (temporaryFile, contents) -> {
            Files.write(temporaryFile, Arrays.copyOf(contents, 7));
            throw new IOException("Simulated write failure");
        };
        assertThrows(IOException.class, () -> FileUtil.writeToFile(file, "replacement data", failingWriter));

        assertEquals("original data", Files.readString(file));
        try (Stream<Path> files = Files.list(testFolder)) {
            assertFalse(files.anyMatch(path -> !path.equals(file)));
        }
    }
}
