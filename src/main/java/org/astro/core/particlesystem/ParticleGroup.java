package org.astro.core.particlesystem;

import org.astro.core.Astro;
import org.astro.core.Entity;
import org.newdawn.slick.Graphics;

import java.util.ArrayList;
import java.util.List;

public class ParticleGroup extends Entity {
    public List<Particle> particles = new ArrayList<>();
    private final boolean autoDestroy;

    public ParticleGroup(float x, float y, boolean autoDestroy) {
        this.x = x;
        this.y = y;
        this.autoDestroy = autoDestroy;
        if (!autoDestroy) Astro.astro.spawn(this);
    }

    public void play(ParticleGenerator pg, int amount) {
        if (autoDestroy) Astro.astro.spawn(this);
        for (int i = 0; i < amount; i++) {
            particles.add(pg.generate());
        }
    }

    @Override
    public void update() {
        for (Particle p : new ArrayList<>(particles)) {
            p.update();
        }

        if (autoDestroy && particles.isEmpty()) destroy();
    }

    @Override
    public void render(Graphics g) {
        for (Particle p : particles) {
            p.render(g);
        }
    }

    @Override
    public void deSpawn() {
        particles = null;
    }

    public void destroy() {
        Astro.astro.deSpawn(this);
    }

    public interface ParticleGenerator {
        Particle generate();
    }
}
