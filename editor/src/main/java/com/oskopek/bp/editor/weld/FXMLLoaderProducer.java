package com.oskopek.bp.editor.weld;

import javafx.fxml.FXMLLoader;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.ResourceBundle;

/**
 * Simple {@link FXMLLoader} producer, including loading of the {@code messages} resource bundle.
 */
public class FXMLLoaderProducer {

    @Inject
    private Instance<Object> instance;

    /**
     * Produces an {@link FXMLLoader} including the {@code messages} resource bundle. Useful for in all places
     * where an {@code *.fxml} component file is being loaded into a JavaFX container.
     * @return an initialized FXML loader
     */
    @Produces
    public FXMLLoader createLoader() {
        ResourceBundle bundle = ResourceBundle.getBundle("com.oskopek.bp.editor.view.messages");
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(bundle);
        loader.setControllerFactory(param -> instance.select(param).get());
        return loader;
    }
}
