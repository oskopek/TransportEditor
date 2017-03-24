package com.oskopek.transporteditor.persistence;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class IOUtilsTest {

    @Test
    public void deleteNonExistingDirectoryRecursively() throws Exception {
        Path nonExistingDir = Paths.get("/tmp/non-existing-folder");
        assertFalse(Files.exists(nonExistingDir));
        IOUtils.deleteDirectoryRecursively(nonExistingDir);
        assertFalse(Files.exists(nonExistingDir));
        Path nonExistingDeepDir = nonExistingDir.resolve("multiple-levels");
        assertFalse(Files.exists(nonExistingDeepDir));
        IOUtils.deleteDirectoryRecursively(nonExistingDeepDir);
        assertFalse(Files.exists(nonExistingDeepDir));
    }

    @Test
    public void deleteNonEmptyDirectoryRecursively() throws Exception {
        Path tempDir = Files.createTempDirectory("teditor-");
        assertTrue(Files.isDirectory(tempDir));
        Path tempFile = Files.createTempFile(tempDir, "teditor-file-", ".txt");
        assertTrue(Files.isRegularFile(tempFile));
        IOUtils.deleteDirectoryRecursively(tempDir);
        assertFalse(Files.exists(tempDir));
        assertFalse(Files.exists(tempFile));
    }

    @Test
    public void deleteEmptyDirectoryRecursively() throws Exception {
        Path tempDir = Files.createTempDirectory("teditor-");
        assertTrue(Files.exists(tempDir));
        IOUtils.deleteDirectoryRecursively(tempDir);
        assertFalse(Files.exists(tempDir));
    }
}
