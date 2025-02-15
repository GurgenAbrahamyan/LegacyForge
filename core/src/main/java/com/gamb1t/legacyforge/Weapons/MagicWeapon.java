package com.gamb1t.legacyforge.Weapons;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;

public class MagicWeapon extends Weapon{
    private  float manaUsage;
    public MagicWeapon(String name, float damage, float attackSpeed, float range, float knockbackInTiles, float manaUsage) {
        super(name, "magic", damage, attackSpeed, range, knockbackInTiles);
        this.manaUsage = manaUsage;
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
