package com.gamb1t.legacyforge.Weapons;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;

import java.util.ArrayList;

public class RangedWeapon extends Weapon {
    private long lastAttackTime;
    private final long attackCooldown = 500;
    private boolean isCharging = false;
    private float chargeTime = 0;

    private String projectilePath;
    private final float maxChargeTime = 1.5f;
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private float animationTimer = 0;
    private float frameDuration = 0.1f;
    private float percentage =0;

    public RangedWeapon() {

    }





    @JsonSetter("projectilePath")
    public void setProjectiles(String s) {

        projectilePath = s;
    }

    @Override
    public Polygon createHitbox(float x, float y) {

        return  null;
    }

    @Override
    public void attack() {
        resetAnimation();
        if (isCharging) return;
        if (canAttack() && (System.currentTimeMillis() - lastAttackTime) >= attackCooldown) {
            lastAttackTime = System.currentTimeMillis();
            isCharging = true;
            chargeTime = 0;
            releaseBow();
        }
    }
    public void resetAnimation() {
       currentFrame = 0;
    }

    public void releaseBow() {
        if (isCharging) {
            isCharging = false;
            shootArrow();
        }
    }

    private void shootArrow() {
        float chargePercentage = chargeTime/maxChargeTime;

        float arrowSpeed = 50;
        Vector2 direction = new Vector2((float) Math.cos(Math.toRadians(rotationAngle)), (float) Math.sin(Math.toRadians(rotationAngle)));

        projectiles.add(new Projectile(GameConstants.GET_WIDTH/2-  player.cameraX , GameConstants.GET_HEIGHT/2- player.cameraY , direction, arrowSpeed, projectilePath));

    }



    @Override
    public void update() {
        if (isCharging) {
            chargeTime += Gdx.graphics.getDeltaTime();
        }

        for (Projectile proj : projectiles) {
            proj.update();
        }

        projectiles.removeIf(Projectile::isDestroyed);

        updateAnimation();
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        if (joystick.getIsAiming()) {
            Sprite spriteToDraw = changedSpritesheet[0][currentFrame];
            spriteToDraw.setPosition(x - 1.5f * GameConstants.Sprite.SIZE, y - 1.5f * GameConstants.Sprite.SIZE);
            spriteToDraw.setOrigin(spriteToDraw.getWidth() / 2, spriteToDraw.getHeight() / 2);
            spriteToDraw.setRotation(rotationAngle);
            spriteToDraw.setSize(GameConstants.Sprite.SIZE * 4, GameConstants.Sprite.SIZE * 4);
            spriteToDraw.draw(batch);
        }

        for (Projectile proj : projectiles) {
            proj.draw(batch, x, y  );
        }
    }

    public void updateAnimation() {
        frameDuration = attackSpeed/5 ;
        if (!isAttacking) return;

        animationTimer += Gdx.graphics.getDeltaTime();

        if (animationTimer > frameDuration) {
            animationTimer = 0;
            if (currentFrame < changedSpritesheet[0].length - 1) {
                currentFrame++;
            } else if(currentFrame == changedSpritesheet[0].length-1) {
                if(joystick.getIsAiming()){
                currentFrame = changedSpritesheet[0].length-1;}
                else{
                    currentFrame=0;
                }
            }

        }
        System.out.println("Current Frame: " + currentFrame + ", isCharging: " + isCharging + ", isAttacking: " + isAttacking);

    }

    public void checkHitboxCollisions(ArrayList<Enemy> enemies) {
        for (Projectile proj : projectiles) {
            if (proj != null) {
                for (Enemy enemy : enemies) {
                    Rectangle rect = enemy.hitbox;
                    Polygon enemyPoly = new Polygon(new float[]{
                        rect.x, rect.y,
                        rect.x + rect.width, rect.y,
                        rect.x + rect.width, rect.y + rect.height,
                        rect.x, rect.y + rect.height
                    });

                    if (enemy != null && proj.getHitbox() != null && enemy.hitbox != null) {
                        if (Intersector.overlapConvexPolygons(proj.getHitbox(), enemyPoly)) {
                            dealDamage(enemy);
                            applyKnockback(enemy);
                            proj.setDestroyed(true);  // Destroy the projectile after hit
                        }
                    }
                }
            }
        }
    }

    private void dealDamage(Enemy enemy) {
        if (enemy != null) {
            enemy.takeDamage(damage);
        }
    }

    private void applyKnockback(Enemy enemy) {
        if (enemy != null) {
            enemy.applyKnockback(enemy, this);
        }
    }
    public void setIsCharging(boolean x){
        isCharging= x;
    }


    public ArrayList<Projectile> getProjectiles() {
        return projectiles;
    }

}

