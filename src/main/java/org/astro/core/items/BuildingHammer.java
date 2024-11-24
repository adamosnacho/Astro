package org.astro.core.items;

import org.astro.core.*;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.ItemEvents;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

import java.io.Serializable;
import java.util.Objects;

public class BuildingHammer extends ItemEvents {
    private static final Sound buildSfx;

    static {
        try {
            buildSfx = new Sound("sfx/build.ogg");
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onInstantiation(Item i) {
        i.itemData = new Data();
    }

    @Override
    public void onUse(Item i) {
        // Cast itemData to Data
        Data d = (Data) i.itemData;

        // Check if the item has valid tileCount and tileName
        if (d.tileCount > 0 && !Objects.equals(d.tileName, "")) {
            // Iterate through all available tiles in the terrain
            for (Terrain.Tile t : Terrain.tiles) {
                // Find the tile with the matching name
                if (Objects.equals(t.name, d.tileName)) {
                    // Calculate the tile's position based on mouse and camera position
                    int tileX = (int) ((Astro.astro.camera.x + (float) Astro.app.getInput().getMouseX()) / 100);
                    int tileY = (int) ((Astro.astro.camera.y + (float) Astro.app.getInput().getMouseY()) / 100);

                    // Attempt to place the tile
                    if (Building.placeTile(tileX, tileY, t)) {
                        // Check collision with the player
                        if (isColliding(Astro.astro.player.x, Astro.astro.player.y, Astro.astro.player.width, Astro.astro.player.height, tileX, tileY)) {
                            Building.breakTile(tileX, tileY);
                            return;
                        }

                        // Check collision with enemies
                        for (Enemy enemy : Enemy.enemies) {
                            if (isColliding(enemy.x, enemy.y, enemy.width, enemy.height, tileX, tileY)) {
                                Building.breakTile(tileX, tileY);
                                return;
                            }
                        }

                        // Tile placed successfully, decrease the count
                        d.tileCount--;
                        if (d.tileCount == 0) {
                            d.tileName = "";
                        }

                        // Play build sound effect
                        buildSfx.play(0.7f + Utils.randomRange(0, 20) / 100f, 0.8f);
                    }
                }
            }
        }
    }

    // Helper method to check collision between an object and a tile
    private boolean isColliding(float objX, float objY, float objWidth, float objHeight, int tileX, int tileY) {
        // Convert tile position to world coordinates
        float tileWorldX = tileX * 100;
        float tileWorldY = tileY * 100;
        float tileSize = 100; // Assuming tile size is 100x100

        // Check for overlap
        return objX < tileWorldX + tileSize &&
                objX + objWidth > tileWorldX &&
                objY < tileWorldY + tileSize &&
                objY + objHeight > tileWorldY;
    }

    @Override
    public String getStatus(Item i) {
        Data d = (Data) i.itemData;
        if (Objects.equals(d.tileName, "")) return "Building Hammer";
        return "Building Hammer\n" + d.tileName + "\nAmount " + d.tileCount;
    }

    public static class Data implements Serializable {
        public String tileName = "";
        public int tileCount = 0;
    }
}
