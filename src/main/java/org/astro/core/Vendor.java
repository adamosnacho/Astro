package org.astro.core;

import org.astro.core.particles.Particle;
import org.astro.core.particles.ParticleGroup;
import org.astro.core.particles.ParticleSystem;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.*;

import static java.lang.Math.abs;

public class Vendor extends EnemyEntity implements TipEntity, Serializable {
    public int speed = 400;
    private Player p;
    private GamePanel gp;
    private int wallHitDir;
    private int animationFrame = 0;
    private boolean facingRight = true;
    private transient BufferedImage sprite;
    private long animationFrameRate = 50;
    private int detectionRange = 700;
    private int minimumRange = 200;
    public int knockBack = 0;
    public final float maxHp;
    public String[] maps = {"Shrimp Boss"};

    public Vendor(GamePanel gp, Player p, int x, int y) {
        height = 144;
        width = 99;
        hp = 500;
        maxHp = hp;
        this.gp = gp;
        this.p = p;
        this.x = x;
        this.y = y;
        new Timer(3000, e -> wallHitDir = gp.terrain.randomRange(-1, 1)).start();
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/art/png/vendor.png")); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }
        gp.enemies.add(this);
        gp.tipManager.tipEntities.add(this);
    }
    @Override
    public void update(double deltaTime) {
        if (Input.btn3Click) {
            if (Input.mouse.getX() + gp.cameraX >= x && Input.mouse.getX() + gp.cameraX <= x + width && Input.mouse.getY() + gp.cameraY >= y && Input.mouse.getY() + gp.cameraY <= y + height) {
                if (p.pi.hand != null && Objects.equals(p.pi.hand.itemName, "packed aluminum")) {
                    String map = maps[gp.terrain.randomRange(0, maps.length - 1)];
                    Vector2 position = Registry.bosses.get(map);
                    InventoryItem mapItem = Items.getInstance(gp).getInventory("map");
                    mapItem.data.add(map);
                    mapItem.data.add(String.valueOf(position.x));
                    mapItem.data.add(String.valueOf(position.y));
                    p.pi.hand = mapItem;
                    gp.deSpawnEntity(this, 10);
                    ParticleGroup pg = new ParticleGroup(x + width / 2, y + height / 2);
                    pg.generateParticles(() -> new Particle(pg, 0, 0, gp.terrain.randomRange(-10, 10), gp.terrain.randomRange(-30, 30), new Color(77, 77, 238), 25, 0.25f, 5000), 40);
                    ParticleSystem.particleGroups.add(pg);
                }
            }
        }
        if (knockBack != 0) {
            moveHorizontally((int) (knockBack * deltaTime * 10), gp.terrain);
            knockBack *= 0.6;
            if (Math.abs(knockBack) < 1) {
                knockBack = 0;
            }
        }

        double dx = (p.x + p.width / 2.0 ) - (x + width / 2.0);
        double dy = p.y - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > detectionRange) return;

        // Normalize the direction vector
        dx /= length;
        dy /= length;

        // Scale by speed and deltaTime
        double moveX = dx * speed * deltaTime;
        double moveY = dy * speed * deltaTime;

        if (gp.terrain.isColliding((int)(x + moveX), (int)(y + moveY), width, height))
        {
            moveX = wallHitDir * speed * deltaTime;
            moveY = wallHitDir * speed * deltaTime;
        }
        if (dx > 0) facingRight = true;
        if (dx < 0) facingRight = false;
        // Move the alien towards the player
        if (length > minimumRange + 400) move(new Vector2(moveX, moveY), gp.terrain);
        else if ((length < minimumRange - 400)) move(new Vector2(-moveX, -moveY), gp.terrain);
    }


    public void move(Vector2 direction, Terrain terrain) {
        int dx = (int)direction.x;
        int dy = (int)direction.y;
        int lx = x;
        int ly = y;
        // Attempt to move horizontally and vertically
        if (dx != 0) {
            moveHorizontally(dx, terrain);
        }
        if (dy != 0) {
            moveVertically(dy, terrain);
        }
        animations(lx != x || ly != y);
    }

    private void moveHorizontally(int dx, Terrain terrain) {
        float newX = x + dx;
        if (!terrain.isColliding((int)newX, y, width, height)) {
            x = (int)newX;
        } else {
            newX = x;
            float step = Math.signum(dx); // Step is either 1 or -1
            while (abs(newX - x) < abs(dx)) {
                newX += step;
                if (terrain.isColliding((int)newX, y, width, height)) {
                    break;
                }
                x = (int)newX;
            }
        }
    }

    private void moveVertically(int dy, Terrain terrain) {
        float newY = y + dy;
        if (!terrain.isColliding(x, (int)newY, width, height)) {
            y = (int)newY;
        } else {
            newY = y;
            float step = Math.signum(dy); // Step is either 1 or -1
            while (abs(newY - y) < abs(dy)) {
                newY += step;
                if (terrain.isColliding(x, (int)newY, width, height)) {
                    break;
                }
                y = (int)newY;
            }
        }
    }

    private long lastFrameTime = 0;
    private void animations(boolean run) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= animationFrameRate) { // apply set animation speed
            // increment animation frame
            switch (animationFrame) {
                case 0:
                    if (run) animationFrame++; // start animation loop if player is running
                    break;
                case 1,2:
                    animationFrame++;
                    break;
                case 3:
                    animationFrame = 0;
                    break;
            }
            lastFrameTime = currentTime;
        }
    }

    @Override
    public void draw(Graphics g) {
        if (gp.player.drawOver(y, height) && !delayedDraw) {
            gp.delayEntityDraw();
            return;
        }
        int y = animationFrame * height;
        if (facingRight) g.drawImage(sprite, this.x, this.y, this.x + width, this.y + height, 0, y, width, y + height, null);
        else g.drawImage(sprite, this.x + width, this.y, this.x, this.y + height, 0, y, width, y + height, null);
    }

    @Override
    public void deSpawn() {
        gp.enemies.remove(this);
        gp.tipManager.tipEntities.remove(this);
    }

    @Override
    public void damage(int amount, boolean damageRight) {
        hp -= amount;
        knockBack = facingRight ? -300 : 300;
        if (hp <= 0) {
            gp.deSpawnEntity(this, 10);
            PhysicalItem drop = Items.getInstance(gp).getPhysical("rope");
            if (gp.terrain.randomRange(0, 10) == 1) drop = Items.getInstance(gp).getPhysical("lead pickaxe");
            drop.x = x;
            drop.y = y;
            gp.spawnEntity(drop, 10);
        }
    }

    @Override
    public Vector2 getSize() {
        return new Vector2(width, height);
    }

    @Override
    public String getDisplay() {
        return "HP: " + hp + " / " + maxHp;
    }

    @Override
    public String getName() {
        return "Vendor";
    }
}
