package com.gamb1t.legacyforge.Weapons;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Intersector;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.GameCharacters;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;

import java.util.ArrayList;

public class MeleeWeapon extends Weapon {
    private long lastAttackTime;
    private final long attackCooldown = 500;
    private Polygon hitbox;
    private float[] hitboxVertices;
    private float animationTimer = 0;
    private final float frameDuration = 0.1f;
    private long lastTimeDamaged = System.currentTimeMillis();

    public MeleeWeapon() {
        this.hitbox = new Polygon();
    }

    @Override
    public Polygon createHitbox(float x, float y) {
        float baseSize = GameConstants.Sprite.SIZE;
        float tipDistance = range * GameConstants.Sprite.SIZE;

        hitboxVertices = new float[] {
            -baseSize , tipDistance,
            baseSize  , tipDistance,
            0, 0
        };

        hitbox.setVertices(hitboxVertices);
        hitbox.setOrigin(0, 0);
        hitbox.setPosition(x, y);
        rotateHitbox(rotationAngle - 90);

        return hitbox;
    }

    private void rotateHitbox(float angle) {
        if (hitbox != null) {
            hitbox.setRotation(angle);
        }
    }

    @Override
    public void attack() {
        if (canAttack() && (System.currentTimeMillis() - lastAttackTime) >= attackCooldown) {
            lastAttackTime = System.currentTimeMillis();
            isAttacking = true;
        }
    }


    @Override
    public void update(float delta) {
        updateAnimation();
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {

        if (isAttacking) {

            float offsetFactor = 1.5f + (range - 1.0f) * 0.25f;
            Sprite spriteToDraw = changedSpritesheet[0][currentFrame];
            spriteToDraw.setPosition(x - offsetFactor*GameConstants.Sprite.SIZE * range, y - offsetFactor*GameConstants.Sprite.SIZE* range);
            spriteToDraw.setOrigin(spriteToDraw.getWidth() / 2, spriteToDraw.getHeight() / 2);
            spriteToDraw.setRotation(rotationAngle);
            spriteToDraw.setSize(GameConstants.Sprite.SIZE * 4 * range, GameConstants.Sprite.SIZE * 4 * range);
            spriteToDraw.draw(batch);

            if (currentFrame == changedSpritesheet[0].length - 1) {
                isAttacking = false;
                 resetAnimation();
            }
        }
    }
    public void resetAnimation(){
        currentFrame = 0;
        animationTimer = 0;
    }

    @Override
    public void updateAnimation() {
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
        }
    }

    @Override
    public <T extends GameCharacters> void checkHitboxCollisions(ArrayList<T> enemies, MapManaging currentmap) {

    }

    @Override
    public <T extends GameCharacters> void checkHitboxCollisionsEntity(ArrayList<T> enemies) {
        if (!isAttacking || hitbox == null) return;

        for (GameCharacters enemy : enemies) {
            if (enemy != null && enemy.hitbox != null) {
                if (Intersector.overlapConvexPolygons(hitbox, enemy.getHitbox())) {
                    dealDamage(enemy);
                    applyKnockback(enemy);
                }
            }
        }
    }

    @Override
    public void checkHitboxCollisionsMap(MapManaging map) {
        // TO DO
    }

    private void dealDamage(GameCharacters enemy) {
        if (enemy != null) {
            enemy.takeDamage(damage/5, (enity instanceof Player) ? ((Player) enity) :null);
        }
    }

    private void applyKnockback(GameCharacters enemy) {
        /*if (enemy != null) {
            enemy.applyKnockback(enemy, this);
        }*/
    }

    public void onJoystickRelease() {
        isAttacking = false;
    }



    public long getAttackCooldown() {
        return attackCooldown;
    }
}
