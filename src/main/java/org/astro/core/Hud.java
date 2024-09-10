package org.astro.core;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hud implements Serializable {
    private SpawnEntities se;
    private GamePanel gp;
    private final int spawnTimeBarWidth = 300;
    private final int spawnTimeBarHeight = 20;
    private final int spawnTimeBarMargin = 10;
    private int spawnTimeBarY = 30;

    private final int craftingMenuWidth = 200;
    private final int craftingMenuHeight = 400;
    private int craftingMenuY = 50;
    private int scrollOffset = 0;
    private final int scrollSpeed = 30;

    private InventoryItem[][] recipes;
    private InventoryItem[] crafts;
    private int totalRecipeHeight; // Cached height of all recipes
    public String command = "";
    public List<HudComponent> hudComponents = new ArrayList<>();

    public Hud(SpawnEntities se, GamePanel gp) {
        this.se = se;
        this.gp = gp;

        // Initialize crafting recipes once
        initializeCraftingRecipes();

        // Add mouse wheel listener to handle scrolling
        gp.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                scrollOffset -= e.getWheelRotation() * scrollSpeed; // Inverted for intuitive scrolling

                // Calculate the maximum scroll offset (i.e., how far we can scroll down)
                int maxScroll = Math.max(0, totalRecipeHeight);

                // Ensure scroll offset stays within valid range
                scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            }
        });
    }

    private void initializeCraftingRecipes() {
        CraftingRecipes craftingRecipes = new CraftingRecipes(gp);
        this.recipes = craftingRecipes.recipes;
        this.crafts = craftingRecipes.crafts;
        this.totalRecipeHeight = calculateTotalRecipeHeight();
    }

    private int calculateTotalRecipeHeight() {
        int totalHeight = 300;

        // Iterate over all recipes and calculate the total height they would occupy
        for (InventoryItem[] recipe : recipes) {
            if (recipe == null) break;

            int rowHeight = 40; // Default row height for a single line of items
            int itemX = 0;

            for (InventoryItem item : recipe) {
                if (item != null) {
                    // Wrap items to the next line if they exceed the menu width
                    if (itemX + 40 > craftingMenuWidth - 55) {
                        itemX = 0; // Reset x position for a new line
                        rowHeight += 45; // Increase the row height
                    }
                    itemX += 45; // Space between items
                }
            }

            totalHeight += rowHeight + 10; // Add the height of this recipe + some margin
        }

        return totalHeight;
    }

    public void draw(Graphics g) {
        commandPrompt(g);
        gp.tipManager.draw(g);

        spawnTimeBarY = gp.getHeight() - spawnTimeBarMargin - spawnTimeBarHeight;
        long timeToSpawn = se.spawnRate - se.getTimeToNextSpawn();
        float filled = (float) timeToSpawn / (float) se.spawnRate;
        g.setColor(Color.WHITE);
        g.fillRect(spawnTimeBarMargin, spawnTimeBarY, spawnTimeBarWidth, spawnTimeBarHeight);
        g.setColor(Color.GREEN);
        g.fillRect(spawnTimeBarMargin, spawnTimeBarY, (int) ((float) spawnTimeBarWidth * filled), spawnTimeBarHeight);

        g.setColor(Color.LIGHT_GRAY);
        g.fillRoundRect(10, 10, 60, 160, 10, 10);
        if (gp.player.pi.slot1 != null) g.drawImage(gp.player.pi.slot1.sprite, 15, 15, 50, 50, null);
        if (gp.player.pi.slot2 != null) g.drawImage(gp.player.pi.slot2.sprite, 15, 65, 50, 50, null);
        if (gp.player.pi.slot3 != null) g.drawImage(gp.player.pi.slot3.sprite, 15, 115, 50, 50, null);

        if (Input.keys.contains(KeyEvent.VK_SHIFT) && !gp.player.inMenu) {
            g.drawString("Crafting", 10, 200);
            InventoryItem[] it = gp.player.pi.craft.toArray(new InventoryItem[0]);
            for (int i = 0; i < gp.player.pi.craft.size(); i++) {
                g.drawImage(it[i].sprite, 15, 210 + 50 * i, 50, 50, null);
            }

            // Draw Crafting Menu
            drawCraftingMenu(g);
        }

        g.setColor(Color.LIGHT_GRAY);
        g.drawString("O2 " + (int) gp.player.po.oxygen + "%", 75, 55);

        filled = (float) gp.player.hp / 1000;
        g.setColor(Color.WHITE);
        g.fillRect(spawnTimeBarMargin, spawnTimeBarY - spawnTimeBarHeight * 2, spawnTimeBarWidth, spawnTimeBarHeight);
        g.setColor(Color.RED);
        g.fillRect(spawnTimeBarMargin, spawnTimeBarY - spawnTimeBarHeight * 2, (int) ((float) spawnTimeBarWidth * filled), spawnTimeBarHeight);

        g.setColor(Color.WHITE);
        if (Input.keys.contains(KeyEvent.VK_F12)) g.drawString("FPS " + (int) gp.fps, 500, 55);

        for(HudComponent hc : hudComponents) {
            hc.draw(g);
        }
    }

    private void drawCraftingMenu(Graphics g) {
        int menuX = gp.getWidth() - craftingMenuWidth - 10;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(menuX, craftingMenuY, craftingMenuWidth, craftingMenuHeight);
        g.setColor(Color.WHITE);
        g.drawString("Recipes", menuX + 5, craftingMenuY - 10);

        // Clip the graphics context to ensure items do not overflow the menu bounds
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setClip(menuX, craftingMenuY, craftingMenuWidth, craftingMenuHeight);

        int recipeY = craftingMenuY + 10 - scrollOffset;

        for (int i = 0; i < recipes.length; i++) {
            InventoryItem[] recipe = recipes[i];
            if (recipe == null) break;

            int itemX = menuX + 5;
            int rowHeight = 40; // Default row height for a single line of items

            // Draw the ingredients
            for (InventoryItem item : recipe) {
                if (item != null) {
                    // Check if the item will be visible within the clipped area
                    if (itemX + 40 > menuX + craftingMenuWidth - 55) { // 55 to leave space for the arrow and craft item
                        itemX = menuX + 5;
                        recipeY += rowHeight + 5; // Increase the row height and add padding

                        // Check if the next row is still within the viewable area
                        if (recipeY > craftingMenuY + craftingMenuHeight) {
                            g2d.dispose(); // Dispose of the clipped graphics context
                            return; // Exit early to avoid drawing items outside the viewable area
                        }
                    }
                    if (recipeY >= craftingMenuY && recipeY <= craftingMenuY + craftingMenuHeight) {
                        g2d.drawImage(item.sprite, itemX, recipeY, 40, 40, null);
                    }
                    itemX += 45; // Space between items
                }
            }
            // Draw the crafted item
            if (crafts[i] != null && recipeY >= craftingMenuY && recipeY <= craftingMenuY + craftingMenuHeight) {
                g2d.drawImage(crafts[i].sprite, menuX + craftingMenuWidth - 50, recipeY, 40, 40, null);
            }

            recipeY += rowHeight + 10; // Space between recipes

            // Check if we need to stop drawing
            if (recipeY > craftingMenuY + craftingMenuHeight) break;
        }

        g2d.dispose(); // Dispose of the clipped graphics context
    }


    private void commandPrompt(Graphics g) {
        if (Input.keys.contains(KeyEvent.VK_F1)) {
            g.setColor(Color.WHITE);
            g.drawString(command, 100, 200);
        }
    }
}
