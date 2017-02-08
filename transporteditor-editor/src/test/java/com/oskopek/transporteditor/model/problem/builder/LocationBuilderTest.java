package com.oskopek.transporteditor.model.problem.builder;

import com.oskopek.transporteditor.model.problem.Location;
import javafx.collections.ObservableList;
import static org.assertj.core.api.Assertions.*;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;
import org.junit.Before;
import org.junit.Test;

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
        assertThat(properties).hasSize(3).allMatch(PropertySheet.Item::isEditable);
    }

    @Test
    public void testBeanMethodsEdit() throws Exception {
        builder.from(new Location("test", 0, 1));
        ObservableList<PropertySheet.Item> properties = BeanPropertyUtils.getProperties(builder);
        PropertySheet.Item xCoordinate = properties.stream().filter(i -> i.getName().equals("xCoordinate")).findAny()
                .orElseThrow(IllegalStateException::new);
        assertThat(xCoordinate).isNotNull();
        xCoordinate.setValue(1);
        assertThat(builder.build()).isEqualTo(new Location("test", 1, 1));
    }

}
