package com.gamb1t.legacyforge.Weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;
import com.gamb1t.legacyforge.ManagerClasses.Joystick;
import com.gamb1t.legacyforge.ManagerClasses.TouchManager;

import java.util.ArrayList;

public abstract class Weapon {
    protected String name, type, sprite;
    protected float damage, attackSpeed, range, knockbackInTiles;
    protected long lastAttackTime = System.currentTimeMillis();
    protected boolean isAttacking;
    protected float rotationAngle = 0;
    private float hitboxWidth;
    private float hitboxHeight;

    protected Texture loadedSprite;
    protected TextureRegion[][] spriteSheet;
    protected TouchManager joystick;
    protected int aniTick, aniSpeed, animationFrameAmount, currentFrame;
    protected Sprite[][] changedSpritesheet;

    public Weapon(String name, String type, float damage, float attackSpeed, float range, float knockbackInTiles) {
        this.name = name;
        this.type = type;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.range = range;
        this.knockbackInTiles = knockbackInTiles;
        this.aniSpeed = (int) (10 / attackSpeed);

    }
    public Weapon(){}


    public abstract void attack();
    public abstract void update();
    public abstract void draw(SpriteBatch batch, float x, float y);

    public void setTexture(String texturePath, int spritesheetWidth, int spritesheetLength) {
        if (loadedSprite == null) {
            loadedSprite = new Texture(texturePath);
        }
        spriteSheet = TextureRegion.split(loadedSprite, GameConstants.Sprite.DEFAULT_SIZE*2, GameConstants.Sprite.DEFAULT_SIZE*2);
        animationFrameAmount = spriteSheet[0].length;
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

    public boolean canAttack() {
        if(System.currentTimeMillis() - lastAttackTime >= (1000 / attackSpeed)){
            return true;}
        else{
            return false;
        }
    }

    private void dealDamage(Enemy enemy) {
        enemy.takeDamage(damage);
    }

    private void applyKnockback(Enemy enemy) {
        enemy.applyKnockback(enemy, this);
    }

    protected void resetAnimation() {
        aniTick = 0;
        currentFrame = 0;
    }

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
    public Rectangle createHitbox(float x, float y) {

        Rectangle hitbox =  new Rectangle(x, y, hitboxWidth, hitboxHeight);
         return  hitbox;

    }

    public void checkHitboxCollisions(Rectangle hitbox, ArrayList<Enemy> ENTITIES) {
        for (Enemy enemy : ENTITIES) {
            if (hitbox.overlaps(enemy.hitbox)) {
                dealDamage(enemy);
                applyKnockback(enemy);
            }
        }
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
    public void setAttackJoystick(TouchManager joystick) { this.joystick = joystick; }
    public void setAttacking(boolean attacking) { isAttacking = attacking; }
    public void setRotation(float angle) {
        this.rotationAngle = angle;
    }

    public float getRotation() {
        return rotationAngle;
    }

    public void onJoystickRelease() {
        if (isAttacking) {
            isAttacking = false;
        }
    }
}
