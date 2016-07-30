package com.oskopek.transporteditor.persistence;

import java.io.IOException;
import java.io.InputStream;

/**
 * Basic methods for reading any persistable object from persistent data storage.
 */
public interface DataReader<Persistable_> {

    /**
     * Reads a persistable object from a file in the filesystem.
     *
     * @param fileName the file from which to read
     * @return an initialized instance of the persistable object
     * @throws IOException if reading the persistable failed
     * @throws IllegalArgumentException if fileName is null
     */
    Persistable_ readFrom(String fileName) throws IOException, IllegalArgumentException;

    /**
     * Reads a persistable object from the stream.
     *
     * @param inputStream the stream from which to read
     * @return an initialized instance of the persistable object
     * @throws IOException if reading the persistable failed
     * @throws IllegalArgumentException if the inputStream is null
     */
    Persistable_ readFrom(InputStream inputStream) throws IOException, IllegalArgumentException;
}
