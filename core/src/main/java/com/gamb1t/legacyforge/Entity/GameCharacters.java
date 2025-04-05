package com.gamb1t.legacyforge.Entity;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;
import com.gamb1t.legacyforge.Weapons.Weapon;

public abstract class GameCharacters {

    protected int aniTick = 0;
    protected int currentFrame = 0;
    protected int animationFrameAmount = 4;
    protected  int aniSpeed = 10;
    protected  int FaceDirection = GameConstants.Face_Dir.DOWN;


    protected float speed;
    protected int hp, maxHp;
    protected float mana = 100, maxMana = 100;
    protected boolean isAlive;
    protected GameScreen.PointF entityPos;
    protected  float width;
    protected  float heigh;

    public Polygon hitbox;

    protected GameScreen gameScreen;
    protected Weapon weapon;

    public float cameraX, cameraY;

    protected TextureRegion[][] SpriteSheet;

    public GameCharacters(float x, float y, float width, float height) {
        this.width = width;
        this.heigh = height;

        float[] vertices = {
            0, 0,
            width, 0,
            width, height,
            0, height
        };
        hitbox = new Polygon(vertices);
        hitbox.setPosition(x, y);

    }


    public abstract void setHitboxPosition();

    public void setPosition(float x, float y){
        entityPos.x = x;
        entityPos.y = y;
        setHitboxPosition();
    }

    public void applyKnockback(Enemy enemy, Weapon weapon) {
        float knockbackForce = weapon.getKnockbackInTiles() * GameConstants.Sprite.SIZE;

        Vector2 knockbackDirection = new Vector2(enemy.getEntityPos().x - getEntityPos().x,
            enemy.getEntityPos().y - getEntityPos().y).nor();

        Vector2 knockbacks = knockbackDirection.scl(knockbackForce);

        enemy.setPosition(enemy.getEntityPos().x + knockbacks.x, enemy.getEntityPos().y + knockbacks.y);

    }

    public TextureRegion[][] getSpriteSheet() {
        return SpriteSheet;
    }

    public void setTexture(String recourceName, int spritesheetLength, int spritesheetWidth){

        Texture entitiesTexture = new Texture(recourceName);

        SpriteSheet = new TextureRegion[spritesheetWidth][spritesheetLength];

        SpriteSheet = TextureRegion.split(entitiesTexture, GameConstants.Sprite.DEFAULT_SIZE, GameConstants.Sprite.DEFAULT_SIZE);

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

    public abstract void drawBar(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font);

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

    public GameScreen.PointF getEntityPos(){
        return entityPos;
    }

    public void addManna(float x){
        mana += x;
    }

    public float getManna(){
        return  mana;
    }

    public Polygon getHitbox(){
        return hitbox;
    }
    public void takeDamage(float x){
        hp -= x;

    }




}
