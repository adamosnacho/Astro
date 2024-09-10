package org.astro.core.particles;

import java.awt.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ParticleSystem {
    public static Set<ParticleGroup> particleGroups = new HashSet<>();

    public static void update(double dt) {
        Iterator<ParticleGroup> iterator = particleGroups.iterator();

        while (iterator.hasNext()) {
            ParticleGroup pg = iterator.next();
            pg.update(dt);
            if (pg.particles.isEmpty()) {
                iterator.remove();
            }
        }
    }

    public static void draw(Graphics g) {
        for (ParticleGroup pg : particleGroups) {
            pg.draw(g);
        }
    }
}
