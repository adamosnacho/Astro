package org.astro.core.breakabletiles;

import org.astro.core.*;
import org.astro.core.Input;
import org.astro.core.items.BuildingHammer;
import org.astro.core.itemsystem.Item;
import org.astro.core.itemsystem.Items;
import org.astro.core.particlesystem.Particle;
import org.astro.core.particlesystem.ParticleGroup;
import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;
import org.newdawn.slick.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.astro.core.PlayerInventory.*;

public class MatterPrinterTile extends BreakableTile implements Save {
    private final SpriteSheet spriteSheet;
    private Item buildingHammer;

    private boolean pickMenu = false;
    private final Image slotSprite;
    private String selectedTile = "";

    private static final Sound popSfx;

    static {
        try {
            popSfx = new Sound("sfx/pop.ogg");
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
    }

    public MatterPrinterTile(float x, float y) {
        super(x, y, Items.items.get("matter_printer"));
        try {
            spriteSheet = new SpriteSheet("art/png/matterPrinter.png", 16, 16);
            sprite = spriteSheet.getSprite(0, 0);

            slotSprite = new Image("art/png/gui/slot.png").getScaledCopy(guiScale);
            slotSprite.setFilter(Image.FILTER_NEAREST);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        Astro.astro.spawn(this);
        Saving.save.add(this);
    }

    public MatterPrinterTile(Object o) {
        super(((Data) o).x, ((Data) o).y, Items.items.get("matter_printer"));
        try {
            spriteSheet = new SpriteSheet("art/png/matterPrinter.png", 16, 16);
            sprite = spriteSheet.getSprite(0, 0);

            slotSprite = new Image("art/png/gui/slot.png").getScaledCopy(guiScale);
            slotSprite.setFilter(Image.FILTER_NEAREST);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        Astro.astro.spawn(this);
        Saving.save.add(this);
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(spriteSheet.getSprite(0, buildingHammer == null ? 0 : 1).getScaledCopy(width, height), x, y);
    }

    @Override
    public void gui(Graphics g) {
        if (pickMenu) renderPickMenu(g);
    }

    @Override
    public void update() {
        if (pickMenu) {
            Astro.astro.player.canMove = false;
            PlayerInventory.inputActive = false;
            if (!org.astro.core.Input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) return;
            int mx = Astro.app.getInput().getMouseX();
            int my = Astro.app.getInput().getMouseY();
            float y = paddingY;
            float x = paddingX;
            List<String> seenNames = new ArrayList<>();
            for (Terrain.Tile tile :  Terrain.tiles) {
                if (seenNames.contains(tile.name)) continue;
                seenNames.add(tile.name);
                if (mx >= x && mx <= x + slotSprite.getWidth() && my >= y && my <= y + slotSprite.getHeight()) {
                    selectedTile = tile.name;
                    Astro.astro.player.canMove = true;
                    PlayerInventory.inputActive = true;
                    pickMenu = false;
                }

                y += paddingY + slotSprite.getHeight();
                if (y > Astro.astro.camera.height - paddingY - slotSprite.getHeight()) {
                    y = paddingY;
                    x += paddingX + slotSprite.getWidth();
                }
            }
        }
    }

    public void renderPickMenu(Graphics g) {
        g.setColor(new Color(120, 120, 120, 220));
        g.fillRect(0, 0, Astro.astro.camera.width, Astro.astro.camera.height);
        g.setColor(Color.white);
        float y = paddingY;
        float x = paddingX;
        List<String> seenNames = new ArrayList<>();
        for (Terrain.Tile tile :  Terrain.tiles) {
            if (seenNames.contains(tile.name)) continue;
            seenNames.add(tile.name);
            Image slot = slotSprite.copy();
            slot.setAlpha(1f);
            g.drawImage(slot, x, y);
            g.drawImage(tile.sprite.getScaledCopy(16, 16).getScaledCopy(guiScale), x + 1 * guiScale, y + 1 * guiScale);

            y += paddingY + slotSprite.getHeight();

            if (y > Astro.astro.camera.height - paddingY - slotSprite.getHeight()) {
                y = paddingY;
                x += paddingX + slotSprite.getWidth();
            }
        }
    }

    @Override
    public void onInteract() {
        boolean heated = false;

        for (BreakableTile bt : tiles) {
            if (bt.x == x && bt.y == y + 100 && bt instanceof FireTile) {
                heated = true;
                break;
            }
        }

        if (pickMenu || !heated) return;
        if (PlayerInventory.hand == null) {
            if (buildingHammer != null) {
                PlayerInventory.hand = buildingHammer;
                PlayerInventory.hand.it.itemEvents.inHand(true, PlayerInventory.hand);
                buildingHammer = null;
            }
        } else if (Objects.equals(PlayerInventory.hand.it.name, "building_hammer")) {
            buildingHammer = PlayerInventory.hand;
            PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
            PlayerInventory.hand = null;
            pickMenu = true;
            BuildingHammer.Data data = (BuildingHammer.Data) buildingHammer.itemData;
            if (data.tileCount != 0) {
                for (int i = 0; i < data.tileCount; i++) {
                    new Item(Items.items.get("matter"), x + Utils.randomRange(0, 60), y + height);
                }
                data.tileCount = 0;
                data.tileName = "";
            }
            popSfx.play(0.7f + Utils.randomRange(0, 20) / 100f, 0.8f);
        } else if (Objects.equals(PlayerInventory.hand.it.name, "matter")) {
            if (!Objects.equals(selectedTile, "") && buildingHammer != null) {
                PlayerInventory.hand.it.itemEvents.inHand(false, PlayerInventory.hand);
                PlayerInventory.hand = null;
                ((BuildingHammer.Data) buildingHammer.itemData).tileCount++;
                ((BuildingHammer.Data) buildingHammer.itemData).tileName = selectedTile;
                matterParticles(x + 30, y + 25);
                Astro.astro.camera.shake(3f, 0.5f);
                popSfx.play(0.7f + Utils.randomRange(0, 20) / 100f, 0.8f);
            }
        }
    }

    private void matterParticles(float px, float py) {
        try {
            Image matterSprite = new Image("art/png/matter.png");
            List<Color> colors = new ArrayList<>();
            for (int x = 0; x < matterSprite.getWidth() - 1; x++) {
                for (int y = 0; y < matterSprite.getHeight() - 1; y++) {
                    try {
                        colors.add(matterSprite.getColor(x, y));
                    } catch (ArrayIndexOutOfBoundsException e) {}
                }
            }
            int vel = 130;
            ParticleGroup pg = new ParticleGroup(px, py, true);
            pg.play(() -> {
                Particle p = new Particle(pg, 0, 0, Utils.randomRange(-vel, vel), Utils.randomRange(-vel, vel), colors.get(Utils.randomRange(0, colors.size() - 1)), 0.001f, 1);
                p.physicsProperties(0.3f, 0, 0);
                p.setSize(10, 10);
                return p;
            }, 50);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object save() {
        return new Data(x, y);
    }

    @Override
    public void aboutToSave() {
        if (buildingHammer != null) {
            buildingHammer.drop(x, y + 100);
        }
    }

    @Override
    public String getInfo() {
        boolean heated = false;

        for (BreakableTile bt : tiles) {
            if (bt.x == x && bt.y == y + 100 && bt instanceof FireTile) {
                heated = true;
                break;
            }
        }

        if (!heated) return "Not heated! Place fire under matter printer.";
        return "";
    }

    private record Data(float x, float y) implements Serializable {}
}
