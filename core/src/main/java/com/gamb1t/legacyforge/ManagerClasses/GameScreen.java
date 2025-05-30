package com.gamb1t.legacyforge.ManagerClasses;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.Networking.LocalInputSender;
import com.gamb1t.legacyforge.Networking.PlayerChangeListener;
import com.gamb1t.legacyforge.Structures.ArmorShop;
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

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

    private static List<Player> PLAYERS = new CopyOnWriteArrayList<>();

    private EnemyLoader enemyLoader;

    private ArrayList<Weapon> weapon;
    private ArrayList<Weapon> enemyWeapon;
    private ArmorShop armorShop;


    private Shop shop;

    Player PLAYER;

    private LocalInputSender localInputSender;


    public MapManaging mapManager;


    private GameUI gameUI;

    private GameRendering gameRendering;
    private GameUpdate gameUpdate;

    public GameScreen(User user, PlayerChangeListener playerChangeListener) {

        weaponLoader = new WeaponLoader("weapons.json", true);
        weaponLoader2 = new WeaponLoader("dungeonEnemyWeapon.json", true);
        weapon = weaponLoader.getWeaponList();

        mapManager = new MapManaging("dungeonTextures.txt", "dungeonHitboxes.txt", "Tiles/Dungeon_Tileset.png", 30, 30);

        mapManager.initializeOutside();

        PLAYER= new Player(user.nickname,  user.level, user.experience, user.money, mapManager.getRespPlayer().x+GameConstants.Sprite.SIZE/2, mapManager.getRespPlayer().y+GameConstants.Sprite.SIZE/2,null, mapManager);
        PLAYER.setIsClient(true);
        PLAYERS.add(PLAYER);
        PLAYER.addChangeListener(playerChangeListener);



        PLAYER.addInventoryWeapons(weaponLoader.getWeaponsFromMap(user.items.weapons));

        PLAYER.setCurrentWeapon(PLAYER.getInventory().getWeaponByName((String) user.items.weapons.get(user.equippedWeapon).get("name")));



        enemyWeapon = weaponLoader2.getWeaponList();


        enemyLoader = new EnemyLoader(PLAYERS, enemyWeapon, "dungeonEnemies.json", mapManager.getRespEnemy(), mapManager);

        batch = new SpriteBatch();

        PLAYER.setRespPoint(mapManager.getRespPlayer());



        PLAYER.setTexture("player_sprites/player_spritesheet.png");

        ArmorLoader armorLoader = new ArmorLoader("armor.json");



        PLAYER.addInventoryArmors(
            armorLoader.getArmorsFromMap(user.items.armor)
        );
        Armor helmet = PLAYER.getInventory().getArmorByName((String) user.items.armor.get(user.equippedArmorHelmet).get("name"));
        PLAYER.equipArmor(helmet);

        Armor chest = PLAYER.getInventory().getArmorByName((String) user.items.armor.get(user.equippedArmorChestPlate).get("name"));
        PLAYER.equipArmor(chest);


        Enemies= enemyLoader.getEnemyList();

        for (Enemy enemy : Enemies) {
            enemy.setTexture(enemyLoader.getSpritesheetPath().get(enemy.getId()));
            enemy.getWeapon().setEntity(enemy);
            enemy.setPlayer(PLAYER);

            if(enemy.getWeapon() instanceof RangedWeapon){
            ((RangedWeapon) enemy.getWeapon()).setMap(mapManager);

            }
        }



        localInputSender = new LocalInputSender(PLAYER, PLAYER.getCurrentWeapon());




        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();


        gameUI = new GameUI();
        InputMultiplexer multiplexer = new InputMultiplexer();
        touchEvents = new TouchManager(PLAYER, PLAYER.getCurrentWeapon(), localInputSender);
        multiplexer.addProcessor(gameUI.getStage());
        multiplexer.addProcessor(touchEvents);
        Gdx.input.setInputProcessor(multiplexer);





        if(mapManager.getShopCoordinates() != null) {
            shop = new Shop(mapManager.getShopCoordinates().x, mapManager.getShopCoordinates().y,
                GameConstants.Sprite.SIZE * 4, GameConstants.Sprite.SIZE * 3, "shops/basic_shop.png", weaponLoader, PLAYER, touchEvents);

            shop.initializeRendeingObjects();
        }
       if(mapManager.getArmorShopCoordinates()!=null) {
           armorShop = new ArmorShop(mapManager.getArmorShopCoordinates().x, mapManager.getArmorShopCoordinates().y,
               GameConstants.Sprite.SIZE * 4, GameConstants.Sprite.SIZE * 3, "shops/armor_shop_sprite.png", armorLoader, PLAYER, touchEvents);

           armorShop.initializeRenderingObjects();
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

        if(shop != null){
        for (Weapon w : shop.getWeaponList()) {

            w.setTexture(w.getSprite());
            w.convertTxtRegToSprite();
        }}

        if(armorShop != null){
        for(Armor armor : armorShop.getArmorList()){
            armor.loadTexture();
        }
    }

        gameRendering = new GameRendering(batch, shapeRenderer, font, PLAYER, Enemies, PLAYERS, mapManager, shop, armorShop,  touchEvents);
        gameUpdate = new GameUpdate(Enemies, PLAYERS, mapManager, shop, armorShop);
        touchEvents.setISinglePlayer(true);
    }

    public int getRandom(int x) {
        return rand.nextInt(x);
    }


    public void update(float delta) {
        gameUpdate.update(delta);
        mapManager.update(delta);
        if(shop!=null){
        shop.update(PLAYER.hitbox);}
        if(armorShop != null){
        armorShop.update(PLAYER.hitbox);
        }
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
    public boolean getDirty(){
       return PLAYER.getDirty();
    }
}
