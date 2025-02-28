package com.gamb1t.legacyforge.Weapons;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;

import java.util.ArrayList;

public class MeleeWeapon extends Weapon {
    private long lastAttackTime;
    private final long attackCooldown = 500;
    private Polygon hitbox;
    private float[] hitboxVertices;

    public MeleeWeapon() {
        this.hitbox = new Polygon();
    }
    @Override
    public Polygon createHitbox(float x, float y) {
        float baseSize = 2*GameConstants.Sprite.SIZE;
        float tipDistance = range * GameConstants.Sprite.SIZE;


        hitboxVertices = new float[]{
            -baseSize / 2, tipDistance,
            baseSize / 2, tipDistance,
            0, 0,
        };

        hitbox.setVertices(hitboxVertices);
       hitbox.setOrigin(0,0);

        hitbox.setPosition(x , y);
        rotateHitbox(rotationAngle-90);

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
            resetAnimation();
        }
    }

    @Override
    public void update() {
        updateAnimation( );

        if (isAttacking && joystick != null) {
            float angle = joystick.getRotation();
            setRotation(angle);
        }
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
            }
        }
    }

    @Override
    public void updateAnimation() {
        if (isAttacking && currentFrame < changedSpritesheet[0].length - 1) {
            currentFrame++;
        }
    }



    @Override
    public void checkHitboxCollisions(ArrayList<Enemy> enemies) {
        if (!isAttacking || hitbox == null) return;

        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.hitbox != null) {
                Rectangle rect = enemy.hitbox;
                Polygon enemyPoly = new Polygon(new float[]{
                    rect.x, rect.y,
                    rect.x + rect.width, rect.y,
                    rect.x + rect.width, rect.y + rect.height,
                    rect.x, rect.y + rect.height
                });

                if (Intersector.overlapConvexPolygons(hitbox, enemyPoly)) {
                    dealDamage(enemy);
                    applyKnockback(enemy);
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

    public void onJoystickRelease() {
        isAttacking = false;
    }

    public long getAttackCooldown() {
        return attackCooldown;
    }
}
