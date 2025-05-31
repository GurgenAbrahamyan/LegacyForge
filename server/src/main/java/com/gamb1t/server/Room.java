package com.gamb1t.server;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.ManagerClasses.ArmorLoader;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.TouchManager;
import com.gamb1t.legacyforge.ManagerClasses.WeaponLoader;
import com.gamb1t.legacyforge.Networking.ConnectionManager;
import com.gamb1t.legacyforge.Networking.Network;
import com.gamb1t.legacyforge.Networking.Network.PlayerState;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.ManagerClasses.GameUpdate;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.Structures.ArmorShop;
import com.gamb1t.legacyforge.Structures.Shop;
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Room implements Runnable {

    private String gameMode;
    private int roomId;

    public static final int MAX_PLAYERS = 10;
    private final Server server;
    private final MapManaging mapManager;
    private final Shop shop;
    private final ArmorShop armorShop;

    public float playerX, playerY;

    private Random rand = new Random();
    private TouchManager touchEvents;

    private WeaponLoader weaponLoader;

    private ArmorLoader armorLoader;

    private static List<Player> PLAYERS;

    private ArrayList<Weapon> weapon;
    private boolean running = true;

    private RoomManager roomManager;

    public String asset = "assets\\";

    private Squad nextSquad;
    private Squad next1v1Squad; // New: Squad for 1v1 matches

    private GameUpdate gameUpdate;

    private final Map<Integer, Player> playersById = new HashMap<>();
    private final Map<Integer, Connection> playersByIdConnection = new HashMap<>();

    public Room(int roomId, String hub, Server server, RoomManager roomManager) {
        this.roomManager = roomManager;
        GameConstants.init();

        playerX = (float) GET_WIDTH / 2;
        playerY = (float) GET_HEIGHT / 2;
        this.gameMode = hub;
        this.roomId = roomId;
        this.server = server;
        PLAYERS = new CopyOnWriteArrayList<>();
        weaponLoader = new WeaponLoader(asset + "weapons.json", false);
        armorLoader = new ArmorLoader(asset + "armor.json");

        weapon = weaponLoader.getWeaponList();

        mapManager = new MapManaging(asset + "1room.txt", asset + "1roomHitbox.txt", "Tiles/Dungeon_Tileset.png", 30, 30);

        shop = new Shop(mapManager.getShopCoordinates().x, mapManager.getShopCoordinates().y,
            GameConstants.Sprite.SIZE * 4, GameConstants.Sprite.SIZE * 3, "shops/basic_shop.png", weaponLoader, null, null);

        armorShop = new ArmorShop(mapManager.getArmorShopCoordinates().x, mapManager.getArmorShopCoordinates().y,
            GameConstants.Sprite.SIZE * 4, GameConstants.Sprite.SIZE * 3, "shops/armor_shop_sprite.png", armorLoader, null, touchEvents);

        for (Weapon w : weapon) {
            if (w instanceof MagicWeapon) {
                ((MagicWeapon) w).setCurrentMap(mapManager);
            }
            w.setServer(server, gameMode, roomId);
        }

        gameUpdate = new GameUpdate(null, PLAYERS, mapManager, shop, armorShop);
        gameUpdate.setServ(server, gameMode, roomId);
    }

    public boolean isFull() {
        return PLAYERS.size() >= MAX_PLAYERS;
    }

    public void addConnection(Connection c, User newPlayer) {
        Player p = new Player(newPlayer.nickname, newPlayer.level, newPlayer.experience, newPlayer.money,
            mapManager.getRespPlayer().x + GameConstants.Sprite.SIZE / 2, mapManager.getRespPlayer().y + GameConstants.Sprite.SIZE / 2, null, mapManager);
        p.setRespPoint(mapManager.getRespPlayer());

        p.setId(c.getID());
        p.setServer(server, roomId, gameMode);
        p.addInventoryWeapons(weaponLoader.getWeaponsFromMap(newPlayer.items.weapons));
        p.setCurrentWeapon(p.getInventory().getWeaponByName((String) newPlayer.items.weapons.get(newPlayer.equippedWeapon).get("name")));
        p.getCurrentWeapon().setEntity(p);
        playersById.put(p.getID(), p);
        playersByIdConnection.put(p.getID(), c);

        p.setFirebaseId(newPlayer.firebaseId);
        p.setLoses(newPlayer.losses);
        p.setWins(newPlayer.wins);
        p.setRating(newPlayer.rating);

        for (Weapon weapon1 : p.getInventory().getWeapons()) {
            weapon1.setServer(server, gameMode, roomId);
            weapon1.setFirebaseId(newPlayer.items.getFireBaseIdWeaponByName(weapon1.getName()));
            if (weapon1 instanceof MagicWeapon) {
                ((MagicWeapon) weapon1).setCurrentMap(mapManager);
            }
        }
        p.addInventoryArmors(armorLoader.getArmorsFromMap(newPlayer.items.armor));
        if (newPlayer.equippedArmorHelmet != null) {
            Armor helmet = p.getInventory().getArmorByName((String) newPlayer.items.armor.get(newPlayer.equippedArmorHelmet).get("name"));
            p.equipArmor(helmet);
        }
        if (newPlayer.equippedArmorChestPlate != null && !newPlayer.equippedArmorChestPlate.isEmpty()) {
            Armor chest = p.getInventory().getArmorByName((String) newPlayer.items.armor.get(newPlayer.equippedArmorChestPlate).get("name"));
            p.equipArmor(chest);
        }
        broadcastState(c, p, newPlayer);
        PLAYERS.add(p);
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
        } else if (action.equals("join1v1")) {
            if (next1v1Squad == null) {
                next1v1Squad = new Squad(player, connection);
            } else if (next1v1Squad.getMembersConnections().size() < 2) {
                next1v1Squad.addMember(player, connection);
                if (next1v1Squad.getMembersConnections().size() == 2) {
                    startOneVsOne();
                    return;
                }
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
                update.inSquad = next1v1Squad != null && next1v1Squad.getMembersConnections().containsKey(playersById.get(conn.getID()));
                update.countdown = -1; // No countdown for 1v1
                update.memberNames = new ArrayList<>();
                if (next1v1Squad != null) {
                    for (Player p : next1v1Squad.getMembersConnections().keySet()) {
                        update.memberNames.add(p.getName());
                    }
                }
                server.sendToTCP(conn.getID(), update);
            }
        } else if (action.equals("leave1v1")) {
            if (next1v1Squad != null) {
                next1v1Squad.removeMember(player, connection);

                for (Connection conn : playersByIdConnection.values()) {
                    Network.SquadUpdate update = new Network.SquadUpdate();
                    update.inSquad = next1v1Squad != null && next1v1Squad.getMembersConnections().containsKey(playersById.get(conn.getID()));
                    update.countdown = -1; // No countdown for 1v1
                    update.memberNames = new ArrayList<>();
                    if (next1v1Squad != null) {
                        for (Player p : next1v1Squad.getMembersConnections().keySet()) {
                            update.memberNames.add(p.getName());
                        }
                    }
                    server.sendToTCP(conn.getID(), update);
                }

                if (next1v1Squad.getMembersConnections().isEmpty()) {
                    next1v1Squad = null;
                }
            }
        }
    }

    private void startOneVsOne() {
        List<Player> players = new ArrayList<>(next1v1Squad.getMembersConnections().keySet());
        if (players.size() != 2) {
            System.err.println("Error: 1v1 squad does not have exactly 2 players: " + players.size());
            return;
        }
        List<Connection> connections = new ArrayList<>();
        for (Player p : players) {
            Connection c = playersByIdConnection.get(p.getID());
            if (c != null) {
                connections.add(c);
            } else {
                System.err.println("No connection for player ID: " + p.getID());
            }
        }
        if (connections.size() != 2) {
            System.err.println("Error: Only found " + connections.size() + " connections for 1v1");
            return;
        }
        roomManager.assignToOneVsOne(connections, players, next1v1Squad);
        next1v1Squad = null;
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

            if (nextSquad != null) {
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
            curWeapon.setAttacking(true);
            if (curWeapon instanceof RangedWeapon) {
                ((RangedWeapon) curWeapon).setAnimOver(true);
            }
            if (curWeapon instanceof MagicWeapon) {
                ((MagicWeapon) curWeapon).setAnimOver(true);
            }
            if (curWeapon instanceof RangedWeapon) {
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
            if (next1v1Squad != null) {
                next1v1Squad.removeMember(p, c);
                if (next1v1Squad.getMembersConnections().isEmpty()) {
                    next1v1Squad = null;
                }
            }
        }
    }

    public void addConnections(List<Connection> connections, List<User> users) {
        List<Player> newPlayers = new ArrayList<>();
        for (int i = 0; i < connections.size(); i++) {
            Connection c = connections.get(i);
            User newPlayer = users.get(i);
            Player p = new Player(newPlayer.nickname, newPlayer.level, newPlayer.experience, newPlayer.money,
                mapManager.getRespPlayer().x + GameConstants.Sprite.SIZE / 2, mapManager.getRespPlayer().y + GameConstants.Sprite.SIZE / 2, null, mapManager);
            p.setRespPoint(mapManager.getRespPlayer());
            p.setId(c.getID());
            p.setServer(server, roomId, gameMode);

            p.addInventoryWeapons(weaponLoader.getWeaponsFromMap(newPlayer.items.weapons));
            p.setCurrentWeapon(p.getInventory().getWeaponByName((String) newPlayer.items.weapons.get(newPlayer.equippedWeapon).get("name")));
            if (p.getCurrentWeapon() != null) {
                p.getCurrentWeapon().setEntity(p);
            }

            p.addInventoryArmors(armorLoader.getArmorsFromMap(newPlayer.items.armor));
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
        Player p = playersById.get(movePacket.id);
        if (p != null) {
            p.setPlayerMoveTrue(new Vector2(movePacket.x, movePacket.y));
        }
    }

    public void cancelMove(Network.StopPlayerMove stopPlayerMove, Connection connection) {
        Player p = playersById.get(stopPlayerMove.id);
        if (p != null) {
            p.setPlayerMoveFalse();
        }
    }

    public void playerAddNewWeapon(Network.OnPlayerBoughtWeapon o, Connection c) {
        Player p = playersById.get(o.playerId);
        if (p != null) {
            p.addWeapon(weaponLoader.deepCopyWeapon(o.weaponName), o.lvl);
            p.getCurrentWeapon().setServer(server, gameMode, roomId);
        }
    }

    public void playerEquipWeapon(Network.OnPlayerEquipWeapon o, Connection c) {
        Player p = playersById.get(o.playerId);
        if (p != null) {
            p.setCurrentWeapon(p.getInventory().getWeaponByName(o.weaponName));
            p.getCurrentWeapon().setServer(server, gameMode, roomId);
            if (p.getCurrentWeapon() instanceof MagicWeapon) {
                ((MagicWeapon) p.getCurrentWeapon()).setCurrentMap(mapManager);
            }
            ConnectionManager.sendToConnectionsExcept(gameMode, roomId, c.getID(), o);
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

    public void playerAddNewArmor(Network.OnPlayerBoughtArmor o, Connection c) {
        Player p = playersById.get(o.playerId);
        if (p != null) {
            p.addArmor(armorLoader.deepCopyArmor(o.weaponArmor), o.lvl);
            p.setTextureArray(armorLoader.getArmorByName(o.weaponArmor).getTextureRegion());
        }
    }

    public void playerEquipArmor(Network.OnPlayerEquipArmor o, Connection c) {
        Player p = playersById.get(o.playerId);
        if (p != null) {
            p.equipArmor(p.getInventory().getArmorByName(o.armorName));
            ConnectionManager.sendToConnectionsExcept(gameMode, roomId, c.getID(), o);
        }
    }

    private void broadcastState(Connection c, Player connectedPlayer, User user) {
        Network.StateMessageOnConnection msg = new Network.StateMessageOnConnection();
        msg.gameMode = "Hub";
        msg.newPlayerId = connectedPlayer.getID();
        msg.weaponName = connectedPlayer.getCurrentWeapon().getName();
        if (connectedPlayer.getEquipment() != null && connectedPlayer.getEquipment().getHelmet() != null) {
            msg.currentHelmet = connectedPlayer.getEquipment().getHelmet().getName();
        }
        if (connectedPlayer.getEquipment() != null && connectedPlayer.getEquipment().getChestplate() != null) {
            msg.currentChestplate = connectedPlayer.getEquipment().getChestplate().getName();
        }
        msg.players = new ArrayList<>();
        msg.curX = connectedPlayer.getEntityPos().x / GameConstants.Sprite.SIZE;
        msg.curY = connectedPlayer.getEntityPos().y / GameConstants.Sprite.SIZE;
        msg.user = user;
        for (Player p : PLAYERS) {
            if (c.getID() != p.getID()) {
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
                ps.weaponName = p.getCurrentWeapon().getName();
                ps.name = p.getName();
                msg.players.add(ps);
            }
        }
        msg.enemies = new ArrayList<>();
        msg.shopInfo = new Network.ShopInfo();
        msg.shopInfo.height = shop.getShopHeight() / GameConstants.Sprite.SIZE;
        msg.shopInfo.width = shop.getShopWidth() / GameConstants.Sprite.SIZE;
        msg.shopInfo.x = shop.getShopX() / GameConstants.Sprite.SIZE;
        msg.shopInfo.y = shop.getShopY() / GameConstants.Sprite.SIZE;
        msg.shopInfo.renderPath = shop.getShopTexture();
        msg.shopInfo.weaponsJson = shop.getWeaponPath().replace(asset, "");
        msg.armorShopInfo = new Network.ShopInfo();
        msg.armorShopInfo.height = armorShop.getShopHeight() / GameConstants.Sprite.SIZE;
        msg.armorShopInfo.width = armorShop.getShopWidth() / GameConstants.Sprite.SIZE;
        msg.armorShopInfo.x = armorShop.getShopX() / GameConstants.Sprite.SIZE;
        msg.armorShopInfo.y = armorShop.getShopY() / GameConstants.Sprite.SIZE;
        msg.armorShopInfo.renderPath = armorShop.getShopTexture();
        msg.armorShopInfo.weaponsJson = armorShop.getArmorPath().replace(asset, "");
        msg.mapInfo.hitboxPath = mapManager.getHitboxFile().replace(asset, "");
        msg.mapInfo.mapPath = mapManager.getMapName().replace(asset, "");
        msg.mapInfo.renderPath = mapManager.getTilesSpritesheet();
        msg.mapInfo.width = mapManager.getMapWidth();
        msg.mapInfo.height = mapManager.getMapHeight();
        msg.playerWeaponJson = weaponLoader.getRecourcePath().replace(asset, "");
        c.sendTCP(msg);

        PlayerState ps = new PlayerState();
        ps.name = connectedPlayer.getName();
        ps.level = connectedPlayer.getLevel();
        ps.experience = (int) connectedPlayer.getExperience();
        ps.money = connectedPlayer.getMoney();
        ps.weaponName = connectedPlayer.getCurrentWeapon().getName();
        ps.id = connectedPlayer.getID();
        ps.x = connectedPlayer.getEntityPos().x / GameConstants.Sprite.SIZE;
        ps.y = connectedPlayer.getEntityPos().y / GameConstants.Sprite.SIZE;
        ps.hp = (int) connectedPlayer.getHp();
        ps.currentHelmet = connectedPlayer.getEquipment().getHelmet() != null ? connectedPlayer.getEquipment().getHelmet().getName() : "";
        ps.currentChestplate = connectedPlayer.getEquipment().getChestplate() != null ? connectedPlayer.getEquipment().getChestplate().getName() : "";
        ConnectionManager.sendToConnectionsExcept(gameMode, roomId, c.getID(), ps);
    }

    public List<Player> getPlayer() {
        return PLAYERS;
    }

    public int getRoomId() {
        return roomId;
    }
}
