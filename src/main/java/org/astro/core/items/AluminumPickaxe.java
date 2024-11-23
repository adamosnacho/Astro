package org.astro.core.items;

import org.astro.core.*;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.Items;
import org.astro.core.particlesystem.Particle;
import org.astro.core.particlesystem.ParticleGroup;
import org.newdawn.slick.Color;

import java.util.ArrayList;
import java.util.List;

public class AluminumPickaxe extends PickaxeTemplate {
    public AluminumPickaxe() {
        super("aluminum_pickaxe", "art/png/aluminumPickaxe.png");
    }

    @Override
    public void onPickaxeUsed(Item item, BreakableTile bt) {
        item.itemData = (int) item.itemData - ClassSettings.loadInt("aluminum pickaxe/wear per breakable tile", 2);
        if ((int) item.itemData <= 0) {
            PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
            PlayerInventory.hand.destroy();
            PlayerInventory.hand = null;
        }
        if (bt.sprite == null) return;
        List<Color> colors = new ArrayList<>();
        for (int x = 0; x < bt.sprite.getWidth() - 1; x++) {
            for (int y = 0; y < bt.sprite.getHeight() - 1; y++) {
                try {
                    colors.add(bt.sprite.getColor(x, y));
                } catch (ArrayIndexOutOfBoundsException e) {}
            }
        }
        int vel = 100;
        ParticleGroup pg = new ParticleGroup(Astro.app.getInput().getMouseX() + Astro.astro.camera.x, Astro.app.getInput().getMouseY() + Astro.astro.camera.y, true);
        pg.play(() -> {
            Particle p = new Particle(pg, 0, 0, Utils.randomRange(-vel, vel), Utils.randomRange(-vel, vel), colors.get(Utils.randomRange(0, colors.size() - 1)), 0.001f, 1);
            p.physicsProperties(0.3f, 0, 0);
            p.setSize(10, 10);
            return p;
        }, 50);
    }

    @Override
    public void onPickaxeUsedTile(Item item, Terrain.Tile t) {
        item.itemData = (int) item.itemData - ClassSettings.loadInt("aluminum pickaxe/wear per terrain tile", 1);
        if ((int) item.itemData <= 0) {
            PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
            PlayerInventory.hand.destroy();
            PlayerInventory.hand = null;
        }
        if (t.sprite == null) return;
        List<Color> colors = new ArrayList<>();
        for (int x = 0; x < t.sprite.getWidth() - 1; x++) {
            for (int y = 0; y < t.sprite.getHeight() - 1; y++) {
                try {
                    colors.add(t.sprite.getColor(x, y));
                } catch (ArrayIndexOutOfBoundsException e) {}
            }
        }
        int vel = 100;
        ParticleGroup pg = new ParticleGroup(Astro.app.getInput().getMouseX() + Astro.astro.camera.x, Astro.app.getInput().getMouseY() + Astro.astro.camera.y, true);
        pg.play(() -> {
            Particle p = new Particle(pg, 0, 0, Utils.randomRange(-vel, vel), Utils.randomRange(-vel, vel), colors.get(Utils.randomRange(0, colors.size() - 1)), 0.001f, 1);
            p.physicsProperties(0.3f, 0, 0);
            p.setSize(10, 10);
            return p;
        }, 50);

        new Item(Items.items.get("matter"), Astro.app.getInput().getMouseX() + Astro.astro.camera.x - 20, Astro.app.getInput().getMouseY() + Astro.astro.camera.y - 20);
    }

    @Override
    public void onCreation(Item i) {
        i.itemData = 100;
    }

    @Override
    public String pickaxeStatus(Item i) {
        return "Aluminum pickaxe\nWear " + i.itemData + "%";
    }
}
