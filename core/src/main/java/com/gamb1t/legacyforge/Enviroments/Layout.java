package com.gamb1t.legacyforge.Enviroments;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;

public class Layout {

    private String textureFile;
    private Texture texture;
    private TextureRegion[] sprites;
    private boolean initialized = false;
    private int tilesInHeight, tilesInWidth;

    Layout(String textureFile) {
        this.textureFile = textureFile;

        initialize();
    }

    private void initialize() {
        if (!initialized) {
            texture = new Texture(textureFile);
            tilesInHeight = texture.getHeight()/ GameConstants.Sprite.DEFAULT_SIZE;
            System.out.println(tilesInHeight);
            tilesInWidth = texture.getWidth()/GameConstants.Sprite.DEFAULT_SIZE;
            System.out.println(tilesInWidth);
            this.sprites = new TextureRegion[tilesInHeight * tilesInWidth];

            for (int j = 0; j < tilesInHeight; j++) {
                for (int i = 0; i < tilesInWidth; i++) {
                    int index = j * tilesInWidth + i;
                    sprites[index] = new TextureRegion(texture,
                        GameConstants.Sprite.DEFAULT_SIZE * i,
                        GameConstants.Sprite.DEFAULT_SIZE * j,
                        GameConstants.Sprite.DEFAULT_SIZE,
                        GameConstants.Sprite.DEFAULT_SIZE);
                }
            }
            initialized = true;
        }
    }

    public TextureRegion getSprite(int id) {
        if (id < 0 || id >= sprites.length) {
            throw new IllegalArgumentException("Invalid sprite ID: " + id);
        }
        return sprites[id];
    }


    public void dispose() {
        if (initialized) {
            texture.dispose();
        }
    }
}
