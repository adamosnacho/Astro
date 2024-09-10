package org.astro;

import org.astro.menu.*;
import javax.swing.*;
import java.awt.*;

public class Main {
    public JFrame frame;
    public Main() {
        init();
        MainMenu mainMenu = new MainMenu(frame);
        frame.add(mainMenu);
        frame.pack();
        frame.setLocationRelativeTo(null); // Center the frame
        frame.setVisible(true);
    }
    public Main(String crash) {
        init();
        MainMenu mainMenu = new MainMenu(frame, crash);
        frame.add(mainMenu);
        frame.pack();
        frame.setLocationRelativeTo(null); // Center the frame
        frame.setVisible(true);
    }
    public void init() {
        frame = new JFrame("Astro");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setPreferredSize(new Dimension(1500, 850));
    }
    public static void main(String[] args) {
        new Main();
    }
}