package pepse.world.avatar;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Displays the avatar energy on the screen.
 * The display is attached to the camera, so it stays in the same screen position.
 *
 * @author Yonatan Algamil
 */
public class EnergyDisplay extends GameObject {
    private static final int DISPLAY_WIDTH = 120;
    private static final int DISPLAY_HEIGHT = 30;
    private static final Vector2 DISPLAY_SIZE = Vector2.of(DISPLAY_WIDTH, DISPLAY_HEIGHT);

    private static final Color TEXT_COLOR = Color.BLACK;
    private static final String ENERGY_SUFFIX = "%";
    private static final String ENERGY_TAG = "energy display";

    private final TextRenderable textRenderable;

    /**
     * Constructs a new EnergyDisplay.
     *
     * @param topLeftCorner The top-left corner of the display.
     * @param initialEnergy The initial energy value to show.
     */
    public EnergyDisplay(Vector2 topLeftCorner, int initialEnergy) {
        super(topLeftCorner,
                DISPLAY_SIZE,
                new TextRenderable(initialEnergy + ENERGY_SUFFIX));

        this.textRenderable = (TextRenderable) renderer().getRenderable();
        this.textRenderable.setColor(TEXT_COLOR);

        setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        setTag(ENERGY_TAG);
    }

    /**
     * Updates the displayed energy value.
     *
     * @param energy The new energy value to display.
     */
    public void updateEnergy(int energy) {
        textRenderable.setString(energy + ENERGY_SUFFIX);
    }
}