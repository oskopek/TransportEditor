package com.oskopek.transporteditor.view;

import javafx.stage.Stage;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.testfx.api.FxAssert.*;
import org.testfx.framework.junit.ApplicationTest;
import static org.testfx.matcher.base.NodeMatchers.*;
import static org.assertj.core.api.Assertions.*;

public class ApplicationStartupUT extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
//        stage.show();
        stage.hide();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ApplicationTest.launch(TransportEditorApplication.class);
        Thread.sleep(5000);
    }

    @Test
    public void shouldContainAboutMenu() {
        assertThat(lookup("#aboutMenu").tryQuery()).isPresent();
        verifyThat("#aboutMenu", isEnabled());
    }

    @Test
    @Ignore("menu item clicking doesn't work yet")
    public void shouldLoadSession() throws Exception {
        verifyThat("#domainMenu", isDisabled());
        clickOn("#sessionMenu");
        verifyThat("#sessionLoadMenuItem", isNotNull());
        verifyThat("#sessionLoadMenuItem", isEnabled());
        clickOn("#sessionLoadMenuItem");
        verifyThat("#domainMenu", isEnabled());
    }

    @Test
    @Ignore("menu item clicking doesn't work yet")
    public void shouldCreateNewSession() throws Exception {
        verifyThat("#domainMenu", isDisabled());
        clickOn("#sessionMenu");
        verifyThat("#sessionNewMenuItem", isNotNull());
        verifyThat("#sessionNewMenuItem", isEnabled());
        clickOn("#sessionNewMenuItem");
        verifyThat("#domainMenu", isEnabled());
    }
}
