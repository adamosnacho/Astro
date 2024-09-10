package org.astro.core;

import org.astro.core.particles.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

public class Arrow extends Entity implements Serializable {
    public transient BufferedImage sprite;
    public final boolean facingRight;
    public float xVel;
    public int damage = 50;
    public final int width = 100;
    public final int height = 100;
    private GamePanel gp;
    private Timer deSpawn;

    public Arrow(GamePanel gp, boolean facingRight, int x, int y) {
        this.x = x;
        this.y = y;
        this.gp = gp;
        this.facingRight = facingRight;
        xVel = facingRight ? 2000 : -2000;
        try {
            sprite = facingRight ? ImageIO.read(getClass().getResourceAsStream("/art/png/ArrowFlight.png"))
                    : getFlippedImage(ImageIO.read(getClass().getResourceAsStream("/art/png/ArrowFlight.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        deSpawn = new Timer(2000, e -> drop());
        deSpawn.start();
    }

    private void drop() {
        Entity arrow = Items.getInstance(gp).getPhysical("arrow");
        arrow.x = x;
        arrow.y = y;
        gp.spawnEntity(arrow, 10);
        gp.deSpawnEntity(this, 9);
        deSpawn.stop();
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(sprite, x, y - height / 2, width, height, null);
    }

    @Override
    public void update(double deltaTime) {
        int startX = x;
        x += (int) (xVel * deltaTime);  // Move arrow based on velocity and deltaTime

        // Check collision with solid tiles
        if (gp.terrain.isColliding(x, y, width, height)) {
            // Stop arrow movement and trigger an effect (like sticking to the wall)
            onTileCollision();
            return;  // Exit the update method after a collision
        }

        // Check collision with enemies
        for (Entity e : gp.enemies) {
            EnemyEntity ee = (EnemyEntity) e;

            // Loop through the positions the arrow travels between startX and the new x position
            for (int xOffset = 0; xOffset < Math.abs(x - startX); xOffset++) {
                int collisionX = facingRight ? startX + xOffset : startX - xOffset;

                // Check if the arrow's current position is within the bounds of the enemy's hitbox
                if (collisionX >= ee.x && collisionX <= ee.x + ee.width && y >= ee.y && y <= ee.y + ee.height) {
                    ee.damage(damage, facingRight);
                    ParticleGroup pg = new ParticleGroup(x + (facingRight ? width : 0), y);
                    pg.generateParticles(() -> new Particle(pg, 0, 0, gp.terrain.randomRange(-30, 30), gp.terrain.randomRange(-30, 30), Color.WHITE, 10, 0.3f, 700), 10);
                    ParticleSystem.particleGroups.add(pg);
                    gp.deSpawnEntity(this, 9);
                    deSpawn.stop();
                    return;
                }
            }
        }
    }

    private void onTileCollision() {
        ParticleGroup pg = new ParticleGroup(x + (facingRight ? width : 0), y);
        pg.generateParticles(() -> new Particle(pg, 0, 0, gp.terrain.randomRange(-70, 70), gp.terrain.randomRange(-70, 70), Color.WHITE, 10, 0.3f, 700), 5);
        ParticleSystem.particleGroups.add(pg);
        drop();
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
}
