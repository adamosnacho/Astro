package org.astro.core.enemies;

import jdk.jshell.execution.Util;
import org.astro.core.Astro;
import org.astro.core.ClassSettings;
import org.astro.core.Enemy;
import org.astro.core.Utils;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.Items;
import org.astro.core.particlesystem.Particle;
import org.astro.core.particlesystem.ParticleGroup;
import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;
import org.newdawn.slick.*;

import java.io.Serializable;

public class Alien extends Enemy implements Save {
    private final SpriteSheet spriteSheet;
    private final Image speer;
    private final Image speerLeft;
    private float animationTimer = 0;
    public float animationSpeed = ClassSettings.loadFloat("alien/animation speed", 200f); // speed of the walking animation
    public int animationFrame = 0;

    public static final String dropName = ClassSettings.loadString("alien/drop", "matter");
    public static final int dropChance = ClassSettings.loadInt("alien/drop chance", 3);

    public static final float spearDamage = ClassSettings.loadFloat("alien/spear damage", 12f);
    public static final float spearAttackInterval = ClassSettings.loadFloat("alien/spear attack interval", 500f);; // time in ms
    public static final float spearAttackDistance = ClassSettings.loadFloat("alien/spear attack distance", 180f);; // time in ms
    public static final float spearAttackSpeed = ClassSettings.loadFloat("alien/spear attack speed", 1f);
    public static final float spearReturnSpeed = ClassSettings.loadFloat("alien/spear return speed", 0.1f);
    public static final float spearAttackTravel = ClassSettings.loadFloat("alien/spear attack travel", 50f);
    private float attackTimer = 0;
    private float spearX = 0;
    private boolean returning = false;
    private boolean attacking = false;
    private boolean hit = false;

    private final Sound alienWalk;
    private final Sound alienHit;

    public Alien(float x, float y) {
        width = 99;
        height = 144;
        Astro.astro.spawn(this);
        z = 0;
        try {
            spriteSheet = new SpriteSheet("art/png/alien.png", width, height);
            speer = new Image("art/png/spear.png", false, Image.FILTER_NEAREST).getScaledCopy(75, 75);
            speerLeft = new Image("art/png/spear.png", false, Image.FILTER_NEAREST).getScaledCopy(75, 75);
            speer.rotate(135);
            speerLeft.rotate(-45);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        this.x = x;
        this.y = y;
        hitColor = new Color(38, 110, 14);
        Saving.save.add(this);
        try {
            alienWalk = new Sound("sfx/playerStep.ogg");
            alienHit = new Sound("sfx/hit.ogg");
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
    }

    public Alien(Object o) {
        Data d = (Data) o;
        width = 99;
        height = 144;
        Astro.astro.spawn(this);
        z = 0;
        try {
            spriteSheet = new SpriteSheet("art/png/alien.png", width, height);
            speer = new Image("art/png/spear.png", false, Image.FILTER_NEAREST).getScaledCopy(75, 75);
            speerLeft = new Image("art/png/spear.png", false, Image.FILTER_NEAREST).getScaledCopy(75, 75);
            speer.rotate(135);
            speerLeft.rotate(-45);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        this.x = d.x;
        this.y = d.y;
        hp = d.hp;
        hitColor = new Color(38, 110, 14);
        Saving.save.add(this);
        try {
            alienWalk = new Sound("sfx/playerStep.ogg");
            alienHit = new Sound("sfx/hit.ogg");
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update() {
        if (dying) {
            updateDeath();
        } else {
            int sx = (int) x;
            int sy = (int) x;
            pathFind(Astro.astro.player.x, Astro.astro.player.y);
            if (isPlayerInRange(spearAttackDistance)) attackTimer += Astro.delta;
            else attackTimer = 0;

            if (isPlayerInRange(spearAttackDistance) && attackTimer >= spearAttackInterval && !attacking) {
                attacking = true;
                attackTimer = 0;
            }

            if (attacking) {
                if (returning) spearX -= spearReturnSpeed * Astro.delta;
                else spearX += spearAttackSpeed * Astro.delta;

                if (spearAttackTravel <= spearX) {
                    attackTimer = 0;
                    returning = true;
                }
                if (spearX <= 0) {
                    spearX = 0;
                    returning = false;
                    attacking = false;
                    hit = false;
                    return;
                }

                if (!returning && isPlayerInRange(spearAttackDistance) && !hit) {
                    attackPlayer();
                    alienHit.play(0.5f + Utils.randomRange(0, 50) / 100f, 0.2f);
                    hit = true;
                }
            }

            if (sx != (int) x || sy != (int) y) walkAnimation();
        }
    }

    private boolean isPlayerInRange(float range) {
        float dx = Astro.astro.player.x - x;
        float dy = (Astro.astro.player.y - y) * 1.8f;
        return Math.sqrt(dx * dx + dy * dy) < range;
    }

    private void attackPlayer() {
        ParticleGroup pg = new ParticleGroup(
                Astro.astro.player.x + (facingLeft ? Astro.astro.player.width - 15 : 15),
                y + 55 - (animationFrame == 3 || animationFrame == 2 ? 5 : 0),
                true
        );
        int vel = 300;
        pg.play(() -> {
            Particle pa = new Particle(pg, 0, 0, Utils.randomRange(-vel, vel), Utils.randomRange(-vel, vel),
                    (Utils.randomRange(0, 2) == 1 ? Color.white : Color.lightGray), 0.001f, 1);
            pa.physicsProperties(0.3f, 0, 5);
            pa.setSize(20, 20);
            return pa;
        }, 10);
        Astro.astro.player.suitWear -= spearDamage;
        Astro.astro.camera.shake(4, 1.5f);
    }

    @Override
    public void render(Graphics g) {
        Image sprite = spriteSheet.getSprite(0, animationFrame).getFlippedCopy(false, false);
        sprite.setFilter(Image.FILTER_NEAREST);
        sprite = sprite.getFlippedCopy(facingLeft, false);
        if (!dying) {
            g.drawImage(sprite, x, y);
            g.drawImage(facingLeft ? speerLeft : speer, x + (facingLeft ? Astro.astro.player.width - speer.getWidth() - 30 - spearX : 30 + spearX), y + 40 - (animationFrame == 3 || animationFrame == 2 ? 5 : 0));
        }
        else renderDie(g, sprite);
    }

    private void walkAnimation() {
        animationTimer += Astro.delta;
        if (animationTimer < animationSpeed) return;
        animationFrame++;
        animationFrame = animationFrame > 3 ? 0 : animationFrame;
        animationTimer = 0;
        if (animationFrame % 4 == 0) {
            float distance = Utils.distance(x, y, Astro.astro.player.x, Astro.astro.player.y);
            float volume = Math.max(0, 1 - (distance / 1000)); // Ensure volume is between 0 and 1
            alienWalk.play(Utils.randomRange(5, 10) / 10f, volume * 0.8f);
        }
    }

    @Override
    public void finishDie() {
        Astro.astro.deSpawn(this);
        if (Utils.randomRange(0, dropChance) == 0) new Item(Items.items.get(dropName), x + Utils.randomRange(0, width - 40), y + Utils.randomRange(0, height - 40));
    }

    @Override
    public Object save() {
        return new Data(x, y, hp);
    }

    @Override
    public void deSpawn() {
        Saving.save.remove(this);
        enemies.remove(this);
    }

    @Override
    public String getInfo() {
        return "Alien\nHP " + hp;
    }

    private record Data(float x, float y, float hp) implements Serializable {}
}
