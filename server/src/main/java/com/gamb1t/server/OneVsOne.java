package com.gamb1t.server;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
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

public class OneVsOne extends Dungeon {
    public static final int MAX_PLAYERS = 2;
    public static final int KILLS_TO_WIN = 3;
    private static final String ASSET_PATH = "assets/"; // Define asset path
    private final WeaponLoader weaponLoader;
    private final ArmorLoader armorLoader;
    private final OneVsOneGameLogic gameLogic;
    private volatile boolean running = true;
    private final Map<Integer, Integer> killCounts = new ConcurrentHashMap<>();
    private boolean roundEnded = false;
    private float roundEndTimer = 0;

    public OneVsOne(int roomId, String roomName, Server server, RoomManager roomManager) {
        super(roomId, roomName, server, roomManager);
        GameConstants.init();
        this.gameMode = "1v1";
        this.roomId = roomId;
        this.server = server;
        PLAYERS = new CopyOnWriteArrayList<>();
        weaponLoader = new WeaponLoader(ASSET_PATH + "weapons.json", false);
        armorLoader = new ArmorLoader(ASSET_PATH + "armor.json");

        mapManager = new MapManaging(
            ASSET_PATH + "1v1Textures.txt",
            ASSET_PATH + "1v1Hitboxes.txt",
            "Tiles/Dungeon_Tileset.png",
            30, 30
        );
        mapManager.setServer(server, roomName, roomId);
        mapManager.setGameMode("1v1");
        mapManager.setPlayers(PLAYERS);

        for (Weapon w : weaponLoader.getWeaponList()) {
            if (w instanceof MagicWeapon) {
                ((MagicWeapon) w).setCurrentMap(mapManager);
            }
            w.setServer(server, roomName, roomId);
        }

        gameLogic = new OneVsOneGameLogic(PLAYERS, mapManager, killCounts);
        gameLogic.setServ(server, roomName, roomId);
    }

    @Override
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
        synchronized (playersConnections) {
            for (Map.Entry<Integer, Connection> entry : playersToReturn) {
                int playerId = entry.getKey();
                Connection connection = entry.getValue();
                Player player = playersById.get(playerId);
                if (player != null && connection != null) {
                    connections.add(connection);
                    users.add(roomManager.convertPlayerToUser(player));
                }
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
                System.out.println("Warning: " + playersConnections.size() + " players still in 1v1 " + roomId);
            }
        }

        Network.MatchEnd matchEnd = new Network.MatchEnd();
        matchEnd.winnerId = winner != null ? winner.getID() : -1;
        ConnectionManager.sendToConnections("1v1", roomId, matchEnd);

        System.out.println("Cleaning up 1v1 " + roomId);
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
            System.err.println("Cannot update Elo: winner or loser is null");
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
        firebase.updateUserStats(winnerUser.firebaseId, winnerUser.wins + 1, winnerUser.losses, newWinnerRating)
            .whenComplete((aVoid, throwable) -> {
                if (throwable != null) {
                    System.err.println("Failed to update winner stats: " + throwable.getMessage());
                } else {
                    System.out.println("Updated winner " + winnerUser.firebaseId + " stats: wins=" + (winnerUser.wins + 1) + ", rating=" + newWinnerRating);
                }
            });

