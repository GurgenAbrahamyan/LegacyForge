package com.gamb1t.legacyforge.Weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.GameCharacters;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.Networking.ConnectionManager;
import com.gamb1t.legacyforge.Networking.Network;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MagicWeapon extends Weapon {


    private long lastAttackTime;
    private final long attackCooldown = 500;
    private boolean animOver;
    private float delta;
    private float mannaUsage;

    @JsonIgnore
    private Texture projectile;
    private String projectilePath;
    private List<Projectile> projectiles = new CopyOnWriteArrayList<>();

    private MapManaging currentMap;

    private  int projId = 0;
    private float animationTimer = 0;
    private float frameDuration = 0.1f;


    public MagicWeapon() {
    }

    @JsonSetter("projectilePath")
    public void setProjectiles(String s) {
        projectilePath = s;
    }

    @JsonSetter("mannaUsage")
    public void setMannaUsage(int x) {
        mannaUsage = x;
    }

    @Override
    public Polygon createHitbox(float x, float y) {
        return null;
    }

    @Override
    public void attack() {
        if (canAttack() && (System.currentTimeMillis() - lastAttackTime) >= attackCooldown) {
            if(enity.getManna() >= mannaUsage){
               enity.addManna(-mannaUsage);
            lastAttackTime = System.currentTimeMillis();
            isAttacking = true;
            shootProjectile();}
        }
    }

    public void attackNoProj() {
        if (canAttack() && (System.currentTimeMillis() - lastAttackTime) >= attackCooldown) {
            if(enity.getManna() >= mannaUsage) {
                enity.addManna(-mannaUsage);
                lastAttackTime = System.currentTimeMillis();
                isAttacking = true;
            }
        }
    }

    private void shootProjectile() {

        float deltaX = (float) Math.cos(Math.toRadians(rotationAngle)) * GameConstants.Sprite.SIZE/10;
        float deltaY = (float) Math.sin(Math.toRadians(rotationAngle)) * GameConstants.Sprite.SIZE/10;

        projectiles.add(new Projectile(enity.getEntityPos().x, enity.getEntityPos().y, deltaX, deltaY, projectile, currentMap, isClient,projId ));
        if(server != null){
            boolean b = enity instanceof Player;

            System.out.println("SENDING PROJECTILE FROM PLAYER:" + b );
            Network.CreateProjectileMessage proj = new Network.CreateProjectileMessage();
            proj.dx = deltaX/GameConstants.Sprite.SIZE/10;
            proj.dy = deltaY/GameConstants.Sprite.SIZE/10;
            proj.x = enity.getEntityPos().x/GameConstants.Sprite.SIZE;
            proj.y = enity.getEntityPos().y/GameConstants.Sprite.SIZE;
            proj.enemyId = enity.getID();
            proj.projectileId = projId;
            proj.isEnemy = enity instanceof Enemy;
            ConnectionManager.sendToConnections(roomName, roomId, proj);
        }
        projId ++;
    }


    @JsonIgnore
    public void setProj(Texture proj){
        this.projectile =proj;
    }

    @JsonIgnore
    public Texture getProjectileTexture(){
        return  projectile;
    }

    public void shootProjectile(Network.CreateProjectileMessage projectileMessage){

        projectiles.add(new Projectile(projectileMessage.x*GameConstants.Sprite.SIZE,
            projectileMessage.y*GameConstants.Sprite.SIZE,
            projectileMessage.dx* GameConstants.Sprite.SIZE/10,
            projectileMessage.dy *  GameConstants.Sprite.SIZE/10, projectile, currentMap, isClient, projectileMessage.projectileId));


    }

    @Override
    public void update(float delta) {
        this.delta = delta;

        for (Projectile proj : projectiles) {
            proj.update();
            if(server != null){
                System.out.println(enity.getID() + " this is magic");
                 ConnectionManager.sendToConnections(roomName, roomId, new Network.SetProjectilePositionMessage(proj.getId(), enity.getID(), proj.getPositionHitbox().x/GameConstants.Sprite.SIZE, proj.getPositionHitbox().y/GameConstants.Sprite.SIZE, enity instanceof Enemy));
            }
            if(proj.isDestroyed()){
                projectiles.remove(proj);
                if (server != null) {
                    Network.DestroyProjectileMessage projectileMessage = new Network.DestroyProjectileMessage();
                    projectileMessage.enemyId = enity.getID();
                    projectileMessage.projectileId = proj.getId();
                    projectileMessage.isEnemy = enity instanceof Enemy;
                    ConnectionManager.sendToConnections(roomName, roomId, projectileMessage);
                }
            }
        }



        updateAnimation();
    }


    public void removeById(Network.DestroyProjectileMessage message) {
        for (int i = 0; i < projectiles.size(); i++) {
            if (projectiles.get(i).getId() == message.projectileId) {
                projectiles.remove(i);
                break;
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        if (isAttacking) {



            Sprite spriteToDraw = changedSpritesheet[0][currentFrame];
            spriteToDraw.setPosition(x - 1.5f * GameConstants.Sprite.SIZE, y - 2f * GameConstants.Sprite.SIZE);
            spriteToDraw.setOrigin(spriteToDraw.getWidth() / 2, spriteToDraw.getHeight() / 2);
            spriteToDraw.setRotation(rotationAngle);
            spriteToDraw.setSize(GameConstants.Sprite.SIZE * 4, GameConstants.Sprite.SIZE * 4);
            spriteToDraw.draw(batch);

        }

            for (Projectile proj : projectiles) {
                proj.draw(batch, playerCamX, playerCamY);

            }


    }
    public void resetAnimation(){

        currentFrame = 0;
    }
    public void updateAnimation() {
        frameDuration = attackSpeed/10 ;

        if (!isAttacking) return;

        animationTimer += Gdx.graphics.getDeltaTime();

        if (animationTimer >= frameDuration) {
            animationTimer = 0;

            if (currentFrame < frameAmountX - 1) {
                currentFrame++;
            } else {
                isAttacking = false;
                resetAnimation();
            }
        }        System.out.println("Current Frame: " + currentFrame + ", isAttacking: " + isAttacking);
    }


    public void setCurrentMap(MapManaging map) {
        currentMap = map;
    }



    @Override
    public <T extends GameCharacters> void checkHitboxCollisionsEntity(List<T> enemies) {
        for (Projectile proj : projectiles) {
            if (proj != null) {
                for (GameCharacters enemy : enemies) {
                    if (enemy != null && proj.getHitbox() != null && enemy.hitbox != null) {
                        if (Intersector.overlapConvexPolygons(proj.getHitbox(), enemy.getHitbox())) {
                            if(enemy.getIsAlive()){
                                if(enemy != enity){
                            dealDamage(enemy);
                            applyKnockback(enemy);
                            proj.setDestroyed(true);
                            System.out.println("Projectile collided with enemy");}
                            }
                        }
                    }
                }


            }
        }
    }

    @JsonIgnore
    public void initProj(){
        projectile = new Texture(projectilePath);
    }

    @Override
    public void checkHitboxCollisionsMap(MapManaging map) {
        for(Projectile proj : projectiles){
            if (currentMap.checkNearbyWallCollision(proj.getHitbox(), proj.getHitbox().getX() + proj.getVelocity().x, proj.getHitbox().getY() + proj.getVelocity().y)) {
                proj.setDestroyed(true);
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
        }*/
    }



    @JsonIgnore
    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public void setAnimOver(boolean b){
        animOver = b;
    }


}
