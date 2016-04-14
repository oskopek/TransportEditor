package com.oskopek.bp.editor.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * StudyGuide JavaFX main class.
 */
public class BPEditorApplication extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("BPEditor");
        this.primaryStage.getIcons().add(new Image(BPEditorApplication.class.getResourceAsStream("logo_64x64.png")));
        initRootLayout();
    }

    /**
     * Initializes the root layout.
     */
    private void initRootLayout() {
        VBox rootLayout = new VBox(); // TODO continue here with the layouts

        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
        primaryStage.show();
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
        launch(args);
    }
}
