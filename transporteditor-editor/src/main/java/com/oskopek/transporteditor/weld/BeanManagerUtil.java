package com.oskopek.transporteditor.weld;

import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.UnsatisfiedResolutionException;

import javax.enterprise.inject.spi.CDI;
import java.lang.reflect.Constructor;

/**
 * Utility class for constructing bean instances in classes that aren't necessary beans and that don't necessarily
 * have to have CDI enabled.
 */
public final class BeanManagerUtil {

    /**
     * Hidden constructor.
     */
    private BeanManagerUtil() {
        // intentionally empty
    }

    /**
     * Constructs a bean instance. Used in classes that aren't necessary beans. The caller doesn't necessarily
     * have to have CDI enabled.
     *
     * @param clazz the class to create an instance of
     * @param <T> the class type
     * @return an instance of the class type
     */
    public static <T> T createBeanInstance(Class<T> clazz) {
        try {
            return CDI.current().select(clazz).get();
        } catch (IllegalStateException ise) { // CDI disabled
            try {
                Constructor<T> constructor = clazz.getDeclaredConstructor(); // get the bean constructor
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to create non-CDI instance.", e);
            }
        } catch (UnsatisfiedResolutionException ure) { // CDI enabled, but unable to resolve bean
            throw new IllegalArgumentException("Failed to create CDI instance.", ure);
        }
    }

}
