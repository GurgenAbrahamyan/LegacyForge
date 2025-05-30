package com.gamb1t.legacyforge;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;
import com.gamb1t.legacyforge.Networking.PlayerChangeListener;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {

    public static AssetManager assets;

    User user;
    GameScreen gameScreen;
    PlayerChangeListener playerChangeListener;


    public Main (User user, PlayerChangeListener playerChangeListener){

        this.user = user;
        this.playerChangeListener = playerChangeListener;


    }



    @Override
    public void create() {

        assets = new AssetManager();


        gameScreen = new GameScreen(user, playerChangeListener);

        this.setScreen(gameScreen);


    }

    public int getMoney(){


        return gameScreen.getPlayerMoney();


    }

    public boolean isInitialized(){
        if(gameScreen !=  null){
            return true;
        }
        return false;
    }




    @Override
    public void render() {
        super.render();

    }

    @Override
    public void dispose() {
    }
}
