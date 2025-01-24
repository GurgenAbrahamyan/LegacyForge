package com.gamb1t.legacyforge.HelperClasses;


import static com.gamb1t.legacyforge.MainActivity.GET_HEIGHT;
import static com.gamb1t.legacyforge.MainActivity.GET_WIDTH;

import android.graphics.Canvas;

public final class GameConstants {
    public static final class Face_Dir{
        public static final int DOWN = 0;
        public static final int UP = 1;
        public static final int LEFT = 2;
        public static final int RIGHT = 3;
    }
    public static final class Sprite{
        public static final int DEFAULT_SIZE = 16;
        public static final int SCALE = 10;
        public static final int SIZE = SCALE*DEFAULT_SIZE;

    }
}