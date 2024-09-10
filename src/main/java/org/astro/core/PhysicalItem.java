package org.astro.core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class PhysicalItem extends Entity implements TipEntity, Serializable {
    public final String imagePath;
    private final GamePanel gp;
    public transient BufferedImage sprite;
    public String itemName;
    public transient CustomDisplay customDisplay;
    public boolean tilePlace;
    public final int width = 40;
    public final int height = 40;
    public List<String> data = new ArrayList<>();
    public List<List<String>> secondData = new ArrayList<>();

    public PhysicalItem(GamePanel gp, String itemName, String imagePath, int x, int y, boolean tilePlace) {
        this.gp = gp;
        this.x = x;
        this.y = y;
        this.itemName = itemName;
        this.imagePath = imagePath;
        this.tilePlace = tilePlace;
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream(imagePath)); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }
        gp.tipManager.tipEntities.add(this);
    }

    private PhysicalItem(PhysicalItem i) {
        this.data.addAll(i.data);
        this.secondData.addAll(i.secondData);
        this.gp = i.gp;
        this.itemName = i.itemName;
        this.x = i.x;
        this.y = i.y;
        this.imagePath = i.imagePath;
        this.customDisplay = i.customDisplay;
        this.tilePlace = i.tilePlace;
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream(imagePath)); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }
        gp.tipManager.tipEntities.add(this);
    }
    public PhysicalItem copy() {
        return new PhysicalItem(this);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Save the current transformation
        AffineTransform originalTransform = g2d.getTransform();

        // Calculate the center of the image
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        g2d.rotate(Math.toRadians(90), centerX, centerY);

        // Draw the image
        g2d.drawImage(sprite, x, y, width, height, null);

        // Restore the original transformation
        g2d.setTransform(originalTransform);
    }

    @Override
    public void update(double deltaTime) {
        if (Input.mouse != null) {
            int clickX = gp.cameraX + Input.mouse.getX();
            int clickY = gp.cameraY + Input.mouse.getY();
            if (Input.mouse.getButton() == MouseEvent.BUTTON3) {
                if (clickX > x && clickX < x + width && clickY > y && clickY < y + height) {
                    InventoryItem pickup = Items.getInstance(gp).getInventory(itemName);
                    pickup.data = data;
                    pickup.secondData = secondData;
                    if (gp.player.pi.pickUp(pickup)) gp.deSpawnEntity(this, 10);
                }
            }
        }
    }

    @Override
    public Vector2 getSize() {
        return new Vector2(width, height);
    }

    @Override
    public String getDisplay() {
        if (Input.keys.contains(KeyEvent.VK_F12)) {
            StringBuilder out = new StringBuilder("Data:");
            for (String datum : data) {
                out.append("\n").append(datum);
            }
            out.append("\nS. Data:");
            for (List<String> secondDatum : secondData) {
                out.append("\n");
                for (String d : secondDatum) {
                    out.append(d).append(", ");
                }
            }
            return out.toString();
        }

        if (customDisplay != null) return customDisplay.get(this);
        return Items.getInstance(gp).itemDescriptions.get(itemName);
    }

    @Override
    public String getName() {
        return itemName + " item";
    }

    @Override
    public void deSpawn() {
        gp.tipManager.tipEntities.remove(this);
    }
}
