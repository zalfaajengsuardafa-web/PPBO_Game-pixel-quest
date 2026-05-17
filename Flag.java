package game.entities;

import game.world.Level;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Flag extends GameObject {

    private static final float ANIM_SPEED = 0.2f;
    private static final int   W = 32, H = 64;

    private BufferedImage[] frames;
    private float animTimer = 0f;
    private int   animFrame = 0;

    public Flag(float x, float y) {
        super(x, y, W, H, "flag");
        loadSprites();
    }

    private void loadSprites() {
        try {
            frames = new BufferedImage[]{
                    ImageIO.read(getClass().getResourceAsStream("/sprites/flag/flag1.png")),
                    ImageIO.read(getClass().getResourceAsStream("/sprites/flag/flag2.png"))
            };
        } catch (IOException e) {
            System.err.println("Failed to load flag sprites");
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)(y - 86), W, H);
    }

    @Override
    public void update(float dt, Level level) {
        animTimer += dt;
        if (animTimer >= ANIM_SPEED) {
            animTimer = 0;
            animFrame = 1 - animFrame;
        }
    }

    @Override
    public void render(Graphics2D g, int camX, int camY) {
        int dx = (int)(x - camX);
        int dy = (int)(y - camY);

        if (frames != null && frames[animFrame] != null) {
            BufferedImage frame = frames[animFrame];
            g.drawImage(frame, dx, dy - 86, 32, 64, null);
        } else {
            g.setColor(Color.YELLOW);
            g.fillRect(dx, dy, W, H);
        }
    }

    public boolean collidesWith(Player player) {
        return getBounds().intersects(player.getBounds());
    }
}