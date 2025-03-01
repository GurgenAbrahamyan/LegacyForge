package com.gamb1t.legacyforge.Enviroments;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MapManaging {

    private GameMap currentMap;
    private float cameraX, cameraY;
    Layout OUTSIDE = new Layout("tileset_floor.png", 22, 26);

    public MapManaging(String mapName, String tilesSpritesheet, int  mapWidth, int mapLength) {
        LoadMap(mapName, mapWidth ,mapLength);
        OUTSIDE = new Layout(tilesSpritesheet, mapWidth, mapLength);
    }

    public void setCameraValues(float cameraX, float cameraY) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
    }

    public boolean canMoveHere(float x, float y) {
        if (x - GameConstants.Sprite.SIZE/2< 0 || y - GameConstants.Sprite.SIZE/2 < 0)
            return false;

        if (x - GameConstants.Sprite.SIZE/2 >= getMaxWidthCurrentMap() || y - GameConstants.Sprite.SIZE/2 >= getMaxHeightCurrentMap())
            return false;

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


    private void LoadMap(String mapName, int mapLengthInTiles, int mapHeightInTiles) {

        int[][] spriteIds = new int[mapHeightInTiles][mapLengthInTiles];

        try{
            InputStream is = getClass().getResourceAsStream(mapName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            int col =0;
            int row = 0;



            while(col < mapLengthInTiles && row < mapHeightInTiles){
                String line = br.readLine();
                while(col < mapLengthInTiles){
                    String numbers[] = line.split(" ");

                    int num = Integer.parseInt(numbers[col]);

                    spriteIds[col][row] = num;
                    col ++;

                }
                if(col == mapLengthInTiles){
                    col = 0;
                    row ++;
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();

        };

        currentMap = new GameMap(spriteIds);
    }
}
