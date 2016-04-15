/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@matfyz.cz>. All rights reserved.
 */

package com.oskopek.bp.editor.model;

public interface Location extends Locatable {

    @Override
    default Location getLocation() {
        return this;
    }
}
