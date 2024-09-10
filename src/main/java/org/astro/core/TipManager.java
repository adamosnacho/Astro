package org.astro.core;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TipManager implements Serializable {
    public GamePanel gp;
    public Set<Entity> tipEntities = new HashSet<>();

    private float fade = 0.0f; // Current fade level
    private final float fadeSpeed = 10.0f; // Speed of fade-in effect
    private final int fontSize = 14; // Smaller font size for the text
    private final int padding = 10; // Padding around the text



    public TipManager(GamePanel gp) {
        this.gp = gp;
    }

    public void update(double deltaTime) {
        // Handle fade effect
        if (Input.btn2) { // Middle mouse button held
            fade = Math.min(fade + fadeSpeed * (float) deltaTime, 1.0f); // Fade in
        } else {
            fade = Math.max(fade - fadeSpeed * (float) deltaTime, 0.0f); // Fade out
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Font originalFont = g2d.getFont(); // Save the original font
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSize)); // Set smaller font size

        for (Entity entity : tipEntities) {
            TipEntity te = (TipEntity) entity;
            int clickX = gp.cameraX + Input.mouse.getX();
            int clickY = gp.cameraY + Input.mouse.getY();

            if (clickX >= entity.x && clickX <= entity.x + te.getSize().x && clickY >= entity.y && clickY <= entity.y + te.getSize().y) {
                // Apply fade effect to the color
                Color backgroundColor = new Color(0, 0, 0, (int) (fade * 150)); // Semi-transparent black
                Color textColor = new Color(255, 255, 255, (int) (fade * 255)); // White with fade

                // Get lines of text
                String[] lines = te.getDisplay().split("\n");
                FontMetrics metrics = g2d.getFontMetrics();
                int textWidth = 0;
                int textHeight = metrics.getHeight();

                // Measure the width of the longest line
                for (String line : lines) {
                    int lineWidth = metrics.stringWidth(line);
                    textWidth = Math.max(textWidth, lineWidth);
                }

                // Calculate the height needed for all lines
                int totalHeight = lines.length * textHeight + (lines.length - 1) * 2; // Adding padding between lines

                // Calculate tooltip position
                int tooltipX = Input.mouse.getX() + 5;
                int tooltipY = Input.mouse.getY() - totalHeight - padding - textHeight; // Adjusting position to make space for title
                if (tooltipY < fontSize) {
                    tooltipY = Input.mouse.getY() + padding + textHeight; // If the tooltip goes above the screen, place it below
                }

                // Draw title above the tooltip background
                g2d.setFont(new Font("Arial", Font.BOLD, fontSize)); // Bold font for the title
                g2d.setColor(textColor);
                g2d.drawString(te.getName(), Input.mouse.getX() + 5, tooltipY - 5); // Drawing title above the tooltip background

                // Draw rounded rectangle for the background
                g2d.setColor(backgroundColor);
                g2d.fillRoundRect(tooltipX, tooltipY, textWidth + 2 * padding, totalHeight + 3 * padding, 10, 10);

                // Draw text within the tooltip
                g2d.setColor(textColor);
                g2d.setFont(new Font("Arial", Font.PLAIN, fontSize)); // Set back to plain font for the text
                int yOffset = tooltipY + padding + textHeight; // Adjust starting y offset for text
                for (String line : lines) {
                    g2d.drawString(line, tooltipX + padding, yOffset);
                    yOffset += textHeight + 2; // Move to the next line with padding
                }
                break;
            }
        }

        g2d.setFont(originalFont);
    }

}
