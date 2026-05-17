package game.battle;

import game.entities.Player;
import game.entities.enemies.Enemy;
import game.entities.enemies.Bee;
import game.entities.Attackable;
import java.util.Random;

public class BattleManager {

    public static final int PLAYER_MAX_HP    = 100;
    public static final int PLAYER_ATTACK    = 12;
    public static final int PLAYER_SPECIAL   = 25;
    public static final int SPECIAL_COOLDOWN = 3;

    public enum BattleState { PLAYER_TURN, ENEMY_TURN, PLAYER_WIN, PLAYER_LOSE, ANIMATING }
    public enum PlayerAction { ATTACK, DEFEND, SPECIAL }
    private Attackable player;

    public BattleManager(int playerHp, int playerMaxHp, int totalScore, Attackable player) {
        this.playerHp    = playerHp;
        this.playerMaxHp = playerMaxHp;
        this.totalScore  = totalScore;
        this.player      = player;
    }

    private int     playerHp;
    private int     playerMaxHp;
    private boolean playerDefending = false;
    private int     specialCooldown = 0;

    private int    enemyHp;
    private int    enemyMaxHp;
    private int    enemyAttack;
    private String enemyType;
    private int    enemyTurnCount = 0;

    private BattleState state       = BattleState.PLAYER_TURN;
    private String      lastMessage = "What will you do?";
    private int         lastDamageToEnemy  = 0;
    private int         lastDamageToPlayer = 0;
    private int         scoreGained        = 0;
    private int         totalScore         = 0;

    private final Random rng = new Random();

    public BattleManager(int playerHp, int playerMaxHp, int totalScore, Player player) {
        this.playerHp    = playerHp;
        this.playerMaxHp = playerMaxHp;
        this.totalScore  = totalScore;
        this.player      = player;
    }

    public void startBattle(Enemy enemy) {
        playerDefending    = false;
        enemyTurnCount     = 0;
        lastDamageToEnemy  = 0;
        lastDamageToPlayer = 0;
        state       = BattleState.PLAYER_TURN;
        lastMessage = "A wild " + enemy.getTag() + " appeared!";

        if (enemy instanceof Bee) {
            enemyHp     = 90;
            enemyMaxHp  = 90;
            enemyAttack = 10;
            enemyType   = "bee";
        } else {
            enemyHp     = 80;
            enemyMaxHp  = 80;
            enemyAttack = 8;
            enemyType   = "mushroom";
        }

        if (specialCooldown > 0) specialCooldown--;
    }

    public void playerAction(PlayerAction action) {
        if (state != BattleState.PLAYER_TURN) return;

        playerDefending    = false;
        lastDamageToEnemy  = 0;
        lastDamageToPlayer = 0;

        switch (action) {
            case ATTACK -> {
                int dmg = player.attack() + rng.nextInt(7) - 3;
                dmg = Math.max(1, dmg);
                enemyHp -= dmg;
                lastDamageToEnemy = dmg;
                lastMessage = "You attacked for " + dmg + " damage!";
            }
            case DEFEND -> {
                playerDefending = true;
                lastMessage = "You brace for impact! (+3 reflect damage)";
            }
            case SPECIAL -> {
                if (specialCooldown > 0) {
                    lastMessage = "Special not ready! (" + specialCooldown + " turns left)";
                    return;
                }
                int dmg = player.attack("special") + rng.nextInt(5) - 2;
                dmg = Math.max(1, dmg);
                enemyHp -= dmg;
                lastDamageToEnemy = dmg;
                specialCooldown   = SPECIAL_COOLDOWN;
                lastMessage = "Special attack! " + dmg + " damage!";
            }
        }

        if (specialCooldown > 0 && action != PlayerAction.SPECIAL) specialCooldown--;

        if (enemyHp <= 0) {
            enemyHp     = 0;
            state       = BattleState.PLAYER_WIN;
            scoreGained = enemyType.equals("bee") ? 200 : 100;
            if (lastDamageToPlayer == 0) scoreGained += 50;
            totalScore += scoreGained;
            lastMessage = "Enemy defeated! +" + scoreGained + " pts!";

            if (player != null) {
                int exp = enemyType.equals("bee") ? 80 : 50;
                player.gainExp(exp);
            }
            return;
        }

        state = BattleState.ENEMY_TURN;
    }

    public void enemyTakeTurn() {
        if (state != BattleState.ENEMY_TURN) return;

        enemyTurnCount++;
        lastDamageToPlayer = 0;

        int dmg = enemyAttack + rng.nextInt(5) - 2;
        dmg = Math.max(1, dmg);

        if (enemyType.equals("bee") && enemyTurnCount % 3 == 0) {
            dmg = (int)(enemyAttack * 1.8f);
            lastMessage = "Bee uses HEAVY STING! " + dmg + " damage!";
        } else {
            lastMessage = enemyType + " attacks for " + dmg + " damage!";
        }

        if (playerDefending) {
            dmg = dmg / 2;
            playerHp  = Math.max(0, playerHp - dmg);
            enemyHp   = Math.max(0, enemyHp - 3);
            lastDamageToPlayer = dmg;
            lastMessage += " (Blocked! Reflected 3 dmg)";
        } else {
            playerHp  = Math.max(0, playerHp - dmg);
            lastDamageToPlayer = dmg;
        }

        playerDefending = false;

        if (playerHp <= 0) {
            playerHp    = 0;
            state       = BattleState.PLAYER_LOSE;
            lastMessage = "You were defeated...";
            return;
        }

        state = BattleState.PLAYER_TURN;
    }

    public void applyWaterDamage() {
        int dmg = (int)(playerMaxHp * 0.15f);
        playerHp = Math.max(0, playerHp - dmg);
    }

    public void healPlayerFull()             { playerHp = playerMaxHp; }
    public void setPlayerHp(int hp)          { this.playerHp = hp; }

    public BattleState getState()            { return state; }
    public String      getLastMessage()      { return lastMessage; }
    public int         getPlayerHp()         { return playerHp; }
    public int         getPlayerMaxHp()      { return playerMaxHp; }
    public int         getEnemyHp()          { return enemyHp; }
    public int         getEnemyMaxHp()       { return enemyMaxHp; }
    public String      getEnemyType()        { return enemyType; }
    public int         getSpecialCooldown()  { return specialCooldown; }
    public int         getLastDmgToEnemy()   { return lastDamageToEnemy; }
    public int         getLastDmgToPlayer()  { return lastDamageToPlayer; }
    public int         getScoreGained()      { return scoreGained; }
    public int         getTotalScore()       { return totalScore; }
    public boolean     isPlayerDefending()   { return playerDefending; }
}