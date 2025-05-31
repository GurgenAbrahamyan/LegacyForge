package com.gamb1t.legacyforge.ManagerClasses;



import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;

import java.util.ArrayList;
import java.util.List;

public class GameUI {
    private List<Player> playerList;
    private  Player main;
    private Sprite stats;


    public GameUI(Player main, List<Player> player) {

        this.main = main;
        this.playerList = player;

        stats = new Sprite(new Texture("aram_daun.png"));


    }


    public void render(SpriteBatch batch, BitmapFont font) {
        batch.begin();


        batch.draw(stats,  GameConstants.Sprite.SIZE, GameConstants.GET_HEIGHT-GameConstants.Sprite.SIZE*3, GameConstants.Sprite.SIZE*6,  GameConstants.Sprite.SIZE*2);

        batch.end();

        batch.begin();

        font.getData().setScale(GameConstants.Sprite.SIZE/22);
        font.draw(batch, "" + main.getHp(), GameConstants.Sprite.SIZE + GameConstants.Sprite.SIZE/2  ,  GameConstants.GET_HEIGHT-GameConstants.Sprite.SIZE*3/2);
        font.draw(batch, "" + main.getMoney(), GameConstants.Sprite.SIZE*4- GameConstants.Sprite.SIZE/2  , GameConstants.GET_HEIGHT - GameConstants.Sprite.SIZE*2);
        if(main.getCurrentWeapon() instanceof MagicWeapon) {
            font.draw(batch, "Manna \n" + main.getManna(), GameConstants.GET_WIDTH - GameConstants.GET_WIDTH / 5 + GameConstants.Sprite.SIZE, GameConstants.GET_HEIGHT - GameConstants.GET_HEIGHT / 8);
        }batch.end();
    }

}
