package org.astro.core.items;

import org.astro.core.*;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.ItemEvents;
import org.astro.core.itemsystem.ItemTemplate;

public class PickaxeTemplate extends ItemTemplate {
    public PickaxeTemplate(String name, String spritePath) {
        super(name, spritePath, null);
        itemEvents = new ItemEvents() {
            @Override
            public void onUse(Item i) {
                int px = (int) (Astro.app.getInput().getMouseX() + Astro.astro.camera.x);
                int py = (int) (Astro.app.getInput().getMouseY() + Astro.astro.camera.y);
                for (BreakableTile breakableTile : BreakableTile.tiles) {
                    if (px >= breakableTile.x && px <= (breakableTile.x + breakableTile.width) &&
                            py >= breakableTile.y && py <= (breakableTile.y + breakableTile.height) && breakableTile.breakable) {
                        breakableTile.breakTile();
                        onPickaxeUsed(i, breakableTile);
                        return;
                    }
                }
                for (Utils.Coords coord : Building.placedTiles.keySet()) {
                    if (coord.x() == px / 100 && coord.y() == py / 100) {
                        Terrain.Tile t = Building.placedTiles.get(coord);
                        if (Building.breakTile(coord.x(), coord.y())) {
                            onPickaxeUsedTile(i, t);
                            return;
                        }
                    }
                }
            }

            @Override
            public void onInstantiation(Item i) {
                onCreation(i);
            }

            @Override
            public String getStatus(Item i) {
                return pickaxeStatus(i);
            }

            @Override
            public String getInstructions(Item i) {
                return "[LMB] to use pickaxe";
            }
        };
    }

    public void onPickaxeUsed(Item i, BreakableTile bt) {}
    public void onPickaxeUsedTile(Item i, Terrain.Tile t) {}

    public void onCreation(Item i) {}

    public String pickaxeStatus(Item i) {return i.it.name + " pickaxe";}
}
