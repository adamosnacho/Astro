package org.astro.core;

import org.astro.core.particles.Particle;
import org.astro.core.particles.ParticleGroup;
import org.astro.core.particles.ParticleSystem;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

import static java.lang.Math.abs;

public class ShrimpBoss extends EnemyEntity implements TipEntity, Serializable {
    public int speed = 250;
    private Player p;
    private GamePanel gp;
    private int wallHitDir;
    private boolean facingRight = true;
    private transient BufferedImage sprite;
    private int detectionRange = 2000;
    private int minimumRange = 300;
    public final int dmg = 500;
    public int knockBack = 0;

    public ShrimpBoss(GamePanel gp, Player p, int x, int y) {
        height = 140;
        width = 160;
        hp = 1000;
        this.gp = gp;
        this.p = p;
        this.x = x;
        this.y = y;
        new Timer(3000, e -> wallHitDir = gp.terrain.randomRange(-1, 1)).start();
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/art/png/shrimp.png")); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }
        gp.enemies.add(this);
        gp.spawnEntity(this, 10);
    }

    @Override
    public void update(double deltaTime) {
        if (gp.terrain.randomRange(0, (int) (deltaTime * 7000)) == 0) {
            shoot();  // Shoot wind projectile at random intervals
        }

        if (knockBack != 0) {
            moveHorizontally((int) (knockBack * deltaTime * 10), gp.terrain);
            knockBack *= 0.6;
            if (Math.abs(knockBack) < 1) {
                knockBack = 0;
            }
        }

        double dx = (p.x + p.width / 2.0) - (x + width / 2.0);
        double dy = p.y - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > detectionRange) return;

        // Normalize the direction vector
        dx /= length;
        dy /= length;

        // Scale by speed and deltaTime
        double moveX = dx * speed * deltaTime;
        double moveY = dy * speed * deltaTime;

        if (gp.terrain.isColliding((int) (x + moveX), (int) (y + moveY), width, height)) {
            moveX = wallHitDir * speed * deltaTime;
            moveY = wallHitDir * speed * deltaTime;
        }

        if (dx > 0) facingRight = true;
        if (dx < 0) facingRight = false;

