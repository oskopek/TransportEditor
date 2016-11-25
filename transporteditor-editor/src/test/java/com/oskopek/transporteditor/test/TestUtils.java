package com.oskopek.transporteditor.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    /**
     * Checks if an instance (could be a subclass) of a given {@link Throwable} was thrown.
     * If not, returns false and logs the exception at the {@code debug} level. Does not rethrow.
     *
     * @param runnable the code to run (runs in the same thread as this method)
     * @param throwable the throwable to check for
     * @return true iff a {@link Throwable} that is an instance of the given {@code throwable} was thrown
     */
    public static boolean isThrown(Runnable runnable, Class<? extends Throwable> throwable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            logger.debug("isThrown expecting {}, caught {}", throwable, t.getClass());
            if (throwable.isInstance(t)) {
                return true;
            }
            logger.debug("Throwable detail: {}", t.toString());
        }
        return false;
    }

    public static String readAllConcatenatedLines(InputStream stream) {
        return readAllLines(stream).stream().collect(Collectors.joining("\n")) + "\n";
    }

    public static List<String> readAllLines(InputStream stream) {
        return readAllLines(stream, Charset.forName("UTF-8"));
    }

    public static List<String> readAllLines(InputStream stream, Charset charset) {
        List<String> lines = new ArrayList<>();
        if (stream == null) {
            return lines;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset))) {
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                lines.add(buffer);
            }
        } catch (IOException e) {
            throw new IllegalStateException("An error occurred during reading lines.", e);
        }
        return lines;
    }

}
