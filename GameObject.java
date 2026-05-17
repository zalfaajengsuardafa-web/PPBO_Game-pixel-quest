package game.entities;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Root class for every object in the game world.
 * Provides position, velocity, collision box and basic lifecycle.
 */
public abstract class GameObject {

    protected float  x, y;
    protected float  velX, velY;
    protected int    width, height;
    protected boolean active;
    protected String tag;

    public GameObject(float x, float y, int width, int height, String tag) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
        this.tag    = tag;
        this.active = true;
    }

    /** Called every frame. dt = seconds since last frame. */
    public abstract void update(float dt, game.world.Level level);

    /** Draw relative to camera origin (camX, camY). */
    public abstract void render(Graphics2D g, int camX, int camY);

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public boolean collidesWith(GameObject other) {
        return active && other.active && getBounds().intersects(other.getBounds());
    }

    public void destroy() { active = false; }

    // ---- Getters ----
    public float   getX()      { return x; }
    public float   getY()      { return y; }
    public float   getVelX()   { return velX; }
    public float   getVelY()   { return velY; }
    public int     getWidth()  { return width; }
    public int     getHeight() { return height; }
    public boolean isActive()  { return active; }
    public String  getTag()    { return tag; }

    // ---- Setters ----
    public void setX(float x)      { this.x = x; }
    public void setY(float y)      { this.y = y; }
    public void setVelX(float vx)  { this.velX = vx; }
    public void setVelY(float vy)  { this.velY = vy; }
}