package pepse.world.avatar;

import danogl.GameObject;
import danogl.gui.ImageReader;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;

/**
 * Manages the avatar animations.
 * This class loads the avatar animation images and switches between them.
 *
 * @author Yonatan Algamil
 */
public class AvatarAnimation {
    private static final double TIME_BETWEEN_CLIPS = 0.15;

    private static final String IDLE_PREFIX = "assets/idle_";
    private static final String RUN_PREFIX = "assets/run_";
    private static final String JUMP_PREFIX = "assets/jump_";
    private static final String IMAGE_SUFFIX = ".png";

    private static final int IDLE_IMAGE_COUNT = 4;
    private static final int RUN_IMAGE_COUNT = 6;
    private static final int JUMP_IMAGE_COUNT = 4;

    private final AnimationRenderable idleAnimation;
    private final AnimationRenderable runAnimation;
    private final AnimationRenderable jumpAnimation;

    private AvatarState currentState;

    /**
     * Constructs a new AvatarAnimation.
     *
     * @param imageReader The image reader used for loading the animation images.
     */
    public AvatarAnimation(ImageReader imageReader) {
        this.idleAnimation = createAnimation(imageReader, IDLE_PREFIX, IDLE_IMAGE_COUNT);
        this.runAnimation = createAnimation(imageReader, RUN_PREFIX, RUN_IMAGE_COUNT);
        this.jumpAnimation = createAnimation(imageReader, JUMP_PREFIX, JUMP_IMAGE_COUNT);
        this.currentState = null;
    }

    /**
     * Updates the avatar animation according to the new avatar state.
     *
     * @param avatar The avatar whose animation should be updated.
     * @param newState The new state of the avatar.
     */
    public void updateAnimation(GameObject avatar, AvatarState newState) {
        if (newState == currentState) {
            return;
        }

        currentState = newState;

        if (newState == AvatarState.IDLE) {
            avatar.renderer().setRenderable(idleAnimation);
        } else if (newState == AvatarState.RUN) {
            avatar.renderer().setRenderable(runAnimation);
        } else if (newState == AvatarState.JUMP) {
            avatar.renderer().setRenderable(jumpAnimation);
        }
    }

    /**
     * Sets the horizontal direction of the avatar image.
     *
     * @param avatar The avatar whose image should be flipped.
     * @param isFacingLeft True if the avatar should face left, false otherwise.
     */
    public void setFacingLeft(GameObject avatar, boolean isFacingLeft) {
        avatar.renderer().setIsFlippedHorizontally(isFacingLeft);
    }

    /*
     * Creates an animation from a sequence of image files.
     */
    private AnimationRenderable createAnimation(ImageReader imageReader,
                                                String prefix,
                                                int imageCount) {
        Renderable[] images = new Renderable[imageCount];

        for (int i = 0; i < imageCount; i++) {
            images[i] = imageReader.readImage(prefix + i + IMAGE_SUFFIX, true);
        }

        return new AnimationRenderable(images, TIME_BETWEEN_CLIPS);
    }
}