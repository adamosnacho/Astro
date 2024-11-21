package org.astro.core;

import org.newdawn.slick.Graphics;
import java.util.Random;

public class Camera {
    public float x;
    public float y;
    public float width;
    public float height;
    public Entity follow;

    // Screen shake variables
    private float shakeIntensity = 0;
    private float shakeDuration = 0;
    private float shakeElapsed = 0;
    private float offsetX = 0; // Shake offset in x-direction
    private float offsetY = 0; // Shake offset in y-direction
    private Random random;

    public Camera(float width, float height, Entity follow) {
        this.width = width;
        this.height = height;
        this.x = 0;
        this.y = 0;
        this.follow = follow;
        this.random = new Random();
        update();
    }

    public void update() {
        // Center the camera on the entity
        this.x = follow.x + (float) follow.width / 2 - width / 2;
        this.y = follow.y + (float) follow.height / 2 - height / 2;

        // Check if shake is active
        if (shakeDuration > 0) {
            shakeElapsed += Astro.delta / 1000f; // Assume Astro.delta is in milliseconds

            if (shakeElapsed < shakeDuration) {
                // Reduce shake intensity over time for smooth stopping
                float currentShakeIntensity = shakeIntensity * (1 - (shakeElapsed / shakeDuration));

                // Random offset for shake effect
                offsetX = (random.nextFloat() - 0.5f) * 2 * currentShakeIntensity;
                offsetY = (random.nextFloat() - 0.5f) * 2 * currentShakeIntensity;
            } else {
                // Shake effect ended, reset shake variables
                shakeDuration = 0;
                shakeElapsed = 0;
                offsetX = 0;
                offsetY = 0;
            }
        } else {
            // No shake active, ensure offsets are zero
            offsetX = 0;
            offsetY = 0;
        }
    }

    public void apply(Graphics g) {
        // Only apply shake offsets if shake is active
        if (shakeDuration > 0) {
            g.translate(-x + offsetX, -y + offsetY);
            g.rotate(x + width / 2, y + height / 2, offsetX / 5f);
        } else {
            // Translate without shake offset
            g.translate(-x, -y);
        }
    }

    public void reset(Graphics g) {
        g.resetTransform(); // Reset the transformation for UI or other elements
    }

    /**
     * Triggers a screen shake effect.
     *
     * @param intensity The intensity of the shake.
     * @param duration  The duration of the shake.
     */
    public void shake(float intensity, float duration) {
        if (shakeDuration > 0) {
            // Stack shake by adding to intensity and extending duration
            if (shakeIntensity + intensity < 10) shakeIntensity += intensity;
        } else {
            shakeIntensity = intensity;
            shakeElapsed = 0; // Reset elapsed time
        }
        shakeDuration = duration;
    }

    public void stopShake() {
        shakeElapsed = 0;
        shakeDuration = 0;
        shakeIntensity = 0;
    }
}
