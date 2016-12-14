package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.persistence.DataReader;
import com.oskopek.transporteditor.persistence.DataWriter;
import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class OpenedTextObjectHandler<Persistable_> implements AutoCloseable {

    private final ObjectProperty<Path> path = new SimpleObjectProperty<>();
    private final ObjectProperty<Persistable_> object = new SimpleObjectProperty<>();
    private final ObjectProperty<DataReader<? extends Persistable_>> reader = new SimpleObjectProperty<>();
    private final ObjectProperty<DataWriter<? super Persistable_>> writer = new SimpleObjectProperty<>();

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private BooleanProperty changedSinceLastSave = new SimpleBooleanProperty(false);

    public OpenedTextObjectHandler() {
        object.addListener((observable, oldValue, newValue) -> changedSinceLastSave.setValue(newValue != null));
    }

    protected void clearObject() {
        nonSafeNewObject(null, writer.get(), reader.get());
    }

    public void load(Path path, DataWriter<? super Persistable_> writer, DataReader<? extends Persistable_> reader)
            throws IllegalArgumentException {
        close();
        this.reader.setValue(reader);
        this.writer.setValue(writer);
        setPath(path);
        if (reader == null) {
            throw new IllegalStateException("Cannot load object with null reader.");
        }
        Persistable_ parsed;
        try {
            parsed = this.reader.get().parse(readToString());
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to parse input file \"{}\": \"{}\".", path, e.getMessage(), e);
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        setObject(parsed);
    }

    public void newObject(Persistable_ object, DataWriter<? super Persistable_> writer,
            DataReader<? extends Persistable_> reader) {
        nonSafeNewObject(object, writer, reader);
    }

    private void nonSafeNewObject(Persistable_ object, DataWriter<? super Persistable_> writer,
            DataReader<? extends Persistable_> reader) {
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
        if (writer.get() == null) {
            throw new IllegalStateException("Cannot save object with null writer.");
        }
        writeFromString(writer.get().serialize(getObject()));
        changedSinceLastSave.setValue(false);
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

    public <IO_ extends DataWriter<? super Persistable_> & DataReader<? extends Persistable_>> void setIO(IO_ io) {
        this.reader.set(io);
        this.writer.set(io);
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

    public void setObject(Persistable_ object) {
        this.object.set(object);
    }

    public ObjectProperty<Persistable_> objectProperty() {
        return object;
    }

    protected boolean isSaveAsNeeded() {
        return getPath() == null;
    }

    protected boolean hasObject() {
        return getObject() != null;
    }

    protected ReadOnlyBooleanProperty changedSinceLastSaveProperty() {
        return changedSinceLastSave;
    }

    protected boolean isChangedSinceLastSave() {
        return changedSinceLastSave.get();
    }

    private List<String> readToLinesList() {
        if (getPath() == null) {
            throw new IllegalStateException("Cannot read lines from null path.");
        }
        try {
            return Files.readAllLines(getPath(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            logger.debug("Could not read lines from \"" + getPath() + "\".", e);
        }
        return null;
    }

    private String readToString() {
        if (getPath() == null) {
            logger.debug("Cannot read lines from null path.");
            return null;
        }
        return String.join("\n", readToLinesList());
    }

    private void writeFromList(List<String> lineList) {
        if (getPath() == null) {
            logger.debug("Cannot write lines to null path.");
            return;
        }
        try {
            Files.write(getPath(), lineList, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new IllegalStateException("Could not write lines to \"" + getPath() + "\".", e);
        }
    }

    private void writeFromString(String contents) {
        if (getPath() == null) {
            logger.debug("Cannot write string to null path.");
            return;
        }
        try (BufferedWriter writer = Files.newBufferedWriter(getPath(), Charset.forName("UTF-8"))) {
            writer.write(contents);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write string to \"" + getPath() + "\".", e);
        }
    }

}
