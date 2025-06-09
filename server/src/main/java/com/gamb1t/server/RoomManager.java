package com.gamb1t.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.gamb1t.legacyforge.Entity.Items;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.Networking.Network;
import com.gamb1t.legacyforge.Networking.ConnectionManager;
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class RoomManager {
    private final Server server;
    private final List<Room> rooms = new ArrayList<>();
    private final List<Dungeon> dungeons = new ArrayList<>();
    private final List<OneVsOne> rooms1v1 = new ArrayList<>();
    private int nextId = 1;
    private int dungeonNextId = 1;
    private int oneVsOneNextId = 1;
    private final Map<Connection, Room> connectionToRoom = new HashMap<>();
    private final Map<Connection, Dungeon> connectionDungeon = new HashMap<>();
    private final Map<Connection, OneVsOne> connection1v1 = new HashMap<>();
    private final List<PlayerInQueue> oneVsOneQueue = Collections.synchronizedList(new ArrayList<>());
    private final Map<CopyOnWriteArrayList<PlayerInQueue>, Float> pendingMatches = new ConcurrentHashMap<>();

    private static class PlayerInQueue {
        Connection connection;
        Player player;

        PlayerInQueue(Connection connection, Player player) {
            this.connection = connection;
            this.player = player;
        }
    }

    public RoomManager(Server s) {
        server = s;
        startMatchmaking();
    }

    public OneVsOne getRoomFor1v1(Connection c) {
        return connection1v1.get(c);
    }

    public synchronized void assignToOneVsOne(List<Connection> connections, List<Player> players, Squad squad) {
        OneVsOne oneVsOne = new OneVsOne(oneVsOneNextId++, "1v1", server, this);
        rooms1v1.add(oneVsOne);
        List<User> users = players.stream().map(this::convertPlayerToUser).collect(Collectors.toList());

        for (Connection connection : connections) {
            connection1v1.put(connection, oneVsOne);
            ConnectionManager.addConnection("1v1", oneVsOne.getRoomId(), connection);
            System.out.println("Assigned connection " + connection.getID() + " to 1v1 " + oneVsOne.getRoomId());
        }
        oneVsOne.addConnections(connections, users);
        new Thread(oneVsOne).start();
        for (Connection connection : connections) {
            Room room = connectionToRoom.get(connection);
            if (room != null) {
                room.removeConnection(connection);
                connectionToRoom.remove(connection);
                ConnectionManager.removeConnection("Hub", room.getRoomId(), connection);
                System.out.println("Sending PlayerDeleted packet for player ID: " + connection.getID() + " in room: " + room.getRoomId());
                ConnectionManager.sendToConnectionsExcept("Hub", room.getRoomId(), connection.getID(),
                    new Network.PlayerDeleted(connection.getID()));
                if (room.getPlayer().isEmpty()) {
                    ConnectionManager.deleteRoom("Hub", room.getRoomId());
                    rooms.remove(room);
                    System.out.println("Removed empty room " + room.getRoomId());
                }
            }
        }
    }

    public synchronized Room assignToRoom(Connection c, User user) {
        for (Room r : rooms) {
            if (!r.isFull()) {
                System.out.println("Added connection to existing room " + r.getRoomId());
                r.addConnection(c, user);
                connectionToRoom.put(c, r);
                ConnectionManager.addConnection("Hub", r.getRoomId(), c);
                return r;
            }
        }
        Room r = new Room(nextId++, "Hub", server, this);
        rooms.add(r);
        connectionToRoom.put(c, r);
        ConnectionManager.addConnection("Hub", r.getRoomId(), c);
        r.addConnection(c, user);
        new Thread(r).start();
        System.out.println("Created new room " + r.getRoomId());
        return r;
    }

    public synchronized void assignToDungeon(List<Connection> connections, List<Player> players, Squad squad) {
        Dungeon dungeon = new Dungeon(dungeonNextId++, "Dungeon", server, this);
        dungeons.add(dungeon);
        List<User> users = players.stream().map(this::convertPlayerToUser).collect(Collectors.toList());

        for (Connection connection : connections) {
            connectionDungeon.put(connection, dungeon);
            ConnectionManager.addConnection("Dungeon", dungeon.getDungeonId(), connection);
            System.out.println("Assigned connection " + connection.getID() + " to dungeon " + dungeon.getDungeonId());
        }
        dungeon.addConnections(connections, users);
        new Thread(dungeon).start();
        for (Connection connection : connections) {
            Room room = connectionToRoom.get(connection);
            if (room != null) {
                room.removeConnection(connection);
                connectionToRoom.remove(connection);
                ConnectionManager.removeConnection("Hub", room.getRoomId(), connection);
                System.out.println("Sending PlayerDeleted packet for player ID: " + connection.getID() + " in room: " + room.getRoomId());
                ConnectionManager.sendToConnectionsExcept("Hub", room.getRoomId(), connection.getID(),
                    new Network.PlayerDeleted(connection.getID()));
                if (room.getPlayer().isEmpty()) {
                    ConnectionManager.deleteRoom("Hub", room.getRoomId());
                    rooms.remove(room);
                    System.out.println("Removed empty room " + room.getRoomId());
                }
            }
        }
    }

    public synchronized Room assignToRoomBatch(List<Connection> connections, List<User> users) {
        if (connections.size() != users.size()) {
            System.err.println("Error: Mismatched connections and users sizes in assignToRoomBatch: " + connections.size() + " vs " + users.size());
            return null;
        }
        for (Room r : rooms) {
            if (r.getPlayer().size() + connections.size() <= Room.MAX_PLAYERS) {
                System.out.println("Adding " + connections.size() + " connections to existing room " + r.getRoomId());
                r.addConnections(connections, users);
                for (Connection c : connections) {
                    connectionToRoom.put(c, r);
                    ConnectionManager.addConnection("Hub", r.getRoomId(), c);
                }
                return r;
            }
        }
        Room r = new Room(nextId++, "Hub", server, this);
        rooms.add(r);
        r.addConnections(connections, users);
        for (Connection c : connections) {
            connectionToRoom.put(c, r);
            ConnectionManager.addConnection("Hub", r.getRoomId(), c);
        }
        new Thread(r).start();
        System.out.println("Created new room " + r.getRoomId() + " for " + connections.size() + " players");
        return r;
    }

    public synchronized void backToRoom(Connection connection, Player player) {
        if (connection == null || player == null) {
            System.err.println("Error: Invalid connection or player in backToRoom, connection=" + connection + ", player=" + player);
            return;
        }

        Dungeon dungeon = connectionDungeon.get(connection);
        if (dungeon != null) {
            System.out.println("Returning player ID " + connection.getID() + " to hub from dungeon " + dungeon.getDungeonId());
            dungeon.removeConnection(connection);
            connectionDungeon.remove(connection);
            ConnectionManager.removeConnection("Dungeon", dungeon.getDungeonId(), connection);
            ConnectionManager.sendToConnectionsExcept("Dungeon", dungeon.getDungeonId(), connection.getID(), new Network.PlayerDeleted(connection.getID()));

            if (dungeon.getPlayer().isEmpty()) {
                ConnectionManager.deleteRoom("Dungeon", dungeon.getDungeonId());
                dungeons.remove(dungeon);
                System.out.println("Dungeon " + dungeon.getDungeonId() + " is empty, removed");
            }
        } else {
            OneVsOne oneVsOne = connection1v1.get(connection);
            if (oneVsOne != null) {
                System.out.println("Returning player ID " + connection.getID() + " to hub from 1v1 " + oneVsOne.getRoomId());
                oneVsOne.removeConnection(connection);
                connection1v1.remove(connection);
                ConnectionManager.removeConnection("1v1", oneVsOne.getRoomId(), connection);
                ConnectionManager.sendToConnectionsExcept("1v1", oneVsOne.getRoomId(), connection.getID(), new Network.PlayerDeleted(connection.getID()));
                if (oneVsOne.getPlayer().isEmpty()) {
                    ConnectionManager.deleteRoom("1v1", oneVsOne.getRoomId());
                    rooms1v1.remove(oneVsOne);
                    System.out.println("1v1 " + oneVsOne.getRoomId() + " is empty, removed");
                }
            }
        }

        User user = convertPlayerToUser(player);
        Room room = assignToRoom(connection, user);
        System.out.println("Player ID " + connection.getID() + " assigned to hub room " + room.getRoomId());
    }

    public void removeConnection(Connection c) {
        Room room = connectionToRoom.get(c);
        if (room != null) {
            room.removeConnection(c);
            connectionToRoom.remove(c);
            ConnectionManager.removeConnection("Hub", room.getRoomId(), c);
            ConnectionManager.sendToConnectionsExcept("Hub", room.getRoomId(), c.getID(), new Network.PlayerDeleted(c.getID()));
            if (room.getPlayer().isEmpty()) {
                ConnectionManager.deleteRoom("Hub", room.getRoomId());
                rooms.remove(room);
                System.out.println("Removed empty room " + room.getRoomId());
            }
        }
    }

    public void removeFromDungeon(Connection c) {
        Dungeon dungeon = connectionDungeon.get(c);
        if (dungeon != null) {
            dungeon.removeConnection(c);
            connectionDungeon.remove(c);
            ConnectionManager.removeConnection("Dungeon", dungeon.getDungeonId(), c);
            ConnectionManager.sendToConnectionsExcept("Dungeon", dungeon.getDungeonId(), c.getID(), new Network.PlayerDeleted(c.getID()));
            if (dungeon.getPlayer().isEmpty()) {
                System.out.println("Dungeon deleted");
                ConnectionManager.deleteRoom("Dungeon", dungeon.getDungeonId());
                dungeons.remove(dungeon);
                System.out.println("Removed empty dungeon " + dungeon.getDungeonId());
            }
        }
    }

    public void removeFrom1v1(Connection c) {
        OneVsOne oneVsOne = connection1v1.get(c);
        if (oneVsOne != null) {
            oneVsOne.removeConnection(c);
            connection1v1.remove(c);
            ConnectionManager.removeConnection("1v1", oneVsOne.getRoomId(), c);
            ConnectionManager.sendToConnectionsExcept("1v1", oneVsOne.getRoomId(), c.getID(), new Network.PlayerDeleted(c.getID()));
            if (oneVsOne.getPlayer().isEmpty()) {
                System.out.println("1v1 deleted");
                ConnectionManager.deleteRoom("1v1", oneVsOne.getRoomId());
                rooms1v1.remove(oneVsOne);
                System.out.println("Removed empty 1v1 " + oneVsOne.getRoomId());
            }
        }
    }

    public void addTo1v1Queue(Connection c) {
        Room room = getRoomFor(c);
        if (room != null) {
            Player player = room.getPlayer().stream().filter(p -> p.getID() == c.getID()).findFirst().orElse(null);
            if (player != null) {
                synchronized (oneVsOneQueue) {
                    for (PlayerInQueue player2 : oneVsOneQueue) {
                        if (player2.connection.getID() == c.getID()) {
                            return;
                        }
                    }
                    oneVsOneQueue.add(new PlayerInQueue(c, player));
                }
                System.out.println("ADDED");
                Network.OneVsOneQueueUpdate update = new Network.OneVsOneQueueUpdate();
                update.inQueue = true;
                update.countdown = -1;
                update.opponentNames = new ArrayList<>();
                server.sendToTCP(c.getID(), update);
            }
        }
    }

    public void removeFrom1v1Queue(Connection c) {
        synchronized (oneVsOneQueue) {
            oneVsOneQueue.removeIf(p -> p.connection.getID() == c.getID());
        }
        System.out.println("Player " + c.getID() + " removed from 1v1 queue");
        Network.OneVsOneQueueUpdate update = new Network.OneVsOneQueueUpdate();
        update.inQueue = false;
        update.countdown = -1;
        update.opponentNames = new ArrayList<>();
        server.sendToTCP(c.getID(), update);
    }

    public boolean isInPendingMatch(Connection c) {
        for (CopyOnWriteArrayList<PlayerInQueue> match : pendingMatches.keySet()) {
            if (match.stream().anyMatch(p -> p.connection.getID() == c.getID())) {
                return true;
            }
        }
        return false;
    }

    private float getPendingMatchCountdown(Connection c) {
        for (Map.Entry<CopyOnWriteArrayList<PlayerInQueue>, Float> entry : pendingMatches.entrySet()) {
            if (entry.getKey().stream().anyMatch(p -> p.connection.getID() == c.getID())) {
                return entry.getValue();
            }
        }
        return -1;
    }

    private ArrayList<String> getPendingMatchOpponentNames(Connection c) {
        for (CopyOnWriteArrayList<PlayerInQueue> match : pendingMatches.keySet()) {
            if (match.stream().anyMatch(p -> p.connection.getID() == c.getID())) {
                return (ArrayList<String>) match.stream()
                    .filter(p -> p.connection.getID() != c.getID())
                    .map(p -> p.player.getName())
                    .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }


    public void removeFromPendingMatches(Connection c) {
        Iterator<Map.Entry<CopyOnWriteArrayList<PlayerInQueue>, Float>> iterator = pendingMatches.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<CopyOnWriteArrayList<PlayerInQueue>, Float> entry = iterator.next();
            CopyOnWriteArrayList<PlayerInQueue> match = entry.getKey();
            if (match.stream().anyMatch(p -> p.connection.getID() == c.getID())) {
                iterator.remove();
                System.out.println("Removed pending 1v1 match for disconnected player ID " + c.getID());
                // Notify the other player if still connected
                for (PlayerInQueue p : match) {
                    if (p.connection.getID() != c.getID() && p.connection.isConnected()) {
                        Network.OneVsOneQueueUpdate update = new Network.OneVsOneQueueUpdate();
                        update.inQueue = false;
                        update.countdown = -1;
                        update.opponentNames = new ArrayList<>();
                        server.sendToTCP(p.connection.getID(), update);
                        System.out.println("Notified player ID " + p.connection.getID() + " of cancelled 1v1 match due to opponent disconnection");
                    }
                }
            }
        }
    }

    private void startMatchmaking() {
        new Thread(() -> {
            while (true) {
                try {
                    Iterator<Map.Entry<CopyOnWriteArrayList<PlayerInQueue>, Float>> iterator = pendingMatches.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<CopyOnWriteArrayList<PlayerInQueue>, Float> entry = iterator.next();
                        CopyOnWriteArrayList<PlayerInQueue> match = entry.getKey();
                        float countdown = entry.getValue() - 1f;
                        System.out.println("Countdown for match " + match.stream().map(p -> p.player.getName()).collect(Collectors.toList()) + ": " + countdown);
                        if (countdown <= 0) {
                            System.out.println("Assigning match: " + match.stream().map(p -> p.player.getName()).collect(Collectors.toList()));
                            assignToOneVsOne(
                                match.stream().map(p -> p.connection).collect(Collectors.toList()),
                                match.stream().map(p -> p.player).collect(Collectors.toList()),
                                null
                            );
                            iterator.remove();
                        } else {
                            entry.setValue(countdown);
                            for (PlayerInQueue p : match) {
                                if (p.connection.isConnected()) {
                                    Network.OneVsOneQueueUpdate update = new Network.OneVsOneQueueUpdate();
                                    update.inQueue = true;
                                    update.countdown = (int) Math.ceil(countdown);
                                    update.opponentNames = (ArrayList<String>) match.stream().map(q -> q.player.getName()).collect(Collectors.toList());
                                    server.sendToTCP(p.connection.getID(), update);
                                }
                            }
                        }
                    }
                    System.out.println("Pending matches size: " + pendingMatches.size());

                    matchPlayers();

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Matchmaking thread interrupted: " + e.getMessage());
                    break;
                }
            }
        }).start();
    }

    private void matchPlayers() {
        synchronized (oneVsOneQueue) {
            System.out.println("matchPlayers called with queue size: " + oneVsOneQueue.size());
            if (oneVsOneQueue.size() < 2) {
                return;
            }
            Collections.shuffle(oneVsOneQueue);
            PlayerInQueue p1 = oneVsOneQueue.remove(0);
            PlayerInQueue p2 = oneVsOneQueue.remove(0);
            CopyOnWriteArrayList<PlayerInQueue> match = new CopyOnWriteArrayList<>(Arrays.asList(p1, p2));
            pendingMatches.put(match, 10f);
            for (PlayerInQueue p : match) {
                if (p.connection.isConnected()) {
                    Network.OneVsOneQueueUpdate update = new Network.OneVsOneQueueUpdate();
                    update.inQueue = true;
                    update.countdown = 10;
                    update.opponentNames = (ArrayList<String>) match.stream().map(q -> q.player.getName()).collect(Collectors.toList());
                    System.out.println("Sending to matched players: " + p.player.getName());
                    server.sendToTCP(p.connection.getID(), update);
                }
            }
        }
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

    public Room getRoomFor(Connection c) {
        return connectionToRoom.get(c);
    }

    public Dungeon getDungeonFor(Connection c){
        return connectionDungeon.get(c);
    }

    public boolean containsInRoom(Connection c) {
        return connectionToRoom.containsKey(c);
    }

    public boolean containsIn1v1Queue(Connection c) {
        synchronized (oneVsOneQueue) {
            for (PlayerInQueue player : oneVsOneQueue) {
                if (player.connection.equals(c)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsInDungeon(Connection c) {
        return connectionDungeon.containsKey(c);
    }

    public boolean containsIn1v1(Connection c) {
        return connection1v1.containsKey(c);
    }

    public List<Dungeon> getDungeons() {
        return dungeons;
    }

    public List<OneVsOne> get1v1Matches() {
        return rooms1v1;
    }
}
