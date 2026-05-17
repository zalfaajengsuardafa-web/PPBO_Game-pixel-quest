package game.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class SpriteSheet {

    private BufferedImage sheet;

    public SpriteSheet(String path) {
        try {
            URL root = getClass().getResource("/sprites");
            System.out.println("Sprites folder URL: " + root);

            URL url = getClass().getResource(path);
            System.out.println("Looking for: " + path + " → " + url);

            if (url == null) {
                System.err.println("Not found on classpath: " + path);
                return;
            }
            sheet = ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getFrame(int col, int row, int w, int h) {
        return sheet.getSubimage(col * w, row * h, w, h);
    }
}