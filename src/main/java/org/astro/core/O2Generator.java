package org.astro.core;

import org.astro.core.particles.Particle;
import org.astro.core.particles.ParticleGroup;
import org.astro.core.particles.ParticleSystem;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class O2Generator extends Entity implements TipEntity, BreakableTileEntity, Serializable {
    private final Pole pole;
    private GamePanel gp;
    private transient BufferedImage sprite;
    public final int width = 100;
    public final int height = 100;
    public List<String> data;
    public float durability = 100;
    public float breakingSpeed = 1f;

    public O2Generator(GamePanel gp, int x, int y, List<String> data) {
        this.data = data;
        this.x = x;
        this.y = y;
        this.gp = gp;
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/art/png/o2Generator.png")); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!data.isEmpty()) durability = Float.parseFloat(data.getFirst());
        pole = new Pole(gp, x, y);
        pole.isConnected = true;
        pole.source = pole;
        PoleManager.getInstance().poles.add(pole);
        gp.tipManager.tipEntities.add(this);
    }


    @Override
    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, null);
    }

    @Override
    public void update(double deltaTime) {
        if (durability <= 0) {
            gp.deSpawnEntity(this, 5);
        }
        durability -= (float) (deltaTime * breakingSpeed);
    }

    @Override
    public void deSpawn() {
        gp.tipManager.tipEntities.remove(this);
        PoleManager.getInstance().poles.remove(pole);
    }

    @Override
    public PhysicalItem getDrop() {
        PhysicalItem drop = Items.getInstance(gp).getPhysical("o2 generator");
        drop.data.add(String.valueOf(durability));
        return drop;
    }

    @Override
    public Vector2 getSize() {
        return new Vector2(width, height);
    }

    @Override
    public String getDisplay() {
        return "Generating...\nDurability: " + (int) durability;
    }

    @Override
    public String getName() {
        return "O2 Generator";
    }
}
