/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.persistence.DataReader;
import com.oskopek.transporteditor.persistence.DataWriter;
import com.oskopek.transporteditor.view.TransportEditorApplication;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class JavaFxOpenedTextObjectHandler<Persistable_> extends OpenedTextObjectHandler<Persistable_> {

    private final TransportEditorApplication application;
    private final ResourceBundle messages;

    public JavaFxOpenedTextObjectHandler(TransportEditorApplication application, ResourceBundle messages) {
        this.application = application;
        this.messages = messages;
    }

    private boolean saveBeforeContinuing() {
        if (getObject() != null) {
            // TODO: Add a save/discard/exit before creating a new object
        }
    }

    public void load(String title, DataWriter<Persistable_> writer, DataReader<Persistable_> reader) {
        if (saveBeforeContinuing()) {
            return;
        }
        Path path = openFileWithDefaultFileChooser(title);
        super.load(path, writer, reader);
    }

    @Override
    public void newObject(Persistable_ object, DataWriter<Persistable_> writer, DataReader<Persistable_> reader) {
        if (saveBeforeContinuing()) {
            return;
        }
        super.newObject(object, writer, reader);
    }

    @Override
    public void save() {
        if (isSaveAsNeeded()) {
            saveAs();
        } else {
            super.save();
        }
    }

    private FileChooser buildDefaultFileChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("XML", "*.xml");
        FileChooser.ExtensionFilter allFileFilter = new FileChooser.ExtensionFilter("All Files", "*");
        chooser.getExtensionFilters().addAll(allFileFilter, xmlFilter);
        chooser.setSelectedExtensionFilter(xmlFilter);
        return chooser;
    }

    private Path openFileWithDefaultFileChooser(String title) {
        return Paths.get(buildDefaultFileChooser(title).showOpenDialog(application.getPrimaryStage()).toString());
    }

    private Path saveFileWithDefaultFileChooser(String title) {
        return Paths.get(buildDefaultFileChooser(title).showSaveDialog(application.getPrimaryStage()).toString());
    }

    public void saveAs() {
        Path path = saveFileWithDefaultFileChooser(messages.getString("%root.saveAs"));
        super.saveAs(path);
    }

    @Override
    public void close() {
        super.close();
    }
}
