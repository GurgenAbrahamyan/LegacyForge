package com.gamb1t.legacyforge.ManagerClasses;

import com.badlogic.gdx.Gdx;

public final class GameConstants {

    public static int GET_WIDTH = Gdx.graphics.getWidth();
    public static int GET_HEIGHT = Gdx.graphics.getHeight();

    public static final class Face_Dir {
        public static final int UP = 0;
        public static final int DOWN = 1;
        public static final int LEFT = 2;
        public static final int RIGHT = 3;
    }

    public static final class Sprite {
        public static final int DEFAULT_SIZE = 16;
        public static final int SCALE = GET_WIDTH / 20 / DEFAULT_SIZE;
        public static final int SIZE = SCALE * DEFAULT_SIZE;

    }}
