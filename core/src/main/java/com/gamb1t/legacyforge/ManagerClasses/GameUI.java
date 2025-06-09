package com.gamb1t.legacyforge.ManagerClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Networking.PlayerChangeListener;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;

import java.util.List;

public class GameUI {
    private List<Player> playerList;
    private Player main;
    private Sprite stats;
    private Sprite returnButton;
    private PlayerChangeListener playerChangeListener;
    private boolean isInHub = false;
    private BitmapFont font;

    public GameUI(Player main, List<Player> player, PlayerChangeListener playerChangeListener) {
        this.main = main;
        this.playerList = player;
        this.playerChangeListener = playerChangeListener;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("gothicbyte.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = GameConstants.Sprite.SIZE / 8;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
        font = generator.generateFont(parameter);
        generator.dispose();
        font.setColor(Color.WHITE);

        stats = new Sprite(new Texture("hitbar.png"));
        returnButton = new Sprite(new Texture("ui/return_button.png"));
        returnButton.setSize(GameConstants.Sprite.SIZE * 2, GameConstants.Sprite.SIZE * 2);
        returnButton.setPosition(
            GameConstants.GET_WIDTH - 2.5f * GameConstants.Sprite.SIZE,
            GameConstants.GET_HEIGHT - 2.5f * GameConstants.Sprite.SIZE
        );
    }

    public void render(SpriteBatch batch, BitmapFont oldFont) {
        batch.begin();
        batch.draw(stats, GameConstants.Sprite.SIZE, GameConstants.GET_HEIGHT - GameConstants.Sprite.SIZE * 3, GameConstants.Sprite.SIZE * 7, GameConstants.Sprite.SIZE * 2);
        if(isInHub) {
            batch.draw(returnButton, returnButton.getX(), returnButton.getY(), returnButton.getWidth(), returnButton.getHeight());
        }
        batch.end();
        batch.begin();
        font.getData().setScale(GameConstants.Sprite.SIZE / 22);

        font.draw(batch, "Lvl:" + main.getLevel(), GameConstants.Sprite.SIZE + GameConstants.Sprite.SIZE * 7/16 , GameConstants.GET_HEIGHT - GameConstants.Sprite.SIZE * 3/2 - GameConstants.Sprite.SIZE/16);
        font.draw(batch, "" + main.getMoney(), GameConstants.Sprite.SIZE * 4, GameConstants.GET_HEIGHT - GameConstants.Sprite.SIZE * 2);
        if (main.getCurrentWeapon() instanceof MagicWeapon) {
            font.draw(batch, "Manna \n" + (int) main.getManna(), GameConstants.Sprite.SIZE*3/2, GameConstants.GET_HEIGHT- GameConstants.Sprite.SIZE * 7/2);
        }
        batch.end();
    }

    public boolean handleTouch(float x, float y) {
        float gameY = GameConstants.GET_HEIGHT - y;

        if (Gdx.input.justTouched()) {
            if (x >= returnButton.getX() && x <= returnButton.getX() + returnButton.getWidth() &&
                gameY >= returnButton.getY() && gameY <= returnButton.getY() + returnButton.getHeight()) {
                if (playerChangeListener != null) {
                    playerChangeListener.onReturnToGameModeSelection(true);
                    return true;
                }
            }
        }
        return false;
    }

    public void dispose() {
        stats.getTexture().dispose();
        returnButton.getTexture().dispose();
        font.dispose();
    }

    public void setIsInHub(boolean b){
        isInHub = b;
    }
}
