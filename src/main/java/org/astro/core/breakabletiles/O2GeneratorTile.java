package org.astro.core.breakabletiles;

import org.astro.core.Astro;
import org.astro.core.BreakableTile;
import org.astro.core.ClassSettings;
import org.astro.core.Player;
import org.astro.core.itemsystem.Items;
import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;

public class O2GeneratorTile extends BreakableTile implements Save {
    public boolean infinite = false;
    public float wear = 100;
    public static float wearSpeed = ClassSettings.loadFloat("o2 generator tile/wear speed", 0.0002f);
    public static final float pipeLen = ClassSettings.loadFloat("o2 generator tile/pipe len", 2000);

    public static final int fanAnimationSpeed = ClassSettings.loadInt("o2 generator tile/fan animation speed", 100);
    private int fanTime = 0;
    private boolean fanAnimationFrame = false;

    private final SpriteSheet spriteSheet;

    private final Image pipe;

    public O2GeneratorTile(float x, float y) {
        super(x, y, Items.items.get("o2_generator"));
        Astro.astro.spawn(this);
        Saving.save.add(this);
        try {
            sprite = new Image("art/png/o2GeneratorIcon.png").getScaledCopy(width, height);
            sprite.setFilter(Image.FILTER_NEAREST);

            spriteSheet = new SpriteSheet("art/png/o2Generator.png", 16, 16);
            spriteSheet.setFilter(Image.FILTER_NEAREST);

            pipe = new Image("art/png/pipe.png").getScaledCopy(20, 10);
            pipe.setFilter(Image.FILTER_NEAREST);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
    }

    public O2GeneratorTile(Object o) {
        super(((float[]) o)[0], ((float[]) o)[1], Items.items.get("o2_generator"));
        Astro.astro.spawn(this);
        Saving.save.add(this);
        try {
            sprite = new Image("art/png/o2GeneratorIcon.png").getScaledCopy(width, height);
            sprite.setFilter(Image.FILTER_NEAREST);

            spriteSheet = new SpriteSheet("art/png/o2Generator.png", 16, 16);
            spriteSheet.setFilter(Image.FILTER_NEAREST);

            pipe = new Image("art/png/pipe.png").getScaledCopy(20, 10);
            pipe.setFilter(Image.FILTER_NEAREST);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        wear = ((float[]) o)[2];
        infinite = ((float[]) o)[3] == 1;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(spriteSheet.getSprite(0, fanAnimationFrame ? 0 : 1).getScaledCopy(width, height), x, y);
    }


    @Override
    public void update() {
        fanTime += Astro.delta;

        if (fanTime >= fanAnimationSpeed) {
            fanTime = 0;
            fanAnimationFrame = !fanAnimationFrame;
        }

        if (!infinite) wear -= wearSpeed * Astro.delta;
    }

    @Override
    public Object onTileBreak() {
        return wear;
    }

    @Override
    public String getInfo() {
        return "Wear " + Math.round(wear) + "%";
    }

    @Override
    public Object save() {
        return new float[] {x, y, wear, infinite ? 1 : 0};
    }
}
