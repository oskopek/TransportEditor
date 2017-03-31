package com.oskopek.transport.persistence;

/**
 * A union interface for classes that are both {@link DataWriter}s and {@link DataReader}s of the same type.
 *
 * @param <Persistable_> the type that is read and written
 */
public interface DataIO<Persistable_> extends DataWriter<Persistable_>, DataReader<Persistable_> {

}
