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
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.MeleeWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.ArrayList;
import java.util.Random;

import com.gamb1t.legacyforge.Structures.Shop;

public class GameScreen implements Screen {

    public float playerX = (float) GET_WIDTH / 2, playerY = (float) GET_HEIGHT / 2;

    private Random rand = new Random();
    private TouchManager touchEvents;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private static ArrayList<Enemy> Enemies = new ArrayList<Enemy>();

    private  WeaponLoader weaponLoader = new WeaponLoader("weapons.json");

    private  WeaponLoader weaponLoader2 = new WeaponLoader("enemyWeapons.json");

    private static ArrayList<Player> PLAYERS = new ArrayList<Player>();

    private EnemyLoader enemyLoader;

    private ArrayList<Weapon> weapon = weaponLoader.getWeaponList();
    private ArrayList<Weapon> enemyWeapon = weaponLoader2.getWeaponList();


    private Shop shop;

    Player PLAYER;


    public MapManaging mapManager;


    private GameUI gameUI;
    public GameScreen(String nickname, float experience, int level, int money) {

        PLAYER= new Player(nickname,  level, experience, money, playerX, playerY, this, weapon.get(0));



        PLAYERS.add(PLAYER);

        weaponLoader = new WeaponLoader("ranged.json");
        weapon.addAll(weaponLoader.getWeaponList());
        weaponLoader = new WeaponLoader("magic.json");
        weapon.addAll(weaponLoader.getWeaponList());


        mapManager = new MapManaging("1room.txt", "1roomHitbox.txt", "Tiles/Dungeon_Tileset.png", 30, 30);


        enemyLoader = new EnemyLoader(this,PLAYER,enemyWeapon, "enemies.json", mapManager.getRespEenemy());

        batch = new SpriteBatch();

        PLAYER.setRespPoint(mapManager.getRespPlayer());



        PLAYER.setTexture("player_sprites/player_spritesheet.png");


        Enemies = enemyLoader.getEnemyList();

        for (Enemy enemy : Enemies) {
            enemy.setTexture("enemies_spritesheet/skeleton_spritesheet.png");
            enemy.setRespPos(mapManager.getRespEenemy());
            enemy.getWeapon().setEntity(enemy);
            if(enemy.getWeapon() instanceof RangedWeapon){
            ((RangedWeapon) enemy.getWeapon()).setMap(mapManager);}
        }





        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        PLAYER.setCurrentWeapon(weapon.get(0));


        gameUI = new GameUI();
        InputMultiplexer multiplexer = new InputMultiplexer();
        touchEvents = new TouchManager(PLAYER, PLAYER.getCureentWeapon());
        multiplexer.addProcessor(gameUI.getStage());
        multiplexer.addProcessor(touchEvents);
        Gdx.input.setInputProcessor(multiplexer);

        shop = new Shop(mapManager.getShopCoordinates().x,  mapManager.getShopCoordinates().y,
            GameConstants.Sprite.SIZE*4, GameConstants.Sprite.SIZE*3, "shops/basic_shop.png", weapon, PLAYER, touchEvents);



        for (Weapon w : weapon) {

           // w.setAttackJoystick((TouchManager) multiplexer.getProcessors().get(1));
            w.setTexture(w.getSprite());
            w.convertTxtRegToSprite();
            if(w instanceof MagicWeapon){
                ((MagicWeapon)  w).setCurrentMap(mapManager);
            }
        }
        for (Weapon w : enemyWeapon) {

            w.setTexture(w.getSprite());
            w.convertTxtRegToSprite();
            if(w instanceof MagicWeapon){
                ((MagicWeapon)  w).setCurrentMap(mapManager);
            }
        }
    }

    public int getRandom(int x) {
        return rand.nextInt(x);
    }

    public static class PointF {
        public float x;
        public float y;

