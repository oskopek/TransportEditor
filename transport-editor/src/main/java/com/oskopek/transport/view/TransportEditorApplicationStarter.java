package com.oskopek.transport.view;

import com.google.common.eventbus.EventBus;
import com.oskopek.transport.event.GraphUpdatedEvent;
import com.oskopek.transport.weld.DeadEventListener;
import com.oskopek.transport.weld.StartupStage;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;

/**
 * A CDI-enabled version of {@link TransportEditorApplication} that initializes the root layout.
 */
class TransportEditorApplicationStarter {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    @Named("fxmlloader")
    private Instance<FXMLLoader> fxmlLoader;
    @Inject
    @Named("mainApp")
    private TransportEditorApplication application;
    @Inject
    private EventBus eventBus;
    @Inject
    private DeadEventListener deadEventListener;

    /**
     * Initializes the root layout.
     *
     * @param primaryStage the primaryStage delegated from the {@link TransportEditorApplication} that calls us
     */
    private void initRootLayout(@Observes @StartupStage Stage primaryStage) {
        FXMLLoader fxmlLoader = this.fxmlLoader.get();
        VBox rootLayout = null;
        fxmlLoader.setLocation(getClass().getResource("RootLayoutPane.fxml"));
        try (InputStream is = getClass().getResourceAsStream("RootLayoutPane.fxml")) {
            rootLayout = fxmlLoader.load(is);
        } catch (IOException e) {
            AlertCreator.handleLoadLayoutError(fxmlLoader.getResources(),
                    a -> application.centerInPrimaryStage(a, -200, -50), e);
        }
        Scene scene = new Scene(rootLayout);
        Platform.runLater(() -> {
            primaryStage.setMinHeight(650d);
            primaryStage.setMinWidth(650d);
            primaryStage.setScene(scene);
            primaryStage.show();
            eventBus.post(new GraphUpdatedEvent());
        });
        eventBus.register(deadEventListener);
        application.setEventBus(eventBus);
        application.setPrimaryStage(primaryStage);
    }
}
