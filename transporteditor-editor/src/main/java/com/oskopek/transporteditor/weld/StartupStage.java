package com.oskopek.transporteditor.weld;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A runtime annotation that is used in the main {@link javafx.application.Application}
 * (specifically {@link com.oskopek.transporteditor.view.TransportEditorApplication}) for calling any initialization
 * methods
 * after creating the primary stage and enabling CDI.
 */
@Qualifier
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface StartupStage {

}
