package com.gamb1t.legacyforge.Enviroments;



public class GameMap {

    private int[][] Ids;

    public GameMap(int[][] spriteIds) {
        this.Ids = spriteIds;
    }

    public int getSpriteID(int xIndex, int yIndex) {
        return Ids[xIndex][yIndex];
    }

    public void setSpriteID(int xIndex, int yIndex, int i) {
        Ids[xIndex][yIndex] = i;
    }

    public int getArrayWidth() {
        return Ids[0].length;
    }

    public int getArrayHeight() {
        return Ids.length;
    }

    public int[][] getIds(){
        return Ids;
    }


}
