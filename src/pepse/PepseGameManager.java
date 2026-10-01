package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.world.Block;
import pepse.world.Sky;
import pepse.world.Terrain;
import pepse.world.avatar.Avatar;
import pepse.world.avatar.EnergyDisplay;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Flora;
import pepse.world.trees.Fruit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the Pepse game.
 * This class creates the game objects and updates the infinite world.
 *
 * @author Yonatan Algamil
 */
public class PepseGameManager extends GameManager {
    private static final float DAY_CYCLE_LENGTH = 30f;
    private static final int SEED = 0;

    private static final int SUN_LAYER_OFFSET = 2;
    private static final int SUN_HALO_LAYER_OFFSET = 1;
    private static final int LEAF_LAYER_OFFSET = 1;

    private static final int SUN_LAYER = Layer.BACKGROUND + SUN_LAYER_OFFSET;
    private static final int SUN_HALO_LAYER = Layer.BACKGROUND + SUN_HALO_LAYER_OFFSET;
    private static final int LEAF_LAYER = Layer.STATIC_OBJECTS + LEAF_LAYER_OFFSET;

    private static final int CHUNK_WIDTH_BLOCKS = 20;
    private static final int CHUNK_WIDTH = Block.SIZE * CHUNK_WIDTH_BLOCKS;
    private static final int WORLD_BUFFER_CHUNKS = 2;

    private static final float CENTER_FACTOR = 0.5f;
    private static final int AVATAR_START_Y_OFFSET = 80;
    private static final int ENERGY_DISPLAY_X = 10;
    private static final int ENERGY_DISPLAY_Y = 10;
    private static final int LAST_X_IN_CHUNK_OFFSET = 1;

    private Terrain terrain;
    private Flora flora;
    private Avatar avatar;
    private Vector2 windowDimensions;

    private final Map<Integer, List<GameObject>> chunkObjects = new HashMap<>();

    /**
     * Runs the Pepse game.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        new PepseGameManager().run();
    }

    /**
     * Initializes the game objects and the world.
     *
     * @param imageReader The image reader used for loading images.
     * @param soundReader The sound reader used for loading sounds.
     * @param inputListener The input listener used for keyboard input.
     * @param windowController The window controller of the game.
     */
    @Override
    public void initializeGame(ImageReader imageReader,
                               SoundReader soundReader,
                               UserInputListener inputListener,
                               WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);

        setLayerCollisions();
        windowDimensions = windowController.getWindowDimensions();

