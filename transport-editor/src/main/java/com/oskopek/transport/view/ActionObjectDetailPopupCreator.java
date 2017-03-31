package com.oskopek.transport.view;

import com.oskopek.transport.model.problem.builder.*;
import com.oskopek.transport.view.plan.ActionObjectDetailPopup;
import org.controlsfx.control.PropertySheet;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * An {@link ActionObjectBuilderConsumer} for showing {@link ActionObjectDetailPopup}s.
 */
@Singleton
public final class ActionObjectDetailPopupCreator extends ActionObjectBuilderConsumer<ActionObjectDetailPopup> {

    @Inject
    private ResourceBundle messages;

    /**
     * Empty constructor.
     */
    private ActionObjectDetailPopupCreator() {
        // intentionally empty
    }

    /**
     * Shows a read-only popup with info from the builder.
     *
     * @param builder the builder
     * @return a filled in {@link ActionObjectDetailPopup}
     * @see ActionObjectDetailPopup
     * @see LocalizableSortableBeanPropertyUtils
     */
    @Override
    protected ActionObjectDetailPopup createInternal(ActionObjectBuilder<?> builder) {
        Map<String, String> info = convertToInfoMap(LocalizableSortableBeanPropertyUtils.getProperties(builder,
                messages));
        return new ActionObjectDetailPopup(info);
    }

    /**
     * Converts an property item list to a simple String -> String ordered map,
     * which the {@link ActionObjectDetailPopup} can read.
     *
     * @param items the items to convert
     * @return an ordered hash map (name -> string value) from the supplied item list
     */
    private static LinkedHashMap<String, String> convertToInfoMap(List<PropertySheet.Item> items) {
        LinkedHashMap<String, String> info = new LinkedHashMap<>(items.size());
        items.forEach(item -> info.put(item.getName(), item.getValue() == null ? "null" : item.getValue().toString()));
        return info;
    }

}
