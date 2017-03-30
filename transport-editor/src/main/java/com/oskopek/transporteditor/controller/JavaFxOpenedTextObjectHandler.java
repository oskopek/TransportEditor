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

/**
 * JavaFX-based extension of {@link OpenedTextObjectHandler}.
 * Uses UI elements like {@link FileChooser} and {@link AlertCreator} to
 * notify user with various messages and input prompts.
 *
 * @param <Persistable_> the type of the persistable object
 */
public class JavaFxOpenedTextObjectHandler<Persistable_> extends OpenedTextObjectHandler<Persistable_> {

    /**
     * Extension filter for {@code *.pddl} files.
     */
    protected static final FileChooser.ExtensionFilter pddlFilter = new FileChooser.ExtensionFilter("PDDL", "*.pddl",
            "*.PDDL");

    /**
     * Extension filter for {@code *.xml} files.
     */
    protected static final FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("XML", "*.xml",
            "*.XML");

    /**
     * Extension filter for {@code *.val} files.
     */
    protected static final FileChooser.ExtensionFilter valFilter = new FileChooser.ExtensionFilter("VAL", "*.val",
            "*.VAL");

    /**
     * Extension filter for {@code *} (all) files.
     */
    protected static final FileChooser.ExtensionFilter allFileFilter = new FileChooser.ExtensionFilter("All Files",
            "*");

    private final TransportEditorApplication application;
    private final ResourceBundle messages;
    private final SaveDiscardDialogPaneCreator creator;
    private FileChooser.ExtensionFilter[] chosenFilters = new FileChooser.ExtensionFilter[]{allFileFilter};

    /**
     * Default constructor.
     *
     * @param application the main application instance
     * @param messages the localization bundle
     * @param creator a save-discard dialog creator
     */
    public JavaFxOpenedTextObjectHandler(TransportEditorApplication application, ResourceBundle messages,
            SaveDiscardDialogPaneCreator creator) {
        this.application = application;
        this.messages = messages;
        this.creator = creator;
    }

    /**
     * Create a file chooser with the selected title and all file extension filter.
     *
     * @param title the title
     * @return the built file chooser
     */
    public static FileChooser buildFileChooser(String title) {
        return buildFileChooser(title, allFileFilter);
    }

