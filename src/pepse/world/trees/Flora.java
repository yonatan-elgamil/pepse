package pepse.world.trees;

import danogl.GameObject;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.utils.ColorSupplier;
import pepse.world.Block;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

/**
 * Creates trees, leaves and fruits in a given x range.
 * The created trees are deterministic according to their x coordinate and seed.
 *
 * @author Yonatan Algamil
 */
public class Flora {
    /**
     * The tag used for tree trunk objects.
     */
    public static final String TRUNK_TAG = "tree trunk";

    /**
     * The tag used for leaf objects.
     */
    public static final String LEAF_TAG = "leaf";

    private static final int TRUNK_RED = 100;
    private static final int TRUNK_GREEN = 50;
    private static final int TRUNK_BLUE = 20;
    private static final Color TRUNK_COLOR = new Color(
            TRUNK_RED,
            TRUNK_GREEN,
            TRUNK_BLUE);

    private static final int LEAF_RED = 50;
    private static final int LEAF_GREEN = 200;
    private static final int LEAF_BLUE = 30;
    private static final Color LEAF_COLOR = new Color(
            LEAF_RED,
            LEAF_GREEN,
            LEAF_BLUE);

    private static final float TREE_PROBABILITY = 0.1f;
    private static final float LEAF_PROBABILITY = 0.75f;
    private static final float FRUIT_PROBABILITY = 0.08f;
    private static final float DEFAULT_CYCLE_LENGTH = 30f;

    private static final int MIN_TRUNK_HEIGHT = 4;
    private static final int MAX_EXTRA_TRUNK_HEIGHT = 4;
    private static final int LEAF_SQUARE_RADIUS = 2;
    private static final int RANDOM_BOUND_ADDITION = 1;
    private static final int FIRST_TRUNK_BLOCK = 1;

    private static final float MIN_LEAF_WAIT_TIME = 0f;
    private static final float MAX_LEAF_WAIT_TIME = 1f;
    private static final float LEAF_MOVEMENT_TIME = 1.5f;
    private static final float MIN_LEAF_ANGLE = -10f;
    private static final float MAX_LEAF_ANGLE = 10f;
    private static final float LEAF_WIDTH_FACTOR = 1.15f;

    private final Function<Float, Float> groundHeightAt;
    private final int seed;

    /**
     * Constructs a new Flora object.
     *
     * @param groundHeightAt A function that returns the ground height at a given x.
     * @param seed The seed used for deterministic tree creation.
     */
    public Flora(Function<Float, Float> groundHeightAt, int seed) {
        this.groundHeightAt = groundHeightAt;
        this.seed = seed;
    }

    /**
     * Creates tree objects in the given x range.
     *
     * @param minX The minimum x coordinate of the range.
     * @param maxX The maximum x coordinate of the range.
     * @return A list of all tree-related objects in the range.
     */
    public List<GameObject> createInRange(int minX, int maxX) {
        List<GameObject> treeObjects = new ArrayList<>();

        int startX = roundDownToBlockSize(minX);
        int endX = roundDownToBlockSize(maxX);

        for (int x = startX; x <= endX; x += Block.SIZE) {
            Random treeRandom = new Random(Objects.hash(x, seed));

            if (treeRandom.nextFloat() < TREE_PROBABILITY) {
                treeObjects.addAll(createTreeAt(x, treeRandom));
            }
        }

        return treeObjects;
    }

    /*
     * Creates one tree at the given x coordinate.
     */
    private List<GameObject> createTreeAt(int x, Random treeRandom) {
        List<GameObject> treeObjects = new ArrayList<>();

        int groundY = roundDownToBlockSize(groundHeightAt.apply((float) x));
        int trunkHeight = MIN_TRUNK_HEIGHT +
                treeRandom.nextInt(MAX_EXTRA_TRUNK_HEIGHT + RANDOM_BOUND_ADDITION);
        int topY = groundY - trunkHeight * Block.SIZE;

        createTrunk(treeObjects, x, groundY, trunkHeight);
        createLeaves(treeObjects, x, topY, treeRandom);

        return treeObjects;
    }

    /*
     * Creates the trunk blocks of a tree.
     */
    private void createTrunk(List<GameObject> treeObjects,
                             int x,
                             int groundY,
                             int trunkHeight) {
        for (int i = FIRST_TRUNK_BLOCK; i <= trunkHeight; i++) {
            int y = groundY - i * Block.SIZE;
            treeObjects.add(createTrunkBlock(x, y));
        }
    }

