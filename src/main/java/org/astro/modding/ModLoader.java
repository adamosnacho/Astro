package org.astro.modding;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModLoader implements Serializable {
    public List<Mod> loadedMods = new ArrayList<>();

    public void loadMods(String modsDir) {
        long startTime = System.currentTimeMillis();
        File folder = new File(modsDir);
        File[] listOfFiles = folder.listFiles();
        System.out.println("Loading mods...");
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(file)) {
                        JarEntry entry = jarFile.getJarEntry("mod.properties");
                        if (entry != null) {
                            Properties properties = new Properties();
                            try (InputStream input = jarFile.getInputStream(entry)) {
                                properties.load(input);
                                String mainClass = properties.getProperty("mainClass");
                                URL jarUrl = file.toURI().toURL();
                                URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl});
                                Class<?> modClass = classLoader.loadClass(mainClass);

                                if (Mod.class.isAssignableFrom(modClass)) {
                                    Mod modInstance = (Mod) modClass.getDeclaredConstructor().newInstance();
                                    loadedMods.add(modInstance);
                                    System.out.println("Loaded mod: " + properties.getProperty("name") + " v" + properties.getProperty("version"));
                                } else {
                                    System.err.println("Class " + modClass.getName() + " does not implement Mod interface, skipping.");
                                }
                            }
                        } else {
                            System.err.println("Mod descriptor (mod.properties) not found in " + file.getName() + ", skipping.");
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load mod: " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("No mods found! Loading game...");
            return;
        }

        System.out.println("Done loading " + loadedMods.size() + " mods. (" + (System.currentTimeMillis() - startTime) + "ms)");
    }
}
