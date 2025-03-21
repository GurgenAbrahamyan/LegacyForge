package com.gamb1t.legacyforge.Weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;

import java.util.ArrayList;

public class MagicWeapon extends Weapon {
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


    private float playerCamX, getPlayerCamY;

    public MagicWeapon() {

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
            castMagic();

        }
    }
    public void resetAnimation() {
        currentFrame = 0;
    }

    public void castMagic() {
        if (isCharging) {
            isCharging = false;
            shootProjectile();
        }
    }

    private void shootProjectile() {

        if (canAttack() && (System.currentTimeMillis() - lastAttackTime) >= attackCooldown) {
            lastAttackTime = System.currentTimeMillis();
            float deltaX= (float) Math.cos(Math.toRadians(rotationAngle))*25;
            float deltaY = (float) Math.sin(Math.toRadians(rotationAngle))*25;
            projectiles.add(new Projectile(GameConstants.GET_WIDTH/2-  player.cameraX , GameConstants.GET_HEIGHT/2- player.cameraY ,deltaX, deltaY, projectilePath, currentMap));
            isAttacking = true;
        }
    }



    @Override
    public void update(float delta) {

        for (Projectile proj : projectiles) {
            proj.update();
        }


        projectiles.removeIf(Projectile::isDestroyed);

        updateAnimation();
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        if (isAttacking) {
            Sprite spriteToDraw = changedSpritesheet[0][currentFrame];
            spriteToDraw.setPosition(x - 1.5f * GameConstants.Sprite.SIZE, y - 1.5f * GameConstants.Sprite.SIZE);
            spriteToDraw.setOrigin(spriteToDraw.getWidth() / 2, spriteToDraw.getHeight() / 2);
            spriteToDraw.setRotation(rotationAngle);
            spriteToDraw.setSize(GameConstants.Sprite.SIZE * 4, GameConstants.Sprite.SIZE * 4);
            spriteToDraw.draw(batch);
            if (currentFrame == changedSpritesheet[0].length - 1) {
                isAttacking = false;
                if (joystick != null) joystick.setIsAiming(false);
                resetAnimation();
            }
        }

        for (Projectile proj : projectiles) {
            proj.draw(batch, playerCamX, getPlayerCamY);
        }


    }



    public void updateAnimation() {
        if (!isAttacking) return;

        animationTimer += Gdx.graphics.getDeltaTime();

        if (animationTimer >= frameDuration) {
            animationTimer = 0;

            if (currentFrame < changedSpritesheet[0].length - 1) {
                currentFrame++;
            } else {
                isAttacking = false;
                resetAnimation();
            }
        }
        System.out.println("Current Frame: " + currentFrame + ", isCharging: " + isCharging + ", isAttacking: " + isAttacking);

    }


    public void checkHitboxCollisions(ArrayList<Enemy> enemies, MapManaging map) {
        for (Projectile proj : projectiles) {
            if (proj != null) {
                for (Enemy enemy : enemies) {

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
        if(currentMap == null){
            currentMap = map;
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
    public void setAnimOver(boolean b){
        animOver = b;
    }
    public void setCameraValues(float x, float y){
        playerCamX = x;
        getPlayerCamY = y;

    }
}
