/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.persistence.DataReader;
import com.oskopek.transporteditor.persistence.DataWriter;

import java.nio.file.Path;

public class JavaFxOpenedTextObjectHandler<Persistable_> extends OpenedTextObjectHandler<Persistable_> {

    public JavaFxOpenedTextObjectHandler() {
        // intentionally empty
    }

    @Override
    public void load(Path path, DataWriter<Persistable_> writer, DataReader<Persistable_> reader) {
        super.load(path, writer, reader);
    }

    @Override
    public void newObject(Persistable_ object, DataWriter<Persistable_> writer, DataReader<Persistable_> reader) {

        super.newObject(object, writer, reader);
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    public void saveAs(Path path) {
        super.saveAs(path);
    }

    @Override
    public void close() {
        super.close();
    }
}
