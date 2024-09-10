package org.astro.core;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

public class Terrain implements Serializable {
    public final int tileSize = 100;
    public int[][] tiles;
    public int width, height;
    private transient BufferedImage tileset;
    private final double[] tileProbabilities = {0, 0.497, 0.497, 0.005, 0.001};

    public static String[] tileDrops = {null, null, null, "lead", "aluminum", null};

    public Terrain(int rows, int cols, GamePanel gp) {
        tiles = new int[rows][cols];
        width = cols;
        height = rows;
        try {
            tileset = ImageIO.read(getClass().getResourceAsStream("/art/png/tiles.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        generateTerrain(gp);
    }

    private void generateTerrain(GamePanel gp) {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int tile = randomPercent(tileProbabilities);
                setTile(row, col, tile);
            }
        }
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (randomRange(1, 100) == 1) spawnWall(row, col, randomRange(0, 3));
                if (randomRange(1, 600) == 1) gp.spawnEntity(new TreasureChest(gp, row * tileSize, col * tileSize), 5);
                if (randomRange(1, 600) == 1) gp.spawnEntity(new MetalRod(gp, row * tileSize, col * tileSize), 5);
            }
        }
    }

    private void spawnWall(int row, int col, int dir) {
        if (row < 0) row = 0;
        if (col < 0) col = 0;
        if (row > height - 1) row = height - 1;
        if (col > width - 1) col = width - 1;
        setTile(row, col, 5);
        if (randomRange(1, 10) == 1) return;
        switch (dir) {
            case 0:
                row--;
                break;
            case 1:
                col++;
                break;
            case 2:
                row++;
                break;
            case 3:
                col--;
                break;
        }
        dir += randomRange(-1, 1);
        if (dir == -1) dir = 3;
        if (dir == 4) dir = 0;
        spawnWall(row, col, dir);
    }

    private int randomPercent(double[] probabilities) {
        Random random = new Random();
        double rand = random.nextDouble();
        double cumulativeProbability = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulativeProbability += probabilities[i];
            if (rand <= cumulativeProbability) {
                return i;
            }
        }
        return 0;
    }

    public void draw(Graphics g, int cameraX, int cameraY, int screenWidth, int screenHeight) {
        int startRow = Math.max(cameraY / tileSize, 0);
        int endRow = Math.min((cameraY + screenHeight) / tileSize + 1, height);
        int startCol = Math.max(cameraX / tileSize, 0);
        int endCol = Math.min((cameraX + screenWidth) / tileSize + 1, width);

        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                if (getTile(row, col) == 0) continue;
                int y = (getTile(row, col) - 1) * tileSize;
                g.drawImage(tileset, col * tileSize, row * tileSize, (col + 1) * tileSize, (row + 1) * tileSize, 0, y, tileSize, y + tileSize, null);
            }
        }
    }


    public boolean isColliding(int x, int y, int width, int height) {
        // Check edges
        for (int i = 0; i < width; i++) {
            if (isTileSolid(x + i, y) || isTileSolid(x + i, y + height - 1)) {
                return true;
            }
        }
        for (int i = 0; i < height; i++) {
            if (isTileSolid(x, y + i) || isTileSolid(x + width - 1, y + i)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTileSolid(int x, int y) {
        int row = y / tileSize;
        int col = x / tileSize;
        if (row >= 0 && row < tiles.length && col >= 0 && col < tiles[0].length) {
            if (getTile(row, col) == 5) return true;
            if (getTile(row, col) == 7) return true;
            if (getTile(row, col) == 8) return true;
        }
        return false;
    }

    public int getTile(int row, int col) {
        if (row >= 0 && row < tiles.length && col >= 0 && col < tiles[0].length) {
            return tiles[row][col];
        } else {
            return 0; // Out of bounds
        }
    }

    public void setTile(int row, int col, int tile) {
        tiles[row][col] = tile;
    }

    public int randomRange(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }
    public Vector2 toCell(int x, int y) {
        return new Vector2(x / tileSize, y / tileSize);
    }
    public Vector2 toGlobal(int row, int col) {
        return new Vector2(row * tileSize, col * tileSize);
    }
}
