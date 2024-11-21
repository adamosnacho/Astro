package org.astro.core.itemsystem;

public class ItemTemplate {
    public final String name;
    public final String spritePath;
    public ItemEvents itemEvents;
    public boolean customRender = false;
    public boolean customInventoryRender = false;

    public ItemTemplate(String name, String spritePath, ItemEvents itemEvents) {
        this.name = name;
        this.spritePath = spritePath;
        this.itemEvents = itemEvents;
    }
}
