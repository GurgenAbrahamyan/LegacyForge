package com.gamb1t.legacyforge.enviorment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.gamb1t.legacyforge.MainActivity;
import com.gamb1t.legacyforge.R;
import com.gamb1t.legacyforge.HelperClasses.GameConstants;
import com.gamb1t.legacyforge.HelperClasses.Interfaces.BitmapMethods;

public enum Layout implements BitmapMethods {

    OUTSIDE(R.drawable.tileset_floor, 22, 26);

    private Bitmap[] sprites;

    Layout(int resID, int tilesInWidth, int tilesInHeight) {
        options.inScaled = false;
        sprites = new Bitmap[tilesInHeight * tilesInWidth];
        Bitmap spriteSheet = BitmapFactory.decodeResource(
                MainActivity.getGameContext().getResources(),
                resID,
                options
        );
        int expectedWidth = tilesInWidth * GameConstants.Sprite.DEFAULT_SIZE;
        int expectedHeight = tilesInHeight * GameConstants.Sprite.DEFAULT_SIZE;

        if (spriteSheet.getWidth() < expectedWidth || spriteSheet.getHeight() < expectedHeight) {
            throw new IllegalArgumentException("Provided dimensions exceed spritesheet size.");
        }

        for (int j = 0; j < tilesInHeight; j++) {
            for (int i = 0; i < tilesInWidth; i++) {
                int index = j * tilesInWidth + i;
                sprites[index] = getScaledBitmap(Bitmap.createBitmap(
                        spriteSheet,
                        GameConstants.Sprite.DEFAULT_SIZE * i,
                        GameConstants.Sprite.DEFAULT_SIZE * j,
                        GameConstants.Sprite.DEFAULT_SIZE,
                        GameConstants.Sprite.DEFAULT_SIZE
                ));
            }
        }
    }

    public Bitmap getSprite(int id) {
        if (id < 0 || id >= sprites.length) {
            throw new IllegalArgumentException("Invalid sprite ID: " + id);
        }
        return sprites[id];
    }
}
