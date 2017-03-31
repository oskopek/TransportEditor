package com.oskopek.transport.weld;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

public class BeanManagerUtilIT {

    private final Supplier<CDI<?>> getCurrentCDI = CDI::current;

    @Test
    public void createBeanInstanceNotPossible() throws Exception {
        assertThatThrownBy(getCurrentCDI::get).isInstanceOf(IllegalStateException.class);
        Weld weld = new Weld();
        try (WeldContainer container = weld.initialize()) {
            assertThat(getCurrentCDI.get()).isNotNull();
            assertThat(BeanManagerUtil.createBeanInstance(FXMLLoaderProducer.class)).isNotNull();
            assertThat(getCurrentCDI.get()).isNotNull();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBeanInstanceOfIllegalType() throws Exception {
        assertThatThrownBy(getCurrentCDI::get).isInstanceOf(IllegalStateException.class);
        Weld weld = new Weld();
        try (WeldContainer container = weld.initialize()) {
            assertThat(getCurrentCDI.get()).isNotNull();
            BeanManagerUtil.createBeanInstance(Serializable.class);
        }
    }

}
