package com.oskopek.transporteditor.model.problem;

import com.oskopek.transporteditor.view.InfoExportable;

public interface ActionObject {

    @InfoExportable(localizationKey = "vdcreator.name")
    String getName();

}
