/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.planner.ExternalPlanner;

public class ExternalPlannerIO extends XStreamGenericIO<ExternalPlanner>
        implements DataReader<ExternalPlanner>, DataWriter<ExternalPlanner> {
    // intentionally empty
}
