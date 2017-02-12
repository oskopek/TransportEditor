package com.oskopek.transporteditor.view;

import javafx.stage.Stage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.testfx.api.FxAssert.*;
import org.testfx.framework.junit.ApplicationTest;
import static org.testfx.matcher.base.NodeMatchers.*;
import static org.assertj.core.api.Assertions.*;

public class ApplicationStartupUT extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setMaximized(true); // needs to happen
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ApplicationTest.launch(TransportEditorApplication.class);
        Thread.sleep(5000);
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
