package org.astro.menu;

import java.io.*;
import java.util.Properties;

public class SettingsManager {
    private static final String SETTINGS_FILE = "settings.properties";

    public static int getWindowWidth() {
        Properties properties = loadProperties();
        return Integer.parseInt(properties.getProperty("window.width", "1280"));
    }

    public static int getWindowHeight() {
        Properties properties = loadProperties();
        return Integer.parseInt(properties.getProperty("window.height", "720"));
    }

    public static void setWindowWidth(int width) {
        saveProperty("window.width", String.valueOf(width));
    }

    public static void setWindowHeight(int height) {
        saveProperty("window.height", String.valueOf(height));
    }


    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return properties;
    }

    private static void saveProperty(String key, String value) {
        Properties properties = loadProperties();
        properties.setProperty(key, value);
        try (OutputStream output = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}
