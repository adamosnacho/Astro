package org.astro.core.breakabletiles;

import org.astro.core.*;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.ItemTemplate;
import org.astro.core.itemsystem.Items;
import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DrillTile extends BreakableTile implements Save {
    public static Map<String, ItemTemplate> digTiles = new HashMap<>();
    private final static int digTime = ClassSettings.loadInt("drill tile/crafting time", 10000);
    private int time;
    public int wear = 100;

    public static void baseDigTiles() {
        digTiles.put("aluminum", Items.items.get("aluminum"));
        digTiles.put("lead", Items.items.get("lead"));
    }

    public DrillTile(int x, int y) {
        super(x, y, Items.items.get("drill"));
        try {
            sprite = new Image("art/png/drill.png", false, Image.FILTER_NEAREST).getScaledCopy(width, height);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        Astro.astro.spawn(this);
        Saving.save.add(this);
    }

    public DrillTile(Object o) {
        super(((DrillSave) o).x, ((DrillSave) o).y, Items.items.get("drill"));
        try {
            sprite = new Image("art/png/drill.png", false, Image.FILTER_NEAREST).getScaledCopy(width, height);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        Astro.astro.spawn(this);
        Saving.save.add(this);
        wear = ((DrillSave) o).wear;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(sprite, x, y);
    }

    @Override
    public void update() {
        time += Astro.delta;
        if (time >= digTime) {
            time = 0;
            new Item(digTiles.get(Terrain.getTile((int) x / 100, (int) y / 100).name), x + Utils.randomRange(0, 60), y + Utils.randomRange(0, 60));
            wear -= 10;
            if (wear <= 0) Astro.astro.deSpawn(this);
        }
    }

    @Override
    public void deSpawn() {
        Saving.save.remove(this);
    }

    @Override
    public Object onTileBreak() {
        return wear;
    }

    @Override
    public String getInfo() {
        return "Drill\nWear " + wear + "%";
    }

    @Override
    public Object save() {
        return new DrillSave(x, y, wear);
    }

    private record DrillSave(float x, float y, int wear) implements Serializable {}
}
