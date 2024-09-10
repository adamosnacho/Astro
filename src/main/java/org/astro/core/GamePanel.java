package org.astro.core;

import org.astro.core.particles.ParticleGroup;
import org.astro.core.particles.ParticleSystem;
import org.astro.core.saving.SaveManager;
import org.astro.modding.*;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class GamePanel extends JPanel implements KeyListener, MouseInputListener, ActionListener, Serializable {
    public int cameraX, cameraY;
    public Player player;
    public Terrain terrain;
    public Lander lander;
    public TipManager tipManager;
    public Navigator navigator;
    public Lighting lighting;
    public Hud hud;
    public final float cameraSmooth = 0.1f;
    final int levelSize = 1000;
    public Set<Entity>[] entities = new HashSet[100];
    private Set<Entity> delayedEntities = new HashSet<>();
    public Set<Entity> enemies = new HashSet();
    public Set<Entity>[] deSpawnQueue = new HashSet[100];
    public Set<Entity> breakableTileEntities = new HashSet<>();
    public ModLoader modLoader;
    public JFrame frame;

    private boolean doneTerrain = false;
    private long terrainStart = 0;
    private long terrainEnd = 0;
    private boolean doneOther = false;
    private long modsStart = 0;
    private long modsEnd = 0;
    public boolean loaded = false;
    private long lastTime = System.nanoTime();

    public double fps = 0;
    public float time = 500;  // Using float for smoother updates
    public float timeSpeed = 1f;  // Speed factor for time updates
    public Entity currentEntity;

    public GamePanel(ModLoader modLoader, JFrame frame) throws Exception {
        this.frame = frame;
        this.modLoader = modLoader;
        setBackground(new Color(255, 112, 0));
        setPreferredSize(new Dimension(1500, 850)); // Initial preferred size
        setFocusable(true); // Allow panel to receive focus
        requestFocusInWindow(); // Request focus when the panel is displayed
        addKeyListener(this); // Register KeyListener
        addMouseMotionListener(this);
        addMouseListener(this);

        // Initialize entities array
        for (int i = 0; i < entities.length; i++) {
            entities[i] = new HashSet<>();
        }
        for (int i = 0; i < deSpawnQueue.length; i++) {
            deSpawnQueue[i] = new HashSet<>();
        }

        // Start terrain generation in a separate thread to avoid blocking the UI
        new Thread(() -> {
            try {
                Tutorial.gp = this;
                PoleManager.gp = this;
                tipManager = new TipManager(this);
                repaint();
                System.out.println("Generating terrain...");
                terrainStart = System.currentTimeMillis();
                terrain = new Terrain(levelSize, levelSize, this);
                int spawnX = levelSize / 2;
                int spawnY = levelSize / 2;

                while (terrain.getTile(spawnY, spawnX) == 5 || terrain.getTile(spawnY + 1, spawnX) == 5) {
                    spawnX += 1;
                }
                cameraX = spawnX * terrain.tileSize;
                cameraY = spawnY * terrain.tileSize;
                lighting = new Lighting(this);
                player = new Player(spawnX * terrain.tileSize, spawnY * terrain.tileSize, this);
                lander = new Lander(this, spawnX * terrain.tileSize, spawnY * terrain.tileSize);
                spawnEntity(lander, 4);
                new ShrimpBossArena(this, terrain, lander);
                doneTerrain = true;
                terrainEnd = System.currentTimeMillis();
                repaint(); // Request repaint after terrain generation is done

                ConsoleInputHandler consoleInputHandler = new ConsoleInputHandler(lighting, this);
                consoleInputHandler.start();
                modsStart = System.currentTimeMillis();
                SpawnEntities se = new SpawnEntities(this, terrain);
                hud = new Hud(se, this);
                navigator = new Navigator(this);
                doneOther = true;
                modsEnd = System.currentTimeMillis();

                for (Mod mod : this.modLoader.loadedMods) {
                    mod.initialize(this);
                }

                Tutorial.TextDialog("This is a tutorial dialog.\nIt will help you understand the game.\nYou're standing beside your lander.\nIt crashed because it was hit by a stray asteroid.\nYou must repair it with 5 reactor cores to return to earth.\nYou will have to fight off aliens.\nTo obtain reactor cores you must\nlocate and defeat 5 bosses.\nTrade with vendors for maps that will lead you to the bosses.\nPressing and holding the mouse middle button over objects\nshows a description of them.\nThis is use full when you can't figure out what something does.\nThis also displays data about the item.\nGetting lost is bad. press and hold 'M' to view saved points.\nYou can add your own points by pressing 'B' and giving them a name.\nIn the left top corner you can see your backpack.\nPress 1 to switch the item you're holding with the firs item with the backpack.\n'1' and '2' do the same but with the second and third item in the backpack.\nDrop the item you are holding by pressing 'Q'\nPick up items of the ground by right clicking them\nLeft click whilst holding a placeable item to place it.\nCraft by holding shift and dropping the items you want to combine.\nLetting go shift crafts the item.\nWhile holding shift, on the right side of your screen you will\nsee a recipe book which will tell you which items to combine.");
                loaded = true;

                Timer timer = new Timer(0, this);
                timer.start();

            } catch (Exception e) {
                crashToMainMenu(e);
            }
        }).start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (loaded) {
            Tutorial.input();
            long now = System.nanoTime();
            double deltaTime = (now - lastTime) / 1_000_000_000.0; // Convert nanoseconds to seconds
            time += (float) (deltaTime * timeSpeed);
            // Update FPS and time
            fps = 1.0 / deltaTime;
            lastTime = now;
            if (Input.keys.contains(KeyEvent.VK_F1)) player.inMenu = true;
            // Update player
            player.update(Input.keys, terrain, deltaTime);



            ParticleSystem.update(deltaTime);

            // Calculate camera position
            int tCameraX = player.x - getWidth() / 2 + player.width / 2;
            int tCameraY = player.y - getHeight() / 2 + player.height / 2;
            cameraX = (int) lerp(cameraX, tCameraX, cameraSmooth);
            cameraY = (int) lerp(cameraY, tCameraY, cameraSmooth);

            // Define the extended viewport
            int extendedViewLeft = cameraX - 500;
            int extendedViewRight = cameraX + getWidth() + 500;
            int extendedViewTop = cameraY - 500;
            int extendedViewBottom = cameraY + getHeight() + 500;

            // Update all entities within the extended viewport
            for (Set<Entity> entitySet : entities) {
                for (Entity en : entitySet) {
                    // Check if entity is within the extended viewport
                    if (en.x > extendedViewLeft && en.x < extendedViewRight &&
                            en.y > extendedViewTop && en.y < extendedViewBottom) {
                        en.update(deltaTime);
                    }
                }
            }

            // De-spawn entities
            for (int i = 0; i < deSpawnQueue.length; i++) {
                for (Entity en : deSpawnQueue[i]) {
                    entities[i].remove(en);
                    en.deSpawn();
                }
            }
            for (Set<Entity> ens : deSpawnQueue) {
                ens.clear();
            }

            // Update mods
            for (Mod mod : this.modLoader.loadedMods) {
                mod.update();
            }

            // Update input and tips
            Input.update();
            tipManager.update(deltaTime);
            player.inMenu = false;
            navigator.update();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Poppins", Font.BOLD, 30));
        if (!loaded) {
            long _s;
            _s = System.currentTimeMillis();
            if (!doneTerrain) {
                if (terrainEnd != 0) _s = terrainEnd;
                g.drawString("Generating terrain... " + (_s - terrainStart) + "ms", 20, 100);
            }
            else if (!doneOther) {
                g.drawString("Done.", 20, 140);

                _s = System.currentTimeMillis();
                if (modsEnd != 0) _s = modsEnd;
                g.drawString("Loading... " + (_s - modsStart) + "ms", 20, 180);
            }
            else g.drawString("Done.", 20, 220);
            return;
        }

        // Calculate camera position
        int tCameraX = player.x - getWidth() / 2 + player.width / 2;
        int tCameraY = player.y - getHeight() / 2 + player.height / 2;
        cameraX = (int) lerp(cameraX, tCameraX, cameraSmooth);
        cameraY = (int) lerp(cameraY, tCameraY, cameraSmooth);
        g.translate(-cameraX, -cameraY);

        // Draw terrain
        terrain.draw(g, cameraX, cameraY, getWidth(), getHeight());

        // Draw entities only if they are within the visible area
        int extendedViewLeft = cameraX - 500;
        int extendedViewRight = cameraX + getWidth() + 500;
        int extendedViewTop = cameraY - 500;
        int extendedViewBottom = cameraY + getHeight() + 500;

        // Draw entities
        for (Set<Entity> entitySet : entities) {
            for (Entity en : entitySet) {
                // Check if entity is within the extended viewport
                if (en.x > extendedViewLeft && en.x < extendedViewRight &&
                        en.y > extendedViewTop && en.y < extendedViewBottom) {
                    currentEntity = en;
                    en.draw(g);
                    currentEntity = null;
                }
            }
        }

        // Draw player (since the player is likely always within view)
        player.draw(g);

        ParticleSystem.draw(g);

        // Draw mods (if any)
        for (Mod mod : this.modLoader.loadedMods) {
            mod.draw(g);
        }

        // Delayed Entities
        for (Entity e : delayedEntities) {
            e.delayedDraw = true;
            e.draw(g);
            e.delayedDraw = false;
        }
        delayedEntities.clear();

        lighting.draw(g);

        // Translate back to original position
        g.translate(cameraX, cameraY);

        // Draw HUD
        hud.draw(g);

        // Draw the navigator
        navigator.draw(g);

        Tutorial.draw(g);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        Input.keys.add(e.getKeyCode());
    }

    public void exitToMainMenu() {
        if (Input.keys.contains(KeyEvent.VK_ESCAPE)) return;
        removeKeyListener(this);
        removeMouseListener(this);
        frame.dispose();
        PoleManager.instance = null;
        System.exit(0);
    }

    public void crashToMainMenu(Exception e) {
        e.printStackTrace();
       exitToMainMenu();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Input.keys.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (Input.keys.contains(KeyEvent.VK_F1)) {
            if (e.getKeyChar() == '`') {
                hud.command = hud.command.substring(0, hud.command.length() - 1);
                return;
            }

            if (e.getKeyChar() == '\n') {
                String[] tokens = parseCommand(hud.command);
                System.out.print("executed command tokens: ");
                for (String token : tokens) {
                    System.out.print(token + " ");
                }
                System.out.println();
                try {
                    runCommand(tokens);
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                hud.command = "";
                return;
            }
            hud.command += e.getKeyChar();
        }
    }

    private String[] parseCommand(String command) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;

        for (char c : command.toCharArray()) {
            if (c == '\"') {
                if (inQuotes) {
                    // Closing quote
                    inQuotes = false;
                    tokens.add(currentToken.toString().trim()); // Trim and add token
                    currentToken.setLength(0); // Clear the StringBuilder
                } else {
                    // Opening quote
                    inQuotes = true;
                    // Don't add the token here, continue accumulating characters
                }
            } else if (c == ' ' && !inQuotes) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString().trim()); // Trim and add token
                    currentToken.setLength(0); // Clear the StringBuilder
                }
            } else {
                currentToken.append(c);
            }
        }

        // Add the last token if it exists
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString().trim()); // Trim and add token
        }

        return tokens.toArray(new String[0]);
    }


    public void runCommand(String[] tokens) throws IOException, ClassNotFoundException {
        if (tokens[0].contains("item")) {
            if (Items.getInstance(this).physicalItemsP.containsKey(tokens[1])) {
                System.out.println("giving " + tokens[1]);
                PhysicalItem drop = Items.getInstance(this).getPhysical(tokens[1]);
                if (tokens.length >= 4) drop.data = new ArrayList<>(Arrays.asList(tokens).subList(3, tokens.length));
                drop.x = player.x;
                drop.y = player.y;
                int count = 1;
                if (tokens.length >= 3) count = Integer.parseInt(tokens[2]);
                System.out.println("amount: " + count);

                for (int i = 0; i < count; i++) {
                    spawnEntity(drop.copy(), 10);
                    drop.x = player.x + i * 50;
                }
            }
        }
        if (tokens[0].contains("tp")) {
            if (tokens.length == 3) {
                player.x = Integer.parseInt(tokens[1]);
                player.y = Integer.parseInt(tokens[2]);
                System.out.println("teleported");
            }
            if (tokens.length == 2 && Registry.bosses.containsKey(tokens[1])) {
                player.x = (int) Registry.bosses.get(tokens[1]).x - 300;
                player.y = (int) Registry.bosses.get(tokens[1]).y;
                System.out.println("teleported");
            }
        }
        if (tokens[0].contains("time")) {
            if (tokens.length == 3) {
                if (tokens[1].contains("set")) {
                    time = Float.parseFloat(tokens[2]);
                    System.out.println("set time");
                }
                if (tokens[1].contains("speed")) {
                    timeSpeed = Float.parseFloat(tokens[2]);
                    System.out.println("modified time speed");
                }
            }
        }
        if (tokens[0].contains("printtime")) System.out.println("Time: " + time + "Time speed: " + timeSpeed);
        if (tokens[0].contains("entities")) System.out.println("entities: " + entities.length);
        if (tokens[0].contains("enemies")) System.out.println("enemies: " + enemies.size());
        if (tokens[0].contains("particles")) {
            int amount = 0;
            for (ParticleGroup pg : ParticleSystem.particleGroups) {
                amount += pg.particles.size();
            }
            System.out.println("particles: " + amount + "\nparticle groups: " + ParticleSystem.particleGroups.size());
        }
        if (tokens[0].contains("spawn")) {
            if (tokens[1].contains("alien")) spawnEntity(new Alien(this, player, player.x + 400, player.y), 10);
            if (tokens[1].contains("vendor")) spawnEntity(new Vendor(this, player, player.x + 400, player.y), 10);
            if (tokens[1].contains("shrimp boss")) spawnEntity(new ShrimpBoss(this, player, player.x + 400, player.y), 10);
        }
        if (tokens[0].contains("map") && tokens.length == 2) {
            if (!Registry.bosses.containsKey(tokens[1])) return;
            PhysicalItem m = Items.getInstance(this).getPhysical("map");
            m.data.add(tokens[1]);
            m.data.add(String.valueOf(Registry.bosses.get(tokens[1]).x));
            m.data.add(String.valueOf(Registry.bosses.get(tokens[1]).y));
            player.pi.drop(m);
        }
        if (tokens[0].contains("save")) SaveManager.save();
        if (tokens[0].contains("load")) SaveManager.load();
    }

    public float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Input.mouse = e;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Input.mouse = e;
        Input.MousePressed();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Input.mouse = e;
        Input.MouseReleased();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        Input.mouse = e;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        Input.mouse = e;
    }

    public void spawnEntity(Entity e, int entityLayer) {
        entities[entityLayer].add(e);
    }

    public void deSpawnEntity(Entity e, int entityLayer) {
        deSpawnQueue[entityLayer].add(e);
    }

    public void delayEntityDraw() {
        delayedEntities.add(currentEntity);
    }

    public void delayEntityDraw(Entity e) {
        delayedEntities.add(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Input.mouse = e;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Input.mouse = e;
    }
}
