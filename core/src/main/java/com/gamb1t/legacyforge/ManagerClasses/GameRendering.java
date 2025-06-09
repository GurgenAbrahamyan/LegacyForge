package com.gamb1t.legacyforge.ManagerClasses;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.Structures.ArmorShop;
import com.gamb1t.legacyforge.Structures.Shop;

import java.util.ArrayList;
import java.util.List;

public class GameRendering {

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Player PLAYER;
    private final ArrayList<Enemy> Enemies;
    private final List<Player> players;
    private final MapManaging mapManager;
    private final Shop shop;
    private final ArmorShop armorShop;
    private final TouchManager touchEvents;
    private final  GameUI gameUi;
    private MultiplayerUi multiplayerUi;
    private SinglePlayerUi singlePlayerUi;

    private boolean isInHub;

    public GameRendering(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font,
                         Player PLAYER, ArrayList< Enemy> Enemies, List<Player> players,
                         MapManaging mapManager, Shop shop, ArmorShop armorShop, TouchManager touchEvents, GameUI gameUI) {
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.font = font;
        this.PLAYER = PLAYER;
        this.Enemies = Enemies;
        this.players = players;
        this.mapManager = mapManager;
        this.shop = shop;
        this.armorShop =armorShop;
        this.touchEvents = touchEvents;
        this.gameUi= gameUI;
    }

    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapManager.draw(batch);

        batch.begin();

        for (Player player : players) {
            float drawX = player.getEntityPos().x + PLAYER.cameraX - GameConstants.Sprite.SIZE / 2;
            float drawY = player.getEntityPos().y + PLAYER.cameraY - GameConstants.Sprite.SIZE / 2;
            float size  = GameConstants.Sprite.SIZE;
            int   dir   = player.getFaceDir();
            int   frame = player.getAniIndex();

            batch.draw(!player.isDead() ? player.getSprite(frame, dir) : player.getSprite(6, 0), drawX, drawY, size, size);

if(player.getEquipment() !=null) {
    player.getEquipment().draw(batch, drawX, drawY, size, size, !player.isDead() ? dir : 0, !player.isDead() ? frame : 6);
}
            if(player.getCurrentWeapon()!= null){
            player.getCurrentWeapon().draw(batch,drawX, drawY);}
        }


        if(Enemies != null) {
            for (Enemy enemy : Enemies) {
                batch.draw(enemy.getIsAlive() ? enemy.getSprite(enemy.getAniIndex(), enemy.getFaceDir()) : enemy.getSprite(6, 0),
                    enemy.getEntityPos().x + PLAYER.cameraX - GameConstants.Sprite.SIZE / 2,
                    enemy.getEntityPos().y + PLAYER.cameraY - GameConstants.Sprite.SIZE / 2,
                    GameConstants.Sprite.SIZE,
                    GameConstants.Sprite.SIZE);

                enemy.getWeapon().draw(batch,
                    enemy.getEntityPos().x + PLAYER.cameraX - GameConstants.Sprite.SIZE / 2,
                    enemy.getEntityPos().y + PLAYER.cameraY - GameConstants.Sprite.SIZE / 2);
            }
        }
        if(shop != null){
        shop.draw(batch, PLAYER.cameraX, PLAYER.cameraY);}
        if(armorShop != null){
        armorShop.draw(batch, PLAYER.cameraX, PLAYER.cameraY);}

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


        PLAYER.drawBar(batch, shapeRenderer, font);
        gameUi.render(batch, new BitmapFont());
        if(isInHub){
        if(singlePlayerUi != null){
            singlePlayerUi.render(batch);
        }
        if(multiplayerUi != null){
            multiplayerUi.render(batch);
        }

        }
        if(shop != null){
            shop.drawShopUi(batch);}
        if(armorShop != null){
            armorShop.drawShopUi(batch);}

        touchEvents.draw(batch);
    }

    public void setMultiplayerUi(MultiplayerUi multiplayerUi) {
        this.multiplayerUi = multiplayerUi;
    }

    public void setSinglePlayerUi(SinglePlayerUi singlePlayerUi) {
        this.singlePlayerUi = singlePlayerUi;
    }

    public void setInHub(boolean inHub) {
        isInHub = inHub;
    }
}
