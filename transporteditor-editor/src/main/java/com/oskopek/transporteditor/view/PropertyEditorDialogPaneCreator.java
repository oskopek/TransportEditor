package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.model.problem.builder.ActionObjectBuilder;
import com.oskopek.transporteditor.view.editor.ActionObjectPropertyEditorFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.PropertySheet;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Supplier;

@Singleton
public class PropertyEditorDialogPaneCreator extends ActionObjectBuilderConsumer<Supplier<Void>> {

    @Inject
    private ResourceBundle messages;

    @Override
    protected Supplier<Void> createInternal(ActionObjectBuilder<?> builder) {
        ObservableList<PropertySheet.Item> properties
                = LocalizableSortableBeanPropertyUtils.getProperties(builder, messages);
        BorderPane pane = new BorderPane();
        PropertySheet sheet = new PropertySheet(properties);
        sheet.setPropertyEditorFactory(new ActionObjectPropertyEditorFactory());
        pane.setCenter(sheet);

        return () -> {
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle(messages.getString("edit"));
            alert.getDialogPane().setContent(pane);
            alert.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && ButtonType.APPLY.equals(result.get())) {
                builder.update();
            }
            return null;
        };
    }
}
