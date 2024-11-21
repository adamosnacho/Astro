package org.astro.core;

import org.astro.core.enemies.Alien;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.Items;

import java.io.*;
import java.util.Random;

import static org.astro.core.Terrain.*;

public class Chunk {
    private int[][] terrain;
    public boolean loaded;
    public final int chunkX;
    public final int chunkY;
    public final int width;
    public final int height;
    public final float smoothness = 6f;

    public Chunk(int chunkX, int chunkY, int width, int height) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.width = width;
        this.height = height;
        this.loaded = false;
    }

    public void load() {
        if (!loaded) {
            terrain = loadTerrainFromFile();
            loaded = true;
        }
    }

    public void unload() {
        if (loaded) {
            saveTerrainToFile();
            terrain = null;
            loaded = false;
        }
    }

    public int[][] getTerrain() {
        return terrain;
    }

    private void saveTerrainToFile() {
        try (FileOutputStream fos = new FileOutputStream( world + "/" + chunkX + "." + chunkY + ".chunk")) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(terrain);
        } catch (IOException e) {
            System.err.println("Error saving chunk " + chunkX + "." + chunkY + "\n" + e.getMessage());
        }
    }

    private int[][] loadTerrainFromFile() {
        File file = new File(world + "/" + chunkX + "." + chunkY + ".chunk");

        // Check if the file exists
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                return (int[][]) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading chunk " + chunkX + "." + chunkY + "\n" + e.getMessage());
            }
        }

        // Generate new terrain if the file does not exist
        int[][] newTerrain = new int[width][height];
        PerlinNoise n = Terrain.noise;

        // Calculate the scale to convert to a range of 0 to 1
        float scaleX = 1.0f / Terrain.chunkWidth; // assuming Terrain.chunkWidth is the width of each chunk
        float scaleY = 1.0f / Terrain.chunkHeight; // assuming Terrain.chunkHeight is the height of each chunk

        // Get the size of the tiles
        int tileSize = Terrain.tiles.size();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Normalize the coordinates
                float noiseX = (chunkX * Terrain.chunkWidth + x) * scaleX;
                float noiseY = (chunkY * Terrain.chunkHeight + y) * scaleY;

                // Calculate noise value
                int noiseValue = (int) (n.smoothNoise(noiseX, noiseY, 0) * tileSize * smoothness);

                // Clamp the noise value between 0 and tileSize
                newTerrain[x][y] = Math.max(0, Math.min(tileSize - 1, noiseValue));
            }
        }
        if (Utils.randomRange(0, 4) == 0) new Item(Items.items.get("rock"), (chunkX * Terrain.chunkWidth + Utils.randomRange(0, chunkWidth)) * tileWidth, (chunkY * chunkHeight + Utils.randomRange(0, chunkHeight)) * tileHeight);
        if (Utils.randomRange(0, 4) == 0) new Item(Items.items.get("stick"), (chunkX * Terrain.chunkWidth + Utils.randomRange(0, chunkWidth)) * tileWidth, (chunkY * chunkHeight + Utils.randomRange(0, chunkHeight)) * tileHeight);
        if (Utils.randomRange(0, 3) == 0) new Alien((chunkX * Terrain.chunkWidth + Utils.randomRange(0, chunkWidth)) * tileWidth, (chunkY * chunkHeight + Utils.randomRange(0, chunkHeight)) * tileHeight);
        return newTerrain;
    }
}
