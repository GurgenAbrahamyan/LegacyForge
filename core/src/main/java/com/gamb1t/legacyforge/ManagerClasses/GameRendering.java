package com.gamb1t.legacyforge.ManagerClasses;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.Structures.Shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameRendering {

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Player PLAYER;
    private final ArrayList<Enemy> Enemies;
    private final ArrayList<Player> players;
    private final MapManaging mapManager;
    private final Shop shop;
    private final TouchManager touchEvents;

    public GameRendering(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font,
                         Player PLAYER, ArrayList< Enemy> Enemies, ArrayList<Player> players,
                         MapManaging mapManager, Shop shop, TouchManager touchEvents) {
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.font = font;
        this.PLAYER = PLAYER;
        this.Enemies = Enemies;
        this.players = players;
        this.mapManager = mapManager;
        this.shop = shop;
        this.touchEvents = touchEvents;
    }

    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapManager.draw(batch);

        batch.begin();

        for (Player player : players) {
            batch.draw(player.getSprite(player.getAniIndex(), player.getFaceDir()),
                player.getEntityPos().x + PLAYER.cameraX - GameConstants.Sprite.SIZE / 2,
                player.getEntityPos().y + PLAYER.cameraY - GameConstants.Sprite.SIZE / 2,
                GameConstants.Sprite.SIZE,
                GameConstants.Sprite.SIZE);

            player.getCureentWeapon().draw(batch,
                player.getEntityPos().x + PLAYER.cameraX - GameConstants.Sprite.SIZE / 2,
                player.getEntityPos().y + PLAYER.cameraY - GameConstants.Sprite.SIZE / 2);
        }

        for (Enemy enemy : Enemies) {
            batch.draw(enemy.getSprite(enemy.getAniIndex(), enemy.getFaceDir()),
                enemy.getEntityPos().x + PLAYER.cameraX - GameConstants.Sprite.SIZE / 2,
                enemy.getEntityPos().y + PLAYER.cameraY - GameConstants.Sprite.SIZE / 2,
                GameConstants.Sprite.SIZE,
                GameConstants.Sprite.SIZE);

            enemy.getWeapon().draw(batch,
                enemy.getEntityPos().x + PLAYER.cameraX - GameConstants.Sprite.SIZE / 2,
                enemy.getEntityPos().y + PLAYER.cameraY - GameConstants.Sprite.SIZE / 2);
        }

        shop.draw(batch, PLAYER.cameraX, PLAYER.cameraY);

        batch.end();

        for (Enemy enemy : Enemies) {
            enemy.drawBar(batch, shapeRenderer, font);
        }
        if(players.size() > 1){
        for(Player player : players){
            if(player != PLAYER){
            player.drawBarMultiplayer(batch, shapeRenderer, font, PLAYER.cameraX, PLAYER.cameraY);
            }
        }
        }

        shop.drawShopUi(batch);

        for (Player player : players) {
            player.drawBar(batch, shapeRenderer, font);
        }

        PLAYER.drawBar(batch, shapeRenderer, font);
        touchEvents.draw(batch);
    }
}
