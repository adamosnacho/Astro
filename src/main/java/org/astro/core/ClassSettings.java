package org.astro.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class ClassSettings {
    /**
     * Loads a float value from a class setting file. If the value is not present, writes the default value to the file.
     *
     * @param loc Location of the setting in the form "dir1/dir2/option"
     * @param def Default value to use if the setting is not found
     * @return The loaded float value or the default value
     */
    public static float loadFloat(String loc, float def) {
        return loadSetting(loc, String.valueOf(def), Float::parseFloat);
    }

    /**
     * Loads an integer value from a class setting file. If the value is not present, writes the default value to the file.
     *
     * @param loc Location of the setting in the form "dir1/dir2/option"
     * @param def Default value to use if the setting is not found
     * @return The loaded integer value or the default value
     */
    public static int loadInt(String loc, int def) {
        return loadSetting(loc, String.valueOf(def), Integer::parseInt);
    }

    private static <T> T loadSetting(String loc, String defaultValue, ValueParser<T> parser) {
        try {
            // Parse directory and setting name
            String[] pth = loc.split("/");
            String dir = String.join("/", Arrays.copyOf(pth, pth.length - 1));
            String optionName = pth[pth.length - 1];

            // Define the path to the .cls file
            Path dirPath = Path.of("cls");
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(dir + ".cls");

            // Load existing settings from the file into a map
            Map<String, String> settings = new HashMap<>();
            if (!Files.exists(filePath)) Files.createFile(filePath);

            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                String[] t = line.split("===");
                if (t.length == 2) {
                    settings.put(t[0], t[1]);
                }
            }

            // If the setting is already present, return its value
            if (settings.containsKey(optionName)) {
                return parser.parse(settings.get(optionName));
            }

            // If the setting is not found, append it with the default value
            settings.put(optionName, defaultValue);
            Files.writeString(filePath, optionName + "===" + defaultValue + System.lineSeparator(),
                    StandardOpenOption.APPEND);
            return parser.parse(defaultValue);
        } catch (IOException e) {
            throw new RuntimeException("Error while loading or writing configuration", e);
        }
    }

    /**
     * Functional interface for parsing setting values.
     *
     * @param <T> Type of the parsed value
     */
    @FunctionalInterface
    private interface ValueParser<T> {
        T parse(String value);
    }
}
