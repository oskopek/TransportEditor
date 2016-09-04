/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.actionbuilder;

import com.oskopek.transporteditor.model.domain.action.DefaultAction;
import com.oskopek.transporteditor.model.problem.ActionObject;
import com.oskopek.transporteditor.model.problem.Locatable;
import com.oskopek.transporteditor.model.problem.Location;

public interface ActionBuilder<Action_ extends DefaultAction<Who__, What__>, Who__ extends Locatable, What__ extends
        ActionObject> {

    <Who_ extends Who__, What_ extends What__> Action_ build(Who_ who, Location where, What_ what);

}