    /**
     * Create a file chooser with the selected title and extension filters.
     *
     * @param title the title
     * @param filters the filters
     * @return the built file chooser
     */
    public static FileChooser buildFileChooser(String title, FileChooser.ExtensionFilter... filters) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);

        if (filters == null || filters.length == 0) {
            filters = new FileChooser.ExtensionFilter[]{allFileFilter};
        }

        chooser.getExtensionFilters().addAll(filters);
        chooser.setSelectedExtensionFilter(filters[0]);
        return chooser;
    }

    /**
     * Create a file chooser with the selected title and chosen extension filters using
     * {@link #prependFilters(FileChooser.ExtensionFilter...)} or the {@code use...()} methods.
     *
     * @param title the title
     * @return the built file chooser
     */
    private FileChooser buildCustomFileChooser(String title) {
        return buildFileChooser(title, chosenFilters);
    }

    /**
     * A builder method used for binding menu item disable properties to the appropriate internal
     * properties.
     *
     * @param menu the menu
     * @param newMenuItem the new object item
     * @param loadMenuItem the load object item
     * @param saveMenuItem the save object item
     * @param saveAsMenuItem the save object as item
     * @param parentHandler the parent handler, if any (else, null)
     * @return the handler instance with the UI elements properly bound
     */
    public JavaFxOpenedTextObjectHandler<Persistable_> bind(Menu menu, MenuItem newMenuItem, MenuItem loadMenuItem,
            MenuItem saveMenuItem, MenuItem saveAsMenuItem, OpenedTextObjectHandler<?> parentHandler) {
        if (parentHandler == null) {
            menu.setDisable(false);
            newMenuItem.setDisable(false);
            loadMenuItem.setDisable(false);
        } else {
            menu.disableProperty().bind(parentHandler.objectProperty().isNull());
            newMenuItem.disableProperty().bind(parentHandler.objectProperty().isNull());
            loadMenuItem.disableProperty().bind(parentHandler.objectProperty().isNull());
            parentHandler.objectProperty().addListener(observable -> clearObject());
        }

        saveMenuItem.disableProperty().bind(changedSinceLastSaveProperty().not());
        saveAsMenuItem.disableProperty().bind(objectProperty().isNull());
        return this;
    }

    /**
     * Prepends the given filters to the chosen filter array.
     *
     * @param filters the filters to prepend
     */
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

    /**
     * A builder method to add the {@code .pddl} file filter.
     *
     * @return this
     */
    public JavaFxOpenedTextObjectHandler<Persistable_> usePddl() {
        prependFilters(pddlFilter);
        return this;
    }

    /**
     * A builder method to add the {@code .xml} file filter.
     *
     * @return this
     */
    public JavaFxOpenedTextObjectHandler<Persistable_> useXml() {
        prependFilters(xmlFilter);
        return this;
    }

    /**
     * A builder method to add the {@code .val} file filter.
     *
     * @return this
     */
    public JavaFxOpenedTextObjectHandler<Persistable_> useVal() {
        prependFilters(valFilter);
        return this;
    }

    /**
     * Checks if the object was changed and raises a save-discard dialog in that
     * case to confirm the overwrite.
     *
     * @param overwritingAction the action to run if there were no changes or if
     * the user confirmed he wants to run it
     * @return true iff the overwriting action was run
     */
    public boolean checkForSaveBeforeOverwrite(Runnable overwritingAction) {
        if (!isChangedSinceLastSave()) {
            overwritingAction.run();
            return true;
        }
        Optional<ButtonType> button = creator.show(messages.getString("shouldSave"));
        ButtonBar.ButtonData result;
        if (button.isPresent()) {
            result = button.get().getButtonData();
        } else {
            return false;
        }

        if (ButtonBar.ButtonData.CANCEL_CLOSE.equals(result)) {
            return false;
        } else if (ButtonBar.ButtonData.YES.equals(result)) {
            save();
            if (!isChangedSinceLastSave()) {
                overwritingAction.run();
            } else {
                throw new IllegalStateException("State changed before new object could be loaded.");
            }
        } else if (ButtonBar.ButtonData.APPLY.equals(result)) {
            saveAsWithDefaultFileChooser();
            if (!isChangedSinceLastSave()) {
                overwritingAction.run();
            } else {
                throw new IllegalStateException("State changed before new object could be loaded.");
            }
        } else if (ButtonBar.ButtonData.NO.equals(result)) {
            overwritingAction.run();
        }
        return true;
    }

    /**
     * Open a file chooser to load the object from a file.
     *
     * @param title the title of the file chooser
     * @param writer the writer to use
     * @param reader the reader to use
     * @return true iff the file was loaded
     */
    public boolean loadWithDefaultFileChooser(String title, DataWriter<Persistable_> writer,
            DataReader<Persistable_> reader) {
        return checkForSaveBeforeOverwrite(() -> {
            Path path = openFileWithDefaultFileChooser(title);
            if (path == null) {
                return;
            }
            try {
                super.load(path, writer, reader);
            } catch (IllegalArgumentException e) {
                // swallow exception
                AlertCreator.showAlert(Alert.AlertType.ERROR, messages.getString("load.failed") + ". " + e.getMessage(),
                        a -> application.centerInPrimaryStage(a, -200, -100), ButtonType.OK);
            }
        });

    }

    @Override
    public boolean newObject(Persistable_ object, DataWriter<? super Persistable_> writer,
            DataReader<? extends Persistable_> reader) {
        return checkForSaveBeforeOverwrite(() -> super.newObject(object, writer, reader));
    }

    @Override
    public void save() {
        if (isSaveAsNeeded()) {
            saveAsWithDefaultFileChooser();
        } else {
            super.save();
        }
    }

    @Override
    public void close() {
        // intentionally empty
    }

    /**
     * Private util method to show the open dialog of a file chooser and get the path.
     *
     * @param title the title of the file chooser
     * @return the path to open or null
     */
    private Path openFileWithDefaultFileChooser(String title) {
        File file = buildCustomFileChooser(title).showOpenDialog(application.getPrimaryStage());
        if (file == null) {
            return null;
        } else {
            return Paths.get(file.toString());
        }
    }

    /**
     * Private util method to show the save dialog of a file chooser and get the path.
     *
     * @param title the title of the file chooser
     * @return the path to save to or null
     */
    private Path saveFileWithDefaultFileChooser(String title) {
        File file = buildCustomFileChooser(title).showSaveDialog(application.getPrimaryStage());
        if (file == null) {
            return null;
        } else {
            return Paths.get(file.toString());
        }
    }

    /**
     * Show the save as file chooser dialog and get the path chosen.
     * If the path was chosen, runs {@link #saveAs(Path)}.
     */
    public void saveAsWithDefaultFileChooser() {
        Path path = saveFileWithDefaultFileChooser(messages.getString("root.saveAs"));
        if (path == null) {
            return;
        }
        super.saveAs(path);
    }
}
