package org.astro.core.menusystem;

import org.astro.core.Astro;
import org.astro.core.Input;
import org.newdawn.slick.*;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    public final int width;
    public final int height;
    private final Color bg;
    public final List<UIElement> ui = new ArrayList<>();
    public float paddingY = 10;
    public float radius = 8;
    public final Image i;
    public float posX;
    public float posY;

    public Menu(int width, int height, Color bg, float posX, float posY) throws SlickException {
        i = new Image(width, height);
        i.setFilter(Image.FILTER_NEAREST);
        this.width = width;
        this.height = height;
        this.bg = bg;
        this.posX = posX;
        this.posY = posY;
        render();
    }

    public void update() {
        float stackY = paddingY;
        for (UIElement uie : ui) {
            uie.update((float) width / 2 - (float) uie.getWidth() / 2, stackY, posX, posY, this);
            stackY += paddingY + uie.getHeight();
        }
    }

    public void display(Graphics g) {
        render();
        g.drawImage(i, posX, posY);
    }

    private void render() {
        try {
            Graphics g = i.getGraphics();
            g.setColor(bg);
            g.fillRoundRect(0, 0, width, height, (int) radius);

            float stackY = paddingY;
            for (UIElement uie : ui) {
                uie.render((float) width / 2 - (float) uie.getWidth() / 2, stackY, g);
                stackY += paddingY + uie.getHeight();
            }
            g.flush();
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
    }

    public void addLabel(Label l) {
        ui.add(l);
    }

    public void addButton(Button b) {
        ui.add(b);
    }

    public static class Button extends UIElement {
        public Color fontColor;
        public Font font;
        public Color bg;
        public Color bgHover;
        public String text;
        public int width;
        public int height;

        private boolean hover = false;
        private final OnPress onPress;

        public Button(String text, Color bg, Color bgHover, int width, int height, OnPress onPress) {
            fontColor = Color.white;
            this.bg = bg;
            this.bgHover = bgHover;
            this.text = text;
            this.width = width;
            this.height = height;
            this.font = Astro.font;
            this.onPress = onPress;
        }

        public Button(String text, Color bg, Color bgHover, int width, int height, Font font, OnPress onPress) {
            fontColor = Color.white;
            this.bg = bg;
            this.bgHover = bgHover;
            this.text = text;
            this.width = width;
            this.height = height;
            this.font = font;
            this.onPress = onPress;
        }

        @Override
        public void render(float x, float y, Graphics g) {
            g.setFont(font);
            g.setColor(hover ? bgHover : bg);
            g.fillRect(x, y, getWidth(), getHeight());
            g.setColor(fontColor);
            g.drawString(text, x + (float) getWidth() / 2 - (float) font.getWidth(text) / 2, y + (float) getHeight() / 2 - (float) font.getHeight(text) / 2);
        }

        @Override
        public void update(float x, float y, float posX, float posY, Menu menu) {
            int px = (int) (Astro.app.getInput().getMouseX() - posX);
            int py = (int) (Astro.app.getInput().getMouseY() - posY);
            hover = px >= x && px <= (x + width) && py >= y && py <= (y + height);
            if (hover && onPress != null && org.astro.core.Input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) onPress.onPress(this);
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public int getWidth() {
            return width;
        }

        public interface OnPress {
            void onPress(Button b);
        }
    }

    public static class Label extends UIElement {
        public Color fontColor;
        public Font font;
        public String text;

        public Label(String text) {
            fontColor = Color.white;
            this.text = text;
            this.font = Astro.fontBig;
        }

        public Label(String text, Font font) {
            fontColor = Color.white;
            this.text = text;
            this.font = font;
        }

        @Override
        public void render(float x, float y, Graphics g) {
            g.setFont(font);
            g.setColor(fontColor);
            g.drawString(text, x, y);
        }

        @Override
        public int getHeight() {
            return font.getHeight(text);
        }

        @Override
        public int getWidth() {
            return font.getWidth(text);
        }
    }

    public static class UIElement {
        public void render(float x, float y, Graphics g) {}
        public void update(float x, float y,  float posX, float posY, Menu menu) {}
        public int getHeight() {return 0;}
        public int getWidth() {return 0;}
    }
}
