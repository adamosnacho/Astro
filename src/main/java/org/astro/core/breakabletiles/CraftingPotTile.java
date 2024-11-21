package org.astro.core.breakabletiles;

import org.astro.core.*;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.Items;
import org.astro.core.particlesystem.Particle;
import org.astro.core.particlesystem.ParticleGroup;
import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import java.io.Serializable;
import java.util.*;

public class CraftingPotTile extends BreakableTile implements Save {
    public static List<Recipe> recipes = new ArrayList<>();

    public List<ItemPotStorage> contents = new ArrayList<>();

    private final ParticleGroup pg;
    private final Image spriteFront;
    private final Image spriteSpoon;

    private static final float stirringSpeed = ClassSettings.loadFloat("crafting pot tile/stirring speed", 0.002f);
    private static final float travelX = ClassSettings.loadFloat("crafting pot tile/travel x", 60f);
    private static final float travelY = ClassSettings.loadFloat("crafting pot tile/travel y", 30f);
    private boolean stirring = false;
    private float stirringPos = 0;

    private int stirringCurrentTime = 0;
    private static final int stirringTime = ClassSettings.loadInt("crafting pot tile/stirring time", 100);
    private int craftingCurrentTime = 0;
    private static final int craftingTime = ClassSettings.loadInt("crafting pot tile/crafting time", 1000);

    public static void baseRecipes() {
        recipes.add(new Recipe(new ArrayList<>(List.of(new String[] {"rock", "rock", "rock", "rock", "stick", "stick"})), "rock_hammer"));
        recipes.add(new Recipe(new ArrayList<>(List.of(new String[] {"lead", "lead", "lead", "lead", "stick", "stick"})), "lead_pickaxe"));
        recipes.add(new Recipe(new ArrayList<>(List.of(new String[] {"rock", "rock", "rock", "rock", "stick", "stick", "rope", "rope"})), "drill"));
        recipes.add(new Recipe(new ArrayList<>(List.of(new String[] {"rope", "rope", "rope", "rope", "rope", "rope", "stick", "stick"})), "bag"));
        recipes.add(new Recipe(new ArrayList<>(List.of(new String[] {"stick", "stick"})), "spoon"));
        recipes.add(new Recipe(new ArrayList<>(List.of(new String[] {"ash", "ash", "ash", "stick"})), "rope"));
        recipes.add(new Recipe(new ArrayList<>(List.of(new String[] {"stick", "stick", "stick", "stick"})), "plank"));
    }

