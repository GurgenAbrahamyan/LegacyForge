package com.gamb1t.legacyforge.Weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Polygon;
import com.esotericsoftware.kryonet.Server;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;

public class Projectile {
    private Vector2 positionHitbox;
    private Vector2 positionSprite;
    private float deltaX, deltaY;
    private boolean destroyed = false;
    private Sprite sprite;
    private Polygon hitbox;
    private Vector2 velocity;
    private MapManaging map;
    private boolean isClient;
    private Server server;
    private int id;

    public Projectile(float x, float y, float deltaX, float deltaY, Texture proj, MapManaging map, boolean isClient, int id) {
        this.positionHitbox = new Vector2(x, y);
        this.positionSprite = new Vector2(x - GameConstants.Sprite.SIZE / 2, y - GameConstants.Sprite.SIZE / 2);
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.velocity = new Vector2(deltaX, deltaY);
        this.isClient = isClient;

        System.out.println(isClient);
        System.out.println(proj);
        if(isClient){
        this.sprite = new Sprite(proj);
        this.sprite.setSize(GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE);
        sprite.setPosition(positionSprite.x, positionSprite.y);}

        this.hitbox = new Polygon(new float[]{
            0, 0,
            GameConstants.Sprite.SIZE * 2 / 3, 0,
            GameConstants.Sprite.SIZE * 2 / 3, GameConstants.Sprite.SIZE / 4,
            0, GameConstants.Sprite.SIZE / 4,
        });

        hitbox.setPosition(positionHitbox.x, positionHitbox.y);
       this.map = map;
       this.id = id;
    }



    public void update() {
        if (!destroyed) {
            float nextX = positionHitbox.x + deltaX;
            float nextY = positionHitbox.y + deltaY;

            if (map.checkNearbyWallCollision(hitbox, nextX, nextY)) {
                destroyed = true;
                return;
            }

            positionHitbox.set(nextX, nextY);
            if(isClient){
            positionSprite.set(positionSprite.x+deltaX, positionSprite.y+deltaY);
            sprite.setPosition(positionSprite.x, positionSprite.y);}

            hitbox.setPosition(positionHitbox.x, positionHitbox.y);

        }
    }
    public void setPosition(float x, float y){
        positionHitbox.set(x, y);
        if(isClient){
            positionSprite.set(x, y);
            sprite.setPosition(positionSprite.x, positionSprite.y);}

        hitbox.setPosition(positionHitbox.x, positionHitbox.y);
    }



    public void draw(SpriteBatch batch, float camX, float camY) {
        if (!destroyed) {
            sprite.setPosition(positionSprite.x + camX, positionSprite.y + camY);
            sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
            sprite.setRotation(velocity.angleDeg());
            hitbox.setRotation(velocity.angleDeg());
            sprite.draw(batch);
        }
    }
    public void drawMultiplayer(SpriteBatch batch, float camX, float camY) {
        if (!destroyed) {
            sprite.setPosition(positionSprite.x + camX-GameConstants.Sprite.SIZE/2, positionSprite.y + camY-GameConstants.Sprite.SIZE/2);
            sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
            sprite.setRotation(velocity.angleDeg());
            hitbox.setRotation(velocity.angleDeg());
            sprite.draw(batch);
        }
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public Vector2 getPositionHitbox() {
        return positionHitbox;
    }

    public Polygon getHitbox() {
        return hitbox;
    }
    public int getId(){
        return  id;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
}
