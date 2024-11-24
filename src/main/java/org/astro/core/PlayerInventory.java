package org.astro.core;

import org.astro.core.itemsystem.Item;
import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;
import org.newdawn.slick.*;

import java.io.Serializable;

public class PlayerInventory implements Save {
    public static Item hand;
    public static Item slot1;
    public static Item slot2;
    public static Item slot3;
    public static float guiScale = 5;
    public static boolean inputActive = true;
    public static boolean dragMoveSystem = true;
    public static boolean showInstructions = true;
    // slots gui
    public static final int paddingX = 35;
    public static final int paddingY = 70;
    public static final float suitWearWidth = 50;
    public static final float suitWearHeight = 5;
    private static Image slotsSprite;
    private static boolean instantiated = false;

    public static Item held;

    private static final Sound popSfx;
    private static final Sound clickSfx;

    static {
        try {
            popSfx = new Sound("sfx/pop.ogg");
            clickSfx = new Sound("sfx/click.ogg");
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() {
        try {
            slotsSprite = new Image("art/png/gui/slots.png").getScaledCopy(guiScale);
            slotsSprite.setAlpha(0.5f);
            slotsSprite.setFilter(Image.FILTER_NEAREST);
            new PlayerInventory(null);

        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
    }

    public PlayerInventory(Object o) {
        if (!instantiated) Saving.save.add(this);
        instantiated = true;

        if (o == null) return;
        InventoryData data = (InventoryData) o;

        if (data.hand != null) hand = new Item(data.hand);

        if (data.slot1 != null) slot1 = new Item(data.slot1);

        if (data.slot2 != null) slot2 = new Item(data.slot2);

        if (data.slot3 != null) slot3 = new Item(data.slot3);
    }

    public static void draw(Graphics g) {
        if (hand == null) return;
        hand.inventoryRender(g);
    }

    public static void drawGui(Graphics g) {
        int x = Astro.app.getInput().getMouseX();
        int y = Astro.app.getInput().getMouseY();

        g.setColor(new Color(255, 255, 255));
        g.drawString("Equipment", paddingX, paddingY - g.getFont().getLineHeight());
        String coords = (int) (Astro.astro.camera.x + Astro.app.getInput().getMouseX()) / 100 + " , " + (int) (Astro.astro.camera.y + Astro.app.getInput().getMouseY()) / 100;
        g.drawString(coords, (float) paddingX, paddingY - g.getFont().getLineHeight() * 2);
        if (hand != null) g.drawString(hand.it.itemEvents.getStatus(hand), paddingX, paddingY + slotsSprite.getHeight());
        if (hand != null && showInstructions) g.drawString("Instructions:\n" + hand.it.itemEvents.getInstructions(hand), paddingX, paddingY + slotsSprite.getHeight() + g.getFont().getHeight(hand.it.itemEvents.getStatus(hand) + "\n.\n."));

        g.drawImage(slotsSprite, paddingX, paddingY);
        if (hand != null) g.drawImage(hand.sprite.getScaledCopy(16, 16).getScaledCopy(guiScale), paddingX + 1 * guiScale, paddingY + 1 * guiScale);
        if (slot1 != null) g.drawImage(slot1.sprite.getScaledCopy(16, 16).getScaledCopy(guiScale), paddingX + 19 * guiScale, paddingY + 1 * guiScale);
        if (slot2 != null) g.drawImage(slot2.sprite.getScaledCopy(16, 16).getScaledCopy(guiScale), paddingX + 1 * guiScale, paddingY + 19 * guiScale);
        if (slot3 != null) g.drawImage(slot3.sprite.getScaledCopy(16, 16).getScaledCopy(guiScale), paddingX + 19 * guiScale, paddingY + 19 * guiScale);
        if (held != null) g.drawImage(held.sprite.getScaledCopy(40, 40), x - 20, y - 20);

        g.drawString("Suit Wear", paddingX, Astro.astro.camera.height - paddingY - suitWearHeight - g.getFont().getLineHeight());

        g.setColor(new Color(102, 102, 102, 200));
        g.fillRect(paddingX, Astro.astro.camera.height - paddingY - suitWearHeight, suitWearWidth * guiScale, suitWearHeight * guiScale);
        float suitWearPercent = Astro.astro.player.suitWear / 200;
        // Smooth transition from red (low wear) to green (full wear)
        int red = (int) ((1 - suitWearPercent) * 255);
        int green = (int) (suitWearPercent * 255);

        g.setColor(new Color(red, green, 102));
        g.fillRect(paddingX, Astro.astro.camera.height - paddingY - suitWearHeight, (Astro.astro.player.suitWear / 200) * suitWearWidth * guiScale, suitWearHeight * guiScale);


        g.setColor(Color.white);
        g.drawString("Oxygen Left", paddingX * 2 + suitWearWidth * guiScale, Astro.astro.camera.height - paddingY - suitWearHeight - g.getFont().getLineHeight());

        g.setColor(new Color(102, 102, 102, 200));
        g.fillRect(paddingX * 2 + suitWearWidth * guiScale, Astro.astro.camera.height - paddingY - suitWearHeight, suitWearWidth * guiScale, suitWearHeight * guiScale);
        float o2Percent = Astro.astro.player.o2 / 200;
        // Smooth transition from red (low wear) to green (full wear)
        red = (int) ((1 - o2Percent) * 255);
        green = (int) (o2Percent * 255);

        g.setColor(new Color(red, green, 102));
        g.fillRect(paddingX * 2 + suitWearWidth * guiScale, Astro.astro.camera.height - paddingY - suitWearHeight, (Astro.astro.player.o2 / 200) * suitWearWidth * guiScale, suitWearHeight * guiScale);
        if (hand != null && dragMoveSystem) hand.it.itemEvents.gui(g, hand);

        Console.drawConsole(g);
    }

    private static void moveOnClick() {
        if (held != null && dragMoveSystem) Astro.astro.player.canMove = false;
        if (!Input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)) return;

        // Toggle between the direct swap and bag-style system based on the boolean
        if (dragMoveSystem) {
            dragMove(); // New method for bag-style handling
        } else {
            moveOnClickDirectSwap(); // Original method for direct swap
        }
    }

    private static void moveOnClickDirectSwap() {
        int x = Astro.app.getInput().getMouseX();
        int y = Astro.app.getInput().getMouseY();

        if (x >= paddingX + 1 * guiScale && y >= paddingY + 1 * guiScale &&
                x <= paddingX + (1 + 16) * guiScale && y <= paddingY + (1 + 16) * guiScale) {
            drop();
        } else if (x >= paddingX + 19 * guiScale && y >= paddingY + 1 * guiScale &&
                x <= paddingX + (19 + 16) * guiScale && y <= paddingY + (1 + 16) * guiScale) {
            swapHandWithSlot(slot1);
        } else if (x >= paddingX + 1 * guiScale && y >= paddingY + 19 * guiScale &&
                x <= paddingX + (1 + 16) * guiScale && y <= paddingY + (19 + 16) * guiScale) {
            swapHandWithSlot(slot2);
        } else if (x >= paddingX + 19 * guiScale && y >= paddingY + 19 * guiScale &&
                x <= paddingX + (19 + 16) * guiScale && y <= paddingY + (19 + 16) * guiScale) {
            swapHandWithSlot(slot3);
        }
    }

    private static void dragMove() {
        int x = Astro.app.getInput().getMouseX();
        int y = Astro.app.getInput().getMouseY();

        if (x >= paddingX + 1 * guiScale && y >= paddingY + 1 * guiScale &&
                x <= paddingX + (1 + 16) * guiScale && y <= paddingY + (1 + 16) * guiScale) {
            if (hand != null) hand.it.itemEvents.inHand(false, hand);
            Item temp = held;
            held = hand;
            hand = temp;
            if (hand != null) hand.it.itemEvents.inHand(true, hand);
            if (held == null) Astro.astro.player.canMove = true;
            clickSfx.play(0.9f + Utils.randomRange(0, 10) / 100f, 0.5f);
        } else if (x >= paddingX + 19 * guiScale && y >= paddingY + 1 * guiScale &&
                x <= paddingX + (19 + 16) * guiScale && y <= paddingY + (1 + 16) * guiScale) {
            if (slot1 != null) slot1.it.itemEvents.inHand(false, slot1);
            Item temp = held;
            held = slot1;
            slot1 = temp;
            if (slot1 != null) slot1.it.itemEvents.inHand(true, slot1);
            if (held == null) Astro.astro.player.canMove = true;
            clickSfx.play(0.9f + Utils.randomRange(0, 10) / 100f, 0.5f);
        } else if (x >= paddingX + 1 * guiScale && y >= paddingY + 19 * guiScale &&
                x <= paddingX + (1 + 16) * guiScale && y <= paddingY + (19 + 16) * guiScale) {
            if (slot2 != null) slot2.it.itemEvents.inHand(false, slot2);
            Item temp = held;
            held = slot2;
            slot2 = temp;
            if (slot2 != null) slot2.it.itemEvents.inHand(true, slot2);
            if (held == null) Astro.astro.player.canMove = true;
            clickSfx.play(0.9f + Utils.randomRange(0, 10) / 100f, 0.5f);
        } else if (x >= paddingX + 19 * guiScale && y >= paddingY + 19 * guiScale &&
                x <= paddingX + (19 + 16) * guiScale && y <= paddingY + (19 + 16) * guiScale) {
            if (slot3 != null) slot3.it.itemEvents.inHand(false, slot3);
            Item temp = held;
            held = slot3;
            slot3 = temp;
            if (slot3 != null) slot3.it.itemEvents.inHand(true, slot3);
            if (held == null) Astro.astro.player.canMove = true;
            clickSfx.play(0.9f + Utils.randomRange(0, 10) / 100f, 0.5f);
        } else if (held != null) {
            held.drop(Astro.astro.player.x + Utils.randomRange(0, Astro.astro.player.width), Astro.astro.player.y + Astro.astro.player.height);
            held = null;
            Astro.astro.player.canMove = true;
            popSfx.play(0.7f + Utils.randomRange(0, 20) / 100f, 0.5f);
        }
    }

    private static void moveOnNum() {
        if (Astro.app.getInput().isKeyPressed(Input.KEY_1)) {
            swapHandWithSlot(slot1);
        } else if (Astro.app.getInput().isKeyPressed(Input.KEY_2)) {
            swapHandWithSlot(slot2);
        } else if (Astro.app.getInput().isKeyPressed(Input.KEY_3)) {
            swapHandWithSlot(slot3);
        }
    }

    private static void swapHandWithSlot(Item slot) {
        if (slot != null) {
            Item temp = hand;
            hand = slot;
            if (slot == slot1) {
                if (temp != null) temp.it.itemEvents.inHand(false, temp);
                slot1 = temp;
            } else if (slot == slot2) {
                if (temp != null) temp.it.itemEvents.inHand(false, temp);
                slot2 = temp;
            } else if (slot == slot3) {
                if (temp != null) temp.it.itemEvents.inHand(false, temp);
                slot3 = temp;
            } else return;
            if (hand != null) hand.it.itemEvents.inHand(true, hand);
        }
    }

    public static void update() {
        Console.console();
        if (inputActive) {
            moveOnClick();
            moveOnNum();
        }

        if (hand == null) return;
        hand.inventoryUpdate();
        if (Astro.app.getInput().isKeyDown(Input.KEY_Q) && inputActive) drop();
    }

    public static boolean pickUp(Item i) {
        if (hand == null) {
            hand = i;
            hand.it.itemEvents.inHand(true, hand);
            popSfx.play(0.7f + Utils.randomRange(0, 20) / 100f, 0.5f);
            return true;
        }
        if (slot1 == null) {
            slot1 = i;
            popSfx.play(0.7f + Utils.randomRange(0, 20) / 100f, 0.5f);
            return true;
        }
        if (slot2 == null) {
            slot2 = i;
            popSfx.play(0.7f + Utils.randomRange(0, 20) / 100f, 0.5f);
            return true;
        }
        if (slot3 == null) {
            slot3 = i;
            popSfx.play(0.7f + Utils.randomRange(0, 20) / 100f, 0.5f);
            return true;
        }
        return false;
    }

    public static void drop() {
        if (hand != null) {
            hand.drop(Astro.astro.player.x + Utils.randomRange(0, Astro.astro.player.width), Astro.astro.player.y + Astro.astro.player.height);
            hand = null;
            popSfx.play(0.7f + Utils.randomRange(0, 20) / 100f, 0.5f);
        }
    }

    @Override
    public Object save() {
        return new InventoryData(hand == null ? null : new Item.ItemData(hand.x, hand.y, hand.it.name, hand.it.itemEvents.save(hand), hand.physical),
                slot1 == null ? null : new Item.ItemData(slot1.x, slot1.y, slot1.it.name, slot1.it.itemEvents.save(slot1), slot1.physical),
                slot2 == null ? null : new Item.ItemData(slot2.x, slot2.y, slot2.it.name, slot2.it.itemEvents.save(slot2), slot2.physical),
                slot3 == null ? null : new Item.ItemData(slot3.x, slot3.y, slot3.it.name, slot3.it.itemEvents.save(slot3), slot3.physical)
                );
    }

    private record InventoryData(Item.ItemData hand, Item.ItemData slot1, Item.ItemData slot2, Item.ItemData slot3) implements Serializable {}
}
