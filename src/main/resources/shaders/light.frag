#version 120

const int MAX_LIGHTS = 100;

struct Light {
    vec2 position;
    float radius;
    float intensity;
    float falloff; // Controls the speed of the light fading
};

uniform Light lights[MAX_LIGHTS];
uniform int lightCount;

uniform float brightness;

void main() {
    vec4 color = vec4(0.0);
    color.a = 1 - brightness;
    vec2 fragPos = gl_FragCoord.xy;

    for (int i = 0; i < lightCount; i++) {
        Light light = lights[i];
        float dist = distance(light.position, fragPos);

        if (dist < light.radius) {
            // Compute the normalized distance within [0, 1]
            float normalizedDist = dist / light.radius;

            // Apply falloff using a power function
            float attenuation = (1.0 - pow(normalizedDist, light.falloff)) * light.intensity;

            // Accumulate the alpha value
            color.a += attenuation;
        }
    }

    // Clamp alpha to avoid overflows (can exceed 1.0 if multiple lights overlap)
    color.a = clamp(1 - color.a, 0.0, 1.0);

    gl_FragColor = color;
}
