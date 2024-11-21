package org.astro.core.lighting;

import org.astro.core.Astro;
import org.astro.core.ShaderUtil;
import org.astro.core.TimeManager;
import org.lwjgl.opengl.GL20;
import org.newdawn.slick.geom.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class LightManager {
    public static int lightShader; // Shader program ID
    public static final List<Light> lights = new ArrayList<>();

    static {
        try {
            lightShader = ShaderUtil.loadShader("shaders/light.vert", "shaders/light.frag");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class Light {
        public Vector2f position;
        public float radius;
        public float intensity;
        public float falloff;

        public Light(float x, float y, float radius, float intensity, float falloff) {
            this.position = new Vector2f(x, y);
            this.radius = radius;
            this.intensity = intensity;
            this.falloff = falloff;
        }
    }

    public static void renderLights() {
        GL20.glUseProgram(lightShader); // Use the light shader program

        // Loop through each light and send data to the shader
        for (int i = 0; i < lights.size(); i++) {
            Light light = lights.get(i);

            // Convert the light's world position to screen space (adjust for camera offset)
            float lightScreenX = light.position.x - Astro.astro.camera.x;
            float lightScreenY = Astro.astro.camera.y - light.position.y + Astro.astro.camera.height;

            // Pass light data to the shader
            int positionLocation = GL20.glGetUniformLocation(lightShader, "lights[" + i + "].position");
            int radiusLocation = GL20.glGetUniformLocation(lightShader, "lights[" + i + "].radius");
            int intensityLocation = GL20.glGetUniformLocation(lightShader, "lights[" + i + "].intensity");
            int falloffLocation = GL20.glGetUniformLocation(lightShader, "lights[" + i + "].falloff");

            // Pass the screen-space position of the light to the shader
            GL20.glUniform2f(positionLocation, lightScreenX, lightScreenY);
            GL20.glUniform1f(radiusLocation, light.radius);
            GL20.glUniform1f(intensityLocation, light.intensity);
            GL20.glUniform1f(falloffLocation, light.falloff);
        }

        // Pass light count to the shader
        int lightCountLocation = GL20.glGetUniformLocation(lightShader, "lightCount");
        GL20.glUniform1i(lightCountLocation, lights.size());

        // Pass brightness to the shader
        int brightnessLocation = GL20.glGetUniformLocation(lightShader, "brightness");
        GL20.glUniform1f(brightnessLocation, TimeManager.brightness);

        // Render the full screen quad to apply the lighting effect
        renderFullScreenQuad();

        GL20.glUseProgram(0); // Unbind the shader program
    }

    private static void renderFullScreenQuad() {
        // Render a fullscreen quad for lighting effect
        org.lwjgl.opengl.GL11.glBegin(org.lwjgl.opengl.GL11.GL_QUADS);
        org.lwjgl.opengl.GL11.glVertex2f(-1, -1);
        org.lwjgl.opengl.GL11.glVertex2f(1, -1);
        org.lwjgl.opengl.GL11.glVertex2f(1, 1);
        org.lwjgl.opengl.GL11.glVertex2f(-1, 1);
        org.lwjgl.opengl.GL11.glEnd();
    }
}
