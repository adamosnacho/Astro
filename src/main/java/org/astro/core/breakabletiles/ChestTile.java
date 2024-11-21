package org.astro.core.breakabletiles;

import org.astro.core.*;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.Items;
import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChestTile extends BreakableTile implements Save {
    private final Image spriteOpen;
    private final Image slots;
    private final Storage s;

    public ChestTile(float x, float y) {
        super(x, y, Items.items.get("chest"));
        try {
            slots = new Image("art/png/gui/chestSlots.png", false, Image.FILTER_NEAREST).getScaledCopy(PlayerInventory.guiScale);
            slots.setAlpha(0.5f);
            sprite = new Image("art/png/chest.png", false, Image.FILTER_NEAREST).getScaledCopy(width, height);
            spriteOpen = new Image("art/png/chestOpened.png", false, Image.FILTER_NEAREST).getScaledCopy(width, height);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        this.s = new Storage();
        Astro.astro.spawn(this);
        Saving.save.add(this);
    }

    public ChestTile(Object o) {
        super(((ChestSave) o).x, ((ChestSave) o).y, Items.items.get("chest"));
        try {
            slots = new Image("art/png/gui/chestSlots.png", false, Image.FILTER_NEAREST).getScaledCopy(PlayerInventory.guiScale);
            slots.setAlpha(0.5f);
            sprite = new Image("art/png/chest.png", false, Image.FILTER_NEAREST).getScaledCopy(width, height);
            spriteOpen = new Image("art/png/chestOpened.png", false, Image.FILTER_NEAREST).getScaledCopy(width, height);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        this.s = new Storage();
        Astro.astro.spawn(this);
        Saving.save.add(this);

        ChestSave cs = (ChestSave) o;
        List<Item.ItemData> contents = cs.contents;

        for (int i = 0; i < s.bagItems.length; i++) {
            if (contents.get(i) == null) continue;
            s.bagItems[i] = new Item(contents.get(i));
        }
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(s.open ? spriteOpen : sprite, x, y);
    }

    @Override
    public void gui(Graphics g) {
        if (!s.open) return;
        int paddingX = PlayerInventory.paddingX;
        int paddingY = PlayerInventory.paddingY;
        float guiScale = PlayerInventory.guiScale;

        int x = Astro.app.getInput().getMouseX();
        int y = Astro.app.getInput().getMouseY();

        // Draw chest slots and title
        g.drawImage(slots, Astro.astro.camera.width - paddingX - slots.getWidth(), paddingY);
        g.drawString("Chest", Astro.astro.camera.width - paddingX - slots.getWidth(), paddingY - g.getFont().getHeight("Bag"));

        int startX = (int) (Astro.astro.camera.width - paddingX - slots.getWidth());
        int offsetX = 1;
        int offsetY = 1;

        // Render items in 4x4 grid (16 slots)
        for (int j = 0; j < s.bagItems.length; j++) {
            if (s.bagItems[j] != null) {
                g.drawImage(s.bagItems[j].sprite.getScaledCopy(16, 16).getScaledCopy(guiScale),
                        startX + offsetX * guiScale, paddingY + offsetY * guiScale);
            }

            offsetX += 18; // Move to the next slot horizontally

            // Move to the next row after every 4 items (for 4x4 grid)
            if ((j + 1) % 4 == 0) {
                offsetX = 1;
                offsetY += 18;
            }
        }

        // Render the held item
        if (s.heldItem != null) {
            g.drawImage(s.heldItem.sprite.getScaledCopy(40, 40), x - 20, y - 20);
            g.setColor(Color.white);
            g.drawString(s.heldItem.it.itemEvents.getStatus(s.heldItem), x - 20, y - 20 - g.getFont().getHeight(s.heldItem.it.itemEvents.getStatus(s.heldItem)));
        }
    }


    @Override
    public void update() {
        if (Input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)) handleClick();
        if (s.open) {
            PlayerInventory.inputActive = false;
            Astro.astro.player.canMove = false;
        }
    }

    public void handleClick() {
        boolean hit = true;
        boolean hit2 = false;
        if (!s.open) return;
        int paddingX = PlayerInventory.paddingX;
        int paddingY = PlayerInventory.paddingY;
        float guiScale = PlayerInventory.guiScale;

        int x = Astro.app.getInput().getMouseX();
        int y = Astro.app.getInput().getMouseY();

        if (x >= paddingX + 1 * guiScale && y >= paddingY + 1 * guiScale &&
                x <= paddingX + (1 + 16) * guiScale && y <= paddingY + (1 + 16) * guiScale) {
            if (s.heldItem == null) {
                s.heldItem = PlayerInventory.hand;
                PlayerInventory.hand = null;
            } else {
                Item temp = PlayerInventory.hand;
                PlayerInventory.hand = s.heldItem;
                s.heldItem = temp;
            }
        } else if (x >= paddingX + 19 * guiScale && y >= paddingY + 1 * guiScale &&
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

        // Handling items in the 4x4 bag inventory grid
        int startX = (int) (Astro.astro.camera.width - paddingX - 4 * 18 * guiScale); // Adjusted for 4 columns
        int offsetX = 1;
        int offsetY = 1;

        for (int j = 0; j < s.bagItems.length; j++) {
            int itemX = (int) (startX + (offsetX * guiScale));
            int itemY = (int) (paddingY + (offsetY * guiScale));

            // Check if click is within the bounds of this bag item
            if (x >= itemX && y >= itemY && x <= itemX + (16 * guiScale) && y <= itemY + (16 * guiScale)) {
                if (s.heldItem == null) {
                    s.heldItem = s.bagItems[j];
                    s.bagItems[j] = null;
                } else {
                    Item temp = s.bagItems[j];
                    s.bagItems[j] = s.heldItem;
                    s.heldItem = temp;
                }
                hit2 = true;
                break;
            }

            offsetX += 18; // Move to the next slot horizontally

            // Move to the next row after every 4 items (for 4x4 grid)
            if ((j + 1) % 4 == 0) {
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
    public void onInteract() {
        if (s.open) {
            s.open = false;
            PlayerInventory.inputActive = true;
            Astro.astro.player.canMove = true;
            if (s.heldItem != null) {
                s.heldItem.drop(Astro.astro.player.x + Utils.randomRange(0, Astro.astro.player.width), Astro.astro.player.y + Astro.astro.player.height);
                s.heldItem = null;
            }
        }
        else if (PlayerInventory.held == null) s.open = true;
    }

    @Override
    public Object onTileBreak() {
        for (Item i : s.bagItems) {
            if (i != null) i.drop(x + Utils.randomRange(0, width - 40), y + Utils.randomRange(0, height - 40));
        }
        return null;
    }

    @Override
    public void deSpawn() {
        Saving.save.remove(this);
    }

    @Override
    public Object save() {
        List<Item.ItemData> contents = new ArrayList<>();

        for (Item i : s.bagItems) {
            contents.add(i == null ? null : new Item.ItemData(i.x, i.y, i.it.name, i.it.itemEvents.save(i), i.physical));
        }

        return new ChestSave(x, y, contents);
    }

    public static class Storage {
        private final Item[] bagItems = new Item[16];
        private Item heldItem;
        private boolean open = false;
    }

    private record ChestSave(float x, float y, List<Item.ItemData> contents) implements Serializable {}
}