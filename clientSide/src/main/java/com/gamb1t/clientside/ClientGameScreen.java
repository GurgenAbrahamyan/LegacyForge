package com.gamb1t.clientside;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Client;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.ManagerClasses.ArmorLoader;
import com.gamb1t.legacyforge.ManagerClasses.EnemyLoader;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.GameRendering;
import com.gamb1t.legacyforge.ManagerClasses.GameUI;
import com.gamb1t.legacyforge.ManagerClasses.MultiplayerUi;
import com.gamb1t.legacyforge.ManagerClasses.TouchManager;
import com.gamb1t.legacyforge.ManagerClasses.WeaponLoader;
import com.gamb1t.legacyforge.Networking.Network;
import com.gamb1t.legacyforge.Networking.NetworkInputSender;
import com.gamb1t.legacyforge.Networking.PlayerChangeListener;
import com.gamb1t.legacyforge.Structures.ArmorShop;
import com.gamb1t.legacyforge.Structures.Shop;
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.MeleeWeapon;
import com.gamb1t.legacyforge.Weapons.Projectile;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientGameScreen implements Screen {
    public float playerX = (float) GET_WIDTH / 2, playerY = (float) GET_HEIGHT / 2;

    private Random rand = new Random();
    private TouchManager touchEvents;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;




    private ArrayList<Enemy> Enemies = new ArrayList<>();
    private Map<Integer, Enemy> EnemiesMap = new HashMap<>();

    private  WeaponLoader weaponLoader;

    private  WeaponLoader weaponLoader2;

    private List<Player> PLAYERS = new CopyOnWriteArrayList<>();



    private ArrayList<Weapon> weapon;
    private ArrayList<Weapon> enemyWeapon;

    private NetworkInputSender networkInputSender;

    private Shop shop;

    private MultiplayerUi multiplayerUi;

    private ArmorShop armorShop;

    private EnemyLoader enemyLoader;

    Player PLAYER;

    public boolean isInHub;

    private ArmorLoader armorLoader;


    public MapManaging mapManager;

    private Client client;


    private GameUI gameUI;

    Map<Integer, Player> playersById = new HashMap<>();
    private GameRendering gameRendering;


    public ClientGameScreen(User user, Network.StateMessageOnConnection connection, Client client, PlayerChangeListener changeListener) {





        this.client = client;
        mapManager = new MapManaging(connection.mapInfo.mapPath, connection.mapInfo.hitboxPath, connection.mapInfo.renderPath, connection.mapInfo.width, connection.mapInfo.height);

        mapManager.initializeOutside();


        weaponLoader = new WeaponLoader(connection.playerWeaponJson, true);
        if(connection.enemyWeaponJson != null) {
            weaponLoader2 = new WeaponLoader(connection.enemyWeaponJson, true);
        }
        for (Weapon w : weaponLoader.getWeaponList()) {

            w.setTexture(w.getSprite());
            w.convertTxtRegToSprite();
            if(w instanceof MagicWeapon){
                ((MagicWeapon)  w).setCurrentMap(mapManager);
                ((MagicWeapon) w).initProj();
            }
            if(w instanceof RangedWeapon){

                    ((RangedWeapon)  w).initProj();

            }
        }





        weapon=  weaponLoader.getWeaponList();

        PLAYER= new Player(user.nickname,  user.level, user.experience, user.money, connection.curX, connection.curY, null, mapManager);
        PLAYER.setIsClient(true);
        PLAYER.addChangeListener(changeListener);
        PLAYER.setFirebaseId(user.firebaseId);

        PLAYER.addInventoryWeapons(weaponLoader.getWeaponsFromMap(user.items.weapons));

        PLAYER.setCurrentWeapon(PLAYER.getInventory().getWeaponByName(connection.weaponName));

        Weapon w2 = PLAYER.getCurrentWeapon();

        w2.setArray( weaponLoader.getWeaponByName(w2.getName()).getArray() );
        w2.convertTxtRegToSprite();
        if(w2 instanceof MagicWeapon){
            ((MagicWeapon)  w2).setCurrentMap(mapManager);
            ((MagicWeapon)  w2).setProj(((MagicWeapon) weaponLoader.getWeaponByName(w2.getName())).getProjectileTexture());
        }

        if(w2 instanceof RangedWeapon){
            ((RangedWeapon)  w2).setProj(((RangedWeapon)weaponLoader.getWeaponByName(w2.getName())).getProjectileTexture());
            System.out.println(((RangedWeapon) w2).getProjectileTexture());
        }
        armorLoader = new ArmorLoader("armor.json");

        for(Armor armor: armorLoader.getArmorList()){
            armor.loadTexture();
        }


        PLAYER.addInventoryArmors(
            armorLoader.getArmorsFromMap(user.items.armor)
        );
        if(user.equippedArmorHelmet != null){
            System.out.println("passed helmet "+connection.currentHelmet );
        Armor helmet = PLAYER.getInventory().getArmorByName(connection.currentHelmet);
        PLAYER.equipArmor(helmet);
        }
        if(user.equippedArmorChestPlate != null){
            System.out.println("passed chest" +connection.currentChestplate);
        Armor chest = PLAYER.getInventory().getArmorByName(connection.currentChestplate);
        PLAYER.equipArmor(chest);
        }



        PLAYER.setId(connection.newPlayerId);

        PLAYER.getCurrentWeapon().setEntity(PLAYER);

        PLAYER.setClient(client);

        playersById.put((connection.newPlayerId), PLAYER);

        PLAYERS.add(PLAYER);
        isInHub = connection.gameMode.equals("Hub");

            multiplayerUi = new MultiplayerUi(PLAYER, client);






        if (connection.players != null) {
            for (Network.PlayerState ps : connection.players) {
                Player other = new Player(
                    ps.name, ps.level, ps.experience, ps.money,
                    ps.x * GameConstants.Sprite.SIZE,
                    ps.y * GameConstants.Sprite.SIZE,
                    null,
                    mapManager
                );
                other.addWeapon(weaponLoader.deepCopyWeapon(ps.weaponName), 1);
                other.setCurrentWeapon(other.getInventory().getWeaponByName(ps.weaponName));

                Weapon w = other.getCurrentWeapon();
                w.setArray(weaponLoader.getWeaponByName(ps.weaponName).getArray());
                w.convertTxtRegToSprite();
                if (w instanceof MagicWeapon) {
                    ((MagicWeapon) w).setCurrentMap(mapManager);
                    ((MagicWeapon) w).setProj(((MagicWeapon) weaponLoader.getWeaponByName(w.getName())).getProjectileTexture());
                }
                if (w instanceof RangedWeapon) {
                    ((RangedWeapon) w).setProj(((RangedWeapon) weaponLoader.getWeaponByName(w.getName())).getProjectileTexture());
                }


                if(ps.currentHelmet != null){

                    other.addArmor(armorLoader.deepCopyArmor(ps.currentHelmet), 1);
                Armor helmetOtherPlayer = other.getInventory().getArmorByName(ps.currentHelmet);


                if (helmetOtherPlayer != null) {
                    helmetOtherPlayer.loadTexture();
                    other.equipArmor(helmetOtherPlayer);
                } }

                else {
                    System.err.println("Helmet not found for player " + ps.name + ": " + ps.currentHelmet);
                }

                if(ps.currentChestplate != null){
                other.addArmor(armorLoader.deepCopyArmor(ps.currentChestplate), 1);
                Armor chestOtherPlayer = other.getInventory().getArmorByName(ps.currentChestplate);
                if (chestOtherPlayer != null) {
                    chestOtherPlayer.loadTexture();
                    other.equipArmor(chestOtherPlayer);
                } else {
                    System.err.println("Chestplate not found for player " + ps.name + ": " + ps.currentChestplate);
                }

                }


                other.setHp(ps.hp);
                other.setId(ps.id);
                PLAYERS.add(other);


                playersById.put(ps.id, other);
            }
        }

        if(connection.gameMode.equals("1v1")){
            Network.RoundStart roundStart = new Network.RoundStart();
            roundStart.roundNumber = 1;

            roundStart.playerScores = new HashMap<>();
            for(int i : playersById.keySet()){
                roundStart.playerScores.put(i, 0);
            }
            multiplayerUi.handleRoundStart(roundStart);
        }


        for(Player player : PLAYERS){
            player.setTexture("player_sprites/player_spritesheet.png");
            player.setRespPoint(mapManager.getRespPlayer());
        }
        if(weaponLoader2 != null) {
            enemyWeapon = weaponLoader2.getWeaponList();

            enemyLoader = new EnemyLoader(PLAYERS, enemyWeapon, connection.jsonPath, mapManager.getRespEnemy(), mapManager);

        }
        networkInputSender = new NetworkInputSender(PLAYER, PLAYER.getCurrentWeapon(), client);







        batch = new SpriteBatch();







        if(connection.enemies != null) {
            int i = 0;
            for (Network.EnemyState es : connection.enemies) {
                Enemy enemy = new Enemy(PLAYERS, new Vector2(es.x*GameConstants.Sprite.SIZE, es.y*GameConstants.Sprite.SIZE), enemyWeapon.get(i), mapManager);
                enemy.setSpeed(es.speed);
                enemy.setId(es.id);


                Enemies.add(enemy);

                EnemiesMap.put(es.id, enemy);

                System.out.println("Enemy id is"+ enemy.getId() + " and weapon is" + enemy.getWeapon().getName());
                enemy.setDirection(es.dirX, es.dirY);
                i++;
            }


            for (Enemy enemy1 : Enemies) {
                enemy1.setTexture(enemyLoader.getSpritesheetPath().get(enemy1.getId()-1)); enemy1.getWeapon().setEntity(enemy1);
                enemy1.setPlayer(PLAYER);
                if (enemy1.getWeapon() instanceof RangedWeapon) {
                    ((RangedWeapon) enemy1.getWeapon()).setMap(mapManager);
                }
            }


        }


        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();



        gameUI = new GameUI(PLAYER, PLAYERS, changeListener);
       gameUI.setIsInHub(isInHub);
        InputMultiplexer multiplexer = new InputMultiplexer();
        touchEvents = new TouchManager(PLAYER, PLAYER.getCurrentWeapon(), networkInputSender);
        multiplexer.addProcessor(touchEvents);
        Gdx.input.setInputProcessor(multiplexer);

        if(connection.shopInfo != null) {
            shop = new Shop(mapManager.getShopCoordinates().x,
                mapManager.getShopCoordinates().y,
                connection.shopInfo.width * GameConstants.Sprite.SIZE,
                connection.shopInfo.height * GameConstants.Sprite.SIZE,
                connection.shopInfo.renderPath, /*connection.shopInfo.weaponsJson*/ weaponLoader, PLAYER, touchEvents);
            shop.initializeRendeingObjects();

            for (Weapon w : shop.getWeaponList()) {

                w.setTexture(w.getSprite());
                w.convertTxtRegToSprite();
                if(w instanceof MagicWeapon){
                    ((MagicWeapon)  w).setCurrentMap(mapManager);
                }
            }
        }
        if(connection.armorShopInfo !=null) {
            armorShop = new ArmorShop(mapManager.getArmorShopCoordinates().x,
                mapManager.getArmorShopCoordinates().y,
                connection.armorShopInfo.width * GameConstants.Sprite.SIZE,
                connection.armorShopInfo.height * GameConstants.Sprite.SIZE,
                connection.armorShopInfo.renderPath, armorLoader, PLAYER, touchEvents);

            armorShop.initializeRenderingObjects();
        }





        if(enemyWeapon != null){
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
        }}

        gameRendering = new GameRendering(batch, shapeRenderer, font, PLAYER, Enemies, PLAYERS, mapManager, shop, armorShop, touchEvents, gameUI);
        gameRendering.setMultiplayerUi(multiplayerUi);
        gameRendering.setInHub(isInHub);
    }

    public void createProjectile(Network.CreateProjectileMessage projectileMessage){
        Weapon curWeapon;
        if(projectileMessage.isEnemy){
            curWeapon = EnemiesMap.get(projectileMessage.enemyId).getWeapon();
        }
        else {
            curWeapon = playersById.get(projectileMessage.enemyId).getCurrentWeapon();
        }



            if(curWeapon instanceof RangedWeapon){
                ((RangedWeapon) curWeapon).shootArrow(projectileMessage);
            }
            else if(curWeapon instanceof MagicWeapon){

                ((MagicWeapon) curWeapon).shootProjectile(projectileMessage);
            }


    }

    public void cancelAttack(Network.AttackCanceled msg){
        Weapon curWeapon;

        if (msg.isEnemy) {
            Enemy enemy = EnemiesMap.get(msg.enemyId);
            if (enemy == null) return;
            curWeapon = enemy.getWeapon();
        } else {
            Player p = playersById.get(msg.enemyId);
            if (p == null) return;
            curWeapon = p.getCurrentWeapon();
        }



        if (curWeapon instanceof RangedWeapon) {
            RangedWeapon rw = (RangedWeapon) curWeapon;
            rw.setAiming(false);
        }


    }

    public void setProjectilePosition(Network.SetProjectilePositionMessage msg) {
        Weapon curWeapon;

        if (msg.isEnemy) {
            Enemy enemy = EnemiesMap.get(msg.enemyId);
            if (enemy == null) return;
            curWeapon = enemy.getWeapon();
        } else {
            Player p = playersById.get(msg.enemyId);
            if (p == null) return;
            curWeapon = p.getCurrentWeapon();
        }

        if (curWeapon instanceof RangedWeapon) {
            RangedWeapon rw = (RangedWeapon) curWeapon;
            for (Projectile proj : rw.getProjectiles()) {
                if (proj.getId() == msg.projectileId) {
                    System.out.println("enemy id for ranged" + msg.enemyId);
                    proj.setPosition(msg.x*GameConstants.Sprite.SIZE-GameConstants.Sprite.SIZE/2, msg.y*GameConstants.Sprite.SIZE-GameConstants.Sprite.SIZE/2);
                    break;
                }
            }
        }

        else if (curWeapon instanceof MagicWeapon) {
            MagicWeapon mw = (MagicWeapon) curWeapon;
            for (Projectile proj : mw.getProjectiles()) {
                if (proj.getId() == msg.projectileId) {
                    proj.setPosition(msg.x*GameConstants.Sprite.SIZE-GameConstants.Sprite.SIZE/2, msg.y*GameConstants.Sprite.SIZE -GameConstants.Sprite.SIZE/2);
                    break;
                }
            }
        }
    }


    public void setNewWeapon(Network.OnPlayerEquipWeapon equipWeapon){
        Player p = playersById.get(equipWeapon.playerId);
        if(p != null){
            System.out.println("Player found ");
            if(p.getInventory().containsWeaponByName(equipWeapon.weaponName)) {
                p.setCurrentWeapon(p.getInventory().getWeaponByName(equipWeapon.weaponName));
            }
            else {
                p.addWeapon(weaponLoader.deepCopyWeapon(equipWeapon.weaponName), 1);
                p.setCurrentWeapon(p.getInventory().getWeaponByName(equipWeapon.weaponName));
                Weapon w = p.getCurrentWeapon();

                w.setArray(weaponLoader.getWeaponByName(equipWeapon.weaponName).getArray());
                w.convertTxtRegToSprite();
                if(w instanceof MagicWeapon){
                    ((MagicWeapon)  w).setCurrentMap(mapManager);
                    ((MagicWeapon)  w).setProj(((MagicWeapon) weaponLoader.getWeaponByName(w.getName())).getProjectileTexture());
                }

                if(w instanceof RangedWeapon){
                    ((RangedWeapon)  w).setProj(((RangedWeapon)weaponLoader.getWeaponByName(w.getName())).getProjectileTexture());
                }
            }

        }
    }
    public void destroyProjectile(Network.DestroyProjectileMessage destroyProjectileMessage){

        Weapon curWeapon;
        if(destroyProjectileMessage.isEnemy){
            curWeapon = EnemiesMap.get(destroyProjectileMessage.enemyId).getWeapon();
        }
        else {
            curWeapon = playersById.get(destroyProjectileMessage.enemyId).getCurrentWeapon();
        }
        if(curWeapon != null){

            if(curWeapon instanceof RangedWeapon){
                ((RangedWeapon) curWeapon).removeById(destroyProjectileMessage);
            }
            else if(curWeapon instanceof MagicWeapon){

                ((MagicWeapon) curWeapon).removeById(destroyProjectileMessage);
            }

        }
    }

    public void startAttack(Network.AttackStartPacket packet){
        if(packet.isEnemy){
            EnemiesMap.get(packet.id).attackStarted();
            return;}
        Weapon  currentWeapon =  playersById.get(packet.id).getCurrentWeapon();

        if(currentWeapon instanceof RangedWeapon ){
            currentWeapon.setAttacking(true);
            ((RangedWeapon) currentWeapon).setAnimOver(true);
            currentWeapon.setAiming(true);
        }
    }

    public void attackDragged(Network.AttackDragged packet){
        if(packet.isEnemy){
            EnemiesMap.get(packet.id).attackDragged(packet.angle);
            return;}

        Weapon curWeapon =  playersById.get(packet.id).getCurrentWeapon();

        if(curWeapon instanceof RangedWeapon){

            curWeapon.setRotation(packet.angle);

        }
        curWeapon.setAiming(true);
    }

    public void endAttack(Network.AttackReleasePacket packet){
        if(packet.isEnemy){
            EnemiesMap.get(packet.id).attackReleased(packet.angle);
            return;}
        Weapon curWeapon =  playersById.get(packet.id).getCurrentWeapon();
        curWeapon.setRotation(packet.angle);
        if(curWeapon.getAiming()){


            if(curWeapon instanceof MeleeWeapon){
            curWeapon.attack();}

            if(curWeapon instanceof RangedWeapon){
                ((RangedWeapon) curWeapon).attackNoProj();}
            if(curWeapon instanceof MagicWeapon){
                ((MagicWeapon) curWeapon).attackNoProj();
            }



            curWeapon.setAttacking(true);
            if(curWeapon instanceof  RangedWeapon){
                ((RangedWeapon) curWeapon).setAnimOver(true);
            }
            if(curWeapon instanceof MagicWeapon){
                ((MagicWeapon) curWeapon).setAnimOver(true);
            }

            if( curWeapon instanceof RangedWeapon){
                ((RangedWeapon) curWeapon).setIsCharging(false);
            }

            curWeapon.setAiming(false);


        }
    }

    public void addPlayer(Network.PlayerState state) {
        if (playersById.containsKey(state.id)) return;

            Player newPlayer = new Player(
            state.name,
            state.level,
            state.experience,
            state.money,
            state.x * GameConstants.Sprite.SIZE,
            state.y * GameConstants.Sprite.SIZE,
            null, mapManager
        );

        newPlayer.addWeapon(weaponLoader.deepCopyWeapon(state.weaponName), 1);
        newPlayer.setCurrentWeapon(newPlayer.getInventory().getWeaponByName(state.weaponName));

        Weapon w = newPlayer.getCurrentWeapon();
        w.setArray(weaponLoader.getWeaponByName(state.weaponName).getArray());
        w.convertTxtRegToSprite();
        if (w instanceof MagicWeapon) {
            ((MagicWeapon) w).setCurrentMap(mapManager);
            ((MagicWeapon) w).setProj(((MagicWeapon) weaponLoader.getWeaponByName(w.getName())).getProjectileTexture());
        }
        if (w instanceof RangedWeapon) {
            ((RangedWeapon) w).setProj(((RangedWeapon) weaponLoader.getWeaponByName(w.getName())).getProjectileTexture());
        }

        if (state.currentChestplate != null) {
        newPlayer.addArmor(armorLoader.deepCopyArmor(state.currentHelmet), 1);
        Armor helmet = newPlayer.getInventory().getArmorByName(state.currentHelmet);

            helmet.setTextureRegion(armorLoader.getArmorByName(helmet.getName()).getTextureRegion());
            newPlayer.equipArmor(helmet);
        } else {
            System.err.println("Helmet not found for new player: " + state.currentHelmet);
        }

        if (state.currentChestplate != null) {
        newPlayer.addArmor(armorLoader.deepCopyArmor(state.currentChestplate), 1);
        Armor chest = newPlayer.getInventory().getArmorByName(state.currentChestplate);

            chest.setTextureRegion(armorLoader.getArmorByName(chest.getName()).getTextureRegion());
            newPlayer.equipArmor(chest);
        }

        newPlayer.setId(state.id);
        newPlayer.setHp(state.hp);


        newPlayer.setTextureArray(PLAYER.getTextureArray());


        newPlayer.setRespPoint(mapManager.getRespPlayer());

        newPlayer.getCurrentWeapon().setEntity(newPlayer);

        playersById.put(state.id, newPlayer);
        PLAYERS.add(newPlayer);
        touchEvents.setISinglePlayer(false);
    }

    public void removePlayerById(int id){
        playersById.remove(id);
        PLAYERS.removeIf(player -> player.getID() == id);
    }

    public void setEnemyPos(Network.EnemyPos ed){


        EnemiesMap.get(ed.id).setPosition(ed.X*GameConstants.Sprite.SIZE, ed.Y*GameConstants.Sprite.SIZE);

    }


    public void dealDamge(Network.CurrentHp dealedDamageToPlayer){
        if(dealedDamageToPlayer.isEnemy){
            EnemiesMap.get(dealedDamageToPlayer.idOfEnemy).setHp(dealedDamageToPlayer.curHp);
        }
        else {
            playersById.get(dealedDamageToPlayer.idOfEnemy).setHp(dealedDamageToPlayer.curHp);
        }

          }


    public void update(float delta) {

        for(Player player :PLAYERS){
            player.noLogicMove();
            if (player.getCurrentWeapon() instanceof RangedWeapon) {
                ((RangedWeapon) player.getCurrentWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            }
            if (player.getCurrentWeapon() instanceof MagicWeapon) {
                ((MagicWeapon) player.getCurrentWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            }

            if (player.getCurrentWeapon() instanceof RangedWeapon) {
                ((RangedWeapon) player.getCurrentWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            }
            if (player.getCurrentWeapon() instanceof MagicWeapon) {
                ((MagicWeapon) player.getCurrentWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            }

            if (player.getMovePlayer()) {
                player.updateAnim();
            }

            if (player.getCurrentWeapon().getAttacking()) {
                if ( player.getCurrentWeapon() instanceof MeleeWeapon) {
                    player.getCurrentWeapon().update(delta);
                    player.getCurrentWeapon().checkHitboxCollisionsMap(mapManager);
                }
                if (player.getCurrentWeapon() instanceof RangedWeapon) {
                    player.getCurrentWeapon().updateAnimation();
                    player.getCurrentWeapon().checkHitboxCollisionsMap(mapManager);
                }
            }

            if (player.getCurrentWeapon() instanceof MagicWeapon) {
                player.getCurrentWeapon().updateAnimation();
                player.getCurrentWeapon().checkHitboxCollisionsMap(mapManager);
            }

        }
        mapManager.setCameraValues(PLAYER.cameraX, PLAYER.cameraY);




        if (Gdx.input.justTouched()) {
            if(shop != null){
            shop.handleTouchInput(Gdx.input.getX(), Gdx.input.getY());}
            if(armorShop != null){
            armorShop.handleTouchInput(Gdx.input.getX(), Gdx.input.getY());}
            if(multiplayerUi != null && isInHub){
                multiplayerUi.handleTouchInput(Gdx.input.getX(), Gdx.input.getY());}
        }





        for (Enemy enemy : Enemies) {
            enemy.noLogicMove();
            enemy.updateAnimation();

            enemy.setPlayerPosX(GameConstants.GET_WIDTH/2 - GameConstants.Sprite.SIZE/2-getClosestPlayer(enemy).cameraX);
            enemy.setPlayerPosY(GameConstants.GET_HEIGHT/2 - GameConstants.Sprite.SIZE/2-getClosestPlayer(enemy).cameraY);

            if(enemy.getWeapon() instanceof MagicWeapon){
                ((MagicWeapon) enemy.getWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            }
            if(enemy.getWeapon() instanceof RangedWeapon){
                ((RangedWeapon) enemy.getWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            }


            if (enemy.getWeapon().getAttacking()) {
                if(enemy.getWeapon() instanceof MeleeWeapon){
                    enemy.getWeapon().update(delta);
                    enemy.getWeapon().checkHitboxCollisionsMap(mapManager);}

            }
            if(enemy.getWeapon() instanceof RangedWeapon){
                enemy.getWeapon().updateAnimation();
                enemy.getWeapon().checkHitboxCollisionsMap(mapManager);
            }

            if(enemy.getWeapon() instanceof MagicWeapon){
                enemy.getWeapon().update(delta);
                enemy.getWeapon().checkHitboxCollisionsMap(mapManager);
            }

        }

        if(shop !=null){
        shop.update(PLAYER.hitbox);}
        if(armorShop != null) {
            armorShop.update(PLAYER.hitbox);
        }
    }

    public void equipArmor(Network.OnPlayerEquipArmor armor){
        Player p = playersById.get(armor.playerId);
        if( p !=null){
            if(!p.getInventory().containsArmorByName(armor.armorName)){
                Armor armor1 = armorLoader.deepCopyArmor(armor.armorName);
                p.addArmor(armor1, 1);
                armor1.setTextureRegion(armorLoader.getArmorByName(armor.armorName).getSpriteSheet());

            }
            p.equipArmor(p.getInventory().getArmorByName(armor.armorName));
        }
    }

    public void setPlayerPos(Network.PlayerPos pp){
        Player player = playersById.get(pp.id);
        if(player != null){
        player.setPosition(pp.x * GameConstants.Sprite.SIZE, pp.y*GameConstants.Sprite.SIZE);}
    }


    @Override
    public void show() {
    }

    private Player getClosestPlayer(Enemy enemy) {
        Player closest =  PLAYERS.get(0);
        float minDist = Float.MAX_VALUE;

        for (Player p : PLAYERS) {
            float dx = p.getEntityPos().x - enemy.getEntityPos().x;
            float dy = p.getEntityPos().y - enemy.getEntityPos().y;
            float dist = dx * dx + dy * dy;

            if (dist < minDist) {
                minDist = dist;
                closest = p;
            }
        }
        return closest;
    }

    @Override
    public void render(float delta) {
        gameRendering.render();


        if(isInHub){
        multiplayerUi.updateHubCountdiwns(delta);
        gameUI.handleTouch(Gdx.input.getX(), Gdx.input.getY());


        }

        else {
            multiplayerUi.update(delta);
            multiplayerUi.renderRoundCountdown(batch);
            multiplayerUi.renderScore(batch);
        }
        update(delta);
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
    }

    public Map<Integer, Enemy> getEnemiesMap(){
        return EnemiesMap;
    }

    public MapManaging getMapManager() {
        return mapManager;
    }

    public int getPlayerMoney(){
        return PLAYER.getMoney();
    }

    public Player getPLAYER(){
        return PLAYER;
    }

    public MultiplayerUi getMultiplayerUi(){
        return  multiplayerUi;
    }
}
