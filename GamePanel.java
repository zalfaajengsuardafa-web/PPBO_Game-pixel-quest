package game;

import game.battle.BattleManager;
import game.battle.BattleScreen;
import game.entities.Flag;
import game.entities.Player;
import game.entities.enemies.Enemy;
import game.world.Level;
import game.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements Runnable {

    public static final int SCREEN_W = Constants.SCREEN_W;
    public static final int SCREEN_H = Constants.SCREEN_H;

    private static final int   TARGET_FPS      = Constants.TARGET_FPS;
    private static final long  NANO_PER_FRAME  = 1_000_000_000L / TARGET_FPS;
    private static final float RESPAWN_DELAY   = 1.2f;
    private static final float BATTLE_END_DELAY = 1.5f;

    private Thread           gameThread;
    private volatile boolean running = false;

    public enum GameState { PLAYING, DEAD, BATTLE, GAME_OVER, VICTORY }
    private GameState gameState = GameState.PLAYING;

    private Player       player;
    private Level        level;
    private InputHandler input;

    private BattleManager battleManager;
    private BattleScreen  battleScreen;
    private Enemy         currentEnemy;
    private float         battleEndTimer = 0f;

    private BufferedImage buffer;
    private Graphics2D    bufferG;

    private int  fpsCounter = 0;
    private int  fpsDisplay = 0;
    private long fpsTimer   = System.nanoTime();

    private int   camX = 0;
    private int   camY = 0;
    private float dt   = 1f / TARGET_FPS;
    private float deathTimer = 0f;

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_W, SCREEN_H));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);
        initObjects();
    }

    private void initObjects() {
        buffer  = new BufferedImage(SCREEN_W, SCREEN_H, BufferedImage.TYPE_INT_ARGB);
        bufferG = buffer.createGraphics();

        level  = Level.createForestLevel();
        player = new Player(level.getSpawnX(), level.getSpawnY());
        player.loadSprites();

        input = new InputHandler();
        addKeyListener(input);
        requestFocusInWindow();
        player.setInputHandler(input);

        battleManager = new BattleManager(BattleManager.PLAYER_MAX_HP, BattleManager.PLAYER_MAX_HP, 0, player);
        battleScreen  = new BattleScreen();
    }

    public void startLoop() {
        if (running) return;
        running    = true;
        gameThread = new Thread(this, "GameLoop");
        gameThread.start();
    }

    public void stopLoop() {
        running = false;
        try { gameThread.join(500); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long lag      = 0;

        while (running) {
            long now   = System.nanoTime();
            long delta = now - lastTime;
            lastTime   = now;
            lag       += delta;

            while (lag >= NANO_PER_FRAME) {
                update();
                lag -= NANO_PER_FRAME;
            }

            render();
            repaint();

            fpsCounter++;
            if (now - fpsTimer >= 1_000_000_000L) {
                fpsDisplay = fpsCounter;
                fpsCounter = 0;
                fpsTimer   = now;
            }

            long sleepNano = NANO_PER_FRAME - (System.nanoTime() - now);
            if (sleepNano > 0) {
                try { Thread.sleep(sleepNano / 1_000_000, (int)(sleepNano % 1_000_000)); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }

    private void update() {
        switch (gameState) {
            case PLAYING   -> updatePlaying();
            case DEAD      -> updateDead();
            case BATTLE    -> updateBattle();
            case GAME_OVER -> updateGameOver();
            case VICTORY   -> updateVictory();
        }
        input.clearPressed();
    }

    private void updatePlaying() {
        player.update(dt, level);

        if (level.update(dt, player)) {
            gameState = GameState.GAME_OVER;
            return;
        }

        updateCamera();

        Flag flag = level.getFlag();
        if (flag != null && flag.collidesWith(player)) {
            gameState = GameState.VICTORY;
            return;
        }

        for (Enemy e : level.getEnemies()) {
            if (!e.isActive()) continue;
            if (player.collidesWith(e)) {
                startBattle(e);
                return;
            }
        }

        if (player.getState() == Player.State.DEAD) {
            gameState  = GameState.DEAD;
            deathTimer = RESPAWN_DELAY;
        }
    }

    private void updateDead() {
        player.update(dt, level);
        deathTimer -= dt;
        if (deathTimer <= 0) {
            player.respawn(level.getSpawnX(), level.getSpawnY());
            gameState = GameState.PLAYING;
        }
    }

    private void updateBattle() {
        battleScreen.update(dt, battleManager);

        BattleManager.BattleState bs = battleManager.getState();

        if (bs == BattleManager.BattleState.PLAYER_TURN) {
            if (input.battleAttackPressed) {
                battleManager.playerAction(BattleManager.PlayerAction.ATTACK);
                battleScreen.triggerAttack();
                battleScreen.showDamagePopup(battleManager.getLastDmgToEnemy(), 0);
            } else if (input.battleDefendPressed) {
                battleManager.playerAction(BattleManager.PlayerAction.DEFEND);
            } else if (input.battleSpecialPressed) {
                battleManager.playerAction(BattleManager.PlayerAction.SPECIAL);
                battleScreen.triggerAttack();
                battleScreen.showDamagePopup(battleManager.getLastDmgToEnemy(), 0);
            }
        }

        if (bs == BattleManager.BattleState.ENEMY_TURN) {
            battleScreen.triggerHurt();
            battleScreen.showDamagePopup(0, battleManager.getLastDmgToPlayer());
        }

        if (bs == BattleManager.BattleState.PLAYER_WIN) {
            battleEndTimer += dt;
            if (input.battleConfirmPressed || battleEndTimer >= BATTLE_END_DELAY) {
                if (currentEnemy != null) currentEnemy.destroy();
                endBattle();
            }
        }

        if (bs == BattleManager.BattleState.PLAYER_LOSE) {
            battleEndTimer += dt;
            if (input.battleConfirmPressed || battleEndTimer >= BATTLE_END_DELAY) {
                gameState = GameState.GAME_OVER;
            }
        }
    }

    private void updateGameOver() {
        if (input.battleConfirmPressed) restartGame();
    }

    private void updateVictory() {
        if (input.battleConfirmPressed) restartGame();
    }

    private void startBattle(Enemy enemy) {
        currentEnemy          = enemy;
        currentEnemy.inBattle = true;
        battleEndTimer        = 0f;
        battleManager.startBattle(enemy);
        gameState = GameState.BATTLE;
    }

    private void endBattle() {
        if (currentEnemy != null) {
            currentEnemy.inBattle = false;
            currentEnemy.resetAnim();
        }
        battleManager.healPlayerFull();
        battleEndTimer = 0f;
        currentEnemy   = null;
        gameState      = GameState.PLAYING;
    }

    private void restartGame() {
        level  = Level.createForestLevel();
        player = new Player(level.getSpawnX(), level.getSpawnY());
        player.loadSprites();
        player.setInputHandler(input);
        battleManager = new BattleManager(BattleManager.PLAYER_MAX_HP, BattleManager.PLAYER_MAX_HP, 0, player);
        gameState = GameState.PLAYING;
        camX = 0;
        camY = 0;
    }

    private void updateCamera() {
        camX = (int)(player.getX() - SCREEN_W / 2f);
        camY = (int)(player.getY() - SCREEN_H / 2f);
        camX = Math.max(0, Math.min(camX, level.getMapWidth()  - SCREEN_W));
        camY = Math.max(0, Math.min(camY, level.getMapHeight() - SCREEN_H));
    }

    private void render() {
        switch (gameState) {
            case PLAYING, DEAD -> renderPlatformer();
            case BATTLE        -> renderBattle();
            case GAME_OVER     -> renderGameOver();
            case VICTORY       -> renderVictory();
        }
    }

    private void renderPlatformer() {
        bufferG.setColor(new Color(20, 12, 28));
        bufferG.fillRect(0, 0, SCREEN_W, SCREEN_H);
        level.render(bufferG, camX, camY);
        player.render(bufferG, camX, camY);
        level.renderForeground(bufferG, camX, camY);
        renderHUD(bufferG);

        if (gameState == GameState.DEAD) {
            bufferG.setColor(new Color(0, 0, 0, 120));
            bufferG.fillRect(0, 0, SCREEN_W, SCREEN_H);
            bufferG.setColor(Color.WHITE);
            bufferG.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
            String msg = "YOU DIED";
            FontMetrics fm = bufferG.getFontMetrics();
            bufferG.drawString(msg, (SCREEN_W - fm.stringWidth(msg)) / 2, SCREEN_H / 2);
        }
    }

    private void renderBattle() {
        battleScreen.render(bufferG, battleManager);

        BattleManager.BattleState bs = battleManager.getState();
        if (bs == BattleManager.BattleState.PLAYER_WIN ||
                bs == BattleManager.BattleState.PLAYER_LOSE) {
            battleScreen.renderEndScreen(bufferG, battleManager);
        }
    }

    private void renderGameOver() {
        bufferG.setColor(new Color(10, 0, 0));
        bufferG.fillRect(0, 0, SCREEN_W, SCREEN_H);

        bufferG.setFont(new Font(Font.MONOSPACED, Font.BOLD, 48));
        bufferG.setColor(Color.RED);
        String title = "GAME OVER";
        FontMetrics fm = bufferG.getFontMetrics();
        bufferG.drawString(title, (SCREEN_W - fm.stringWidth(title)) / 2, SCREEN_H / 2 - 40);

        bufferG.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        bufferG.setColor(Color.WHITE);
        String sub = "Score: " + battleManager.getTotalScore();
        FontMetrics fm2 = bufferG.getFontMetrics();
        bufferG.drawString(sub, (SCREEN_W - fm2.stringWidth(sub)) / 2, SCREEN_H / 2 + 10);

        bufferG.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        bufferG.setColor(Color.GRAY);
        String hint = "Press ENTER to restart";
        FontMetrics fm3 = bufferG.getFontMetrics();
        bufferG.drawString(hint, (SCREEN_W - fm3.stringWidth(hint)) / 2, SCREEN_H / 2 + 45);
    }

    private void renderVictory() {
        bufferG.setColor(new Color(0, 20, 0));
        bufferG.fillRect(0, 0, SCREEN_W, SCREEN_H);

        bufferG.setFont(new Font(Font.MONOSPACED, Font.BOLD, 48));
        bufferG.setColor(Color.GREEN);
        String title = "YOU WIN!";
        FontMetrics fm = bufferG.getFontMetrics();
        bufferG.drawString(title, (SCREEN_W - fm.stringWidth(title)) / 2, SCREEN_H / 2 - 40);

        bufferG.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        bufferG.setColor(Color.WHITE);
        String sub = "Score: " + battleManager.getTotalScore();
        FontMetrics fm2 = bufferG.getFontMetrics();
        bufferG.drawString(sub, (SCREEN_W - fm2.stringWidth(sub)) / 2, SCREEN_H / 2 + 10);

        bufferG.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        bufferG.setColor(Color.GRAY);
        String hint = "Press ENTER to restart";
        FontMetrics fm3 = bufferG.getFontMetrics();
        bufferG.drawString(hint, (SCREEN_W - fm3.stringWidth(hint)) / 2, SCREEN_H / 2 + 45);
    }

    private void renderHUD(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        g.drawString("FPS: " + fpsDisplay, SCREEN_W - 80, 20);
        g.drawString("HP: " + battleManager.getPlayerHp() + "/" + battleManager.getPlayerMaxHp(), 10, 20);
        g.drawString("SCORE: " + battleManager.getTotalScore(), 10, 38);
        g.drawString("LVL: " + player.getStats().getLevel(), 10, 56);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(buffer, 0, 0, SCREEN_W, SCREEN_H, null);
        g2.dispose();
    }

    public GameState getGameState()            { return gameState; }
    public void      setGameState(GameState s) { gameState = s; }
    public Player    getPlayer()               { return player; }
}