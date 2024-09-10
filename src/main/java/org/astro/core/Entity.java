package org.astro.core;

import java.awt.*;
import java.io.Serializable;

public class Entity implements Serializable {
    public int x = 0;
    public int y = 0;
    public boolean delayedDraw = false;
    public void draw(Graphics g) {}
    public void update(double deltaTime) {}
    public void deSpawn() {}
}
