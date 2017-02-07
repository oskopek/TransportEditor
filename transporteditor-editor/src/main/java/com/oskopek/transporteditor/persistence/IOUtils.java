package com.oskopek.transporteditor.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class IOUtils {

    private IOUtils() {
        // intentionally empty
    }

    public static String concatReadAllLines(InputStream stream) throws IOException {
        return readAllLines(stream).stream().collect(Collectors.joining("\n")) + "\n";
    }

    public static List<String> readAllLines(InputStream stream) throws IOException {
        return readAllLines(stream, Charset.forName("UTF-8"));
    }

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
