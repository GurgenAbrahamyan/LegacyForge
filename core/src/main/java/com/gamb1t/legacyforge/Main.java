package com.gamb1t.legacyforge;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    String nickname;
    float exp;
    int level;
    int money;
    GameScreen gameScreen;

    public Main (String nickname, float experience, int level, int money){

        this.nickname=nickname;
        this.exp= experience;
        this.level = level;
        this.money= money;


    }



    @Override
    public void create() {

        gameScreen = new GameScreen(nickname, exp, level, money);




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
