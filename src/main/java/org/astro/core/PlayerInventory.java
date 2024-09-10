package org.astro.core;

import org.astro.core.particles.Particle;
import org.astro.core.particles.ParticleGroup;
import org.astro.core.particles.ParticleSystem;
import org.astro.core.saving.Savable;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.List;

public class PlayerInventory implements Savable {
    private Player p;
    private GamePanel gp;

    public InventoryItem hand;
    public InventoryItem slot1;
    public InventoryItem slot2;
    public InventoryItem slot3;

    public Set<InventoryItem> craft = new HashSet<>();

    public PlayerInventory(Player p, GamePanel gp) {
        this.p = p;
        this.gp = gp;
    }

    private boolean pressed1 = false;
    private boolean pressed2 = false;
    private boolean pressed3 = false;

    public void input() {
        MouseEvent me = Input.mouse;
        handleRightClickSwitching(me);
        handleSlotSwitching();
        if (p.inMenu) return;

        // Handle dropping/crafting items
        if (Input.keys.contains(KeyEvent.VK_Q)) {
            if (hand != null) {
                if (Input.keys.contains(KeyEvent.VK_SHIFT)) {
                    craft.add(hand);
                    if (hand.updLst != null) hand.updLst.state(false, hand);
                    hand = null;
                } else {
                    PhysicalItem drop = Items.getInstance(gp).getPhysical(hand.itemName);
                    drop.data = hand.data;
                    drop.secondData = hand.secondData;
                    drop(drop);
                }
            }
        }

        // Handle crafting with the items in the crafting pool
        if (!Input.keys.contains(KeyEvent.VK_SHIFT) && !craft.isEmpty()) {
            CraftingRecipes craftingRecipes = new CraftingRecipes(gp);
            boolean crafted = false;
            for (int i = 0; i < craftingRecipes.recipes.length; i++) {
                boolean broken = false;
                InventoryItem[] pool = craft.toArray(new InventoryItem[0]);
                for (InventoryItem e : craftingRecipes.recipes[i]) {
                    boolean contains = false;
                    for (int j = 0; j < pool.length; j++) {
                        if (pool[j] == null) continue;
                        if (pool[j].itemName == e.itemName) {
                            if (pool[j].itemName.equals("blueprint")) {
                                if (pool[j].data.isEmpty() || !Objects.equals(pool[j].data.getFirst(), craftingRecipes.crafts[i].itemName))
                                    continue;
                            }
                            contains = true;
                            pool[j] = null;
                            break;
                        }
                    }
                    if (!contains) {
                        broken = true;
                        break;
                    }
                }
                int len = 0;
                for (InventoryItem e : pool) {
                    if (e == null) continue;
                    len++;
                }
                if (broken || len != 0) continue;
                drop(Items.getInstance(gp).getPhysical(craftingRecipes.crafts[i].itemName));
                for (InventoryItem e : craftingRecipes.recipes[i]) {
                    if (Objects.equals(e.itemName, "blueprint")) {
                        PhysicalItem bp = Items.getInstance(gp).getPhysical("blueprint");
                        bp.data.add(craftingRecipes.crafts[i].itemName);
                        drop(bp);
                    }
                }
                crafted = true;
                break;
            }
            if (!crafted) {
                for (InventoryItem e : craft) {
                    PhysicalItem i = Items.getInstance(gp).getPhysical(e.itemName);
                    i.data = e.data;
                    i.secondData = e.secondData;
                    drop(i);
                }
            }
            craft.clear();
        }


        // Handle left-click for placing or using items
        if (Input.btn1Click) {
            int posX = gp.cameraX + me.getX();
            int posY = gp.cameraY + me.getY();

            // Snap to the nearest tiles
            int tileSize = gp.terrain.tileSize;
            posX = (posX / tileSize) * tileSize;
            posY = (posY / tileSize) * tileSize;

            for (Entity en : gp.entities[5]) {
                if (en.x == posX && en.y == posY) {
                    if (gp.breakableTileEntities.contains(en) && hand != null && hand.isBreakingTool) {
                        gp.deSpawnEntity(en, 5);
                        PhysicalItem drop = ((BreakableTileEntity) en).getDrop();
                        drop.x = posX;
                        drop.y = posY;
                        gp.spawnEntity(drop, 10);
                        gp.tipManager.tipEntities.add(drop);
                        hand.onUse.onUse(hand);
                        ParticleGroup pg = new ParticleGroup(gp.cameraX + Input.mouse.getX(), gp.cameraY + Input.mouse.getY());
                        pg.generateParticles(() -> {
                            Color c = new Color(drop.sprite.getRGB(gp.terrain.randomRange(0, drop.sprite.getWidth() - 1), gp.terrain.randomRange(0, drop.sprite.getHeight() - 1)));
                            Particle p = new Particle(pg, 0, 0, gp.terrain.randomRange(-10, 10), gp.terrain.randomRange(-10, 10), c, 20, 0.5f, 3000);
                            p.drag = 0.1f;
                            p.constantY = 20;
                            return p;
                        }, 30);
                        ParticleSystem.particleGroups.add(pg);
                    }
                }
            }
            if (hand != null && hand.placeable) {
                gp.breakableTileEntities.add(hand.updLst.onPlace(gp, hand, posX, posY, hand.data, hand.secondData));
                hand = null;
            }
        }
    }

