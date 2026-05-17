package game.battle;

import game.utils.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BattleScreen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int W = Constants.SCREEN_W;
    private static final int H = Constants.SCREEN_H;

    // ── Assets ────────────────────────────────────────────────────────────────
    private BufferedImage battleBg;
    private BufferedImage tile2t;
    private BufferedImage tile5;

    // Player sprites
    private BufferedImage[] playerAttackFrames;
    private BufferedImage   playerDefeated;
    private BufferedImage   playerIdle;

    // Enemy sprites
    private BufferedImage[] mushroomFrames;
    private BufferedImage[] mushroomHurtFrames;
    private BufferedImage[] beeFrames;
    private BufferedImage[] beeHurtFrames;

    // UI
    private BufferedImage heartFull;
    private BufferedImage heartHalf;
    private BufferedImage heartEmpty;

    // ── Animation ─────────────────────────────────────────────────────────────
    private float   animTimer       = 0f;
    private int     animFrame       = 0;
    private float   attackAnimTimer = 0f;
    private int     attackFrame     = 0;
    private boolean isAttacking     = false;
    private boolean isHurt          = false;
    private float   hurtTimer       = 0f;

    private static final float ANIM_SPEED    = 0.15f;
    private static final float ATTACK_SPEED  = 0.12f; // lebih lambat dari 0.08
    private static final float HURT_DURATION = 0.6f;
    private static final int   ATTACK_FRAMES =  3;
    private static final int   ATTACK_MOVE   = 18; // px per frame maju

    // ── Damage popup ──────────────────────────────────────────────────────────
    private int   popupDmgEnemy  = 0;
    private int   popupDmgPlayer = 0;
    private float popupTimer     = 0f;
    private static final float POPUP_DURATION = 1.2f;

    // ── Message ───────────────────────────────────────────────────────────────
    private String message = "";

    // ── Constructor ───────────────────────────────────────────────────────────
    public BattleScreen() {
        loadAssets();
    }

    // ── Asset loading ─────────────────────────────────────────────────────────
    private void loadAssets() {
        battleBg       = load("/sprites/ui/battle_bg.png");
        playerDefeated = load("/sprites/player/defeated.png");
        playerIdle     = load("/sprites/player/walk1.png");
        tile2t         = load("/sprites/tiles/2t.png");
        tile5          = load("/sprites/tiles/5.png");

        playerAttackFrames = new BufferedImage[3];
        for (int i = 1; i <= 3; i++)
            playerAttackFrames[i - 1] = load("/sprites/player/attack" + i + ".png");

        mushroomFrames = new BufferedImage[]{
                load("/sprites/mushroom/walk1.png"),
                load("/sprites/mushroom/walk2.png")
        };
        mushroomHurtFrames = new BufferedImage[]{
                load("/sprites/mushroom/hurt1.png"),
                load("/sprites/mushroom/hurt2.png")
        };
        beeFrames = new BufferedImage[]{
                load("/sprites/bee/fly1.png"),
                load("/sprites/bee/fly2.png")
        };
        beeHurtFrames = new BufferedImage[]{
                load("/sprites/bee/hurt1.png"),
                load("/sprites/bee/hurt2.png")
        };

        heartFull  = load("/sprites/ui/heart_full.png");
        heartHalf  = load("/sprites/ui/heart_half.png");
        heartEmpty = load("/sprites/ui/heart_empty.png");
    }

    private BufferedImage load(String path) {
        try {
            var stream = getClass().getResourceAsStream(path);
            if (stream == null) { System.err.println("Missing: " + path); return null; }
            return ImageIO.read(stream);
        } catch (IOException e) { return null; }
    }

    // ── Update ────────────────────────────────────────────────────────────────
    public void update(float dt, BattleManager bm) {
        message = bm.getLastMessage();

        // Idle animation
        animTimer += dt;
        if (animTimer >= ANIM_SPEED) {
            animTimer = 0;
            animFrame = 1 - animFrame;
        }

        // Attack animation
        if (isAttacking) {
            attackAnimTimer += dt;
            if (attackAnimTimer >= ATTACK_SPEED) {
                attackAnimTimer = 0;
                attackFrame++;
                if (attackFrame >= ATTACK_FRAMES) {
                    attackFrame = 0;
                    isAttacking = false;
                }
            }
        }

        // Hurt animation
        if (isHurt) {
            hurtTimer += dt;
            if (hurtTimer >= HURT_DURATION) {
                isHurt    = false;
                hurtTimer = 0;
            }
        }

        // Popup timer
        if (popupTimer > 0) popupTimer -= dt;

        // Auto enemy turn
        if (bm.getState() == BattleManager.BattleState.ENEMY_TURN) {
            bm.enemyTakeTurn();
            if (bm.getLastDmgToPlayer() > 0) {
                popupDmgPlayer = bm.getLastDmgToPlayer();
                popupTimer     = POPUP_DURATION;
            }
        }
    }

    // ── Trigger effects ───────────────────────────────────────────────────────
    public void triggerAttack() {
        isAttacking     = true;
        attackFrame     = 0;
        attackAnimTimer = 0;
    }

    public void triggerHurt() {
        isHurt    = true;
        hurtTimer = 0;
    }

    public void showDamagePopup(int dmgEnemy, int dmgPlayer) {
        popupDmgEnemy  = dmgEnemy;
        popupDmgPlayer = dmgPlayer;
        popupTimer     = POPUP_DURATION;
    }

    // ── Render ────────────────────────────────────────────────────────────────
    public void render(Graphics2D g, BattleManager bm) {
        // Background
        if (battleBg != null) {
            g.drawImage(battleBg, 0, 0, W, H, null);
        } else {
            g.setColor(new Color(20, 10, 30));
            g.fillRect(0, 0, W, H);
        }

        renderGround(g);
        renderEnemy(g, bm);
        renderPlayer(g, bm);
        renderPlayerHP(g, bm);
        renderEnemyHP(g, bm);
        renderActionButtons(g, bm);
        renderMessage(g);
        renderDamagePopup(g);
        renderScore(g, bm);
    }

    // ── Ground ────────────────────────────────────────────────────────────────
    private void renderGround(Graphics2D g) {
        int groundY  = H / 2;
        int tileSize = 32;

        for (int x = 0; x < W; x += tileSize) {
            if (tile2t != null)
                g.drawImage(tile2t, x, groundY, tileSize, tileSize, null);
            else {
                g.setColor(new Color(80, 160, 60));
                g.fillRect(x, groundY, tileSize, tileSize);
            }
        }

        for (int y = groundY + tileSize; y < H; y += tileSize) {
            for (int x = 0; x < W; x += tileSize) {
                if (tile5 != null)
                    g.drawImage(tile5, x, y, tileSize, tileSize, null);
                else {
                    g.setColor(new Color(139, 105, 20));
                    g.fillRect(x, y, tileSize, tileSize);
                }
            }
        }
    }

    // ── Enemy ─────────────────────────────────────────────────────────────────
    private void renderEnemy(Graphics2D g, BattleManager bm) {
        boolean isBee = bm.getEnemyType().equals("bee");
        BufferedImage[] frames = isHurt
                ? (isBee ? beeHurtFrames : mushroomHurtFrames)
                : (isBee ? beeFrames     : mushroomFrames);

        if (frames == null || frames[0] == null) {
            g.setColor(isBee ? Color.YELLOW : Color.RED);
            g.fillRect(W - 220, 80, 100, 100);
            return;
        }

        BufferedImage frame = frames[animFrame % frames.length];
        int scale = isBee ? 4 : 3;
        int sw = frame.getWidth()  * scale;
        int sh = frame.getHeight() * scale;
        int ex = W - 60 - sw;
        int ey = H / 2 - sh;

        // Flip enemy menghadap kiri
        g.drawImage(frame, ex + sw, ey, -sw, sh, null);
    }

    // ── Player ────────────────────────────────────────────────────────────────
    private void renderPlayer(Graphics2D g, BattleManager bm) {
        BufferedImage frame;

        if (bm.getState() == BattleManager.BattleState.PLAYER_LOSE) {
            frame = playerDefeated;
        } else if (isAttacking && playerAttackFrames != null) {
            frame = playerAttackFrames[Math.min(attackFrame, ATTACK_FRAMES - 1)];
        } else {
            frame = playerIdle;
        }

        if (frame == null) {
            g.setColor(new Color(80, 120, 200));
            g.fillRect(60, H / 2 - 80, 60, 80);
            return;
        }

        int sw = frame.getWidth()  * 3;
        int sh = frame.getHeight() * 3;
        int py = H / 2 - sh;

        // Maju ke depan tiap frame saat attack
        int px = 60;
        if (isAttacking) {
            px += attackFrame * ATTACK_MOVE;
        }

        g.drawImage(frame, px, py, sw, sh, null);
    }

    // ── Player HP ─────────────────────────────────────────────────────────────
    private void renderPlayerHP(Graphics2D g, BattleManager bm) {
        int hp        = bm.getPlayerHp();
        int maxHp     = bm.getPlayerMaxHp();
        int hpPerHeart = maxHp / 5;
        int heartSize = 32;
        int startX    = 10;
        int startY    = H - 50;

        for (int i = 0; i < 5; i++) {
            int heartHpMin = i * hpPerHeart;
            int heartHpMid = heartHpMin + hpPerHeart / 2;

            BufferedImage img;
            if (hp >= heartHpMin + hpPerHeart) img = heartFull;
            else if (hp >= heartHpMid)          img = heartHalf;
            else                                img = heartEmpty;

            if (img != null) {
                g.drawImage(img, startX + i * (heartSize + 4), startY, heartSize, heartSize, null);
            } else {
                g.setColor(hp >= heartHpMin + hpPerHeart ? Color.RED :
                        hp >= heartHpMid ? new Color(200, 80, 80) : Color.DARK_GRAY);
                g.fillRect(startX + i * (heartSize + 4), startY, heartSize, heartSize);
            }
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        g.drawString("HP: " + hp + "/" + maxHp, startX, startY - 5);
    }

    // ── Enemy HP ──────────────────────────────────────────────────────────────
    private void renderEnemyHP(Graphics2D g, BattleManager bm) {
        boolean isBee = bm.getEnemyType().equals("bee");
        int barW = isBee ? 200 : 150;
        int barH = isBee ? 20  : 14;
        int bx   = W - barW - 20;
        int by   = 20;

        g.setColor(new Color(60, 20, 20));
        g.fillRoundRect(bx, by, barW, barH, 8, 8);

        float ratio = (float) bm.getEnemyHp() / bm.getEnemyMaxHp();
        g.setColor(new Color(220, 50, 50));
        g.fillRoundRect(bx, by, (int)(barW * ratio), barH, 8, 8);

        g.setColor(Color.WHITE);
        g.drawRoundRect(bx, by, barW, barH, 8, 8);

        if (isBee) {
            if (heartFull != null)
                g.drawImage(heartFull, bx - 36, by - 4, 40, 40, null);
        } else {
            int hearts = 3;
            for (int i = 0; i < hearts; i++) {
                float threshold = (float)(i + 1) / hearts;
                BufferedImage img = ratio >= threshold ? heartFull : heartEmpty;
                if (img != null)
                    g.drawImage(img, bx - (hearts - i) * 22, by, 20, 20, null);
            }
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        g.drawString(bm.getEnemyHp() + "/" + bm.getEnemyMaxHp(), bx + 4, by + barH - 3);
    }

    // ── Action buttons ────────────────────────────────────────────────────────
    private void renderActionButtons(Graphics2D g, BattleManager bm) {
        if (bm.getState() != BattleManager.BattleState.PLAYER_TURN) return;

        String[] labels = {"⚔ ATTACK", "🛡 DEFEND", "✨ SPECIAL"};
        Color[]  colors = {
                new Color(180, 60, 60),
                new Color(60, 100, 180),
                new Color(160, 60, 180)
        };
        String[] keys = {"[Z]", "[X]", "[C]"};

        int btnW   = 120, btnH = 36;
        int totalW = labels.length * btnW + (labels.length - 1) * 10;
        int startX = (W - totalW) / 2;
        int by     = H - 90;

        for (int i = 0; i < labels.length; i++) {
            int     bx       = startX + i * (btnW + 10);
            boolean disabled = (i == 2 && bm.getSpecialCooldown() > 0);

            g.setColor(disabled ? new Color(80, 80, 80) : colors[i]);
            g.fillRoundRect(bx, by, btnW, btnH, 10, 10);

            g.setColor(Color.WHITE);
            g.drawRoundRect(bx, by, btnW, btnH, 10, 10);

            g.setColor(disabled ? Color.GRAY : Color.WHITE);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
            FontMetrics fm = g.getFontMetrics();
            String label = disabled ? "✨ (" + bm.getSpecialCooldown() + ")" : labels[i];
            g.drawString(label, bx + (btnW - fm.stringWidth(label)) / 2, by + 24);

            g.setColor(new Color(200, 200, 200));
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
            g.drawString(keys[i], bx + btnW - 28, by + btnH - 4);
        }
    }

    // ── Message ───────────────────────────────────────────────────────────────
    private void renderMessage(Graphics2D g) {
        int mx = 10, my = H - 130;
        int mw = W - 20, mh = 34;

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(mx, my, mw, mh, 8, 8);
        g.setColor(new Color(200, 200, 200));
        g.drawRoundRect(mx, my, mw, mh, 8, 8);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        g.drawString(message, mx + 10, my + 22);
    }

    // ── Damage popup ──────────────────────────────────────────────────────────
    private void renderDamagePopup(Graphics2D g) {
        if (popupTimer <= 0) return;

        float alpha = Math.min(1f, popupTimer / POPUP_DURATION);
        int   a     = (int)(alpha * 255);

        if (popupDmgEnemy > 0) {
            g.setColor(new Color(255, 80, 80, a));
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
            g.drawString("-" + popupDmgEnemy, W - 180, H / 2 - 20);
        }

        if (popupDmgPlayer > 0) {
            g.setColor(new Color(255, 160, 0, a));
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
            g.drawString("-" + popupDmgPlayer, 80, H / 2 - 20);
        }
    }

    // ── Score ─────────────────────────────────────────────────────────────────
    private void renderScore(Graphics2D g, BattleManager bm) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        g.drawString("SCORE: " + bm.getTotalScore(), W - 120, 16);
    }

    // ── End screen ────────────────────────────────────────────────────────────
    public void renderEndScreen(Graphics2D g, BattleManager bm) {
        boolean win = bm.getState() == BattleManager.BattleState.PLAYER_WIN;

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, W, H);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 36));
        g.setColor(win ? Color.YELLOW : Color.RED);
        String title = win ? "VICTORY!" : "DEFEATED...";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (W - fm.stringWidth(title)) / 2, H / 2 - 30);

        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        String sub = win
                ? "+" + bm.getScoreGained() + " pts! Press ENTER to continue"
                : "Press ENTER for Game Over";
        FontMetrics fm2 = g.getFontMetrics();
        g.drawString(sub, (W - fm2.stringWidth(sub)) / 2, H / 2 + 10);
    }

    // ── Button hit test ───────────────────────────────────────────────────────
    public int getButtonIndexAt(int mouseX, int mouseY) {
        int btnW   = 120, btnH = 36;
        int totalW = 3 * btnW + 2 * 10;
        int startX = (W - totalW) / 2;
        int by     = H - 90;

        for (int i = 0; i < 3; i++) {
            int bx = startX + i * (btnW + 10);
            if (mouseX >= bx && mouseX <= bx + btnW &&
                    mouseY >= by && mouseY <= by + btnH) return i;
        }
        return -1;
    }
}