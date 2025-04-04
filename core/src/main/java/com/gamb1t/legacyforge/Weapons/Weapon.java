package com.gamb1t.legacyforge.Weapons;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.GameCharacters;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;

import java.util.ArrayList;

public abstract class Weapon {
    protected String name, type, sprite;
    protected float damage, attackSpeed, range, knockbackInTiles;
    protected long lastAttackTime = System.currentTimeMillis();
    protected boolean isAttacking;
    protected boolean isAiming;
    protected float rotationAngle = 0;
    protected  int price;

    @JsonIgnore
    protected float playerCamX, playerCamY;

    @JsonIgnore
    protected Texture loadedSprite;
    @JsonIgnore
    protected TextureRegion[][] spriteSheet;

    protected int aniTick, aniSpeed, animationFrameAmount, currentFrame;
    @JsonIgnore
    protected Sprite[][] changedSpritesheet;
    protected GameCharacters enity;

    public Weapon(){
        this.aniSpeed = (int) (10 / attackSpeed);
    }
    public abstract Polygon createHitbox(float x, float y);




    public abstract void attack();
    public abstract void update(float delta);
    public abstract void draw(SpriteBatch batch, float x, float y);

    public void setTexture(String texturePath) {
        if (loadedSprite == null) {
            loadedSprite = new Texture(texturePath);
        }
        spriteSheet = TextureRegion.split(loadedSprite, GameConstants.Sprite.DEFAULT_SIZE*3, GameConstants.Sprite.DEFAULT_SIZE*3);

        System.out.println("Rows: " + spriteSheet.length + ", Columns: " + spriteSheet[0].length);
        animationFrameAmount = spriteSheet[0].length;
    }

    public void updateAnimation() {

    }

    public boolean canAttack() {
        if(System.currentTimeMillis() - lastAttackTime >= (1000 / attackSpeed)){
            lastAttackTime = System.currentTimeMillis();
            return true;}
        else{
            return false;
        }
    }


    private void applyKnockback(Enemy enemy) {
        enemy.applyKnockback(enemy, this);
    }
     public void resetAnimation(){}

    public void convertTxtRegToSprite() {
        if (spriteSheet != null) {
            changedSpritesheet = new Sprite[spriteSheet.length][spriteSheet[0].length];
            for (int x = 0; x < spriteSheet.length; x++) {
                for (int y = 0; y < spriteSheet[0].length; y++) {
                    changedSpritesheet[x][y] = new Sprite(spriteSheet[x][y]);
                }
            }
        }
    }

    public <T extends GameCharacters> void checkHitboxCollisions(ArrayList<T> ENEMIES, MapManaging currentMap) {

    }



    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }
    public float getAttackSpeed() { return attackSpeed; }
    public void setAttackSpeed(float attackSpeed) { this.attackSpeed = attackSpeed; }
    public String getType() { return type; }
    public float getRange() { return range; }
    public boolean getAttacking(){return  isAttacking;}
    public void setRange(float range) { this.range = range; }
    public float getKnockbackInTiles() { return knockbackInTiles; }
    public void setKnockbackInTiles(float knockbackInTiles) { this.knockbackInTiles = knockbackInTiles; }
    public String getSprite() { return sprite; }
    public void setSprite(String sprite) { this.sprite = sprite; }
    //public void setAttackJoystick(TouchManager joystick) { this.joystick = joystick; }
    public void setAttacking(boolean attacking) { isAttacking = attacking; }
    public void setRotation(float angle) {
        this.rotationAngle = angle;
    }
    public void setAiming(boolean b){
        isAiming = b;
    }
    public boolean getAiming(){
        return  isAiming;
    }

    public float getRotation() {
        return rotationAngle;
    }

    public void onJoystickRelease() {
        if (isAttacking) {
            isAttacking = false;
        }
    }

    public int getPrice() {
        return  price;
    }
    public  void setPrice(int i){
        price = i;
    }

    public void setCameraValues(float x, float y){
        playerCamX = x;
        playerCamY = y;

    }
    @JsonIgnore
    public <T extends GameCharacters> void setEntity(T enity){
        this.enity = enity;
    }
}
