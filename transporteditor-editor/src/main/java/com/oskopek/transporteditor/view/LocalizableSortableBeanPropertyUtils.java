package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.model.problem.builder.FieldLocalization;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javaslang.Tuple;
import javaslang.collection.Stream;
import javaslang.control.Try;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanProperty;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * Custom adaptation of {@link org.controlsfx.property.BeanPropertyUtils} localizing and ordering its items
 * with the annotation {@link FieldLocalization}.
 */
public final class LocalizableSortableBeanPropertyUtils {

    /**
     * Empty constructor.
     */
    private LocalizableSortableBeanPropertyUtils() {
        // intentionally empty
    }

    /**
     * Construct all {@link PropertySheet} items via {@link #getProperties(Object, ResourceBundle, Predicate)}.
     *
     * @param bean the bean to get the properties from
     * @param messages the resource bundle used for localization of field names
     * @return an observable list of {@link PropertySheet.Item}s
     * @see org.controlsfx.property.BeanPropertyUtils#getProperties(Object)
     */
    public static ObservableList<PropertySheet.Item> getProperties(final Object bean, ResourceBundle messages) {
        return getProperties(bean, messages, p -> true);
    }

    /**
     * Construct the {@link PropertySheet} items from properties for which the predicate holds.
     * Additionally, they have to be annotated with the {@link FieldLocalization} annotation, which provides
     * localization, ordering and more for the resulting item list.
     *
     * @param bean the bean to get the properties from
     * @param messages the resource bundle used for localization of field names
     * @param test the predicate to filter properties with
     * @return an observable list of {@link PropertySheet.Item}s
     * @see org.controlsfx.property.BeanPropertyUtils#getProperties(Object)
     */
    public static ObservableList<PropertySheet.Item> getProperties(final Object bean, ResourceBundle messages,
            Predicate<PropertyDescriptor> test) {
        BeanInfo beanInfo = Try.of(() -> Introspector.getBeanInfo(bean.getClass(), Object.class))
                .getOrElseThrow(e -> new IllegalArgumentException("Failed get properties from bean.", e));

        Stream<PropertySheet.Item> stream = Stream.of(beanInfo.getPropertyDescriptors()).filter(test)
                .map(pd -> Tuple.of(pd, pd.getReadMethod().getAnnotation(FieldLocalization.class)))
                .map(t -> Tuple.of(t._1, t._2.priority(), messages.getString(t._2.key()), t._2.editable(),
                        t._2.editor()))
                .sorted((t1, t2) -> new CompareToBuilder().append(t1._2, t2._2).append(t1._3, t2._3).toComparison())
                .map(tuple -> {
                    tuple._1.setName(tuple._3);
                    if (!Void.class.equals(tuple._5)) {
                        tuple._1.setPropertyEditorClass(tuple._5);
                    }
                    BeanProperty property = new BeanProperty(bean, tuple._1);
                    property.setEditable(tuple._4 && property.isEditable());
                    return property;
                });
        return FXCollections.observableList(stream.filter(p -> p.getValue() != null).toJavaList());
    }
}
