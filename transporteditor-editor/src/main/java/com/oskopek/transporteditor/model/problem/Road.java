package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.model.domain.action.ActionCost;

public interface Road extends ActionObject {

    ActionCost getLength();

}
