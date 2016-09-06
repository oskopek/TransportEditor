/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.problem;


public class PlaceholderActionObject implements ActionObject {

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported on placeholder!");
    }

}
