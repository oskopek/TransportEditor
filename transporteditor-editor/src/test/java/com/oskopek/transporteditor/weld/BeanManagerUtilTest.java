package com.oskopek.transporteditor.weld;

import com.oskopek.transporteditor.TestUtils;
import static org.junit.Assert.*;
import org.junit.Test;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;

public class BeanManagerUtilTest {

    private final Runnable getCurrentCDI = CDI::current;

    @Test
    public void createBeanInstanceWithNoCDI() throws Exception {
        assertTrue(TestUtils.isThrown(getCurrentCDI, IllegalStateException.class));
        assertNotNull(BeanManagerUtil.createBeanInstance(FXMLLoaderProducer.class));
        assertTrue(TestUtils.isThrown(getCurrentCDI, IllegalStateException.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBeanInstanceOfIllegalTypeNoCDI() throws Exception {
        assertTrue(TestUtils.isThrown(getCurrentCDI, IllegalStateException.class));
        BeanManagerUtil.createBeanInstance(Serializable.class);
    }

}
