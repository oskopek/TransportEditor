/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.persistence.DataReader;
import com.oskopek.transporteditor.persistence.DataWriter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class OpenedTextObjectHandler<Persistable_> implements AutoCloseable {

    private final ObjectProperty<Path> path = new SimpleObjectProperty<>();
    private final ObjectProperty<Persistable_> object = new SimpleObjectProperty<>();
    private final ObjectProperty<DataReader<Persistable_>> reader = new SimpleObjectProperty<>();
    private final ObjectProperty<DataWriter<Persistable_>> writer = new SimpleObjectProperty<>();

    private boolean changedSinceLastSave = false;

    public OpenedTextObjectHandler() {
        this.object.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                changedSinceLastSave = true;
            }
        });
    }

    public void load(Path path, DataWriter<Persistable_> writer, DataReader<Persistable_> reader) {
        close();
        this.reader.setValue(reader);
        this.writer.setValue(writer);
        setPath(path);
        setObject(this.reader.get().parse(readToString()));
    }

    public void newObject(Persistable_ object, DataWriter<Persistable_> writer, DataReader<Persistable_> reader) {
        this.reader.setValue(reader);
        this.writer.setValue(writer);
        setPath(null);
        setObject(object);
    }

    public void save() {
        if (isSaveAsNeeded()) {
            throw new IllegalStateException("Cannot save with no path set.");
        }
        if (!hasObject()) {
            throw new IllegalStateException("Cannot save null object.");
        }
        writeFromString(writer.get().serialize(getObject()));
        changedSinceLastSave = false;
    }

    public void saveAs(Path path) {
        if (path == null) {
            throw new IllegalStateException("Cannot save as to null path.");
        }
        setPath(path);
        save();
    }

    @Override
    public void close() {
        setObject(null);
        setPath(null);
    }

    public Path getPath() {
        return path.get();
    }

    private void setPath(Path path) {
        this.path.set(path);
    }

    private ObjectProperty<Path> pathProperty() {
        return path;
    }

    public Persistable_ getObject() {
        return object.get();
    }

    private void setObject(Persistable_ object) {
        this.object.set(object);
    }

    private ObjectProperty<Persistable_> objectProperty() {
        return object;
    }

    protected boolean isSaveAsNeeded() {
        return getPath() == null;
    }

    protected boolean hasObject() {
        return getObject() == null;
    }

    protected boolean isChangedSinceLastSave() {
        return changedSinceLastSave;
    }

    private List<String> readToLinesList() {
        if (getPath() == null) {
            throw new IllegalStateException("Cannot read lines from null path.");
        }
        try {
            return Files.readAllLines(getPath(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read lines from \"" + getPath() + "\".", e);
        }
    }

    private String readToString() {
        if (getPath() == null) {
            throw new IllegalStateException("Cannot read lines from null path.");
        }
        return String.join("\n", readToLinesList());
    }

    private void writeFromList(List<String> lineList) {
        if (getPath() == null) {
            throw new IllegalStateException("Cannot write lines to null path.");
        }
        try {
            Files.write(getPath(), lineList, Charset.forName("UTF-8"), StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write lines to \"" + getPath() + "\".", e);
        }
    }

    private void writeFromString(String contents) {
        if (getPath() == null) {
            throw new IllegalStateException("Cannot write string to null path.");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(getPath(), Charset.forName("UTF-8"),
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(contents);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write string to \"" + getPath() + "\".", e);
        }
    }

}
