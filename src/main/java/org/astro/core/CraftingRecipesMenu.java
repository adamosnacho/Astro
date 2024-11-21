package org.astro.core;

import org.astro.core.breakabletiles.CraftingPotTile;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.Items;
import org.newdawn.slick.*;

import java.util.Objects;

import static org.astro.core.PlayerInventory.*;
import static org.astro.core.PlayerInventory.guiScale;

public class CraftingRecipesMenu {
    public static boolean open = false;
    public static CraftingPotTile lastCraftingPotTile = null;

    private static Image slotSprite;
    private static Image checkmark;
    private static Image cross;
    private static CraftingPotTile.Recipe selectedRecipe = null;

    public static void init() {
        try {
            slotSprite = new Image("art/png/gui/slot.png").getScaledCopy(guiScale);
            slotSprite.setFilter(Image.FILTER_NEAREST);

            checkmark = new Image("art/png/gui/checkmark.png").getScaledCopy(guiScale);
            checkmark.setFilter(Image.FILTER_NEAREST);
            checkmark.setAlpha(0.4f);

            cross = new Image("art/png/gui/cross.png").getScaledCopy(guiScale);
            cross.setFilter(Image.FILTER_NEAREST);
            cross.setAlpha(0.54f);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
    }

    public static void update() {
        boolean keyR = Astro.app.getInput().isKeyPressed(org.newdawn.slick.Input.KEY_R);

        if (keyR && !open && Astro.astro.player.canMove && PlayerInventory.inputActive) open = true;
        else if (keyR) {
            Astro.astro.player.canMove = true;
            PlayerInventory.inputActive = true;
            open = false;
        }

        if (open) {
            Astro.astro.player.canMove = false;
            PlayerInventory.inputActive = false;
            if (!Input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) return;
            int mx = Astro.app.getInput().getMouseX();
            int my = Astro.app.getInput().getMouseY();
            float y = paddingY;
            float x = paddingX;
            for (CraftingPotTile.Recipe recipe : CraftingPotTile.recipes) {
                if (mx >= x && mx <= x + slotSprite.getWidth() && my >= y && my <= y + slotSprite.getHeight()) {
                    if (selectedRecipe == recipe) selectedRecipe = null;
                    else selectedRecipe = recipe;
                }

                y += paddingY + slotSprite.getHeight();
                if (y > Astro.astro.camera.height - paddingY - slotSprite.getHeight()) {
                    y = paddingY;
                    x += paddingX + slotSprite.getWidth();
                }
            }
        }
    }

    public static void render(Graphics g) {
        if (open) {
            g.setColor(new Color(120, 120, 120, 220));
            g.fillRect(0, 0, Astro.astro.camera.width, Astro.astro.camera.height);
            g.setColor(Color.white);
            float y = paddingY;
            float x = paddingX;
            for (CraftingPotTile.Recipe recipe : CraftingPotTile.recipes) {
                Image slot = slotSprite.copy();
                slot.setAlpha(1f);
                if (recipe == selectedRecipe) slot.setImageColor(0, 100, 0);
                else slot.setImageColor(255, 255, 255);
                g.drawImage(slot, x, y);
                g.drawImage(new Item(Items.items.get(recipe.out())).sprite.getScaledCopy(16, 16).getScaledCopy(guiScale), x + 1 * guiScale, y + 1 * guiScale);

                y += paddingY + slotSprite.getHeight();

                if (y > Astro.astro.camera.height - paddingY - slotSprite.getHeight()) {
                    y = paddingY;
                    x += paddingX + slotSprite.getWidth();
                }
            }
        }
        else if (selectedRecipe != null) {
            float guiScale = PlayerInventory.guiScale / 2f;
            float x = paddingX;
            float y = Astro.astro.camera.height - paddingY - slotSprite.getScaledCopy(18, 18).getScaledCopy(guiScale).getHeight() - paddingY / 2f;
            g.setColor(Color.white);
            g.setFont(Astro.font);
            String name = selectedRecipe.out().replaceAll("_", " ");
            String capitalizedName = "";
            for (int j = 0; j < name.length(); j++) {
                if (j == 0) capitalizedName += Character.toString(name.charAt(0)).toUpperCase();
                else capitalizedName += name.charAt(j);
            }

            int i = 0;
            g.drawString(capitalizedName + " crafting recipe", x, y - g.getFont().getHeight(capitalizedName + " crafting recipe"));
            for (String item : selectedRecipe.recipe()) {
                Image slot = slotSprite.getScaledCopy(18, 18).getScaledCopy(guiScale);
                slot.setAlpha(0.5f);
                g.drawImage(slot, x, y);
                g.drawImage(new Item(Items.items.get(item)).sprite.getScaledCopy(16, 16).getScaledCopy(guiScale), x + 1 * guiScale, y + 1 * guiScale);

                if (lastCraftingPotTile != null && lastCraftingPotTile.contents.size() > i) {
                    if (Objects.equals(lastCraftingPotTile.contents.get(i).item().it.name, item)) g.drawImage(checkmark.getScaledCopy(18, 18).getScaledCopy(guiScale), x, y);
                    else g.drawImage(cross.getScaledCopy(18, 18).getScaledCopy(guiScale), x, y);
                }

                x += (paddingX / 2f) + slot.getWidth();
                i++;
            }
        }
    }
}
