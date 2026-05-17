package game.entities.enemies;

import game.utils.Constants;
import game.world.Level;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Bee extends Enemy {

    private static final int W = 48;
    private static final int H = 32;

    private BufferedImage[] flyFrames;

    // Hover/floating effect
    private float hoverTimer = 0f;
    private float hoverOffset = 0f;
    private static final float HOVER_SPEED = 2.5f;
    private static final float HOVER_AMP   = 20f; // pixel naik turun

    private float spawnY; // posisi Y awal untuk hover reference

    public Bee(float x, float y) {
        super(x, y, W, H, "bee", Constants.BEE_SPEED, 1.5f * 32f);
        this.spawnY = y;
        loadSprites();
    }

    private void loadSprites() {
        try {
            flyFrames = new BufferedImage[]{
                    ImageIO.read(getClass().getResourceAsStream("/sprites/bee/fly1.png")),
                    ImageIO.read(getClass().getResourceAsStream("/sprites/bee/fly2.png"))
            };
        } catch (IOException e) {
            System.err.println("Failed to load bee sprites");
            e.printStackTrace();
        }
    }

    @Override
    public void update(float dt, Level level) {
        if (!active) return;

        // Patrol horizontal
        updateAI(dt, level);

        // Hover naik turun — bee terbang jadi tidak pakai gravity
        hoverTimer += dt * HOVER_SPEED;
        hoverOffset = (float) Math.sin(hoverTimer) * HOVER_AMP;
        y = spawnY + hoverOffset;

        // Move X saja, tidak pakai gravity/vertical collision
        x += velX * dt;
        resolveHorizontalOnly(level);

        updateAnimation(dt);
    }

    private void resolveHorizontalOnly(Level level) {
        Rectangle bounds = getBounds();
        for (Rectangle tile : level.getSolidTiles(bounds)) {
            if (bounds.intersects(tile)) {
                if (velX > 0) { x = tile.x - W; walkDir = -1; }
                else          { x = tile.x + tile.width; walkDir = 1; }
                velX        = 0;
                facingRight = walkDir > 0;
            }
        }
    }

    @Override
    public void render(Graphics2D g, int camX, int camY) {
        int dx = (int)(x - camX);
        int dy = (int)(y - camY);

        if (flyFrames != null && flyFrames[0] != null) {
            BufferedImage frame = flyFrames[animFrame % flyFrames.length];
            int sw = frame.getWidth()  * 4; // 4x scale
            int sh = frame.getHeight() * 4;
            if (!facingRight) {
                g.drawImage(frame, dx + sw, dy, -sw, sh, null);
            } else {
                g.drawImage(frame, dx, dy, sw, sh, null);
            }
        } else {
            g.setColor(new Color(220, 180, 0));
            g.fillRect(dx, dy, W, H);
        }
    }

    @Override protected void onHurt()  {}
    @Override protected void onDeath() { active = false; }
    @Override
    public int attack() {
        return 10;
    }
}