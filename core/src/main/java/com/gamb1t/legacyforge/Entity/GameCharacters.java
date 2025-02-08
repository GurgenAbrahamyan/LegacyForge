package com.gamb1t.legacyforge.Entity;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;

public abstract class GameCharacters {

    protected int aniTick = 0;
    protected int currentFrame = 0;
    protected int animationFrameAmount = 4;
    protected  int aniSpeed = 10;
    protected  int FaceDirection = GameConstants.Face_Dir.DOWN;




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

    public void updateAnimation() {

        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            currentFrame++;
            if (currentFrame >= animationFrameAmount)
                currentFrame = 0;
        }
    }

    public void resetAnimation() {
        aniTick = 0;
        currentFrame = 0;
    }

    public int getAniIndex() {
        return currentFrame;
    }

    public int getFaceDir() {
        return FaceDirection;
    }

    public void setFaceDir(int faceDir) {
        this.FaceDirection = faceDir;
    }



}
