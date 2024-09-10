package org.astro.core.saving;

public interface Savable {
    Object save();
    void load(Object o);
}