        public PointF(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public void update(float delta) {
        PLAYER.update(delta);
        PLAYER.regenerateMana(delta);
        PLAYER.regenerateHP(delta);
        mapManager.setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
        if(PLAYER.getCureentWeapon() instanceof RangedWeapon){
            ((RangedWeapon) PLAYER.getCureentWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
        }
        if(PLAYER.getCureentWeapon() instanceof MagicWeapon){
         ((MagicWeapon) PLAYER.getCureentWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
        }

        if (PLAYER.getMovePlayer()) {
            PLAYER.updateAnim();
        }
        if(Gdx.input.isTouched()){
            shop.handleTouchInput(Gdx.input.getX(), Gdx.input.getY());
        }

        if (PLAYER.getCureentWeapon().getAttacking()) {



            PLAYER.getCureentWeapon().createHitbox(playerX - PLAYER.cameraX, playerY - PLAYER.cameraY);



        }
        if (PLAYER.getCureentWeapon().getAttacking()) {
            if(PLAYER.getCureentWeapon() instanceof RangedWeapon || PLAYER.getCureentWeapon() instanceof MeleeWeapon){
            PLAYER.getCureentWeapon().update(delta);
            PLAYER.getCureentWeapon().checkHitboxCollisions(Enemies, mapManager);}
        }

        if(PLAYER.getCureentWeapon() instanceof MagicWeapon){
            PLAYER.getCureentWeapon().update(delta);
            PLAYER.getCureentWeapon().checkHitboxCollisions(Enemies, mapManager);
        }


        for (Enemy enemy : Enemies) {
            enemy.updateMove(delta);
            enemy.updateAnimation();

            enemy.setPlayerPosX(playerX - GameConstants.Sprite.SIZE/2- PLAYER.cameraX);
            enemy.setPlayerPosY(playerY - GameConstants.Sprite.SIZE/2-PLAYER.cameraY);

            enemy.attackPlayer();

            if(enemy.getWeapon() instanceof MagicWeapon){
                ((MagicWeapon) enemy.getWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            }
            if(enemy.getWeapon() instanceof RangedWeapon){
                ((RangedWeapon) enemy.getWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            }

            if (enemy.getWeapon().getAttacking()) {



                enemy.getWeapon().createHitbox(enemy.getEntityPos().x, enemy.getEntityPos().y);



            }

            if (enemy.getWeapon().getAttacking()) {
                if(enemy.getWeapon() instanceof MeleeWeapon){
                    for(Player player : PLAYERS){
                    if(!player.isDead()){
                    enemy.getWeapon().update(delta);
                    enemy.getWeapon().checkHitboxCollisions(PLAYERS, mapManager);}}}
            }
                if(enemy.getWeapon() instanceof RangedWeapon){
                enemy.getWeapon().update(delta);
                enemy.getWeapon().checkHitboxCollisions(PLAYERS, mapManager);
            }

            if(enemy.getWeapon() instanceof MagicWeapon){
                enemy.getWeapon().update(delta);
                enemy.getWeapon().checkHitboxCollisions(PLAYERS, mapManager);
            }





        }

        shop.update(PLAYER.hitbox);
    }


    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        update(delta);

        for (Enemy enemy : Enemies) {
            if (PLAYER.getHit(enemy.hitbox)) {
                System.out.println("Damaged: " + enemy.getDamage());
              PLAYER.takeDamage(enemy.getDamage());
            }
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapManager.draw(batch);

        batch.begin();

        batch.draw(PLAYER.getSprite(PLAYER.getAniIndex(), PLAYER.getFaceDir()), playerX - GameConstants.Sprite.SIZE/2, playerY - GameConstants.Sprite.SIZE/2,
            GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE);

        shop.draw(batch, PLAYER.cameraX, PLAYER.cameraY);




        for (Enemy enemy : Enemies) {
            batch.draw(enemy.getSprite(enemy.getAniIndex(), enemy.getFaceDir()), enemy.getEntityPos().x + PLAYER.cameraX - GameConstants.Sprite.SIZE/2,
                enemy.getEntityPos().y + PLAYER.cameraY- GameConstants.Sprite.SIZE/2, GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE);
            enemy.getWeapon().draw(batch,enemy.getEntityPos().x+PLAYER.cameraX  -GameConstants.Sprite.SIZE/2, enemy.getEntityPos().y+PLAYER.cameraY  -GameConstants.Sprite.SIZE/2);
        }

            PLAYER.getCureentWeapon().draw(batch, playerX  - GameConstants.Sprite.SIZE/2, playerY - GameConstants.Sprite.SIZE/2);





        batch.end();


        for (Enemy enemy : Enemies) {
            enemy.drawBar(batch, shapeRenderer, font);
        }

        shop.drawShopUi(batch);

        PLAYER.drawBar(batch, shapeRenderer, font);

        touchEvents.draw(batch);
       // gameUI.render();
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
