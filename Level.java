package game.world;

import game.entities.Flag;
import game.entities.Player;
import game.entities.enemies.Bee;
import game.entities.enemies.Enemy;
import game.entities.enemies.Mushroom;
import game.utils.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Level {

    private final String[][] tiles;
    private final int        mapCols;
    private final int        mapRows;
    private final int        tileSize;

    private float spawnX;
    private float spawnY;

    private final Color  bgColor;
    private final String biome;

    private BufferedImage backgroundImage;

    private final List<Enemy>                enemies   = new ArrayList<>();
    private final Map<String, BufferedImage> tileCache = new HashMap<>();

    private Flag flag;

    private boolean isSolidId(String id) {
        if (id == null || id.equals("0") || id.equals("6") || id.equals("f")) return false;
        if (id.startsWith("4")) return false;
        return true;
    }

    private BufferedImage getTileImage(String id) {
        if (id == null || id.equals("0")) return null;
        return tileCache.computeIfAbsent(id, k -> {
            String path = "/sprites/tiles/" + k + ".png";
            try {
                var stream = getClass().getResourceAsStream(path);
                if (stream == null) { System.err.println("MISSING TILE: " + k); return null; }
                return ImageIO.read(stream);
            } catch (Exception e) {
                System.err.println("ERROR loading tile: " + k + " -> " + e.getMessage());
                return null;
            }
        });
    }

    public Level(String[][] tiles, float spawnX, float spawnY, String biome) {
        this.tiles    = tiles;
        this.mapRows  = tiles.length;
        this.mapCols  = tiles[0].length;
        this.tileSize = Constants.TILE_SIZE;
        this.spawnX   = spawnX;
        this.spawnY   = spawnY;
        this.biome    = biome;
        this.bgColor  = resolveBgColor(biome);
        this.backgroundImage = loadBackgroundImage();
    }

    public void addEnemy(Enemy e) { enemies.add(e); }
    public List<Enemy> getEnemies() { return enemies; }

    public void setFlag(Flag f) { this.flag = f; }
    public Flag getFlag() { return flag; }

    public List<Rectangle> getSolidTiles(Rectangle bounds) {
        List<Rectangle> result = new ArrayList<>();
        int col0 = Math.max(0, bounds.x / tileSize);
        int col1 = Math.min(mapCols - 1, (bounds.x + bounds.width) / tileSize);
        int row0 = Math.max(0, bounds.y / tileSize);
        int row1 = Math.min(mapRows - 1, (bounds.y + bounds.height) / tileSize);
        for (int r = row0; r <= row1; r++)
            for (int c = col0; c <= col1; c++)
                if (isSolidId(tiles[r][c]))
                    result.add(new Rectangle(c * tileSize, r * tileSize, tileSize, tileSize));
        return result;
    }

    public boolean isSolid(int worldX, int worldY) {
        int col = worldX / tileSize;
        int row = worldY / tileSize;
        if (col < 0 || col >= mapCols || row < 0 || row >= mapRows) return false;
        return isSolidId(tiles[row][col]);
    }

    public boolean update(float dt, Player player) {
        for (Enemy e : enemies) {
            if (!e.isActive()) continue;
            e.update(dt, this);
            e.checkPlayerContact(player);
        }
        if (flag != null) flag.update(dt, this);
        return player.getY() > getMapHeight();
    }

    public void render(Graphics2D g, int camX, int camY) {
        if (backgroundImage != null) {
            int bgX = (int)(-camX * 0.3f) % Constants.SCREEN_W;
            g.drawImage(backgroundImage, bgX - Constants.SCREEN_W, 0, Constants.SCREEN_W, Constants.SCREEN_H, null);
            g.drawImage(backgroundImage, bgX,                      0, Constants.SCREEN_W, Constants.SCREEN_H, null);
            g.drawImage(backgroundImage, bgX + Constants.SCREEN_W, 0, Constants.SCREEN_W, Constants.SCREEN_H, null);
        } else {
            g.setColor(bgColor);
            g.fillRect(0, 0, Constants.SCREEN_W, Constants.SCREEN_H);
        }
        renderTiles(g, camX, camY);
        renderTiles(g, camX, camY);
        if (flag != null) flag.render(g, camX, camY);
        for (Enemy e : enemies) {
            if (!e.isActive()) continue;
            e.render(g, camX, camY);
        }
    }

    public void renderForeground(Graphics2D g, int camX, int camY) {
        int col0 = Math.max(0, camX / tileSize);
        int col1 = Math.min(mapCols - 1, (camX + Constants.SCREEN_W) / tileSize + 1);
        int row0 = Math.max(0, camY / tileSize);
        int row1 = Math.min(mapRows - 1, (camY + Constants.SCREEN_H) / tileSize + 1);
        for (int r = row0; r <= row1; r++) {
            for (int c = col0; c <= col1; c++) {
                String id = tiles[r][c];
                if (!id.startsWith("4")) continue;
                int sx = c * tileSize - camX;
                int sy = r * tileSize - camY;
                BufferedImage img = getTileImage(id);
                if (img != null) g.drawImage(img, sx, sy, tileSize, tileSize, null);
            }
        }
    }

    private void renderTiles(Graphics2D g, int camX, int camY) {
        int col0 = Math.max(0, camX / tileSize);
        int col1 = Math.min(mapCols - 1, (camX + Constants.SCREEN_W) / tileSize + 1);
        int row0 = Math.max(0, camY / tileSize);
        int row1 = Math.min(mapRows - 1, (camY + Constants.SCREEN_H) / tileSize + 1);
        for (int r = row0; r <= row1; r++) {
            for (int c = col0; c <= col1; c++) {
                String id = tiles[r][c];
                if (id.equals("0") || id.equals("f") || id.startsWith("4")) continue;
                int sx = c * tileSize - camX;
                int sy = r * tileSize - camY;
                BufferedImage img = getTileImage(id);
                if (img != null) g.drawImage(img, sx, sy, tileSize, tileSize, null);
            }
        }
    }

    public float  getSpawnX()    { return spawnX; }
    public float  getSpawnY()    { return spawnY; }
    public int    getMapWidth()  { return mapCols * tileSize; }
    public int    getMapHeight() { return mapRows * tileSize; }
    public int    getMapCols()   { return mapCols; }
    public int    getMapRows()   { return mapRows; }
    public String getBiome()     { return biome; }

    private Color resolveBgColor(String biome) {
        return switch (biome.toLowerCase()) {
            case "forest" -> new Color(0x5c94fc);
            case "cave"   -> new Color(0x0a0a0f);
            case "desert" -> new Color(0xf0c87c);
            default       -> new Color(0x5c94fc);
        };
    }

    private BufferedImage loadBackgroundImage() {
        try {
            var stream = getClass().getResourceAsStream("/sprites/background/background.png");
            if (stream == null) return null;
            return ImageIO.read(stream);
        } catch (Exception e) { return null; }
    }

    public static Level createForestLevel() {
        String[][] map = {
                {"0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0"},
                {"0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0"},
                {"0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0"},
                {"0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0"},
                {"0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0"},
                {"0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","2tlb","2tb","2trb","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0"},
                {"0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","4b","0","0","0","0","0","2tlb","2tb","2trb","0","0","0","0","0","0","0","2tlb","2trb","0","0","0","0","0","0","0","0","0","0","0","2ltr","0","0","0","0","3tlb","3trb","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0"},
                {"0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","2tl","2t","2tr","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","2tl","2t","5","2tr","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0"},
                {"0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","2a","0","0","0","0","0","0","0","2tl","5","5","5","2t","2tr","0","0","0","0","0","0","0","0","0","2a","0","0","0","0","0","0","0","0","0","0","0","0","2tl","5","5","5","5","2tr","0","0","0","0","0","3ltr","0","0","0","0","0","0","0","4d","0","0","0","0","0","0","0"},
                {"0","0","0","0","4d","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","5lb","5","5","5","5","5r","4c","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","5lb","5","5","5","5b","5br","0","0","0","0","0","5lb","3trb","0","3a","0","0","0","3tl","3t","3tr","0","0","0","0","0","0"},
                {"0","0","0","0","1a","0","0","0","0","0","0","0","0","0","2ltr","0","0","0","0","0","0","0","0","0","0","0","5lb","5b","5b","5b","5b","2tb","2trb","0","0","0","0","0","0","0","0","0","0","0","0","4c","0","0","0","0","0","0","0","0","5lb","5b","5br","0","0","0","0","0","0","0","0","0","0","0","0","0","3tlb","5","5","5r","0","0","0","0","0","0"},
                {"0","0","0","0","0","0","0","0","0","0","0","0","0","2tlb","5b","2trb","0","0","0","0","2tlb","2tb","2t","2tr","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","2tl","2t","2tr","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","5lb","5b","5br","0","0","0","0","0","0"},
                {"0","0","0","0","0","0","0","0","0","0","1ltr","0","0","0","0","0","0","0","0","0","0","0","5lb","5br","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","2a","0","0","2tlb","2t","5","5","5","2tr","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","f","0","0"},
                {"0","0","0","0","0","0","0","0","0","1tl","5r","0","0","0","0","0","0","2tlb","2trb","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","5lb","5","5","5","5br","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","3tl","3t","3t","3t"},
                {"0","0","0","0","0","0","0","0","1tl","5","5r","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","2a","0","0","0","0","0","0","0","0","0","0","2ltr","0","0","0","0","0","0","5lb","5b","5br","0","0","0","0","2tl","2t","2tr","0","0","0","0","0","0","0","0","0","0","0","0","0","4d","0","0","0","0","0","0","3tl","3t","5","5","5","5"},
                {"1t","1t","1t","1t","1tr","0","0","1tlb","5b","5","5","1t","1tr","0","0","0","0","0","0","0","0","4b","0","0","0","0","0","0","0","0","0","0","0","0","0","0","2tl","5","2tr","0","0","0","0","0","0","0","0","0","0","0","0","5l","5","5r","0","0","0","0","0","0","3tlb","3tb","3trb","0","3tlb","3t","3t","3tr","0","0","0","3tl","3t","3t","5","5","5","5","5","5"},
                {"5","5","5","5","5r","0","0","0","0","5l","5","5","5r","0","0","0","2tl","2t","2t","2t","2t","2t","2t","2t","2tr","0","0","0","0","0","0","0","0","0","0","2tl","5","5","5","2t","2tr","0","0","0","0","0","0","0","0","0","0","5l","5","5r","0","0","0","0","3a","0","0","0","0","0","0","5l","5","5r","0","0","3tl","5","5","5","5","3b","3b","3b","5","5"},
                {"5","5","5","5","5","1tr","0","0","4d","5l","5","5","5r","0","0","2tl","5","5","5","5","5","5","5","5","5r","0","0","0","2tl","2t","2t","2t","2t","2t","2t","5","5","5","5","5","5r","0","0","0","0","0","0","0","0","0","0","5l","5","5r","0","3tlb","3trb","0","0","0","0","0","0","0","0","5l","5","5r","0","0","5l","5","5","5","3r","0","0","0","3l","5r"},
                {"5","5","5","5","5","5","1t","1t","1t","5","5","5","5r","0","0","5l","5","5","5","5","5","5","5","5","5r","0","0","0","5l","5","5","5","5","5","5","5","5","5","5","5","5","2tr","0","0","0","0","0","0","0","0","0","5l","5","5r","0","0","0","0","0","0","0","0","0","0","0","5l","5","5r","0","0","5l","5","5","5","5","3t","3t","3t","5","5"},
                {"5","5","5","5","5","5","5","5","5","5","5","5","5r","6","6","5l","5","5","5","5","5","5","5","5","5r","6","6","6","5l","5","5","5","5","5","5","5","5","5","5","5","5","5r","6","6","6","6","6","6","6","6","6","5l","5","5r","6","6","6","6","6","6","6","6","6","6","6","5l","5","5r","6","6","5l","5","5","5","5","5","5","5","5","5"}
        };

        Level lvl = new Level(map, 2 * 32f, 13 * 32f, "forest");
        lvl.addEnemy(new Mushroom(10 * 32f, 11 * 32f));
        lvl.addEnemy(new Mushroom(33 * 32f, 16 * 32f));
        lvl.addEnemy(new Mushroom(65 * 32f, 14 * 32f));
        lvl.addEnemy(new Bee(74 * 32f, 12 * 32f));
        lvl.setFlag(new Flag(78 * 32f, 14 * 32f));
        return lvl;
    }
}