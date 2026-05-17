package game.entities;

import game.InputHandler;
import game.utils.Constants;
import game.world.Level;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.List;

public class Player extends LivingEntity implements Attackable {

    // ── Physics ───────────────────────────────────────────────────────────────
    private static final float SPEED      = Constants.PLAYER_SPEED;
    private static final float JUMP_FORCE = Constants.PLAYER_JUMP_FORCE;
    private static final float GRAVITY    = Constants.GRAVITY;
    private static final float MAX_FALL   = Constants.MAX_FALL_SPEED;
    private static final int   W          = 32, H = 32;

    private boolean onGround  = false;
    private int     jumpsLeft = 0;
    private static final int MAX_JUMPS = 2;
    public float getVelY() { return velY; }

    private CharacterStats stats = new CharacterStats();

    // ── State ─────────────────────────────────────────────────────────────────
    public enum State { IDLE, WALK, JUMP, FALL, DEAD }
    private State state = State.IDLE;

    // ── Input ─────────────────────────────────────────────────────────────────
    private InputHandler input;

    // ── Animation ─────────────────────────────────────────────────────────────
    private BufferedImage[] idleFrames;
    private BufferedImage[] walkFrames;
    private BufferedImage[] jumpFrames;
    private int   animFrame = 0;
    private float animTimer = 0;
    private static final float ANIM_SPEED = 0.15f;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Player(float x, float y) {
        super(x, y, W, H, "player", 1, 1.5f);
    }

    public void setInputHandler(InputHandler input) {
        this.input = input;
    }

