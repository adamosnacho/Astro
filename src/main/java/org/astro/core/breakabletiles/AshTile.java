package org.astro.core.breakabletiles;

import org.astro.core.Astro;
import org.astro.core.BreakableTile;
import org.astro.core.itemsystem.Items;
import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.io.Serializable;

public class AshTile extends BreakableTile implements Save {
    public AshTile(float x, float y) {
        super(x, y, Items.items.get("ash"));
        try {
            sprite = new Image("art/png/ashFloor.png").getScaledCopy(width, height);
            sprite.setFilter(Image.FILTER_NEAREST);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        Astro.astro.spawn(this);
        Saving.save.add(this);
    }

    public AshTile(Object o) {
        super(((Data) o).x, ((Data) o).y, Items.items.get("ash"));
        try {
            sprite = new Image("art/png/ashFloor.png").getScaledCopy(width, height);
            sprite.setFilter(Image.FILTER_NEAREST);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        Astro.astro.spawn(this);
        Saving.save.add(this);
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(sprite, x, y);
    }

    @Override
    public void deSpawn() {
        Saving.save.remove(this);
    }

    @Override
    public Object save() {
        return new Data(x, y);
    }
    private record Data(float x, float y) implements Serializable {}
}
