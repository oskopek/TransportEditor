package com.oskopek.transport.controller;

import com.google.common.eventbus.EventBus;
import com.oskopek.transport.view.TransportEditorApplication;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ResourceBundle;

/**
 * Abstraction over controllers.
 */
public abstract class AbstractController {

    @Inject
    @Named("mainApp")
    protected TransportEditorApplication application;

    @Inject
    protected transient ResourceBundle messages;

    @Inject
    protected EventBus eventBus;

}
