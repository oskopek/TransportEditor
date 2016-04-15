package com.oskopek.bp.editor.model;

public interface Node extends Locatable {

    @Override
    default Node getLocation() {
        return this;
    }
}
