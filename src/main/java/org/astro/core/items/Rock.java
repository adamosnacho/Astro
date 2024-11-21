package org.astro.core.items;

import org.astro.core.Astro;
import org.astro.core.Entity;
import org.astro.core.PlayerInventory;
import org.astro.core.Utils;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.ItemEvents;
import org.astro.core.itemsystem.ItemTemplate;
import org.astro.core.itemsystem.Items;
import org.astro.core.particlesystem.Particle;
import org.astro.core.particlesystem.ParticleGroup;
import org.newdawn.slick.Color;

import java.util.Objects;

public class Rock extends ItemTemplate {
    public Rock() {
        super("rock", "art/png/rock.png", new ItemEvents());
        itemEvents = new TorchEvents();
    }

    private static class TorchEvents extends ItemEvents {
        @Override
        public void onUse(Item i) {
            for (Entity e : Astro.astro.entities) {
                if (e instanceof Item) {
                    int cx = (int) (Astro.app.getInput().getMouseX() + Astro.astro.camera.x);
                    int cy = (int) (Astro.app.getInput().getMouseY() + Astro.astro.camera.y);
                    if (cx > e.x && cx < e.x + e.width && cy > e.y && cy < e.y + e.height && Objects.equals(((Item) e).it.name, "rock")) {
                        PlayerInventory.hand = new Item(Items.items.get("crafting_pot"));
                        PlayerInventory.hand.it.itemEvents.inHand(true, PlayerInventory.hand);
                        Astro.astro.deSpawn(e);

                        ParticleGroup pg = new ParticleGroup(e.x, e.y, true);
                        int vel = 500;
                        pg.play(() -> {
                            Particle pa = new Particle(pg, 0, 0, Utils.randomRange(-vel, vel), Utils.randomRange(-vel, vel), (Utils.randomRange(0, 2) == 0 ? Color.lightGray : Color.gray), 0.001f, 1);
                            pa.physicsProperties(0.3f, 0, 5);
                            pa.setSize(20, 20);
                            return pa;
                        }, 30);
                    }
                }
            }
        }

        @Override
        public String getStatus(Item i) {
            return "Rock";
        }

        @Override
        public String getInstructions(Item i) {
            return "[LMB] on rock to make Crafting Pot";
        }
    }
}
