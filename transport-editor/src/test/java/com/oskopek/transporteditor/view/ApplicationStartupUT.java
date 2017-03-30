package com.oskopek.transporteditor.view;

import com.oskopek.transport.view.TransportEditorApplication;
import javafx.stage.Stage;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import javax.enterprise.inject.spi.CDI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;

public class ApplicationStartupUT extends ApplicationTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        ApplicationTest.launch(TransportEditorApplication.class);
        Thread.sleep(5000);
    }

    @AfterClass
    public static void tearDownClass() {
        WeldContainer container = (WeldContainer) CDI.current();
        container.close();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setMaximized(true); // needs to happen
    }

    @Before
    public void setUp() throws Exception {
        clickOn("#sessionMenu");
        clickOn("#sessionNewMenuItem");
        if (lookup("#dialogPane").query() != null) {
            clickOn("Discard");
        }
    }

    @Test
    public void shouldContainAboutMenu() {
        assertThat(lookup("#aboutMenu").tryQuery()).isPresent();
        verifyThat("#aboutMenu", isEnabled());
    }

    @Test
    public void shouldCreateNewSession() throws Exception {
        verifyThat("#domainMenu", isEnabled());
        verifyThat("#problemMenu", isDisabled());
        clickOn("#sessionMenu");
        verifyThat("#sessionNewMenuItem", isNotNull());
        verifyThat("#sessionNewMenuItem", isEnabled());
        clickOn("#sessionNewMenuItem");
        verifyThat("#domainMenu", isEnabled());
        verifyThat("#problemMenu", isDisabled());
    }

    @Test
    public void shouldCreateNewDomain() throws Exception {
        verifyThat("#domainMenu", isEnabled());
        verifyThat("#problemMenu", isDisabled());
        clickOn("#domainMenu");
        verifyThat("#domainNewMenuItem", isNotNull());
        verifyThat("#domainNewMenuItem", isEnabled());
        clickOn("#domainNewMenuItem");
        clickOn("#sequentialRadio");
        clickOn("Apply");
        verifyThat("#problemMenu", isEnabled());
    }
}
