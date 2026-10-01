package pepse.world.avatar;

/**
 * Represents the movement state of the avatar.
 *
 * @author Yonatan Algamil
 */
public enum AvatarState {
    /**
     * The avatar is standing on the ground.
     */
    IDLE,

    /**
     * The avatar is running left or right.
     */
    RUN,

    /**
     * The avatar is in the air.
     */
    JUMP
}