        // Move the alien towards the player
        if (length > minimumRange + 10) {
            move(new Vector2(moveX, moveY), gp.terrain);
            return;
        } else if ((length < minimumRange - 10)) {
            move(new Vector2(-moveX, -moveY), gp.terrain);
            return;
        }
    }

    private void shoot() {
        double dx = p.x - x;
        double dy = p.y - y;
        double length = Math.sqrt(dx * dx + dy * dy);
        dx /= length; // Normalize
        dy /= length;

        // Set the velocity of the wind projectile towards the player
        int projectileSpeed = 600;
        int velX = (int) (dx * projectileSpeed);
        int velY = (int) (dy * projectileSpeed);

        new Projectile(velX, velY, x + width / 2, y + height / 2);
    }

    public void move(Vector2 direction, Terrain terrain) {
        int dx = (int) direction.x;
        int dy = (int) direction.y;
        // Attempt to move horizontally and vertically
        if (dx != 0) {
            moveHorizontally(dx, terrain);
        }
        if (dy != 0) {
            moveVertically(dy, terrain);
        }
    }

    private void moveHorizontally(int dx, Terrain terrain) {
        float newX = x + dx;
        if (!terrain.isColliding((int) newX, y, width, height)) {
            x = (int) newX;
        } else {
            newX = x;
            float step = Math.signum(dx); // Step is either 1 or -1
            while (abs(newX - x) < abs(dx)) {
                newX += step;
                if (terrain.isColliding((int) newX, y, width, height)) {
                    break;
                }
                x = (int) newX;
            }
        }
    }

    private void moveVertically(int dy, Terrain terrain) {
        float newY = y + dy;
        if (!terrain.isColliding(x, (int) newY, width, height)) {
            y = (int) newY;
        } else {
            newY = y;
            float step = Math.signum(dy); // Step is either 1 or -1
            while (abs(newY - y) < abs(dy)) {
                newY += step;
                if (terrain.isColliding(x, (int) newY, width, height)) {
                    break;
                }
                y = (int) newY;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!facingRight) g.drawImage(sprite, x, y, null);
        else g.drawImage(getFlippedImage(sprite), x, y, null);
    }

    @Override
    public void deSpawn() {
        gp.enemies.remove(this);
    }

    @Override
    public void damage(int amount, boolean damageRight) {
        hp -= amount;
        knockBack = facingRight ? -300 : 300;
        if (hp <= 0) {
            gp.deSpawnEntity(this, 10);
            PhysicalItem drop = Items.getInstance(gp).getPhysical("blueprint");
            drop.data.add("harpoon");
            drop.x = x + gp.terrain.randomRange(0, width);
            drop.y = y + gp.terrain.randomRange(0, height);
            gp.spawnEntity(drop, 10);

            for (int i = 0; i < 2; i++) {
                drop = Items.getInstance(gp).getPhysical("packed aluminum");
                drop.x = x + gp.terrain.randomRange(0, width);
                drop.y = y + gp.terrain.randomRange(0, height);
                gp.spawnEntity(drop, 10);
            }
        }
    }

    private BufferedImage getFlippedImage(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        // Create a new image with the same dimensions
        BufferedImage flipped = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = flipped.createGraphics();

        // Flip horizontally
        AffineTransform flipTransform = new AffineTransform();
        flipTransform.translate(width, 0);  // Move to the right edge of the image
        flipTransform.scale(-1, 1);  // Flip horizontally
        g2d.drawImage(original, flipTransform, null);
        g2d.dispose();

        return flipped;
    }

    @Override
    public Vector2 getSize() {
        return new Vector2(width, height);
    }

    @Override
    public String getDisplay() {
        return "HP: " + hp + " / 500";
    }

    @Override
    public String getName() {
        return "Shrimp Boss";
    }

    private class Projectile extends Entity {
        BufferedImage legSprite;
        int velX;
        int velY;
        double angle = 0;  // Angle of rotation
        Timer des;

        private Projectile(int velX, int velY, int x, int y) {
            this.velY = velY;
            this.velX = velX;
            this.y = y;
            this.x = x;
            gp.spawnEntity(this, 7);
            des = new Timer(4000, e -> gp.deSpawnEntity(this, 7));
            des.start();
            try {
                legSprite = ImageIO.read(getClass().getResourceAsStream("/art/png/shrimpLeg.png")); // load sprite
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void update(double deltaTime) {
            x += velX * deltaTime;
            y += velY * deltaTime;
            angle += deltaTime * 10 * Math.PI;
            if (gp.terrain.isColliding(x, y, width, height)) {
                gp.deSpawnEntity(this, 7);
                ParticleGroup pg = new ParticleGroup(x, y);
                pg.generateParticles(() -> new Particle(pg, 0, 0, gp.terrain.randomRange(-70, 70), gp.terrain.randomRange(-70, 70), Color.RED, 10, 0.3f, 700), 5);
                ParticleSystem.particleGroups.add(pg);
            }
            if (x >= p.x && x <= p.x + p.width && y >= p.y && y <= p.y +  p.height) {
                gp.deSpawnEntity(this, 7);
                ParticleGroup pg = new ParticleGroup(x, y);
                pg.generateParticles(() -> new Particle(pg, 0, 0, gp.terrain.randomRange(-70, 70), gp.terrain.randomRange(-70, 70), Color.WHITE, 10, 0.3f, 700), 10);
                ParticleSystem.particleGroups.add(pg);
                gp.player.hp -= dmg;
            }
        }

        @Override
        public void draw(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            AffineTransform originalTransform = g2d.getTransform();

            // Translate to the center of the image before rotating
            g2d.translate(x + legSprite.getWidth() / 2, y + legSprite.getHeight() / 2);
            g2d.rotate(angle);

            // Draw the sprite centered at the new origin
            g2d.drawImage(legSprite, -legSprite.getWidth() / 2, -legSprite.getHeight() / 2, null);

            // Reset the transform to avoid affecting other drawings
            g2d.setTransform(originalTransform);
        }

        @Override
        public void deSpawn() {
            des.stop();
        }
    }

}
