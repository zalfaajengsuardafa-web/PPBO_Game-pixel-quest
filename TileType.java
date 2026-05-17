package game.world;

/**
 * Tile ID system:
 * Format: (terrain * 100) + edge_code
 * Terrain: 1=snow, 2=grass, 3=ash
 *
 * Edge codes:
 * 0  = no edges (solid fill)
 * 1  = t   (top)
 * 2  = b   (bottom)
 * 3  = l   (left)
 * 4  = r   (right)
 * 5  = tb  (top+bottom)
 * 6  = tl  (top+left)
 * 7  = tr  (top+right)
 * 8  = bl  (bottom+left)
 * 9  = br  (bottom+right)
 * 10 = tlb (top+left+bottom)
 * 11 = trb (top+right+bottom)
 * 12 = tlr (top+left+right)
 * 13 = blr (bottom+left+right)
 * 14 = a   (all edges)
 * 15 = lr  (left+right)
 * 16 = lb  (left+bottom) -- alias for bl
 * 17 = rb  (right+bottom) -- alias for br
 *
 * Examples:
 * 200 = grass, no edges
 * 201 = grass_t
 * 205 = grass_tb
 * 210 = grass_tlb
 * 300 = ash, no edges
 * 314 = ash_a
 */
public final class TileType {
    private TileType() {}

    // ── SNOW (1xx) ────────────────────────────────────────────────────────────
    public static final int SNOW        = 100;
    public static final int SNOW_T      = 101;
    public static final int SNOW_B      = 102;
    public static final int SNOW_L      = 103;
    public static final int SNOW_R      = 104;
    public static final int SNOW_TB     = 105;
    public static final int SNOW_TL     = 106;
    public static final int SNOW_TR     = 107;
    public static final int SNOW_BL     = 108;
    public static final int SNOW_BR     = 109;
    public static final int SNOW_TLB    = 110;
    public static final int SNOW_TRB    = 111;
    public static final int SNOW_TLR    = 112;
    public static final int SNOW_BLR    = 113;
    public static final int SNOW_A      = 114;
    public static final int SNOW_LR     = 115;

    // ── GRASS (2xx) ───────────────────────────────────────────────────────────
    public static final int GRASS       = 200;
    public static final int GRASS_T     = 201;
    public static final int GRASS_B     = 202;
    public static final int GRASS_L     = 203;
    public static final int GRASS_R     = 204;
    public static final int GRASS_TB    = 205;
    public static final int GRASS_TL    = 206;
    public static final int GRASS_TR    = 207;
    public static final int GRASS_BL    = 208;
    public static final int GRASS_BR    = 209;
    public static final int GRASS_TLB   = 210;
    public static final int GRASS_TRB   = 211;
    public static final int GRASS_TLR   = 212;
    public static final int GRASS_BLR   = 213;
    public static final int GRASS_A     = 214;
    public static final int GRASS_LR    = 215;

    // ── ASH (3xx) ─────────────────────────────────────────────────────────────
    public static final int ASH         = 300;
    public static final int ASH_T       = 301;
    public static final int ASH_B       = 302;
    public static final int ASH_L       = 303;
    public static final int ASH_R       = 304;
    public static final int ASH_TB      = 305;
    public static final int ASH_TL      = 306;
    public static final int ASH_TR      = 307;
    public static final int ASH_BL      = 308;
    public static final int ASH_BR      = 309;
    public static final int ASH_TLB     = 310;
    public static final int ASH_TRB     = 311;
    public static final int ASH_TLR     = 312;
    public static final int ASH_BLR     = 313;
    public static final int ASH_A       = 314;
    public static final int ASH_LR      = 315;

    // WATER
    public static final int WATER       = 316;

    // ── Helper: get filename from tile ID ─────────────────────────────────────
    public static String getFilename(int id) {
        if (id == 0) return null;

        int terrain = id / 100;
        int edge    = id % 100;

        String terrainName = switch (terrain) {
            case 1 -> "1";
            case 2 -> "2";
            case 3 -> "3";
            default -> null;
        };
        if (terrainName == null) return null;

        String edgeName = switch (edge) {
            case 0  -> "";
            case 1  -> "t";
            case 2  -> "b";
            case 3  -> "l";
            case 4  -> "r";
            case 5  -> "tb";
            case 6  -> "tl";
            case 7  -> "tr";
            case 8  -> "bl";
            case 9  -> "br";
            case 10 -> "tlb";
            case 11 -> "trb";
            case 12 -> "tlr";
            case 13 -> "blr";
            case 14 -> "a";
            case 15 -> "lr";
            default -> "";
        };

        return terrainName + (edgeName.isEmpty() ? "" : edgeName) + ".png";
    }

    public static boolean isSolid(int id) {
        return id != 0;
    }
}