package com.gamb1t.clientside;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Client;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.GameRendering;
import com.gamb1t.legacyforge.ManagerClasses.GameUI;
import com.gamb1t.legacyforge.ManagerClasses.TouchManager;
import com.gamb1t.legacyforge.ManagerClasses.WeaponLoader;
import com.gamb1t.legacyforge.Networking.Network;
import com.gamb1t.legacyforge.Networking.NetworkInputSender;
import com.gamb1t.legacyforge.Structures.Shop;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.MeleeWeapon;
import com.gamb1t.legacyforge.Weapons.Projectile;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ClientGameScreen implements Screen {
    public float playerX = (float) GET_WIDTH / 2, playerY = (float) GET_HEIGHT / 2;

    private Random rand = new Random();
    private TouchManager touchEvents;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;



    private static ArrayList<Enemy> Enemies = new ArrayList<>();
    private static Map<Integer, Enemy> EnemiesMap = new HashMap<>();

    private  WeaponLoader weaponLoader;

    private  WeaponLoader weaponLoader2;

    private static ArrayList<Player> PLAYERS = new ArrayList<>();


    private ArrayList<Weapon> weapon;
    private ArrayList<Weapon> enemyWeapon;

    private NetworkInputSender networkInputSender;

    private Shop shop;

    Player PLAYER;
    private AssetManager assetManager;


    public MapManaging mapManager;

    private Client client;


    private GameUI gameUI;

    Map<Integer, Player> playersById = new HashMap<>();
    private GameRendering gameRendering;


    public ClientGameScreen(String name, int experience, int level, int money, Network.StateMessageOnConnection connection, Client client, AssetManager assetManager) {


        this.assetManager= assetManager;


        this.client = client;
        mapManager = new MapManaging(connection.mapInfo.mapPath, connection.mapInfo.hitboxPath, connection.mapInfo.renderPath, connection.mapInfo.width, connection.mapInfo.height);

        mapManager.initializeOutside();


        weaponLoader = new WeaponLoader(connection.playerWeaponJson, true);
        weaponLoader2 = new WeaponLoader(connection.enemyWeaponJson, true);


        weapon=  weaponLoader.getWeaponList();

        PLAYER= new Player(name,  level, experience, money, playerX, playerY, weaponLoader.getWeaponByName(connection.weaponName), mapManager);

        PLAYER.setId(connection.newPlayerId);

        PLAYER.getCureentWeapon().setEntity(PLAYER);

        PLAYER.setPosition(playerX, playerY);

        playersById.put((connection.newPlayerId), PLAYER);

        PLAYERS.add(PLAYER);




        if (connection.players != null) {
            for (Network.PlayerState ps : connection.players) {
                Player other = new Player(
                    ps.name, ps.level, ps.experience, ps.money,
                    ps.x * GameConstants.Sprite.SIZE,
                    ps.y * GameConstants.Sprite.SIZE,
                    weaponLoader.getWeaponByName(ps.weaponName),
                    mapManager
                );
                other.setId(ps.id);
                PLAYERS.add(other);
                System.out.println(ps.id);
                playersById.put(ps.id, other);
            }
        }


        for(Player player : PLAYERS){
            player.setTexture("player_sprites/player_spritesheet.png");
            player.setRespPoint(mapManager.getRespPlayer());
        }

        networkInputSender = new NetworkInputSender(PLAYER, PLAYER.getCureentWeapon(), client);


        enemyWeapon = weaponLoader2.getWeaponList();




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
                enemy1.setTexture("enemies_spritesheet/skeleton_spritesheet.png");
                enemy1.getWeapon().setEntity(enemy1);
                enemy1.setPlayer(PLAYER);
                if (enemy1.getWeapon() instanceof RangedWeapon) {
                    ((RangedWeapon) enemy1.getWeapon()).setMap(mapManager);
                }
            }


        }


        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();


        gameUI = new GameUI();
        InputMultiplexer multiplexer = new InputMultiplexer();
        touchEvents = new TouchManager(PLAYER, PLAYER.getCureentWeapon(), networkInputSender);
        multiplexer.addProcessor(gameUI.getStage());
        multiplexer.addProcessor(touchEvents);
        Gdx.input.setInputProcessor(multiplexer);

       shop = new Shop(mapManager.getShopCoordinates().x,
           mapManager.getShopCoordinates().y,
            connection.shopInfo.width*GameConstants.Sprite.SIZE,
           connection.shopInfo.height*GameConstants.Sprite.SIZE,
           connection.shopInfo.renderPath, /*connection.shopInfo.weaponsJson*/ weaponLoader, PLAYER, touchEvents);

       shop.initializeRendeingObjects();

        for (Weapon w : shop.getWeaponList()) {

            w.setTexture(w.getSprite());
            w.convertTxtRegToSprite();
            if(w instanceof MagicWeapon){
                ((MagicWeapon)  w).setCurrentMap(mapManager);
            }
        }

        weapon.get(0).setTexture(weapon.get(0).getSprite());
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

        touchEvents.setIsMultiplayer(true);
        gameRendering = new GameRendering(batch, shapeRenderer, font, PLAYER, Enemies, PLAYERS, mapManager, shop, touchEvents);
    }

    public void createProjectile(Network.CreateProjectileMessage projectileMessage){
        Weapon curWeapon;
        if(projectileMessage.isEnemy){
            curWeapon = EnemiesMap.get(projectileMessage.enemyId).getWeapon();
        }
        else {
            curWeapon = playersById.get(projectileMessage.enemyId).getCureentWeapon();
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
            curWeapon = p.getCureentWeapon();
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
            curWeapon = p.getCureentWeapon();
        }

        if (curWeapon instanceof RangedWeapon) {
            RangedWeapon rw = (RangedWeapon) curWeapon;
            for (Projectile proj : rw.getProjectiles()) {
                if (proj.getId() == msg.projectileId) {
                    System.out.println("enemy id for ranged" + msg.enemyId);
                    proj.setPosition(msg.x*GameConstants.Sprite.SIZE, msg.y*GameConstants.Sprite.SIZE);
                    break;
                }
            }
        }

        else if (curWeapon instanceof MagicWeapon) {
            MagicWeapon mw = (MagicWeapon) curWeapon;
            for (Projectile proj : mw.getProjectiles()) {
                if (proj.getId() == msg.projectileId) {
                    System.out.println("enemy id for magic" + msg.enemyId);
                    proj.setPosition(msg.x*GameConstants.Sprite.SIZE, msg.y*GameConstants.Sprite.SIZE);
                    break;
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
            curWeapon = playersById.get(destroyProjectileMessage.enemyId).getCureentWeapon();
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
        Weapon  currentWeapon =  playersById.get(packet.id).getCureentWeapon();

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

        Weapon curWeapon =  playersById.get(packet.id).getCureentWeapon();

        if(curWeapon instanceof RangedWeapon){

            curWeapon.setRotation(packet.angle);

        }
        curWeapon.setAttacking(true);
    }

    public void endAttack(Network.AttackReleasePacket packet){
        if(packet.isEnemy){
            EnemiesMap.get(packet.id).attackReleased(packet.angle);
            return;}
        Weapon curWeapon =  playersById.get(packet.id).getCureentWeapon();
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

        String skinPath = "player_sprites/player_spritesheet.png";

        if (!assetManager.isLoaded(skinPath)) {
            assetManager.load(skinPath, Texture.class);
        }

        Player newPlayer = new Player(
            state.name,
            state.level,
            state.experience,
            state.money,
            state.x * GameConstants.Sprite.SIZE,
            state.y * GameConstants.Sprite.SIZE,
            weaponLoader.getWeaponByName(state.weaponName), mapManager
        );

        newPlayer.setId(state.id);
        newPlayer.setHp(state.hp);


        newPlayer.setTextureArray(PLAYER.getTextureArray());


        newPlayer.setRespPoint(mapManager.getRespPlayer());

        newPlayer.getCureentWeapon().setEntity(newPlayer);

        playersById.put(state.id, newPlayer);
        PLAYERS.add(newPlayer);
    }

    public void removePlayerById(int id){
        playersById.remove(id);
        PLAYERS.removeIf(player -> player.getID() == id);
    }

    public void setEnemyPos(Network.EnemyPos ed){


        EnemiesMap.get(ed.id).setPosition(ed.X*GameConstants.Sprite.SIZE, ed.Y*GameConstants.Sprite.SIZE);

    }


    public void dealDamge(Network.CurentHp dealedDamageToPlayer){
        if(dealedDamageToPlayer.isEnemy == true){
            EnemiesMap.get(dealedDamageToPlayer.idOfEnemy).setHp(dealedDamageToPlayer.curHp);
        }
        else {
            playersById.get(dealedDamageToPlayer.idOfEnemy).setHp(dealedDamageToPlayer.curHp);
        }

          }


    public void update(float delta) {

        for(Player player :PLAYERS){
            player.noLogicMove();
            if (player.getCureentWeapon() instanceof RangedWeapon) {
                ((RangedWeapon) player.getCureentWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            }
            if (player.getCureentWeapon() instanceof MagicWeapon) {
                ((MagicWeapon) player.getCureentWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            }

            if (player.getCureentWeapon() instanceof RangedWeapon) {
                ((RangedWeapon) player.getCureentWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            }
            if (player.getCureentWeapon() instanceof MagicWeapon) {
                ((MagicWeapon) player.getCureentWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            }

            if (player.getMovePlayer()) {
                player.updateAnim();
            }

            if (player.getCureentWeapon().getAttacking()) {
                if ( player.getCureentWeapon() instanceof MeleeWeapon) {
                    player.getCureentWeapon().update(delta);
                    player.getCureentWeapon().checkHitboxCollisionsMap(mapManager);
                }
                if (player.getCureentWeapon() instanceof RangedWeapon) {
                    player.getCureentWeapon().updateAnimation();
                    player.getCureentWeapon().checkHitboxCollisionsMap(mapManager);
                }
            }

            if (player.getCureentWeapon() instanceof MagicWeapon) {
                player.getCureentWeapon().updateAnimation();
                player.getCureentWeapon().checkHitboxCollisionsMap(mapManager);
            }

        }
        mapManager.setCameraValues(PLAYER.cameraX, PLAYER.cameraY);




        if (Gdx.input.isTouched()) {
            shop.handleTouchInput(Gdx.input.getX(), Gdx.input.getY());
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

        shop.update(PLAYER.hitbox);
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
