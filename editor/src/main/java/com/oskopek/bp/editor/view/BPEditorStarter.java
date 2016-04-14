package com.oskopek.bp.editor.view;

import com.oskopek.bp.editor.weld.StartupScene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by skopeko on 14.4.16.
 */
public class BPEditorStarter {

    @Inject
    private FXMLLoader fxmlLoader;

    /**
            * Initializes the root layout.
            */
    private void initRootLayout(@Observes @StartupScene Stage primaryStage) {
        VBox rootLayout;
        try (InputStream is = getClass().getResourceAsStream("RootLayout.fxml")) {
            rootLayout = fxmlLoader.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("An error occurred while reading the root layout.", e);
        }
        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
