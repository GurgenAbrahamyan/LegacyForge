package com.gamb1t.legacyforge.ManagerClasses;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.Networking.LocalInputSender;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.MeleeWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.gamb1t.legacyforge.Structures.Shop;

public class GameScreen implements Screen {

    public float playerX = (float) GET_WIDTH / 2, playerY = (float) GET_HEIGHT / 2;

    private Random rand = new Random();
    private TouchManager touchEvents;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private static ArrayList<Enemy> Enemies = new ArrayList<>();

    private  WeaponLoader weaponLoader;

    private  WeaponLoader weaponLoader2;

    private static ArrayList<Player> PLAYERS = new ArrayList<Player>();

    private EnemyLoader enemyLoader;

    private ArrayList<Weapon> weapon;
    private ArrayList<Weapon> enemyWeapon;


    private Shop shop;

    Player PLAYER;

    private LocalInputSender localInputSender;


    public MapManaging mapManager;


    private GameUI gameUI;

    private GameRendering gameRendering;
    private GameUpdate gameUpdate;

    public GameScreen(String nickname, float experience, int level, int money) {

        weaponLoader = new WeaponLoader("weapons.json", true);
        weaponLoader2 = new WeaponLoader("enemyWeapons.json", true);
        weapon = weaponLoader.getWeaponList();

        mapManager = new MapManaging("1room.txt", "1roomHitbox.txt", "Tiles/Dungeon_Tileset.png", 30, 30);

        mapManager.initializeOutside();
        PLAYER= new Player(nickname,  level, experience, money, playerX, playerY, weapon.get(0), mapManager);



        weapon.get(0).setTexture(weapon.get(0).getSprite());
        weapon.get(0).convertTxtRegToSprite();
        PLAYER.getCureentWeapon().setEntity(PLAYER);
        PLAYERS.add(PLAYER);



        enemyWeapon = weaponLoader2.getWeaponList();


        enemyLoader = new EnemyLoader(PLAYERS, enemyWeapon, "enemies.json", mapManager.getRespEenemy(), mapManager);

        batch = new SpriteBatch();

        PLAYER.setRespPoint(mapManager.getRespPlayer());



        PLAYER.setTexture("player_sprites/player_spritesheet.png");



        Enemies= enemyLoader.getEnemyList();

        for (Enemy enemy : Enemies) {
            int id =0;
            enemy.setTexture(enemyLoader.getSpritesheetPath().get(id));
            enemy.getWeapon().setEntity(enemy);
            enemy.setPlayer(PLAYER);

            if(enemy.getWeapon() instanceof RangedWeapon){
            ((RangedWeapon) enemy.getWeapon()).setMap(mapManager);

            id++;
            }
        }


        localInputSender = new LocalInputSender(PLAYER, PLAYER.getCureentWeapon());




        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();


        gameUI = new GameUI();
        InputMultiplexer multiplexer = new InputMultiplexer();
        touchEvents = new TouchManager(PLAYER, PLAYER.getCureentWeapon(), localInputSender);
        multiplexer.addProcessor(gameUI.getStage());
        multiplexer.addProcessor(touchEvents);
        Gdx.input.setInputProcessor(multiplexer);

        shop = new Shop(mapManager.getShopCoordinates().x,  mapManager.getShopCoordinates().y,
            GameConstants.Sprite.SIZE*4, GameConstants.Sprite.SIZE*3, "shops/basic_shop.png", weaponLoader, PLAYER, touchEvents);

        shop.initializeRendeingObjects();

        for (Weapon w : shop.getWeaponList()) {

            w.setTexture(w.getSprite());
            w.convertTxtRegToSprite();
            if(w instanceof MagicWeapon){
                ((MagicWeapon)  w).setCurrentMap(mapManager);
            }
        }

        for (Weapon w : weapon) {

            w.setTexture(w.getSprite());
            w.convertTxtRegToSprite();
            if(w instanceof MagicWeapon){
                ((MagicWeapon)  w).setCurrentMap(mapManager);
                ((MagicWeapon)  w).initProj();
            }
            if(w instanceof RangedWeapon){
                ((RangedWeapon)  w).initProj();
            }
        }
        for (Weapon w : enemyWeapon) {

            w.setTexture(w.getSprite());
            w.convertTxtRegToSprite();
            if(w instanceof MagicWeapon){
                ((MagicWeapon)  w).setCurrentMap(mapManager);
                ((MagicWeapon)  w).initProj();
            }
            if(w instanceof RangedWeapon){
                ((RangedWeapon)  w).initProj();
            }
        }

        gameRendering = new GameRendering(batch, shapeRenderer, font, PLAYER, Enemies, PLAYERS, mapManager, shop, touchEvents);
        gameUpdate = new GameUpdate(Enemies, PLAYERS, mapManager, shop);
        shop.update(PLAYER.hitbox);

    }

    public int getRandom(int x) {
        return rand.nextInt(x);
    }


    public void update(float delta) {
        gameUpdate.update(delta);
        shop.update(PLAYER.hitbox);
    }


    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        update(delta);



      gameRendering.render();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
    }

    public int getPlayerMoney(){
        return PLAYER.getMoney();
    }

    public Player getPLAYER(){
        return PLAYER;
    }
}
