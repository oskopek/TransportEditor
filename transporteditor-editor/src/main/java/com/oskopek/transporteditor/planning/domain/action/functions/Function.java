/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action.functions;

import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import com.oskopek.transporteditor.planning.problem.ActionObject;

public interface Function {

    /**
     * Implementations can choose how many parameters they take and how strict they are.
     *
     * @param actionObjects an array of actionObjects, possibly empty, non-null
     * @return non-null
     */
    ActionCost apply(ActionObject... actionObjects);

}
