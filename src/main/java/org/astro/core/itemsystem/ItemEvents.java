package org.astro.core.itemsystem;

import org.newdawn.slick.Graphics;

public class ItemEvents {
    public void onPickup(Item i) {} // on item picked up
    public void onDrop(Item i) {} // on item dropped
    public void render(Graphics g, Item i) {} // how to render item on ground (called only if ItemTemplate.customRender is true)
    public void gui(Graphics g, Item i) {} // when in hand a gui can be made using this
    public void guiUpdate(Item i) {}
    public void update(Item i) {}
    public void inventoryRender(Graphics g, Item i) {} // when in hand how to render item (called only if ItemTemplate.customInventoryRender is true)
    public void onUse(Item i) {} // when in hand what happens when this item is used (lmb)
    public void onInstantiation(Item i) {}
    public void inHand(boolean is, Item i) {}
    public String getStatus(Item i) {
        String name = i.it.name.replaceAll("_", " ");
        String capitalizedName = "";
        for (int j = 0; j < name.length(); j++) {
            if (j == 0) capitalizedName += Character.toString(name.charAt(0)).toUpperCase();
            else capitalizedName += name.charAt(j);
        }
        return capitalizedName;
    }
    public String getInstructions(Item i) {return "";}

    // Saving
    public Object save(Item i) {return i.itemData;}
    public Object load(Item i, Object data) {return data;}
}
