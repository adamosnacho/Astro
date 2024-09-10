package org.astro.core;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class Bag implements UpdateListener, Serializable {
    public InventoryItem[] data = new InventoryItem[20]; // Chest items
    public GamePanel gp;
    public final int width = 100;
    public final int height = 100;
    public boolean open = false;
    private int scrollOffset = 0;
    private HudComponent hc;

    public Bag(GamePanel gp) {
        this.gp = gp;
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

        hc = g -> {
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

            // Skip drawing the hand slot
            itemY += slotSize + slotMargin; // Adjust Y position

            // Draw the remaining player inventory slots
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
        };
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

    private void handleItemClick(int clickX, int clickY) {
        int slotSize = 50;
        int slotMargin = 5; // Margin around the slots
        int itemX = gp.getWidth() - 350; // X position for player items
        int itemY = 100; // Starting Y position for player items

        // Skip checking the hand slot for clicks
        itemY += slotSize + slotMargin;

        // Check for click on player inventory slots
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
        InventoryItem itemToMove;
        if (playerSlot == 1) {
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

        if (gp.player.pi.slot1 == null) {
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
    public void update(InventoryItem item, GamePanel gp, double deltaTime) {
        if ((Input.keys.contains(KeyEvent.VK_ESCAPE) || Input.btn1Click) && open) {
            save(item);
            Chest.anyOpen = false;
            open = false;
            gp.hud.hudComponents.remove(hc);
        } else if (Input.btn1Click && !Chest.anyOpen) {
            updateItems(item);
            Chest.anyOpen = true;
            open = true;
            gp.hud.hudComponents.add(hc);
        }

        if (open) {
            if (Input.btn3Click) {
                handleItemClick(Input.mouse.getX(), Input.mouse.getY());
                save(item);
            }
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
        if (!state) {
            open = false;
            Chest.anyOpen = false;
            gp.hud.hudComponents.remove(hc);
        }
        updateItems(item);
    }

    private void updateItems(InventoryItem item) {
        Arrays.fill(data, null);

        int dataSize = item.data.size();
        int secondDataSize = item.secondData != null ? item.secondData.size() : 0;

        for (int i = 0; i < dataSize; i++) {
            InventoryItem it = Items.getInstance(gp).getInventory(item.data.get(i));

            // Check if secondData exists for this item, and if so, assign it
            if (i < secondDataSize) {
                it.data = item.secondData.get(i);
            } else {
                // Handle items with no secondData by initializing an empty list or setting it to null
                it.data = new ArrayList<>();  // or it.data = null; based on your application needs
            }

            data[i] = it;
        }
    }

    private void save(InventoryItem item) {
        item.data.clear();
        item.secondData.clear();

        for (InventoryItem datum : data) {
            if (datum == null) continue;
            // Save the item name
            item.data.add(datum.itemName);

            // Ensure the item has data and add it to secondData
            if (!datum.data.isEmpty()) {
                item.secondData.add(datum.data);
            } else {
                // Add an empty list to maintain alignment with the data list
                item.secondData.add(new ArrayList<>());
            }
        }
    }
}
