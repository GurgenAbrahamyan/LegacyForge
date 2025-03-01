package com.gamb1t.legacyforge.Weapons;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;

public class MagicWeapon extends Weapon{
    private  float manaUsage;
    public MagicWeapon(String name, float damage, float attackSpeed, float range, float knockbackInTiles, float manaUsage) {
        super(name, "magic", damage, attackSpeed, range, knockbackInTiles);
        this.manaUsage = manaUsage;
    }


    @Override
    public Polygon createHitbox(float x, float y) {
        return null;
    }

    @Override
    public void attack() {
          /// TODO
    }

    @Override
    public void update() {

    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {

    }
}
