package com.oskopek.transport.model.problem.builder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for localizing bean properties in the view.
 *
 * @see com.oskopek.transport.view.LocalizableSortableBeanPropertyUtils
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldLocalization {

    /**
     * The key into the {@link java.util.ResourceBundle} used for localization.
     *
     * @return the key
     */
    String key();

    /**
     * The priority for ordering this field in the property list.
     *
     * @return the priority, lower is more prioritized
     */
    int priority() default 5;

    /**
     * A disambiguation field for finding the correct editor.
     *
     * @return the editor class hint
     * @see com.oskopek.transport.view.editor.ActionObjectPropertyEditorFactory
     */
    Class<?> editor() default Void.class;

    /**
     * True iff the field should be editable.
     *
     * @return is field editable
     */
    boolean editable() default true;

}
