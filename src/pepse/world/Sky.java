package pepse.world;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Creates the sky background object.
 *
 * @author Yonatan Algamil
 */
public class Sky {
    private static final String BASIC_SKY_COLOR_HEX = "#80C6E5";
    private static final Color BASIC_SKY_COLOR = Color.decode(BASIC_SKY_COLOR_HEX);
    private static final String SKY_TAG = "sky";

    private Sky() {
    }

    /**
     * Creates a sky object.
     *
     * @param windowDimensions The dimensions of the game window.
     * @return The created sky object.
     */
    public static GameObject create(Vector2 windowDimensions) {
        GameObject sky = new GameObject(
                Vector2.ZERO,
                windowDimensions,
                new RectangleRenderable(BASIC_SKY_COLOR));

        sky.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sky.setTag(SKY_TAG);

        return sky;
    }
}