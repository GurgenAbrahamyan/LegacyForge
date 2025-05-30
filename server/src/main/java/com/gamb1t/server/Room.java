package com.gamb1t.server;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.ManagerClasses.ArmorLoader;
import com.gamb1t.legacyforge.ManagerClasses.EnemyLoader;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.TouchManager;
import com.gamb1t.legacyforge.ManagerClasses.WeaponLoader;
import com.gamb1t.legacyforge.Networking.ConnectionManager;
import com.gamb1t.legacyforge.Networking.Network;
import com.gamb1t.legacyforge.Networking.Network.PlayerState;
import com.gamb1t.legacyforge.Networking.Network.EnemyState;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.ManagerClasses.GameUpdate;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.Structures.ArmorShop;
import com.gamb1t.legacyforge.Structures.Shop;
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.*;
import java.util.concurrent.*;

public class Room implements Runnable{

    private String gameMode;
    private int roomId;

    public static final int MAX_PLAYERS = 10;
    private final Server server;
    private final MapManaging mapManager;
    private final Shop shop;
    private final ArmorShop armorShop;
    private ArrayList<Enemy> Enemies = new ArrayList<>();

    public float playerX , playerY;

    private Random rand = new Random();
    private TouchManager touchEvents;


    private  WeaponLoader weaponLoader;

    private  WeaponLoader weaponLoader2;
    private ArmorLoader armorLoader;

    private static List<Player> PLAYERS;

    private EnemyLoader enemyLoader;

    private ArrayList<Weapon> weapon;
    private ArrayList<Weapon> enemyWeapon;
    private boolean running = true;


    private RoomManager roomManager;



    public String asset = "assets\\";


    private Squad nextSquad;


    private GameUpdate gameUpdate;

    private final Map<Integer, Player> playersById = new HashMap<>();
    private final Map<Integer, Connection> playersByIdConnection = new HashMap<>();

    public Room(int roomId, String hub, Server server, RoomManager roomManagerManager) {


        this.roomManager = roomManagerManager;
        GameConstants.init();

        playerX = (float) GET_WIDTH /2;
        playerY = (float) GET_HEIGHT /2;
        this.gameMode = hub;
        this.roomId = roomId;
        this.server = server;
        PLAYERS = new CopyOnWriteArrayList<>();
        weaponLoader = new WeaponLoader(asset+"weapons.json", false);
        weaponLoader2 = new WeaponLoader(asset+"enemyWeapons.json", false);
        armorLoader = new ArmorLoader(asset + "armor.json");

        weapon = weaponLoader.getWeaponList();

        mapManager = new MapManaging(asset+"1room.txt", asset+"1roomHitbox.txt", "Tiles/Dungeon_Tileset.png", 30, 30);

        System.out.println(GET_WIDTH + "" +GET_HEIGHT);





        enemyWeapon = weaponLoader2.getWeaponList();



        enemyLoader = new EnemyLoader(PLAYERS,enemyWeapon, asset+"enemies.json", mapManager.getRespEnemy(), mapManager);






        Enemies = enemyLoader.getEnemyList();


        int i = 1;
        for (Enemy enemy : Enemies) {
             enemy.getWeapon().setEntity(enemy);
            if(enemy.getWeapon() instanceof RangedWeapon){
                ((RangedWeapon) enemy.getWeapon()).setMap(mapManager);
                enemy.getWeapon().setServer(server, gameMode, roomId);
            }

            enemy.setServer(server, roomId, gameMode);
            enemy.setId(i);

            System.out.println("Enemy is using" + enemy.getWeapon().getName() + " and its id is " + enemy.getId());
            i++;
        }






        shop = new Shop(mapManager.getShopCoordinates().x,  mapManager.getShopCoordinates().y,
            GameConstants.Sprite.SIZE*4, GameConstants.Sprite.SIZE*3, "shops/basic_shop.png", weaponLoader, null, null);

        armorShop = new ArmorShop(mapManager.getArmorShopCoordinates().x,  mapManager.getArmorShopCoordinates().y,
            GameConstants.Sprite.SIZE*4, GameConstants.Sprite.SIZE*3, "shops/armor_shop_sprite.png", armorLoader, null, touchEvents);


        for (Weapon w : weapon) {

            if(w instanceof MagicWeapon){
                ((MagicWeapon)  w).setCurrentMap(mapManager);
            }
            w.setServer(server, gameMode, roomId);
        }
        for (Weapon w : enemyWeapon) {

            if(w instanceof MagicWeapon){
                ((MagicWeapon)  w).setCurrentMap(mapManager);
            }
            w.setServer(server, gameMode, roomId);
        }

        gameUpdate = new GameUpdate(Enemies, PLAYERS, mapManager, shop, armorShop);

        gameUpdate.setServ(server, gameMode, roomId);


    }

