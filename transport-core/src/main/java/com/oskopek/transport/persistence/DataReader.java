package com.oskopek.transport.persistence;

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
    Persistable_ parse(String contents);

    /**
     * Parse a persistable object from a string with proper type casting.
     *
     * @param contents the string representation of the persistable
     * @param clazz the class to cast to
     * @return an initialized instance of the persistable object
     * @throws IllegalArgumentException if the contents cannot be parsed or if a class cast exception occurred
     */
    default Persistable_ parse(String contents, Class<? extends Persistable_> clazz) {
        try {
            return clazz.cast(parse(contents));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "Could not parse contents. " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }
}
