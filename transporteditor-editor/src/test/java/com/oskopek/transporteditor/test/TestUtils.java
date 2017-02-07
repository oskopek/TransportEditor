package com.oskopek.transporteditor.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Various methods for easier testing. Util methods missing in JUnit, Mockito, etc.
 */
public final class TestUtils {

    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

    /**
     * Util class, hide the default constructor.
     */
    private TestUtils() {
        // intentionally empty
    }

    public static <T> void assertNotContains(T element, Collection<T> collection) {
        assertFalse("Collection \"" + collection + "\" contains not expected element \"" + element + "\".",
                collection.contains(element));
    }

    public static <T> void assertContains(T element, Collection<T> collection) {
        assertTrue("Collection \"" + collection + "\" does not contain expected element \"" + element + "\".",
                collection.contains(element));
    }

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
