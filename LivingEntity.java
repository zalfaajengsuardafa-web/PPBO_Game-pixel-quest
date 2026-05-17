package game.entities;

import java.awt.Graphics2D;
import java.awt.Color;

/**
 * Any entity that has health, can take damage and die.
 * Extends GameObject with HP management and hurt/flash state.
 */
public abstract class LivingEntity extends GameObject {

    protected int   maxHp;
    protected int   hp;
    protected float invincibleTimer; // seconds of invincibility after hit
    protected float invincibleTime;  // total invincibility window
    protected boolean facingRight;

    // Visual hurt-flash
    protected float  hurtFlashTimer;
    protected static final float HURT_FLASH_DURATION = 0.08f;

    public LivingEntity(float x, float y, int width, int height,
                        String tag, int maxHp, float invincibleTime) {
        super(x, y, width, height, tag);
        this.maxHp          = maxHp;
        this.hp             = maxHp;
        this.invincibleTime = invincibleTime;
        this.facingRight    = true;
    }

    /**
     * Deal damage to this entity.
     * @return true if the hit landed (entity was not invincible).
     */
    public boolean takeDamage(int amount) {
        if (invincibleTimer > 0 || !active) return false;
        hp -= amount;
        invincibleTimer  = invincibleTime;
        hurtFlashTimer   = HURT_FLASH_DURATION;
        onHurt();
        if (hp <= 0) {
            hp = 0;
            onDeath();
        }
        return true;
    }

    public void heal(int amount) {
        hp = Math.min(hp + amount, maxHp);
    }

    protected abstract void onHurt();
    protected abstract void onDeath();

    /** Draw a small HP bar above the entity. */
    protected void renderHealthBar(Graphics2D g, int camX, int camY) {
        int bx = (int)(x - camX);
        int by = (int)(y - camY) - 8;
        int bw = width;
        int bh = 4;
        g.setColor(new Color(0x333333));
        g.fillRect(bx, by, bw, bh);
        float ratio = (float) hp / maxHp;
        g.setColor(new Color(0xe94560));
        g.fillRect(bx, by, (int)(bw * ratio), bh);
    }

    protected void updateInvincibility(float dt) {
        if (invincibleTimer > 0) invincibleTimer -= dt;
        if (hurtFlashTimer  > 0) hurtFlashTimer  -= dt;
    }

    public boolean isInvincible() { return invincibleTimer > 0; }
    public int  getHp()    { return hp; }
    public int  getMaxHp() { return maxHp; }
    public boolean isDead(){ return hp <= 0; }
    public boolean isFacingRight() { return facingRight; }
}