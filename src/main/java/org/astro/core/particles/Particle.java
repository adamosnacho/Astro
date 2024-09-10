package org.astro.core.particles;

import javax.swing.*;
import java.awt.*;

public class Particle {
    public Color color;
    public float x, y;
    public float velX, velY;
    public float size;
    public float drag = 0.01f;
    public float sizeChange;
    public float constantY = 0;
    public float constantX = 0;
    public Timer timer;

    public Particle(ParticleGroup particleGroup, float x, float y, float velX, float velY, Color color, float size, float sizeChange, int lifeTime) {
        this.x = x;
        this.y = y;
        this.velX = velX;
        this.velY = velY;
        this.color = color;
        this.size = size;
        this.sizeChange = sizeChange;
        timer = new Timer(lifeTime, e -> delete(particleGroup));
        timer.start();
    }

    private void delete(ParticleGroup particleGroup) {
        particleGroup.particles.remove(this);
        timer.stop();
    }

    public void update(double dt) {
        // Update position based on velocity and deltaTime
        x += velX * dt * 10;
        y += velY * dt * 10;

        // Update velocity based on constant acceleration
        velX += constantX * dt * 10;
        velY += constantY * dt;

        // Apply drag, drag should be proportional to dt
        velX *= 1 - (drag * dt * 10);
        velY *= 1 - (drag * dt * 10);

        size *= 1 - (sizeChange * dt * 10);
    }


    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect((int) (x - size / 2), (int) (y - size / 2), (int) size, (int) size);
    }
}