        createSky();
        createTerrainAndFlora();
        Vector2 avatarLocation = createAvatar(inputListener, imageReader);
        setCameraOnAvatar(avatarLocation);
        createWorldAround(avatar.getCenter().x());
        createEnergyDisplay();
        createDayNightObjects();
    }

    /**
     * Updates the infinite world around the avatar.
     *
     * @param deltaTime The time passed since the previous update.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        createWorldAround(avatar.getCenter().x());
        removeFarChunks(avatar.getCenter().x());
    }

    /*
     * Creates the sky object.
     */
    private void createSky() {
        GameObject sky = Sky.create(windowDimensions);
        gameObjects().addGameObject(sky, Layer.BACKGROUND);
    }

    /*
     * Creates the terrain and flora factories.
     */
    private void createTerrainAndFlora() {
        terrain = new Terrain(windowDimensions, SEED);
        flora = new Flora(terrain::groundHeightAt, SEED);
    }

    /*
     * Creates the avatar and adds it to the game.
     */
    private Vector2 createAvatar(UserInputListener inputListener,
                                 ImageReader imageReader) {
        Vector2 avatarLocation = getInitialAvatarLocation();

        avatar = new Avatar(
                avatarLocation,
                inputListener,
                imageReader);

        gameObjects().addGameObject(avatar, Layer.DEFAULT);
        return avatarLocation;
    }

    /*
     * Calculates the initial avatar location.
     */
    private Vector2 getInitialAvatarLocation() {
        float avatarX = windowDimensions.x() * CENTER_FACTOR;
        float avatarY = terrain.groundHeightAt(avatarX) - AVATAR_START_Y_OFFSET;

        return Vector2.of(avatarX, avatarY);
    }

    /*
     * Sets the camera to follow the avatar.
     */
    private void setCameraOnAvatar(Vector2 avatarLocation) {
        setCamera(new Camera(
                avatar,
                windowDimensions.mult(CENTER_FACTOR).subtract(avatarLocation),
                windowDimensions,
                windowDimensions));
    }

    /*
     * Creates the energy display.
     */
    private void createEnergyDisplay() {
        EnergyDisplay energyDisplay = new EnergyDisplay(
                Vector2.of(ENERGY_DISPLAY_X, ENERGY_DISPLAY_Y),
                avatar.getEnergy());

        avatar.setEnergyCallback(energyDisplay::updateEnergy);
        gameObjects().addGameObject(energyDisplay, Layer.UI);
    }

    /*
     * Creates the sun, sun halo and night objects.
     */
    private void createDayNightObjects() {
        GameObject sun = Sun.create(windowDimensions, DAY_CYCLE_LENGTH);
        GameObject sunHalo = SunHalo.create(sun);
        GameObject night = Night.create(windowDimensions, DAY_CYCLE_LENGTH);

        gameObjects().addGameObject(sunHalo, SUN_HALO_LAYER);
        gameObjects().addGameObject(sun, SUN_LAYER);
        gameObjects().addGameObject(night, Layer.FOREGROUND);
    }

    /*
     * Creates world chunks around the given x coordinate.
     */
    private void createWorldAround(float centerX) {
        int minChunk = xToChunk(centerX - windowDimensions.x());
        int maxChunk = xToChunk(centerX + windowDimensions.x());

        for (int chunk = minChunk; chunk <= maxChunk; chunk++) {
            createChunkIfNeeded(chunk);
        }
    }

    /*
     * Creates a chunk if it was not already created.
     */
    private void createChunkIfNeeded(int chunk) {
        if (chunkObjects.containsKey(chunk)) {
            return;
        }

        int minX = chunk * CHUNK_WIDTH;
        int maxX = minX + CHUNK_WIDTH - LAST_X_IN_CHUNK_OFFSET;
        List<GameObject> objectsInChunk = new ArrayList<>();

        addTerrainObjects(minX, maxX, objectsInChunk);
        addFloraObjects(minX, maxX, objectsInChunk);

        chunkObjects.put(chunk, objectsInChunk);
    }

    /*
     * Adds terrain objects to the game and to the chunk list.
     */
    private void addTerrainObjects(int minX,
                                   int maxX,
                                   List<GameObject> objectsInChunk) {
        for (Block block : terrain.createInRange(minX, maxX)) {
            gameObjects().addGameObject(block, Layer.STATIC_OBJECTS);
            objectsInChunk.add(block);
        }
    }

    /*
     * Adds flora objects to the game and to the chunk list.
     */
    private void addFloraObjects(int minX,
                                 int maxX,
                                 List<GameObject> objectsInChunk) {
        for (GameObject treeObject : flora.createInRange(minX, maxX)) {
            gameObjects().addGameObject(treeObject, getLayerForObject(treeObject));
            objectsInChunk.add(treeObject);
        }
    }

    /*
     * Removes chunks that are too far from the avatar.
     */
    private void removeFarChunks(float centerX) {
        int currentChunk = xToChunk(centerX);
        List<Integer> chunksToRemove = new ArrayList<>();

        for (int chunk : chunkObjects.keySet()) {
            if (Math.abs(chunk - currentChunk) > WORLD_BUFFER_CHUNKS) {
                chunksToRemove.add(chunk);
            }
        }

        for (int chunk : chunksToRemove) {
            removeChunk(chunk);
        }
    }

    /*
     * Removes one chunk from the game.
     */
    private void removeChunk(int chunk) {
        List<GameObject> objects = chunkObjects.get(chunk);

        for (GameObject object : objects) {
            gameObjects().removeGameObject(object, getLayerForObject(object));
        }

        chunkObjects.remove(chunk);
    }

    /*
     * Returns the layer of a game object according to its tag.
     */
    private int getLayerForObject(GameObject object) {
        if (object.getTag().equals(Flora.LEAF_TAG)) {
            return LEAF_LAYER;
        }

        if (object.getTag().equals(Fruit.FRUIT_TAG)) {
            return Layer.DEFAULT;
        }

        return Layer.STATIC_OBJECTS;
    }

    /*
     * Converts an x coordinate to a chunk index.
     */
    private int xToChunk(float x) {
        return (int) Math.floor(x / CHUNK_WIDTH);
    }

    /*
     * Defines which layers should collide with each other.
     */
    private void setLayerCollisions() {
        gameObjects().layers().shouldLayersCollide(
                Layer.DEFAULT,
                Layer.STATIC_OBJECTS,
                true);

        gameObjects().layers().shouldLayersCollide(
                Layer.DEFAULT,
                LEAF_LAYER,
                false);

        gameObjects().layers().shouldLayersCollide(
                LEAF_LAYER,
                LEAF_LAYER,
                false);

        gameObjects().layers().shouldLayersCollide(
                LEAF_LAYER,
                Layer.STATIC_OBJECTS,
                false);
    }
}