package com.oskopek.transporteditor.persistence;

/**
 * Basic methods for serializing any persistable object to a string serialized form.
 *
 * @param <Persistable_> the persistable object type
 */
public interface DataWriter<Persistable_> {

    /**
     * Serialize a persistable object to a string.
     *
     * @param object the persistable object to serialize
     * @param <T> the type of the serialized object
     * @return an serialized representation of the persistable object
     * @throws IllegalArgumentException if the object cannot be serialized
     */
    <T extends Persistable_> String serialize(T object);
}
