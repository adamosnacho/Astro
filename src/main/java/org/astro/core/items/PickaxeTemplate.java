package org.astro.core.items;

import org.astro.core.*;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.ItemEvents;
import org.astro.core.itemsystem.ItemTemplate;

public class PickaxeTemplate extends ItemTemplate {
    public PickaxeTemplate(String name, String spritePath) {
        super(name, spritePath, null);
        itemEvents = new ItemEvents() {
            @Override
            public void onUse(Item i) {
                int px = (int) (Astro.app.getInput().getMouseX() + Astro.astro.camera.x);
                int py = (int) (Astro.app.getInput().getMouseY() + Astro.astro.camera.y);
                for (BreakableTile breakableTile : BreakableTile.tiles) {
                    if (px >= breakableTile.x && px <= (breakableTile.x + breakableTile.width) &&
                            py >= breakableTile.y && py <= (breakableTile.y + breakableTile.height) && breakableTile.breakable) {
                        breakableTile.breakTile();
                        onPickaxeUsed(i, breakableTile);
                        break;
                    }
                }
            }

            @Override
            public void onInstantiation(Item i) {
                onCreation(i);
            }

            @Override
            public String getStatus(Item i) {
                return pickaxeStatus(i);
            }

            @Override
            public String getInstructions(Item i) {
                return "[LMB] to use pickaxe";
            }
        };
    }

    public void onPickaxeUsed(Item i, BreakableTile bt) {}

    public void onCreation(Item i) {}

    public String pickaxeStatus(Item i) {return i.it.name + " pickaxe";}
}
