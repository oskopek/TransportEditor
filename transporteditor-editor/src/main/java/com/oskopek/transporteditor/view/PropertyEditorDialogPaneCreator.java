package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.builder.LocationBuilder;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.PropertySheet;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ResourceBundle;

@Singleton
public class PropertyEditorDialogPaneCreator {

    @Inject
    private ResourceBundle messages;

    @Inject
    private transient Logger logger;

    public Stage edit(Location location) {
        LocationBuilder builder = new LocationBuilder();
        builder.from(location);
        ObservableList<PropertySheet.Item> properties
                = LocalizableSortableBeanPropertyUtils.getProperties(builder, messages);
        return createFromSheet(properties);
    }

    private Stage createFromSheet(ObservableList<PropertySheet.Item> items) {
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(new PropertySheet(items));
        dialogPane.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setScene(new Scene(dialogPane));
        return stage;
    }
}
