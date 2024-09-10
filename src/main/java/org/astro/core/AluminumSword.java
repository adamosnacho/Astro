package org.astro.core;

import org.astro.core.particles.Particle;
import org.astro.core.particles.ParticleGroup;
import org.astro.core.particles.ParticleSystem;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.List;

public class AluminumSword implements UpdateListener, Serializable {
    private double rotation = 90;
    public final double accelerationSwing = 0.9;
    public final double accelerationPrep = 0.7;
    public final double speedReturn = 5;

    // attack stages
    public boolean attackPrep = false;
    public boolean attackSwing = false;
    public boolean attackReturn = false;

    public final int damage = 80; // Sword damage
    private boolean hasHitEnemy = false; // Tracks whether an enemy has been hit in the current swing

    @Override
    public void update(InventoryItem item, GamePanel gp, double deltaTime) {
        if (Input.btn1 && !(attackPrep || attackReturn || attackSwing)) attackPrep = true;

        if (attackPrep) {
            rotation -= rotation * accelerationPrep * deltaTime * 50;
            if (rotation < 1) {
                rotation = 1;
                attackPrep = false;
                attackSwing = true;
                hasHitEnemy = false; // Reset hit tracking for the new swing
            }
        } else if (attackSwing) {
            rotation += rotation * accelerationSwing * deltaTime * 50;
            if (rotation > 180) {
                rotation = 180;
                attackSwing = false;
                attackReturn = true;
            }

            if (!hasHitEnemy) { // Only check for collisions if no enemy has been hit yet
                for (Entity e : gp.enemies) {
                    if (isCollidingWithSword(e, gp.player) && !hasHitEnemy) {
                        ((EnemyEntity) e).damage(damage, gp.player.facingRight);
                        hasHitEnemy = true;

                        // Calculate the collision point for particle spawning
                        Point collisionPoint = calculateCollisionPoint(e, gp.player);
                        spawnParticlesAtCollision(gp, collisionPoint);
                    }
                }
            }

        } else if (attackReturn) {
            rotation -= speedReturn * deltaTime * 50;
            if (rotation <= 90) {
                rotation = 90;
                attackReturn = false;
            }
        }
    }

    private Point calculateCollisionPoint(Entity e, Player p) {
        // Calculate the sword's box collider based on the player's position and facing direction
        int swordWidth = 100;
        int swordHeight = 30;

        int swordX = p.facingRight ? p.x + p.width : p.x - swordWidth;
        int swordY = p.y + p.height / 2 - swordHeight / 2;

        Rectangle swordHitBox = new Rectangle(swordX, swordY, swordWidth, swordHeight);
        Rectangle enemyHitBox = new Rectangle(e.x, e.y, ((EnemyEntity) e).width, ((EnemyEntity) e).height);

        // Calculate the center of the intersection rectangle
        Rectangle intersection = swordHitBox.intersection(enemyHitBox);
        int collisionX = intersection.x + intersection.width / 2;
        int collisionY = intersection.y + intersection.height / 2;

        return new Point(collisionX, collisionY);
    }

    private boolean isCollidingWithSword(Entity e, Player p) {
        // Calculate the sword's box collider based on the player's position and facing direction
        int swordWidth = 100;  // Width of the sword's hitbox
        int swordHeight = 30;  // Height of the sword's hitbox

        int swordX = p.facingRight ? p.x + p.width : p.x - swordWidth;
        int swordY = p.y + p.height / 2 - swordHeight / 2;

        // Use bounding box collision detection
        Rectangle swordHitBox = new Rectangle(swordX, swordY, swordWidth, swordHeight);
        Rectangle enemyHitBox = new Rectangle(e.x, e.y, ((EnemyEntity) e).width, ((EnemyEntity) e).height);

        return swordHitBox.intersects(enemyHitBox);
    }

    private void spawnParticlesAtCollision(GamePanel gp, Point collisionPoint) {
        ParticleGroup pg = new ParticleGroup(collisionPoint.x, collisionPoint.y);
        pg.generateParticles(() -> new Particle(pg, 0, 0, gp.terrain.randomRange(-30, 30), gp.terrain.randomRange(-30, 30), Color.WHITE, 10, 0.3f, 700), 10);
        ParticleSystem.particleGroups.add(pg);
    }

    @Override
    public void draw(Graphics g, GamePanel gp, InventoryItem i) {
        Graphics2D g2d = (Graphics2D) g;
        Player p = gp.player;

        // Scale down the sword to half its size
        int originalWidth = 200;
        int originalHeight = 200;
        i.width = originalWidth / 2;
        i.height = originalHeight / 2;

        // Save the original transform
        AffineTransform originalTransform = g2d.getTransform();

        int yOffset = switch (p.animationFrame) {
            case 1, 2 -> -5;
            default -> 0;
        };

        // Set the rotation around the lower-left corner of the sword image
        if (p.facingRight) {
            g2d.rotate(Math.toRadians(rotation - 90), p.x + p.width - 20, p.y + p.height / 2 + yOffset + 20);
            g2d.drawImage(i.sprite, p.x + p.width - 20, p.y + p.height / 2 + yOffset + 20 - i.height, i.width, i.height, null);
        } else {
            g2d.rotate(Math.toRadians(-rotation + 90), p.x + p.width + 20 - i.width, p.y + p.height / 2 + yOffset + 20);
            g2d.drawImage(getFlippedImage(i.sprite), p.x + p.width + 20 - i.width * 2, p.y + p.height / 2 + yOffset + 20 - i.height, i.width, i.height, null);
        }

        // Restore the original transform
        g2d.setTransform(originalTransform);
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
    public Entity onPlace(GamePanel gp, InventoryItem item, int x, int y, List<String> data, List<List<String>> secondData) {
        return null; // Implement placing logic if needed
    }

    @Override
    public void state(boolean state, InventoryItem item) {

    }
}
