/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@matfyz.cz>. All rights reserved.
 */

package com.oskopek.bp.editor.model;

import com.oskopek.bp.editor.model.Locatable;
import com.oskopek.bp.editor.model.graph.Node;

public abstract class Location implements Node, Locatable {

    @Override
    public Node getCurrentNode() {
        return this;
    }
}
