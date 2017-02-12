package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.model.problem.builder.*;
import com.oskopek.transporteditor.view.plan.ActionObjectDetailPopup;
import org.controlsfx.control.PropertySheet;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.LinkedHashMap;

@Singleton
public final class ActionObjectDetailPopupCreator extends ActionObjectBuilderConsumer<ActionObjectDetailPopup> {

    @Inject
    private ResourceBundle messages;

    private ActionObjectDetailPopupCreator() {
        // intentionally empty
    }

    @Override
    protected ActionObjectDetailPopup createInternal(ActionObjectBuilder<?> builder) {
        Map<String, String> info = convertToInfoMap(LocalizableSortableBeanPropertyUtils.getProperties(builder,
                messages));
        return new ActionObjectDetailPopup(info);
    }

    private LinkedHashMap<String, String> convertToInfoMap(List<PropertySheet.Item> items) {
        LinkedHashMap<String, String> info = new LinkedHashMap<>(items.size());
        items.forEach(item -> info.put(item.getName(), item.getValue() == null ? "null" : item.getValue().toString()));
        return info;
    }

}
