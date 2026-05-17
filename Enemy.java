package game.entities.enemies;

import game.entities.LivingEntity;
import game.entities.Player;
import game.utils.Constants;
import game.world.Level;

import java.awt.*;

public abstract class Enemy extends LivingEntity {

    protected float   speed;
    protected float   walkDir;
    protected boolean onGround;
    protected float   animTimer;
    protected int     animFrame;
    protected int     enemyAttack = 8;
    protected static final float ANIM_SPEED = 0.5f;

    protected float patrolOrigin;
    protected float patrolRange;

    public boolean inBattle = false;

    public Enemy(float x, float y, int width, int height,
                 String tag, float speed, float patrolRange) {
        super(x, y, width, height, tag, 1, 0f);
        this.speed        = speed;
        this.walkDir      = 1;
        this.patrolOrigin = x;
        this.patrolRange  = patrolRange;
    }

    @Override
    public void update(float dt, Level level) {
        if (!active || inBattle) return;
        updateAI(dt, level);
        applyGravity(dt);
        move(dt, level);
        updateAnimation(dt);
    }

    public void destroy() { active = false; }

    public int attack() { return enemyAttack; }

    protected void updateAI(float dt, Level level) {
        if (patrolRange > 0) {
            if (x >= patrolOrigin + patrolRange) walkDir = -1;
            if (x <= patrolOrigin - patrolRange) walkDir =  1;
            velX = walkDir * speed;
        } else {
            velX = 0;
        }
    }

    protected void applyGravity(float dt) {
        if (!onGround) {
            velY += Constants.GRAVITY * dt;
            if (velY > Constants.MAX_FALL_SPEED) velY = Constants.MAX_FALL_SPEED;
        }
    }

    protected void move(float dt, Level level) {
        x += velX * dt;
        resolveHorizontal(level);
        y += velY * dt;
        onGround = false;
        resolveVertical(level);
    }

    private void resolveHorizontal(Level level) {
        Rectangle bounds = getBounds();
        for (Rectangle tile : level.getSolidTiles(bounds)) {
            if (bounds.intersects(tile)) {
                if (velX > 0) { x = tile.x - width;     walkDir = -1; }
                else          { x = tile.x + tile.width; walkDir =  1; }
                velX        = 0;
                facingRight = walkDir > 0;
            }
        }
        if (onGround && velX != 0) {
            int checkX = (int)(x + (walkDir > 0 ? width + 2 : -2));
            int checkY = (int)(y + height + 2);
            if (!level.isSolid(checkX, checkY)) {
                walkDir     = -walkDir;
                facingRight = walkDir > 0;
            }
        }
    }

    private void resolveVertical(Level level) {
        Rectangle bounds = getBounds();
        for (Rectangle tile : level.getSolidTiles(bounds)) {
            if (bounds.intersects(tile)) {
                if (velY > 0) { y = tile.y - height; onGround = true; }
                else          { y = tile.y + tile.height; }
                velY = 0;
            }
        }
    }

    protected void updateAnimation(float dt) {
        animTimer += dt;
        if (animTimer >= ANIM_SPEED) {
            animTimer = 0;
            animFrame = 1 - animFrame;
        }
    }

    public void checkPlayerContact(Player player) {}

    @Override protected void onHurt()  {}
    @Override protected void onDeath() { active = false; }

    public void resetAnim() {
        animTimer = 0;
        animFrame = 0;
    }
}