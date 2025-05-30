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
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.*;
import java.util.concurrent.*;

public class Dungeon implements Runnable {

    public static final int MAX_PLAYERS = 10;

    protected int roomId;
    protected Server server;
    protected MapManaging mapManager;

    private ArrayList<Enemy> Enemies = new ArrayList<>();

    public float playerX, playerY;

    protected Random rand = new Random();

    protected WeaponLoader weaponLoader;
    protected WeaponLoader weaponLoader2;
    protected ArmorLoader armorLoader;

    protected List<Player> PLAYERS;
    protected EnemyLoader enemyLoader;

    protected ArrayList<Weapon> weapon;
    protected ArrayList<Weapon> enemyWeapon;
    protected volatile boolean running = true;

    public String asset = "assets\\";
    public String gameMode;

    protected RoomManager roomManager;

    private GameUpdate gameUpdate;

    protected final Map<Integer, Player> playersById = new ConcurrentHashMap<>(); // Thread-safe map
    protected final Map<Integer, Connection> playersConnections = new ConcurrentHashMap<>(); // Thread-safe map

    public Dungeon(int dungeonId, String roomName, Server server, RoomManager roomManager) {
        playerX = (float) GET_WIDTH / 2;
        playerY = (float) GET_HEIGHT / 2;

        GameConstants.init();
        this.roomManager = roomManager;
        this.gameMode = roomName;

        this.roomId = dungeonId;
        this.server = server;
        PLAYERS = new CopyOnWriteArrayList<>();
        weaponLoader = new WeaponLoader(asset + "weapons.json", false);
        weaponLoader2 = new WeaponLoader(asset + "dungeonEnemyWeapon.json", false);
        armorLoader = new ArmorLoader(asset + "armor.json");

        weapon = weaponLoader.getWeaponList();

        mapManager = new MapManaging(
            asset + "dungeonTextures.txt",
            asset + "dungeonHitboxes.txt",
            "Tiles/Dungeon_Tileset.png",
            30, 30
        );
        mapManager.setServer(server, roomName, dungeonId);

        enemyWeapon = weaponLoader2.getWeaponList();

        enemyLoader = new EnemyLoader(PLAYERS, enemyWeapon, asset + "dungeonEnemies.json", mapManager.getRespEnemy(), mapManager);
        Enemies = enemyLoader.getEnemyList();

        int i = 1;
        for (Enemy enemy : Enemies) {
            enemy.getWeapon().setEntity(enemy);
            if (enemy.getWeapon() instanceof RangedWeapon) {
                ((RangedWeapon) enemy.getWeapon()).setMap(mapManager);
            }
            enemy.setServer(server, roomId, roomName);
            enemy.setId(i++);
        }

        for (Weapon w : weapon) {
            if (w instanceof MagicWeapon) {
                ((MagicWeapon) w).setCurrentMap(mapManager);
            }
            w.setServer(server, roomName, roomId);
        }
        for (Weapon w : enemyWeapon) {
            if (w instanceof MagicWeapon) {
                ((MagicWeapon) w).setCurrentMap(mapManager);
            }
            w.setServer(server, roomName, roomId);
        }

        gameUpdate = new GameUpdate(Enemies, PLAYERS, mapManager, null, null);
        gameUpdate.setServ(server, roomName, roomId);
    }

