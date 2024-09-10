package org.astro.core.saving;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SaveManager {
    public static List<Savable> save = new ArrayList<>();
    public static String saveFolder = "world.sv";

    public static void save() {
        try {
            for (Savable s : save) {
                // Create directories if they don't exist
                String directoryPath = saveFolder + "/" + s.getClass().getPackage().getName().replace('.', '/');
                Files.createDirectories(Paths.get(directoryPath));

                // Create the file path
                String filePath = directoryPath + "/" + s.getClass().getSimpleName() + ".sv";

                // Delete the file if it already exists
                Files.deleteIfExists(Paths.get(filePath));

                // Save the object
                try (FileOutputStream fos = new FileOutputStream(filePath);
                     ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(s.save());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void load() {
        try {
            for (Savable s : save) {
                // Create the file path
                String filePath = saveFolder + "/" + s.getClass().getPackage().getName().replace('.', '/') + "/" + s.getClass().getSimpleName() + ".sv";
                File file = new File(filePath);

                if (file.exists()) {
                    // Load the object
                    try (FileInputStream fis = new FileInputStream(filePath); ObjectInputStream ois = new ObjectInputStream(fis)) {
                        Object obj = ois.readObject();
                        s.load(obj);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void register(Savable sv) {
        save.add(sv);
    }
    public static void deRegister(Savable sv) {
        save.remove(sv);
    }
}
