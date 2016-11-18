package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.persistence.DataReader;
import com.oskopek.transporteditor.persistence.DataWriter;
import com.oskopek.transporteditor.view.SaveDiscardDialogPaneCreator;
import com.oskopek.transporteditor.view.TransportEditorApplication;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

public class JavaFxOpenedTextObjectHandler<Persistable_> extends OpenedTextObjectHandler<Persistable_> {

    private final TransportEditorApplication application;
    private final ResourceBundle messages;
    private final SaveDiscardDialogPaneCreator creator;

    public JavaFxOpenedTextObjectHandler(TransportEditorApplication application, ResourceBundle messages,
            SaveDiscardDialogPaneCreator creator) {
        this.application = application;
        this.messages = messages;
        this.creator = creator;
    }

    public static FileChooser buildDefaultFileChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("XML", "*.xml");
        FileChooser.ExtensionFilter allFileFilter = new FileChooser.ExtensionFilter("All Files", "*");
        chooser.getExtensionFilters().addAll(allFileFilter, xmlFilter);
        chooser.setSelectedExtensionFilter(xmlFilter);
        return chooser;
    }

    public JavaFxOpenedTextObjectHandler<Persistable_> bind(Menu menu, MenuItem newMenuItem, MenuItem loadMenuItem,
            MenuItem saveMenuItem, MenuItem saveAsMenuItem,
            OpenedTextObjectHandler<?> parentHandler) {
        if (parentHandler == null) {
            menu.setDisable(false);
            newMenuItem.setDisable(false);
            loadMenuItem.setDisable(false);
        } else {
            menu.disableProperty().bind(parentHandler.objectProperty().isNull());
            newMenuItem.disableProperty().bind(parentHandler.objectProperty().isNull());
            loadMenuItem.disableProperty().bind(parentHandler.objectProperty().isNull());
        }

        saveMenuItem.disableProperty().bind(changedSinceLastSaveProperty().not());
        saveAsMenuItem.disableProperty().bind(objectProperty().isNull());
        return this;
    }

    public void checkForSaveBeforeOverwrite(Runnable overwritingAction) {
        ButtonType result = null;
        if (isChangedSinceLastSave()) {
            Optional<ButtonType> button = creator.show(messages.getString("shouldSave"));
            if (button.isPresent()) {
                result = button.get();
            }
        } else {
            overwritingAction.run();
        }

        if (ButtonType.YES.equals(result)) {
            save();
            if (!isChangedSinceLastSave()) {
                overwritingAction.run();
            } else {
                throw new IllegalStateException("State changed before new object could be loaded.");
            }
        } else if (ButtonType.NO.equals(result)) {
            overwritingAction.run();
        }
    }

    public void load(String title, DataWriter<Persistable_> writer, DataReader<Persistable_> reader) {
        checkForSaveBeforeOverwrite(() -> {
            Path path = openFileWithDefaultFileChooser(title);
            if (path == null) {
                return;
            }
            super.load(path, writer, reader);
        });

    }

    @Override
    public void newObject(Persistable_ object, DataWriter<Persistable_> writer, DataReader<Persistable_> reader) {
        checkForSaveBeforeOverwrite(() -> super.newObject(object, writer, reader));
    }

    @Override
    public void save() {
        if (isSaveAsNeeded()) {
            saveAs();
        } else {
            super.save();
        }
    }

    @Override
    public void close() {
        checkForSaveBeforeOverwrite(() -> {
            super.close();
        });
    }

    private Path openFileWithDefaultFileChooser(String title) {
        File file = buildDefaultFileChooser(title).showOpenDialog(application.getPrimaryStage());
        if (file == null) {
            return null;
        } else {
            return Paths.get(file.toString());
        }
    }

    private Path saveFileWithDefaultFileChooser(String title) {
        File file = buildDefaultFileChooser(title).showSaveDialog(application.getPrimaryStage());
        if (file == null) {
            return null;
        } else {
            return Paths.get(file.toString());
        }
    }

    public void saveAs() {
        Path path = saveFileWithDefaultFileChooser(messages.getString("root.saveAs"));
        if (path == null) {
            return;
        }
        super.saveAs(path);
    }
}
