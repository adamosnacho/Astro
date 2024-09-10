package org.astro.core;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import static org.astro.core.PoleManager.gp;

public class InventoryItem implements Serializable {
    public final String imagePath;
    public transient BufferedImage sprite;
    public String itemName;
    public int width = 40;
    public int height = 40;
    public transient UpdateListener updLst;
    public OnUse onUse;
    public boolean placeable;
    public boolean customDraw = false;
    public boolean isBreakingTool = false;
    public List<String> data = new ArrayList<>();
    public List<List<String>> secondData = new ArrayList<>();
    public boolean inHand = false;

    public InventoryItem(String itemName, String imagePath, boolean placeable) {
        this.itemName = itemName;
        this.imagePath = imagePath;
        this.placeable = placeable;
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream(imagePath)); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Copy constructor
    public InventoryItem(InventoryItem item) {
        this.itemName = item.itemName;
        this.imagePath = item.imagePath;
        this.updLst = item.updLst;
        this.placeable = item.placeable;
        this.customDraw = item.customDraw;
        this.isBreakingTool = item.isBreakingTool;
        this.onUse = item.onUse;
        this.data.addAll(item.data);
        this.secondData.addAll(item.secondData);
        try {
            this.sprite = ImageIO.read(getClass().getResourceAsStream(item.imagePath)); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InventoryItem copy() {
        return new InventoryItem(this);
    }
    public void draw(Graphics g, Player p) {
        if (customDraw) {
            updLst.draw(g, gp, this);
            return;
        }

        int yOffset = switch (p.animationFrame) {
            case 1, 2 -> -5;
            default -> 0;
        };
        if (p.facingRight) {
            g.drawImage(sprite, p.x + p.width - width, p.y + p.height / 2 + yOffset, width, height, null);
        } else {
            g.drawImage(sprite, p.x, p.y + p.height / 2 + yOffset, width, height, null);
        }
    }
    public void update(GamePanel gp, double deltaTime) {
        if (updLst != null) updLst.update(this, gp, deltaTime);
    }
}
