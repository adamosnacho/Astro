package org.astro.core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import javax.swing.*;

public class Pole extends Entity implements BreakableTileEntity, Serializable {
    private GamePanel gp;
    private transient BufferedImage sprite;
    public final int width = 100;
    public final int height = 100;
    public final int connectionDistance = 1000; // [px] This is 5 blocks
    public boolean isConnected = false;
    public Pole source;

    public Pole(GamePanel gp, int x, int y) {
        this.x = x;
        this.y = y;
        this.gp = gp;
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/art/png/pole.png")); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (source != null && !source.isConnected) {
            source = null;
            isConnected = false;
        }

        // Draw the pole image
        g2.drawImage(sprite, x, y, null);

        // Set the color to white for the connection lines
        g2.setColor(Color.WHITE);
        if (isConnected) g2.setColor(Color.CYAN);

        // Redraw the image to keep the original appearance on top of the tint
        g2.drawImage(sprite, x, y, null);

        for (Pole p : PoleManager.getInstance().poles) {
            if (p == this) continue;
            if (PoleManager.getInstance().distance(this, p) > connectionDistance) continue;
            if (!isConnected && p.isConnected) {
                source = p;
                isConnected = true;
            }
            g2.setStroke(new BasicStroke(5));
            g2.drawLine(x + sprite.getWidth() / 2, y, p.x + p.sprite.getWidth() / 2, p.y);
        }
    }



    @Override
    public void deSpawn() {
        // Update connected poles recursively
        recursivelyUpdateConnection(this);
        PoleManager.getInstance().poles.remove(this);
    }

    private void recursivelyUpdateConnection(Pole removedPole) {
        for (Pole p : PoleManager.getInstance().poles) {
            if (p.source == removedPole) {
                p.isConnected = false;
                p.source = null;
                // Recursively update the poles connected to this pole
                recursivelyUpdateConnection(p);
            }
        }
    }

    @Override
    public PhysicalItem getDrop() {
        return Items.getInstance(gp).getPhysical("pole");
    }
}
