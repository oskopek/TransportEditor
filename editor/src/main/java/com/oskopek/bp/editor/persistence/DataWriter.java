/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@matfyz.cz>. All rights reserved.
 */

package com.oskopek.bp.editor.persistence;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Basic methods for writing a  to persistent data storage.
 */
public interface DataWriter {

    /**
     * Writes a to a file in the filesystem.
     *
     * @param fileName the file to write to
     * @throws IOException              if something writing the plan failed
     * @throws IllegalArgumentException if plan or fileName is null
     * @see DataReader#readFrom(String)
     */
    void writeTo(String fileName) throws IOException, IllegalArgumentException;

    /**
     * Writes a  to an {@link OutputStream}.
     *
     * @param outputStream the stream to write to
     * @throws IOException              if something writing the plan failed
     * @throws IllegalArgumentException if plan or outputStream is null
     * @see DataReader#readFrom(java.io.InputStream)
     */
    void writeTo(OutputStream outputStream) throws IOException, IllegalArgumentException;

}
