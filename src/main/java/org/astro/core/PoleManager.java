package org.astro.core;

import java.io.Serializable;
import java.util.*;
import java.util.List;

public class PoleManager implements Serializable {
    public static PoleManager instance;
    public static GamePanel gp;

    public List<Pole> poles = new ArrayList<>();

    private PoleManager() {
        poles.add(new Pole(gp, gp.lander.x - 22, gp.lander.y + gp.lander.height / 2 - 42));
        poles.getFirst().isConnected = true;
        poles.getFirst().source = poles.getFirst();
    }

    public static PoleManager getInstance() {
        if (instance == null) {
            instance = new PoleManager();
        }
        return instance;
    }

    // Method to calculate the distance between two poles
    public double distance(Pole p1, Pole p2) {
        int dx = p1.x - p2.x;
        int dy = p1.y - p2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
