package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Creates the night effect of the day-night cycle.
 * The effect is created by changing the opacity of a black rectangle.
 *
 * @author Yonatan Algamil
 */
public class Night {
    private static final float DAYLIGHT_OPACITY = 0f;
    private static final float MIDNIGHT_OPACITY = 0.5f;
    private static final float HALF_CYCLE_DIVIDER = 2f;

    private static final Color NIGHT_COLOR = Color.BLACK;
    private static final String NIGHT_TAG = "night";

    private Night() {
    }

    /**
     * Creates a night overlay object.
     *
     * @param windowDimensions The dimensions of the game window.
     * @param cycleLength The full day-night cycle length.
     * @return The created night overlay.
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {
        GameObject night = new GameObject(
                Vector2.ZERO,
                windowDimensions,
                new RectangleRenderable(NIGHT_COLOR));

        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        night.setTag(NIGHT_TAG);

        new Transition<Float>(
                night,
                night.renderer()::setOpaqueness,
                DAYLIGHT_OPACITY,
                MIDNIGHT_OPACITY,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                cycleLength / HALF_CYCLE_DIVIDER,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null);

        return night;
    }
}