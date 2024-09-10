package org.astro.core;

import java.util.Random;

public class ShrimpBossArena {
    public int x, y;
    private GamePanel gp;
    public int[][] structure = {
            {7, 7, 7, 7, 7, 7, 7, 7, 7, 7},
            {7, 6, 6, 6, 6, 6, 8, 8, 8, 7},
            {7, 6, 6, 6, 6, 6, 6, 6, 8, 7},
            {7, 6, 6, 8, 6, 6, 6, 6, 8, 7},
            {1, 6, 6, 8, 6, 6, 6, 6, 6, 7},
            {1, 6, 6, 8, 6, 6, 6, 6, 6, 7},
            {7, 6, 6, 8, 6, 6, 6, 6, 8, 7},
            {7, 6, 6, 6, 6, 6, 6, 6, 8, 7},
            {7, 6, 6, 6, 6, 6, 8, 8, 8, 7},
            {7, 7, 7, 7, 7, 7, 7, 7, 7, 7}
    };

    // Constructor
    public ShrimpBossArena(GamePanel gp, Terrain t, Lander l) {
        this.gp = gp;
        Random rand = new Random();

        int distance = 300 + t.randomRange(0, 300);

        // Generate a random angle in radians
        double angle = rand.nextDouble() * 2 * Math.PI; // 0 to 2*PI radians

        // Calculate the new position based on the random distance and angle in tile units
        this.x = l.x / 100 + (int) (distance * Math.cos(angle));
        this.y = l.y / 100 + (int) (distance * Math.sin(angle));

        // Ensure the coordinates are within world bounds
        this.x = Math.max(0, Math.min(this.x, t.height - structure[0].length));
        this.y = Math.max(0, Math.min(this.y, t.width - structure.length));

        System.out.println("ShrimpBossArena X: " + x);
        System.out.println("ShrimpBossArena Y: " + y);

        Registry.bosses.put("Shrimp Boss", new Vector2(x * 100, y * 100));

        spawnArena(t);
    }

    private void spawnArena(Terrain t) {
        for (int i = 0; i < structure.length; i++) {
            for (int j = 0; j < structure[i].length; j++) {
                t.setTile(y + i, x + j, structure[i][j]);
            }
        }
        new ShrimpBoss(gp, gp.player, x * 100 + 500, y * 100 + 500);
    }
}
