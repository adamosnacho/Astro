package org.astro.core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

public class MetalRod extends Entity implements BreakableTileEntity, Serializable {
    private GamePanel gp;
    private transient BufferedImage sprite;
    public final int width = 100;
    public final int height = 100;

    public MetalRod(GamePanel gp, int x, int y) {
        this.x = x;
        this.y = y;
        this.gp = gp;
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/art/png/MetalRodGround.png")); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }
        gp.spawnEntity(this, 5);
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, null);
    }


    @Override
    public PhysicalItem getDrop() {
        return Items.getInstance(gp).getPhysical("metal rod");
    }
}
