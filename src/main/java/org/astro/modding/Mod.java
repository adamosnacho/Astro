package org.astro.modding;

import org.astro.core.GamePanel;

import java.awt.*;

public interface Mod {
    void initialize(GamePanel gp);
    void update();
    void draw(Graphics g);
}