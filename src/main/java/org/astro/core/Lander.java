package org.astro.core;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

public class Lander extends Entity implements TipEntity, Serializable {
    public final int width = 300;
    public final int height = 300;
    private GamePanel gp;
    private transient BufferedImage sprite;
    private Timer t;
    private int frame = 1;

    public Lander(GamePanel gp, int x, int y) {
        this.x = x;
        this.y = y;
        this.gp = gp;
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/art/png/lander.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        t = new Timer(500, e -> {frame++; if (frame == 4) t.stop();});
        t.start();
        gp.tipManager.tipEntities.add(this);
        gp.lighting.addLightSource(x, y, 600, 0.9f);
        gp.player.addCollisionLine(new Vector2(x + 75, y + height), width - 150);
    }

    @Override
    public void draw(Graphics g) {
        if (gp.player.drawOver(y, height) && !delayedDraw) {
            gp.delayEntityDraw();
            return;
        }
        // Draw the sprite image
        int spriteY = frame * height;
        g.drawImage(sprite, this.x, this.y, this.x + width, this.y + height, 0, spriteY, width, spriteY + height, null);
    }

    @Override
    public Vector2 getSize() {
        return new Vector2(width, height);
    }

    @Override
    public String getDisplay() {
        return "The asteroid hit a few crucial components of the lander.\nRepair it to return to earth.";
    }

    @Override
    public String getName() {
        return "Crashed Lander";
    }
}
