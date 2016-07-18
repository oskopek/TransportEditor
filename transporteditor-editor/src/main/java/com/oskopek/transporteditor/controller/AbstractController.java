package com.oskopek.transporteditor.controller;

import com.google.common.eventbus.EventBus;
import com.oskopek.transporteditor.view.TransportEditorApplication;

import javax.inject.Inject;
import java.util.ResourceBundle;

/**
 * Abstraction over controllers.
 */
public abstract class AbstractController {

    @Inject
    protected TransportEditorApplication studyGuideApplication;

    @Inject
    protected transient ResourceBundle messages;

    @Inject
    protected EventBus eventBus;

}
