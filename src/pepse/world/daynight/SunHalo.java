package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Creates the halo that follows the sun.
 *
 * @author Yonatan Algamil
 */
public class SunHalo {
    private static final int HALO_SIZE = 180;

    private static final int HALO_RED = 255;
    private static final int HALO_GREEN = 255;
    private static final int HALO_BLUE = 0;
    private static final int HALO_ALPHA = 20;
    private static final float CENTER_FACTOR = 0.5f;

    private static final Color HALO_COLOR = new Color(
            HALO_RED,
            HALO_GREEN,
            HALO_BLUE,
            HALO_ALPHA);
    private static final String HALO_TAG = "sun halo";

    private SunHalo() {
    }

    /**
     * Creates a sun halo object.
     *
     * @param sun The sun object that the halo follows.
     * @return The created sun halo object.
     */
    public static GameObject create(GameObject sun) {
        Vector2 haloDimensions = Vector2.ONES.mult(HALO_SIZE);

        GameObject sunHalo = new GameObject(
                sun.getCenter().subtract(haloDimensions.mult(CENTER_FACTOR)),
                haloDimensions,
                new OvalRenderable(HALO_COLOR));

        sunHalo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sunHalo.setTag(HALO_TAG);
        sunHalo.addComponent(deltaTime -> sunHalo.setCenter(sun.getCenter()));

        return sunHalo;
    }
}