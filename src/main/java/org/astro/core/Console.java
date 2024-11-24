package org.astro.core;

import org.astro.core.breakabletiles.FireTile;
import org.astro.core.enemies.Alien;
import org.astro.core.enemies.Shrimp;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.Items;
import org.astro.core.lighting.LightManager;
import org.astro.core.saving.Saving;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.KeyListener;

public class Console {
    public static boolean active = false;
    private static String command = "";
    private static final int x = 100;
    private static final int y = 100;

    public static void init() {
        Astro.app.getInput().addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(int i, char c) {
                if (active) {
                    // Check if the pressed key corresponds to a typable character
                    if (Character.isLetterOrDigit(c) || Character.isLetter(c) || c == ' ' || c == '_' || c == '.') {
                        command += c;
                    }

                    // Handle backspace
                    if (i == Input.KEY_BACK && command.length() > 0) {
                        command = command.substring(0, command.length() - 1);
                    }
                }
            }


            @Override
            public void keyReleased(int i, char c) {

            }

            @Override
            public void setInput(org.newdawn.slick.Input input) {

            }

            @Override
            public boolean isAcceptingInput() {
                return true;
            }

            @Override
            public void inputEnded() {

            }

            @Override
            public void inputStarted() {

            }
        });
    }

    public static void drawConsole(Graphics g) {
        if (!active) return;
        g.setFont(Astro.fontBig);
        g.setColor(new Color(100, 100, 100, 150));
        g.fillRect(0, 0, Astro.astro.camera.width, Astro.astro.camera.height);
        g.setColor(new Color(255, 255, 255));
        g.drawString("Command > " + command, x, Astro.astro.camera.height - y);
        g.setFont(Astro.font);
    }

    public static void console() {
        if (Astro.app.getInput().isKeyPressed(Input.KEY_C) && !active) {
            active = true;
        } else if (Astro.app.getInput().isKeyPressed(Input.KEY_ESCAPE) && active) {
            active = false;
            Astro.astro.player.canMove = true;
        }

        if (active) {
            Astro.astro.player.canMove = false;
            if (Astro.app.getInput().isKeyPressed(Input.KEY_ENTER)) {
                runCommand(command);
                command = "";
                active = false;
                Astro.astro.player.canMove = true;
            }
        }
    }


    public static void runCommand(String c) {
        String[] t = c.split(" ");

        if (t[0].contains("item")) {
            if (t.length > 2) {
                for (int i = 0; i < Integer.parseInt(t[2]); i++) {
                    new Item(Items.items.get(t[1].replace(" ", "")), Astro.astro.player.x + i * 40, Astro.astro.player.y);
                }
            }
            else new Item(Items.items.get(t[1].replace(" ", "")), Astro.astro.player.x, Astro.astro.player.y);
        }
        if (t[0].contains("save")) Saving.save();
        if (t[0].contains("exit")) {
            if (t.length > 1 && t[1].contains("nosave")) Astro.app.exit();
            else {
                Saving.save();
                Astro.app.exit();
            }
        }
        if (t[0].contains("test1")) new Alien(Astro.astro.player.x + 200, Astro.astro.player.y);
        if (t[0].contains("test2")) new Shrimp(Astro.astro.player.x + 200, Astro.astro.player.y);
        if (t[0].contains("test3")) new FireTile(Astro.astro.player.x + 200, Astro.astro.player.y);
        if (t[0].contains("heal")) Astro.astro.player.suitWear = 200;
        if (t[0].contains("tp")) {
            Astro.astro.player.x = Float.parseFloat(t[1]);
            Astro.astro.player.y = Float.parseFloat(t[2]);
        }
        if (t[0].contains("time")) TimeManager.time = Float.parseFloat(t[1]);
        if (t[0].contains("track")) {
            Music.track = Music.tracks.get(Integer.parseInt(t[1]) - 1).left;
            Music.track.play();
            Music.track.setVolume(0);
            Music.track.fade(ClassSettings.loadInt("music/fade duration", 1500), Music.tracks.get(Integer.parseInt(t[1]) - 1).right, false);
        }
    }
}
