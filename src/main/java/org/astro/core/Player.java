package org.astro.core;

import org.astro.core.lighting.LightManager;
import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;
import org.newdawn.slick.*;

import java.io.Serializable;
import java.nio.channels.Pipe;

public class Player extends Entity implements Save {
    public float speed = ClassSettings.loadFloat("player/speed", 0.5f); // Speed of the player
    public boolean facingLeft = false;
    public int animationFrame = 0;
    public boolean canMove = true;
    private final SpriteSheet spriteSheet;

    private float animationTimer = 0;
    public final static float animationSpeed = ClassSettings.loadInt("player/animationSpeed", 100); // speed of the walking animation

    public float suitWear = 200f;
    public float o2 = 200f;

    private final LightManager.Light light;

    public Player() {
        z = 1;
        height = 144;
        width = 99;
        Astro.astro.spawn(this);
        try {
            spriteSheet = new SpriteSheet("art/png/player.png", width, height);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        PlayerInventory.init();

        Saving.save.add(this);
        light = new LightManager.Light(x, y, 600, 1.5f, 0.7f);
        LightManager.lights.add(light);
    }

    public Player(Object o) {
        Data d = (Data) o;
        x = d.x;
        y = d.y;
        suitWear = d.suitWear;
        o2 = d.o2;
        while (Terrain.getTile((int) x / 100, (int) y / 100).solid || Terrain.getTile((int) x / 100, (int) (y + 100) / 100).solid) {
            x += 100;
        }
        z = 1;
        height = 144;
        width = 99;
        Astro.astro.spawn(this);
        try {
            spriteSheet = new SpriteSheet("art/png/player.png", width, height);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        PlayerInventory.init();

        Saving.save.add(this);
        light = new LightManager.Light(x, y, 600, 1.5f, 0.7f);
        LightManager.lights.add(light);
    }


    @Override
    public void render(Graphics g) {
        Image sprite = spriteSheet.getSprite(0, animationFrame).getFlippedCopy(facingLeft, false);
        sprite.setFilter(Image.FILTER_NEAREST);
        g.drawImage(sprite, x, y);
        PlayerInventory.draw(g);
    }

    @Override
    public void gui(Graphics g) {
        PlayerInventory.drawGui(g);
    }

    @Override
    public void update() {
        PlayerInventory.update();
        if (!canMove) return;

        animationTimer += Astro.delta;

        float moveX = 0;
        float moveY = 0;

        // Get input
        if (Astro.app.getInput().isKeyDown(Input.KEY_D)) {
            moveX += 1; // Move right
        }
        if (Astro.app.getInput().isKeyDown(Input.KEY_A)) {
            moveX -= 1; // Move left
        }
        if (Astro.app.getInput().isKeyDown(Input.KEY_S)) {
            moveY += 1; // Move down
        }
        if (Astro.app.getInput().isKeyDown(Input.KEY_W)) {
            moveY -= 1; // Move up
        }

        // Normalize direction and apply speed with delta adjustment
        float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
        if (length > 0) {  // Prevent division by zero
            moveX /= length;
            moveY /= length;
        }

        // Adjust movement by speed and delta
        move(moveX * speed * Astro.delta, moveY * speed * Astro.delta);

        // Walking animation control
        if (moveX != 0 || moveY != 0) walkAnimation();
        else animationFrame = 0;

        if (moveX != 0) facingLeft = moveX < 0;

        light.position.x = x + width / 2f;
        light.position.y = y + height / 2f;
    }



    private void walkAnimation() {
        if (animationTimer < animationSpeed) return;
        animationFrame++;
        animationFrame = animationFrame > 3 ? 0 : animationFrame;
        animationTimer = 0;
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

    public boolean isColliding(float x, float y, int width, int height) {
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

    @Override
    public Object save() {
        return new Data(x, y, suitWear, o2);
    }

    private record Data(float x, float y, float suitWear, float o2) implements Serializable {
    }
}
