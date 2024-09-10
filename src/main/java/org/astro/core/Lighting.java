package org.astro.core;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Lighting implements Serializable {
    private GamePanel gp;
    private List<LightSource> lightSources;
    private transient BufferedImage lightMap;
    public float quality = 0.25f;
    public final int playerLightSource;
    private float ambientLightIntensity;

    public Lighting(GamePanel gp) {
        this.gp = gp;
        this.lightSources = new ArrayList<>();
        this.lightMap = new BufferedImage((int) (gp.getWidth() * quality), (int) (gp.getHeight() * quality), BufferedImage.TYPE_INT_ARGB);

        // Add an initial light source at (50000, 50000) with a large radius and high intensity
        playerLightSource = addLightSource(50000, 50000, 400, 0.7f);
    }

    public synchronized int addLightSource(int x, int y, int radius, float intensity) {
        LightSource lightSource = new LightSource(x, y, radius, intensity);
        lightSources.add(lightSource);
        return lightSources.size() - 1;
    }

    public synchronized void setLightSourcePosition(int index, int x, int y) {
        lightSources.get(index).x = x;
        lightSources.get(index).y = y;
        // No need to regenerate texture when only moving the light
    }

    public synchronized void setLightSourceIntensity(int index, float intensity) {
        LightSource light = lightSources.get(index);
        if (light.intensity != intensity) { // Only recalculate if intensity changes
            light.intensity = intensity;
            light.generateLightTexture(); // Recalculate the light texture
        }
    }

    public synchronized void setLightSourceRadius(int index, int radius) {
        LightSource light = lightSources.get(index);
        if (light.radius != radius) { // Only recalculate if radius changes
            light.radius = radius;
            light.generateLightTexture(); // Recalculate the light texture
        }
    }

    public synchronized void removeLightSource(int index) {
        if (index >= 0 && index < lightSources.size()) {
            lightSources.remove(index);
        }
    }

    public synchronized void clearLightSources() {
        lightSources.clear();
    }

    private void updateAmbientLight() {

        int cycleTime = 600; // 10 minutes cycle (600 seconds)
        int timeInCycle = (int) (gp.time % cycleTime);

        float timeOfDay = (float) timeInCycle / cycleTime;

        ambientLightIntensity = timeOfDay <= 0.5f ? timeOfDay * 2 : (1.0f - timeOfDay) * 2;

    }

    public synchronized void generateLightMap() {

        updateAmbientLight();

        Graphics2D lmGraphics = lightMap.createGraphics();

        // Use AlphaComposite.Src to avoid clearing issues
        lmGraphics.setComposite(AlphaComposite.Src);

        // Fill the entire map with ambient light
        lmGraphics.setColor(new Color(0, 0, 0, (int) (ambientLightIntensity * 255)));
        lmGraphics.fillRect(0, 0, lightMap.getWidth(), lightMap.getHeight());

        // Draw each light source's pre-generated texture
        for (LightSource light : lightSources) {
            drawLight(lmGraphics, light);
        }

        lmGraphics.dispose();

    }

    private synchronized void drawLight(Graphics2D lmGraphics, LightSource light) {

        // Calculate the position relative to the camera
        int screenX = (int) ((light.x - gp.cameraX) * quality) - (light.lightTexture.getWidth() / 2);
        int screenY = (int) ((light.y - gp.cameraY) * quality) - (light.lightTexture.getHeight() / 2);

        // Set the blending mode to subtract light (DstOut)
        lmGraphics.setComposite(AlphaComposite.DstOut);
        lmGraphics.drawImage(light.lightTexture, screenX, screenY, null);
    }

    public BufferedImage getLightMap() {
        return lightMap;
    }

    public void draw(Graphics g) {
        // Set the player light source position to follow the player
        setLightSourcePosition(playerLightSource, gp.player.x + gp.player.width / 2, gp.player.y + gp.player.height / 2);
        generateLightMap();
        g.drawImage(getLightMap(), gp.cameraX, gp.cameraY, gp.getWidth(), gp.getHeight(), null);
    }

    private class LightSource implements Serializable {
        int x, y;
        int radius;
        float intensity;
        BufferedImage lightTexture;

        LightSource(int x, int y, int radius, float intensity) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.intensity = intensity;
            generateLightTexture(); // Pre-generate light texture when light is created
        }

        // Generate a pre-calculated light texture (like the RadialGradientPaint)
        public void generateLightTexture() {

            int textureSize = (int) (radius * 2 * quality);
            lightTexture = new BufferedImage(textureSize, textureSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = lightTexture.createGraphics();

            // Manually draw a radial gradient-like texture with custom falloff
            for (int y = 0; y < textureSize; y++) {
                for (int x = 0; x < textureSize; x++) {
                    // Calculate the distance from the center
                    int dx = x - textureSize / 2;
                    int dy = y - textureSize / 2;
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance <= textureSize / 2) {
                        // Linear falloff from center to edge
                        float falloff = (float) (1 - (distance / (textureSize / 2.0)));
                        int alpha = (int) (falloff * intensity * 255);

                        // Set the pixel with the calculated alpha
                        lightTexture.setRGB(x, y, new Color(0, 0, 0, alpha).getRGB());
                    }
                }
            }

            g2d.dispose();
        }
    }
}
