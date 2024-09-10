package org.astro.core;

import javax.swing.*;
import java.io.Serializable;
import java.util.Random;
import java.util.Set;

public class SpawnEntities implements Serializable {
    private GamePanel gp;
    private Terrain t;
    public int spawnRate = 60000; // [ms]
    public Timer spawnTimer;
    private long lastSpawnTime = 0;

    public SpawnEntities(GamePanel gp, Terrain t) {
        this.gp = gp;
        this.t = t;
        spawnTimer = new Timer(spawnRate, e -> spawn());
        spawnTimer.start();
        spawn();
    }

    public void spawn() {
        if (gp.enemies.size() >= 32) return;
        lastSpawnTime = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            int row = randomRange((int)t.toCell(gp.player.x, gp.player.y).x - 30, (int)t.toCell(gp.player.x, gp.player.y).x + 30);
            int col = randomRange((int)t.toCell(gp.player.x, gp.player.y).y - 30, (int)t.toCell(gp.player.x, gp.player.y).y + 30);
            Vector2 globalPos = t.toGlobal(col, row);
            float dx = (int)globalPos.x - gp.player.x;
            float dy = (int)globalPos.y - gp.player.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (t.getTile(row, col) == 5 || t.getTile(row + 1, col) == 5 || distance < 500) {
                i--; // Retry this iteration
                continue;
            }
            gp.spawnEntity(new Alien(gp, gp.player, (int)globalPos.x, (int)globalPos.y), 10);
        }
        while (true) {
            int row = randomRange((int) t.toCell(gp.player.x, gp.player.y).x - 30, (int) t.toCell(gp.player.x, gp.player.y).x + 30);
            int col = randomRange((int) t.toCell(gp.player.x, gp.player.y).y - 30, (int) t.toCell(gp.player.x, gp.player.y).y + 30);
            Vector2 globalPos = t.toGlobal(col, row);
            float dx = (int) globalPos.x - gp.player.x;
            float dy = (int) globalPos.y - gp.player.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (t.getTile(row, col) == 5 || t.getTile(row + 1, col) == 5 || distance < 500) {
                continue;
            }
            gp.spawnEntity(new Vendor(gp, gp.player, (int) globalPos.x, (int) globalPos.y), 10);
            break;
        }
    }


    private int randomRange(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public long getTimeToNextSpawn() {
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - lastSpawnTime;
        return spawnRate - timeElapsed;
    }
}