    public boolean isFull() {
        return PLAYERS.size() >= MAX_PLAYERS;
    }

    public void addConnection(Connection c, User newPlayer) {
        Player p = new Player(newPlayer.nickname, newPlayer.level, newPlayer.experience, newPlayer.money, mapManager.getRespPlayer().x+GameConstants.Sprite.SIZE/2, mapManager.getRespPlayer().y+GameConstants.Sprite.SIZE/2, null, mapManager);
        p.setRespPoint(mapManager.getRespPlayer());

        p.setId(c.getID());

        p.setServer(server, roomId, gameMode);

        p.addInventoryWeapons(weaponLoader.getWeaponsFromMap(newPlayer.items.weapons));

        p.setCurrentWeapon(p.getInventory().getWeaponByName((String) newPlayer.items.weapons.get(newPlayer.equippedWeapon).get("name")));

        p.getCurrentWeapon().setEntity(p);
        playersById.put(p.getID(), p);
        playersByIdConnection.put(p.getID(), c);

        System.out.println("Player init");

        p.setFirebaseId(newPlayer.firebaseId);
        p.setLoses(newPlayer.losses);
        p.setWins(newPlayer.wins);
        p.setRating(newPlayer.rating);

        for(Weapon weapon1 : p.getInventory().getWeapons()){
            System.out.println(weapon1.getName());
            weapon1.setServer(server, gameMode, roomId);
            weapon1.setFirebaseId(newPlayer.items.getFireBaseIdWeaponByName(weapon1.getName()));
            if(weapon1 instanceof  MagicWeapon){
                ((MagicWeapon) weapon1).setCurrentMap(mapManager);
            }
        }
        p.addInventoryArmors(
            armorLoader.getArmorsFromMap(newPlayer.items.armor)
        );
        if(newPlayer.equippedArmorHelmet != null) {
            Armor helmet = p.getInventory().getArmorByName((String) newPlayer.items.armor.get(newPlayer.equippedArmorHelmet).get("name"));
            p.equipArmor(helmet);}

        if(newPlayer.equippedArmorChestPlate != null) {
            Armor chest = p.getInventory().getArmorByName((String) newPlayer.items.armor.get(newPlayer.equippedArmorChestPlate).get("name"));
            p.equipArmor(chest);

        }
        broadcastState(c, p, newPlayer);


        PLAYERS.add(p);
    }
    public void handlePvpRequest(Network.PvpRequest request, Connection c) {
        Player p = playersById.get(c.getID());
        if (p == null) {
            System.err.println("Player not found for connection ID: " + c.getID());
            return;
        }
        if (!"1v1".equals(request.mode)) {
            System.err.println("Invalid PvP mode: " + request.mode);
            return;
        }

        Room room = roomManager.getRoomFor(c);
        if (room == null || room.getRoomId() != this.roomId) {
            System.err.println("Player " + c.getID() + " not in room " + roomId);
            return;
        }

        User user = roomManager.convertPlayerToUser(p);
        roomManager.queueForOneVsOne(c, user);
        System.out.println("Player " + p.getID() + " requested 1v1, added to queue");
    }




    public void startAttack(Network.AttackStartPacket packet){
        Weapon currentWeapon =  playersById.get(packet.id).getCurrentWeapon();




        ConnectionManager.sendToConnectionsExcept(gameMode, roomId, packet.id, packet);

        if(currentWeapon instanceof RangedWeapon ){
            currentWeapon.setAttacking(true);
            ((RangedWeapon) currentWeapon).setAnimOver(true);
            currentWeapon.setAiming(true);
        }


    }

