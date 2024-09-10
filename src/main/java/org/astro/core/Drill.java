package org.astro.core;

import org.astro.core.particles.Particle;
import org.astro.core.particles.ParticleGroup;
import org.astro.core.particles.ParticleSystem;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.*;
import java.io.Serializable;
import java.util.List;

public class Drill extends Entity implements BreakableTileEntity, TipEntity, Serializable {
    private GamePanel gp;
    private transient BufferedImage sprite;
    public final int width = 100;
    public final int height = 100;
    public final int digRate = 20000; // [ms] between drop
    private Timer t;
    private long lastDigTime;
    public String dropName;
    public List<String> data;
    public int durability = 100;

    public Drill(GamePanel gp, int x, int y, List<String> data) {
        this.data = data;
        this.x = x;
        this.y = y;
        this.gp = gp;
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/art/png/drill.png")); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }
        t = new Timer(digRate, s -> dropItem());
        t.start();
        lastDigTime = System.currentTimeMillis();
        gp.tipManager.tipEntities.add(this);

        // Initialize drop
        Vector2 cellPos = gp.terrain.toCell(x, y);
        dropName = Terrain.tileDrops[gp.terrain.getTile((int) cellPos.y, (int) cellPos.x)];
        if (dropName == null) t.stop(); // Stop the timer if there's nothing to dig
        if (!data.isEmpty()) durability = Integer.parseInt(data.getFirst());
    }

    private void dropItem() {
        if (dropName == null) return;
        PhysicalItem drop = Items.getInstance(gp).getPhysical(dropName);
        drop.x = x + gp.terrain.randomRange(0, gp.terrain.tileSize - drop.width);
        drop.y = y + gp.terrain.randomRange(0, gp.terrain.tileSize - drop.height);
        gp.spawnEntity(drop, 10);
        lastDigTime = System.currentTimeMillis(); // Reset the last dig time
        durability -= 10;
        ParticleGroup pg = new ParticleGroup(x + width / 2, y + height / 2);
        pg.generateParticles(() -> {
            int c = gp.terrain.randomRange(100, 150);
            Particle p = new Particle(pg, gp.terrain.randomRange(-(width / 10), width / 10), gp.terrain.randomRange(-(height / 10), height / 10), gp.terrain.randomRange(-5, 5), gp.terrain.randomRange(-5, 5), new Color(c, c, c), 20, 0.1f, 3000);
            p.drag = 0.08f;
            return p;
        }, 30);
        ParticleSystem.particleGroups.add(pg);
        if (durability <= 0) gp.deSpawnEntity(this, 5);
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, null);
    }

    @Override
    public void update(double deltaTime) {
        if (gp.terrain.randomRange(0, 100) == 1 && dropName != null) {
            ParticleGroup pg = new ParticleGroup(x + width / 2, y + height / 2);
            pg.generateParticles(() -> {
                int c = gp.terrain.randomRange(100, 150);
                Particle p = new Particle(pg, gp.terrain.randomRange(-(width / 10), width / 10), gp.terrain.randomRange(-(height / 10), height / 10), gp.terrain.randomRange(-3, 3), gp.terrain.randomRange(-3, 3), new Color(c, c, c), 10, 0.1f, 3000);
                p.drag = 0.08f;
                return p;
            }, 10);
            ParticleSystem.particleGroups.add(pg);
        }
    }

    @Override
    public void deSpawn() {
        t.stop();
        gp.tipManager.tipEntities.remove(this);
    }

    @Override
    public PhysicalItem getDrop() {
        PhysicalItem drop = Items.getInstance(gp).getPhysical("drill");
        drop.data.add(String.valueOf(durability));
        return drop;
    }

    @Override
    public Vector2 getSize() {
        return new Vector2(width, height);
    }

    @Override
    public String getDisplay() {
        if (dropName == null) return "idle"; // Drill is idle if there's nothing to dig
        else {
            long timeElapsedSinceLastDig = System.currentTimeMillis() - lastDigTime;
            float progress = Math.min((float) timeElapsedSinceLastDig / digRate * 100, 100);
            return String.format("%s drill\nProgress: %.0f%%\nDurability: %d%%", dropName, progress, durability);
        }
    }

    @Override
    public String getName() {
        return "Drill";
    }
}
