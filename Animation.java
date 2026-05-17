package game.utils;

/**
 * Frame-based animation controller.
 * Each animation has a name, frame count, and frame duration.
 */
public class Animation {

    public enum State {
        IDLE, WALK, JUMP, FALL, MELEE, SHOOT, ROLL, HURT, DEAD
    }

    private State   currentState;
    private int     frame;
    private float   timer;
    private float   frameDuration;
    private int     frameCount;
    private boolean looping;
    private boolean finished;

    // Frame counts per state (matching Qrow's sprite sheet description)
    public static int getFrameCount(State s) {
        return switch (s) {
            case IDLE   -> 1;
            case WALK   -> 4;
            case JUMP   -> 2;
            case FALL   -> 2;
            case MELEE  -> 3;
            case SHOOT  -> 2;
            case ROLL   -> 4;
            case HURT   -> 2;
            case DEAD   -> 3;
        };
    }

    public static float getFrameDuration(State s) {
        return switch (s) {
            case WALK   -> 0.12f;
            case ROLL   -> 0.08f;
            case MELEE  -> 0.10f;
            case SHOOT  -> 0.12f;
            case HURT   -> 0.15f;
            case DEAD   -> 0.18f;
            default     -> 0.15f;
        };
    }

    public static boolean isLooping(State s) {
        return switch (s) {
            case IDLE, WALK, JUMP, FALL -> true;
            default -> false;
        };
    }

    public Animation(State initialState) {
        setState(initialState);
    }

    public void setState(State s) {
        if (s == currentState) return;
        currentState  = s;
        frame         = 0;
        timer         = 0;
        frameCount    = getFrameCount(s);
        frameDuration = getFrameDuration(s);
        looping       = isLooping(s);
        finished      = false;
    }

    public void update(float dt) {
        if (finished) return;
        timer += dt;
        if (timer >= frameDuration) {
            timer -= frameDuration;
            frame++;
            if (frame >= frameCount) {
                if (looping) frame = 0;
                else { frame = frameCount - 1; finished = true; }
            }
        }
    }

    public State getState()    { return currentState; }
    public int   getFrame()    { return frame; }
    public boolean isFinished(){ return finished; }
}