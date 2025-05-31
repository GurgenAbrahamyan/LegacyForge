package com.gamb1t.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.ManagerClasses.ArmorLoader;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.WeaponLoader;
import com.gamb1t.legacyforge.Networking.ConnectionManager;
import com.gamb1t.legacyforge.Networking.Network;
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.MeleeWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;
import com.gamb1t.server.DBcontrol.FirebaseServiceServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class OneVsOne implements Runnable {
    public static final int MAX_PLAYERS = 2;
    public static final int KILLS_TO_WIN = 3;
    private static final String ASSET_PATH = "assets\\";
    private final WeaponLoader weaponLoader;
    private final ArmorLoader armorLoader;
    private final OneVsOneGameLogic gameLogic;
    private volatile boolean running = true;
    private final Map<Integer, Integer> killCounts = new ConcurrentHashMap<>();
    private boolean roundEnded = false;
    private float roundEndTimer = 0;

    protected int roomId;
    protected Server server;
    protected MapManaging mapManager;
    protected List<Player> PLAYERS;
    protected String asset = "assets\\";
    protected String gameMode;
    protected RoomManager roomManager;
    protected final Map<Integer, Player> playersById = new ConcurrentHashMap<>();
    protected final Map<Integer, Connection> playersConnections = new ConcurrentHashMap<>();

    public OneVsOne(int roomId, String roomName, Server server, RoomManager roomManager) {
        this.roomId = roomId;
        this.gameMode = roomName;
        this.server = server;
        this.roomManager = roomManager;
        PLAYERS = new CopyOnWriteArrayList<>();
        weaponLoader = new WeaponLoader(asset + "weapons.json", false);
        armorLoader = new ArmorLoader(asset + "armor.json");

        mapManager = new MapManaging(
            asset + "1v1Textures.txt",
            asset + "1v1Hitboxes.txt",
            "Tiles/Dungeon_Tileset.png",
            30, 30
        );
        mapManager.setServer(server, roomName, roomId);
        mapManager.setGameMode(roomName);
        mapManager.setPlayers(PLAYERS);

        gameLogic = new OneVsOneGameLogic(PLAYERS, mapManager, killCounts);
        gameLogic.setServ(server, roomName, roomId);
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
                gameLogic.update(delta);
            }

            mapManager.update(delta);

            if (roundEnded) {
                roundEndTimer -= delta;
                if (roundEndTimer <= 0) {
                    roundEnded = false;
                    gameLogic.startNewRound();
                }
            }

            if (mapManager.getGoingBack()) {
                endMatch();
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
        Player player = playersById.get(packet.id);
        if (player == null) {
            System.err.println("No player found for ID: " + packet.id);
            return;
        }
        Weapon currentWeapon = player.getCurrentWeapon();
        if (currentWeapon == null) {
            System.err.println("No weapon equipped for player ID: " + packet.id);
            return;
        }
        System.out.println("Broadcasting AttackStartPacket for player ID: " + packet.id + " to room " + roomId);
        ConnectionManager.sendToConnectionsExcept(gameMode, roomId, packet.id, packet);
        if (currentWeapon instanceof RangedWeapon) {
            currentWeapon.setAttacking(true);
            ((RangedWeapon) currentWeapon).setAnimOver(true);
            currentWeapon.setAiming(true);
        }
    }

    public void attackDragged(Network.AttackDragged packet) {
        Player player = playersById.get(packet.id);
        if (player == null) {
            System.err.println("No player found for ID: " + packet.id);
            return;
        }
        Weapon curWeapon = player.getCurrentWeapon();
        if (curWeapon == null) {
            System.err.println("No weapon equipped for player ID: " + packet.id);
            return;
        }
        if (curWeapon instanceof RangedWeapon) {
            curWeapon.setRotation(packet.angle);
        }
        curWeapon.setAiming(true);
        System.out.println("Broadcasting AttackDragged packet for player ID: " + packet.id + " to room " + roomId);
        ConnectionManager.sendToConnectionsExcept(gameMode, roomId, packet.id, packet);
    }

    public void endAttack(Network.AttackReleasePacket packet) {
        Player player = playersById.get(packet.id);
        if (player == null) {
            System.err.println("No player found for ID: " + packet.id);
            return;
        }
        Weapon curWeapon = player.getCurrentWeapon();
        if (curWeapon == null) {
            System.err.println("No weapon equipped for player ID: " + packet.id);
            return;
        }
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
        System.out.println("Broadcasting AttackReleasePacket for player ID: " + packet.id + " to room " + roomId);
        ConnectionManager.sendToConnectionsExcept(gameMode, roomId, packet.id, packet);
    }

    public void applyMove(Network.MovePacket movePacket, Connection c) {
        Player p = playersById.get(movePacket.id);
        if (p != null) {
            p.setPlayerMoveTrue(new Vector2(movePacket.x, movePacket.y));
        } else {
            System.err.println("No player found for move packet ID: " + movePacket.id);
        }
    }

    public void cancelMove(Network.StopPlayerMove stopPlayerMove, Connection connection) {
        Player p = playersById.get(stopPlayerMove.id);
        if (p != null) {
            p.setPlayerMoveFalse();
        } else {
            System.err.println("No player found for stop move packet ID: " + stopPlayerMove.id);
        }
    }

    public int getRoomId() {
        return roomId;
    }

    public MapManaging getMapManager() {
        return mapManager;
    }

    public List<Player> getPlayer() {
        return PLAYERS;
    }

    public String getRoomName() {
        return gameMode;
    }

    public boolean isFull() {
        return PLAYERS.size() >= MAX_PLAYERS;
    }

    public synchronized void addConnections(List<Connection> connections, List<User> users) {
        if (connections.size() != 2 || users.size() != 2) {
            System.err.println("1v1 requires exactly 2 players, got: " + connections.size());
            return;
        }

        List<Player> newPlayers = new ArrayList<>();
        for (int i = 0; i < connections.size(); i++) {
            Connection c = connections.get(i);
            User newPlayer = users.get(i);
            Player p = new Player(newPlayer.nickname, newPlayer.level, newPlayer.experience, newPlayer.money, mapManager.getRespPlayer().x + GameConstants.Sprite.SIZE / 2, mapManager.getRespPlayer().y + GameConstants.Sprite.SIZE / 2, null, mapManager);
            p.setRespPoint(mapManager.getRespPlayer());
            p.setId(c.getID());
            p.setServer(server, roomId, gameMode);
            p.setFirebaseId(newPlayer.firebaseId);

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

       /* for (int i = 0; i < connections.size(); i++) {
            Connection c = connections.get(i);
            Player p = newPlayers.get(i);
            Network.PvpMatchStart matchStart = new Network.PvpMatchStart();
            System.out.println("Sending PvpMatchStart for player Firebase ID: " + p.getFirebaseId());
            matchStart.playerId = p.getFirebaseId();
            matchStart.opponentId = users.get(1 - i).firebaseId;
            matchStart.opponentName = users.get(1 - i).nickname;
            c.sendTCP(matchStart);
        }*/
    }

    public synchronized void removeConnection(Connection c) {
        Player p = playersById.get(c.getID());
        if (p != null) {
            PLAYERS.remove(p);
            playersById.remove(p.getID());
            playersConnections.remove(c.getID());
            killCounts.remove(c.getID());

            System.out.println("Removed player ID: " + c.getID() + " from 1v1 match " + roomId);
            ConnectionManager.sendToConnectionsExcept("1v1", roomId, c.getID(), new Network.PlayerDeleted(c.getID()));

            if (PLAYERS.size() == 1) {
                Player winner = PLAYERS.get(0);
                updateEloAndStats(winner);
                mapManager.setGoingBack(true);
            } else {
                running = false;
                ConnectionManager.deleteRoom("1v1", roomId);
                roomManager.get1v1Matches().remove(this);
            }
        }
    }

    private void endMatch() {
        Player winner = null;
        for (Player p : PLAYERS) {
            if (killCounts.getOrDefault(p.getID(), 0) >= KILLS_TO_WIN) {
                winner = p;
                break;
            }
        }

        List<Map.Entry<Integer, Connection>> playersToReturn;
        synchronized (playersConnections) {
            playersToReturn = new ArrayList<>(playersConnections.entrySet());
        }

        if (winner != null) {
            updateEloAndStats(winner);
        }

        List<Connection> connections = new ArrayList<>();
        List<User> users = new ArrayList<>();
        for (Map.Entry<Integer, Connection> entry : playersToReturn) {
            int playerId = entry.getKey();
            Connection connection = entry.getValue();
            Player player = playersById.get(playerId);
            if (player != null && connection != null) {
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

        Network.MatchEnd matchEnd = new Network.MatchEnd();
        matchEnd.winnerId = winner != null ? winner.getID() : -1;
        ConnectionManager.sendToConnections("1v1", roomId, matchEnd);

        ConnectionManager.deleteRoom("1v1", roomId);
        roomManager.get1v1Matches().remove(this);
        running = false;
    }

    private void updateEloAndStats(Player winner) {
        Player loser = null;
        for (Player p : PLAYERS) {
            if (!p.equals(winner)) {
                loser = p;
                break;
            }
        }

        if (winner == null || loser == null) {
            return;
        }

        User winnerUser = roomManager.convertPlayerToUser(winner);
        User loserUser = roomManager.convertPlayerToUser(loser);

        int winnerRating = winnerUser.rating;
        int loserRating = loserUser.rating;

        double expectedWinner = 1.0 / (1.0 + Math.pow(10, (loserRating - winnerRating) / 400.0));
        double expectedLoser = 1.0 - expectedWinner;

        int K = 32;
        int newWinnerRating = (int) (winnerRating + K * (1 - expectedWinner));
        int newLoserRating = (int) (loserRating + K * (0 - expectedLoser));

        FirebaseServiceServer firebase = FirebaseServiceServer.getInstance();
        firebase.updateUserStats(winnerUser.firebaseId, winnerUser.wins + 1, winnerUser.losses, newWinnerRating);
        firebase.updateUserStats(loserUser.firebaseId, loserUser.wins, loserUser.losses + 1, newLoserRating);
    }

    private void broadcastState(Connection c, Player connectedPlayer, User user) {
        Network.StateMessageOnConnection msg = new Network.StateMessageOnConnection();
        msg.gameMode = "1v1";
        msg.newPlayerId = c.getID();
        msg.weaponName = connectedPlayer.getCurrentWeapon() != null ? connectedPlayer.getCurrentWeapon().getName() : null;
        msg.currentHelmet = connectedPlayer.getEquipment().getHelmet() != null ? connectedPlayer.getEquipment().getHelmet().getName() : null;
        msg.currentChestplate = connectedPlayer.getEquipment().getChestplate() != null ? connectedPlayer.getEquipment().getChestplate().getName() : "";
        msg.players = new ArrayList<>();
        msg.curX = connectedPlayer.getEntityPos().x / GameConstants.Sprite.SIZE;
        msg.curY = connectedPlayer.getEntityPos().y / GameConstants.Sprite.SIZE;
        msg.user = user;

        synchronized (PLAYERS) {
            for (Player p : PLAYERS) {
                if (p.getID() != c.getID()) {
                    Network.PlayerState ps = new Network.PlayerState();
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
        msg.mapInfo.hitboxPath = mapManager.getHitboxFile().replace(asset, "");
        msg.mapInfo.mapPath = mapManager.getMapName().replace(asset, "");
        msg.mapInfo.renderPath = mapManager.getTilesSpritesheet();
        msg.mapInfo.width = mapManager.getMapWidth();
        msg.mapInfo.height = mapManager.getMapHeight();

        msg.playerWeaponJson = weaponLoader.getRecourcePath().replace(asset, "");

        System.out.println("Sending StateMessageOnConnection to ID " + c.getID() + " with " + msg.players.size() + " players");
        c.sendTCP(msg);

        Network.PlayerState ps = new Network.PlayerState();
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

        ConnectionManager.sendToConnectionsExcept(gameMode, roomId, connectedPlayer.getID(), ps);
    }

    public class OneVsOneGameLogic {
        private List<Player> players;
        private MapManaging mapManager;
        private Server server;
        private String roomName;
        private int id;
        private final Map<Integer, Integer> killCounts;

        public OneVsOneGameLogic(List<Player> players, MapManaging mapManager, Map<Integer, Integer> killCounts) {
            this.players = players;
            this.mapManager = mapManager;
            this.killCounts = killCounts;
        }

        public void setServ(Server server, String roomName, int id) {
            this.server = server;
            this.roomName = roomName;
            this.id = id;
        }

        public void update(float delta) {
            if (!mapManager.isPvpStarted()) {
                return;
            }

            for(Player PLAYER : PLAYERS) {
                if(server != null){
                    ConnectionManager.sendToConnections(roomName, id, new Network.PlayerPos(PLAYER.getEntityPos().x/GameConstants.Sprite.SIZE, PLAYER.getEntityPos().y/GameConstants.Sprite.SIZE, PLAYER.getID()));

                }
                PLAYER.getCurrentWeapon().setDelta(delta);
                PLAYER.update(delta);
                PLAYER.regenerateMana(delta);
                PLAYER.regenerateHP(delta);
                mapManager.setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
                if (PLAYER.getCurrentWeapon() instanceof RangedWeapon) {
                    ((RangedWeapon) PLAYER.getCurrentWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
                }
                if (PLAYER.getCurrentWeapon() instanceof MagicWeapon) {
                    ((MagicWeapon) PLAYER.getCurrentWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
                }

                if (PLAYER.getCurrentWeapon().getAttacking()) {


                    PLAYER.getCurrentWeapon().createHitbox(PLAYER.getEntityPos().x, PLAYER.getEntityPos().y);


                }
                if (PLAYER.getCurrentWeapon().getAttacking()) {
                    if (PLAYER.getCurrentWeapon() instanceof RangedWeapon || PLAYER.getCurrentWeapon() instanceof MeleeWeapon) {
                        PLAYER.getCurrentWeapon().update(delta);
                        PLAYER.getCurrentWeapon().checkHitboxCollisionsEntity(PLAYERS);
                        PLAYER.getCurrentWeapon().checkHitboxCollisionsMap(mapManager);
                    }
                }

                if (PLAYER.getCurrentWeapon() instanceof MagicWeapon) {
                    PLAYER.getCurrentWeapon().update(delta);
                    PLAYER.getCurrentWeapon().checkHitboxCollisionsEntity(PLAYERS);
                    PLAYER.getCurrentWeapon().checkHitboxCollisionsMap(mapManager);
                }
            }


        }

        public void startNewRound() {
            mapManager.resetRound();
            int index = 0;
            for (Player p : players) {
                Vector2 spawn = index == 0 ? mapManager.getRespPlayerAll().get(0) : mapManager.getRespPlayerAll().get(1);
                p.setPosition(spawn.x + GameConstants.Sprite.SIZE / 2, spawn.y + GameConstants.Sprite.SIZE / 2);
                p.setHp(p.getMaxHp());
                p.getCurrentWeapon().setAttacking(false);
                p.getCurrentWeapon().setAiming(false);
                index++;
                Network.CurrentHp hp = new Network.CurrentHp();
                hp.idOfEnemy = p.getID();
                hp.curHp = (int) p.getHp();
                hp.isEnemy = false;
                ConnectionManager.sendToConnections(roomName, id, hp);
            }
            Network.RoundReset reset = new Network.RoundReset();
            ConnectionManager.sendToConnections(roomName, id, reset);
        }
    }
}