    public void handleSquadAction(int playerId, String action) {
        Player player = playersById.get(playerId);
        Connection connection = playersByIdConnection.get(playerId);
        if (player == null || connection == null) {
            System.err.println("Invalid player or connection for playerId: " + playerId);
            return;
        }

        if (action.equals("join")) {
            if (nextSquad == null) {
                nextSquad = new Squad(player, connection);
            } else if (!nextSquad.isFull()) {
                nextSquad.addMember(player, connection);
            } else {
                Network.SquadUpdate update = new Network.SquadUpdate();
                update.inSquad = false;
                update.countdown = -1;
                update.memberNames = new ArrayList<>();
                server.sendToTCP(playerId, update);
                return;
            }

            for (Connection conn : playersByIdConnection.values()) {
                Network.SquadUpdate update = new Network.SquadUpdate();
                update.inSquad = nextSquad.getMembersConnections().containsKey(playersById.get(conn.getID()));
                update.countdown = nextSquad.getCountdown();
                update.memberNames = new ArrayList<>();
                for (Player p : nextSquad.getMembersConnections().keySet()) {
                    update.memberNames.add(p.getName());
                }
                server.sendToTCP(conn.getID(), update);
            }
        } else if (action.equals("leave")) {
            if (nextSquad != null) {
                nextSquad.removeMember(player, connection);

                for (Connection conn : playersByIdConnection.values()) {
                    Network.SquadUpdate update = new Network.SquadUpdate();
                    update.inSquad = nextSquad != null && nextSquad.getMembersConnections().containsKey(playersById.get(conn.getID()));
                    update.countdown = nextSquad != null ? nextSquad.getCountdown() : -1;
                    update.memberNames = new ArrayList<>();
                    if (nextSquad != null) {
                        for (Player p : nextSquad.getMembersConnections().keySet()) {
                            update.memberNames.add(p.getName());
                        }
                    }
                    server.sendToTCP(conn.getID(), update);
                }

                if (nextSquad.getMembersConnections().isEmpty()) {
                    nextSquad = null;
                }
            }
        }
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

            if (!PLAYERS.isEmpty()) {
                gameUpdate.update(delta);
            }

            if(nextSquad!= null){
                nextSquad.update(delta);
                if (nextSquad.isActive() && nextSquad.getCountdown() <= 0) {
                   startDungeon();
                   nextSquad = null;
                }

            }


            long sleep = frameTime - (System.nanoTime() - now);
            if (sleep > 0) {
                try {
                    Thread.sleep((sleep / 1_000_000), (int) (sleep % 1_000_000));
                } catch (InterruptedException ignored) {}
            }
        }
    }

    public void attackDragged(Network.AttackDragged packet){
        Weapon curWeapon = playersById.get(packet.id).getCurrentWeapon();
        if(curWeapon instanceof RangedWeapon){

            curWeapon.setRotation(packet.angle);

        }
        curWeapon.setAiming(true);

        ConnectionManager.sendToConnectionsExcept(gameMode, roomId, packet.id, packet);
    }

    public void endAttack(Network.AttackReleasePacket packet){
        Weapon curWeapon = playersById.get(packet.id).getCurrentWeapon();

        curWeapon.setRotation(packet.angle);
        if(curWeapon.getAiming()){

            curWeapon.attack();
            System.out.println("Attacked");



            curWeapon.setAttacking(true);
            if(curWeapon instanceof  RangedWeapon){
                ((RangedWeapon) curWeapon).setAnimOver(true);
            }
            if(curWeapon instanceof MagicWeapon){
                System.out.println("JUST DID FOR MAGIC");
                ((MagicWeapon) curWeapon).setAnimOver(true);
            }

            if( curWeapon instanceof RangedWeapon){
                ((RangedWeapon) curWeapon).setIsCharging(false);
            }

            curWeapon.setAiming(false);


        }

        ConnectionManager.sendToConnectionsExcept(gameMode, roomId, packet.id, packet);
    }


    public void removeConnection(Connection c) {
        Player p = playersById.get(c.getID());
        if (p != null) {
            PLAYERS.remove(p);
            playersById.remove(p.getID());
            playersByIdConnection.remove(p.getID());
            if (nextSquad != null) {
                nextSquad.removeMember(p, c);
                if (nextSquad.getMembersConnections().isEmpty()) {
                    nextSquad = null;
                }
            }
    }


    }

    public void addConnections(List<Connection> connections, List<User> users) {
        List<Player> newPlayers = new ArrayList<>();
        for (int i = 0; i < connections.size(); i++) {
            Connection c = connections.get(i);
            User newPlayer = users.get(i);
            Player p = new Player(newPlayer.nickname, newPlayer.level, newPlayer.experience, newPlayer.money, mapManager.getRespPlayer().x + GameConstants.Sprite.SIZE / 2, mapManager.getRespPlayer().y + GameConstants.Sprite.SIZE / 2, null, mapManager);
            p.setRespPoint(mapManager.getRespPlayer());
            p.setId(c.getID());
            p.setServer(server, roomId, gameMode);

            p.addInventoryWeapons(weaponLoader.getWeaponsFromMap(newPlayer.items.weapons));
            p.setCurrentWeapon(p.getInventory().getWeaponByName((String) newPlayer.items.weapons.get(newPlayer.equippedWeapon).get("name")));
            if (p.getCurrentWeapon() != null) {
                p.getCurrentWeapon().setEntity(p);
            }

            p.addInventoryArmors(armorLoader.getArmorsFromMap(newPlayer.items.armor));
            System.out.println(newPlayer.equippedArmorHelmet + " is the helmet");
            Armor helmet = p.getInventory().getArmorByName((String) newPlayer.items.armor.get(newPlayer.equippedArmorHelmet).get("name"));
            if (helmet != null) {
                p.equipArmor(helmet);
            }
            if (newPlayer.equippedArmorChestPlate != null && !newPlayer.equippedArmorChestPlate.isEmpty()) {
                Armor chest = p.getInventory().getArmorByName((String) newPlayer.items.armor.get(newPlayer.equippedArmorChestPlate).get("name"));
                if (chest != null) {
                    p.equipArmor(chest);
                }
            }
            synchronized (this) {
                playersById.put(p.getID(), p);
                playersByIdConnection.put(p.getID(), c);
                newPlayers.add(p);
                PLAYERS.add(p);
            }
            System.out.println("Added player " + p.getID() + " to dungeon " + roomId + ", PLAYERS size: " + PLAYERS.size());
            for (Weapon weapon : p.getInventory().getWeapons()) {
                System.out.println("Player " + p.getID() + " weapon: " + weapon.getName());
                weapon.setServer(server, gameMode, roomId);
                if (weapon instanceof MagicWeapon) {
                    ((MagicWeapon) weapon).setCurrentMap(mapManager);
                }
            }
        }
        for (int i = 0; i < connections.size(); i++) {
            broadcastState(connections.get(i), newPlayers.get(i), users.get(i));
        }
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

    public void playerAddNewWeapon(Network.OnPlayerBoughtWeapon o, Connection c){
        Player p = playersById.get(o.playerId);
        if(p!=null){
            p.addWeapon(weaponLoader.deepCopyWeapon(o.weaponName), o.lvl);
            p.getCurrentWeapon().setServer(server, gameMode, roomId);
        }
    }
    public void playerEquipWeapon(Network.OnPlayerEquipWeapon o, Connection c){
        Player p = playersById.get(o.playerId);
        if(p!=null){
            p.setCurrentWeapon(p.getInventory().getWeaponByName(o.weaponName));
            System.out.println("Setting "+ p.getCurrentWeapon().getName());
            p.getCurrentWeapon().setServer(server, gameMode, roomId);
            if( p.getCurrentWeapon() instanceof  MagicWeapon){
                ((MagicWeapon)  p.getCurrentWeapon()).setCurrentMap(mapManager);
            }


            ConnectionManager.sendToConnectionsExcept(gameMode, roomId, c.getID(), o );
        }
    }
    public void removeFor1v1(List<Connection> connections, CopyOnWriteArrayList<Player> players ){

        for (Player player : players) {
            Connection connection = playersByIdConnection.get(player.getID());
            if (connection != null) {
                removeConnection(connection);
            }
        }
    }



    public void startDungeon() {
        List<Connection> connections = new ArrayList<>(nextSquad.getMembersConnections().values());
         CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<>(nextSquad.getMembersConnections().keySet());

        roomManager.assignToDungeon(connections, players, nextSquad);

        for (Player player : players) {
            Connection connection = playersByIdConnection.get(player.getID());
            if (connection != null) {
                removeConnection(connection);
            }
        }
        nextSquad = null;
    }



    public void playerAddNewArmor(Network.OnPlayerBoughtArmor o, Connection c){
        Player p = playersById.get(o.playerId);
        if(p!=null){
            p.addArmor(armorLoader.deepCopyArmor((o.weaponArmor)), o.lvl);
            p.setTextureArray(armorLoader.getArmorByName(o.weaponArmor).getTextureRegion());
        }
    }
    public void playerEquipArmor(Network.OnPlayerEquipArmor o, Connection c){
        Player p = playersById.get(o.playerId);
        if(p!=null){
            p.equipArmor(p.getInventory().getArmorByName(o.armorName));
            ConnectionManager.sendToConnectionsExcept(gameMode, roomId, c.getID(), o);
        }
    }





    private void broadcastState(Connection c, Player connectedPlayer, User user) {
        Network.StateMessageOnConnection msg = new Network.StateMessageOnConnection();

        msg.gameMode = "Hub";
        msg.newPlayerId = connectedPlayer.getID();
        msg.weaponName = connectedPlayer.getCurrentWeapon().getName();

        if(connectedPlayer.getEquipment()!= null  && connectedPlayer.getEquipment().getHelmet() != null) {
            msg.currentHelmet = connectedPlayer.getEquipment().getHelmet().getName();
        }
        System.out.println( connectedPlayer.getEquipment().getChestplate().getName()+" is not null");
        if(connectedPlayer.getEquipment()!= null  && connectedPlayer.getEquipment().getChestplate() != null){

           msg.currentChestplate = connectedPlayer.getEquipment().getChestplate().getName();}
        msg.jsonPath = enemyLoader.getJsonPath().replace(asset, "");
        msg.players = new ArrayList<>();
        msg.curX = connectedPlayer.getEntityPos().x/GameConstants.Sprite.SIZE;
        msg.curY = connectedPlayer.getEntityPos().y/GameConstants.Sprite.SIZE;
        msg.user = user;
        if(!PLAYERS.isEmpty()){
        for (Player p : PLAYERS) {

            if(c.getID() != p.getID()){
            PlayerState ps = new PlayerState();
            ps.id = p.getID();
            ps.x = p.getEntityPos().x/GameConstants.Sprite.SIZE;
            ps.y = p.getEntityPos().y/GameConstants.Sprite.SIZE;
            ps.level= p.getLevel();
            ps.currentHelmet = p.getEquipment().getHelmet().getName();
            ps.currentChestplate = p.getEquipment().getChestplate().getName();

            ps.experience = (int) p.getExperience();
            ps.hp = (int)p.getHp();
            ps.money = p.getMoney();
            ps.weaponName = p.getCurrentWeapon().getName();
            ps.name = p.getName();
            msg.players.add(ps);}
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

        msg.shopInfo = new Network.ShopInfo();
        msg.shopInfo.height = shop.getShopHeight()/GameConstants.Sprite.SIZE;
        msg.shopInfo.width = shop.getShopWidth()/GameConstants.Sprite.SIZE;
        msg.shopInfo.x = shop.getShopX()/GameConstants.Sprite.SIZE;
        msg.shopInfo.y = shop.getShopY()/GameConstants.Sprite.SIZE;
        msg.shopInfo.renderPath = shop.getShopTexture();
        msg.shopInfo.weaponsJson = shop.getWeaponPath().replace(asset, "");

        msg.armorShopInfo = new Network.ShopInfo();
        msg.armorShopInfo.height = armorShop.getShopHeight()/GameConstants.Sprite.SIZE;
        msg.armorShopInfo.width = armorShop.getShopWidth()/GameConstants.Sprite.SIZE;
        msg.armorShopInfo.x = armorShop.getShopX()/GameConstants.Sprite.SIZE;
        msg.armorShopInfo.y = armorShop.getShopY()/GameConstants.Sprite.SIZE;
        msg.armorShopInfo.renderPath = armorShop.getShopTexture();
        msg.armorShopInfo.weaponsJson = armorShop.getArmorPath().replace(asset, "");

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
        ps.experience = (int) connectedPlayer.getExperience();
        ps.money = connectedPlayer.getMoney();
        ps.weaponName = connectedPlayer.getCurrentWeapon().getName();
        ps.id = connectedPlayer.getID();
        ps.x = connectedPlayer.getEntityPos().x/GameConstants.Sprite.SIZE;
        ps.y = connectedPlayer.getEntityPos().y/GameConstants.Sprite.SIZE;
        if(connectedPlayer.getEquipment() != null && connectedPlayer.getEquipment().getHelmet() !=null){
            System.out.println("helmet is initialized");
        ps.currentHelmet = connectedPlayer.getEquipment().getHelmet().getName();
        }

        if(connectedPlayer.getEquipment() != null && connectedPlayer.getEquipment().getChestplate() !=null){
            System.out.println("chestplate");
        ps.currentChestplate = connectedPlayer.getEquipment().getChestplate().getName();
        }

        ConnectionManager.sendToConnectionsExcept(gameMode, roomId, c.getID(), ps);



    }

    public List<Player> getPlayer(){
        return  PLAYERS;
    }
    public int getRoomId(){
        return  roomId;
    }
}
