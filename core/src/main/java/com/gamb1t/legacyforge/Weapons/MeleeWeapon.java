package com.gamb1t.legacyforge.Weapons;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;

import java.util.ArrayList;

import javax.swing.text.html.parser.Entity;

public class MeleeWeapon extends Weapon {

    private long lastAttackTime;
    private final long attackCooldown = 500;


    public MeleeWeapon() {
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
        if (isAttacking) {
            updateAnimation();
        }
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        if (isAttacking) {
            Sprite spriteToDraw = changedSpritesheet[0][currentFrame];
            spriteToDraw.setPosition(x - GameConstants.Sprite.SIZE, y - GameConstants.Sprite.SIZE);
            spriteToDraw.setOrigin(spriteToDraw.getWidth() / 2, spriteToDraw.getHeight() / 2);
            spriteToDraw.setRotation(rotationAngle);
            spriteToDraw.setSize(GameConstants.Sprite.SIZE * 3, GameConstants.Sprite.SIZE * 3);
            spriteToDraw.draw(batch);

            if (currentFrame == changedSpritesheet[0].length - 1) {
                isAttacking = false;
                joystick.setIsAiming(false);
            }
        }
    }

    @Override
    public void updateAnimation() {
        if (isAttacking) {
            if (currentFrame < changedSpritesheet[0].length - 1) {
                currentFrame++;
            }
        }
    }


    private void dealDamage(Enemy enemy) {
        enemy.takeDamage(damage);
    }

    private void applyKnockback(Enemy enemy) {
        enemy.applyKnockback(enemy, this);
    }

    public void onJoystickRelease() {
        if (isAttacking) {
            isAttacking = false;
        }
    }

    public long getAttackCooldown() {
        return attackCooldown;
    }
}
