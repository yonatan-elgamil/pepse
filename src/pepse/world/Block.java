package pepse.world;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * Represents a fixed square block in the world.
 * Blocks are immovable objects that prevent intersections.
 *
 * @author Yonatan Algamil
 */
public class Block extends GameObject {
    /**
     * The width and height of each block.
     */
    public static final int SIZE = 30;

    /**
     * Constructs a new Block.
     *
     * @param topLeftCorner The top-left corner of the block.
     * @param renderable The renderable used to draw the block.
     */
    public Block(Vector2 topLeftCorner, Renderable renderable) {
        super(topLeftCorner, Vector2.ONES.mult(SIZE), renderable);

        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
    }
}