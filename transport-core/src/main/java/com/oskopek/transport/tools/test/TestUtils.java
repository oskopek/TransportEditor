package com.oskopek.transport.tools.test;

import com.oskopek.transport.persistence.IOUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Various methods for easier testing. Util methods missing in JUnit, Mockito, etc.
 */
public final class TestUtils {

    /**
     * Util class, hide the default constructor.
     */
    private TestUtils() {
        // intentionally empty
    }

    /**
     * Asserts that the two PDDL strings are equal. Ignores whitespace and comments.
     *
     * @param contents the expected PDDL content
     * @param serialized the serialized PDDL content for comparison
     */
    public static void assertPDDLContentEquals(String contents, String serialized) {
        String contentComp = contents.replaceAll(";.*", "").trim();
        String serializedComp = serialized.replaceAll(";.*", "").trim();

        String noSpaceContentComp = contentComp.replaceAll("\\s+", "");
        String noSpaceSerializedComp = serializedComp.replaceAll("\\s+", "");

        if (!noSpaceContentComp.equals(noSpaceSerializedComp)) {
            if (!contentComp.equals(serializedComp)) {
                throw new AssertionError(contentComp + "\n\n!=\n\n" + serializedComp);
            }
        }
    }

    /**
     * Gets the contents of a file in the test resource persistence directory (usually problems, domain, plans, ...).
     *
     * @param name the name of the file, relative to the persistence directory
     * @return the contents of the text file
     * @throws IOException if an error during reading occurs
     */
    public static String getPersistenceTestFile(String name) throws IOException {
        return IOUtils.concatReadAllLines(Files.newInputStream(
                Paths.get("../transport-core/src/test/resources/com/oskopek/transport/persistence/" + name)));
    }

}
