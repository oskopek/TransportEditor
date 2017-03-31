package com.oskopek.transport.view.editor;

import com.oskopek.transport.model.domain.action.ActionCost;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;

/**
 * Simple property editor using an integer spinner for {@link ActionCost}.
 */
public class ActionCostEditor extends AbstractPropertyEditor<ActionCost, Spinner<Integer>> {

    private ObjectProperty<ActionCost> actionCostObservableValue;

    /**
     * Constructs the spinner and sets the default value.
     *
     * @param item the property sheet item
     */
    public ActionCostEditor(PropertySheet.Item item) {
        super(item, new Spinner<>());
        if (actionCostObservableValue == null) {
            actionCostObservableValue = new SimpleObjectProperty<>();
        }
        getEditor().valueProperty().addListener(
                (observable, oldValue, newValue) -> actionCostObservableValue.setValue(ActionCost.valueOf(newValue)));
        setValue((ActionCost) item.getValue());
    }

    @Override
    protected ObservableValue<ActionCost> getObservableValue() {
        if (actionCostObservableValue == null) {
            actionCostObservableValue = new SimpleObjectProperty<>();
        }
        return actionCostObservableValue;
    }

    @Override
    public void setValue(ActionCost value) {
        getEditor().setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE,
                value.getCost()));
    }
}
