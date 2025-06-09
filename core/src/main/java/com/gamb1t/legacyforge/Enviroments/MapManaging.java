package com.gamb1t.legacyforge.Enviroments;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Server;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.Networking.ConnectionManager;
import com.gamb1t.legacyforge.Networking.Network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MapManaging {
    private GameMap currentMap;
    private GameMap hitboxes;
    private float cameraX, cameraY;
    private Polygon[][] hitbox;
    private Layout OUTSIDE;
    private ArrayList<Vector2> respEnemy = new ArrayList<>();
    private List<Vector2> respPlayer = new ArrayList<>();
    private Vector2 shopCoordinates;
    private Vector2 armorShopCoordinates;
    private List<Vector2> doorPairs = new ArrayList<>();
    private String mapName;
    private String hitboxFile;
    private String tilesSpritesheet;
    private int mapWidth;
    private int mapHeight;
    private ArrayList<Enemy> enemies;
    private int killCount = 0;
    private final int KILLS_PER_DOOR = 4;
    private float countdownTimer = 5;
    private boolean isStartingDoorOpened = false;
    private Server server;
    private String roomName;
    private int roomId;
    private boolean goingBack;
    private String gameMode = " ";
    private List<Player> players;
    private boolean pvpStarted = false;
    private static final Random rand = new Random();

    private static class Door {
        Vector2 position;
        int id;

        Door(Vector2 position, int id) {
            this.position = position;
            this.id = id;
        }
    }

    public MapManaging(String mapName, String hitboxesFile, String tilesSpritesheet, int mapWidth, int mapHeight) {
        this.mapName = mapName;
        this.hitboxFile = hitboxesFile;
        this.tilesSpritesheet = tilesSpritesheet;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        if (mapName == null || hitboxesFile == null || tilesSpritesheet == null) {
            Gdx.app.error("MapManaging", "Invalid map file paths");
            return;
        }
        currentMap = LoadFile(mapName, mapWidth, mapHeight);
        hitboxes = LoadFile(hitboxFile, mapWidth, mapHeight);
        if (currentMap == null || hitboxes == null) {
            Gdx.app.error("MapManaging", "Failed to load map or hitbox files");
            return;
        }
        createHitboxes();
    }

    private int getDoorId(Vector2 doorPos) {
        int col = (int) (doorPos.x / GameConstants.Sprite.SIZE);
        int row = (int) (doorPos.y / GameConstants.Sprite.SIZE);
        return currentMap.getSpriteID(col, row);
    }

    public void initializeOutside() {
        if (tilesSpritesheet != null) {
            OUTSIDE = new Layout(tilesSpritesheet);
            OUTSIDE.initialize();
        } else {
            Gdx.app.error("MapManaging", "Tiles spritesheet is null");
        }
    }

    public void setCameraValues(float cameraX, float cameraY) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
    }

    public void updateCameraToFollowPlayer(float playerX, float playerY) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        cameraX = playerX - screenWidth / 2f + GameConstants.Sprite.SIZE / 2f;
        cameraY = playerY - screenHeight / 2f + GameConstants.Sprite.SIZE / 2f;
        cameraX = Math.max(0, Math.min(cameraX, getMaxWidthCurrentMap() - screenWidth));
        cameraY = Math.max(0, Math.min(cameraY, getMaxHeightCurrentMap() - screenHeight));
    }

    public boolean canMoveHere(float x, float y) {
        if (x < 0 || y < 0) return false;
        if (currentMap == null) return false;
        if (x >= getMaxWidthCurrentMap() || y >= getMaxHeightCurrentMap()) return false;
        return true;
    }

    public int getMaxWidthCurrentMap() {
        return currentMap != null ? currentMap.getArrayWidth() * GameConstants.Sprite.SIZE : 0;
    }

    public int getMaxHeightCurrentMap() {
        return currentMap != null ? currentMap.getArrayHeight() * GameConstants.Sprite.SIZE : 0;
    }

    public void draw(SpriteBatch spriteBatch) {
        if (spriteBatch == null || OUTSIDE == null || currentMap == null) {
            Gdx.app.error("MapManaging", "Cannot draw: spriteBatch, OUTSIDE, or currentMap is null");
            return;
        }
        spriteBatch.begin();
        for (int j = 0; j < currentMap.getArrayHeight(); j++) {
            for (int i = 0; i < currentMap.getArrayWidth(); i++) {
                int spriteID = currentMap.getSpriteID(i, j);
                if (spriteID != -999 && OUTSIDE.getSprite(spriteID) != null) {
                    spriteBatch.draw(
                        OUTSIDE.getSprite(spriteID),
                        i * GameConstants.Sprite.SIZE + cameraX,
                        j * GameConstants.Sprite.SIZE + cameraY,
                        GameConstants.Sprite.SIZE,
                        GameConstants.Sprite.SIZE
                    );
                }
            }
        }
        spriteBatch.end();
    }

    public void createHitboxes() {
        if (hitboxes == null) {
            Gdx.app.error("MapManaging", "Hitboxes map is null");
            return;
        }
        hitbox = new Polygon[mapHeight][mapWidth];
        int doorCount = 0;
        int wallCount = 0;
        for (int row = 0; row < mapHeight; row++) {
            for (int col = 0; col < mapWidth; col++) {
                int spriteID = hitboxes.getSpriteID(col, row);
                if (spriteID == 1) {
                    Polygon polygon = new Polygon(new float[]{
                        0, 0,
                        GameConstants.Sprite.SIZE, 0,
                        GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE,
                        0, GameConstants.Sprite.SIZE
                    });
                    float hitboxX = col * GameConstants.Sprite.SIZE;
                    float hitboxY = row * GameConstants.Sprite.SIZE;
                    polygon.setPosition(hitboxX, hitboxY);
                    hitbox[row][col] = polygon;
                    wallCount++;
                }
            }
        }
        for (Vector2 v : doorPairs) {
            Polygon polygon = new Polygon(new float[]{
                0, 0,
                GameConstants.Sprite.SIZE * 2, 0,
                GameConstants.Sprite.SIZE * 2, GameConstants.Sprite.SIZE * 2,
                0, GameConstants.Sprite.SIZE * 2
            });
            float hitboxX = (int) v.x * GameConstants.Sprite.SIZE;
            float hitboxY = (int) v.y * GameConstants.Sprite.SIZE;
            polygon.setPosition(hitboxX, hitboxY);
            hitbox[(int) v.y][(int) v.x] = polygon;
            doorCount++;
        }
        Gdx.app.log("MapManaging", "Hitboxes created: " + doorCount + " doors, " + wallCount + " walls");
    }

    public boolean checkNearbyWallCollision(Polygon polygonToCheck, float x, float y) {
        if (polygonToCheck == null || hitbox == null) return false;
        int spriteSize = GameConstants.Sprite.SIZE;
        int currentRow = (int) (y / spriteSize);
        int currentCol = (int) (x / spriteSize);

        int startRow = Math.max(0, currentRow - 1);
        int endRow = Math.min(hitbox.length - 1, currentRow + 1);
        int startCol = Math.max(0, currentCol - 1);
        int endCol = Math.min(hitbox[0].length - 1, currentCol + 1);

        Polygon predictPolyg = new Polygon(polygonToCheck.getVertices());
        predictPolyg.setPosition(x, y);

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                if (hitbox[row][col] != null && Intersector.overlapConvexPolygons(predictPolyg, hitbox[row][col])) {
                    return true;
                }
            }
        }
        return false;
    }

    public void openDoor(Vector2 doorPos) {
        if (currentMap == null || hitbox == null) return;
        for (Vector2 door : doorPairs) {
            if (door.equals(doorPos)) {
                currentMap.setSpriteID((int) doorPos.x, (int) doorPos.y, 11);
                currentMap.setSpriteID((int) doorPos.x + 1, (int) doorPos.y, 11);
                currentMap.setSpriteID((int) doorPos.x, (int) doorPos.y + 1, 11);
                currentMap.setSpriteID((int) doorPos.x + 1, (int) doorPos.y + 1, 11);
                hitboxes.setSpriteID((int) doorPos.x, (int) doorPos.y, 0);
                hitbox[(int) doorPos.y][(int) doorPos.x] = null;
                if (server != null) {
                    Network.DoorOpened doorOpened = new Network.DoorOpened();
                    doorOpened.x = (int) doorPos.x;
                    doorOpened.y = (int) doorPos.y;
                    ConnectionManager.sendToConnections(roomName, roomId, doorOpened);
                }
            }
        }
    }

    public void closeDoor(Vector2 doorPos){

        System.out.println(doorPos.x+ " " + doorPos.y);
                currentMap.setSpriteID((int) doorPos.x, (int) doorPos.y, 36);
                currentMap.setSpriteID((int) doorPos.x + 1, (int) doorPos.y, 37);
                currentMap.setSpriteID((int) doorPos.x, (int) doorPos.y + 1, 36);
                currentMap.setSpriteID((int) doorPos.x + 1, (int) doorPos.y + 1, 37);
    }

    public void update(float delta) {
        if (!isStartingDoorOpened && !doorPairs.isEmpty() && !gameMode.equals("1v1")) {
            countdownTimer -= delta;
            if (countdownTimer <= 0) {
                openDoor(doorPairs.get(0));
                isStartingDoorOpened = true;
                Gdx.app.log("MapManaging", "Starting door opened after 5 seconds");
                countdownTimer = 5;
            }
        } else if (areAllDoorsOpened() && !gameMode.equals("1v1")) {
            countdownTimer -= delta;
            if (countdownTimer <= 0) {
                goingBack = true;
            }
        }
        updateKillCountAndOpenDoors();
    }

    public void setGoingBack(boolean goingBack) {
        this.goingBack = goingBack;
    }

    public boolean updateKillCountAndOpenDoors() {
        if (enemies == null || doorPairs.isEmpty() || !isStartingDoorOpened) {
            return false;
        }
        int currentKillCount = 0;
        for (Enemy enemy : enemies) {
            if (!enemy.getIsAlive()) {
                currentKillCount++;
            }
        }
        if (currentKillCount > killCount) {
            killCount = currentKillCount;
            Gdx.app.log("MapManaging", "Kill count updated to: " + killCount);
            int doorIndexToOpen = killCount / KILLS_PER_DOOR;
            if (doorIndexToOpen > 0 && doorIndexToOpen <= doorPairs.size() - 1) {
                Vector2 doorPos = doorPairs.get(doorIndexToOpen);
                openDoor(doorPos);
                Gdx.app.log("MapManaging", "Door pair " + (doorIndexToOpen) + " opened");
                return true;
            }
        }
        return false;
    }

    public boolean areAllDoorsOpened() {
        return doorPairs.size() <= killCount / KILLS_PER_DOOR;
    }

    private GameMap LoadFile(String mapName, int mapLengthInTiles, int mapHeightInTiles) {
        if (mapName == null) {
            Gdx.app.error("MapManaging", "Map file name is null");
            return null;
        }
        FileHandle handle = Gdx.files.internal(mapName);
        if (!handle.exists()) {
            Gdx.app.error("MapManaging", "Map file not found: " + mapName);
            return null;
        }
        int[][] spriteIds = new int[mapLengthInTiles][mapHeightInTiles];
        String[] lines = handle.readString().split("\n");
        List<Door> doors = new ArrayList<>();

        for (int k = 0; k < Math.min(lines.length, mapHeightInTiles); k++) {
            int row = mapHeightInTiles - 1 - k;
            String line = lines[k];
            if (line == null || line.trim().isEmpty()) continue;
            String[] numbers = line.trim().split("\\s+");
            for (int col = 0; col < Math.min(numbers.length, mapLengthInTiles); col++) {
                try {
                    String value = numbers[col];
                    int id = Integer.parseInt(value);
                    if (id == -1) {
                        System.out.println("Added respPlayer");
                        respPlayer.add(new Vector2(col * GameConstants.Sprite.SIZE, row * GameConstants.Sprite.SIZE));
                    } else if (id == -2) {
                        respEnemy.add(new Vector2(col * GameConstants.Sprite.SIZE, row * GameConstants.Sprite.SIZE));
                    } else if (id >= -30 && id <= -12) {
                        System.out.println("Added doors");
                        System.out.println(col + " " + row);
                        doors.add(new Door(new Vector2(col, row), id));
                    } else if (id == -5) {
                        shopCoordinates = new Vector2(col * GameConstants.Sprite.SIZE, row * GameConstants.Sprite.SIZE);
                    } else if (id == -6) {
                        armorShopCoordinates = new Vector2(col * GameConstants.Sprite.SIZE, row * GameConstants.Sprite.SIZE);
                    }
                    spriteIds[col][row] = id;
                } catch (NumberFormatException e) {
                    Gdx.app.error("MapManaging", "Invalid number in map file: " + numbers[col] + " at col " + col + ", row " + row);
                    spriteIds[col][row] = 0;
                }
            }
        }

        doors.sort((d1, d2) -> Integer.compare(d2.id, d1.id));
        doorPairs.clear();
        for (Door door : doors) {
            doorPairs.add(door.position);
            System.out.println("The id is" + door.id + " ArrayPos is" + door.position.x + " " + door.position.y);
        }

        return new GameMap(spriteIds);
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
        Gdx.app.log("MapManaging", "Game mode set to: " + gameMode);
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
        Gdx.app.log("MapManaging", "Players set, count: " + (players != null ? players.size() : 0));
    }

    public boolean isPvpStarted() {
        return isStartingDoorOpened;
    }

    public void resetRound() {
        countdownTimer = 5;
        pvpStarted = false;
        isStartingDoorOpened = false;
        killCount = 0;
        Gdx.app.log("MapManaging", "Round reset for room: " + roomId);
        if (server != null) {
            Network.RoundReset reset = new Network.RoundReset();
            reset.countdownInSeconds = 3;
            ConnectionManager.sendToConnections(roomName, roomId, reset);
        }
    }

    public Vector2 getRespPlayer() {
        return respPlayer.get(0);
    }

    public ArrayList<Vector2> getRespEnemy() {
        return respEnemy;
    }

    public Vector2 getShopCoordinates() {
        return shopCoordinates;
    }

    public Vector2 getArmorShopCoordinates() {
        return armorShopCoordinates;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public String getMapName() {
        return mapName;
    }

    public String getHitboxFile() {
        return hitboxFile;
    }

    public String getTilesSpritesheet() {
        return tilesSpritesheet;
    }

    public void addEnemiesForDoor(ArrayList<Enemy> enemies) {
        if (doorPairs != null) {
            this.enemies = enemies;
            Gdx.app.log("MapManaging", "Enemies added: " + enemies.size());
        }
    }

    public List<Vector2> getRespPlayerAll() {
        return respPlayer;
    }

    public int getKillCount() {
        return killCount;
    }

    public int getKillsPerDoor() {
        return KILLS_PER_DOOR;
    }

    public float getCountdownTimer() {
        return countdownTimer;
    }

    public boolean isStartingDoorOpened() {
        return isStartingDoorOpened;
    }

    public boolean getGoingBack() {
        return goingBack;
    }

    public void setServer(Server server, String roomName, int roomId) {
        this.server = server;
        this.roomName = roomName;
        this.roomId = roomId;
    }

    public void closeAllDoors() {
        if (currentMap == null || hitbox == null || doorPairs.isEmpty()) {
            Gdx.app.error("MapManaging", "Cannot close doors: map or doorPairs empty");
            return;
        }
        for (Vector2 doorPos : doorPairs) {
            int x = (int) doorPos.x;
            int y = (int) doorPos.y;
            currentMap.setSpriteID(x, y, -12);
            currentMap.setSpriteID(x + 1, y, -12);
            currentMap.setSpriteID(x, y + 1, -12);
            currentMap.setSpriteID(x + 1, y + 1, -12);
            hitboxes.setSpriteID(x, y, 1);
            Polygon polygon = new Polygon(new float[]{
                0, 0,
                GameConstants.Sprite.SIZE * 2, 0,
                GameConstants.Sprite.SIZE * 2, GameConstants.Sprite.SIZE * 2,
                0, GameConstants.Sprite.SIZE * 2
            });
            polygon.setPosition(x * GameConstants.Sprite.SIZE, y * GameConstants.Sprite.SIZE);
            hitbox[y][x] = polygon;
            if (server != null) {
                Network.DoorClosed doorClosed = new Network.DoorClosed();
                doorClosed.x = x;
                doorClosed.y = y;
                ConnectionManager.sendToConnections(roomName, roomId, doorClosed);
            }
        }
        isStartingDoorOpened = false;
        pvpStarted = false;
        Gdx.app.log("MapManaging", "All doors closed for room: " + roomId);
    }

    public void openAllDoors() {
        if (currentMap == null || hitbox == null || doorPairs.isEmpty()) {
            Gdx.app.error("MapManaging", "Cannot open doors: map or doorPairs empty");
            return;
        }
        for (Vector2 doorPos : doorPairs) {
            int x = (int) doorPos.x;
            int y = (int) doorPos.y;
            currentMap.setSpriteID(x, y, 11);
            currentMap.setSpriteID(x + 1, y, 11);
            currentMap.setSpriteID(x, y + 1, 11);
            currentMap.setSpriteID(x + 1, y + 1, 11);
            hitboxes.setSpriteID(x, y, 0);
            hitbox[y][x] = null;
            if (server != null) {
                Network.DoorOpened doorOpened = new Network.DoorOpened();
                doorOpened.x = x;
                doorOpened.y = y;
                ConnectionManager.sendToConnections(roomName, roomId, doorOpened);
            }
        }
        isStartingDoorOpened = true;
        pvpStarted = true;
        Gdx.app.log("MapManaging", "All doors opened for room: " + roomId);
    }

    public void assignRandomSpawnPoints(List<Player> players) {
        if (respPlayer.size() != 2 || players.size() != 2) {
            Gdx.app.error("MapManaging", "Invalid spawn points or player count: spawns=" + respPlayer.size() + ", players=" + players.size());
            return;
        }
        List<Vector2> spawns = new ArrayList<>(respPlayer);
        Collections.shuffle(spawns, rand);
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            Vector2 spawn = spawns.get(i);
            p.setPosition(spawn.x + GameConstants.Sprite.SIZE / 2, spawn.y + GameConstants.Sprite.SIZE / 2);
            p.setRespPoint(spawn);
            Gdx.app.log("MapManaging", "Player " + p.getName() + " assigned spawn at (" + spawn.x + ", " + spawn.y + ")");
        }
    }
}
