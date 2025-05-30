package com.gamb1t.legacyforge.Weapons;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Equipment {



    private Armor helmet;
    private Armor chestplate;

    public void equipArmor(Armor armor) {
        if (armor.getType().equals("helmet")) {
            helmet = armor;
        } else if (armor.getType().equals("chestplate")) {
            chestplate = armor;
        }
    }

    public void draw(SpriteBatch batch, float x, float y, float width, float height, int faceDir, int aniIndex) {
        if (chestplate != null) {
            chestplate.draw(batch, x, y, width, height, faceDir, aniIndex);
        }
        if (helmet != null) {
            helmet.draw(batch, x, y, width, height, faceDir, aniIndex);
        }
    }

    public Armor getHelmet() {
        return helmet;
    }

    public Armor getChestplate() {
        return chestplate;
    }
}

