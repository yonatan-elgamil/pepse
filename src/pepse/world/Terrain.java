package pepse.world;

import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.utils.ColorSupplier;
import pepse.utils.NoiseGenerator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates the terrain blocks and provides the ground height at each x coordinate.
 *
 * @author Yonatan Algamil
 */
public class Terrain {
    private static final int GROUND_RED = 212;
    private static final int GROUND_GREEN = 123;
    private static final int GROUND_BLUE = 74;
    private static final Color BASE_GROUND_COLOR = new Color(
            GROUND_RED,
            GROUND_GREEN,
            GROUND_BLUE);

    private static final float GROUND_HEIGHT_FACTOR = 2f / 3f;
    private static final int TERRAIN_DEPTH = 20;
    private static final int NOISE_FACTOR_MULTIPLIER = 7;
    private static final int NOISE_FACTOR = Block.SIZE * NOISE_FACTOR_MULTIPLIER;
    private static final String GROUND_TAG = "ground";

    private final float groundHeightAtX0;
    private final NoiseGenerator noiseGenerator;

    /**
     * Constructs a new Terrain.
     *
     * @param windowDimensions The dimensions of the game window.
     * @param seed The seed used for generating the terrain noise.
     */
    public Terrain(Vector2 windowDimensions, int seed) {
        this.groundHeightAtX0 = windowDimensions.y() * GROUND_HEIGHT_FACTOR;
        this.noiseGenerator = new NoiseGenerator(seed, (int) groundHeightAtX0);
    }

    /**
     * Returns the ground height at the given x coordinate.
     *
     * @param x The x coordinate.
     * @return The ground height at x.
     */
    public float groundHeightAt(float x) {
        return groundHeightAtX0 + (float) noiseGenerator.noise(x, NOISE_FACTOR);
    }

    /**
     * Creates terrain blocks in the given x range.
     *
     * @param minX The minimum x coordinate of the range.
     * @param maxX The maximum x coordinate of the range.
     * @return A list of terrain blocks in the given range.
     */
    public List<Block> createInRange(int minX, int maxX) {
        List<Block> blocks = new ArrayList<>();

        int startX = roundDownToBlockSize(minX);
        int endX = roundDownToBlockSize(maxX);

        for (int x = startX; x <= endX; x += Block.SIZE) {
            addGroundColumn(blocks, x);
        }

        return blocks;
    }

    /*
     * Adds one vertical column of ground blocks.
     */
    private void addGroundColumn(List<Block> blocks, int x) {
        int groundY = roundDownToBlockSize(groundHeightAt(x));

        for (int i = 0; i < TERRAIN_DEPTH; i++) {
            int y = groundY + i * Block.SIZE;
            blocks.add(createGroundBlock(x, y));
        }
    }

    /*
     * Creates one ground block.
     */
    private Block createGroundBlock(int x, int y) {
        Block block = new Block(
                Vector2.of(x, y),
                new RectangleRenderable(
                        ColorSupplier.approximateColor(BASE_GROUND_COLOR)));

        block.setTag(GROUND_TAG);
        return block;
    }

    /*
     * Rounds a coordinate down to the nearest block-size multiple.
     */
    private int roundDownToBlockSize(float value) {
        return (int) Math.floor(value / Block.SIZE) * Block.SIZE;
    }
}