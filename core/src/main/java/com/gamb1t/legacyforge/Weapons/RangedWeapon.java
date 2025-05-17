package com.gamb1t.legacyforge.Weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.gamb1t.legacyforge.Entity.GameCharacters;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Networking.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RangedWeapon extends Weapon {
    private long lastAttackTime;
    private final long attackCooldown = 500;
    private boolean isCharging = false;
    private boolean animOver = false;
    private float chargeTime = 0;
    public int projId = 0;

    private String projectilePath;
    private Texture projectileTexture;
    private final float maxChargeTime = 1.5f;
    private List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    private float animationTimer = 0;
    private float frameDuration = 0.1f;

    private float delta;

    MapManaging currentMap;

    float minSpeed = 10;
    float maxSpeed = GameConstants.Sprite.SIZE/4;

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

    public void attackNoProj() {
        if (isCharging) return;
        if (canAttack() && (System.currentTimeMillis() - lastAttackTime) >= attackCooldown) {
            lastAttackTime = System.currentTimeMillis();
            isCharging = true;
            chargeTime = 0;

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

    public void removeById(Network.DestroyProjectileMessage message){
        for(Projectile projectile : projectiles){
            if(projectile.getId() == message.projectileId){
                projectiles.remove(projectile);
            }
        }
    }

    private void shootArrow() {



        float deltaX= (float) Math.cos(Math.toRadians(rotationAngle))*maxSpeed;
        float deltaY = (float) Math.sin(Math.toRadians(rotationAngle))*maxSpeed;

        projectiles.add(new Projectile(enity.getEntityPos().x , enity.getEntityPos().y,deltaX, deltaY, projectileTexture, currentMap, isClient, projId));

        if(server != null){
            Network.CreateProjectileMessage proj = new Network.CreateProjectileMessage();
            proj.dx = deltaX/maxSpeed;
            proj.dy = deltaY/maxSpeed;
            proj.x = enity.getEntityPos().x/GameConstants.Sprite.SIZE;
            proj.y = enity.getEntityPos().y/GameConstants.Sprite.SIZE;
            proj.enemyId = enity.getID();
            proj.projectileId = projId;
            proj.isEnemy = enity instanceof Enemy;
            server.sendToAllTCP(proj);
        }
        projId++;
    }
    public void shootArrow(Network.CreateProjectileMessage projectileMessage){

        projectiles.add(new Projectile(projectileMessage.x*GameConstants.Sprite.SIZE, projectileMessage.y*GameConstants.Sprite.SIZE, projectileMessage.dx*maxSpeed, projectileMessage.dy*maxSpeed, projectileTexture, currentMap, isClient, projectileMessage.projectileId));
    }

    public void initProj(){
        projectileTexture = new Texture(projectilePath);
    }



    @Override
    public void update(float delta) {

        this.delta = delta;

        for (Projectile proj : projectiles) {
            if(proj != null){
            proj.update();
            if(server != null){
                System.out.println(enity.getID());
                server.sendToAllTCP(new Network.SetProjectilePositionMessage(proj.getId(), enity.getID(), proj.getPositionHitbox().x/GameConstants.Sprite.SIZE, proj.getPositionHitbox().y/GameConstants.Sprite.SIZE, enity instanceof Enemy));
            }
            if(proj.isDestroyed()){

                if(server != null){
                    Network.DestroyProjectileMessage projectileMessage = new Network.DestroyProjectileMessage();
                    projectileMessage.enemyId = enity.getID();
                    projectileMessage.projectileId= proj.getId();
                    projectileMessage.isEnemy = enity instanceof Enemy;
                    server.sendToAllTCP(projectileMessage);
                }

                projectiles.remove(proj);

            }

            }
        }






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



    public void updateAnimation() {
        frameDuration = attackSpeed/5 ;

        if (!animOver) return;

        animationTimer += Gdx.graphics.getDeltaTime();

        if (animationTimer > frameDuration) {
            animationTimer = 0;
            if (currentFrame < frameAmountX - 1) {
                currentFrame++;
            } else if(currentFrame == frameAmountX-1) {
                if(isAiming){
                    currentFrame = frameAmountX-1;}
                else{
                    resetAnimation();

                    animOver=false;

                }
            }

        }
       // System.out.println("Current Frame: " + currentFrame + ", isCharging: " + isCharging + ", isAttacking: " + isAttacking);

    }

    @Override
    public <T extends GameCharacters> void checkHitboxCollisionsEntity(ArrayList<T> enemies) {

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

                    }
                }
            }
        }

    }

    @Override
    public void checkHitboxCollisionsMap(MapManaging map) {
        if(currentMap == null){
            currentMap = map;
        }
        for (Projectile proj : projectiles) {
            if (proj != null) {

                        currentMap = map;
                        if (currentMap.checkNearbyWallCollision(proj.getHitbox(),  proj.getHitbox().getX() + proj.getVelocity().x, proj.getHitbox().getY() + proj.getVelocity().y)) {
                        proj.setDestroyed(true);
                    }
                }
        }

    }




    private void dealDamage(GameCharacters enemy) {
        if (enemy != null) {
            enemy.takeDamage(damage, enity instanceof Player ? (Player) enity : enity);
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


    public List<Projectile> getProjectiles() {
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
