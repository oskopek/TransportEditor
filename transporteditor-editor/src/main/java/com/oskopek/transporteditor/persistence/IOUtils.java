package com.oskopek.transporteditor.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for input/output related methods.
 */
public final class IOUtils {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Default empty constructor.
     */
    private IOUtils() {
        // intentionally empty
    }

    /**
     * Reads all lines via {@link #readAllLines(InputStream)} and joins them with {@code \n}.
     *
     * @param stream the stream to read from
     * @return the concatenated string
     * @throws IOException if an error during reading occurs
     */
    public static String concatReadAllLines(InputStream stream) throws IOException {
        return readAllLines(stream).stream().collect(Collectors.joining("\n"));
    }

    /**
     * Reads all lines via {@link #readAllLines(InputStream, Charset)} with UTF-8 as the charset.
     *
     * @param stream the stream to read from
     * @return the read lines
     * @throws IOException if an error during reading occurs
     */
    public static List<String> readAllLines(InputStream stream) throws IOException {
        return readAllLines(stream, UTF8);
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

    /**
     * Writes the string to a file at the given path, overwriting any file present. Uses UTF-8.
     *
     * @param path the path to write to
     * @param string the string to write
     * @throws IOException if an error during writing occurs
     */
    public static void writeToFile(Path path, String string) throws IOException {
        Files.write(path, Arrays.asList(string), UTF8);
    }

}
