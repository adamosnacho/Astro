package org.astro.core.breakabletiles;

import org.astro.core.*;
import org.astro.core.particlesystem.Particle;
import org.astro.core.particlesystem.ParticleGroup;
import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;
import org.newdawn.slick.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FireTile extends BreakableTile implements Save {
    private final SpriteSheet spriteSheet;

    public static final float animationSpeed = ClassSettings.loadFloat("fire tile/animation speed", 150);
    private int animationFrame = 0;
    private int time = 0;

    private boolean playerInFire = false;

    private final List<Enemy> hitEnemies = new ArrayList<>();
    private int hitTime = 0;
    private final static int hitInterval = ClassSettings.loadInt("fire tile/hit interval", 1600);

    private int timeLeft = 30000;

    public FireTile(float x, float y) {
        super(x, y, null);

        try {
            spriteSheet = new SpriteSheet("art/png/fire.png", 16, 16);
            spriteSheet.setFilter(Image.FILTER_NEAREST);
            sprite = spriteSheet.getSprite(0, 0);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        breakable = false;

        Astro.astro.spawn(this);
        Saving.save.add(this);
    }

    public FireTile(Object o) {
        super(((Data) o).x, ((Data) o).y, null);

        timeLeft = ((Data) o).timeLeft;
        try {
            spriteSheet = new SpriteSheet("art/png/fire.png", 16, 16);
            spriteSheet.setFilter(Image.FILTER_NEAREST);
            sprite = spriteSheet.getSprite(0, 0);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        breakable = false;

        Astro.astro.spawn(this);
        Saving.save.add(this);
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(spriteSheet.getSprite(0, animationFrame).getScaledCopy(100, 100), x, y);
    }

    @Override
    public void update() {
        timeLeft -= Astro.delta;
        if (timeLeft <= 0) {
            breakTile();
            new AshTile(x, y);
            return;
        }
        time += Astro.delta;
        if (time >= animationSpeed) {
            time = 0;
            animationFrame++;
            if (animationFrame > 3) animationFrame = 0;
        }
        hitTime += Astro.delta;
        if (hitTime >= hitInterval) {
            hitEnemies.clear();
            hitTime = 0;
        }

        // Player interaction with fire
        if (x < Astro.astro.player.x + Astro.astro.player.width && x + width > Astro.astro.player.x &&
                y < Astro.astro.player.y + Astro.astro.player.height && y + height > Astro.astro.player.y) {
            Astro.astro.player.suitWear -= 0.1f * Astro.delta;
            Astro.astro.camera.shake(1f, 0.1f);
            playerInFire = true;
        } else if (playerInFire) {
            playerInFire = false;
            Astro.astro.camera.stopShake();
        }

        // Check for enemies in contact with fire
        for (Enemy e : Enemy.enemies) {
            if (x < e.x + e.width && x + width > e.x && y < e.y + e.height && y + height > e.y) {
                if (hitEnemies.contains(e)) continue;

                // Reduce enemy health and set on fire status
                e.hp -= 20;
                e.onFire = true;

                // Spawn particles at the enemy's location
                ParticleGroup pg = new ParticleGroup(e.x + e.width / 2f, e.y + e.height / 2f, true);
                int vel = 1000;
                pg.play(() -> {
                    Particle pa = new Particle(pg, 0, 0, Utils.randomRange(-vel, vel), Utils.randomRange(-vel, vel),
                            (Utils.randomRange(0, 2) == 1 ? Color.orange : e.hitColor), 0.0005f, 5);
                    pa.physicsProperties(0.3f, 0, 5);
                    pa.setSize(20, 20);
                    return pa;
                }, 10);

                // Add enemy to hit list
                hitEnemies.add(e);
            } else if (e.onFire) {
                e.onFire = false;
            }
        }
    }

    @Override
    public Object save() {
        return new Data(x, y, timeLeft);
    }

    @Override
    public void deSpawn() {
        Saving.save.remove(this);
    }

    @Override
    public String getInfo() {
        return "Fire\nBurns out: " + timeLeft / 1000 + "s";
    }

    private record Data(float x, float y, int timeLeft) implements Serializable { }
}