    // ── Sprite loading ────────────────────────────────────────────────────────
    public void loadSprites() {
        try {
            idleFrames = new BufferedImage[]{
                    ImageIO.read(getClass().getResourceAsStream("/sprites/player/walk1.png"))
            };
            walkFrames = new BufferedImage[]{
                    ImageIO.read(getClass().getResourceAsStream("/sprites/player/walk1.png")),
                    ImageIO.read(getClass().getResourceAsStream("/sprites/player/walk2.png")),
                    ImageIO.read(getClass().getResourceAsStream("/sprites/player/walk3.png")),
                    ImageIO.read(getClass().getResourceAsStream("/sprites/player/walk4.png"))
            };
            jumpFrames = new BufferedImage[]{
                    ImageIO.read(getClass().getResourceAsStream("/sprites/player/jump1.png")),
                    ImageIO.read(getClass().getResourceAsStream("/sprites/player/jump2.png")),
                    ImageIO.read(getClass().getResourceAsStream("/sprites/player/jump3.png"))
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage[] currentFrames() {
        switch (state) {
            case WALK:            return walkFrames;
            case JUMP: case FALL: return jumpFrames;
            default:              return idleFrames;
        }
    }

    // ── Update ────────────────────────────────────────────────────────────────
    @Override
    public void update(float dt, Level level) {
        if (input == null || state == State.DEAD) return;

        updateInvincibility(dt);

        // ── Horizontal input ──────────────────────────────────────────────────
        velX = 0;
        if (input.left)  { velX = -SPEED; facingRight = false; }
        if (input.right) { velX =  SPEED; facingRight = true;  }

        // ── Reset jumps saat landing ──────────────────────────────────────────
        if (onGround) jumpsLeft = MAX_JUMPS;

        // ── Jump ─────────────────────────────────────────────────────────────
        if (input.jumpPressed && jumpsLeft > 0) {
            velY     = JUMP_FORCE;
            jumpsLeft--;
            onGround = false;
        }

        // ── Gravity — hanya saat di udara ────────────────────────────────────
        if (!onGround) {
            velY += GRAVITY * dt;
            if (velY > MAX_FALL) velY = MAX_FALL;
        }

        // ── Move X + collide ──────────────────────────────────────────────────
        x += velX * dt;
        resolveCollisionX(level);

        // ── Move Y + collide ──────────────────────────────────────────────────
        // Simpan status onGround sebelum di-reset
        boolean wasOnGround = onGround;
        onGround = false;

        // Hanya gerak Y kalau ada velocity atau sebelumnya melayang
        if (velY != 0 || !wasOnGround) {
            y += velY * dt;
            y = Math.round(y);
            resolveCollisionY(level);
        } else {
            // Tetap di tanah, pastikan collision tetap dicek
            resolveCollisionY(level);
        }

        // ── World bounds ──────────────────────────────────────────────────────
        if (x < 0) x = 0;
        if (x + W > level.getMapWidth()) x = level.getMapWidth() - W;

        // ── State machine ─────────────────────────────────────────────────────
        State newState;
        if (state == State.DEAD) {
            newState = State.DEAD;
        } else if (!onGround) {
            newState = velY < 0 ? State.JUMP : State.FALL;
        } else if (velX != 0) {
            newState = State.WALK;
        } else {
            newState = State.IDLE;
        }

        if (newState != state) {
            state     = newState;
            animFrame = 0;
            animTimer = 0;
        }

        // ── Animation tick ────────────────────────────────────────────────────
        animTimer += dt;
        if (animTimer >= ANIM_SPEED) {
            animTimer = 0;
            BufferedImage[] frames = currentFrames();
            if (frames.length > 1) {
                animFrame = (animFrame + 1) % frames.length;
            } else {
                animFrame = 0;
            }
        }
    }

    // ── Collision ─────────────────────────────────────────────────────────────
    private void resolveCollisionX(Level level) {
        Rectangle bounds = new Rectangle((int)x, (int)y + 2, W, H - 4);
        List<Rectangle> tiles = level.getSolidTiles(bounds);
        for (Rectangle tile : tiles) {
            if (!bounds.intersects(tile)) continue;
            if (velX > 0) x = tile.x - W;
            else if (velX < 0) x = tile.x + tile.width;
            velX = 0;
            bounds = new Rectangle((int)x, (int)y + 2, W, H - 4);
        }
    }

    private void resolveCollisionY(Level level) {
        // Cek lantai dengan ray pendek ke bawah
        if (velY >= 0) {
            // Cek 3 titik di bawah player
            int bottom = (int)(y + H);
            int left   = (int)(x + 4);
            int right  = (int)(x + W - 4);
            int mid    = (int)(x + W / 2);

            if (level.isSolid(left, bottom) ||
                    level.isSolid(right, bottom) ||
                    level.isSolid(mid, bottom)) {
                // Snap ke tile di bawah
                int tileRow = bottom / 32;
                y = tileRow * 32 - H;
                velY = 0;
                onGround = true;
                return;
            }
        }
        Rectangle bounds = new Rectangle((int)x + 2, (int)y, W - 4, H);
        List<Rectangle> tiles = level.getSolidTiles(bounds);
        for (Rectangle tile : tiles) {
            if (!bounds.intersects(tile)) continue;
            if (velY >= 0) {
                y = tile.y - H;
                onGround = true;
            } else {
                y = tile.y + tile.height + 1;
            }
            velY = 0;
            bounds = new Rectangle((int)x + 2, (int)y, W - 4, H);
        }
    }

    // ── LivingEntity callbacks ────────────────────────────────────────────────
    @Override
    protected void onHurt() {
        state = State.DEAD;
        velX  = 0;
        velY  = -200;
    }

    @Override
    protected void onDeath() {
        active = false;
    }

    // ── Respawn ───────────────────────────────────────────────────────────────
    public void respawn(float spawnX, float spawnY) {
        x               = spawnX;
        y               = spawnY;
        velX            = 0;
        velY            = 0;
        jumpsLeft       = 0;
        hp              = maxHp;
        active          = true;
        invincibleTimer = 0;
        state           = State.IDLE;
    }

    // ── Render ────────────────────────────────────────────────────────────────
    @Override
    public void render(Graphics2D g, int camX, int camY) {
        int dx = (int)(x - camX);
        int dy = (int)(y - camY);

        if (idleFrames != null && walkFrames != null && jumpFrames != null) {
            BufferedImage[] frames = currentFrames();
            if (frames == null || frames.length == 0) return;
            int safeFrame = animFrame % frames.length;
            BufferedImage frame = frames[safeFrame];
            int sw = frame.getWidth()  * 3;
            int sh = frame.getHeight() * 3;
            int offsetX = (W - sw) / 2;
            int offsetY = H - sh;

            if (!facingRight) {
                g.drawImage(frame, dx + offsetX + sw, dy + offsetY, -sw, sh, null);
            } else {
                g.drawImage(frame, dx + offsetX, dy + offsetY, sw, sh, null);
            }
        } else {
            g.setColor(new Color(Constants.COLOR_PLAYER, true));
            g.fillRect(dx, dy, W, H);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("state:" + state + " onG:" + onGround + " velY:" + (int)velY, dx, dy - 5);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 11));
            g.drawString("state:" + state + " onG:" + onGround + " velY:" + (int)velY, dx, dy - 5);
        }
    }

    @Override
    public int attack() {
        return stats.getAttackPower();
    }

    @Override
    public int attack(int bonusDamage) {
        return stats.getAttackPower() + bonusDamage;
    }

    @Override
    public int attack(String skillName) {
        return switch (skillName) {
            case "special" -> stats.getAttackPower() * 2;
            case "weak"    -> stats.getAttackPower() / 2;
            default        -> stats.getAttackPower();
        };
    }

    public boolean gainExp(int amount) {
        return stats.addExp(amount);
    }

    public CharacterStats getStats() { return stats; }

    // ── Getters ───────────────────────────────────────────────────────────────
    public State getState() { return state; }
    public boolean isOnGround() { return onGround; }
}
