package com.oskopek.transporteditor.view;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

public class SpriteBuilder<T extends Sprite> {

    private final T sprite;

    public SpriteBuilder(SpriteManager manager, String name, Class<T> clazz) {
        this.sprite = manager.addSprite(name, clazz);
    }

    public SpriteBuilder attachTo(Edge edge) {
        sprite.attachToEdge(edge.getId());
        return this;
    }

    public SpriteBuilder attachTo(Node node) {
        sprite.attachToNode(node.getId());
        return this;
    }

    public SpriteBuilder setPosition(double percentage) {
        sprite.setPosition(percentage);
        return this;
    }

    public SpriteBuilder setClass(String cssClass) {
        sprite.setAttribute("ui.class", cssClass);
        return this;
    }

    public T build() {
        return sprite;
    }

}
