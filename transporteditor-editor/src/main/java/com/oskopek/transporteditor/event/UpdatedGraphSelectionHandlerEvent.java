package com.oskopek.transporteditor.event;

import com.oskopek.transporteditor.controller.RoadGraphSelectionHandler;

public class UpdatedGraphSelectionHandlerEvent extends SingleValueEvent<RoadGraphSelectionHandler> {

    public UpdatedGraphSelectionHandlerEvent(RoadGraphSelectionHandler graphSelectionHandler) {
        super(graphSelectionHandler);
    }

}
