package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);

    /**
     * Draw a row of tiles to the board, anchored at a given position
     */
    public static void drawRow(TETile[][] tiles, Position p, TETile tile, int length) {
        for (int dx = 0; dx < length; dx++) {
            tiles[p.x + dx][p.y] = tile;
        }
    }

    /**
     * A helper method
     */
    public static void addHexagonHelper(TETile[][] tiles, Position p, TETile tile, int b, int t) {
        // Draw this row
        Position startOfRow = p.shift(b, 0);
        drawRow(tiles, startOfRow, tile, t);

        // Draw remaining rows recursively
        if (b > 0) {
            Position nextP = p.shift(0, -1);
            addHexagonHelper(tiles, nextP, tile, b - 1, t + 2);
        }

        // Draw this row again to be the reflection
        Position startOfReflectedRow = startOfRow.shift(0, - (2 * b +  1));
        drawRow(tiles, startOfReflectedRow, tile, t);
    }

    /**
     * Adds a hexagon to the world at position P of size 'SIZE'
     */
    public static void addHexagon(TETile[][] tiles, Position p, TETile tile, int size) {
        if (size < 2) {
            return;
        }

        addHexagonHelper(tiles, p, tile, size - 1, size);
    }

    /**
     * Adds a column of hexagons, each of whose biomes are chosen randomly
     * to the world at position P. Each of the hexagons are of size Size
     */
    public static void addHexColumn(TETile[][] tiles, Position p, int size, int num) {
        if (size < 1) {
            return;
        }

        // Draw this hexagon
        addHexagon(tiles, p, randomTile(), size);

        // Draw n - 1 hexagons below it
        if (num > 1) {
            Position bottomNeighbor = getBottomNeighbor(p, size);
            addHexColumn(tiles, bottomNeighbor, size, num - 1);
        }
    }

    /**
     * Fills the given 2D array of tiles with Nothing tiles.
     */
    public static void fillBoardWithNothing(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(5);
        switch (tileNum) {
            case 0:
                return Tileset.FLOWER;
            case 1:
                return Tileset.SAND;
            case 2:
                return Tileset.FLOOR;
            case 3:
                return Tileset.GRASS;
            case 4:
                return Tileset.WALL;
            default:
                return Tileset.NOTHING;
        }
    }

    /**
     * Get the position of bottom neighbor of a hexagon at position p
     */
    public static Position getBottomNeighbor(Position p, int n) {
        return p.shift(0, - 2 * n);
    }

    /**
     * Get the position of the top right neighbor of a hexagon at position p.
     * N is the size of the hexagon we are searching
     */
    public static Position getTopRightNeighbor(Position p, int n) {
        return p.shift(2 * n - 1, n);
    }

    /**
     * Get the position of the bottom right neighbor of a hexagon at position p.
     * N is the size of the hexagon we are searching
     */
    public static Position getBottomRightNeighbor(Position p, int n) {
        return p.shift(2 * n - 1, -n);
    }

    /**
     * Draw the hexagonal world
     */
    public static void drawWorld(TETile[][] tiles, Position p, int hexSize, int tessSize) {
        // Draw the first hexagon
        addHexColumn(tiles, p, hexSize, tessSize);

        // Expand up and to the right
        for (int i = 1; i < tessSize; i++) {
            p = getTopRightNeighbor(p, hexSize);
            addHexColumn(tiles, p, hexSize, tessSize + i);
        }

        // Expand down and to the the right
        for (int i = tessSize - 2; i >= 0; i--) {
            p = getBottomRightNeighbor(p, hexSize);
            addHexColumn(tiles, p, hexSize, tessSize + i);
        }
    }

    //private helper class to deal with positions
    private static class Position {
        int x;
        int y;
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position shift(int dx, int dy) {
            return new Position(x + dx, y + dy);
        }
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        fillBoardWithNothing(world);
        Position anchor = new Position(12, 34);
        drawWorld(world, anchor, 3, 3);

        ter.renderFrame(world);
    }
}
