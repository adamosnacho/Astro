package org.astro.core;

import java.io.Serializable;

public class Vector2 implements Serializable {
    public double x;
    public double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 add(Vector2 other) {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public Vector2 multiply(double z) {
        return new Vector2(x * z, y * z);
    }

    public Vector2 devide(double z) {
        return new Vector2(x / z, y / z);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }
}
