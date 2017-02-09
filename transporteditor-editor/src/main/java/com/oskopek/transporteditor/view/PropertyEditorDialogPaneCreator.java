package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.model.problem.builder.ActionObjectBuilder;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.PropertySheet;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ResourceBundle;
import java.util.function.Supplier;

@Singleton
public class PropertyEditorDialogPaneCreator extends ActionObjectBuilderConsumer<Supplier<Alert>> {

    @Inject
    private ResourceBundle messages;

    @Override
    protected Supplier<Alert> createInternal(ActionObjectBuilder<?> builder) {
        ObservableList<PropertySheet.Item> properties
                = LocalizableSortableBeanPropertyUtils.getProperties(builder, messages);
        BorderPane pane = new BorderPane();
        pane.setCenter(new PropertySheet(properties));

        return () -> {
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle(messages.getString("edit"));
            alert.getDialogPane().setContent(pane);
            alert.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
            return alert;
        }; // TODO: Update immutable stuff back
    }
}
