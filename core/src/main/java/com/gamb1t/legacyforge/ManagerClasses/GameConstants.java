package com.gamb1t.legacyforge.ManagerClasses;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.Sprite.DEFAULT_SIZE;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.Sprite.SCALE;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.Sprite.SIZE;

import com.badlogic.gdx.Gdx;

public final class GameConstants {

    public static  int GET_WIDTH = (Gdx.graphics != null) ? (Gdx.graphics.getWidth()) : (1920);
    public static  int GET_HEIGHT = (Gdx.graphics != null) ? (Gdx.graphics.getHeight()) : (1080);

    public static final class Face_Dir {
        public static final int UP = 0;
        public static final int DOWN = 1;
        public static final int LEFT = 2;
        public static final int RIGHT = 3;
    }

    public static void init(){

        GET_WIDTH =  (1920);
        GET_HEIGHT = (1080);

        DEFAULT_SIZE = 16;
        SCALE = GET_WIDTH/20/DEFAULT_SIZE;
        SIZE = SCALE*DEFAULT_SIZE;



    }


    public static final class Sprite {
        public static int DEFAULT_SIZE = 16;
        public static int SCALE = GET_WIDTH / 20 / DEFAULT_SIZE;
        public static int SIZE = SCALE * DEFAULT_SIZE;

    }}
