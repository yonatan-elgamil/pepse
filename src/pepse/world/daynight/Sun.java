package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Creates the sun object and its movement during the day cycle.
 *
 * @author Yonatan Algamil
 */
public class Sun {
    private static final int SUN_SIZE = 100;

    private static final float WINDOW_CENTER_FACTOR = 0.5f;
    private static final float INITIAL_SUN_HEIGHT_FACTOR = 1f / 3f;
    private static final float CYCLE_CENTER_HEIGHT_FACTOR = 2f / 3f;
    private static final float START_ANGLE = 0f;
    private static final float FULL_CIRCLE_ANGLE = 360f;

    private static final Color SUN_COLOR = Color.YELLOW;
    private static final String SUN_TAG = "sun";

    private Sun() {
    }

    /**
     * Creates the sun object.
     *
     * @param windowDimensions The dimensions of the game window.
     * @param cycleLength The full day cycle length.
     * @return The created sun object.
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {
        Vector2 sunDimensions = Vector2.ONES.mult(SUN_SIZE);

        Vector2 initialSunCenter = Vector2.of(
                windowDimensions.x() * WINDOW_CENTER_FACTOR,
                windowDimensions.y() * INITIAL_SUN_HEIGHT_FACTOR);

        Vector2 cycleCenter = Vector2.of(
                windowDimensions.x() * WINDOW_CENTER_FACTOR,
                windowDimensions.y() * CYCLE_CENTER_HEIGHT_FACTOR);

        GameObject sun = new GameObject(
                initialSunCenter.subtract(sunDimensions.mult(WINDOW_CENTER_FACTOR)),
                sunDimensions,
                new OvalRenderable(SUN_COLOR));

        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sun.setTag(SUN_TAG);

        new Transition<Float>(
                sun,
                angle -> sun.setCenter(
                        initialSunCenter.subtract(cycleCenter)
                                .rotated(angle)
                                .add(cycleCenter)),
                START_ANGLE,
                FULL_CIRCLE_ANGLE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_LOOP,
                null);

        return sun;
    }
}