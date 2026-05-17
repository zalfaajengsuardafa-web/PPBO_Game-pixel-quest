package game.entities.enemies;

import game.utils.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Mushroom extends Enemy {

    private static final int W = 32;
    private static final int H = 32;

    private BufferedImage[] walkFrames;

    public Mushroom(float x, float y) {
        super(x, y, W, H, "mushroom", Constants.MUSHROOM_SPEED, 0f); // semua 0
        loadSprites();
    }

    private void loadSprites() {
        try {
            walkFrames = new BufferedImage[]{
                    ImageIO.read(getClass().getResourceAsStream("/sprites/mushroom/walk1.png")),
                    ImageIO.read(getClass().getResourceAsStream("/sprites/mushroom/walk2.png"))
            };
        } catch (IOException e) {
            System.err.println("Failed to load mushroom sprites");
            e.printStackTrace();
        }
    }

    @Override
    public void render(Graphics2D g, int camX, int camY) {
        int dx = (int)(x - camX);
        int dy = (int)(y - camY);

        if (walkFrames != null && walkFrames[0] != null) {
            BufferedImage frame = walkFrames[animFrame % walkFrames.length];
            int sw = frame.getWidth()  * 3;
            int sh = frame.getHeight() * 3;
            if (!facingRight) {
                g.drawImage(frame, dx + sw, dy, -sw, sh, null);
            } else {
                g.drawImage(frame, dx, dy, sw, sh, null);
            }
        } else {
            // Fallback kotak merah kalau sprite gagal load
            g.setColor(new Color(180, 50, 50));
            g.fillRect(dx, dy, W, H);
        }
    }

    @Override protected void onHurt()  {}
    @Override protected void onDeath() { active = false; }
}