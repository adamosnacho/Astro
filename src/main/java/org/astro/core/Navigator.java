package org.astro.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Navigator implements KeyListener {
    public transient GamePanel gp;
    public transient Map<String, Vector2> points = new HashMap<>();
    public int cursorBlinkSpeed = 500;
    public boolean cursorShown = false;

    private boolean bPressed = false;
    private String pointNameInput = "";
    private boolean inputtingName = false;

    public Navigator(GamePanel gp) {
        this.gp = gp;
        HudComponent hc = g -> {
            if (!inputtingName) return;
            g.setColor(Color.BLACK);
            g.fillRect(200, 200, gp.getWidth() - 400, 200);
            g.setColor(Color.WHITE);
            g.drawString("New point:", 250, 250);
            g.drawString(pointNameInput + (cursorShown ? "|" : ""), 250, 350);
        };
        gp.hud.hudComponents.add(hc);
        gp.addKeyListener(this);
        new Timer(cursorBlinkSpeed, e -> cursorShown = !cursorShown).start();
    }

    public void update() {
        if (inputtingName) {
            gp.player.inMenu = true;

            if (Input.keys.contains(KeyEvent.VK_ENTER)) {
                if (points.containsKey(pointNameInput)) points.remove(pointNameInput);
                else points.put(pointNameInput, new Vector2(gp.player.x + gp.player.width / 2, gp.player.y + gp.player.height / 2));
                pointNameInput = "";
                inputtingName = false;
            }
        }

        if (Input.keys.contains(KeyEvent.VK_B) && !Input.keys.contains(KeyEvent.VK_F1)) {
            if (!bPressed) {
                inputtingName = true;
                gp.player.inMenu = true;
            }
            bPressed = true;
        } else {
            bPressed = false;
        }
        if (Input.keys.contains(KeyEvent.VK_ESCAPE)) {
            inputtingName = false;
            pointNameInput = "";
        }
    }

    public void draw(Graphics g) {
        if (gp.player.inMenu) return;
        // Drawing point names when 'M' is pressed
        if (Input.keys.contains(KeyEvent.VK_M)) {
            int screenWidth = gp.getWidth();
            int screenHeight = gp.getHeight();
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;

            FontMetrics fm = g.getFontMetrics();

            for (Map.Entry<String, Vector2> point : points.entrySet()) {
                Vector2 pointPosition = point.getValue();

                // Calculate the vector from the player to the point
                float dx = (float) (pointPosition.x - (gp.player.x + gp.player.width / 2));
                float dy = (float) (pointPosition.y - (gp.player.y + gp.player.height / 2));

                // Normalize the direction vector
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < 500) {
                    g.setColor(Color.WHITE);
                    g.drawString(point.getKey(), (int) pointPosition.x - gp.cameraX, (int) pointPosition.y - gp.cameraY);
                    continue;
                }

                float nx = (float) (dx / distance);
                float ny = (float) (dy / distance);

                // Find where the direction intersects the screen edges
                float slope = ny / nx;

                int edgeX, edgeY;
                final int padding = 10; // Fixed padding from the edge

                if (Math.abs(slope) > (float) screenHeight / screenWidth) {
                    // Intersection with the top or bottom
                    if (ny > 0) {
                        edgeY = screenHeight - padding; // 10 pixels padding from edge
                        edgeX = (int) (centerX + (screenHeight - padding - centerY) / slope);
                    } else {
                        edgeY = padding; // 10 pixels padding from edge
                        edgeX = (int) (centerX - (centerY - padding) / slope);
                    }
                } else {
                    // Intersection with the left or right
                    if (nx > 0) {
                        edgeX = screenWidth - padding; // 10 pixels padding from edge
                        edgeY = (int) (centerY + slope * (screenWidth - padding - centerX));
                    } else {
                        edgeX = padding; // 10 pixels padding from edge
                        edgeY = (int) (centerY - slope * (centerX - padding));
                    }
                }

                // Adjust text position to ensure it's fully visible
                String pointName = point.getKey() + " " + (int) (distance / 100) + "m";
                int textWidth = fm.stringWidth(pointName);
                int textHeight = fm.getHeight();

                int textX = edgeX;
                int textY = edgeY;

                // Adjust the text position to fit within the screen
                if (textX + textWidth > screenWidth - padding) {
                    textX = screenWidth - textWidth - padding;
                } else if (textX < padding) {
                    textX = padding;
                }

                if (textY + textHeight > screenHeight - padding) {
                    textY = screenHeight - textHeight - padding;
                } else if (textY < padding) {
                    textY = padding;
                }
                textY += 20;
                textX -= 5;
                // Draw the point name
                g.setColor(Color.WHITE);
                g.drawString(pointName, textX, textY);
            }
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {
        if (inputtingName) {
            char keyChar = e.getKeyChar();

            // Handle backspace
            if (keyChar == '\b') {
                if (!pointNameInput.isEmpty()) {
                    pointNameInput = pointNameInput.substring(0, pointNameInput.length() - 1);
                }
            } else {
                pointNameInput += keyChar;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // No actions needed here for now
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // No actions needed here for now
    }
}
