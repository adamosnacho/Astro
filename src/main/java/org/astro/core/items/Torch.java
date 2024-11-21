package org.astro.core.items;

import org.astro.core.*;
import org.astro.core.breakabletiles.FireTile;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.ItemEvents;
import org.astro.core.itemsystem.ItemTemplate;
import org.astro.core.itemsystem.Items;

import org.astro.core.particlesystem.Particle;
import org.astro.core.particlesystem.ParticleGroup;
import org.newdawn.slick.*;

import javax.xml.crypto.Data;
import java.io.Serializable;
import java.util.Objects;

public class Torch extends ItemTemplate {
    public static final int animationSpeed = 150;
    private final SpriteSheet spriteSheet;

    public Torch() {
        super("torch", "art/png/unLitTorch.png", new ItemEvents());
        itemEvents = new TorchEvents();
        customInventoryRender = true;
        try {
            spriteSheet = new SpriteSheet("art/png/torch.png", 16, 16);
            spriteSheet.setFilter(Image.FILTER_NEAREST);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
    }

    public class TorchEvents extends ItemEvents {
        @Override
        public void onInstantiation(Item i) {
            i.itemData = new Data();
        }

        @Override
        public void inventoryRender(Graphics g, Item i) {
            Data d = (Data) i.itemData;
            Image sprite = d.lit ? spriteSheet.getSprite(0, d.animationFrame).getScaledCopy(i.width * 2, i.height * 2) : i.sprite.getScaledCopy(i.width * 2, i.height * 2);
            Player player = Astro.astro.player;
            int oY = -4;
            if (player.animationFrame == 1 || player.animationFrame == 2) oY = -9;
            oY -= 50;
            if (player.facingLeft) g.drawImage(sprite.getFlippedCopy(true, false), player.x - 30, player.y + (float) player.height / 2 + oY);
            else g.drawImage(sprite.getFlippedCopy(false, false), player.x + player.width - sprite.getWidth() + 30, player.y + (float) player.height / 2 + oY);
        }

        @Override
        public void guiUpdate(Item i) {
            Data d = (Data) i.itemData;
            d.time += Astro.delta;
            if (d.time >= animationSpeed) {
                d.time = 0;
                d.animationFrame++;
                if (d.animationFrame > 6) d.animationFrame = 0;
            }

            if (d.lit) {
                d.timeLeft -= Astro.delta;
                if (d.timeLeft <= 0) {
                    i.it.itemEvents.inHand(false, i);
                    PlayerInventory.hand = new Item(Items.items.get("ash"));
                }
            }
        }

        @Override
        public void inHand(boolean is, Item i) {
            Data d = (Data) i.itemData;
            if (!is) d.lit = false;

        }

        @Override
        public void onDrop(Item i) {
            Data d = (Data) i.itemData;
            if (d.lit) {
                FireTile ft = new FireTile(i.x, i.y + 100);
                Astro.astro.deSpawn(i);
                new Item(Items.items.get("ash"), ft.x + Utils.randomRange(0, ft.width - 40), ft.y + Utils.randomRange(0, ft.height - 40));
                int vel = 7;
                ParticleGroup pg = new ParticleGroup(ft.x + 50, ft.y + 50, true);
                pg.play(() -> {
                    Particle p = new Particle(pg, Utils.randomRange(-50, 50), Utils.randomRange(-50, 50), Utils.randomRange(-vel, vel), Utils.randomRange(-vel, vel), Color.black, 0.1f, 10);
                    p.physicsProperties(0.95f, 0, 0);
                    p.setSize(5, 5);
                    return p;
                }, 80);
            }
        }

        @Override
        public void onUse(Item i) {
            Data d = (Data) i.itemData;
            for (Entity e : Astro.astro.entities) {
                if (e instanceof Item) {
                    int cx = (int) (Astro.app.getInput().getMouseX() + Astro.astro.camera.x);
                    int cy = (int) (Astro.app.getInput().getMouseY() + Astro.astro.camera.y);
                    if (cx > e.x && cx < e.x + e.width && cy > e.y && cy < e.y + e.height && Objects.equals(((Item) e).it.name, "rock")) d.lit = true;
                }
            }
        }

        @Override
        public String getStatus(Item i) {
            Data d = (Data) i.itemData;
            return "Torch\nBurns out: " + d.timeLeft / 1000 + "s\n[LMB] on rock to light";
        }

        @Override
        public String getInstructions(Item i) {
            return "[LMB] on stick to make a spoon\n[LMB] on rock to make torch";
        }

        public static class Data implements Serializable {
            boolean lit = false;
            private int time = 0;
            private int animationFrame = 0;
            private int timeLeft = 80000;
        }
    }
}