    public CraftingPotTile(float x, float y) {
        super(x, y, Items.items.get("crafting_pot"));
        width = 100;
        height = 100;
        pg = new ParticleGroup(this.x + (float) width / 2, this.y + 20, false);
        try {
            sprite = new Image("art/png/craftingPot.png", false, Image.FILTER_NEAREST).getScaledCopy(100, 100);
            spriteFront = new Image("art/png/craftingPotFront.png", false, Image.FILTER_NEAREST).getScaledCopy(100, 100);
            spriteSpoon = new Image("art/png/spoon.png", false, Image.FILTER_NEAREST).getScaledCopy(40, 40).getFlippedCopy(false, true);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        Astro.astro.spawn(this);
        Saving.save.add(this);
    }

    public CraftingPotTile(Object o) {
        super(((PotSave) o).x, ((PotSave) o).y, Items.items.get("crafting_pot"));
        PotSave ps = (PotSave) o;
        contents.clear();
        for (ItemPotSaveData ipsd : ps.items) {
            Item item = new Item(Items.items.get(ipsd.item.name()));
            item.itemData = item.it.itemEvents.load(item, ipsd.item.data());
            contents.add(new ItemPotStorage(item, ipsd.x));
        }

        width = 100;
        height = 100;
        pg = new ParticleGroup(this.x + (float) width / 2, this.y + 20, false);
        try {
            sprite = new Image("art/png/craftingPot.png", false, Image.FILTER_NEAREST).getScaledCopy(100, 100);
            spriteFront = new Image("art/png/craftingPotFront.png", false, Image.FILTER_NEAREST).getScaledCopy(100, 100);
            spriteSpoon = new Image("art/png/spoon.png", false, Image.FILTER_NEAREST).getScaledCopy(40, 40).getFlippedCopy(false, true);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        Astro.astro.spawn(this);
        Saving.save.add(this);
    }

    @Override
    public Object onTileBreak() {
        if (CraftingRecipesMenu.lastCraftingPotTile == this) CraftingRecipesMenu.lastCraftingPotTile = null;
        dropContent();
        return null;
    }

    @Override
    public void deSpawn() {
        Saving.save.remove(this);
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(sprite, x, y);

        if (stirring) g.drawImage(spriteSpoon, (float) (x + Math.toDegrees(Math.sin(stirringPos)) / 360 * travelX) + width / 2f - 20, (float) (y + Math.toDegrees(Math.cos(stirringPos)) / 360 * travelY + 10));

        for (ItemPotStorage itemPotStorage : contents) {
            g.drawImage(itemPotStorage.item.sprite, x + itemPotStorage.x, y + 20);
        }

        g.drawImage(spriteFront, x, y);
    }

    public void stirringParticles() {
        if (stirringCurrentTime > stirringTime) {
            stirringCurrentTime = 0;
            int vel = 100;
            List<Color> colors = new ArrayList<>();
            for (ItemPotStorage ips : contents) {
                for (int x = 0; x < ips.item.sprite.getWidth() - 1; x++) {
                    for (int y = 0; y < ips.item.sprite.getHeight() - 1; y++) {
                        try {
                            colors.add(ips.item.sprite.getColor(x, y));
                        } catch (ArrayIndexOutOfBoundsException e) {}
                    }
                }
            }
            pg.x = (float) (x + Math.toDegrees(Math.sin(stirringPos)) / 360 * travelX) + width / 2f;
            pg.y = (float) (y + Math.toDegrees(Math.cos(stirringPos)) / 360 * travelY + 20);
            pg.play(() -> {
                Particle p = new Particle(pg, 0, 0, Utils.randomRange(-vel, vel), Utils.randomRange(-vel, vel), colors.get(Utils.randomRange(0, colors.size() - 1)), 0.01f, 3);
                p.physicsProperties(0.3f, 0, 0);
                p.setSize(10, 10);
                return p;
            }, 50);
        }
    }

    @Override
    public void update() {
        if (stirring) {
            stirringPos += Astro.delta * stirringSpeed;
            stirringCurrentTime += Astro.delta;
            stirringParticles();

            craftingCurrentTime += Astro.delta;
            if (craftingCurrentTime > craftingTime) {
                craft();
                craftingCurrentTime = 0;
                stirringCurrentTime = 0;
                stirring = false;
            }
        }

    }

    @Override
    public void onInteract() {
        CraftingRecipesMenu.lastCraftingPotTile = this;
        if (PlayerInventory.hand != null && !stirring) {
            if (Objects.equals(PlayerInventory.hand.it.name, "spoon")) {
                if (contents.size() == 0) return;

                if (Astro.app.getInput().isKeyDown(Input.KEY_LSHIFT) || Astro.app.getInput().isKeyDown(Input.KEY_RSHIFT)) {
                    dropContent();
                    return;
                }

                PlayerInventory.hand.itemData = (int) PlayerInventory.hand.itemData - 10;
                if ((int) PlayerInventory.hand.itemData <= 0) {
                    PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
                    PlayerInventory.hand.destroy();
                    PlayerInventory.hand = null;
                }
                stirring = true;
                return;
            }

            PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
            PlayerInventory.hand.destroy();

            int vel = 70;
            List<Color> colors = new ArrayList<>();
            for (int x = 0; x < PlayerInventory.hand.sprite.getWidth() - 1; x++) {
                for (int y = 0; y < PlayerInventory.hand.sprite.getHeight() - 1; y++) {
                    try {
                        colors.add(PlayerInventory.hand.sprite.getColor(x, y));
                    } catch (ArrayIndexOutOfBoundsException e) {}
                }
            }
            pg.x = this.x + (float) width / 2;
            pg.y = this.y + 20;
            pg.play(() -> {
                Particle p = new Particle(pg, 0, 0, Utils.randomRange(-vel, vel), Utils.randomRange(-vel, vel), colors.get(Utils.randomRange(0, colors.size() - 1)), 0.01f, 3);
                p.physicsProperties(0.3f, 0, 0);
                p.setSize(10, 10);
                return p;
            }, 50);

            contents.add(new ItemPotStorage(PlayerInventory.hand, Utils.randomRange(10, 100 - PlayerInventory.hand.width - 10)));
            PlayerInventory.hand = null;
        }
    }

    private void craft() {

        // Try to match contents with each recipe
        for (Recipe recipe : recipes) {
            boolean crafted = true;
            if (recipe.recipe.size() != contents.size()) continue;
            for (int i = 0; i < recipe.recipe.size(); i++) {
                if (!Objects.equals(contents.get(i).item.it.name, recipe.recipe.get(i))) {
                    crafted = false;
                    break;
                }
            }

            if (crafted) {
                // Crafting succeeded, spawn crafted item and clear contents
                new Item(Items.items.get(recipe.out), x + Utils.randomRange(0, width - 40), y + Utils.randomRange(0, height - 40) + height);
                contents.clear();
                return;
            }
        }

        // If no recipe matched, drop all items
        dropContent();
    }

    public void dropContent() {
        for (ItemPotStorage ips : contents) {
            if (ips.item != null) {
                ips.item.drop(
                        x + Utils.randomRange(0, width - 40),
                        y + Utils.randomRange(0, height - 40) + height
                );
            }
        }
        contents.clear();
    }

    @Override
    public Object save() {
        List<ItemPotSaveData> ipsdl = new ArrayList<>();
        for (ItemPotStorage ips : contents) {
            ipsdl.add(new ItemPotSaveData(new Item.ItemData(ips.item.x, ips.item.y, ips.item.it.name, ips.item.it.itemEvents.load(ips.item, ips.item.itemData), false), ips.x));
        }

        return new PotSave(ipsdl, x, y);
    }


    public record ItemPotStorage(Item item, float x) {}

    public record ItemPotSaveData(Item.ItemData item, float x) implements Serializable {}

    public record PotSave(List<ItemPotSaveData> items, float x, float y) implements Serializable {}

    public record Recipe(List<String> recipe, String out) {};
}