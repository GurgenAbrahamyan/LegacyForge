package com.gamb1t.legacyforge.Weapons;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;

public class RangedWeapon extends Weapon{

    public RangedWeapon(String name, float damage, float attackSpeed, float range, float knockbackInTiles) {
        super(name, "ranged", damage, attackSpeed, range, knockbackInTiles);
    }


    @Override
    public Polygon createHitbox(float x, float y) {
        return null;
    }

    @Override
    public void attack() {
      //todo
    }

    @Override
    public void update() {

    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {

    }
}
