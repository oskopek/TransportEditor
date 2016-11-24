package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.persistence.DataReader;
import com.oskopek.transporteditor.persistence.DataWriter;
import com.oskopek.transporteditor.view.AlertCreator;
import com.oskopek.transporteditor.view.SaveDiscardDialogPaneCreator;
import com.oskopek.transporteditor.view.TransportEditorApplication;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

public class JavaFxOpenedTextObjectHandler<Persistable_> extends OpenedTextObjectHandler<Persistable_> {

    public static final FileChooser.ExtensionFilter pddlFilter = new FileChooser.ExtensionFilter("PDDL", "*.pddl",
            "*.PDDL");
    public static final FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("XML", "*.xml",
            "*.XML");
    public static final FileChooser.ExtensionFilter valFilter = new FileChooser.ExtensionFilter("VAL", "*.val",
            "*.VAL");
    public static final FileChooser.ExtensionFilter allFileFilter = new FileChooser.ExtensionFilter("All Files", "*");
    private final TransportEditorApplication application;
    private final ResourceBundle messages;
    private final SaveDiscardDialogPaneCreator creator;
    private FileChooser.ExtensionFilter[] chosenFilters = new FileChooser.ExtensionFilter[] {allFileFilter};

    public JavaFxOpenedTextObjectHandler(TransportEditorApplication application, ResourceBundle messages,
            SaveDiscardDialogPaneCreator creator) {
        this.application = application;
        this.messages = messages;
        this.creator = creator;
    }

    public static FileChooser buildFileChooser(String title) {
        return buildFileChooser(title, allFileFilter);
    }

    public static FileChooser buildFileChooser(String title, FileChooser.ExtensionFilter... filters) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);

        if (filters == null || filters.length == 0) {
            filters = new FileChooser.ExtensionFilter[] {allFileFilter};
        }

        chooser.getExtensionFilters().addAll(filters);
        chooser.setSelectedExtensionFilter(filters[0]);
        return chooser;
    }

    private FileChooser buildCustomFileChooser(String title) {
        return buildFileChooser(title, chosenFilters);
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

    private void prependFilters(FileChooser.ExtensionFilter... filters) {
        if (filters == null) {
            return;
        }
        FileChooser.ExtensionFilter[] newFilters = new FileChooser.ExtensionFilter[chosenFilters.length
                + filters.length];
        System.arraycopy(chosenFilters, 0, newFilters, filters.length, chosenFilters.length);
        System.arraycopy(filters, 0, newFilters, 0, filters.length);
        chosenFilters = newFilters;
    }

    public JavaFxOpenedTextObjectHandler<Persistable_> usePddl() {
        prependFilters(pddlFilter);
        return this;
    }

    public JavaFxOpenedTextObjectHandler<Persistable_> useXml() {
        prependFilters(xmlFilter);
        return this;
    }

    public JavaFxOpenedTextObjectHandler<Persistable_> useVal() {
        prependFilters(valFilter);
        return this;
    }

    public void checkForSaveBeforeOverwrite(Runnable overwritingAction) {
        if (!isChangedSinceLastSave()) {
            overwritingAction.run();
            return;
        }
        Optional<ButtonType> button = creator.show(messages.getString("shouldSave"));
        ButtonBar.ButtonData result;
        if (button.isPresent()) {
            result = button.get().getButtonData();
        } else {
            return;
        }

        if (ButtonBar.ButtonData.YES.equals(result)) {
            save();
            if (!isChangedSinceLastSave()) {
                overwritingAction.run();
            } else {
                throw new IllegalStateException("State changed before new object could be loaded.");
            }
        } else if (ButtonBar.ButtonData.APPLY.equals(result)) {
            saveAs();
            if (!isChangedSinceLastSave()) {
                overwritingAction.run();
            } else {
                throw new IllegalStateException("State changed before new object could be loaded.");
            }
        } else if (ButtonBar.ButtonData.NO.equals(result)) {
            overwritingAction.run();
        }
    }

    public void loadWithDefaultFileChooser(String title, DataWriter<Persistable_> writer,
            DataReader<Persistable_> reader) {
        checkForSaveBeforeOverwrite(() -> {
            Path path = openFileWithDefaultFileChooser(title);
            if (path == null) {
                return;
            }
            try {
                super.load(path, writer, reader);
            } catch (IllegalArgumentException e) {
                // swallow exception
                AlertCreator.showAlert(Alert.AlertType.ERROR, messages.getString("load.failed") + ". " + e.getMessage(),
                        ButtonType.OK);
            }
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
        // intentionally empty
    }

    private Path openFileWithDefaultFileChooser(String title) {
        File file = buildCustomFileChooser(title).showOpenDialog(application.getPrimaryStage());
        if (file == null) {
            return null;
        } else {
            return Paths.get(file.toString());
        }
    }

    private Path saveFileWithDefaultFileChooser(String title) {
        File file = buildCustomFileChooser(title).showSaveDialog(application.getPrimaryStage());
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
