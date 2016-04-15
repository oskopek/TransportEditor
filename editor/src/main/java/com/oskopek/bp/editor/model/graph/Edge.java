/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@matfyz.cz>. All rights reserved.
 */

package com.oskopek.bp.editor.model.graph;

public interface Edge {

    Node getFrom();
    Node getTo();
    Cost getCost();

}
