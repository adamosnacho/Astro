package org.astro.core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class Harpoon implements UpdateListener, Serializable {
    private int animationFrame = 0;
    private int height = 100;
    private int width = 100;
    public long primeTime = 1000;
    public int primeAnimationTravel = 50;
    public transient BufferedImage sprite;
    public transient BufferedImage arrowSprite;
    public boolean primed = false;
    public boolean prime = false;
    private long primeStart = 0;
    private InventoryItem i;
    private HudComponent hc;
    private GamePanel gp;

    public Harpoon(GamePanel gp) {
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/art/png/harpoon.png")); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            arrowSprite = ImageIO.read(getClass().getResourceAsStream("/art/png/arrow.png")); // load sprite
        } catch (IOException e) {
            e.printStackTrace();
        }
        hc = g -> {
            if (i == null) return;
            if (i.data.isEmpty()) return;
            g.setColor(Color.LIGHT_GRAY);
            Font originalFont = g.getFont();
            g.setFont(new Font("Poppins", Font.PLAIN, 14));
            g.drawString("x" + i.data.getFirst(), 120, 110);
            g.drawString("Durability: " + i.data.get(2) + "%", 120, 130);
            if (primed) g.drawString("Primed", 120, 150);
            g.drawImage(arrowSprite, 90, 70, 50, 50, null);
            g.setFont(originalFont);
        };
        this.gp = gp;
    }
    @Override
    public void update(InventoryItem i, GamePanel gp, double deltaTime) {
        if (i.data.size() != 3) {
            i.data.clear();
            i.data.add("0");
            i.data.add("not primed");
            i.data.add("100");
        }
        primed = i.data.get(1) == "primed";
        animationFrame = primed ? 1 : 0;
        this.i = i;
        if (Input.keys.contains(KeyEvent.VK_R)) {
            if (gp.player.pi.slot1 != null && gp.player.pi.slot1.itemName == "arrow" && Integer.parseInt(i.data.getFirst()) != 10) {
                gp.player.pi.slot1 = null;
                i.data.set(0, String.valueOf(Integer.parseInt(i.data.getFirst()) + 1));
            }
            if (gp.player.pi.slot2 != null && gp.player.pi.slot2.itemName == "arrow" && Integer.parseInt(i.data.getFirst()) != 10) {
                gp.player.pi.slot2 = null;
                i.data.set(0, String.valueOf(Integer.parseInt(i.data.getFirst()) + 1));
            }
            if (gp.player.pi.slot3 != null && gp.player.pi.slot3.itemName == "arrow" && Integer.parseInt(i.data.getFirst()) != 10) {
                gp.player.pi.slot3 = null;
                i.data.set(0, String.valueOf(Integer.parseInt(i.data.getFirst()) + 1));
            }
            i.data.set(0, i.data.getFirst());
        }

        if (Input.btn1Click && primed) {
            primed = false;
            i.data.set(1, "not primed");
            i.data.set(2, String.valueOf(Integer.parseInt(i.data.get(2)) - 5));
            gp.spawnEntity(new Arrow(gp, gp.player.facingRight, gp.player.x + (gp.player.facingRight ? gp.player.width : -gp.player.width), gp.player.y + gp.player.height / 2), 9);
            if (Integer.parseInt(i.data.get(2)) == 0) gp.player.pi.hand = null;
            return;
        }
        if (Input.btn1 && !primed) {
            if (prime) {
                if (System.currentTimeMillis() - primeStart >= primeTime) {
                    prime = false;
                    primed = true;
                    i.data.set(1, "primed");
                    i.data.set(0, String.valueOf(Integer.parseInt(i.data.getFirst()) - 1));
                }
            }
            else if (Integer.parseInt(i.data.getFirst()) > 0 && Input.btn1Click) {
                prime = true;
                primeStart = System.currentTimeMillis();
            }
        }
        else prime = false;
    }

    @Override
    public void draw(Graphics g, GamePanel gp, InventoryItem i) {
        Player p = gp.player;

        int loadingOffset = 0;
        if (prime) loadingOffset = (int) ((float) (System.currentTimeMillis() - primeStart) / primeTime * primeAnimationTravel);

        // Calculate the middle of the player
        int playerCenterX = p.x + p.width / 2 + (p.facingRight ? -loadingOffset : loadingOffset);
        int playerCenterY = p.y + p.height / 2;
        // Calculate the vertical offset (with animation adjustment)
        int yOffset = switch (p.animationFrame) {
            case 1, 2 -> -5;
            default -> 0;
        };
        int y = playerCenterY + yOffset + 55 - height;

        // Calculate the horizontal position based on player direction
        if (p.facingRight) {
            // For facing right, start from the middle and go right
            g.drawImage(sprite, playerCenterX, y, playerCenterX + width, y + height, 0, animationFrame * (sprite.getHeight() / 2), sprite.getWidth(), animationFrame * (sprite.getHeight() / 2) + (sprite.getHeight() / 2), null);
        } else {
            // For facing left, start from the middle and go left
            int x = playerCenterX - width;
            g.drawImage(getFlippedImage(sprite), x, y, x + width, y + height, 0, animationFrame * (sprite.getHeight() / 2), sprite.getWidth(), animationFrame * (sprite.getHeight() / 2) + (sprite.getHeight() / 2), null);
        }
    }

    @Override
    public Entity onPlace(GamePanel gp, InventoryItem item, int x, int y, List<String> data, List<List<String>> secondData) {
        return null;
    }

    @Override
    public void state(boolean state, InventoryItem item) {
        if (state) gp.hud.hudComponents.add(hc);
        else gp.hud.hudComponents.remove(hc);
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
