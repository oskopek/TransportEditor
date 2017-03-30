package com.oskopek.transporteditor.weld;

import com.oskopek.transport.weld.MessagesProducer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.*;

public class MessagesProducerTest {

    private Locale originalLocale;
    private ResourceBundle resourceBundle;

    @Before
    public void setUp() throws Exception {
        originalLocale = Locale.getDefault();
    }

    @Test
    public void createMessagesResourceBundleFound() throws Exception {
        resourceBundle = new MessagesProducer().createMessagesResourceBundle();
        assertNotNull(resourceBundle);
        assertNotNull(resourceBundle.getString("root.new"));
    }

    @Test
    public void createMessagesResourceBundleUS() throws Exception {
        Locale.setDefault(Locale.US);
        resourceBundle = new MessagesProducer().createMessagesResourceBundle();
        assertNotNull(resourceBundle);
        assertEquals("New", resourceBundle.getString("root.new"));
    }

    @Test
    @Ignore("Ignored until adding german translation")
    public void createMessagesResourceBundleGerman() throws Exception {
        Locale.setDefault(Locale.GERMAN);
        resourceBundle = new MessagesProducer().createMessagesResourceBundle();
        assertNotNull(resourceBundle);
        assertEquals("Neu", resourceBundle.getString("root.new"));
    }

    @After
    public void tearDown() throws Exception {
        Locale.setDefault(originalLocale);
    }
}
