package org.astro.menu;

import org.astro.core.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModMenu extends JPanel {
    private JFrame frame;

    public ModMenu(JFrame frame) {
        this.frame = frame;
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        setBackground(new Color(34, 40, 49)); // Dark background color

        JPanel contentPanel = new JPanel();

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(34, 40, 49));

        JLabel title = createText("Mods", 72, new Color(225, 225, 225));

        contentPanel.add(Box.createVerticalStrut(100));
        contentPanel.add(title);
        contentPanel.add(Box.createVerticalStrut(100));
        JButton back = createButton("Back");
        back.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.getContentPane().add(new MainMenu(frame));
            frame.revalidate();
            frame.repaint();
        });
        contentPanel.add(back);
        File folder = new File("mods");
        File[] listOfFiles = folder.listFiles();
        System.out.println("Listing mods...");
        if (listOfFiles != null) {
            contentPanel.add(createText("---------------------------------------------", 20, new Color(225, 225, 225)));
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(file)) {
                        JarEntry entry = jarFile.getJarEntry("mod.properties");
                        if (entry != null) {
                            Properties properties = new Properties();
                            try (InputStream input = jarFile.getInputStream(entry)) {
                                properties.load(input);

                                contentPanel.add(createText(properties.getProperty("name") + " v" + properties.getProperty("version"), 20, new Color(253, 53, 53)));
                                contentPanel.add(createText(properties.getProperty("description"), 20, new Color(253, 53, 53)));
                                contentPanel.add(createText("----------------------------------------------", 20, new Color(225, 225, 225)));
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
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustButtonSizes();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                adjustButtonSizes();
            }
        });
        adjustButtonSizes();
    }

    private RoundedButton createButton(String text) {
        RoundedButton button = new RoundedButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Poppins", Font.PLAIN, 24));
        button.setBackground(new Color(0, 106, 181)); // Teal button background
        button.setForeground(new Color(255, 255, 255)); // White text
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(49, 137, 204)); // Darker teal when hovered
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 106, 181)); // Original color when not hovered
            }
        });

        return button;
    }

    private JLabel createText(String text, int fontSize, Color color) {
        JLabel l = new JLabel(text);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setFont(new Font("Poppins", Font.BOLD, fontSize)); // Large font size
        l.setForeground(color); // Text color
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private void adjustButtonSizes() {
        int width = frame.getWidth() - 700; // Adjust button width based on frame width, with padding
        Dimension size = new Dimension(width, 50); // Width dynamic, Height 50

        for (Component component : getComponents()) {
            if (component instanceof RoundedButton) {
                RoundedButton button = (RoundedButton) component;
                button.setPreferredSize(size);
                button.setMaximumSize(size);
                button.setMinimumSize(size);
            }
        }

        revalidate();
        repaint();
    }
}
