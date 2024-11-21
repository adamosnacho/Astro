package org.astro.core.items;

import org.astro.core.*;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.ItemEvents;
import org.astro.core.itemsystem.ItemTemplate;
import org.astro.core.itemsystem.Items;

import java.util.Objects;

public class Stick extends ItemTemplate {
    public Stick() {
        super("stick", "art/png/stick.png", new ItemEvents());
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
                        PlayerInventory.hand = new Item(Items.items.get("torch"));
                        PlayerInventory.hand.it.itemEvents.inHand(true, PlayerInventory.hand);
                        ((Torch.TorchEvents.Data) PlayerInventory.hand.itemData).lit = true;
                    }
                    if (cx > e.x && cx < e.x + e.width && cy > e.y && cy < e.y + e.height && Objects.equals(((Item) e).it.name, "stick")) {
                        PlayerInventory.hand = new Item(Items.items.get("spoon"));
                        PlayerInventory.hand.it.itemEvents.inHand(true, PlayerInventory.hand);
                        Astro.astro.deSpawn(e);
                        ((Item) e).destroy();
                    }
                }
            }
        }

        @Override
        public String getInstructions(Item i) {
            return "[LMB] on stick to make a spoon\n[LMB] on rock to make torch";
        }
    }
}
