package org.astro.core.itemsystem;

import org.astro.core.Astro;
import org.astro.core.PlayerInventory;
import org.astro.core.Terrain;
import org.astro.core.breakabletiles.*;
import org.astro.core.items.*;
import org.astro.core.items.LeadPickaxe;

import java.util.HashMap;
import java.util.Map;

public class Items {
    public static Map<String, ItemTemplate> items = new HashMap<>();

    public static void registerItem(ItemTemplate it) {
        items.put(it.name, it);
    }

    public static void baseItems() {
        registerItem(new ItemTemplate("lead", "art/png/lead.png", new ItemEvents()));
        registerItem(new ItemTemplate("aluminum", "art/png/aluminum.png", new ItemEvents()));
        registerItem(new ItemTemplate("blueprint", "art/png/blueprint.png", new ItemEvents()));
        registerItem(new ItemTemplate("rope", "art/png/rope.png", new ItemEvents()));
        registerItem(new ItemTemplate("plank", "art/png/plank.png", new ItemEvents()));
        registerItem(new Rock());
        registerItem(new ItemTemplate("ash", "art/png/ash.png", new ItemEvents() {
            @Override
            public void onUse(Item i) {
                int px = (int) (Astro.app.getInput().getMouseX() + Astro.astro.camera.x);
                int py = (int) (Astro.app.getInput().getMouseY() + Astro.astro.camera.y);
                new AshTile(px, py);
                PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
                PlayerInventory.hand = null;
            }
        }));
        registerItem(new ItemTemplate("drill", "art/png/drill.png", new ItemEvents() {
            @Override
            public void onUse(Item i) {
                int px = (int) (Astro.app.getInput().getMouseX() + Astro.astro.camera.x);
                int py = (int) (Astro.app.getInput().getMouseY() + Astro.astro.camera.y);
                if (!DrillTile.digTiles.containsKey(Terrain.getTile(px / Terrain.tileWidth, py / Terrain.tileHeight).name)) return;

                new DrillTile(px, py).wear = (int) i.itemData;
                PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
                PlayerInventory.hand = null;
            }

            @Override
            public void onInstantiation(Item i) {
                i.itemData = 100;
            }

            @Override
            public String getStatus(Item i) {
                return "Drill\nWear " + i.itemData + "%";
            }
        }));
        registerItem(new ItemTemplate("o2_generator", "art/png/o2GeneratorIcon.png", new ItemEvents() {
            @Override
            public void onUse(Item i) {
                int px = (int) (Astro.app.getInput().getMouseX() + Astro.astro.camera.x);
                int py = (int) (Astro.app.getInput().getMouseY() + Astro.astro.camera.y);

                new O2GeneratorTile(px, py).wear = (float) i.itemData;
                PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
                PlayerInventory.hand = null;
            }

            @Override
            public void onInstantiation(Item i) {
                i.itemData = 100f;
            }

            @Override
            public String getStatus(Item i) {
                return "O2 Generator\nWear " + i.itemData + "%";
            }
        }));
        registerItem(new LeadPickaxe());
        registerItem(new Bag());
        registerItem(new ItemTemplate("chest", "art/png/chest.png", new ItemEvents() {
            @Override
            public void onUse(Item i) {
                int px = (int) (Astro.app.getInput().getMouseX() + Astro.astro.camera.x);
                int py = (int) (Astro.app.getInput().getMouseY() + Astro.astro.camera.y);
                new ChestTile(px, py);
                PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
                PlayerInventory.hand = null;
            }
        }));
        registerItem(new ItemTemplate("crafting_pot", "art/png/craftingPot.png", new ItemEvents() {
            @Override
            public void onUse(Item i) {
                int px = (int) (Astro.app.getInput().getMouseX() + Astro.astro.camera.x);
                int py = (int) (Astro.app.getInput().getMouseY() + Astro.astro.camera.y);
                new CraftingPotTile(px, py);
                PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
                PlayerInventory.hand = null;
            }
        }));
        registerItem(new ItemTemplate("wood_working_table", "art/png/woodWorkingTableIcon.png", new ItemEvents() {
            @Override
            public void onUse(Item i) {
                int px = (int) (Astro.app.getInput().getMouseX() + Astro.astro.camera.x);
                int py = (int) (Astro.app.getInput().getMouseY() + Astro.astro.camera.y);
                new WoodWorkingTableTile(px, py);
                PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
                PlayerInventory.hand = null;
            }
        }));
        registerItem(new ItemTemplate("o2_pole", "art/png/pole.png", new ItemEvents() {
            @Override
            public void onUse(Item i) {
                int px = (int) (Astro.app.getInput().getMouseX() + Astro.astro.camera.x);
                int py = (int) (Astro.app.getInput().getMouseY() + Astro.astro.camera.y);
                new O2Pole(px, py);
                PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
                PlayerInventory.hand = null;
            }
        }));
        registerItem(new ItemTemplate("spoon", "art/png/spoon.png", new ItemEvents() {
            @Override
            public void onInstantiation(Item i) {
                i.itemData = 100;
            }

            @Override
            public String getStatus(Item i) {
                return "Spoon\nWear " + i.itemData + "%";
            }

            @Override
            public String getInstructions(Item i) {
                return "[RMB] on Crafting Pot to mix\n[RMB] + [SHIFT] on Crafting pot\nto extract items";
            }
        }));
        ItemTemplate hammer = new ItemTemplate("rock_hammer", "art/png/hammer.png", new RockHammer());
        hammer.customInventoryRender = true;
        registerItem(hammer);
        registerItem(new Torch());
        registerItem(new Stick());
    }
}
