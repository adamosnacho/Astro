package org.astro.core;

import org.astro.core.menusystem.Menu;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Settings {
    public static boolean active = false;
    public static final List<Setting<?>> settingsList = new ArrayList<>();
    private static int selectedSettingIndex = -1; // To keep track of which setting is selected for editing

    // Path to the settings file
    private static final String SETTINGS_FILE = "pref.s";
    private static Menu menu;
    private static boolean needToReload = false;

    public static void init() {
        try {
            menu = new Menu((int) (Astro.astro.camera.width - 400), (int) (Astro.astro.camera.height - 10), new Color(0, 0, 0, 0.7f), 200, 5);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }

        Settings.addSetting(new Settings.Category("Graphics"));
        Settings.addSetting(new Settings.Setting<>("V-sync", true, s -> {
            Astro.app.setVSync(s);
            Settings.Setting<Integer> vtfpss = Settings.findSetting("Target fps (will be used if v-sync is off)");
            assert vtfpss != null;
            vtfpss.setValue(vtfpss.getValue());
        }));
        Settings.addSetting(new Settings.Setting<>("Show fps", false, use -> Astro.app.setShowFPS(use)));
        Settings.addSetting(new Settings.Setting<>("Target fps (will be used if v-sync is off)", 80, fps -> {
            Settings.Setting<Boolean> vss = Settings.findSetting("V-sync");
            if (vss != null && !vss.getValue()) {
                Astro.app.setTargetFrameRate(fps);
            }
            else {
                Astro.app.setTargetFrameRate(10000);
            }
        }));
        Settings.addSetting(new Settings.Category("Help"));
        Settings.addSetting(new Settings.Setting<>("Show Instructions", true, use -> PlayerInventory.showInstructions = use));
        loadSetting();
    }

    // Method to render the settings GUI
    public static void renderGui(Graphics g) {
        if (!active) return;
        menu.display(g);
    }

    public static void update() {
        Input input = Astro.app.getInput();

        boolean p = input.isKeyPressed(Input.KEY_P);
        if (p && !active && Astro.astro.player.canMove) {
            active = true;
            Astro.astro.player.canMove = false; // Prevent player movement when settings are active
        } else if (p && active) {
            active = false;
            Astro.astro.player.canMove = true; // Re-enable player movement
        }

        if (active) menu.update();
        if (needToReload) {
            needToReload = false;
            saveSettings();
            loadSetting();
        }
    }

    public static <T> void addSetting(Setting<T> setting) {
        settingsList.add(setting);
    }


    // Method to save settings to a file
    public static void saveSettings() {
        Path path = Paths.get(SETTINGS_FILE);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (Setting<?> setting : settingsList) {
                writer.write(setting.name + "=" + setting.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to load settings from a file
    public static void loadSetting() {
        Path path = Paths.get(SETTINGS_FILE);
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String name = parts[0];
                        String value = parts[1];
                        System.out.println("loading setting " + name + " value: " + value);
                        // Find the setting by name and set its value
                        for (Setting<?> setting : settingsList) {
                            if (setting.name.equals(name)) {
                                if (setting.getValue() instanceof Integer) {
                                    // Safe cast to Setting<Integer> and set value
                                    ((Setting<Integer>) setting).setValue(Integer.parseInt(value));
                                } else if (setting.getValue() instanceof Boolean) {
                                    // Safe cast to Setting<Boolean> and set value
                                    ((Setting<Boolean>) setting).setValue(Boolean.parseBoolean(value));
                                }
                            }
                        }
                    }
                }
                System.out.println("Settings loaded from " + SETTINGS_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No settings file found, using default settings.");
        }

        menu.ui.clear();
        menu.addLabel(new Menu.Label("Settings"));

        for (Setting<?> setting : settingsList) {
            // Add label if it's a category
            if (setting instanceof Category) {
                menu.addLabel(new Menu.Label("___________________________"));
                menu.addLabel(new Menu.Label(setting.name));
            }

            // Only proceed with creating a toggle button if the setting is Boolean
            if (setting.getValue() instanceof Boolean) {
                Boolean initialValue = (Boolean) setting.getValue();

                // Set initial button colors based on the setting value
                Color bgColor = initialValue ? new Color(0, 220, 0) : new Color(220, 0, 0);
                Color hoverColor = initialValue ? new Color(0, 255, 0) : new Color(255, 0, 0);

                // Create the button with the appropriate colors and toggle functionality
                Menu.Button toggleButton = new Menu.Button(setting.name, bgColor, hoverColor, (int) (Astro.astro.camera.width - 600), 70, b -> {
                    // Safely cast to Setting<Boolean> since we verified the type
                    Setting<Boolean> booleanSetting = (Setting<Boolean>) setting;

                    // Toggle the Boolean value
                    Boolean newValue = !booleanSetting.getValue();
                    booleanSetting.setValue(newValue);

                    // Update button colors based on the new value
                    if (newValue) {
                        b.bg = new Color(0, 220, 0);       // Green for "On"
                        b.bgHover = new Color(0, 255, 0);  // Red hover when "On"
                    } else {
                        b.bg = new Color(220, 0, 0);       // Red for "Off"
                        b.bgHover = new Color(255, 0, 0);  // Green hover when "Off"
                    }
                });

                // Add the button to the menu
                menu.addButton(toggleButton);
            }

            if (setting.getValue() instanceof Integer) {
                menu.addLabel(new Menu.Label(setting.name + " | " + setting.getValue(), Astro.font));
                menu.addButton(new Menu.Button("-", new Color(220, 0, 0), new Color(255, 0, 0), 50, 50, Astro.fontBig, b -> {
                    Setting<Integer> integerSetting = (Setting<Integer>) setting;
                    integerSetting.setValue(integerSetting.getValue() - 1);
                    needToReload = true;
                }));
                menu.addButton(new Menu.Button("+", new Color(0, 220, 0), new Color(0, 255, 0), 50, 50, Astro.fontBig, b -> {
                    Setting<Integer> integerSetting = (Setting<Integer>) setting;
                    integerSetting.setValue(integerSetting.getValue() + 1);
                    needToReload = true;
                }));
            }
        }
        menu.addLabel(new Menu.Label("___________________________"));
        menu.addButton(new Menu.Button("Save & Exit", new Color(255, 118, 2), new Color(206, 95, 2), (int) (Astro.astro.camera.width - 600), 70, b -> {
            saveSettings();
            active = false;
            Astro.astro.player.canMove = true;
        }));
    }

    // Setting class to hold individual settings
    public static class Setting<T> {
        public final String name;
        private T value;
        private final Consumer<T> onChangeCallback;

        public Setting(String name, T initialValue, Consumer<T> onChangeCallback) {
            this.name = name;
            setValue(initialValue);
            this.onChangeCallback = onChangeCallback;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T newValue) {
            this.value = newValue;
            if (onChangeCallback != null) {
                onChangeCallback.accept(newValue);
            }
        }
    }

    // Category setting class to represent non-selectable categories
    public static class Category extends Setting<String> {
        public Category(String name) {
            super(name, null, null); // No value or callback for categories
        }
    }

    public static <T> Setting<T> findSetting(String name) {
        for (Setting<?> s : settingsList) {
            if (!(s instanceof Category) && Objects.equals(s.name, name)) {
                return (Setting<T>) s; // Cast to the appropriate type safely
            }
        }
        return null; // Return null if not found
    }

}
