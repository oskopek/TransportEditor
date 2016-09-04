/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;

import javafx.beans.property.StringProperty;

public class PlaceholderActionObject implements ActionObject {

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported on placeholder!");
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Not supported on placeholder!");
    }

    @Override
    public StringProperty nameProperty() {
        throw new UnsupportedOperationException("Not supported on placeholder!");
    }
}