    private void handleSlotSwitching() {
        if (Input.keys.contains(KeyEvent.VK_1)) {
            if (!pressed1) {
                switchItemsInSlot(1);
                pressed1 = true;
            }
        } else {
            pressed1 = false;
        }

        if (Input.keys.contains(KeyEvent.VK_2)) {
            if (!pressed2) {
                switchItemsInSlot(2);
                pressed2 = true;
            }
        } else {
            pressed2 = false;
        }

        if (Input.keys.contains(KeyEvent.VK_3)) {
            if (!pressed3) {
                switchItemsInSlot(3);
                pressed3 = true;
            }
        } else {
            pressed3 = false;
        }
    }

    private void switchItemsInSlot(int slot) {
        if (hand != null && hand.updLst != null) hand.updLst.state(false, hand);
        InventoryItem newHand = null;

        switch (slot) {
            case 1:
                newHand = slot1;
                slot1 = hand;
                break;
            case 2:
                newHand = slot2;
                slot2 = hand;
                break;
            case 3:
                newHand = slot3;
                slot3 = hand;
                break;
        }

        hand = newHand;
        if (hand != null) hand.inHand = true;
        if (hand != null && hand.updLst != null) hand.updLst.state(true, hand);
    }

    private void handleRightClickSwitching(MouseEvent me) {
        if (Input.btn3Click) { // Right click
            if (isSlotClicked(me, 1)) {
                switchItemsInSlot(1);
            } else if (isSlotClicked(me, 2)) {
                switchItemsInSlot(2);
            } else if (isSlotClicked(me, 3)) {
                switchItemsInSlot(3);
            }
        }
    }

    private boolean isSlotClicked(MouseEvent me, int slot) {
        // Replace with actual logic to determine if a slot was clicked based on mouse position
        // For simplicity, assume the slots are at specific positions in the GUI
        // Example:
        int mouseX = me.getX();
        int mouseY = me.getY();

        // Define the screen positions for slot 1, 2, and 3
        Rectangle slot1Area = new Rectangle(15, 15, 50, 50);
        Rectangle slot2Area = new Rectangle(15, 65, 50, 50);
        Rectangle slot3Area = new Rectangle(15, 115, 50, 50);

        switch (slot) {
            case 1:
                return slot1Area.contains(mouseX, mouseY);
            case 2:
                return slot2Area.contains(mouseX, mouseY);
            case 3:
                return slot3Area.contains(mouseX, mouseY);
            default:
                return false;
        }
    }

    public void draw(Graphics g) {
        if (hand == null) return;
        hand.draw(g, p);
    }

    public void update(double deltaTime) {
        if (hand == null) return;
        hand.update(gp, deltaTime);
    }

    public boolean pickUp(InventoryItem item) {
        if (hand == null) {
            hand = item;
            if (hand.updLst != null) hand.updLst.state(true, hand);
            return true;
        }
        if (slot1 == null) {
            slot1 = item;
            if (slot1.updLst != null) slot1.updLst.state(true, slot1);
            return true;
        }
        if (slot2 == null) {
            slot2 = item;
            if (slot2.updLst != null) slot2.updLst.state(true, slot2);
            return true;
        }
        if (slot3 == null) {
            slot3 = item;
            if (slot3.updLst != null) slot3.updLst.state(true, slot3);
            return true;
        }
        return false;
    }

    public void drop(PhysicalItem drop) {
        drop.x = p.x + gp.terrain.randomRange(0, p.width - drop.width);
        drop.y = p.y + p.height;
        gp.spawnEntity(drop, 10);
        if (hand != null && hand.updLst != null) hand.updLst.state(false, hand);
        hand = null;
    }

    record savableItem(String name, List<String> data, List<List<String>> secondData) implements Serializable {
    }
    record savableInventory(savableItem hand, savableItem slot1, savableItem slot2, savableItem slot3) implements Serializable {
    }
    @Override
    public Object save() {
        return new savableInventory(
                hand != null ? new savableItem(hand.itemName, hand.data, hand.secondData) : null,
                slot1 != null ? new savableItem(slot1.itemName, slot1.data, slot1.secondData) : null,
                slot2 != null ? new savableItem(slot2.itemName, slot2.data, slot2.secondData) : null,
                slot3 != null ? new savableItem(slot3.itemName, slot3.data, slot3.secondData) : null
        );
    }

    @Override
    public void load(Object o) {
        savableInventory i = (savableInventory) o;
        if (i.hand != null) {
            hand = Items.getInstance(gp).getInventory(i.hand.name);
            hand.data = i.hand.data;
            hand.secondData = i.hand.secondData;
        }

        if (i.slot1 != null) {
            slot1 = Items.getInstance(gp).getInventory(i.slot1.name);
            slot1.data = i.slot1.data;
            slot1.secondData = i.slot1.secondData;
        }

        if (i.slot2 != null) {
            slot2 = Items.getInstance(gp).getInventory(i.slot2.name);
            slot2.data = i.slot2.data;
            slot2.secondData = i.slot2.secondData;
        }

        if (i.slot3 != null) {
            slot3 = Items.getInstance(gp).getInventory(i.slot3.name);
            slot3.data = i.slot3.data;
            slot3.secondData = i.slot3.secondData;
        }
    }
}
