package org.astro.core.items;

import org.astro.core.*;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.ItemEvents;
import org.astro.core.particlesystem.Particle;
import org.astro.core.particlesystem.ParticleGroup;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import java.io.Serializable;
import java.util.ArrayList;

public class RockHammer extends ItemEvents {
    public static final float hitRange = ClassSettings.loadFloat("rock hammer/hit range", 40f);
    public static final float damage = ClassSettings.loadFloat("rock hammer/damage", 15f);
    public static final float speed = ClassSettings.loadFloat("rock hammer/speed", 0.1f);
    public static final float rotationSpeed = ClassSettings.loadFloat("rock hammer/rotation speed", 0.7f);
    public static final float returnSpeed = ClassSettings.loadFloat("rock hammer/return speed", 0.01f);

    @Override
    public void onInstantiation(Item i) {
        i.itemData = new HammerStorage();
    }

    @Override
    public void guiUpdate(Item i) {
        HammerStorage hs = ((HammerStorage) i.itemData);
        if (!hs.attacking) return;
        hs.time += Astro.delta * speed * Astro.delta;
        hs.rotation += hs.returning ? -hs.time * returnSpeed : hs.time * rotationSpeed;

        if (hs.rotation > 90) {
            hs.returning = true;
            hs.time = 0;
            hs.rotation = 90;
        }

        if (hs.returning && hs.rotation <= 0) {
            hs.attacking = false;
            hs.rotation = 0;
            return;
        }

        if (hs.hit || hs.returning) return;

        Player p = Astro.astro.player;
        float hitPointX = p.x + (p.facingLeft ? -hitRange : p.width + hitRange);
        float hitPointY = p.y + p.height / 2f - 5;

        // Check for hits
        for (Enemy enemy : new ArrayList<>(Enemy.enemies)) {
            if (enemy.x <= hitPointX && enemy.x + enemy.width >= hitPointX && enemy.y <= hitPointY && enemy.y + enemy.height >= hitPointY) {
                enemy.knockBack(p.facingLeft ? -1 : 1);
                ParticleGroup pg = new ParticleGroup(hitPointX, hitPointY, true);
                int vel = 300;
                pg.play(() -> {
                    Particle pa = new Particle(pg, 0, 0, Utils.randomRange(-vel, vel), Utils.randomRange(-vel, vel), (Utils.randomRange(0, 2) == 1 ? enemy.hitColor  : Color.lightGray), 0.001f, 1);
                    pa.physicsProperties(0.3f, 0, 5);
                    pa.setSize(20, 20);
                    return pa;
                }, 10);
                hs.hit = true;
                enemy.hp -= damage;
                Astro.astro.camera.shake(2, 0.5f);
            }
        }
    }

    @Override
    public String getStatus(Item i) {
        return "Hammer\nWear " + ((HammerStorage) i.itemData).wear + "%";
    }

    @Override
    public String getInstructions(Item i) {
        return "[LMB] to attack";
    }

    @Override
    public void inventoryRender(Graphics g, Item i) {
        HammerStorage hs = ((HammerStorage) i.itemData);
        Player player = Astro.astro.player;
        Image sprite = i.sprite;
        if (player.facingLeft) {
            sprite = i.sprite.getFlippedCopy(true, false);
        }

        sprite.setCenterOfRotation(20,40 + ((hs.rotation / 90) * 30));
        sprite.setRotation(hs.rotation * (player.facingLeft ? -1 : 1));
        int oY = -20;
        if (hs.attacking) oY -= (int) ((hs.rotation / 90) * 30);
        if ((player.animationFrame == 1 || player.animationFrame == 2) && !hs.attacking) oY = -25;
        if (player.facingLeft) g.drawImage(sprite, player.x, player.y + (float) player.height / 2 + oY);
        else g.drawImage(sprite, player.x + player.width - i.width, player.y + (float) player.height / 2 + oY);
        Player p = Astro.astro.player;
        float hitPointX = p.x + (p.facingLeft ? -hitRange : p.width + hitRange);
        float hitPointY = p.y + p.height / 2f - 5;
        g.setColor(Color.white);
        if (Input.isMouseDown(Input.MOUSE_MIDDLE_BUTTON)) g.fillRect(hitPointX, hitPointY, 5, 5);
    }

    @Override
    public void onUse(Item i) {
        HammerStorage hs = ((HammerStorage) i.itemData);
        if (hs.attacking) return;
        hs.wear -= 0.5f;
        hs.attacking = true;
        hs.returning = false;
        hs.rotation = 0;
        hs.time = 0;
        hs.hit = false;

        if (hs.wear <= 0) {
            PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
            PlayerInventory.hand = null;
        }
    }

    private static class HammerStorage implements Serializable {
        private float wear = 100;
        private boolean attacking = false;
        private boolean returning = false;
        private boolean hit = false;
        private float time = 0;
        private float rotation = 0;
    }
}