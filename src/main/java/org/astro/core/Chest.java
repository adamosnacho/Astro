package org.astro.core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

public class Chest extends Entity implements TipEntity, BreakableTileEntity, Serializable {
    public static boolean anyOpen = false;

    private transient BufferedImage sprite;
    private transient BufferedImage spriteOpened;
    public InventoryItem[] data = new InventoryItem[20]; // Chest items
    public GamePanel gp;
    public final int width = 100;
    public final int height = 100;
    public boolean open = false;
    private int scrollOffset = 0;

    public Chest(GamePanel gp, int x, int y) {
        this.gp = gp;
        this.x = x;
        this.y = y;
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/art/png/chest.png")); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            spriteOpened = ImageIO.read(getClass().getResourceAsStream("/art/png/chestOpened.png")); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add a mouse wheel listener for scrolling
        gp.addMouseWheelListener(e -> {
            if (open) {
                int notches = e.getWheelRotation();
                scrollOffset += notches * 40; // Adjust scroll speed
                int l = 0;
                for (int i = 0; i < data.length; i++) {
                    if (data[i] != null) l = i;
                }
                int maxScroll = l * 60; // This will be 0 if the content fits within the visible area, otherwise a negative value
                scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);
            }
        });

        gp.hud.hudComponents.add(g -> {
            if (!open) return;
            Graphics2D g2d = (Graphics2D) g;
            AffineTransform originalTransform = g2d.getTransform();

            // Draw background for the chest GUI
            g.setColor(Color.DARK_GRAY);
            g.fillRect(gp.getWidth() - 400, 50, 300, gp.getHeight() - 100);
            g2d.setClip(gp.getWidth() - 400, 50, 300, gp.getHeight() - 100);

            // Draw dividing line
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawLine(gp.getWidth() - 250, 52, gp.getWidth() - 250, gp.getHeight() - 52);

            // Draw player slots (left side of the line) - Static and not affected by scroll
            int slotSize = 50;
            int slotMargin = 5; // Margin around the slots
            int itemX = gp.getWidth() - 350; // X position for player items
            int itemY = 100; // Starting Y position for player items

            // Draw player inventory slots
            drawPlayerSlot(g2d, itemX, itemY, gp.player.pi.hand);
            itemY += slotSize + slotMargin;
            drawPlayerSlot(g2d, itemX, itemY, gp.player.pi.slot1);
            itemY += slotSize + slotMargin;
            drawPlayerSlot(g2d, itemX, itemY, gp.player.pi.slot2);
            itemY += slotSize + slotMargin;
            drawPlayerSlot(g2d, itemX, itemY, gp.player.pi.slot3);

            // Draw chest contents (right side of the line) with scrolling
            itemX = gp.getWidth() - 200; // X position for chest items
            itemY = 100 - scrollOffset; // Y position adjusted for scrolling

            for (InventoryItem item : data) {
                drawInventorySlots(g2d, itemX, itemY, slotSize, slotMargin, item);
                itemY += slotSize + slotMargin; // Move down for the next item
            }

            g2d.setTransform(originalTransform);
        });
        gp.tipManager.tipEntities.add(this);
    }

    private void drawPlayerSlot(Graphics2D g2d, int x, int y, InventoryItem item) {
        int slotSize = 50;
        int slotMargin = 5;

        // Draw slot background
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(x - slotMargin, y - slotMargin, slotSize + 2 * slotMargin, slotSize + 2 * slotMargin);

        // Draw item if it exists
        if (item != null) {
            g2d.drawImage(item.sprite, x, y, slotSize, slotSize, null);
        }
    }


    // Helper method to draw inventory slots with items
    private void drawInventorySlots(Graphics2D g2d, int x, int y, int size, int margin, InventoryItem item) {
        // Draw slot
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(x - margin, y - margin, size + 2 * margin, size + 2 * margin);

        // Draw item if it exists
        if (item != null) {
            g2d.drawImage(item.sprite, x, y, size, size, null);
        }
    }

    @Override
    public void draw(Graphics g) {
        if (open) g.drawImage(spriteOpened, x, y, width, height, null);
        else g.drawImage(sprite, x, y, width, height, null);
    }

    @Override
    public void update(double deltaTime) {
        if (open) gp.player.inMenu = true;
        double dx = gp.player.x - x;
        double dy = gp.player.y - y;

        double sqrt = Math.sqrt(dx * dx + dy * dy);
        if (open && sqrt >= 600) chestState(false);

        int clickX = gp.cameraX + Input.mouse.getX();
        int clickY = gp.cameraY + Input.mouse.getY();
        if (Input.btn3Click) {
            if (clickX > x && clickX < x + width && clickY > y && clickY < y + height) {
                chestState(sqrt < 600);
            } else if (open) {
                handleItemClick(Input.mouse.getX(), Input.mouse.getY());
            }
        }
        if (Input.keys.contains(KeyEvent.VK_ESCAPE)) chestState(false);
    }

    private void chestState(boolean op) {
        if (Chest.anyOpen && op) return;
        Chest.anyOpen = op;
        open = op;
    }

    private void handleItemClick(int clickX, int clickY) {
        int slotSize = 50;
        int slotMargin = 5; // Margin around the slots
        int itemX = gp.getWidth() - 350; // X position for player items
        int itemY = 100; // Starting Y position for player items

        // Check for click on player inventory slots
        if (gp.player.pi.hand != null && clickX > itemX && clickX < itemX + slotSize && clickY > itemY && clickY < itemY + slotSize) {
            moveItemToChest(0);
            return;
        }
        itemY += slotSize + slotMargin;
        if (gp.player.pi.slot1 != null && clickX > itemX && clickX < itemX + slotSize && clickY > itemY && clickY < itemY + slotSize) {
            moveItemToChest(1);
            return;
        }
        itemY += slotSize + slotMargin;
        if (gp.player.pi.slot2 != null && clickX > itemX && clickX < itemX + slotSize && clickY > itemY && clickY < itemY + slotSize) {
            moveItemToChest(2);
            return;
        }
        itemY += slotSize + slotMargin;
        if (gp.player.pi.slot3 != null && clickX > itemX && clickX < itemX + slotSize && clickY > itemY && clickY < itemY + slotSize) {
            moveItemToChest(3);
            return;
        }

        // Check for click on chest items
        int chestX = gp.getWidth() - 200; // X position for chest items
        itemY = 100 - scrollOffset; // Adjust Y position for chest items

        for (int i = 0; i < data.length; i++) {
            if (data[i] != null && clickX > chestX && clickX < chestX + slotSize && clickY > itemY && clickY < itemY + slotSize) {
                moveItemToPlayer(i);
                return;
            }
            itemY += slotSize + slotMargin; // Move down for the next item
        }
    }


    private void moveItemToChest(int playerSlot) {
        InventoryItem itemToMove = null;
        if (playerSlot == 0) {
            itemToMove = gp.player.pi.hand;
            if (gp.player.pi.hand.updLst != null) gp.player.pi.hand.updLst.state(false, gp.player.pi.hand);
            if (helper(itemToMove)) return;
            gp.player.pi.hand = null;
        } else if (playerSlot == 1) {
            itemToMove = gp.player.pi.slot1;
            if (helper(itemToMove)) return;
            gp.player.pi.slot1 = null;
        } else if (playerSlot == 2) {
            itemToMove = gp.player.pi.slot2;
            if (helper(itemToMove)) return;
            gp.player.pi.slot2 = null;
        } else if (playerSlot == 3) {
            itemToMove = gp.player.pi.slot3;
            if (helper(itemToMove)) return;
            gp.player.pi.slot3 = null;
        }
    }
    private boolean helper(InventoryItem it) {
        if (!it.secondData.isEmpty()) return true;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == null) {
                data[i] = it;
                return false;
            }
        }
        return true;
    }

    private void moveItemToPlayer(int chestSlot) {
        InventoryItem itemToMove = data[chestSlot];

        if (gp.player.pi.hand == null) {
            gp.player.pi.hand = itemToMove;
            if (gp.player.pi.hand.updLst != null) gp.player.pi.hand.updLst.state(true, gp.player.pi.hand);
            data[chestSlot] = null;
        } else if (gp.player.pi.slot1 == null) {
            gp.player.pi.slot1 = itemToMove;
            data[chestSlot] = null;
        } else if (gp.player.pi.slot2 == null) {
            gp.player.pi.slot2 = itemToMove;
            data[chestSlot] = null;
        } else if (gp.player.pi.slot3 == null) {
            gp.player.pi.slot3 = itemToMove;
            data[chestSlot] = null;
        }
    }

    @Override
    public Vector2 getSize() {
        return new Vector2(width, height);
    }

    @Override
    public String getDisplay() {
        if (data.length == 0) return Items.getInstance(gp).itemDescriptions.get("chest");

        StringBuilder display = new StringBuilder("Chest contains:");
        for (InventoryItem item : data) {
            if (item == null) continue;
            display.append("\n- ").append(item.itemName);
        }
        return display.toString();
    }

    @Override
    public String getName() {
        return "Chest";
    }

    @Override
    public PhysicalItem getDrop() {
        return Items.getInstance(gp).getPhysical("chest");
    }

    @Override
    public void deSpawn() {
        for (InventoryItem i : data) {
            if (i == null) continue;
            PhysicalItem drop = Items.getInstance(gp).getPhysical(i.itemName);
            drop.data = i.data;
            drop.secondData = i.secondData;
            drop.x = x + gp.terrain.randomRange(0, width);
            drop.y = y + gp.terrain.randomRange(0, height);
            gp.spawnEntity(drop, 10);
        }
        gp.tipManager.tipEntities.remove(this);
        open = false;
        anyOpen = false;
    }
}
