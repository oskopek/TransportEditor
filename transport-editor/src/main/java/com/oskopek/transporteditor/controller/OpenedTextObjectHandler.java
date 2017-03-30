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

/**
 * Represents an open text file de/serializable to a POJO using a {@link DataReader}
 * and a {@link DataWriter}. Provides state management for saving and loading data,
 * providing the user with feedback about discarding/saving changes, etc.
 *
 * @param <Persistable_> the type of the persistable object
 */
public class OpenedTextObjectHandler<Persistable_> implements AutoCloseable {

    private final ObjectProperty<Path> path = new SimpleObjectProperty<>();
    private final ObjectProperty<Persistable_> object = new SimpleObjectProperty<>();
    private final ObjectProperty<DataReader<? extends Persistable_>> reader = new SimpleObjectProperty<>();
    private final ObjectProperty<DataWriter<? super Persistable_>> writer = new SimpleObjectProperty<>();

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private BooleanProperty changedSinceLastSave = new SimpleBooleanProperty(false);

    /**
     * Default constructor.
     */
    public OpenedTextObjectHandler() {
        object.addListener((observable, oldValue, newValue) -> changedSinceLastSave.setValue(newValue != null));
    }

    /**
     * Clear the object that we are referencing. Similar to loading an empty new object.
     */
    protected void clearObject() {
        nonSafeNewObject(null, writer.get(), reader.get());
    }

    /**
     * Load the object using the corresponding writer/reader from the given path.
     *
     * @param path the path to load from
     * @param writer the writer to write with afterwards
     * @param reader the reader to read with
     * @throws IllegalArgumentException if an error during parsing occurs
     */
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

    /**
     * @param object the new object instance
     * @param writer the writer to write with afterwards
     * @param reader the reader to read with afterwards
     * @return true if we succeeded in loading the new object (may not return true if f.e.
     * user selected to not overwrite his existing changes by setting this new object)
     */
    public boolean newObject(Persistable_ object, DataWriter<? super Persistable_> writer,
            DataReader<? extends Persistable_> reader) {
        return nonSafeNewObject(object, writer, reader);
    }

    /**
     * Doesn't check for anything, just overwrites.
     *
     * @param object the new object instance
     * @param writer the writer to write with afterwards
     * @param reader the reader to read with afterwards
     * @return true if we succeeded in loading the new object (may not return true if f.e.
     * setting the object failed, for any reason)
     */
    private boolean nonSafeNewObject(Persistable_ object, DataWriter<? super Persistable_> writer,
            DataReader<? extends Persistable_> reader) {
        this.reader.setValue(reader);
        this.writer.setValue(writer);
        setPath(null);
        setObject(object);
        return true;
    }

    /**
     * Tries to serialize to an existing path.
     *
     * @throws IllegalStateException if a path isn't set, if we're saving a null object or
     * if the writer isn't set
     */
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

    /**
     * Tries to save to a given path. Saves the path for future use.
     * Uses {@link #save()} internally.
     *
     * @param path the path to set to
     * @throws IllegalStateException if the path is null
     */
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

    /**
     * Set a unified IO, a writer and reader as the writer and reader.
     *
     * @param io the io
     * @param <IO_> the IO type
     */
    public <IO_ extends DataWriter<? super Persistable_> & DataReader<? extends Persistable_>> void setIO(IO_ io) {
        this.reader.set(io);
        this.writer.set(io);
    }

    /**
     * Get the persistence path.
     *
     * @return the path
     */
    public Path getPath() {
        return path.get();
    }

    /**
     * Set the persistence path.
     *
     * @param path the path to set
     */
    private void setPath(Path path) {
        this.path.set(path);
    }

    /**
     * The path property.
     *
     * @return the path property
     */
    private ObjectProperty<Path> pathProperty() {
        return path;
    }

    /**
     * Get the object.
     *
     * @return the object
     */
    public Persistable_ getObject() {
        return object.get();
    }

    /**
     * Set the object.
     *
     * @param object the object
     */
    public void setObject(Persistable_ object) {
        this.object.set(object);
    }

    /**
     * The object property.
     *
     * @return the object property
     */
    public ObjectProperty<Persistable_> objectProperty() {
        return object;
    }

    /**
     * Determines if save as is needed, or save is sufficient.
     * Does not determine if a save is needed at all (i.e. if
     * an unsaved change is present).
     *
     * @return true iff a save as is needed
     */
    protected boolean isSaveAsNeeded() {
        return getPath() == null;
    }

    /**
     * True if an object is loaded.
     *
     * @return true iff object is not null
     */
    protected boolean hasObject() {
        return getObject() != null;
    }

    /**
     * Property of unsaved changes present.
     *
     * @return true iff unsaved changes are present
     */
    protected ReadOnlyBooleanProperty changedSinceLastSaveProperty() {
        return changedSinceLastSave;
    }

    /**
     * Are unsaved changes present?
     *
     * @return true iff unsaved changes are present
     */
    protected boolean isChangedSinceLastSave() {
        return changedSinceLastSave.get();
    }

    /**
     * Reads the file with UTF-8 encoding to a list of strings.
     *
     * @return a list of lines
     */
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

    /**
     * Reads the file contents to a string.
     * Uses {@link #readToLinesList()} internally.
     *
     * @return the string contents of the file
     */
    private String readToString() {
        if (getPath() == null) {
            logger.debug("Cannot read lines from null path.");
            return null;
        }
        return String.join("\n", readToLinesList());
    }

    /**
     * Write to a file from a list of lines in UTF-8.
     *
     * @param lineList the list of lines
     * @throws IllegalStateException if the write failed
     */
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

    /**
     * Write to the file from a single string.
     *
     * @param contents the string contents of the written file
     * @throws IllegalStateException if the write failed
     */
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
