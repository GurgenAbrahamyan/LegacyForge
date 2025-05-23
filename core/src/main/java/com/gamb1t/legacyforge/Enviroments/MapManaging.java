package com.gamb1t.legacyforge.Enviroments;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;

import java.util.ArrayList;

public class MapManaging {

    private GameMap currentMap;
    private GameMap hitboxes;
    private float cameraX, cameraY;
    private Polygon[][] hitbox;
    private Layout OUTSIDE;
    private ArrayList<Vector2> respEenemy = new ArrayList<>();
    private Vector2 respPlayer = new Vector2();

    private Vector2 shopCoordinates = new Vector2();

    private String mapName;
    private String hitboxFile;
    private String tilesSpritesheet;
    private  int mapWidth;
    private  int mapHeight;


    public MapManaging(String mapName, String hitboxesFile, String tilesSpritesheet, int mapWidth, int mapHeight) {
        this.mapName = mapName;
        this.hitboxFile= hitboxesFile;
        this.tilesSpritesheet = tilesSpritesheet;
        this.mapWidth =mapWidth;
        this.mapHeight = mapHeight;
        currentMap = LoadFile(mapName, mapWidth, mapHeight);
        hitboxes = LoadFile(hitboxesFile, mapWidth, mapHeight);
        createHitboxes();

    }

    public void initializeOutside(){
        OUTSIDE =new Layout(tilesSpritesheet);
        OUTSIDE.initialize();
    }

    public void setCameraValues(float cameraX, float cameraY) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
    }

    public boolean canMoveHere(float x, float y) {
        if (x < 0 || y < 0) return false;
        if (x >= getMaxWidthCurrentMap() || y >= getMaxHeightCurrentMap()) return false;
        return true;
    }

    public int getMaxWidthCurrentMap() {
        return currentMap.getArrayWidth() * GameConstants.Sprite.SIZE;
    }
    public int getMaxHeightCurrentMap() {
        return currentMap.getArrayHeight() * GameConstants.Sprite.SIZE;
    }

    public void draw(SpriteBatch spriteBatch) {
        spriteBatch.begin();
        for (int j = 0; j < currentMap.getArrayHeight(); j++) {
            for (int i = 0; i < currentMap.getArrayWidth(); i++) {
                spriteBatch.draw(
                    OUTSIDE.getSprite(currentMap.getSpriteID(i, j)),
                    i * GameConstants.Sprite.SIZE + cameraX,
                    j * GameConstants.Sprite.SIZE + cameraY,
                    GameConstants.Sprite.SIZE,
                    GameConstants.Sprite.SIZE
                );
            }
        }
        spriteBatch.end();
    }

    public void createHitboxes() {
        int mapHeight = hitboxes.getArrayHeight();
        int mapWidth = hitboxes.getArrayWidth();
        hitbox = new Polygon[mapHeight][mapWidth];

        for (int row = 0; row < mapHeight; row++) {
            for (int col = 0; col < mapWidth; col++) {
                if (hitboxes.getSpriteID(col, row) == 1) {
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

                    System.out.println("Hitbox created at: " + hitboxX + ", " + hitboxY);
                }
            }
        }
    }


    public boolean checkNearbyWallCollision(Polygon polygonToCheck, float x, float y) {
        int spriteSize = GameConstants.Sprite.SIZE;
        int currentRow = (int) (y / spriteSize);
        int currentCol = (int) (x / spriteSize);

        int startRow = Math.max(0, currentRow - 1);
        int endRow = Math.min(hitbox.length - 1, currentRow + 1);
        int startCol = Math.max(0, currentCol - 1);
        int endCol = Math.min(hitbox[startRow].length - 1, currentCol + 1);

        Polygon predictPolyg = new Polygon(polygonToCheck.getVertices());
        predictPolyg.setPosition(x, y);

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                if (hitbox[row][col] != null && Intersector.overlapConvexPolygons(predictPolyg, hitbox[row][col])) {
                    //System.out.println("colided");
                    return true;
                }
            }
        }

        return false;
    }



    private GameMap LoadFile(String mapName, int mapLengthInTiles, int mapHeightInTiles) {
        int[][] spriteIds = new int[mapLengthInTiles][mapHeightInTiles];

        FileHandle handle =  Gdx.files.internal(mapName);
        System.out.println(handle);
        int row = mapHeightInTiles-1;
        for (String line : handle.readString().split("\n")) {
            if (row < 0) break;

            String[] numbers = line.split(" ");
            for (int col = 0; col < Math.min(numbers.length, mapLengthInTiles); col++) {
                if(Integer.parseInt(numbers[col]) == -1){
                    respPlayer.set(col*GameConstants.Sprite.SIZE, row*GameConstants.Sprite.SIZE);
                    continue;

                }
                if(Integer.parseInt(numbers[col]) == -2){
                    respEenemy.add(new Vector2(col*GameConstants.Sprite.SIZE, row*GameConstants.Sprite.SIZE));
                continue;

                }
                if(Integer.parseInt(numbers[col]) == -5){
                    shopCoordinates.set(col*GameConstants.Sprite.SIZE, row*GameConstants.Sprite.SIZE);
                }



                spriteIds[col][row] = Integer.parseInt(numbers[col]);
            }
            row--;
        }

        return new GameMap(spriteIds);
    }

    public Vector2 getRespPlayer(){
        return respPlayer;
    }
    public ArrayList<Vector2> getRespEenemy(){
        return respEenemy;
    }
    public Vector2 getShopCoordinates(){
        return  shopCoordinates;
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
}

