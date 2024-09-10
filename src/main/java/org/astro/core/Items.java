package org.astro.core;

import org.astro.core.particles.Particle;
import org.astro.core.particles.ParticleGroup;
import org.astro.core.particles.ParticleSystem;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Items {
    private GamePanel gp;
    private static Items instance;
    public Map<String, PhysicalItem> physicalItemsP = new HashMap<>();
    public Map<String, InventoryItem> inventoryItemsP = new HashMap<>();
    public Map<String, String> itemDescriptions = new HashMap<>();

    private Items(GamePanel gp) {
        this.gp = gp;
        //Physical items
        physicalItemsP.put("drill", new PhysicalItem(gp, "drill", "/art/png/drill.png", 0, 0, true));
        physicalItemsP.put("o2 generator", new PhysicalItem(gp, "o2 generator", "/art/png/o2Generator.png", 0, 0, true));
        physicalItemsP.put("lead", new PhysicalItem(gp, "lead", "/art/png/lead.png", 0, 0, false));
        physicalItemsP.put("aluminum", new PhysicalItem(gp, "aluminum", "/art/png/aluminum.png", 0, 0, false));
        physicalItemsP.put("pole", new PhysicalItem(gp, "pole", "/art/png/pole.png", 0, 0, true));
        physicalItemsP.put("aluminum sword", new PhysicalItem(gp, "aluminum sword", "/art/png/sword.png", 0, 0, false));
        physicalItemsP.put("lead pickaxe", new PhysicalItem(gp, "lead pickaxe", "/art/png/leadPickaxe.png", 0, 0, false));
        physicalItemsP.put("rope", new PhysicalItem(gp, "rope", "/art/png/rope.png", 0, 0, false));
        physicalItemsP.put("chest", new PhysicalItem(gp, "chest", "/art/png/chest.png", 0, 0, true));
        physicalItemsP.put("bag", new PhysicalItem(gp, "bag", "/art/png/bagIcon.png", 0, 0, true));
        physicalItemsP.put("metal rod", new PhysicalItem(gp, "metal rod", "/art/png/MetalRod.png", 0, 0, true));
        physicalItemsP.put("map", new PhysicalItem(gp, "map", "/art/png/Map.png", 0, 0, false));
        physicalItemsP.put("harpoon", new PhysicalItem(gp, "harpoon", "/art/png/harpoonIcon.png", 0, 0, false));
        physicalItemsP.put("arrow", new PhysicalItem(gp, "arrow", "/art/png/arrow.png", 0, 0, false));
        physicalItemsP.put("blueprint", new PhysicalItem(gp, "blueprint", "/art/png/blueprint.png", 0, 0, false));
        physicalItemsP.put("packed aluminum", new PhysicalItem(gp, "packed aluminum", "/art/png/packedAluminum.png", 0, 0, false));

        physicalItemsP.get("bag").customDisplay = item -> {
            if (item.data.isEmpty()) return Items.getInstance(gp).itemDescriptions.get("bag");

            StringBuilder display = new StringBuilder("Bag contains:");

            for (String d : item.data) {
                if (d == null) continue;
                display.append("\n- ").append(d);
            }
            return display.toString();
        };

        physicalItemsP.get("drill").customDisplay = item -> {
            String durability = "100";
            if (!item.data.isEmpty()) durability = item.data.getFirst();
            return "A device used to dig out minerals.\nDurability: " + durability + "%";
        };

        physicalItemsP.get("o2 generator").customDisplay = item -> {
            if (item.data.isEmpty()) return Items.getInstance(gp).itemDescriptions.get(item.itemName);
            return Items.getInstance(gp).itemDescriptions.get(item.itemName) + "\nDurability: " + (int) Float.parseFloat(item.data.getFirst());
        };

        physicalItemsP.get("map").customDisplay = item -> {
            if (item.data.isEmpty()) return Items.getInstance(gp).itemDescriptions.get(item.itemName);
            return Items.getInstance(gp).itemDescriptions.get(item.itemName) + "\nLeads to: " + item.data.getFirst();
        };

        physicalItemsP.get("lead pickaxe").customDisplay = item -> {
            String durability = "100";
            if (!item.data.isEmpty()) durability = item.data.getFirst();
            return "A pickaxe crafted from lead.\nDurability: " + durability + "%";
        };

        physicalItemsP.get("harpoon").customDisplay = item -> {
            if (item.data.size() < 2) return itemDescriptions.get(item.itemName);
            return itemDescriptions.get(item.itemName) + "\nArrows: " + item.data.getFirst() + "\nDurability: " + item.data.get(2) + "%\n" + item.data.get(1);
        };

        physicalItemsP.get("blueprint").customDisplay = item -> {
            if (item.data.isEmpty()) return itemDescriptions.get(item.itemName);
            return itemDescriptions.get(item.itemName) + "\nCrafts: " + item.data.getFirst();
        };


        //Inventory items
        inventoryItemsP.put("drill", new InventoryItem("drill", "/art/png/drill.png", true));
        inventoryItemsP.get("drill").updLst = new UpdateListener() {
            @Override
            public void update(InventoryItem item, GamePanel gp, double deltaTime) {

            }

            @Override
            public void draw(Graphics g, GamePanel gp, InventoryItem i) {

            }

            @Override
            public Entity onPlace(GamePanel gp, InventoryItem item, int x, int y, List<String> data, List<List<String>> secondData) {
                Drill drill = new Drill(gp, x, y, data);
                gp.spawnEntity(drill, 5);
                return drill;
            }

            @Override
            public void state(boolean state, InventoryItem item) {

            }
        };

        inventoryItemsP.put("o2 generator", new InventoryItem("o2 generator", "/art/png/o2Generator.png", true));
        inventoryItemsP.get("o2 generator").updLst = new UpdateListener() {
            @Override
            public void update(InventoryItem item, GamePanel gp, double deltaTime) {

            }

            @Override
            public void draw(Graphics g, GamePanel gp, InventoryItem i) {

            }

            @Override
            public Entity onPlace(GamePanel gp, InventoryItem item, int x, int y, List<String> data, List<List<String>> secondData) {
                Entity o2g = new O2Generator(gp, x, y, data);
                gp.spawnEntity(o2g, 5);
                return o2g;
            }

            @Override
            public void state(boolean state, InventoryItem item) {

            }
        };

        inventoryItemsP.put("pole", new InventoryItem("pole", "/art/png/pole.png", true));
        inventoryItemsP.get("pole").updLst = new UpdateListener() {
            @Override
            public void update(InventoryItem item, GamePanel gp, double deltaTime) {

            }

            @Override
            public void draw(Graphics g, GamePanel gp, InventoryItem i) {

            }

            @Override
            public Entity onPlace(GamePanel gp, InventoryItem item, int x, int y, List<String> data, List<List<String>> secondData) {
                Pole pole = new Pole(gp, x, y);
                gp.spawnEntity(pole, 5);
                PoleManager.getInstance().poles.add(pole);
                return pole;
            }

            @Override
            public void state(boolean state, InventoryItem item) {

            }
        };

        inventoryItemsP.put("lead", new InventoryItem("lead", "/art/png/lead.png", false));
        inventoryItemsP.put("aluminum", new InventoryItem("aluminum", "/art/png/aluminum.png", false));
        inventoryItemsP.put("map", new InventoryItem("map", "/art/png/Map.png", false));
        inventoryItemsP.get("map").updLst = new UpdateListener() {
            @Override
            public void update(InventoryItem item, GamePanel gp, double deltaTime) {
                if (Input.btn1Click) {
                    gp.navigator.points.put(item.data.getFirst(), new Vector2(Double.parseDouble(item.data.get(1)), Double.parseDouble(item.data.get(2))));
                    Player p = gp.player;
                    int yOffset = switch (p.animationFrame) {
                        case 1, 2 -> -5;
                        default -> 0;
                    };
                    int x, y;
                    if (p.facingRight) {
                        x = p.x + p.width - item.width;
                        y = p.y + p.height / 2 + yOffset;
                    } else {
                        x = p.x;
                        y = p.y + p.height / 2 + yOffset;
                    }
                    ParticleGroup pg = new ParticleGroup(x, y);
                    pg.generateParticles(() -> new Particle(pg, 0, 0, gp.terrain.randomRange(-30, 30), gp.terrain.randomRange(-30, 30), Color.GRAY, 10, 0.3f, 700), 10);
                    ParticleSystem.particleGroups.add(pg);
                    gp.player.pi.hand = null;
                }
            }

            @Override
            public void draw(Graphics g, GamePanel gp, InventoryItem i) {

            }

            @Override
            public Entity onPlace(GamePanel gp, InventoryItem item, int x, int y, List<String> data, List<List<String>> secondData) {
                return null;
            }

            @Override
            public void state(boolean state, InventoryItem item) {

            }
        };
        inventoryItemsP.put("blueprint", new InventoryItem("blueprint", "/art/png/blueprint.png", false));
        inventoryItemsP.put("packed aluminum", new InventoryItem("packed aluminum", "/art/png/packedAluminum.png", false));

        inventoryItemsP.put("harpoon", new InventoryItem("harpoon", "/art/png/harpoonIcon.png", false));
        inventoryItemsP.get("harpoon").updLst = new Harpoon(gp);
        inventoryItemsP.get("harpoon").customDraw = true;

        inventoryItemsP.put("aluminum sword", new InventoryItem("aluminum sword", "/art/png/sword.png", false));
        inventoryItemsP.get("aluminum sword").updLst = new AluminumSword();
        inventoryItemsP.get("aluminum sword").customDraw = true;

        inventoryItemsP.put("metal rod", new InventoryItem("metal rod", "/art/png/MetalRod.png", true));
        inventoryItemsP.get("metal rod").updLst = new UpdateListener() {
            @Override
            public void update(InventoryItem item, GamePanel gp, double deltaTime) {

            }

            @Override
            public void draw(Graphics g, GamePanel gp, InventoryItem i) {

            }

            @Override
            public Entity onPlace(GamePanel gp, InventoryItem item, int x, int y, List<String> data, List<List<String>> secondData) {
                return new MetalRod(gp, x, y);
            }

            @Override
            public void state(boolean state, InventoryItem item) {

            }
        };

        inventoryItemsP.put("chest", new InventoryItem("chest", "/art/png/chest.png", true));
        inventoryItemsP.get("chest").updLst = new UpdateListener() {
            @Override
            public void update(InventoryItem item, GamePanel gp, double deltaTime) {

            }
            @Override
            public void draw(Graphics g, GamePanel gp, InventoryItem i) {

            }
            @Override
            public Entity onPlace(GamePanel gp, InventoryItem item, int x, int y, List<String> data, List<List<String>> secondData) {
                Entity e = new Chest(gp, x, y);
                gp.spawnEntity(e, 5);
                return e;
            }

            @Override
            public void state(boolean state, InventoryItem item) {

            }
        };

        inventoryItemsP.put("rope", new InventoryItem("rope", "/art/png/rope.png", false));
        inventoryItemsP.put("arrow", new InventoryItem("arrow", "/art/png/arrow.png", false));

        inventoryItemsP.put("bag", new InventoryItem("bag", "/art/png/bagIcon.png", false));
        inventoryItemsP.get("bag").updLst = new Bag(gp);


        inventoryItemsP.put("lead pickaxe", new InventoryItem("lead pickaxe", "/art/png/leadPickaxe.png", false));
        inventoryItemsP.get("lead pickaxe").isBreakingTool = true;
        inventoryItemsP.get("lead pickaxe").onUse = item -> {
            if (item.data.isEmpty()) item.data.add("100");
            int durability = Integer.parseInt(item.data.getFirst()) - 10;
            item.data.removeFirst();
            item.data.add(String.valueOf(durability));
            if (durability <= 0) gp.player.pi.hand = null;
        };


        // Item descriptions
        itemDescriptions.put("drill", "A device used to dig out minerals.");
        itemDescriptions.put("o2 generator", "A machine that generates oxygen, can connect to poles.");
        itemDescriptions.put("lead", "A mineral used for crafting.");
        itemDescriptions.put("aluminum", "A mineral used for crafting.");
        itemDescriptions.put("packed aluminum", "A polished crystal made of 10 aluminum. Valuable.");
        itemDescriptions.put("pole", "Used to extend the range of your oxygen tether.");
        itemDescriptions.put("aluminum sword", "A fine blade crafted from aluminum. Deals 50 hp to enemies.");
        itemDescriptions.put("rope", "A simple rope used for crafting.");
        itemDescriptions.put("lead pickaxe", "A pickaxe crafted from lead.");
        itemDescriptions.put("chest", "A simple way to store items.");
        itemDescriptions.put("bag", "A portable way to store items.");
        itemDescriptions.put("metal rod", "A rusty metal rod.");
        itemDescriptions.put("harpoon", "A simple harpoon");
        itemDescriptions.put("arrow", "Right click on a harpoon to load it.");
        itemDescriptions.put("map", "Use it to locate structures.");
        itemDescriptions.put("blueprint", "Used for crafting.");
    }

    public void addCustomItem(InventoryItem ii, PhysicalItem pi, String description) {
        physicalItemsP.put(pi.itemName, pi);
        inventoryItemsP.put(pi.itemName, ii);
        itemDescriptions.put(pi.itemName, description);
    }

    public static Items getInstance(GamePanel gp) {
        if (instance == null) {
            instance = new Items(gp);
        }
        return instance;
    }

    public PhysicalItem getPhysical(String itemName) {
        PhysicalItem i = physicalItemsP.get(itemName);
        return i.copy();
    }

    public InventoryItem getInventory(String itemName) {
        InventoryItem i = inventoryItemsP.get(itemName);
        return i.copy(); // Use the clone method to copy properties and onPlace method
    }
}
