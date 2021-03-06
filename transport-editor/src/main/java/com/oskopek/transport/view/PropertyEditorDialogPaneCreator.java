package com.oskopek.transport.view;

import com.oskopek.transport.model.problem.builder.ActionObjectBuilder;
import com.oskopek.transport.model.problem.builder.InvalidValueException;
import com.oskopek.transport.view.editor.ActionObjectPropertyEditorFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.PropertySheet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Supplier;

/**
 * Create and display a dialog for editing properties of
 * an {@link com.oskopek.transport.model.problem.ActionObject}.
 */
@Singleton
public class PropertyEditorDialogPaneCreator extends ActionObjectBuilderConsumer<Supplier<Void>> {

    @Inject
    private ResourceBundle messages;

    @Inject
    @Named("mainApp")
    private TransportEditorApplication application;

    @Override
    protected Supplier<Void> createInternal(ActionObjectBuilder<?> builder) {
        ObservableList<PropertySheet.Item> properties
                = LocalizableSortableBeanPropertyUtils.getProperties(builder, messages);
        BorderPane pane = new BorderPane();
        PropertySheet sheet = new PropertySheet(properties);
        sheet.setPropertyEditorFactory(new ActionObjectPropertyEditorFactory(application.getPlanningSession()
                .getProblem().getRoadGraph()));
        pane.setCenter(sheet);

        return () -> {
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle(messages.getString("edit"));
            alert.getDialogPane().setContent(pane);
            alert.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
            application.centerInPrimaryStage(alert, -100, -150);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && ButtonType.APPLY.equals(result.get())) {
                try {
                    builder.update();
                } catch (InvalidValueException e) {
                    AlertCreator.showAlert(Alert.AlertType.ERROR,
                            messages.getString("editor.invalidvalue") + " (" + e.getMessage() + ").",
                            a -> application.centerInPrimaryStage(a, -200, -50));
                }
            }
            return null;
        };
    }
}