    public boolean isFull() {
        return PLAYERS.size() >= MAX_PLAYERS;
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

            mapManager.update(delta);

            if (mapManager.getGoingBack()) {
                // Collect players to return
                List<Map.Entry<Integer, Connection>> playersToReturn;
                synchronized (playersConnections) {
                    playersToReturn = new ArrayList<>(playersConnections.entrySet());
                }
                System.out.println("Returning " + playersToReturn.size() + " players to hub in dungeon " + roomId);

                List<Connection> connections = new ArrayList<>();
                List<User> users = new ArrayList<>();
                for (Map.Entry<Integer, Connection> entry : playersToReturn) {
                    int playerId = entry.getKey();
                    Connection connection = entry.getValue();
                    Player player = playersById.get(playerId);
                    if (player != null && connection != null) {
                        connections.add(connection);
                        users.add(roomManager.convertPlayerToUser(player));
                    } else {
                        System.out.println("Skipped player ID " + playerId + ": player=" + player + ", connection=" + connection);
                    }
                }

                if (!connections.isEmpty()) {
                    if (connections.size() == 1) {
                        System.out.println("Returning single player ID " + connections.get(0).getID() + " to hub");
                        roomManager.assignToRoom(connections.get(0), users.get(0));
                    } else {
                        System.out.println("Returning " + connections.size() + " players to hub via batch");
                        roomManager.assignToRoomBatch(connections, users);
                    }
                }

                synchronized (playersConnections) {
                    if (!playersConnections.isEmpty()) {
                        System.out.println("Warning: " + playersConnections.size() + " players still in dungeon " + roomId);
                    }
                }


                System.out.println("Cleaning up dungeon " + roomId);
                ConnectionManager.deleteRoom("Dungeon", roomId);
                roomManager.getDungeons().remove(this);
                running = false; // Stop the dungeon thread
            }

            long sleep = frameTime - (System.nanoTime() - now);
            if (sleep > 0) {
                try {
                    Thread.sleep((sleep / 1_000_000), (int) (sleep % 1_000_000));
                } catch (InterruptedException ignored) {}
            }
        }
    }

    public void startAttack(Network.AttackStartPacket packet) {
        Weapon currentWeapon = playersById.get(packet.id).getCurrentWeapon();
        ConnectionManager.sendToConnectionsExcept(gameMode, roomId, packet.id, packet);
        if (currentWeapon instanceof RangedWeapon) {
            currentWeapon.setAttacking(true);
            ((RangedWeapon) currentWeapon).setAnimOver(true);
            currentWeapon.setAiming(true);
        }
    }

    public void attackDragged(Network.AttackDragged packet) {
        Weapon curWeapon = playersById.get(packet.id).getCurrentWeapon();
        if (curWeapon instanceof RangedWeapon) {
            curWeapon.setRotation(packet.angle);
        }
        curWeapon.setAiming(true);
        ConnectionManager.sendToConnectionsExcept(gameMode, roomId, packet.id, packet);
    }

    public void endAttack(Network.AttackReleasePacket packet) {
        Weapon curWeapon = playersById.get(packet.id).getCurrentWeapon();
        curWeapon.setRotation(packet.angle);
        if (curWeapon.getAiming()) {
            curWeapon.attack();
            System.out.println("Attacked");
            curWeapon.setAttacking(true);
            if (curWeapon instanceof RangedWeapon) {
                ((RangedWeapon) curWeapon).setAnimOver(true);
            }
            if (curWeapon instanceof MagicWeapon) {
                System.out.println("JUST DID FOR MAGIC");
                ((MagicWeapon) curWeapon).setAnimOver(true);
            }
            if (curWeapon instanceof RangedWeapon) {
                ((RangedWeapon) curWeapon).setIsCharging(false);
            }
            curWeapon.setAiming(false);
        }
        ConnectionManager.sendToConnectionsExcept(gameMode, roomId, packet.id, packet);
    }

    public void applyMove(Network.MovePacket movePacket, Connection c) {
        System.out.println(movePacket.id);
        Player p = playersById.get(movePacket.id);
        if (p != null) {
            System.out.println("player is moving!!!!1");
            p.setPlayerMoveTrue(new Vector2(movePacket.x, movePacket.y));
        }
    }

    public void cancleMove(Network.StopPlayerMove stopPlayerMove, Connection connection) {
        Player p = playersById.get(stopPlayerMove.id);
        if (p != null) {
            p.setPlayerMoveFalse();
        }
    }

    public int getDungeonId() {
        return roomId;
    }

    public MapManaging getMapManager() {
        return mapManager;
    }

    protected void broadcastState(Connection c, Player connectedPlayer, User user) {
        Network.StateMessageOnConnection msg = new Network.StateMessageOnConnection();
        msg.gameMode = "Dungeon";
        msg.newPlayerId = c.getID();
        msg.weaponName = connectedPlayer.getCurrentWeapon() != null ? connectedPlayer.getCurrentWeapon().getName() : null;
        msg.currentHelmet = connectedPlayer.getEquipment().getHelmet() != null ? connectedPlayer.getEquipment().getHelmet().getName() : null;
        msg.currentChestplate = connectedPlayer.getEquipment().getChestplate() != null ? connectedPlayer.getEquipment().getChestplate().getName() : "";
        msg.jsonPath = enemyLoader.getJsonPath().replace(asset, "");
        msg.players = new ArrayList<>();
        msg.curX = connectedPlayer.getEntityPos().x / GameConstants.Sprite.SIZE;
        msg.curY = connectedPlayer.getEntityPos().y / GameConstants.Sprite.SIZE;
        msg.user = user;

        synchronized (PLAYERS) {
            for (Player p : PLAYERS) {
                if (p.getID() != c.getID()) {
                    PlayerState ps = new PlayerState();
                    ps.id = p.getID();
                    ps.x = p.getEntityPos().x / GameConstants.Sprite.SIZE;
                    ps.y = p.getEntityPos().y / GameConstants.Sprite.SIZE;
                    ps.level = p.getLevel();
                    ps.currentHelmet = p.getEquipment().getHelmet() != null ? p.getEquipment().getHelmet().getName() : "";
                    ps.currentChestplate = p.getEquipment().getChestplate() != null ? p.getEquipment().getChestplate().getName() : "";
                    ps.experience = (int) p.getExperience();
                    ps.hp = (int) p.getHp();
                    ps.money = p.getMoney();
                    ps.weaponName = p.getCurrentWeapon() != null ? p.getCurrentWeapon().getName() : "";
                    ps.name = p.getName();
                    msg.players.add(ps);
                }
            }
        }
        msg.enemies = new ArrayList<>();
        for (Enemy e : Enemies) {
            EnemyState es = new EnemyState();
            es.id = e.getID();
            es.x = e.getEntityPos().x / GameConstants.Sprite.SIZE;
            es.y = e.getEntityPos().y / GameConstants.Sprite.SIZE;
            es.dirX = e.getDirX();
            es.dirY = e.getDirY();
            es.hp = (int) e.getHp();
            es.speed = e.getSpeed();
            msg.enemies.add(es);
        }

        msg.mapInfo.hitboxPath = mapManager.getHitboxFile().replace(asset, "");
        msg.mapInfo.mapPath = mapManager.getMapName().replace(asset, "");
        msg.mapInfo.renderPath = mapManager.getTilesSpritesheet();
        msg.mapInfo.width = mapManager.getMapWidth();
        msg.mapInfo.height = mapManager.getMapHeight();

        msg.playerWeaponJson = weaponLoader.getRecourcePath().replace(asset, "");
        msg.enemyWeaponJson = weaponLoader2.getRecourcePath().replace(asset, "");

        System.out.println("Sending StateMessageOnConnection to ID " + c.getID() + " with " + msg.players.size() + " players");
        c.sendTCP(msg);

        PlayerState ps = new PlayerState();
        ps.name = connectedPlayer.getName();
        ps.level = connectedPlayer.getLevel();
        ps.experience = (int) connectedPlayer.getExperience();
        ps.money = connectedPlayer.getMoney();
        ps.weaponName = connectedPlayer.getCurrentWeapon() != null ? connectedPlayer.getCurrentWeapon().getName() : "";
        ps.id = connectedPlayer.getID();
        ps.x = connectedPlayer.getEntityPos().x / GameConstants.Sprite.SIZE;
        ps.y = connectedPlayer.getEntityPos().y / GameConstants.Sprite.SIZE;
        ps.currentHelmet = connectedPlayer.getEquipment().getHelmet() != null ? connectedPlayer.getEquipment().getHelmet().getName() : "";
        ps.currentChestplate = connectedPlayer.getEquipment().getChestplate() != null ? connectedPlayer.getEquipment().getChestplate().getName() : "";
        ps.hp = (int) connectedPlayer.getHp();
        System.out.println("Sending PlayerState for ID " + ps.id + " to other clients in dungeon " + roomId);
        ConnectionManager.sendToConnectionsExcept(gameMode, roomId, connectedPlayer.getID(), ps);
    }

    public synchronized void removeConnection(Connection c) {
        Player p = playersById.get(c.getID());
        if (p != null) {
            PLAYERS.remove(p);
            playersById.remove(p.getID());
            playersConnections.remove(c.getID());

            System.out.println("Removed connection ID " + c.getID() + " from dungeon " + roomId);
            if (PLAYERS.isEmpty()) {
                running = false;
                ConnectionManager.deleteRoom("Dungeon", roomId);
                roomManager.getDungeons().remove(this);
                System.out.println("Dungeon " + roomId + " is empty, cleaned up");
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
                playersConnections.put(p.getID(), c);
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

    public List<Player> getPlayer() {
        return PLAYERS;
    }

    public String getRoomName() {
        return gameMode;
    }
}
