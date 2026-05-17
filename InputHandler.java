package game;

import java.awt.event.*;

public class InputHandler extends KeyAdapter {

    // ── Platformer ────────────────────────────────────────────────────────────
    public boolean left, right, jump;
    public boolean jumpPressed;

    // ── Battle ────────────────────────────────────────────────────────────────
    public boolean battleAttack;
    public boolean battleDefend;
    public boolean battleSpecial;
    public boolean battleConfirm;

    // Single-frame press flags
    public boolean battleAttackPressed;
    public boolean battleDefendPressed;
    public boolean battleSpecialPressed;
    public boolean battleConfirmPressed;

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            // Platformer
            case KeyEvent.VK_LEFT,  KeyEvent.VK_A -> left  = true;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> right = true;
            case KeyEvent.VK_SPACE, KeyEvent.VK_W,
                 KeyEvent.VK_UP -> {
                if (!jump) jumpPressed = true;
                jump = true;
            }
            // Battle
            case KeyEvent.VK_Z -> { if (!battleAttack)  battleAttackPressed  = true; battleAttack  = true; }
            case KeyEvent.VK_X -> { if (!battleDefend)  battleDefendPressed  = true; battleDefend  = true; }
            case KeyEvent.VK_C -> { if (!battleSpecial) battleSpecialPressed = true; battleSpecial = true; }
            case KeyEvent.VK_ENTER -> { battleConfirmPressed = true; battleConfirm = true; }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT,  KeyEvent.VK_A -> left  = false;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> right = false;
            case KeyEvent.VK_SPACE, KeyEvent.VK_W,
                 KeyEvent.VK_UP                   -> jump  = false;
            case KeyEvent.VK_Z      -> battleAttack  = false;
            case KeyEvent.VK_X      -> battleDefend  = false;
            case KeyEvent.VK_C      -> battleSpecial = false;
            case KeyEvent.VK_ENTER  -> battleConfirm = false;
        }
    }

    public void clearPressed() {
        jumpPressed          = false;
        battleAttackPressed  = false;
        battleDefendPressed  = false;
        battleSpecialPressed = false;
        battleConfirmPressed = false;
    }
}