package org.astro.core.items;

import org.astro.core.Astro;
import org.astro.core.PlayerInventory;
import org.astro.core.Utils;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.ItemEvents;
import org.astro.core.itemsystem.ItemTemplate;
import org.astro.core.itemsystem.Items;
import org.newdawn.slick.*;
import org.astro.core.Input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Bag extends ItemTemplate {
    private final Image slots;

    public Bag() {
        super("bag", "art/png/bagIcon.png", null);
        try {
            slots = new Image("art/png/gui/bagSlots.png", false, Image.FILTER_NEAREST).getScaledCopy(PlayerInventory.guiScale);
            slots.setAlpha(0.5f);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }

        itemEvents = new BagItemEvents();
    }

    private class BagItemEvents extends ItemEvents {
        @Override
        public void onInstantiation(Item i) {
            i.itemData = new Storage();
        }

        @Override
        public void gui(Graphics g, Item i) {
            Storage s = (Storage) i.itemData;
            if (!s.open) return;
            int paddingX = PlayerInventory.paddingX;
            int paddingY = PlayerInventory.paddingY;
            float guiScale = PlayerInventory.guiScale;

            int x = Astro.app.getInput().getMouseX();
            int y = Astro.app.getInput().getMouseY();

            g.drawImage(slots, Astro.astro.camera.width - paddingX - slots.getWidth(), paddingY);
            g.drawString("Bag", Astro.astro.camera.width - paddingX - slots.getWidth(), paddingY - g.getFont().getHeight("Bag"));

            int startX = (int) (Astro.astro.camera.width - paddingX - slots.getWidth());
            int offsetX = 1;
            int offsetY = 1;
            for (int j = 0; j < s.bagItems.length / 2; j++) {
                if (s.bagItems[j] != null) g.drawImage(s.bagItems[j].sprite.getScaledCopy(16, 16).getScaledCopy(guiScale), startX + offsetX * guiScale, paddingY + offsetY * guiScale);
                offsetX += 18;
            }
            offsetY += 18;
            offsetX = 1;
            for (int j = s.bagItems.length / 2; j < s.bagItems.length; j++) {
                if (s.bagItems[j] != null) g.drawImage(s.bagItems[j].sprite.getScaledCopy(16, 16).getScaledCopy(guiScale), startX + offsetX * guiScale, paddingY + offsetY * guiScale);
                offsetX += 18;
            }

            if (s.heldItem != null) {
                g.drawImage(s.heldItem.sprite.getScaledCopy(40, 40), x - 20, y - 20);
                g.setColor(Color.white);
                g.drawString(s.heldItem.it.itemEvents.getStatus(s.heldItem), x - 20, y - 20 - g.getFont().getHeight(s.heldItem.it.itemEvents.getStatus(s.heldItem)));
            }
        }

        @Override
        public void guiUpdate(Item i) {
            if (Input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)) handleClick(i);
            if (((Storage) i.itemData).open) {
                PlayerInventory.inputActive = false;
                Astro.astro.player.canMove = false;
            }
        }

        @Override
        public void onUse(Item i) {
            Storage s = (Storage) i.itemData;
            if (s.open) {
                s.open = false;
                PlayerInventory.inputActive = true;
                Astro.astro.player.canMove = true;
                if (s.heldItem != null) {
                    s.heldItem.drop(Astro.astro.player.x + Utils.randomRange(0, Astro.astro.player.width), Astro.astro.player.y + Astro.astro.player.height);
                    s.heldItem = null;
                }
            }
            else if (PlayerInventory.held == null && Astro.astro.player.canMove) s.open = true;
        }

        @Override
        public void inHand(boolean is, Item i) {
            Storage s = (Storage) i.itemData;
            if (!is) {
                PlayerInventory.inputActive = true;
                s.open = false;
                if (s.heldItem != null) {
                    s.heldItem.drop(Astro.astro.player.x + Utils.randomRange(0, Astro.astro.player.width), Astro.astro.player.y + Astro.astro.player.height);
                    s.heldItem = null;
                }
            }
        }

        public void handleClick(Item i) {
            Storage s = (Storage) i.itemData;
            boolean hit = true;
            boolean hit2 = false;
            if (!s.open) return;
            int paddingX = PlayerInventory.paddingX;
            int paddingY = PlayerInventory.paddingY;
            float guiScale = PlayerInventory.guiScale;

            int x = Astro.app.getInput().getMouseX();
            int y = Astro.app.getInput().getMouseY();

            // Handling slots in PlayerInventory
            if (x >= paddingX + 19 * guiScale && y >= paddingY + 1 * guiScale &&
                    x <= paddingX + (19 + 16) * guiScale && y <= paddingY + (1 + 16) * guiScale) {
                if (s.heldItem == null) {
                    s.heldItem = PlayerInventory.slot1;
                    PlayerInventory.slot1 = null;
                } else {
                    Item temp = PlayerInventory.slot1;
                    PlayerInventory.slot1 = s.heldItem;
                    s.heldItem = temp;
                }
            } else if (x >= paddingX + 1 * guiScale && y >= paddingY + 19 * guiScale &&
                    x <= paddingX + (1 + 16) * guiScale && y <= paddingY + (19 + 16) * guiScale) {
                if (s.heldItem == null) {
                    s.heldItem = PlayerInventory.slot2;
                    PlayerInventory.slot2 = null;
                } else {
                    Item temp = PlayerInventory.slot2;
                    PlayerInventory.slot2 = s.heldItem;
                    s.heldItem = temp;
                }
            } else if (x >= paddingX + 19 * guiScale && y >= paddingY + 19 * guiScale &&
                    x <= paddingX + (19 + 16) * guiScale && y <= paddingY + (19 + 16) * guiScale) {
                if (s.heldItem == null) {
                    s.heldItem = PlayerInventory.slot3;
                    PlayerInventory.slot3 = null;
                } else {
                    Item temp = PlayerInventory.slot3;
                    PlayerInventory.slot3 = s.heldItem;
                    s.heldItem = temp;
                }
            } else hit = false;

            // Handling items in the bag inventory
            int startX = (int) (Astro.astro.camera.width - paddingX - (float) s.bagItems.length / 2 * 18 * guiScale); // Adjusted starting X for bag items
            int offsetX = 1;
            int offsetY = 1;

            for (int j = 0; j < s.bagItems.length; j++) {
                // Calculate position in grid
                int itemX = (int) (startX + (offsetX * guiScale));
                int itemY = (int) (paddingY + (offsetY * guiScale));

                // Check if click is within the bounds of this bag item
                if (x >= itemX && y >= itemY && x <= itemX + (16 * guiScale) && y <= itemY + (16 * guiScale)) {
                    if (s.heldItem == null) {
                        // Pick up the item
                        s.heldItem = s.bagItems[j];
                        s.bagItems[j] = null;
                    } else {
                        // Swap with held item
                        Item temp = s.bagItems[j];
                        s.bagItems[j] = s.heldItem;
                        s.heldItem = temp;
                    }
                    hit2 = true;
                    break;
                }

                // Update grid position for the next item
                offsetX += 18;
                if (j == s.bagItems.length / 2 - 1) { // Move to next row halfway through
                    offsetX = 1;
                    offsetY += 18;
                }
            }
            if (!hit && !hit2 && s.heldItem != null) {
                s.heldItem.drop(Astro.astro.player.x + Utils.randomRange(0, Astro.astro.player.width), Astro.astro.player.y + Astro.astro.player.height);
                s.heldItem = null;
            }
        }

        @Override
        public Object save(Item i) {
            Storage s = (Storage) i.itemData;
            List<Item.ItemData> data = new ArrayList<>();
            for (int j = 0; j < s.bagItems.length; j++) {
                data.add(s.bagItems[j] == null ? null : new Item.ItemData(s.bagItems[j].x, s.bagItems[j].y, s.bagItems[j].it.name, s.bagItems[j].it.itemEvents.save(s.bagItems[j]), s.bagItems[j].physical));
            }
            return data;
        }

        @Override
        public Object load(Item i, Object data) {
            Storage s = new Storage();
            List<Item.ItemData> id = (List<Item.ItemData>) data;
            for (int j = 0; j < id.size(); j++) {
                if (id.get(j) == null) continue;
                s.bagItems[j] = new Item(Items.items.get(id.get(j).name()));
                s.bagItems[j].itemData = s.bagItems[j].it.itemEvents.load(s.bagItems[j], id.get(j).data());
            }
            return s;
        }

        @Override
        public String getInstructions(Item i) {
            return "[LMB] to open / close bag\n[RMB] to move items in bag";
        }
    }

    private static class Storage implements Serializable {
        private final transient Item[] bagItems = new Item[8];
        private transient Item heldItem;
        private boolean open = false;
    }
}