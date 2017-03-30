package com.oskopek.transport.model.problem.builder;

import com.oskopek.transport.model.problem.Location;
import com.oskopek.transporteditor.view.LocalizableSortableBeanPropertyUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.*;

public class LocationBuilderTest {

    private LocationBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = new LocationBuilder();
    }

    @Test
    public void testBuild() throws Exception {
        builder.setName("test");
        builder.setxCoordinate(0);
        builder.setyCoordinate(1);
        assertThat(builder.build()).isEqualTo(new Location("test", 0, 1));
    }

    @Test
    public void testFrom() throws Exception {
        builder.from(new Location("test", 0, 1));
        assertThat(builder.build()).isEqualTo(new Location("test", 0, 1));
    }

    @Test
    public void testBeanMethods() throws Exception {
        builder.from(new Location("test", 0, 1));
        ObservableList<PropertySheet.Item> properties = BeanPropertyUtils.getProperties(builder);
        assertThat(properties).hasSize(4).allMatch(PropertySheet.Item::isEditable);
    }

    @Test
    public void testBeanMethodsEdit() throws Exception {
        builder.from(new Location("test", 0, 1));
        ObservableList<PropertySheet.Item> properties = BeanPropertyUtils.getProperties(builder);
        assertThat(properties).hasSize(4);
        PropertySheet.Item xCoordinate = properties.stream().filter(i -> i.getName().equals("xCoordinate")).findAny()
                .orElseThrow(IllegalStateException::new);
        assertThat(xCoordinate).isNotNull();
        xCoordinate.setValue(1);
        assertThat(builder.build()).isEqualTo(new Location("test", 1, 1));
    }

    @Test
    public void testLocalizableBeanMethodsEdit() throws Exception {
        builder.from(new Location("test", 0, 1));
        ObservableList<PropertySheet.Item> properties
                = LocalizableSortableBeanPropertyUtils.getProperties(builder, new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                return key;
            }

            @Override
            public Enumeration<String> getKeys() {
                return Collections.emptyEnumeration();
            }
        });
        assertThat(properties).hasSize(3);
        assertThat(properties).allMatch(Objects::nonNull);
        PropertySheet.Item xCoordinate = properties.stream().filter(i -> i.getName().equals("location.X")).findAny()
                .orElseThrow(IllegalStateException::new);
        assertThat(xCoordinate).isNotNull();
        xCoordinate.setValue(1);
        assertThat(builder.build()).isEqualTo(new Location("test", 1, 1));
    }

    @Test
    public void testBeanMethodsEditWithUpdate() throws Exception {
        ObjectProperty<Location> locationProp = new SimpleObjectProperty<>(new Location("test", 0, 1));
        builder.from(locationProp.get(), locationProp::setValue);
        ObservableList<PropertySheet.Item> properties = BeanPropertyUtils.getProperties(builder);
        PropertySheet.Item xCoordinate = properties.stream().filter(i -> i.getName().equals("xCoordinate")).findAny()
                .orElseThrow(IllegalStateException::new);
        assertThat(xCoordinate).isNotNull();
        xCoordinate.setValue(1);
        assertThat(locationProp.get()).isEqualTo(new Location("test", 0, 1));
        builder.update();
        assertThat(locationProp.get()).isEqualTo(new Location("test", 1, 1));
    }

}
