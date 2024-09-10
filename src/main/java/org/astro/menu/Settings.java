package org.astro.menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Properties;

public class Settings extends JPanel {
    private JFrame frame;
    private JTextField widthField;
    private JTextField heightField;
    private JLabel resolutionDisplayLabel;

    // Initial aspect ratio (width / height)
    private final double initialAspectRatio;

    public Settings(JFrame frame) {
        this.frame = frame;
        this.initialAspectRatio = 16.0 / 9.0;

        setLayout(new GridBagLayout());
        setBackground(new Color(34, 40, 49)); // Dark background color
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding around components
        gbc.anchor = GridBagConstraints.CENTER;
        Toolkit toolkit = Toolkit.getDefaultToolkit();

        // Get the screen size
        Dimension screenSize = toolkit.getScreenSize();
        JLabel title = createLabel("Settings, your display: " + screenSize.width + "x" + screenSize.height, 72, new Color(255, 255, 255)); // White color
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(title, gbc);

        // Create resolution settings panel
        JPanel resolutionPanel = new JPanel(new GridBagLayout());
        resolutionPanel.setBackground(new Color(34, 40, 49));

        JLabel widthLabel = createLabel("Width:", 24, new Color(255, 255, 255));
        JLabel heightLabel = createLabel("Height:", 24, new Color(255, 255, 255));

        widthField = createTextField(SettingsManager.getWindowWidth());
        widthField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateHeightFromWidth();
            }
        });

        heightField = createTextField(SettingsManager.getWindowHeight());
        heightField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateWidthFromHeight();
            }
        });

        JButton applyButton = createButton("Apply");
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int width = Integer.parseInt(widthField.getText());
                    int height = Integer.parseInt(heightField.getText());
                    SettingsManager.setWindowWidth(width);
                    SettingsManager.setWindowHeight(height);
                    resolutionDisplayLabel.setText(getResolutionDisplayText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter valid numbers for width and height.");
                }
            }
        });

        resolutionDisplayLabel = createLabel(getResolutionDisplayText(), 24, new Color(255, 255, 255));


        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        resolutionPanel.add(widthLabel, gbc);
        gbc.gridx = 1;
        resolutionPanel.add(widthField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        resolutionPanel.add(heightLabel, gbc);
        gbc.gridx = 1;
        resolutionPanel.add(heightField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        resolutionPanel.add(applyButton, gbc);
        gbc.gridy = 4;
        resolutionPanel.add(resolutionDisplayLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(resolutionPanel, gbc);

        JButton back = createButton("Back");
        back.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.getContentPane().add(new MainMenu(frame));
            frame.revalidate();
            frame.repaint();
        });
        gbc.gridy = 6;
        add(back, gbc);
    }

    private RoundedButton createButton(String text) {
        RoundedButton button = new RoundedButton(text);
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

    private JTextField createTextField(int value) {
        JTextField textField = new JTextField(6);
        textField.setText(String.valueOf(value));
        return textField;
    }

    private JLabel createLabel(String text, int fontSize, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Poppins", Font.BOLD, fontSize));
        label.setForeground(color);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private String getResolutionDisplayText() {
        int width = SettingsManager.getWindowWidth();
        int height = SettingsManager.getWindowHeight();
        return String.format("Current Resolution: %dx%d", width, height);
    }

    private void updateHeightFromWidth() {
        try {
            int width = Integer.parseInt(widthField.getText());
            int height = (int) (width / initialAspectRatio);
            heightField.setText(String.valueOf(height));
        } catch (NumberFormatException ex) {
            // Handle the exception or ignore
        }
    }

    private void updateWidthFromHeight() {
        try {
            int height = Integer.parseInt(heightField.getText());
            int width = (int) (height * initialAspectRatio);
            widthField.setText(String.valueOf(width));
        } catch (NumberFormatException ex) {
            // Handle the exception or ignore
        }
    }
}
