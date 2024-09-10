package org.astro.core;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class PlayerOxygen implements Serializable {
    private GamePanel gp;
    public float oxygen = 100;
    public final int depletionRate = 3; // [ms] between loss of one oxygen
    public final int updateRate = 500; // [ms] between updates
    public Pole connectedPole;
    public final int cordLength = 500; // [px]
    public PlayerOxygen(GamePanel gp) {
        this.gp = gp;
        new Timer(updateRate, e -> check()).start();
    }
    private void check() {
        connectedPole = null;
        int best = cordLength;
        for (Pole p : PoleManager.getInstance().poles) {
            if (PoleManager.getInstance().distance(p, new Pole(gp, gp.player.x, gp.player.y)) < best && p.isConnected) {
                best = (int)PoleManager.getInstance().distance(p, new Pole(gp, gp.player.x, gp.player.y));
                connectedPole = p;
            }
        }


        if (connectedPole != null) return;
        for (Pole p : PoleManager.getInstance().poles) {
            if (PoleManager.getInstance().distance(p, new Pole(gp, gp.player.x, gp.player.y)) < cordLength) connectedPole = p;
        }
    }
    public void update(double deltaTime) {
        if (connectedPole != null && connectedPole.isConnected) oxygen += depletionRate * deltaTime;
        else oxygen -= depletionRate * deltaTime;
        if (oxygen < 0) oxygen = 0;
        if (oxygen > 100) oxygen = 100;
        if (oxygen == 0) gp.player.hp -= 100 * deltaTime;
    }
    public void draw(Graphics g) {
        if (connectedPole == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.WHITE);
        if (connectedPole.isConnected) g2.setColor(Color.CYAN);
        g2.setStroke(new BasicStroke(5));
        if (gp.player.facingRight) g2.drawLine(gp.player.x + 3, gp.player.y + 60, connectedPole.x + connectedPole.width / 2, connectedPole.y);
        else g2.drawLine(gp.player.x + gp.player.width - 3, gp.player.y + 60, connectedPole.x + connectedPole.width / 2, connectedPole.y);
    }
}
