package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.plan.PlanningSession;
import com.oskopek.transporteditor.weld.StartupStage;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;

/**
 * TransportEditor JavaFX main class.
 */
@Singleton
public class TransportEditorApplication extends Application {

    private final String logoResource = "logo_64x64.png";
    private final String logoResourceLarge = "logo_640x640.png";
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectProperty<PlanningSession> planningSession = new SimpleObjectProperty<>();
    private transient Stage primaryStage;

    /**
     * Main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage initStage) throws Exception {
        final Stage primaryStage = new Stage(StageStyle.DECORATED);
        Task<ObservableValue<Stage>> mainStageTask = new Task<ObservableValue<Stage>>() {
            @Override
            protected ObservableValue<Stage> call() throws Exception {
                Weld weld = new Weld();
                WeldContainer container = weld.initialize(); // Initialize Weld CDI
                primaryStage.setTitle("TransportEditor");
                primaryStage.setOnCloseRequest(event -> {
                    logger.debug("Closing down Weld.");
                    weld.shutdown();
                });
                primaryStage.getIcons().add(
                        new Image(TransportEditorApplication.class.getResourceAsStream(logoResource)));
                container.event().select(Stage.class, new AnnotationLiteral<StartupStage>() {
                }).fire(primaryStage);
                return new ReadOnlyObjectWrapper<>(primaryStage);
            }
        };
        mainStageTask.exceptionProperty().addListener((observable, oldValue, newValue) ->
            Platform.runLater(() -> {
                throw new IllegalStateException("Main stage loading failed.", newValue);
            }));
        showSplashScreen(initStage, mainStageTask);
    }

    /**
     * Helper method to construct a splash screen and display it while preparing the main stage for the application.
     *
     * @param initStage the initial stage to display the splash screen on
     * @param mainStageTask the task that loads, constructs and displays the main app
     */
    private void showSplashScreen(Stage initStage, Task<ObservableValue<Stage>> mainStageTask) {
        int SPLASH_WIDTH = 640;
        int SPLASH_HEIGHT = 640;
        Pane splashLayout = new VBox();
        ImageView splash = new ImageView(new Image(getClass().getResourceAsStream(logoResourceLarge)));
        splashLayout.getChildren().add(splash);
        mainStageTask.stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(javafx.util.Duration.seconds(1.2), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();
            }
        });
        Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.initStyle(StageStyle.TRANSPARENT);
        initStage.show();
        new Thread(mainStageTask).start();
    }

    /**
     * Get the main {@link javafx.stage.Window} element of the app.
     *
     * @return non-null
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Set the main {@link javafx.stage.Window} element of the app.
     *
     * @param primaryStage non-null stage
     * @throws IllegalArgumentException if the stage is null
     */
    public void setPrimaryStage(Stage primaryStage) throws IllegalArgumentException {
        if (primaryStage == null) {
            throw new IllegalArgumentException("Primary stage cannot be null.");
        }
        this.primaryStage = primaryStage;
    }

    /**
     * Get the current planning session in the UI.
     *
     * @return may be null (no session loaded)
     */
    public PlanningSession getPlanningSession() {
        return planningSession.get();
    }

    /**
     * Set the current planning session.
     *
     * @param planningSession may be null (no session)
     */
    public void setPlanningSession(PlanningSession planningSession) {
        this.planningSession.set(planningSession);
    }

    /**
     * Property for {@link #getPlanningSession()}.
     *
     * @return the planning session property, not null
     */
    public ObjectProperty<PlanningSession> planningSessionProperty() {
        return planningSession;
    }
}
