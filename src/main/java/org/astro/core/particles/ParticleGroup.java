package org.astro.core.particles;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class ParticleGroup {
    public Set<Particle> particles = new HashSet<>();
    public float x, y;

    public ParticleGroup(float groupX, float groupY) {
        x = groupX;
        y = groupY;
    }

    public void generateParticles(ParticleGenerator pg, int amount) {
        for (int i = 0; i < amount; i++) {
            particles.add(pg.create());
        }
        ParticleSystem.particleGroups.add(this);
    }

    public void dispose() {
        ParticleSystem.particleGroups.remove(this);
    }

    public void draw(Graphics g) {
        g.translate((int) x, (int) y);
        for (Particle p : particles) {
            p.draw(g);
        }
        g.translate((int) -x, (int) -y);
    }

    public void update(double deltaTime) {
        for (Particle p : particles) {
            p.update(deltaTime);
        }
    }

    public interface ParticleGenerator {
        Particle create();
    }
}
