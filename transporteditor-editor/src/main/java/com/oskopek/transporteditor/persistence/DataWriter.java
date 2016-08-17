package com.oskopek.transporteditor.persistence;

/**
 * Basic methods for serializing any persistable object to a string serialized form.
 *
 * @param <Persistable_> the persistable object type
 */
public interface DataWriter<Persistable_> {

    /**
     * Serialize a persistable object to a string.s
     *
     * @param object the persistable object to serialize
     * @return an serialized representation of the persistable object
     * @throws IllegalArgumentException if the object cannot be serialized
     */
    String serialize(Persistable_ object) throws IllegalArgumentException;
}
