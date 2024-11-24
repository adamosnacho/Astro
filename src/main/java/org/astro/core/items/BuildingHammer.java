package org.astro.core.items;

import org.astro.core.Astro;
import org.astro.core.Building;
import org.astro.core.Terrain;
import org.astro.core.Utils;
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
        Data d = (Data) i.itemData;

        if (d.tileCount > 0 && !Objects.equals(d.tileName, "")) {
            for (Terrain.Tile t : Terrain.tiles) {
                if (Objects.equals(t.name, d.tileName) && Building.placeTile((int) ((Astro.astro.camera.x + (float) Astro.app.getInput().getMouseX()) / 100), (int) ((Astro.astro.camera.y + (float) Astro.app.getInput().getMouseY()) / 100), t)) {
                    d.tileCount--;
                    if (d.tileCount == 0) d.tileName = "";
                    buildSfx.play(0.7f + Utils.randomRange(0, 20) / 100f, 0.8f);
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
