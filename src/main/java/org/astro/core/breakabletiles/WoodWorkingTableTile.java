package org.astro.core.breakabletiles;

import org.astro.core.*;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.Items;
import org.astro.core.particlesystem.Particle;
import org.astro.core.particlesystem.ParticleGroup;
import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;
import org.newdawn.slick.*;

import java.io.Serializable;
import java.util.*;

public class WoodWorkingTableTile extends BreakableTile implements Save {
    private final SpriteSheet spriteSheet;
    private boolean animationFrame = false;
    private int time = 0;

    private String cuttingName;
    private Image cutting;

    private int cutTime = 0;
    private int particleSpawnTime = 0;  // New variable for particle spawn timing

    public static final int animationSpeed = ClassSettings.loadInt("wood working table tile/animation speed", 50);
    public static final int cuttingTime = ClassSettings.loadInt("wood working table tile/cutting time", 5500);
    public static final int particleSpawnInterval = ClassSettings.loadInt("wood working table tile/particle spawn interval", 300); // New constant for particle spawn interval

    public static final Map<String, Drop> dropTable = new HashMap<>();

    public static void baseDropTable() {
        dropTable.put("plank", new Drop("stick", 4));
        dropTable.put("torch", new Drop("stick", 1));
        dropTable.put("stick", new Drop("torch", 1));
    }

    public WoodWorkingTableTile(float x, float y) {
        super(x, y, Items.items.get("wood_working_table"));
        try {
            spriteSheet = new SpriteSheet("art/png/woodWorkingTable.png", 16, 16);
            spriteSheet.setFilter(Image.FILTER_NEAREST);
            sprite = spriteSheet.getSprite(0, 0);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        Astro.astro.spawn(this);
        Saving.save.add(this);
    }

    public WoodWorkingTableTile(Object o) {
        super(((Data) o).x, ((Data) o).y, Items.items.get("wood_working_table"));
        try {
            spriteSheet = new SpriteSheet("art/png/woodWorkingTable.png", 16, 16);
            spriteSheet.setFilter(Image.FILTER_NEAREST);
            sprite = spriteSheet.getSprite(0, 0);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        Astro.astro.spawn(this);
        Saving.save.add(this);
    }

    @Override
    public void update() {
        time += Astro.delta;
        if (time >= animationSpeed) {
            time = 0;
            animationFrame = !animationFrame;
        }

        if (cutting != null) {
            cutTime += Astro.delta;
            particleSpawnTime += Astro.delta; // Track time for particle spawn

            if (particleSpawnTime >= particleSpawnInterval) {
                spawnParticles();
                particleSpawnTime = 0; // Reset particle spawn timer
            }
        }

        if (cutTime >= cuttingTime) {
            dropCutItem();
            cutTime = 0;
            cutting = null;
            cuttingName = null;
        }
    }

    private void spawnParticles() {
        if (cutting != null) {
            List<Color> colors = new ArrayList<>();
            for (int x = 0; x < cutting.getWidth() - 1; x++) {
                for (int y = 0; y < cutting.getHeight() - 1; y++) {
                    try {
                        colors.add(cutting.getColor(x, y));
                    } catch (ArrayIndexOutOfBoundsException e) {}
                }
            }

            int velX = 60;
            int velY = 700;
            ParticleGroup pg = new ParticleGroup(x + width / 2f + 3, y + height / 2f, true);
            pg.play(() -> {
                Particle p = new Particle(pg, 0, 0, Utils.randomRange(-velX, velX), Utils.randomRange(-velY, velY),
                        Utils.randomRange(0, 10) == 1 ? Color.lightGray : colors.get(Utils.randomRange(0, colors.size() - 1)), 0.01f, 10);
                p.physicsProperties(0.1f, 0, 0);
                p.setSize(10, 10);
                return p;
            }, 50);
        }
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(spriteSheet.getSprite(0, animationFrame ? 0 : 1).getScaledCopy(width, height), x, y);

        if (cutting != null) {
            g.drawImage(cutting, x + width / 2f - cutting.getWidth() / 2f + 3, y + ((float) cutTime / cuttingTime) * height - cutting.getHeight() / 2f);
        }
    }

    @Override
    public void deSpawn() {
        dropCutItem();
        Saving.save.remove(this);
    }

    @Override
    public Object save() {
        dropCutItem();
        return new Data(x, y);
    }

    private void dropCutItem() {
        if (cutting == null) return;
        for (int i = 0; i < dropTable.get(cuttingName).dropCount; i++) {
            new Item(Items.items.get(dropTable.get(cuttingName).dropName), x + Utils.randomRange(0, 60), y + height);
        }
    }

    @Override
    public void onInteract() {
        if (PlayerInventory.hand != null && (Objects.equals(PlayerInventory.hand.it.name, "plank") || Objects.equals(PlayerInventory.hand.it.name, "torch"))) {
            try {
                cutting = new Image(PlayerInventory.hand.it.spritePath).getScaledCopy(60, 60);
                cutting.setFilter(Image.FILTER_NEAREST);
            } catch (SlickException e) {
                throw new RuntimeException(e);
            }

            cuttingName = PlayerInventory.hand.it.name;

            PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
            PlayerInventory.hand = null;
        }
    }

    private record Data(float x, float y) implements Serializable {}

    public record Drop(String dropName, int dropCount) {}
}
