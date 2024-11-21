package org.astro.core;

import org.newdawn.slick.Graphics;

public class Entity {
    public int z = 0;
    public float x = 0;
    public float y = 0;
    public int width = 100;
    public int height = 100;
    public void render(Graphics g) {}
    public void gui(Graphics g) {}
    public void update() {}
    public void spawn() {}
    public void deSpawn() {}
    public void onInteract() {}
    public String getInfo() {return "";}
}
