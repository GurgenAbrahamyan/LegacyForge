package com.gamb1t.legacyforge.Weapons;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.gamb1t.legacyforge.Entity.GameCharacters;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.Entity.Enemy;

import java.util.ArrayList;

public class RangedWeapon extends Weapon {
    private long lastAttackTime;
    private final long attackCooldown = 500;
    private boolean isCharging = false;
    private boolean animOver = false;
    private float chargeTime = 0;

    private String projectilePath;
    private final float maxChargeTime = 1.5f;
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private float animationTimer = 0;
    private float frameDuration = 0.1f;

    MapManaging currentMap;

    float minSpeed = 10;
    float maxSpeed = 50;

    float chargePercentage = Math.min(chargeTime / maxChargeTime, 1.0f);
    float arrowSpeed = minSpeed + chargePercentage * (maxSpeed - minSpeed);



    float minDamage = damage/5;
    float maxDamage = damage;
    float projectileDamage = minDamage + chargePercentage * (maxDamage - minDamage);




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



        float deltaX= (float) Math.cos(Math.toRadians(rotationAngle))*maxSpeed;
        float deltaY = (float) Math.sin(Math.toRadians(rotationAngle))*maxSpeed;

        projectiles.add(new Projectile(enity.getEntityPos().x , enity.getEntityPos().y,deltaX, deltaY, projectilePath, currentMap));

    }



    @Override
    public void update(float delta) {

        for (Projectile proj : projectiles) {
            if(proj != null){
            proj.update();}
        }


        projectiles.removeIf(Projectile::isDestroyed);

        updateAnimation();
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        if (isAiming) {
            Sprite spriteToDraw = changedSpritesheet[0][currentFrame];
            spriteToDraw.setPosition(x - 1.5f * GameConstants.Sprite.SIZE, y - 1.5f * GameConstants.Sprite.SIZE);
            spriteToDraw.setOrigin(spriteToDraw.getWidth() / 2, spriteToDraw.getHeight() / 2);
            spriteToDraw.setRotation(rotationAngle);
            spriteToDraw.setSize(GameConstants.Sprite.SIZE * 4, GameConstants.Sprite.SIZE * 4);
            spriteToDraw.draw(batch);
        }

        for (Projectile proj : projectiles) {
            proj.draw(batch, playerCamX, playerCamY);
        }
    }

    public void updateCharge() {

        chargeTime += Gdx.graphics.getDeltaTime();
        if (chargeTime > maxChargeTime) {
            chargeTime = maxChargeTime;
        }


        float chargePercentage = Math.min(chargeTime / maxChargeTime, 1.0f);

        currentFrame = (int) (chargePercentage * (changedSpritesheet[0].length - 1));

        float stretchFactor = 1.0f + chargePercentage * 0.5f;
        changedSpritesheet[0][currentFrame].setScale(stretchFactor);

        System.out.println("Charging: " + chargeTime + "s, Charge Percentage: " + chargePercentage);

    }


    public void updateAnimation() {
        frameDuration = attackSpeed/5 ;

        if (!animOver) return;

        animationTimer += Gdx.graphics.getDeltaTime();

        if (animationTimer > frameDuration) {
            animationTimer = 0;
            if (currentFrame < changedSpritesheet[0].length - 1) {
                currentFrame++;
            } else if(currentFrame == changedSpritesheet[0].length-1) {
                if(isAiming){
                    currentFrame = changedSpritesheet[0].length-1;}
                else{
                    resetAnimation();

                    animOver=false;

                }
            }

        }
        System.out.println("Current Frame: " + currentFrame + ", isCharging: " + isCharging + ", isAttacking: " + isAttacking);

    }


    public <T extends GameCharacters> void checkHitboxCollisions(ArrayList<T> enemies, MapManaging map) {
        if(currentMap == null){
            currentMap = map;
        }
        for (Projectile proj : projectiles) {
            if (proj != null) {
                for (GameCharacters enemy : enemies) {

                    if (enemy != null && proj.getHitbox() != null && enemy.hitbox != null) {
                        if (Intersector.overlapConvexPolygons(proj.getHitbox(), enemy.getHitbox())) {
                            dealDamage(enemy);
                            applyKnockback(enemy);
                            proj.setDestroyed(true);
                            System.out.println("colided");
                        }
                        currentMap = map;

                    } else if (currentMap.checkNearbyWallCollision(proj.getHitbox(),  proj.getHitbox().getX() + proj.getVelocity().x, proj.getHitbox().getY() + proj.getVelocity().y)) {
                        proj.setDestroyed(true);
                    }
                }
            }
        }

    }



    private void dealDamage(GameCharacters enemy) {
        if (enemy != null) {
            enemy.takeDamage(damage);
        }
    }

    private void applyKnockback(GameCharacters enemy) {
      /*  if (enemy != null) {
            enemy.applyKnockback(enemy, this);
        */
    }
    public void setIsCharging(boolean x){
        isCharging= x;
    }


    public ArrayList<Projectile> getProjectiles() {
        return projectiles;
    }
    public void setAnimOver(boolean b){
        animOver = b;
    }


    public void setMap (MapManaging map){
        currentMap = map;
    }

    public boolean getAiming(){
        return  isAiming;
    }

}
