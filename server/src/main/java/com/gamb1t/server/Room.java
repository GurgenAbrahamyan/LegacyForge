package com.gamb1t.server;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.gamb1t.legacyforge.ManagerClasses.EnemyLoader;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.TouchManager;
import com.gamb1t.legacyforge.ManagerClasses.WeaponLoader;
import com.gamb1t.legacyforge.Networking.Network;
import com.gamb1t.legacyforge.Networking.Network.PlayerState;
import com.gamb1t.legacyforge.Networking.Network.EnemyState;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.ManagerClasses.GameUpdate;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.Structures.Shop;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.*;
import java.util.concurrent.*;

public class Room implements Runnable{

    public static final int MAX_PLAYERS = 10;
    private final int roomId;
    private final Server server;
    private final MapManaging mapManager;
    private final Shop shop;
    private ArrayList<Enemy> Enemies = new ArrayList<>();
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    public float playerX , playerY;

    private Random rand = new Random();
    private TouchManager touchEvents;


    private  WeaponLoader weaponLoader;

    private  WeaponLoader weaponLoader2;

    private static ArrayList<Player> PLAYERS = new ArrayList<Player>();

    private EnemyLoader enemyLoader;

    private ArrayList<Weapon> weapon;
    private ArrayList<Weapon> enemyWeapon;
    private boolean running = true;




    public String asset = "assets\\";





    private GameUpdate gameUpdate;

    private final Map<Integer, Player> playersById = new HashMap<>();

    public Room(int roomId, Server server) {


        GameConstants.init();

        playerX = (float) GET_WIDTH /2;
        playerY = (float) GET_HEIGHT /2;
        this.roomId = roomId;
        this.server = server;
        weaponLoader = new WeaponLoader(asset+"weapons.json", false);
        weaponLoader2 = new WeaponLoader(asset+"enemyWeapons.json", false);
        weapon = weaponLoader.getWeaponList();

        mapManager = new MapManaging(asset+"1room.txt", asset+"1roomHitbox.txt", "Tiles/Dungeon_Tileset.png", 30, 30);

        System.out.println(GET_WIDTH + "" +GET_HEIGHT);




        enemyWeapon = weaponLoader2.getWeaponList();



        enemyLoader = new EnemyLoader(PLAYERS,enemyWeapon, asset+"enemies.json", mapManager.getRespEenemy(), mapManager);






        Enemies = enemyLoader.getEnemyList();


        int i = 1;
        for (Enemy enemy : Enemies) {
             enemy.getWeapon().setEntity(enemy);
            if(enemy.getWeapon() instanceof RangedWeapon){
                ((RangedWeapon) enemy.getWeapon()).setMap(mapManager);
             //enemy.getWeapon().setServer(server);
            }

            enemy.setServer(server);
            enemy.setId(i);

            System.out.println("Enemy is using" + enemy.getWeapon().getName() + " and its id is " + enemy.getId());
            i++;
        }






        shop = new Shop(mapManager.getShopCoordinates().x,  mapManager.getShopCoordinates().y,
            GameConstants.Sprite.SIZE*4, GameConstants.Sprite.SIZE*3, "shops/basic_shop.png", weaponLoader, null, null);



        for (Weapon w : weapon) {

            if(w instanceof MagicWeapon){
                ((MagicWeapon)  w).setCurrentMap(mapManager);
            }
            w.setServer(server);
        }
        for (Weapon w : enemyWeapon) {

            if(w instanceof MagicWeapon){
                ((MagicWeapon)  w).setCurrentMap(mapManager);
            }
            w.setServer(server);
        }

        gameUpdate = new GameUpdate(Enemies, PLAYERS, mapManager, shop);

        gameUpdate.setServ(server);


    }

    public boolean isFull() {
        return PLAYERS.size() >= MAX_PLAYERS;
    }

    public void addConnection(Connection c, Network.PlayerInitMessage newPlayer) {
        Player p = new Player(newPlayer.name, newPlayer.level, newPlayer.experience, newPlayer.money, playerX, playerY, weapon.get(2), mapManager);
        p.setRespPoint(mapManager.getRespPlayer());
        p.setId(c.getID());
        p.getCureentWeapon().setEntity(p);
        playersById.put(p.getID(), p);
        p.setServer(server);

        broadcastState(c, p);

        PLAYERS.add(p);
    }

