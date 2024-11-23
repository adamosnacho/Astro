package org.astro.core;

import org.astro.core.saving.Save;
import org.astro.core.saving.Saving;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.astro.core.Terrain.*;

public class Building implements Save {
    public static Map<Utils.Coords, Tile> placedTiles = new HashMap<>();

    static { Saving.save.add(new Building()); }

    public Building(Object o) {
        Map<Utils.Coords, String> sPlacedTiles = (Map<Utils.Coords, String>) o;
        placedTiles.clear();
        sPlacedTiles.forEach(((coords, s) -> {
            for (Tile tile : tiles) {
                if (Objects.equals(tile.name, s)) placedTiles.put(coords, tile);
            }
        }));
    }
    public Building() {}

    public static boolean placeTile(int x, int y, Tile tile) {
        if (placedTiles.containsKey(new Utils.Coords(x, y))) return false;

        placedTiles.put(new Utils.Coords(x, y), getTile(x, y));

        setTile(x, y, tile);
        return true;
    }

    public static boolean breakTile(int x, int y) {
        if (placedTiles.containsKey(new Utils.Coords(x, y))){
            setTile(x, y, placedTiles.get(new Utils.Coords(x, y)));
            placedTiles.remove(new Utils.Coords(x, y));
            return true;
        }
        return false;
    }

    @Override
    public Object save() {
        Map<Utils.Coords, String> sPlacedTiles = new HashMap<>();
        placedTiles.forEach(((coords, tile) -> sPlacedTiles.put(coords, tile.name)));
        return sPlacedTiles;
    }
}
