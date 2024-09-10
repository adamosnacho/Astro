package org.astro.core;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tutorial implements Serializable {
    public static GamePanel gp;
    private static List<String> dialogs = new ArrayList<>();

    public static void TextDialog(String dialog) {
        dialogs.addAll(List.of(dialog.split("\n")));
    }

    public static void draw(Graphics g) {
        if (dialogs.isEmpty()) {
            return;
        }
        gp.player.inMenu = true;
        g.setColor(Color.BLACK);
        g.fillRect(150, gp.getHeight() - 150, gp.getWidth() - 300, 100);
        g.setColor(Color.WHITE);
        g.drawString(dialogs.getFirst(), 200, gp.getHeight() - 100);
    }

    public static void input() {
        if (!dialogs.isEmpty() && Input.btn1Click) {
            dialogs.removeFirst();
        }
    }
}
