package org.astro.core.particlesystem;

import org.astro.core.Astro;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class Particle {
    public float localX;
    public float localY;
    public float velX;
    public float velY;

    private final Color color;
    private final float lifetime;
    private final ParticleGroup pg;
    private float drag = 0.5f;
    private float gravityX = 0;
    private float gravityY = 0;
    private float width = 5;
    private float height = 5;
    private float time = 0;
    private final float sizeChange;

    public Particle(ParticleGroup pg, float localX, float localY, float startVelX, float startVelY, Color color, float sizeChange, float lifetime) {
        this.localX = localX;
        this.localY = localY;
        this.lifetime = lifetime;
        this.velX = startVelX;
        this.velY = startVelY;
        this.color = color;
        this.pg = pg;
        this.sizeChange = sizeChange;
    }

    public void physicsProperties(float drag, float gravityX, float gravityY) {
        this.gravityX = gravityX;
        this.gravityY = gravityY;
        this.drag = drag;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void update() {
        float delta = Astro.delta / 1000f;
        time += delta;

        localX += (velX + gravityX) * delta;
        localY += (velY + gravityY) * delta;

        // Apply drag to velocity, scaled by delta
        velX *= (float) Math.pow(drag, delta);
        velY *= (float) Math.pow(drag, delta);
        width *= (float) Math.pow(sizeChange, delta);
        height *= (float) Math.pow(sizeChange, delta);

        if (time >= lifetime) pg.particles.remove(this);
    }

    public void render(Graphics g) {
        g.setColor(color);
        g.fillRect(localX + pg.x, localY + pg.y, width, height);
    }
}
