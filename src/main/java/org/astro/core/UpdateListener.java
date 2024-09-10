package org.astro.core;


import java.awt.*;
import java.util.List;

public interface UpdateListener {
    void update(InventoryItem item, GamePanel gp, double deltaTime);
    void draw(Graphics g, GamePanel gp, InventoryItem i);
    Entity onPlace(GamePanel gp, InventoryItem item, int x, int y, List<String> data, List<List<String>> secondData);
    void state(boolean state, InventoryItem item);
}
