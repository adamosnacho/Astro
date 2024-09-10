package org.astro.core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

public class TreasureChest extends Entity implements TipEntity, BreakableTileEntity, Serializable {
    private GamePanel gp;
    private transient BufferedImage sprite;
    private transient BufferedImage spriteOpened;
    public final int width = 100;
    public final int height = 100;
    public boolean opened = false;

    public String[] pool = {"drill", "lead", "aluminum", "lead pickaxe", "rope"};

    public TreasureChest(GamePanel gp, int x, int y) {
        this.x = x;
        this.y = y;
        this.gp = gp;
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
        gp.tipManager.tipEntities.add(this);
        gp.breakableTileEntities.add(this);
    }

    @Override
    public void draw(Graphics g) {
        if (opened) g.drawImage(spriteOpened, x, y, width, height, null);
        else g.drawImage(sprite, x, y, width, height, null);
    }

    @Override
    public void update(double deltaTime) {
        if (opened) return;
        int clickX = gp.cameraX + Input.mouse.getX();
        int clickY = gp.cameraY + Input.mouse.getY();
        if (Input.mouse.getButton() == MouseEvent.BUTTON3) {
            if (clickX > x && clickX < x + width && clickY > y && clickY < y + height) {
                opened = true;
                dropLoot();
            }
        }
    }

    private void dropLoot() {
        drop(pool[gp.terrain.randomRange(0, pool.length - 1)]);
        if (gp.terrain.randomRange(1, 2) == 1) return;
        drop(pool[gp.terrain.randomRange(0, pool.length - 1)]);
    }

    private void drop(String itemName) {
        PhysicalItem drop = Items.getInstance(gp).getPhysical(itemName);
        drop.x = x + gp.terrain.randomRange(0, width);
        drop.y = y + height;
        gp.spawnEntity(drop, 10);
    }

    @Override
    public Vector2 getSize() {
        return new Vector2(width, height);
    }

    @Override
    public String getDisplay() {
        StringBuilder poolList = new StringBuilder();
        for (String pItem : pool) {
            poolList.append("- ").append(pItem).append("\n");
        }

        return "Possibly contains:\n" + poolList;
    }

    @Override
    public String getName() {
        return "Treasure Chest";
    }

    @Override
    public void deSpawn() {
        gp.tipManager.tipEntities.remove(this);
    }

    @Override
    public PhysicalItem getDrop() {
        return Items.getInstance(gp).getPhysical("chest");
    }
}
