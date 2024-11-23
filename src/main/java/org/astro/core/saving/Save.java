package org.astro.core.saving;

public interface Save {
    Object save();
    default void aboutToSave() {}
}
