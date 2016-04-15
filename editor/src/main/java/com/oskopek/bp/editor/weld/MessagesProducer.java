/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@matfyz.cz>. All rights reserved.
 */

package com.oskopek.bp.editor.weld;

import javax.enterprise.inject.Produces;
import java.util.ResourceBundle;

/**
 * A simple {@link java.util.ResourceBundle} producer for the {@code messages} properties resource bundle.
 */
public class MessagesProducer {

    /**
     * Create a {@link java.util.ResourceBundle} {@code messages}, used for localization in the UI.
     * @return an initialized resource bundle used for localization
     */
    @Produces
    public ResourceBundle createMessagesResourceBundle() {
        return ResourceBundle.getBundle("com.oskopek.bp.editor.view.messages");
    }

}
