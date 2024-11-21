package org.astro.core;

import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;

public class TimeManager implements Save {
    public static float brightness = 0f; // 0 - 1 | 1 is darkest
    public static float time = 0.5f; // current time in days
    public static float day = 360; // seconds in a day

    static {new TimeManager();}

    public TimeManager() {
        Saving.save.add(this);
    }

    public TimeManager(Object o) {
        time = (float) o;
    }

    public static void update() {
        // Increment time using delta time converted to days
        time += (Astro.delta / 1000f) / day;

        if (time > 1) time = 0;

        brightness = (1 + (float) Math.cos(2 * Math.PI * (time - 0.5))) / 2;
    }

    @Override
    public Object save() {
        return time;
    }
}
