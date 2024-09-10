package org.astro.menu;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter; // Import ComponentAdapter
import java.awt.event.ComponentEvent;
import java.io.IOException;

import org.astro.core.Game;


public class MainMenu extends JPanel {
    private JFrame frame;

    public MainMenu(JFrame frame) {
        this.frame = frame;
        init();
    }

    public MainMenu(JFrame frame, String crash) {
        this.frame = frame;
        init();
        JOptionPane.showMessageDialog(frame, "Crash occurred :[\n" + crash);
    }

    private void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(34, 40, 49)); // Dark background color
        try {
            frame.setIconImage(ImageIO.read(getClass().getResourceAsStream("/art/png/blueprint.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JLabel title = createLabel("Astro", 72, new Color(255, 255, 255)); // White color

        RoundedButton startButton = createButton("Play");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        RoundedButton modsButton = createButton("Mods");
        modsButton.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.getContentPane().add(new ModMenu(frame));
            frame.revalidate();
            frame.repaint();
        });

        RoundedButton settingsButton = createButton("Settings");
        settingsButton.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.getContentPane().add(new Settings(frame));
            frame.revalidate();
            frame.repaint();
        });

        RoundedButton creditsButton = createButton("Credits");
        creditsButton.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.getContentPane().add(new Credits(frame));
            frame.revalidate();
            frame.repaint();
        });

        RoundedButton exitButton = createButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));

        add(Box.createVerticalStrut(100));
        add(title);
        add(Box.createVerticalStrut(300));
        add(startButton);
        add(Box.createVerticalStrut(10));
        add(modsButton);
        add(Box.createVerticalStrut(10));
        add(settingsButton);
        add(Box.createVerticalStrut(10));
        add(creditsButton);
        add(Box.createVerticalStrut(10));
        add(exitButton);

        // Adjust button sizes based on frame size when frame is shown or resized
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

    private JLabel createLabel(String text, int fontSize, Color color) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(new Font("Poppins", Font.BOLD, fontSize));
        label.setForeground(color);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
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

    private void startGame() {
        frame.dispose();
        new Game();
    }
}
