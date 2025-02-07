package com.gamb1t.legacyforge.Entity;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;

public class GameCharacters {


    private TextureRegion[][] SpriteSheet;

    public GameCharacters(String recourceName, int spritesheetLength, int spritesheetWidth) {

        Texture entitiesTexture = new Texture(recourceName);

        SpriteSheet = new TextureRegion[spritesheetWidth][spritesheetLength];

        SpriteSheet = TextureRegion.split(entitiesTexture, GameConstants.Sprite.DEFAULT_SIZE, GameConstants.Sprite.DEFAULT_SIZE);

    }

    public TextureRegion[][] getSpriteSheet() {
        return SpriteSheet;
    }

    public TextureRegion getSprite(int yPos, int xPos) {
        return SpriteSheet[yPos][xPos];
    }


}
