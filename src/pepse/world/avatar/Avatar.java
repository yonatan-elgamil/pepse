package pepse.world.avatar;

import danogl.GameObject;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.util.Vector2;

import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/**
 * Represents the player character in the game.
 * The avatar handles movement, jumping, energy changes and animation state.
 *
 * @author Yonatan Algamil
 */
public class Avatar extends GameObject {
    private static final float VELOCITY_X = 300;
    private static final float VELOCITY_Y = -650;
    private static final float GRAVITY = 600;

    private static final int AVATAR_WIDTH = 50;
    private static final int AVATAR_HEIGHT = 80;
    private static final Vector2 AVATAR_DIMENSIONS = Vector2.of(AVATAR_WIDTH, AVATAR_HEIGHT);

    private static final String IDLE_IMAGE_PATH = "assets/idle_0.png";
    private static final String AVATAR_TAG = "avatar";

    private static final int MAX_ENERGY = 100;
    private static final int MIN_ENERGY = 0;
    private static final int IDLE_ENERGY_GAIN = 1;
    private static final int RUN_ENERGY_COST = 2;
    private static final int JUMP_ENERGY_COST = 20;
    private static final int DOUBLE_JUMP_ENERGY_COST = 50;

    private final UserInputListener inputListener;
    private final AvatarAnimation avatarAnimation;

    private Consumer<Integer> energyCallback;
    private AvatarState currentState;
    private int energy;
    private boolean spaceWasPressed;
    private boolean hasDoubleJumped;

    /**
     * Constructs a new Avatar.
     *
     * @param topLeftCorner The initial top-left corner of the avatar.
     * @param inputListener The input listener used for keyboard input.
     * @param imageReader The image reader used for loading avatar images.
     */
    public Avatar(Vector2 topLeftCorner,
                  UserInputListener inputListener,
                  ImageReader imageReader) {
        super(topLeftCorner,
                AVATAR_DIMENSIONS,
                imageReader.readImage(IDLE_IMAGE_PATH, true));

        this.inputListener = inputListener;
        this.avatarAnimation = new AvatarAnimation(imageReader);
        this.currentState = AvatarState.IDLE;
        this.energy = MAX_ENERGY;
        this.energyCallback = null;
        this.spaceWasPressed = false;
        this.hasDoubleJumped = false;

        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        transform().setAccelerationY(GRAVITY);
        setTag(AVATAR_TAG);

        avatarAnimation.updateAnimation(this, currentState);
    }

    /**
     * Updates the avatar according to input, physics state and energy.
     *
     * @param deltaTime The time passed since the previous update.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        boolean isLeftPressed = inputListener.isKeyPressed(KeyEvent.VK_LEFT);
        boolean isRightPressed = inputListener.isKeyPressed(KeyEvent.VK_RIGHT);
        boolean isSpacePressed = inputListener.isKeyPressed(KeyEvent.VK_SPACE);
        boolean isNewSpacePress = isSpacePressed && !spaceWasPressed;

        handleJump(isNewSpacePress, isOnGround());

        boolean isAvatarOnGround = isOnGround();
        handleHorizontalMovement(isLeftPressed, isRightPressed, isAvatarOnGround);
        updateCurrentState(isLeftPressed, isRightPressed, isAvatarOnGround);
        updateEnergyByCurrentState();

        avatarAnimation.updateAnimation(this, currentState);
        spaceWasPressed = isSpacePressed;
    }

    /**
     * Returns the current energy of the avatar.
     *
     * @return The current avatar energy.
     */
    public int getEnergy() {
        return energy;
    }

    /**
     * Adds energy to the avatar.
     *
     * @param amount The amount of energy to add.
     */
    public void addEnergy(int amount) {
        increaseEnergy(amount);
    }

    /**
     * Sets the callback that is called when the avatar energy changes.
     *
     * @param energyCallback The callback to update when energy changes.
     */
    public void setEnergyCallback(Consumer<Integer> energyCallback) {
        this.energyCallback = energyCallback;
        notifyEnergyChanged();
    }

