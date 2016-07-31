package com.oskopek.transporteditor.weld;

import com.oskopek.transporteditor.test.TestUtils;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import static org.junit.Assert.*;
import org.junit.Test;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;

public class BeanManagerUtilIT {

    private final Runnable getCurrentCDI = CDI::current;

    @Test
    public void createBeanInstanceNotPossible() throws Exception {
        assertTrue(TestUtils.isThrown(getCurrentCDI, IllegalStateException.class));
        Weld weld = new Weld();
        try (WeldContainer container = weld.initialize()) {
            assertFalse(TestUtils.isThrown(getCurrentCDI, IllegalStateException.class));
            assertNotNull(BeanManagerUtil.createBeanInstance(FXMLLoaderProducer.class));
            assertFalse(TestUtils.isThrown(getCurrentCDI, IllegalStateException.class));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBeanInstanceOfIllegalType() throws Exception {
        assertTrue(TestUtils.isThrown(getCurrentCDI, IllegalStateException.class));
        Weld weld = new Weld();
        try (WeldContainer container = weld.initialize()) {
            assertFalse(TestUtils.isThrown(getCurrentCDI, IllegalStateException.class));
            BeanManagerUtil.createBeanInstance(Serializable.class);
        }
    }

}
