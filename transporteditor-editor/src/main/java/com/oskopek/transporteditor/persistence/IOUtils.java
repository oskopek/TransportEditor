package com.oskopek.transporteditor.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for input/output related methods.
 */
public final class IOUtils {

    /**
     * Default empty constructor.
     */
    private IOUtils() {
        // intentionally empty
    }

    /**
     * Reads all lines via {@link #readAllLines(InputStream)} and joins them with {@code \n}.
     * Appends a trailing {@code \n}.
     *
     * @param stream the stream to read from
     * @return the concatenated string
     * @throws IOException if an error during reading occurs
     */
    public static String concatReadAllLines(InputStream stream) throws IOException {
        return readAllLines(stream).stream().collect(Collectors.joining("\n")) + "\n";
    }

    /**
     * Reads all lines via {@link #readAllLines(InputStream, Charset)} with UTF-8 as the charset.
     *
     * @param stream the stream to read from
     * @return the read lines
     * @throws IOException if an error during reading occurs
     */
    public static List<String> readAllLines(InputStream stream) throws IOException {
        return readAllLines(stream, Charset.forName("UTF-8"));
    }

    /**
     * Reads all lines using a {@link BufferedReader} and the given charset.
     *
     * @param stream the stream to read from
     * @param charset the charset to use for reading from the stream
     * @return the read lines
     * @throws IOException if an error during reading occurs
     */
    public static List<String> readAllLines(InputStream stream, Charset charset) throws IOException {
        if (stream == null) {
            return Collections.emptyList();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset))) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new IOException("An error occurred during reading lines.", e);
        }
    }

}
