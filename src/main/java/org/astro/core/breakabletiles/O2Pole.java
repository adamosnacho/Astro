package org.astro.core.breakabletiles;

import org.astro.core.Astro;
import org.astro.core.BreakableTile;
import org.astro.core.itemsystem.Items;
import org.astro.core.oxygensupplysystem.OxygenPipeConnector;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class O2Pole extends BreakableTile {
    private static O2Pole chosen = null;

    public final OxygenPipeConnector o2pipe;

    public O2Pole(float x, float y) {
        super(x, y, Items.items.get("o2_pole"));
        try {
            sprite = new Image("art/png/pole.png").getScaledCopy(width, height);
            sprite.setFilter(Image.FILTER_NEAREST);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }

        o2pipe = new OxygenPipeConnector(this.x + width / 2f, this.y + 5);

        Astro.astro.spawn(this);
    }

    @Override
    public void render(Graphics g) {
        for (OxygenPipeConnector connector : o2pipe.connectedConnectors) {
            o2pipe.renderPipe(g, connector);
        }
        g.drawImage(sprite, x, y);
    }

    @Override
    public void onInteract() {
        if (chosen == null) chosen = this;
        else if (chosen == this) chosen = null;
        else if (!chosen.o2pipe.connectedConnectors.contains(o2pipe)) {
            chosen.o2pipe.connect(o2pipe);
            o2pipe.connect(chosen.o2pipe);
            chosen = null;
        } else {
            chosen.o2pipe.disconnect(o2pipe);
            o2pipe.disconnect(chosen.o2pipe);
            chosen = null;
        }
    }
}
