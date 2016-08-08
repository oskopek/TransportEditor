/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain;

import edu.uci.ics.jung.graph.Graph;

/**
 * Wrapper interface around a JUNG graph type.
 */
public interface RoadGraph extends Graph<Location, Road> {
    // intentionally empty
}