    /*
     * Notifies the energy display when the energy value changes.
     */
    private void notifyEnergyChanged() {
        if (energyCallback != null) {
            energyCallback.accept(energy);
        }
    }

    /*
     * Handles horizontal movement according to keyboard input.
     */
    private void handleHorizontalMovement(boolean isLeftPressed,
                                          boolean isRightPressed,
                                          boolean isAvatarOnGround) {
        float xVelocity = 0;

        if (isLeftPressed == isRightPressed) {
            transform().setVelocityX(0);
            return;
        }

        if (isAvatarOnGround && energy < RUN_ENERGY_COST) {
            transform().setVelocityX(0);
            return;
        }

        if (isLeftPressed) {
            xVelocity -= VELOCITY_X;
            avatarAnimation.setFacingLeft(this, true);
        }

        if (isRightPressed) {
            xVelocity += VELOCITY_X;
            avatarAnimation.setFacingLeft(this, false);
        }

        transform().setVelocityX(xVelocity);
    }

    /*
     * Handles regular jump and double jump.
     */
    private void handleJump(boolean isNewSpacePress, boolean isAvatarOnGround) {
        if (!isNewSpacePress) {
            return;
        }

        if (isAvatarOnGround && energy >= JUMP_ENERGY_COST) {
            decreaseEnergy(JUMP_ENERGY_COST);
            transform().setVelocityY(VELOCITY_Y);
            currentState = AvatarState.JUMP;
            return;
        }

        if (canDoubleJump(isAvatarOnGround)) {
            decreaseEnergy(DOUBLE_JUMP_ENERGY_COST);
            transform().setVelocityY(VELOCITY_Y);
            currentState = AvatarState.JUMP;
            hasDoubleJumped = true;
        }
    }

    /*
     * Checks whether the avatar can perform a double jump.
     */
    private boolean canDoubleJump(boolean isAvatarOnGround) {
        return !isAvatarOnGround &&
                isFalling() &&
                !hasDoubleJumped &&
                energy >= DOUBLE_JUMP_ENERGY_COST;
    }

    /*
     * Updates the current movement state of the avatar.
     */
    private void updateCurrentState(boolean isLeftPressed,
                                    boolean isRightPressed,
                                    boolean isAvatarOnGround) {
        if (!isAvatarOnGround) {
            currentState = AvatarState.JUMP;
            return;
        }

        hasDoubleJumped = false;

        boolean isExactlyOneSidePressed = isLeftPressed != isRightPressed;
        boolean canRun = energy >= RUN_ENERGY_COST;

        if (isExactlyOneSidePressed && canRun) {
            currentState = AvatarState.RUN;
            return;
        }

        currentState = AvatarState.IDLE;
    }

    /*
     * Updates the avatar energy according to the current state.
     */
    private void updateEnergyByCurrentState() {
        if (currentState == AvatarState.RUN) {
            decreaseEnergy(RUN_ENERGY_COST);
            return;
        }

        if (currentState == AvatarState.IDLE) {
            increaseEnergy(IDLE_ENERGY_GAIN);
        }
    }

    /*
     * Checks whether the avatar is currently on the ground.
     */
    private boolean isOnGround() {
        return getVelocity().y() == 0;
    }

    /*
     * Checks whether the avatar is falling.
     */
    private boolean isFalling() {
        return getVelocity().y() > 0;
    }

    /*
     * Increases the avatar energy.
     */
    private void increaseEnergy(int amount) {
        setEnergy(energy + amount);
    }

    /*
     * Decreases the avatar energy.
     */
    private void decreaseEnergy(int amount) {
        setEnergy(energy - amount);
    }

    /*
     * Sets the avatar energy inside the legal range.
     */
    private void setEnergy(int newEnergy) {
        int fixedEnergy = Math.max(MIN_ENERGY, Math.min(MAX_ENERGY, newEnergy));

        if (fixedEnergy != energy) {
            energy = fixedEnergy;
            notifyEnergyChanged();
        }
    }
}