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
import com.gamb1t.legacyforge.Entity.Items;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.Main;
import com.gamb1t.legacyforge.Networking.LocalInputSender;
import com.gamb1t.legacyforge.Networking.PlayerChangeListener;
import com.gamb1t.legacyforge.Structures.ArmorShop;
import com.gamb1t.legacyforge.Structures.Shop;
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class GameScreen implements Screen {

    public static final int MAX_PLAYERS = 1; // Single-player only

    public float playerX = (float) GET_WIDTH / 2, playerY = (float) GET_HEIGHT / 2;

    private Random rand = new Random();
    private TouchManager touchEvents;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private boolean isInHub;
    private boolean isInDungeon;
    private SinglePlayerUi ui;

    private ArrayList<Enemy> enemies;
    private ArrayList<Player> PLAYERS = new ArrayList<>();
    private WeaponLoader weaponLoader;
    private WeaponLoader enemyWeaponLoader;
    private ArmorLoader armorLoader;
    private EnemyLoader enemyLoader;
    private ArrayList<Weapon> weapons;
    private ArrayList<Weapon> enemyWeapons;
    private Shop shop;
    private ArmorShop armorShop;

    private Player player;
    private MapManaging mapManager;
    private GameUI gameUI;
    private GameRendering gameRendering;
    private GameUpdate gameUpdate;

    private LocalInputSender localInputSender;
    private Main main;

    PlayerChangeListener playerChangeListener;

    public GameScreen(User user, PlayerChangeListener playerChangeListener, Main main) {

        this(user, playerChangeListener, main, 0);

    }

    public GameScreen(User user, PlayerChangeListener playerChangeListener, Main main, int mode) {

        this.playerChangeListener = playerChangeListener;
        this.main = main;
        isInHub = mode == 0;
        isInDungeon = mode == 1;


        String weaponJson = "weapons.json";
        String enemyWeaponJson = isInDungeon ? "dungeonEnemyWeapon.json" : "enemyWeapon.json";
        String enemyJson = isInDungeon ? "dungeonEnemies.json" : "enemies.json";
        String mapTexture = isInDungeon ? "dungeonTextures.txt" : "1room.txt";
        String mapHitbox = isInDungeon ? "dungeonHitboxes.txt" :  "1roomHitbox.txt";


        weaponLoader = new WeaponLoader(weaponJson, true);

        armorLoader = new ArmorLoader( "armor.json");
        if(isInDungeon){
        enemyWeaponLoader = new WeaponLoader(enemyWeaponJson, true);

            enemyWeapons = enemyWeaponLoader.getWeaponList();
        }
        weapons = weaponLoader.getWeaponList();


        mapManager = new MapManaging(mapTexture, mapHitbox, "Tiles/Dungeon_Tileset.png", isInDungeon? 30 : 17, isInDungeon? 30:17);
        mapManager.initializeOutside();

        if(isInHub){
            ui = new SinglePlayerUi( main);
        }
        player = new Player(
            user.nickname,
            user.level,
            user.experience,
            user.money,
            mapManager.getRespPlayer().x + GameConstants.Sprite.SIZE / 2,
            mapManager.getRespPlayer().y + GameConstants.Sprite.SIZE / 2,
            null,
            mapManager
        );
        player.setId(0);
        player.setIsClient(true);
        player.addChangeListener(playerChangeListener);
        player.setRespPoint(mapManager.getRespPlayer());
        player.setTexture("player_sprites/player_spritesheet.png");


        player.addInventoryWeapons(weaponLoader.getWeaponsFromMap(user.items.weapons));
        player.setCurrentWeapon(player.getInventory().getWeaponByName((String) user.items.weapons.get(user.equippedWeapon).get("name")));
        if (player.getCurrentWeapon() != null) {
            player.getCurrentWeapon().setEntity(player);
        }
        player.addInventoryArmors(armorLoader.getArmorsFromMap(user.items.armor));
        Armor helmet = player.getInventory().getArmorByName((String) user.items.armor.get(user.equippedArmorHelmet).get("name"));
        if (helmet != null) {
            player.equipArmor(helmet);
        }
        if (user.equippedArmorChestPlate != null && !user.equippedArmorChestPlate.isEmpty()) {
            Armor chest = player.getInventory().getArmorByName((String) user.items.armor.get(user.equippedArmorChestPlate).get("name"));
            if (chest != null) {
                player.equipArmor(chest);
            }
        }

        PLAYERS.add(player);

        enemies = new ArrayList<>();
        if (isInDungeon) {
            enemyLoader = new EnemyLoader(PLAYERS, enemyWeapons, enemyJson, mapManager.getRespEnemy(), mapManager);
            enemies = enemyLoader.getEnemyList();
            int enemyId = 1;
            for (Enemy enemy : enemies) {
                enemy.setTexture(enemyLoader.getSpritesheetPath().get(enemy.getId()));
                enemy.getWeapon().setEntity(enemy);
                enemy.setPlayer(player);
                enemy.setId(enemyId++);
                if (enemy.getWeapon() instanceof RangedWeapon) {
                    ((RangedWeapon) enemy.getWeapon()).setMap(mapManager);
                }
            }
        }

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        localInputSender = new LocalInputSender(player, player.getCurrentWeapon());
        touchEvents = new TouchManager(player, player.getCurrentWeapon(), localInputSender);
        touchEvents.setISinglePlayer(true);
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(touchEvents);
        Gdx.input.setInputProcessor(multiplexer);

        if (isInHub && mapManager.getShopCoordinates() != null) {
            shop = new Shop(
                mapManager.getShopCoordinates().x,
                mapManager.getShopCoordinates().y,
                GameConstants.Sprite.SIZE * 4,
                GameConstants.Sprite.SIZE * 3,
                "shops/basic_shop.png",
                weaponLoader,
                player,
                touchEvents
            );
            shop.initializeRendeingObjects();
        }
        if (isInHub && mapManager.getArmorShopCoordinates() != null) {
            armorShop = new ArmorShop(
                mapManager.getArmorShopCoordinates().x,
                mapManager.getArmorShopCoordinates().y,
                GameConstants.Sprite.SIZE * 4,
                GameConstants.Sprite.SIZE * 3,
                "shops/armor_shop_sprite.png",
                armorLoader,
                player,
                touchEvents
            );
            armorShop.initializeRenderingObjects();
        }

        if(isInDungeon){
        for (Weapon w : enemyWeapons) {
            w.setTexture(w.getSprite());
            w.convertTxtRegToSprite();
            if (w instanceof MagicWeapon) {
                ((MagicWeapon) w).setCurrentMap(mapManager);
                ((MagicWeapon) w).initProj();
            }
            if (w instanceof RangedWeapon) {
                ((RangedWeapon) w).initProj();
            }
        }}
        if (shop != null) {
            for (Weapon w : shop.getWeaponList()) {
                w.setTexture(w.getSprite());
                w.convertTxtRegToSprite();
            }
        }
        if (armorShop != null) {
            for (Armor armor : armorShop.getArmorList()) {
                armor.loadTexture();
            }
        }

        gameUI = new GameUI(player, new ArrayList<>(), playerChangeListener);
        gameUI.setIsInHub(isInHub);
        gameRendering = new GameRendering(batch, shapeRenderer, font, player, enemies, PLAYERS, mapManager, shop, armorShop, touchEvents, gameUI);
        gameRendering.setSinglePlayerUi(ui);
        gameRendering.setInHub(isInHub);
        gameUpdate = new GameUpdate(enemies, PLAYERS, mapManager, shop, armorShop);
    }



    public void update(float delta) {
        gameUpdate.update(delta);
        mapManager.update(delta);
        if (shop != null) {
            shop.update(player.hitbox);
        }
        if (armorShop != null) {
            armorShop.update(player.hitbox);
        }
        if (mapManager.getGoingBack() && isInDungeon) {
            main.setScreen(new GameScreen(convertPlayerToUser(player), playerChangeListener, main));
        }
        if(isInHub){
            ui.update(delta);
            gameUI.handleTouch(Gdx.input.getX(), Gdx.input.getY());
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
        font.dispose();
        gameUI.dispose();
    }

    public int getPlayerMoney() {
        return player.getMoney();
    }

    public Player getPlayer() {
        return player;
    }

    public boolean getDirty() {
        return player.getDirty();
    }

    public User convertPlayerToUser(Player player) {
        User user = new User();
        user.nickname = player.getName();
        user.level = player.getLevel();
        user.experience = (int) player.getExperience();
        user.money = player.getMoney();
        user.firebaseId = player.getFirebaseId();
        user.wins = player.getWins();
        user.losses = player.getLoses();
        user.rating = player.getRating();
        user.items = new Items();
        user.items.weapons = new HashMap<>();
        user.items.armor = new HashMap<>();

        for (Weapon weapon : player.getInventory().getWeapons()) {
            Map<String, Object> weaponMap = new HashMap<>();
            weaponMap.put("name", weapon.getName());
            weaponMap.put("level", weapon.getLevel());
            String firebaseId = weapon.getFireBaseId() != null ? weapon.getFireBaseId() : UUID.randomUUID().toString();
            user.items.weapons.put(firebaseId, weaponMap);
            if (player.getCurrentWeapon() != null && weapon.getName().equals(player.getCurrentWeapon().getName())) {
                user.equippedWeapon = firebaseId;
            }
        }

        for (Armor armor : player.getInventory().getArmors()) {
            Map<String, Object> armorMap = new HashMap<>();
            armorMap.put("name", armor.getName());
            armorMap.put("level", armor.getLevel());
            String firebaseId = armor.getFirebaseId() != null ? armor.getFirebaseId() : UUID.randomUUID().toString();
            user.items.armor.put(firebaseId, armorMap);
            if (player.getEquipment().getHelmet() != null && armor.getName().equals(player.getEquipment().getHelmet().getName())) {
                user.equippedArmorHelmet = firebaseId;
            }
            if (player.getEquipment().getChestplate() != null && armor.getName().equals(player.getEquipment().getChestplate().getName())) {
                user.equippedArmorChestPlate = firebaseId;
            }
        }

        if (user.equippedWeapon == null) {
            user.equippedWeapon = "";
        }
        if (user.equippedArmorHelmet == null) {
            user.equippedArmorHelmet = "";
        }
        if (user.equippedArmorChestPlate == null) {
            user.equippedArmorChestPlate = "";
        }

        return user;
    }
}