    @Override
    public void run() {
        final int targetFPS = 60;
        final long frameTime = 1_000_000_000 / targetFPS;
        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            float delta = (now - lastTime) / 1_000_000_000f;
            lastTime = now;

            if(!PLAYERS.isEmpty()) {
                gameUpdate.update(delta);
            }
            long sleep = frameTime - (System.nanoTime() - now);
            if (sleep > 0) {
                try {
                    Thread.sleep((sleep / 1_000_000), (int) (sleep % 1_000_000));
                } catch (InterruptedException ignored) {}
            }
        }
    }

    public void startAttack(Network.AttackStartPacket packet){
       Weapon currentWeapon =  playersById.get(packet.id).getCureentWeapon();


        server.sendToAllExceptTCP(packet.id, packet);

        if(currentWeapon instanceof RangedWeapon ){
            currentWeapon.setAttacking(true);
            ((RangedWeapon) currentWeapon).setAnimOver(true);
            currentWeapon.setAiming(true);
        }


    }

    public void attackDragged(Network.AttackDragged packet){
        Weapon curWeapon = playersById.get(packet.id).getCureentWeapon();
        if(curWeapon instanceof RangedWeapon){

            curWeapon.setRotation(packet.angle);

        }
        curWeapon.setAttacking(true);

        server.sendToAllExceptTCP(packet.id, packet);
    }

    public void endAttack(Network.AttackReleasePacket packet){
        Weapon curWeapon = playersById.get(packet.id).getCureentWeapon();

        curWeapon.setRotation(packet.angle);
        if(curWeapon.getAiming()){

            curWeapon.attack();



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

        server.sendToAllExceptTCP(packet.id, packet);
    }

    public void removeConnection(Connection c) {
        PLAYERS.removeIf(p -> p.getID()==c.getID());
    }

    public void applyMove(Network.MovePacket movePacket, Connection c) {
        System.out.println(movePacket.id);
        Player p = playersById.get(movePacket.id);;
        if (p != null) {
            System.out.println("player is moving!!!!1");
            p.setPlayerMoveTrue(new Vector2(movePacket.x, movePacket.y));
           // broadcastMoveTrue(c, new Network.PlayerPos(p.getEntityPos().x/GameConstants.Sprite.SIZE, p.getEntityPos().y/GameConstants.Sprite.SIZE, p.getID()));
        }

    }

    public void cancleMove(Network.StopPlayerMove stopPlayerMove, Connection connection){
        Player p = playersById.get(stopPlayerMove.id);
        if(p!=null){
            p.setPlayerMoveFalse();
          //  broadcastMoveStop(stopPlayerMove , connection);
        }
    }




    private void broadcastState(Connection c, Player connectedPlayer) {
        Network.StateMessageOnConnection msg = new Network.StateMessageOnConnection();
        msg.newPlayerId = connectedPlayer.getID();
        msg.weaponName = connectedPlayer.getCureentWeapon().getName();
        msg.players = new ArrayList<>();
        if(!PLAYERS.isEmpty()){
        for (Player p : PLAYERS) {

            PlayerState ps = new PlayerState();
            ps.id = p.getID();
            ps.x = p.getEntityPos().x/GameConstants.Sprite.SIZE;
            ps.y = p.getEntityPos().y/GameConstants.Sprite.SIZE;
            ps.level= p.getLevel();

            System.out.println(ps.id);
            ps.experience = p.getExperience();
            ps.hp = (int)p.getHp();
            ps.money = p.getMoney();
            ps.weaponName = p.getCureentWeapon().getName();
            ps.name = p.getName();
            msg.players.add(ps);
        }}
        msg.enemies = new ArrayList<>();

        for (Enemy e : Enemies) {
            EnemyState es = new EnemyState();
            es.id = e.getId();
            System.out.println(es.id);
            es.x = e.getEntityPos().x/GameConstants.Sprite.SIZE;
            es.y = e.getEntityPos().y/GameConstants.Sprite.SIZE;
            System.out.println(es.x+" "+es.y);
            es.dirX = e.getDirX();
            es.dirY = e.getDirY();
            es.hp = (int) e.getHp();
            es.speed = e.getSpeed();
            msg.enemies.add(es);
        }

        msg.shopInfo.height = shop.getShopHeight()/GameConstants.Sprite.SIZE;
        msg.shopInfo.width = shop.getShopWidth()/GameConstants.Sprite.SIZE;
        msg.shopInfo.x = shop.getShopX()/GameConstants.Sprite.SIZE;
        msg.shopInfo.y = shop.getShopY()/GameConstants.Sprite.SIZE;
        msg.shopInfo.renderPath = shop.getShopTexture();
        msg.shopInfo.weaponsJson = shop.getWeaponPath().replace(asset, "");

        msg.mapInfo.hitboxPath = mapManager.getHitboxFile().replace(asset, "");
        msg.mapInfo.mapPath = mapManager.getMapName().replace(asset, "");
        msg.mapInfo.renderPath = mapManager.getTilesSpritesheet();
        msg.mapInfo.width = mapManager.getMapWidth();
        msg.mapInfo.height = mapManager.getMapHeight();

        msg.playerWeaponJson = weaponLoader.getRecourcePath().replace(asset, "");
        msg.enemyWeaponJson = weaponLoader2.getRecourcePath().replace(asset, "");


        c.sendTCP(msg);

        PlayerState ps =new PlayerState();
        ps.name = connectedPlayer.getName();
        ps.level = connectedPlayer.getLevel();
        ps.experience = connectedPlayer.getExperience();
        ps.money = connectedPlayer.getMoney();
        ps.weaponName = connectedPlayer.getCureentWeapon().getName();
        ps.id = connectedPlayer.getID();
        ps.x = connectedPlayer.getEntityPos().x/GameConstants.Sprite.SIZE;
        ps.y = connectedPlayer.getEntityPos().y/GameConstants.Sprite.SIZE;

        server.sendToAllExceptTCP(c.getID(), ps );



    }

    public ArrayList<Player> getPlayer(){
        return  PLAYERS;
    }
}
