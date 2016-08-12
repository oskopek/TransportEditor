/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.problem;

import com.oskopek.transporteditor.planning.domain.action.ActionCost;
import javafx.beans.property.ObjectProperty;

public interface Road extends ActionObject {

    ActionCost getLength();

    void setLength(ActionCost length);

    ObjectProperty<ActionCost> lengthProperty();

}
