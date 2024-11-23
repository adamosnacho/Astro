package org.astro.core;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Terrain {
    public static final List<Tile> tiles = new ArrayList<>();
    public static String world = "";
    public static final Map<String, Chunk> loadedChunks = new HashMap<>(); // Cache for loaded chunks
    public static int chunkWidth = 15; // Number of tiles in one chunk width
    public static int chunkHeight = 15; // Number of tiles in one chunk height
    public static int tileWidth = 100;
    public static int tileHeight = 100;
    public static PerlinNoise noise;

    public static void init() {
        Path path = Paths.get("worldInfo.s");
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String name = parts[0];
                        String value = parts[1];
                        if (Objects.equals(name, "world")) {
                            Terrain.world = value;
                            Files.createDirectories(Path.of(world));
                        }
                        if (Objects.equals(name, "seed")) noise = new PerlinNoise(Integer.parseInt(value));
                    }
                }
                System.out.println("World info loaded from worldInfo.s");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("worldInfo.s must exist for terrain generation!");
        }
    }

    public static void registerTile(Image sprite, boolean solid, String name, Color color) {
        tiles.add(new Tile(sprite, solid, name, color));
    }

    public static void draw(Graphics g) {
        Camera camera = Astro.astro.camera;

        // Calculate the camera's position in chunk coordinates
        int camChunkX = (int) ((camera.x + camera.width / 2) / (chunkWidth * tileWidth));
        int camChunkY = (int) ((camera.y + camera.height / 2) / (chunkHeight * tileHeight));

        int renderDistance = 1; // Distance in chunks around the camera to load/draw

        // Iterate through the chunks within the render distance
        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dy = -renderDistance; dy <= renderDistance; dy++) {
                int chunkX = camChunkX + dx;
                int chunkY = camChunkY + dy;

                // Load the chunk if it's not already loaded
                String chunkKey = chunkX + "," + chunkY;
                if (!loadedChunks.containsKey(chunkKey)) {
                    Chunk chunk = new Chunk(chunkX, chunkY, chunkWidth, chunkHeight);
                    chunk.load();
                    loadedChunks.put(chunkKey, chunk);
                }

                // Draw the chunk
                drawChunk(g, loadedChunks.get(chunkKey));
            }
        }

        // Unload chunks that are outside the render distance
        unloadFarChunks(camChunkX, camChunkY, renderDistance);
    }

    private static void drawChunk(Graphics g, Chunk chunk) {
        if (chunk.loaded) {
            int[][] terrainData = chunk.getTerrain();
            for (int x = 0; x < chunkWidth; x++) {
                for (int y = 0; y < chunkHeight; y++) {
                    int tileIndex = terrainData[x][y];
                    float sx = (chunk.chunkX * chunkWidth + x) * tileWidth;
                    float sy = (chunk.chunkY * chunkHeight + y) * tileHeight;
                    if (sx + tileWidth >= Astro.astro.camera.x && sx - tileWidth <= Astro.astro.camera.x + Astro.astro.camera.width &&
                            sy + tileHeight >= Astro.astro.camera.y && sy - tileHeight <= Astro.astro.camera.y + Astro.astro.camera.height
                    ) g.drawImage(tiles.get(tileIndex).sprite.getScaledCopy(tileWidth, tileHeight), sx, sy);
                }
            }
        }
    }

    private static void unloadFarChunks(int camChunkX, int camChunkY, int renderDistance) {
        // Create a set of keys for chunks that should remain loaded
        HashMap<String, Boolean> loadedKeys = new HashMap<>();
        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dy = -renderDistance; dy <= renderDistance; dy++) {
                loadedKeys.put((camChunkX + dx) + "," + (camChunkY + dy), true);
            }
        }

        loadedChunks.keySet().removeIf(chunkKey -> {
            if (!loadedKeys.containsKey(chunkKey)) {
                loadedChunks.get(chunkKey).unload(); // Unload the chunk
                return true; // Remove from loadedChunks
            }
            return false; // Keep the chunk
        });
    }

    public static void registerTiles() throws SlickException {
        registerTile(new Image("art/png/tiles/mars.png", false, Image.FILTER_NEAREST), false, "mars", new Color(255, 140, 0));
        registerTile(new Image("art/png/tiles/mars-var.png", false, Image.FILTER_NEAREST), false, "mars", new Color(255, 140, 0));
        registerTile(new Image("art/png/tiles/lead.png", false, Image.FILTER_NEAREST), false, "lead", new Color(255, 216, 143));
        registerTile(new Image("art/png/tiles/aluminum.png", false, Image.FILTER_NEAREST), false, "aluminum", new Color(214, 163, 255));
        registerTile(new Image("art/png/tiles/wall.png", false, Image.FILTER_NEAREST), true, "wall", new Color(255, 72, 0));
        registerTile(new Image("art/png/tiles/stone.png", false, Image.FILTER_NEAREST), true, "stone", new Color(92, 92, 92));
    }

    public static Tile getTile(int x, int y) {
        if (loadedChunks.containsKey(x / chunkWidth + "," + y / chunkHeight)) {
            return tiles.get(loadedChunks.get(x / chunkWidth + "," + y / chunkHeight).getTerrain()[x % chunkWidth][y % chunkHeight]);
        }
        Chunk c = new Chunk(x / chunkWidth, y / chunkHeight, chunkWidth, chunkHeight);
        c.load();
        String chunkKey = x / chunkWidth + "," + y / chunkHeight;
        loadedChunks.put(chunkKey, c);

        return tiles.get(c.getTerrain()[x % chunkWidth][y % chunkHeight]);
    }

    public static void setTile(int x, int y, Tile tile) {
        int index = 0;
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i) == tile) index = i;
        }

        if (loadedChunks.containsKey(x / chunkWidth + "," + y / chunkHeight)) {
            loadedChunks.get(x / chunkWidth + "," + y / chunkHeight).getTerrain()[x % chunkWidth][y % chunkHeight] = index;
            return;
        }
        Chunk c = new Chunk(x / chunkWidth, y / chunkHeight, chunkWidth, chunkHeight);
        c.load();
        String chunkKey = x / chunkWidth + "," + y / chunkHeight;
        loadedChunks.put(chunkKey, c);
        c.getTerrain()[x % chunkWidth][y % chunkHeight] = index;
    }

    public static class Tile {
        public final Image sprite;
        public final boolean solid;
        public final String name;
        public final Color color;

        private Tile(Image sprite, boolean solid, String name, Color color) {
            this.sprite = sprite;
            this.solid = solid;
            this.name = name;
            this.color = color;
        }
    }
}
