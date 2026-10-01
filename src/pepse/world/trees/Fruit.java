package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.ScheduledTask;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import pepse.world.avatar.Avatar;

import java.awt.Color;

/**
 * Represents a fruit that can be eaten by the avatar.
 * When eaten, the fruit gives energy, disappears, and respawns after a cycle.
 *
 * @author Yonatan Algamil
 */
public class Fruit extends GameObject {
    /**
     * The tag used for fruit objects.
     */
    public static final String FRUIT_TAG = "fruit";

    private static final int ENERGY_BONUS = 10;
    private static final Color FRUIT_COLOR = Color.RED;
    private static final float FULL_OPACITY = 1f;
    private static final float HIDDEN_OPACITY = 0f;

    private final float cycleLength;
    private boolean isEaten;

    /**
     * Constructs a new Fruit.
     *
     * @param topLeftCorner The top-left corner of the fruit.
     * @param dimensions The dimensions of the fruit.
     * @param cycleLength The time until the fruit respawns after being eaten.
     */
    public Fruit(Vector2 topLeftCorner, Vector2 dimensions, float cycleLength) {
        super(topLeftCorner, dimensions, new OvalRenderable(FRUIT_COLOR));

        this.cycleLength = cycleLength;
        this.isEaten = false;

        setTag(FRUIT_TAG);
    }

    /**
     * Checks whether this fruit should collide with another object.
     *
     * @param other The other object.
     * @return True if the fruit should collide with the other object.
     */
    @Override
    public boolean shouldCollideWith(GameObject other) {
        return !isEaten && other instanceof Avatar;
    }

    /**
     * Handles a collision with another object.
     *
     * @param other The other object.
     * @param collision The collision information.
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        if (!(other instanceof Avatar) || isEaten) {
            return;
        }

        Avatar avatar = (Avatar) other;
        avatar.addEnergy(ENERGY_BONUS);
        eatFruit();
    }

    /*
     * Hides the fruit and schedules its respawn.
     */
    private void eatFruit() {
        isEaten = true;
        renderer().setOpaqueness(HIDDEN_OPACITY);

        new ScheduledTask(
                this,
                cycleLength,
                false,
                this::respawnFruit);
    }

    /*
     * Makes the fruit visible and collidable again.
     */
    private void respawnFruit() {
        isEaten = false;
        renderer().setOpaqueness(FULL_OPACITY);
    }
}