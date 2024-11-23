package org.astro.core.items;

import org.astro.core.Astro;
import org.astro.core.Building;
import org.astro.core.Terrain;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.ItemEvents;

import java.io.Serializable;
import java.util.Objects;

public class BuildingHammer extends ItemEvents {
    @Override
    public void onInstantiation(Item i) {
        i.itemData = new Data();
    }

    @Override
    public void onUse(Item i) {
        Data d = (Data) i.itemData;

        if (d.tileCount > 0 && !Objects.equals(d.tileName, "")) {
            for (Terrain.Tile t : Terrain.tiles) {
                if (Objects.equals(t.name, d.tileName) && Building.placeTile((int) ((Astro.astro.camera.x + (float) Astro.app.getInput().getMouseX()) / 100), (int) ((Astro.astro.camera.y + (float) Astro.app.getInput().getMouseY()) / 100), t)) {
                    d.tileCount--;
                    if (d.tileCount == 0) d.tileName = "";
                }
            }
        }
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
