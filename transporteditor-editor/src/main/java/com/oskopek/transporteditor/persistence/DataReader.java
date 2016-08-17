package com.oskopek.transporteditor.persistence;

/**
 * Basic methods for parsing any persistable object from a string serialized form.
 *
 * @param <Persistable_> the persistable object type
 */
public interface DataReader<Persistable_> {

    /**
     * Parse a persistable object from a string.
     *
     * @param contents the string representation of the persistable
     * @return an initialized instance of the persistable object
     * @throws IllegalArgumentException if the contents cannot be parsed
     */
    Persistable_ parse(String contents) throws IllegalArgumentException;
}
