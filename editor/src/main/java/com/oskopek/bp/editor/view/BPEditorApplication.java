package com.oskopek.bp.editor.view;

import com.oskopek.bp.editor.weld.StartupScene;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;

/**
 * StudyGuide JavaFX main class.
 */
@Singleton
public class BPEditorApplication extends Application {

    private Stage primaryStage;

    private WeldContainer container;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize Weld CDI
        container = new Weld().initialize();

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("BPEditor");
        this.primaryStage.getIcons().add(new Image(BPEditorApplication.class.getResourceAsStream("logo_64x64.png")));

        // Now that JavaFX thread is ready // TODO reference https://dzone.com/articles/fxml-javafx-powered-cdi-jboss
        // let's inform whoever cares using standard CDI notification mechanism:
        // CDI events
        container.event().select(Stage.class, new AnnotationLiteral<StartupScene>() {}).fire(primaryStage);
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
     * Main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        BPEditorApplication.launch(args);
    }
}
