package com.oskopek.transporteditor.view;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

/**
 * Sprite builder utility class used for building stylized sprites for
 * a {@link com.oskopek.transporteditor.model.problem.RoadGraph}.
 * @param <T> the class of the built sprite
 */
public class SpriteBuilder<T extends Sprite> {

    private final T sprite;

    /**
     * Default constructor. Adds the sprite to the manager.
     *
     * @param manager the manager
     * @param name the name of the sprite
     * @param clazz the class of the sprite
     */
    public SpriteBuilder(SpriteManager manager, String name, Class<T> clazz) {
        this.sprite = manager.addSprite(name, clazz);
    }

    /**
     * Attach sprite to an edge.
     *
     * @param edge the edge to attach to
     * @return this
     */
    public SpriteBuilder attachTo(Edge edge) {
        sprite.attachToEdge(edge.getId());
        return this;
    }

    /**
     * Attach sprite to an node.
     *
     * @param node the node to attach to
     * @return this
     */
    public SpriteBuilder attachTo(Node node) {
        sprite.attachToNode(node.getId());
        return this;
    }

    /**
     * Set the position of the sprite.
     *
     * @param percentage the percentage (edge = distance along from source to destination, node = ?)
     * @param radiusPx the radius in pixels
     * @param degrees the degrees of rotation on the edge XY plane (x axis is in the direction of the arrow)
     * @return this
     */
    public SpriteBuilder setPosition(double percentage, double radiusPx, double degrees) {
        if (radiusPx == 0d && degrees == 0d) {
            sprite.setPosition(percentage);
            return this;
        }

        double rad = Math.toRadians(degrees);
        double yOffset = Math.sin(rad) * radiusPx; // y axis is perpendicular to the x axis
        double xOffset = Math.cos(rad) * 0.05; // down scale to 5% // x axis is in the direction of the arrow

        sprite.setPosition(StyleConstants.Units.PX, percentage + xOffset, yOffset, 0);
        return this;
    }

    /**
     * Set the position of the sprite.
     *
     * @param radiusPx the radius in pixels
     * @param degrees the degrees of rotation on the XY plane
     * @return this
     */
    public SpriteBuilder setPosition(double radiusPx, double degrees) {
        sprite.setPosition(StyleConstants.Units.PX, radiusPx, 0, degrees);
        return this;
    }

    /**
     * Set the CSS class of the sprite.
     *
     * @param cssClass the CSS class name
     * @return this
     */
    public SpriteBuilder setClass(String cssClass) {
        sprite.setAttribute("ui.class", cssClass);
        return this;
    }

    /**
     * Returns the sprite built.
     *
     * @return the built sprite
     */
    public T build() {
        return sprite;
    }

}
