package com.gamb1t.legacyforge.Entities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import com.gamb1t.legacyforge.MainActivity;
import com.gamb1t.legacyforge.R;

public class Entity {


    private Bitmap spriteSheet;
    private Bitmap[][] sprites = new Bitmap[7][4];
    private BitmapFactory.Options options = new BitmapFactory.Options();
    public int entityX;
    public int entityY;

    Entity(int resID) {
        options.inScaled = false;
        spriteSheet = BitmapFactory.decodeResource(MainActivity.getGameContext().getResources(), resID, options);
        for (int j = 0; j < sprites.length; j++)
            for (int i = 0; i < sprites[j].length; i++)
                sprites[j][i] = getScaledBitmap(Bitmap.createBitmap(spriteSheet, 16 * i, 16 * j, 16, 16));
    }

    public Bitmap getSpriteSheet() {
        return spriteSheet;
    }

    public Bitmap getSprite(int yPos, int xPos) {
        return sprites[yPos][xPos];
    }

    private Bitmap getScaledBitmap(Bitmap bitmap){
        return Bitmap.createScaledBitmap(bitmap,bitmap.getWidth() * 6,bitmap.getHeight()*6,false);
    }

}
