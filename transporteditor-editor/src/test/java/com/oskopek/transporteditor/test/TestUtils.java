package com.oskopek.transporteditor.test;

import static org.junit.Assert.*;

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

        if (noSpaceContentComp.equals(noSpaceSerializedComp)) {
            assertTrue(true);
        } else {
            assertEquals(contentComp, serializedComp);
        }
    }



}
