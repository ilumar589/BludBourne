package com.packtpub.libgdx.bludbourne;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;

import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;
import static jdk.internal.joptsimple.internal.Strings.isNullOrEmpty;

public class MapManager {

    private static final String TAG = MapManager.class.getSimpleName();

    // All maps for the game
    private Map<String, String> mapTable;
    private Map<String, Vector2> playerStartLocationTable;

    // Map names
    private final static String TOP_WORLD = "TOP_WORLD";
    private final static String TOWN = "TOWN";
    private final static String CASTLE_OF_DOOM = "CASTLE_OF_DOOM";

    // Map layers
    private final static String MAP_COLLISION_LAYER = "MAP_COLLISION_LAYER";
    private final static String MAP_SPAWNS_LAYER = "MAP_SPAWNS_LAYER";
    private final static String MAP_PORTAL_LAYER = "MAP_PORTAL_LAYER";

    private final static String PLAYER_START = "PLAYER_START";

    private Vector2 playerStartPositionRect;
    private Vector2 closestPlayerStartPosition;
    private Vector2 convertedUnits;

    private Vector2 playerStart;
    private TiledMap currentMap;
    private String currentMapName;
    private MapLayer collisionLayer;
    private MapLayer portalLayer;
    private MapLayer spawnsLayer;

    public final static float UNIT_SCALE = 1/16f;

    public MapManager() {
        playerStart = new Vector2(0, 0);

        mapTable = new Hashtable<>();
        mapTable.put(TOP_WORLD, "maps/topworld.tmx");
        mapTable.put(TOWN, "maps/town.tmx");
        mapTable.put(CASTLE_OF_DOOM, "maps/castle_of_doom.tmx");

        playerStartLocationTable = new Hashtable<>();
        playerStartLocationTable.put(TOP_WORLD, playerStart.cpy());
        playerStartLocationTable.put(TOWN, playerStart.cpy());
        playerStartLocationTable.put(CASTLE_OF_DOOM, playerStart.cpy());

        playerStartPositionRect = new Vector2(0, 0);
        closestPlayerStartPosition = new Vector2(0, 0);
        convertedUnits = new Vector2(0, 0);

        currentMap = null;
    }

    public void loadMap(String mapName) {
        playerStart.set(0, 0);

        String mapFullPath = mapTable.get(mapName);

        if (isNullOrEmpty(mapFullPath)) {
            Gdx.app.debug(TAG, "Path " + mapFullPath + " is invalid");
            return;
        }

        if (!isNull(currentMap)) {
            currentMap.dispose();
        }

        Utility.loadMapAsset(mapFullPath);

        if (Utility.isAssetLoaded(mapFullPath)) {
            currentMap = Utility.getMapAsset(mapFullPath);
            currentMapName = mapName;
        } else {
            Gdx.app.debug(TAG, "Map not loaded");
            return;
        }

        collisionLayer = Objects.requireNonNull(currentMap).getLayers().get(MAP_COLLISION_LAYER);
        if (isNull(collisionLayer)) {
            Gdx.app.debug(TAG, "No collision layer!");
        }

        portalLayer = Objects.requireNonNull(currentMap).getLayers().get(MAP_PORTAL_LAYER);
        if (isNull(portalLayer)) {
            Gdx.app.debug(TAG, "No portal layer!");
        }

        spawnsLayer = Objects.requireNonNull(currentMap).getLayers().get(MAP_SPAWNS_LAYER);
        if (isNull(spawnsLayer)) {
            Gdx.app.debug(TAG, "No spawn layer!");
        } else {
            Vector2 start = playerStartLocationTable.get(currentMapName);
            if (start.isZero()) {
                setClosestPlayerStartPosition(playerStart);
                start = playerStartLocationTable.get(currentMapName);
            }

            playerStart.set(start.x, start.y);
        }

        Gdx.app.debug(TAG, "Player Start: (" + playerStart.x + "," + playerStart.y + ")");
    }

    public TiledMap getCurrentMap() {
        if (isNull(currentMap)) {
            currentMapName = TOWN;
            loadMap(currentMapName);
        }

        return currentMap;
    }

    public MapLayer getCollisionLayer() {
        return collisionLayer;
    }

    public MapLayer getPortalLayer() {
        return portalLayer;
    }

    public Vector2 getPlayerStartUnitScaled() {
        Vector2 playerStart = this.playerStart.cpy();
        playerStart.set(this.playerStart.x * UNIT_SCALE, this.playerStart.y * UNIT_SCALE);
        return playerStart;
    }

    public void setClosestStartPositionFromScaledUnits(Vector2 position) {
        convertedUnits.set(position.x/UNIT_SCALE, position.y/UNIT_SCALE);
        setClosestPlayerStartPosition(convertedUnits);
    }

    private void setClosestPlayerStartPosition(final Vector2 position) {
        Gdx.app.debug(TAG, "setClosestStartPosition INPUT: (" + position.x + "," + position.y + ") " + currentMapName);

        // Get last known position on this map
        playerStartPositionRect.set(0, 0);
        closestPlayerStartPosition.set(0, 0);
        float shortestDistance = 0f;

        // Go through all player start positions and choose the one closest to the last known position
        for (MapObject mapObject : spawnsLayer.getObjects()) {
            if (mapObject.getName().equalsIgnoreCase(PLAYER_START)) {
                // this sets playerStartPositionRect with the mapObject's rectangle coordinates and returns it in the same function
               ((RectangleMapObject)mapObject).getRectangle().getPosition(playerStartPositionRect);
                float distance = position.dst(playerStartPositionRect);

                Gdx.app.debug(TAG, "distance: " + distance + " for " + currentMapName);

                if ((distance < shortestDistance) || distance == 0 ) {
                    closestPlayerStartPosition.set(playerStartPositionRect);
                    shortestDistance = distance;

                    Gdx.app.debug(TAG, "closest START is: (" + closestPlayerStartPosition.x + "," + closestPlayerStartPosition.y + ") " +  currentMapName);
                }
            }
        }

        playerStartLocationTable.put(currentMapName, closestPlayerStartPosition.cpy());
    }
}
