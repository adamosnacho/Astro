package org.astro.core;

import org.newdawn.slick.*;
import java.util.ArrayList;
import java.util.List;

public class Enemy extends Entity {
    public static List<Enemy> enemies = new ArrayList<>();

    public float keepDistance = 100;
    public Color hitColor = Color.white;
    public boolean facingLeft = false;
    public float speed = 0.15f; // Speed of the player
    private float knockBackVel = 0;
    public float hp = 100;
    public boolean dying = false;
    public boolean onFire = false;

    private float time = 0;
    private final float dieTime = 1000;

    public Enemy() {
        x = 12;
        y = 12;
        while (Terrain.getTile((int) x / 100, (int) y / 100).solid || Terrain.getTile((int) x / 100, (int) (y + 100) / 100).solid) {
            x += 100;
        }
        z = 0;
        height = 144;
        width = 99;
        Astro.astro.spawn(this);
        enemies.add(this);
    }

    public void pathFind(float tx, float ty) {
        float moveX = Utils.clamp(tx - x, -1, 1);
        float moveY = Utils.clamp(ty - y, -1, 1);

        facingLeft = moveX < 0;

        if (Math.sqrt(((tx + Astro.astro.player.width / 2f) - (x + width / 2f)) * ((tx + Astro.astro.player.width / 2f) - (x + width / 2f)) + ((ty + Astro.astro.player.height / 2f) - (y + height / 2f)) * ((ty + Astro.astro.player.height / 2f) - (y + height / 2f))) < keepDistance) {
            moveX *= -1;
            moveY *= -1;
        }

        float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
        if (length > 0) {
            moveX /= length;
            moveY /= length;
        }
        // Adjust movement by speed and delta
        move((moveX * speed + knockBackVel) * Astro.delta, moveY * speed * Astro.delta);
        knockBackVel *= (float) Math.pow(0.99f, Astro.delta);

        if (hp <= 0) die();
    }

    public void move(float dx, float dy) {
        // Calculate the new position
        float newX = x + dx;
        float newY = y + dy;

        // Check for collisions before moving
        if (!isColliding(newX, y, width, height)) {
            x = newX; // Move horizontally if no collision
        }
        if (!isColliding(x, newY, width, height)) {
            y = newY; // Move vertically if no collision
        }
    }

    private boolean isColliding(float x, float y, int width, int height) {
        int step = 5; // Small interval between checks along each edge
        int padding = 1; // Padding to prevent clipping

        // Check top and bottom edges
        for (int offsetX = padding; offsetX <= width - padding; offsetX += step) {
            // Top edge
            if (Terrain.getTile((int) ((x + offsetX) / 100), (int) ((y) / 100)).solid) {
                return true;
            }
            // Bottom edge
            if (Terrain.getTile((int) ((x + offsetX) / 100), (int) ((y + height - 1) / 100)).solid) {
                return true;
            }
        }

        // Check left and right edges
        for (int offsetY = padding; offsetY <= height - padding; offsetY += step) {
            // Left edge
            if (Terrain.getTile((int) ((x) / 100), (int) ((y + offsetY) / 100)).solid) {
                return true;
            }
            // Right edge
            if (Terrain.getTile((int) ((x + width - 1) / 100), (int) ((y + offsetY) / 100)).solid) {
                return true;
            }
        }
        return false;
    }

    public void updateDeath() {
        time += Astro.delta * speed;
        if (time >= dieTime) finishDie();
    }

    public void knockBack(float knockBackForce) {
        knockBackVel += knockBackForce;
    }

    public void die() {
        dying = true;
    }

    public void finishDie() {
        Astro.astro.deSpawn(this);
    }

    public void renderDie(Graphics g, Image sprite) {
        sprite.setImageColor(0, 0, 0, (dieTime - time) / dieTime);
        g.drawImage(sprite, x, y);
    }

    @Override
    public void deSpawn() {
        enemies.remove(this);
    }
}
