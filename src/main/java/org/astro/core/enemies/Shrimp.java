package org.astro.core.enemies;

import org.astro.core.*;
import org.astro.core.particlesystem.Particle;
import org.astro.core.particlesystem.ParticleGroup;
import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;
import org.newdawn.slick.*;

import java.io.Serializable;

public class Shrimp extends Enemy implements Save {
    private final Image sprite;
    private float animationTimer = 0;
    public float animationSpeed = ClassSettings.loadFloat("shrimp/animation speed", 200f); // speed of the walking animation
    public int animationFrame = 0;
    private float shootTimer = 0; // timer for shooting
    private final float shootInterval = ClassSettings.loadFloat("shrimp/shoot interval", 5000f); // 5 seconds in milliseconds

    public Shrimp(float x, float y) {
        width = 192;
        height = 168;
        Astro.astro.spawn(this);
        z = 0;
        try {
            sprite = new Image("art/png/shrimp.png", false, Image.FILTER_NEAREST).getScaledCopy(192, 168);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        this.x = x;
        this.y = y;
        hitColor = new Color(200, 0, 0);
        Saving.save.add(this);
        keepDistance = 400f;
        hp = 400;
        speed = 0.2f;
    }

    public Shrimp(Object o) {
        Data d = (Data) o;
        width = 192;
        height = 168;
        Astro.astro.spawn(this);
        z = 0;
        try {
            sprite = new Image("art/png/shrimp.png", false, Image.FILTER_NEAREST).getScaledCopy(192, 168);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        this.x = d.x;
        this.y = d.y;
        hp = d.hp;
        hitColor = new Color(200, 0, 0);
        Saving.save.add(this);
        keepDistance = 400f;
        speed = 0.2f;
    }

    @Override
    public void update() {
        if (dying) {
            updateDeath();
        } else {
            int sx = (int) x;
            int sy = (int) x;
            pathFind(Astro.astro.player.x, Astro.astro.player.y);
            if (sx != (int) x || sy != (int) y) walkAnimation();

            // Shooting logic
            shootTimer += Astro.delta;
            if (shootTimer >= shootInterval) {
                shootAtPlayer();
                shootTimer = 0;
            }
        }
    }

    private boolean isPlayerInRange(float range) {
        float dx = Astro.astro.player.x - x;
        float dy = (Astro.astro.player.y - y) * 1.8f;
        return Math.sqrt(dx * dx + dy * dy) < range;
    }

    @Override
    public void render(Graphics g) {
        if (!dying) {
            g.drawImage(sprite.getFlippedCopy(facingLeft, false), x, y);
        }
        else renderDie(g, sprite.getFlippedCopy(facingLeft, false));
    }

    private void walkAnimation() {
        animationTimer += Astro.delta;
        if (animationTimer < animationSpeed) return;
        animationFrame++;
        animationFrame = animationFrame > 3 ? 0 : animationFrame;
        animationTimer = 0;
    }

    private void shootAtPlayer() {
        float dx = (Astro.astro.player.x + Astro.astro.player.width / 2f) - (x + width / 2f);
        float dy = (Astro.astro.player.y + Astro.astro.player.height / 2f) - (y + height / 2f);
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0) { // Normalize direction vector
            dx /= length;
            dy /= length;
        }
        new ShrimpProjectile(x + width / 2f, y + height / 2f, dx, dy); // Spawn projectile at Shrimp's position
    }

    @Override
    public Object save() {
        return new Data(x, y, hp);
    }

    @Override
    public void deSpawn() {
        Saving.save.remove(this);
        enemies.remove(this);
    }

    @Override
    public String getInfo() {
        return "Shrimp\nHP " + hp;
    }

    public static class ShrimpProjectile extends Entity {
        private static final float speed = 1f;
        private static final float rotationSpeed = 0.01f;
        private static final float damage = 20f;
        private final float xVel;
        private final float yVel;
        private final Image sprite;

        public ShrimpProjectile(float x, float y, float xDir, float yDir) {
            float length = (float) Math.sqrt(xDir * xDir + yDir * yDir);
            if (length > 0) { // Normalize direction
                xDir /= length;
                yDir /= length;
            }
            this.xVel = xDir * speed;
            this.yVel = yDir * speed;
            this.x = x;
            this.y = y;
            try {
                sprite = new Image("art/png/shrimpLeg.png").getScaledCopy(50, 50);
            } catch (SlickException e) {
                throw new RuntimeException(e);
            }
            width = 50;
            height = 50;
            Astro.astro.spawn(this);
        }

        @Override
        public void update() {
            x += xVel * Astro.delta;
            y += yVel * Astro.delta;
            sprite.rotate(Astro.delta * rotationSpeed);
            if (!(x + width >= Astro.astro.camera.x && x - width <= Astro.astro.camera.x + Astro.astro.camera.width &&
                    y + height >= Astro.astro.camera.y && y - height <= Astro.astro.camera.y + Astro.astro.camera.height)) Astro.astro.deSpawn(this);

            // check for hit player
            Player p = Astro.astro.player;
            if (x >= p.x && x <= p.x + p.width && y >= p.y && y <= p.y + p.height) {
                attackPlayer();
                Astro.astro.deSpawn(this);
            }
        }

        private void attackPlayer() {
            ParticleGroup pg = new ParticleGroup(x, y, true);
            int vel = 300;
            pg.play(() -> {
                Particle pa = new Particle(pg, 0, 0, Utils.randomRange(-vel, vel), Utils.randomRange(-vel, vel),
                        (Utils.randomRange(0, 2) == 1 ? Color.white : Color.lightGray), 0.001f, 1);
                pa.physicsProperties(0.3f, 0, 5);
                pa.setSize(20, 20);
                return pa;
            }, 15);
            Astro.astro.player.suitWear -= damage;
            Astro.astro.camera.shake(5, 0.6f);
        }

        @Override
        public void render(Graphics g) {
            g.drawImage(sprite, x, y);
        }
    }

    private record Data(float x, float y, float hp) implements Serializable {}
}
