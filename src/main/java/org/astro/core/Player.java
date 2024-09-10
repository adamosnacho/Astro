package org.astro.core;

import org.astro.core.saving.SaveManager;
import org.astro.core.saving.Savable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Player implements Serializable, Savable {
    public int x, y; // [px] player position
    public boolean inMenu = false;
    public boolean facingRight;
    public boolean running;
    public int animationFrame = 0; // [i] current frame of animation
    public final int width = 99; // [px] player collision width
    public final int height = 144; // [px] player collision height
    public int speed = 500; // [px/frame] player speed
    transient private BufferedImage sprite; // player sprite
    private final long animationFrameRate = 50; // [ms] length of each frame
    public PlayerInventory pi;
    public PlayerOxygen po;

    public float hp = 1000;

    private List<HorizontalLine> collisionLines = new ArrayList<>();

    public Player(int x, int y, GamePanel gp) {
        // player spawn coords
        this.x = x;
        this.y = y;
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/art/png/player.png")); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }
        pi = new PlayerInventory(this, gp);
        po = new PlayerOxygen(gp);
        SaveManager.register(this);
        SaveManager.register(pi);
    }

    public void update(Set<Integer> keys, Terrain terrain, double deltaTime) {
        if (inMenu) return;
        Vector2 direction = new Vector2(0, 0); // direction the player will move in
        if (keys.contains(KeyEvent.VK_A)) {
            direction = direction.add(new Vector2(-1, 0));
        }
        if (keys.contains(KeyEvent.VK_D)) {
            direction = direction.add(new Vector2(1, 0));
        }
        if (keys.contains(KeyEvent.VK_W)) {
            direction = direction.add(new Vector2(0, -1));
        }
        if (keys.contains(KeyEvent.VK_S)) {
            direction = direction.add(new Vector2(0, 1));
        }
        if (direction.x == 1) facingRight = true;
        if (direction.x == -1) facingRight = false;
        walk(direction, terrain, deltaTime); // apply movement to player
        pi.input();
        pi.update(deltaTime);
        po.update(deltaTime);
        if (hp > 1000) hp = 1000;
    }

    public void walk(Vector2 direction, Terrain terrain, double deltaTime) {
        // Calculate initial dx and dy based on the direction and speed
        double dx = direction.x * speed * deltaTime;
        double dy = direction.y * speed * deltaTime;

        // Normalize dx and dy
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length != 0) {
            dx = dx / length * speed * deltaTime;
            dy = dy / length * speed * deltaTime;
        }

        int lx = x;
        int ly = y;

        // Attempt to move horizontally and vertically
        if (dx != 0) {
            moveHorizontally((float) dx, terrain);
        }
        if (dy != 0) {
            moveVertically((float) dy, terrain);
        }
        running = lx != x || ly != y;
        animations(running);
    }

    private long lastFrameTime;

    private void animations(boolean run) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= animationFrameRate) { // apply set animation speed
            // increment animation frame
            switch (animationFrame) {
                case 0:
                    if (run) animationFrame++; // start animation loop if player is running
                    break;
                case 1, 2:
                    animationFrame++;
                    break;
                case 3:
                    animationFrame = 0;
                    break;
            }
            lastFrameTime = currentTime;
        }
    }

    private void moveHorizontally(float dx, Terrain terrain) {
        float stepSize = 1.0f; // Smaller step size to check for collisions along the path
        float remainingDistance = Math.abs(dx);
        float direction = Math.signum(dx); // Get the direction of movement (-1 for left, 1 for right)

        while (remainingDistance > 0) {
            // Move step by step
            float step = Math.min(stepSize, remainingDistance);
            float newX = x + direction * step;

            if (!terrain.isColliding((int) newX, y, width, height)) {
                x = (int) newX; // Move player if no collision
            } else {
                // Collision detected, stop moving horizontally
                break;
            }

            remainingDistance -= step; // Reduce the remaining distance
        }
    }


    private void moveVertically(float dy, Terrain terrain) {
        float stepSize = 1.0f; // Smaller step size to check for collisions along the path
        float remainingDistance = Math.abs(dy);
        float direction = Math.signum(dy); // Get the direction of movement (-1 for up, 1 for down)
        boolean collision = false;

        while (remainingDistance > 0) {
            // Move step by step
            float step = Math.min(stepSize, remainingDistance);
            float newY = y + direction * step;

            // Check for terrain collision
            if (terrain.isColliding(x, (int) newY, width, height)) {
                collision = true;
                break;
            }

            // Check for horizontal line collisions
            for (HorizontalLine line : collisionLines) {
                if (line.isColliding(x, (int) newY, width)) {
                    collision = true;

                    if (direction > 0) { // Moving downwards (falling)
                        // Align player to rest exactly on top of the line
                        y = (int) line.start.y - height;
                    } else if (direction < 0) { // Moving upwards (jumping)
                        // Align player to rest exactly below the line
                        y = (int) line.start.y + 5 - height; // Adjust by 1 pixel to avoid continuous collision
                    }

                    break;
                }
            }

            if (!collision) {
                y = (int) newY; // Move player if no collision
            } else {
                break; // Stop moving vertically on collision
            }

            remainingDistance -= step; // Reduce the remaining distance
        }
    }




    public void draw(Graphics g) {
        po.draw(g);
        int y = animationFrame * height;
        if (facingRight) {
            g.drawImage(sprite, this.x, this.y, this.x + width, this.y + height, 0, y, width, y + height, null);
        } else {
            g.drawImage(sprite, this.x + width, this.y, this.x, this.y + height, 0, y, width, y + height, null);
        }
        pi.draw(g);
    }

    public boolean drawOver(int y, int height) {
        return y + height >= this.y + this.height;
    }

    public int addCollisionLine(Vector2 start, double length) {
        collisionLines.add(new HorizontalLine(start, length));
        return collisionLines.size() - 1;
    }

    public void clearCollisionLines() {
        collisionLines.clear();
    }

    public void updateLine(int index, Vector2 start, double length) {
        collisionLines.get(index).length = length;
        collisionLines.get(index).start = start;
    }

    @Override
    public Object save() {
        List<Float> o = new ArrayList<>();
        o.add((float) x);
        o.add((float) y);
        o.add(hp);
        o.add(po.oxygen);
        return o;
    }

    @Override
    public void load(Object o) {
        List<Float> l = (List) o;
        x = l.get(0).intValue();
        y = l.get(1).intValue();
        hp = l.get(2);
        po.oxygen = l.get(3);
    }

    private class HorizontalLine implements Serializable {
        Vector2 start; // Start point of the line
        double length; // Length of the line extending to the right

        HorizontalLine(Vector2 start, double length) {
            this.start = start;
            this.length = length;
        }

        boolean isColliding(int x, int y, int width) {
            // Allow a slightly larger margin for collision detection (within 8 pixels above the line and 5 below)
            boolean verticalOverlap = (y + height >= start.y - 8 && y + height <= start.y + 5);

            // Check if the player's horizontal span (x to x + width) overlaps with the line's span
            boolean horizontalOverlap = x + width > start.x && x < start.x + length;

            return verticalOverlap && horizontalOverlap;
        }
    }
}
