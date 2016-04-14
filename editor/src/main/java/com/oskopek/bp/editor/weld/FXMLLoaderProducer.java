package com.oskopek.bp.editor.weld;

import javafx.fxml.FXMLLoader;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.ResourceBundle;

/**
 * Created by skopeko on 14.4.16.
 */
public class FXMLLoaderProducer {

    @Inject
    private Instance<Object> instance;

    @Produces
    public FXMLLoader createLoader() {
        ResourceBundle bundle = ResourceBundle.getBundle("com.oskopek.bp.editor.view.messages");
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(bundle);
        loader.setControllerFactory(param -> instance.select(param).get());
        return loader;
    }
}
