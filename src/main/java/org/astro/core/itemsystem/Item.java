package org.astro.core.itemsystem;

import org.astro.core.*;
import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.io.Serializable;

public final class Item extends Entity implements Save {
    public boolean physical;
    public ItemTemplate it;
    public transient Image sprite;
    public Object itemData = null;

    public Item(ItemTemplate it) {
        z = -1;
        this.physical = false;
        this.it = it;
        width = 40;
        height = 40;
        try {
            sprite = new Image(it.spritePath, false, Image.FILTER_NEAREST).getScaledCopy(width, height);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        it.itemEvents.onInstantiation(this);
        sprite.setFilter(Image.FILTER_NEAREST);
    }

    public Item(ItemTemplate it, float x, float y) {
        z = -1;
        this.physical = true;
        this.x = x;
        this.y = y;
        this.it = it;
        width = 40;
        height = 40;
        try {
            sprite = new Image(it.spritePath).getScaledCopy(width, height);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        Astro.astro.spawn(this);
        it.itemEvents.onInstantiation(this);
        sprite.setFilter(Image.FILTER_NEAREST);
    }

    public Item(Object o) { // load constructor
        ItemData id = (ItemData) o;
        this.physical = id.physical;
        this.it = Items.items.get(id.name);
        this.itemData = it.itemEvents.load(this, id.data);
        this.x = id.x;
        this.y = id.y;

        z = -1;
        width = 40;
        height = 40;
        try {
            sprite = new Image(it.spritePath).getScaledCopy(width, height);
            sprite.setFilter(Image.FILTER_NEAREST);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        if (physical) Astro.astro.spawn(this);
    }

    @Override
    public void render(Graphics g) {
        if (!it.customRender) g.drawImage(sprite, x, y);
        else it.itemEvents.render(g, this);
    }

    public void inventoryUpdate() {
        if (Input.isMousePressed(0)) it.itemEvents.onUse(this);
        it.itemEvents.guiUpdate(this);
    }

    public void inventoryRender(Graphics g) {
        if (!it.customInventoryRender) {
            Player player = Astro.astro.player;
            int oY = -4;
            if (player.animationFrame == 1 || player.animationFrame == 2) oY = -9;
            if (player.facingLeft) g.drawImage(sprite.getFlippedCopy(true, false), player.x, player.y + (float) player.height / 2 + oY);
            else g.drawImage(sprite.getFlippedCopy(false, false), player.x + player.width - width, player.y + (float) player.height / 2 + oY);
        }
        else it.itemEvents.inventoryRender(g, this);
    }

    @Override
    public void onInteract() {
        pickUp();
    }

    @Override
    public void update() {
        it.itemEvents.update(this);
    }

    public void pickUp() {
        if (!physical) return;
        if (!PlayerInventory.pickUp(this)) return;
        Astro.astro.deSpawn(this);
        physical = false;
        it.itemEvents.onPickup(this);
        it.itemEvents.inHand(true, this);
    }

    public void drop(float x, float y) {
        if (physical) return;
        this.x = x;
        this.y = y;
        Astro.astro.spawn(this);
        physical = true;
        it.itemEvents.onDrop(this);
        it.itemEvents.inHand(false, this);
    }

    @Override
    public Object save() {
        return new ItemData(x, y, it.name, it.itemEvents.save(this), physical);
    }

    @Override
    public void spawn() {
        Saving.save.add(this);
    }

    @Override
    public void deSpawn() {
        Saving.save.remove(this);
    }

    public void destroy() {
        Saving.save.remove(this);
        Astro.astro.deSpawn(this);
    }

    public record ItemData(float x, float y, String name, Object data, boolean physical) implements Serializable {}
}
