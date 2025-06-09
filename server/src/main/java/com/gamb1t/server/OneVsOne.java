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
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class OneVsOne implements Runnable {

    public static final int MAX_PLAYERS = 2;
    public static final int TOTAL_ROUNDS = 5;
    public static final float COUNTDOWN_TIME = 3f;
    public static final float FLEX_TIME = 1f;

    private String gameMode;
    private int roomId;

    private final Server server;
    private final MapManaging mapManager;

    public float playerX, playerY;

    private Random rand = new Random();

    private boolean inCooldown = false;
    private float cooldownTimer = 0f;

    private WeaponLoader weaponLoader;
    private ArmorLoader armorLoader;

    private static List<Player> PLAYERS;

    private ArrayList<Weapon> weapon;
    private boolean running = true;

    private RoomManager roomManager;

    public String asset = "assets\\";

    private GameUpdate gameUpdate;

    private final Map<Integer, Player> playersById = new HashMap<>();
    private final Map<Integer, Connection> playersByIdConnection = new HashMap<>();
    private final Map<Integer, Integer> playerScores = new HashMap<>(); // Track rounds won by each player

    private int currentRound = 0;
    private float countdownTimer = 0f;
    private float flexTimer = 0f;
    private boolean inCountdown = false;
    private boolean inFlex = false;
    private boolean matchEnded = false;
    private Player roundWinner = null;

    public OneVsOne(int roomId, String hub, Server server, RoomManager roomManager) {
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

        mapManager = new MapManaging(asset + "1v1Textures.txt", asset + "1v1Hitboxes.txt", "Tiles/Dungeon_Tileset.png", 20, 20);

        for (Weapon w : weapon) {
            if (w instanceof MagicWeapon) {
                ((MagicWeapon) w).setCurrentMap(mapManager);
            }
            w.setServer(server, gameMode, roomId);
        }

        mapManager.setServer(server, hub, roomId);

        gameUpdate = new GameUpdate(null, PLAYERS, mapManager, null, null);
        gameUpdate.setServ(server, gameMode, roomId);
        gameUpdate.setFriendlyFire(true);
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
                mapManager.update(delta);
            }

            if (!inCountdown && !inFlex && !matchEnded && !inCooldown) {
                if (PLAYERS.size() == MAX_PLAYERS && currentRound == 0) {
                    inCountdown = true;
                    countdownTimer = COUNTDOWN_TIME;
                    mapManager.assignRandomSpawnPoints(PLAYERS);
                    mapManager.resetRound();
                    startRound();
                } else {
                    checkRoundEnd();
                }
            }

            if (inCountdown) {
                countdownTimer -= delta;
                if (countdownTimer <= 0) {
                    inCountdown = false;
                    if (currentRound <= TOTAL_ROUNDS && !matchEnded) {
                        mapManager.openAllDoors();
                    } else {
                        inFlex = true;
                        flexTimer = FLEX_TIME;
                    }
                }
            } else if (inCooldown) {
                cooldownTimer -= delta;
                if (cooldownTimer <= 0) {
                    inCooldown = false;
                    mapManager.closeAllDoors();
                    mapManager.assignRandomSpawnPoints(PLAYERS);
                    mapManager.resetRound();
                    inCountdown = true;
                    countdownTimer = COUNTDOWN_TIME;
                    startRound();
                }
            } else if (inFlex) {
                flexTimer -= delta;
                if (flexTimer <= 0) {
                    inFlex = false;
                    endMatch();
                }
            } else if (!matchEnded) {
                checkRoundEnd();
            }

            if (PLAYERS.size() < MAX_PLAYERS && !matchEnded && currentRound > 0) {
                endMatch();
            }

            if (mapManager.getGoingBack()) {
                List<Map.Entry<Integer, Connection>> playersToReturn = new ArrayList<>(playersByIdConnection.entrySet());
                System.out.println("Returning " + playersToReturn.size() + " players to hub in 1v1 " + roomId);

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

                playersByIdConnection.clear();
                System.out.println("Cleaning up 1v1 room: " + roomId);
                ConnectionManager.deleteRoom(gameMode, roomId);
                roomManager.get1v1Matches().remove(this);
                running = false;
            }

            long sleep = frameTime - (System.nanoTime() - now);
            if (sleep > 0) {
                try {
                    Thread.sleep((sleep / 1_000_000), (int) (sleep % 1_000_000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void startRound() {
        currentRound++;
        System.out.println("Starting round " + currentRound);
        roundWinner = null;
        for (Player p : PLAYERS) {
            playerScores.putIfAbsent(p.getID(), 0); }


        Network.RoundStart roundStart = new Network.RoundStart();
        roundStart.roundNumber = currentRound;
        roundStart.playerScores = new HashMap<>(playerScores);

        ConnectionManager.sendToConnections(gameMode, roomId, roundStart);
    }

    private void checkRoundEnd() {
        List<Player> alivePlayers = new ArrayList<>();
        for (Player p : PLAYERS) {
            if (p.getHp() > 0) {
                alivePlayers.add(p);
            }
        }

        if (alivePlayers.size() <= 1) {
            roundWinner = alivePlayers.isEmpty() ? null : alivePlayers.get(0);
            if (roundWinner != null) {
                System.out.println("Round " + currentRound + " winner: " + roundWinner.getName());
                playerScores.put(roundWinner.getID(), playerScores.getOrDefault(roundWinner.getID(), 0) + 1);
            } else {
                System.out.println("Round " + currentRound + " ended with no winner (both dead)");
            }
            inCooldown = true;
            cooldownTimer = 3f;
        }
    }

    private void endMatch() {
        if (matchEnded) return;
        matchEnded = true;

        List<Map.Entry<Integer, Connection>> playersToReturn = new ArrayList<>(playersByIdConnection.entrySet());

        List<Connection> connections = new ArrayList<>();
        List<User> users = new ArrayList<>();
        for (Map.Entry<Integer, Connection> entry : playersToReturn) {
            int playerId = entry.getKey();
            Connection connection = entry.getValue();
            Player player = playersById.get(playerId);
            if (player != null && connection != null) {
                int score = playerScores.getOrDefault(playerId, 0);
                if (score >= (TOTAL_ROUNDS / 2) + 1 || currentRound >= TOTAL_ROUNDS) {
                    player.setWins(player.getWins() + 1);
                    player.setRating(player.getRating() + 10);
                } else {
                    player.setLoses(player.getLoses() + 1);
                    player.setRating(Math.max(0, player.getRating() - 10));
                }
                connections.add(connection);
                users.add(roomManager.convertPlayerToUser(player));
            }
        }

        if (!connections.isEmpty()) {
            if (connections.size() == 1) {
                roomManager.assignToRoom(connections.get(0), users.get(0));
            } else {
                roomManager.assignToRoomBatch(connections, users);
            }
        }

        playersByIdConnection.clear();
        System.out.println("Cleaning up room: " + roomId);
        ConnectionManager.deleteRoom(gameMode, roomId);
        roomManager.get1v1Matches().remove(this);
        running = false;
    }

    public void startAttack(Network.AttackStartPacket packet) {
        Weapon currentWeapon = playersById.get(packet.id).getCurrentWeapon();
        ConnectionManager.sendToConnections(gameMode, roomId, packet);

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
            playerScores.remove(p.getID()); // Remove score when player leaves
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
                playerScores.put(p.getID(), 0);
            }
            System.out.println("Added player " + p.getID() + " to room: " + roomId + ", PLAYERS size: " + PLAYERS.size());
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

    private void broadcastState(Connection c, Player connectedPlayer, User user) {
        Network.StateMessageOnConnection msg = new Network.StateMessageOnConnection();
        msg.gameMode = "1v1"; // Set to 1v1 mode
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
