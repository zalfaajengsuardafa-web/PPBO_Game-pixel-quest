package game;

import game.utils.Constants;

import javax.swing.*;

/**
 * Entry point for PixelQuestP.
 * Creates the JFrame, attaches GamePanel, and kicks off the game loop.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("PixelQuestP — Qrow's Quest");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel panel = new GamePanel();
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Request focus so key events work
            panel.requestFocusInWindow();

            panel.startLoop();
        });
    }
}