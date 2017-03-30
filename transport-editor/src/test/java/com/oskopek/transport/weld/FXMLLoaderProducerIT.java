package com.oskopek.transport.weld;

import javafx.fxml.FXMLLoader;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.exceptions.UnsatisfiedResolutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FXMLLoaderProducerIT {

    private WeldContainer container;

    @Before
    public void setUp() throws Exception {
        container = new Weld().initialize();
    }

    @Test
    public void weldInjectionFromMessageProducer() throws Exception {
        FXMLLoaderProducer loaderProducer = container.instance().select(FXMLLoaderProducer.class).get();
        assertNotNull(loaderProducer);

        FXMLLoader loader = loaderProducer.createLoader();
        assertNotNull(loader);
        assertNotNull(loader.getResources());

        FXMLLoader loader2 = loaderProducer.createLoader();
        assertNotNull(loader2);
        assertNotNull(loader2.getResources());

        assertEquals(loader, loader2);
    }

    @Test
    public void weldInjectionFromMessageProducerNoCDIWrongWay() throws Exception {
        FXMLLoaderProducer loaderProducer = new FXMLLoaderProducer();
        assertNotNull(loaderProducer);

        FXMLLoader loader = loaderProducer.createLoader();
        assertNotNull(loader);
        assertNull(loader.getResources());
    }

    @Test(expected = UnsatisfiedResolutionException.class)
    public void weldInjectionFromMessageProducerNoCDI() throws Exception {
        container.close();
        container.instance().select(FXMLLoaderProducer.class).get();
    }

    @After
    public void tearDown() throws Exception {
        try {
            container.close();
        } catch (IllegalStateException e) {
            // ignore already shutdown exception
        }
    }
}
