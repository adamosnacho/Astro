package org.astro.core.saving;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static org.astro.core.Terrain.world;

public class Saving {
    public static Set<Save> save = new HashSet<>();

    public static void save() {
        // Ensure the data directory exists
        try {
            deleteDirectoryRecursively(Path.of(world + "/data"));
            Files.createDirectories(Path.of(world + "/data"));
        } catch (IOException e) {
            throw new RuntimeException("Error creating directories", e);
        }

        // Save each object in the 'save' set
        for (Save s : save) {
            String filename = world + "/data/" + s.getClass().getName();
            File f = new File(filename + "-0");
            int i = 1;
            while (f.exists()) {
                f = new File(filename + "-" + i);
                i++;
            }
            try (FileOutputStream fos = new FileOutputStream(f);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(s.save());
            } catch (IOException e) {
                System.err.println("Error while saving " + f.getPath());
                throw new RuntimeException(e);
            }
        }
    }

    public static Map<String, Object> load() {
        Map<String, Object> instants = new HashMap<>();
        try {
            Files.createDirectories(Path.of(world + "/data"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File dir = new File(world + "/data/");
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            try (FileInputStream fis = new FileInputStream(f); ObjectInputStream ois = new ObjectInputStream(fis)) {
                String className = f.getName().split("-")[0];
                Object data = ois.readObject();
                Class<?> clazz = Class.forName(className);
                Save instance = (Save) clazz.getDeclaredConstructor(Object.class).newInstance(data);
                instants.put(f.getName(), instance);
            } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                System.err.println("Error loading saved class " + f.getName());
                throw new RuntimeException(e);
            }
        }
        return instants;
    }

    public static void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}