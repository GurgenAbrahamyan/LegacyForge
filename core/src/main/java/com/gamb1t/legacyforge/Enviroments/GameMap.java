package com.gamb1t.legacyforge.Enviroments;



public class GameMap {

    private int[][] spriteIds;

    public GameMap(int[][] spriteIds) {
        this.spriteIds = spriteIds;
    }

    public int getSpriteID(int xIndex, int yIndex) {
        return spriteIds[yIndex][xIndex];
    }

    public int getArrayWidth() {
        return spriteIds[0].length;
    }

    public int getArrayHeight() {
        return spriteIds.length;
    }


}
