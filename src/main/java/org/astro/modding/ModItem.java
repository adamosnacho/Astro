package org.astro.modding;

import org.astro.core.GamePanel;
import org.astro.core.InventoryItem;
import org.astro.core.PhysicalItem;

public interface ModItem {
    String getItemName();
    String getImagePath();
    boolean isTilePlaceable();
    void onPlace(GamePanel gp, int x, int y);
    void onBreak(GamePanel gp);
    Class<? extends org.astro.core.Entity> getEntityClass();
}
