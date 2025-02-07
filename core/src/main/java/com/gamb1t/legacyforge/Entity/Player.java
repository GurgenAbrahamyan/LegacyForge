package com.gamb1t.legacyforge.Entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;

public class Player extends GameCharacters {
    private int x = 0;
    private int y =0 ;
    private int lvl = 1;
    private int health = 100;

    public Player(String recourceName, int spritesheetLength, int spritesheetWidth) {
        super(recourceName, spritesheetLength, spritesheetWidth);
    }

}

