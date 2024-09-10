package org.astro.menu;

import javax.swing.*;
import java.awt.*;

public class Credits extends JPanel {
    private JFrame frame;

    public Credits(JFrame frame) {
        this.frame = frame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(34, 40, 49)); // Dark background color

        JLabel title = createLabel("Credits", 72, new Color(255, 255, 255)); // White color
        JLabel cred = createLabel("Developer - Adam Ryan", 32, new Color(255, 255, 255));
        JLabel cred2 = createLabel("Artist - Adam Ryan, Aleks Kowalewski, Konrad Jablonowski", 32, new Color(255, 255, 255));
        JLabel cred3 = createLabel("Publisher - Adam Ryan", 32, new Color(255, 255, 255));
        JLabel cred4 = createLabel("Idea - Wiktor Fuksa", 32, new Color(255, 255, 255));

        add(title);
        add(Box.createVerticalStrut(100));
        add(cred);
        add(cred2);
        add(cred3);
        add(cred4);

        JButton back = createButton("Back");
        back.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.getContentPane().add(new MainMenu(frame));
            frame.revalidate();
            frame.repaint();
        });
        add(Box.createVerticalStrut(50));
        add(back);
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
}
