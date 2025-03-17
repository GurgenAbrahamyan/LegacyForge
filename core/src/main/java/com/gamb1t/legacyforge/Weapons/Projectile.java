package com.gamb1t.legacyforge.Weapons;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Polygon;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;

public class Projectile {
    private Vector2 positionHitbox;
    private Vector2 positionSprite;
    private Vector2 velocity;
    private boolean destroyed = false;
    private Sprite sprite;
    private Polygon hitbox;
    private float damage;

    public Projectile(float x, float y, Vector2 direction, float speed, String proj) {
        this.positionHitbox = new Vector2(x, y);
        this.positionSprite = new Vector2(x - GameConstants.Sprite.SIZE/2, y - GameConstants.Sprite.SIZE/2);
        this.velocity = direction.scl(speed);
        this.sprite= new Sprite(new Texture(proj));
        this.sprite.setSize(GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE);
        this.hitbox = new Polygon(new float[] {
            0, 0,
            GameConstants.Sprite.SIZE*2/3, 0,
            GameConstants.Sprite.SIZE*2/3, GameConstants.Sprite.SIZE/4,
            0, GameConstants.Sprite.SIZE/4,
        });
        sprite.setPosition(positionSprite.x, positionSprite.y);
        hitbox.setPosition(positionHitbox.x, positionHitbox.y);
    }


    public void update() {
        if (!destroyed) {
            positionHitbox.add(velocity);
            positionSprite.add(velocity);
            sprite.setPosition(positionSprite.x, positionSprite.y);
            hitbox.setPosition(positionHitbox.x, positionHitbox.y);
        }
    }

    public void draw(SpriteBatch batch, float camX, float camY) {
        if (!destroyed) {
            sprite.setPosition(positionSprite.x+camX, positionSprite.y+camY);
            sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
            sprite.setRotation(velocity.angleDeg());
            hitbox.setRotation(velocity.angleDeg());;
            sprite.draw(batch);
        }
    }



    public Vector2 getPositionHitbox() {
        return positionHitbox;
    }

    public Polygon getHitbox() {
        return hitbox;
    }

    public boolean isDestroyed() {
        return destroyed;
    }


    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }


}