    /*
     * Creates one trunk block.
     */
    private Block createTrunkBlock(int x, int y) {
        Block trunkBlock = new Block(
                Vector2.of(x, y),
                new RectangleRenderable(
                        ColorSupplier.approximateColor(TRUNK_COLOR)));

        trunkBlock.setTag(TRUNK_TAG);
        return trunkBlock;
    }

    /*
     * Creates the leaves and fruits of a tree.
     */
    private void createLeaves(List<GameObject> treeObjects,
                              int trunkX,
                              int topY,
                              Random treeRandom) {
        for (int row = -LEAF_SQUARE_RADIUS; row <= LEAF_SQUARE_RADIUS; row++) {
            for (int col = -LEAF_SQUARE_RADIUS; col <= LEAF_SQUARE_RADIUS; col++) {
                createLeafAndFruitIfNeeded(treeObjects, trunkX, topY, row, col, treeRandom);
            }
        }
    }

    /*
     * Creates a leaf and maybe a fruit at one leaf-grid position.
     */
    private void createLeafAndFruitIfNeeded(List<GameObject> treeObjects,
                                            int trunkX,
                                            int topY,
                                            int row,
                                            int col,
                                            Random treeRandom) {
        if (treeRandom.nextFloat() > LEAF_PROBABILITY) {
            return;
        }

        int x = trunkX + col * Block.SIZE;
        int y = topY + row * Block.SIZE;

        GameObject leaf = createLeaf(x, y, treeRandom);
        treeObjects.add(leaf);

        addFruitIfNeeded(treeObjects, x, y, treeRandom);
    }

    /*
     * Creates one leaf.
     */
    private GameObject createLeaf(int x, int y, Random treeRandom) {
        GameObject leaf = new GameObject(
                Vector2.of(x, y),
                Vector2.ONES.mult(Block.SIZE),
                new RectangleRenderable(
                        ColorSupplier.approximateColor(LEAF_COLOR)));

        leaf.setTag(LEAF_TAG);
        addLeafMovement(leaf, treeRandom);

        return leaf;
    }

    /*
     * Creates a fruit in the given position according to the fruit probability.
     */
    private void addFruitIfNeeded(List<GameObject> treeObjects,
                                  int x,
                                  int y,
                                  Random treeRandom) {
        if (treeRandom.nextFloat() >= FRUIT_PROBABILITY) {
            return;
        }

        Fruit fruit = new Fruit(
                Vector2.of(x, y),
                Vector2.ONES.mult(Block.SIZE),
                DEFAULT_CYCLE_LENGTH);

        treeObjects.add(fruit);
    }

    /*
     * Adds wind movement to a leaf after a short random delay.
     */
    private void addLeafMovement(GameObject leaf, Random treeRandom) {
        float waitTime = MIN_LEAF_WAIT_TIME +
                treeRandom.nextFloat() * (MAX_LEAF_WAIT_TIME - MIN_LEAF_WAIT_TIME);

        Vector2 originalDimensions = leaf.getDimensions();
        float originalWidth = originalDimensions.x();
        float widerWidth = originalWidth * LEAF_WIDTH_FACTOR;

        new ScheduledTask(
                leaf,
                waitTime,
                false,
                () -> startLeafMovement(leaf, originalDimensions, originalWidth, widerWidth));
    }

    /*
     * Starts the leaf movement transitions.
     */
    private void startLeafMovement(GameObject leaf,
                                   Vector2 originalDimensions,
                                   float originalWidth,
                                   float widerWidth) {
        createLeafAngleTransition(leaf);
        createLeafWidthTransition(leaf, originalDimensions, originalWidth, widerWidth);
    }

    /*
     * Creates the leaf angle transition.
     */
    private void createLeafAngleTransition(GameObject leaf) {
        new Transition<Float>(
                leaf,
                leaf.renderer()::setRenderableAngle,
                MIN_LEAF_ANGLE,
                MAX_LEAF_ANGLE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                LEAF_MOVEMENT_TIME,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null);
    }

    /*
     * Creates the leaf width transition.
     */
    private void createLeafWidthTransition(GameObject leaf,
                                           Vector2 originalDimensions,
                                           float originalWidth,
                                           float widerWidth) {
        new Transition<Float>(
                leaf,
                width -> leaf.setDimensions(
                        Vector2.of(width, originalDimensions.y())),
                originalWidth,
                widerWidth,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                LEAF_MOVEMENT_TIME,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null);
    }

    /*
     * Rounds a coordinate down to the nearest block-size multiple.
     */
    private int roundDownToBlockSize(float value) {
        return (int) Math.floor(value / Block.SIZE) * Block.SIZE;
    }
}