package org.astro.core;

import org.astro.Main;
import org.astro.menu.SettingsManager;
import org.astro.modding.ModLoader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class Game extends JFrame implements WindowListener {
    private ModLoader modLoader;
    private GamePanel panel;

    public Game() {
        try {
            setIconImage(ImageIO.read(getClass().getResourceAsStream("/art/png/blueprint.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        modLoader = new ModLoader();
        modLoader.loadMods("mods");

        try {
            panel =new GamePanel(modLoader, this);
        } catch (Exception e) {
            panel.crashToMainMenu(e);
        }

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);

        addWindowListener(this);
        setTitle("Astro");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent window close on X button
        setSize(SettingsManager.getWindowWidth(), SettingsManager.getWindowHeight());
        setLocationRelativeTo(null); // Center the frame on the screen
        setVisible(true);
        setResizable(false);

        // Key binding to ignore Escape key
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ignore");
        getRootPane().getActionMap().put("ignore", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // No action needed
    }

    @Override
    public void windowClosing(WindowEvent e) {
        dispose();
        panel.exitToMainMenu();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        // No action needed
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // No action needed
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // No action needed
    }

    @Override
    public void windowActivated(WindowEvent e) {
        // No action needed
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // No action needed
    }
}
