package org.astro.core;

import org.astro.core.breakabletiles.CraftingPotTile;
import org.astro.core.breakabletiles.DrillTile;
import org.astro.core.breakabletiles.WoodWorkingTableTile;
import org.astro.core.itemsystem.Items;
import org.astro.core.lighting.LightManager;
import org.astro.core.oxygensupplysystem.OxygenPipeConnector;
import org.astro.core.saving.Saving;
import org.newdawn.slick.*;
import org.newdawn.slick.font.effects.ColorEffect;

import java.util.*;

public class Astro extends BasicGame {
    public static Font font;
    public static Font fontBig;
    public static boolean load = true;
    public static Astro astro;
    public static int delta;
    public List<Entity> entities = new ArrayList<>();
    public static AppGameContainer app;
    // Game Objects
    public Player player;
    public Camera camera;

    // queues
    private final List<Entity> deSpawnQueue = new ArrayList<>();
    private final List<Entity> spawnQueue = new ArrayList<>();

    public Astro() {
        super("Astro");
        astro = this;
    }

    private boolean firstRender = true;

    public static void main(String[] arguments) {
        try {
            app = new AppGameContainer(new Astro());
            app.setDisplayMode(1920, 1080, false);
            app.setVSync(false);
            app.setIcon("art/png/tiles/mars.png");
            app.start();
        }
        catch (SlickException e)
        {
            e.printStackTrace();
        }
    }

    public UnicodeFont createUnicodeFont(String fontName, int fontStyle, int fontSize) throws SlickException {
        UnicodeFont uFont = new UnicodeFont(new java.awt.Font(fontName, fontStyle, fontSize));
        uFont.getEffects().add(new ColorEffect(java.awt.Color.white)); // Set font color
        uFont.addAsciiGlyphs();  // Load ASCII glyphs
        uFont.loadGlyphs();      // Load the glyphs for rendering
        return uFont;
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        font = createUnicodeFont("Arial", java.awt.Font.BOLD, 16);
        fontBig = createUnicodeFont("Arial", java.awt.Font.BOLD, 32);
        app.setDefaultFont(font);

        Terrain.registerTiles();
        Terrain.init();

        Items.baseItems();
        Console.init();
        DrillTile.baseDigTiles();
        CraftingPotTile.baseRecipes();
        CraftingRecipesMenu.init();
        WoodWorkingTableTile.baseDropTable();

        Map<String, Object> instants = new HashMap<>();
        if (load) instants = Saving.load();

        if (!load || instants.get("org.astro.core.Player-0") == null) {
            player = new Player();
            player.x = 100000;
            player.y = 100000;

            while (player.isColliding(player.x, player.y, player.width, player.height)) {
                player.x += 100;
            }
        }
        else player = (Player) instants.get("org.astro.core.Player-0");
        camera = new Camera(1920, 1080, player);

        Settings.init();
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException
    {
        Input.update();
        Astro.delta = delta;
        TimeManager.update();

        for (Entity e : entities) {
            if (Math.sqrt((e.x - player.x) * (e.x - player.x) + (e.y - player.y) * (e.y - player.y)) > 1500) continue;
            int px = (int) (app.getInput().getMouseX() + camera.x);
            int py = (int) (app.getInput().getMouseY() + camera.y);
            if (px >= e.x && px <= (e.x + e.width) &&
                    py >= e.y && py <= (e.y + e.height) &&
                    Input.isMousePressed(1)) {
                e.onInteract();
            }

            e.update();
        }
        entities.addAll(spawnQueue);
        entities.removeAll(deSpawnQueue);
        if (!deSpawnQueue.isEmpty() || !spawnQueue.isEmpty()) sortEntityZ();
        spawnQueue.clear();
        deSpawnQueue.clear();

        Settings.update();
        CraftingRecipesMenu.update();
        camera.update();
        Music.update();
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException
    {
        if (firstRender) {
            g.setColor(Color.white);
            g.fillRect(0, 0, camera.width, camera.height);
            g.setColor(Color.black);
            g.setFont(fontBig);
            g.drawString("Loading...", camera.width / 2f - fontBig.getWidth("Loading...") / 2f, camera.height / 2f - fontBig.getHeight("Loading...") / 2f);
            firstRender = false;
            return;
        }
        g.setFont(font);
        g.setBackground(new Color(255, 112, 0));
        camera.apply(g);

        Terrain.draw(g);

        Entity infoEntity = null;

        for (Entity e : entities) {
            if (Math.sqrt((e.x - player.x) * (e.x - player.x) + (e.y - player.y) * (e.y - player.y)) > 1500) continue;
            e.render(g);
            int px = (int) (app.getInput().getMouseX() + camera.x);
            int py = (int) (app.getInput().getMouseY() + camera.y);
            if (px >= e.x && px <= (e.x + e.width) &&
                    py >= e.y && py <= (e.y + e.height) &&
                    (app.getInput().isMouseButtonDown(Input.MOUSE_MIDDLE_BUTTON) || app.getInput().isKeyDown(Input.KEY_E))) {
                if (!Objects.equals(e.getInfo(), "")) infoEntity = e;
            }
        }

        LightManager.renderLights();

        camera.reset(g);

        player.gui(g);

        for (Entity e : entities) {
            if (e != player) e.gui(g);
        }

        CraftingRecipesMenu.render(g);

        if (infoEntity != null) {
            g.setColor(Color.white);
            int x = app.getInput().getMouseX();
            int y = app.getInput().getMouseY();
            g.drawString(infoEntity.getInfo(), x, y - g.getFont().getHeight(infoEntity.getInfo()));
        }

        Settings.renderGui(g);

        OxygenPipeConnector.renderedPipeConnections.clear();
    }

    public void spawn(Entity e) {
        spawnQueue.add(e);
        e.spawn();
    }

    public void deSpawn(Entity e) {
        deSpawnQueue.add(e);
        e.deSpawn();
    }

    public void sortEntityZ() {
        entities.sort(Comparator.comparingInt(e -> e.z));
    }

    @Override
    public boolean closeRequested() {
        for (Chunk c : Terrain.loadedChunks.values()) {
            c.unload();
        }

        Saving.save();
        return true;
    }
}
