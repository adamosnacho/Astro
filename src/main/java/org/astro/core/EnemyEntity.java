package org.astro.core;

import java.io.Serializable;

public class EnemyEntity extends Entity implements Serializable {
    public float hp = 100;
    public int width;
    public int height;
    public void damage(int amount, boolean damageRight) {}
}
