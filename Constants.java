package game.utils;

public final class Constants {
    private Constants() {}

    // Window
    public static final int TILE_SIZE  = 32;
    public static final int COLS       = 20;
    public static final int ROWS = 19;
    public static final int SCREEN_W   = TILE_SIZE * COLS; // 640
    public static final int SCREEN_H = TILE_SIZE * ROWS; // jadi 608
    public static final int TARGET_FPS = 60;

    // Physics
    public static final float GRAVITY        = 600f;  // was 900f
    public static final float MAX_FALL_SPEED = 400f;  // was 500f

    // Player
    public static final float PLAYER_SPEED      = 180f;
    public static final float PLAYER_JUMP_FORCE = -320f; // was -380f

    // Enemies
    public static final float MUSHROOM_SPEED = 60f;
    public static final float BUNNY_SPEED    = 90f;
    public static final float BEE_SPEED      = 100f;

    // Colors
    public static final int COLOR_BG       = 0xFF1a1a2e;
    public static final int COLOR_TILE_1   = 0xFF2d4a3e;
    public static final int COLOR_TILE_2   = 0xFF3d6b4f;
    public static final int COLOR_PLATFORM = 0xFF5c4a1e;
    public static final int COLOR_PLAYER   = 0xFF2c2c54;
    public static final int COLOR_ACCENT   = 0xFFe94560;
}