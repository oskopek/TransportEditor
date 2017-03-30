package com.oskopek.transporteditor.weld;

import org.junit.Test;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;

public class BeanManagerUtilTest {

    private final Supplier<CDI<?>> getCurrentCDI = CDI::current;

    @Test
    public void createBeanInstanceWithNoCDI() throws Exception {
        assertThatThrownBy(getCurrentCDI::get).isInstanceOf(IllegalStateException.class);
        assertNotNull(BeanManagerUtil.createBeanInstance(FXMLLoaderProducer.class));
        assertThatThrownBy(getCurrentCDI::get).isInstanceOf(IllegalStateException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBeanInstanceOfIllegalTypeNoCDI() throws Exception {
        assertThatThrownBy(getCurrentCDI::get).isInstanceOf(IllegalStateException.class);
        BeanManagerUtil.createBeanInstance(Serializable.class);
    }

}
