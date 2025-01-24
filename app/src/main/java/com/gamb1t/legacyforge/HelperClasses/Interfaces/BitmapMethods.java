package com.gamb1t.legacyforge.HelperClasses.Interfaces;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.gamb1t.legacyforge.HelperClasses.GameConstants;

public interface BitmapMethods {

    BitmapFactory.Options options = new BitmapFactory.Options();

    default Bitmap getScaledBitmap(Bitmap bitmap){
        // here
        return Bitmap.createScaledBitmap(bitmap,bitmap.getWidth() * GameConstants.Sprite.SCALE,bitmap.getHeight()*GameConstants.Sprite.SCALE,false);
    }
}