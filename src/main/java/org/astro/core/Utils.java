package org.astro.core;

import java.util.Random;

public class Utils {
    public static int randomPercent(float[] probabilities) {
        Random random = new Random();
        double rand = random.nextDouble();
        double cumulativeProbability = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulativeProbability += probabilities[i];
            if (rand <= cumulativeProbability) {
                return i;
            }
        }
        return 0;
    }

    public static int randomRange(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min; // Generates a number in the range [min, max)
    }

    public static float clamp(float x, float min, float max) {
        return Math.max(Math.min(x, max), min);
    }

    public static int clamp(int x, int min, int max) {
        return Math.max(Math.min(x, max), min);
    }
}
