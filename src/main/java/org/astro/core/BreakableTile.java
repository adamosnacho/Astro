package org.astro.core;

import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.ItemTemplate;
import org.newdawn.slick.Image;

import java.util.ArrayList;
import java.util.List;

public class BreakableTile extends Entity {
    public static List<BreakableTile> tiles = new ArrayList<>();
    public ItemTemplate drop;
    public Image sprite;

    public boolean breakable = true;

    public BreakableTile(float x, float y, ItemTemplate drop) {
        z = -2;
        this.x = (float) ((int) x / 100) * 100;
        this.y = (float) ((int) y / 100) * 100;
        this.width = Terrain.tileWidth;
        this.height = Terrain.tileHeight;
        tiles.add(this);
        this.drop = drop;
    }

    public Object onTileBreak() {return null;}

    public final void breakTile() {
        Object data = onTileBreak();
        Astro.astro.deSpawn(this);
        tiles.remove(this);
        Item di = new Item(drop, x + Utils.randomRange(0, width), y + Utils.randomRange(0, height));
        di.itemData = data;
    }
}
