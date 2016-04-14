package com.oskopek.bp.editor.view;

import javafx.fxml.FXMLLoader;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * Created by skopeko on 14.4.16.
 */
public class FXMLLoaderProducer {

    @Inject
    private Instance<Object> instance;

    @Produces
    public FXMLLoader createLoader() {
        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(param -> instance.select(param).get());
        return loader;
    }
}
