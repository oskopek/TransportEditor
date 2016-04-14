package com.oskopek.bp.editor.persistence;

import java.io.IOException;
import java.io.InputStream;

/**
 * Basic methods for reading a  from persistent data storage.
 */
public interface DataReader {

    /**
     * Reads a  from a file in the filesystem.
     *
     * @param fileName the file from which to read
     * @return an initialized StudyPlan
     * @throws IOException              if reading the plan failed
     * @throws IllegalArgumentException if fileName is null
     */
    Object readFrom(String fileName) throws IOException, IllegalArgumentException;

    /**
     * Reads a  from the stream.
     *
     * @param inputStream the stream from which to read
     * @return an initialized StudyPlan
     * @throws IOException              if reading the plan failed
     * @throws IllegalArgumentException if the inputStream is null
     */
    Object readFrom(InputStream inputStream) throws IOException, IllegalArgumentException;

}
