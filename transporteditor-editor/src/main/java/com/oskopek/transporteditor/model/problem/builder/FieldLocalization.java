package com.oskopek.transporteditor.model.problem.builder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldLocalization {

    String key();
    int priority() default 5;
    Class<?> editor() default Void.class;

}