        firebase.updateUserStats(loserUser.firebaseId, loserUser.wins, loserUser.losses + 1, newLoserRating)
            .whenComplete((aVoid, throwable) -> {
                if (throwable != null) {
                    System.err.println("Failed to update loser stats: " + throwable.getMessage());
                } else {
                    System.out.println("Updated loser " + loserUser.firebaseId + " stats: losses=" + (loserUser.losses + 1) + ", rating=" + newLoserRating);
                }
            });
    }

    @Override
    public synchronized void addConnections(List<Connection> connections, List<User> users) {
        if (connections.size() != 2 || users.size() != 2) {
            System.err.println("1v1 requires exactly 2 players, got: " + connections.size());
            return;
        }

        List<Player> newPlayers = new ArrayList<>();
        for (int i = 0; i < connections.size(); i++) {
            Connection c = connections.get(i);
            User newPlayer = users.get(i);
            Vector2 spawnPoint = (i == 0) ? mapManager.getRespPlayerAll().get(0) : mapManager.getRespPlayerAll().get(1);
            Player p = new Player(
                newPlayer.nickname,
                newPlayer.level,
                newPlayer.experience,
                newPlayer.money,
                spawnPoint.x + GameConstants.Sprite.SIZE / 2,
                spawnPoint.y + GameConstants.Sprite.SIZE / 2,
                null,
                mapManager
            );
            p.setRespPoint(spawnPoint);
            p.setId(c.getID());
            p.setServer(server, roomId, "1v1");

            p.addInventoryWeapons(weaponLoader.getWeaponsFromMap(newPlayer.items.weapons));
            p.setCurrentWeapon(p.getInventory().getWeaponByName((String) newPlayer.items.weapons.get(newPlayer.equippedWeapon).get("name")));
            if (p.getCurrentWeapon() != null) {
                p.getCurrentWeapon().setEntity(p);
            } else {
                System.err.println("No weapon equipped for player " + p.getID());
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
                playersConnections.put(p.getID(), c);
                newPlayers.add(p);
                PLAYERS.add(p);
                killCounts.put(p.getID(), 0);
            }
            System.out.println("Added player " + p.getID() + " to 1v1 " + roomId + ", spawn: " + spawnPoint);


            Network.PvpMatchStart matchStart = new Network.PvpMatchStart();
            matchStart.playerId = p.getFirebaseId();
            matchStart.opponentId = users.get(1 - i).firebaseId;
            matchStart.opponentName = users.get(1 - i).nickname;
            c.sendTCP(matchStart);
        }

        for (int i = 0; i < connections.size(); i++) {
            broadcastState(connections.get(i), newPlayers.get(i), users.get(i));
        }
    }

    @Override
    public synchronized void removeConnection(Connection c) {
        Player p = playersById.get(c.getID());
        if (p != null) {
            PLAYERS.remove(p);
            playersById.remove(p.getID());
            playersConnections.remove(c.getID());
            killCounts.remove(c.getID());

            System.out.println("Removed connection ID " + c.getID() + " from 1v1 " + roomId);
            ConnectionManager.sendToConnectionsExcept("1v1", roomId, c.getID(), new Network.PlayerDeleted(c.getID()));

            // Award win to remaining player
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

    public void notifyKill(int killerId) {
        killCounts.merge(killerId, 1, Integer::sum);
        roundEnded = true;
        roundEndTimer = 3;
        System.out.println("Player " + killerId + " scored a kill, total: " + killCounts.get(killerId));
        if (killCounts.get(killerId) >= KILLS_TO_WIN) {
            mapManager.setGoingBack(true);
        }
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

            for (Player player : players) {
                if (player.getHp() <= 0) {
                    continue;
                }

                player.getCurrentWeapon().setDelta(delta);
                player.update(delta);
                player.regenerateMana(delta);
                player.regenerateHP(delta);
                mapManager.setCameraValues(player.cameraX, player.cameraY);

                if (player.getCurrentWeapon() instanceof RangedWeapon) {
                    ((RangedWeapon) player.getCurrentWeapon()).setCameraValues(player.cameraX, player.cameraY);
                }
                if (player.getCurrentWeapon() instanceof MagicWeapon) {
                    ((MagicWeapon) player.getCurrentWeapon()).setCameraValues(player.cameraX, player.cameraY);
                }

                if (player.getMovePlayer()) {
                    player.updateAnim();
                }

                if (player.getCurrentWeapon().getAttacking()) {
                    player.getCurrentWeapon().createHitbox(player.getEntityPos().x, player.getEntityPos().y);
                }

                if (player.getCurrentWeapon().getAttacking()) {
                    if (player.getCurrentWeapon() instanceof RangedWeapon || player.getCurrentWeapon() instanceof MeleeWeapon) {
                        player.getCurrentWeapon().update(delta);
                        player.getCurrentWeapon().checkHitboxCollisionsEntity(PLAYERS);
                        player.getCurrentWeapon().checkHitboxCollisionsMap(mapManager);
                    }
                }

                if (player.getCurrentWeapon() instanceof MagicWeapon) {
                    player.getCurrentWeapon().update(delta);
                    player.getCurrentWeapon().checkHitboxCollisionsEntity(PLAYERS);
                    player.getCurrentWeapon().checkHitboxCollisionsMap(mapManager);
                }

                if (server != null) {
                    Network.PlayerPos pos = new Network.PlayerPos(
                        player.getEntityPos().x / GameConstants.Sprite.SIZE,
                        player.getEntityPos().y / GameConstants.Sprite.SIZE,
                        player.getID()
                    );
                    ConnectionManager.sendToConnections(roomName, id, pos);

                    Network.CurrentHp hp = new Network.CurrentHp(); // Fixed typo
                    hp.idOfEnemy = player.getID();
                    hp.curHp = (int) player.getHp();
                    hp.isEnemy = false;
                    ConnectionManager.sendToConnections(roomName, id, hp);
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
                Network.CurrentHp hp = new Network.CurrentHp(); // Fixed typo
                hp.idOfEnemy = p.getID();
                hp.curHp = (int) p.getHp();
                hp.isEnemy = false;
                ConnectionManager.sendToConnections(roomName, id, hp);
            }
            Network.RoundReset reset = new Network.RoundReset();
            ConnectionManager.sendToConnections(roomName, id, reset);
            System.out.println("New round started in 1v1 " + id);
        }
    }
}
