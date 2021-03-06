package com.oskopek.transport.view;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.builder.PackageBuilder;
import javafx.collections.ObservableList;
import org.assertj.core.api.Assertions;
import org.controlsfx.control.PropertySheet;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;
import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.*;

public class PackageBuilderTest {

    private final Location location1 = new Location("loc1", 0, 1);
    private final Location location2 = new Location("loc2", 1, 0);
    private final Package defaultPackage = new Package("test", location1, location2, ActionCost.ONE);
    private final ResourceBundle defaultBundle = new ResourceBundle() {
        @Override
        protected Object handleGetObject(String key) {
            return key;
        }

        @Override
        public Enumeration<String> getKeys() {
            return Collections.emptyEnumeration();
        }
    };
    private PackageBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = new PackageBuilder();
    }

    @Test
    public void testBuild() throws Exception {
        builder.setName("test");
        builder.setLocation(location1);
        builder.setTarget(location2);
        builder.setSize(ActionCost.ONE);
        assertThat(builder.build()).isEqualTo(defaultPackage);
    }

    @Test
    public void testLocalizableBeanMethodsEdit() throws Exception {
        builder.from(defaultPackage);
        ObservableList<PropertySheet.Item> properties
                = LocalizableSortableBeanPropertyUtils.getProperties(builder, defaultBundle);
        PropertySheet.Item location = properties.stream().filter(i -> i.getName().equals("location")).findAny()
                .orElseThrow(IllegalStateException::new);
        assertThat(location).isNotNull();
        location.setValue(location2);
        assertThat(builder.build()).isEqualTo(new Package("test", location2, location2, ActionCost.ONE));
    }

    @Test
    public void testLocalizableBeanMethodsEditWithNull() throws Exception {
        builder.from(defaultPackage);
        ObservableList<PropertySheet.Item> properties
                = LocalizableSortableBeanPropertyUtils.getProperties(builder, defaultBundle);
        PropertySheet.Item location = properties.stream().filter(i -> i.getName().equals("location")).findAny()
                .orElseThrow(IllegalStateException::new);
        assertThat(location).isNotNull();
        location.setValue(null);
        assertThat(builder.build()).isEqualTo(new Package("test", null, location2, ActionCost.ONE));
    }

    @Test
    public void testLocalizableBeanMethodsEditFromNull() throws Exception {
        builder.from(new Package("test", null, location2, ActionCost.ONE));
        ObservableList<PropertySheet.Item> properties
                = LocalizableSortableBeanPropertyUtils.getProperties(builder, defaultBundle);
        Assertions.assertThat(properties).hasSize(3);
        Optional<PropertySheet.Item> location = properties.stream().filter(i -> i.getName().equals("location"))
                .findAny();
        Assertions.assertThat(location).isNotNull().isEmpty();
        builder.setLocation(location1);
        assertThat(builder.build()).isEqualTo(defaultPackage);
    }

}
