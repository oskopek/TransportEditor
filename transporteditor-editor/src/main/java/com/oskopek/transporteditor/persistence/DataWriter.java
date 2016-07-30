package com.oskopek.transporteditor.persistence;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Basic methods for writing a persistable object to persistent data storage.
 *
 * @param <Persistable_> the persistable object type
 */
public interface DataWriter<Persistable_> {

    /**
     * Writes a persistable object to a file in the filesystem.
     *
     * @param persistable the object to persist
     * @param fileName the file to write to
     * @throws IOException if something writing the persistable failed
     * @throws IllegalArgumentException if persistable or fileName is null
     */
    void writeTo(Persistable_ persistable, String fileName) throws IOException, IllegalArgumentException;

    /**
     * Writes a persistable object to an {@link OutputStream}.
     *
     * @param persistable the object to persist
     * @param outputStream the stream to write to
     * @throws IOException if something writing the persistable failed
     * @throws IllegalArgumentException if persistable or outputStream is null
     */
    void writeTo(Persistable_ persistable, OutputStream outputStream) throws IOException, IllegalArgumentException;

}
