package com.gamb1t.legacyforge.Networking;

import com.esotericsoftware.kryonet.Connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionManager {
    private static final Map<String, Map<Integer, List<Connection>>> connectionMap = new HashMap<>();

    public static synchronized void addConnection(String gameMode, int roomId, Connection connection) {
        connectionMap
            .computeIfAbsent(gameMode, k -> new HashMap<>())
            .computeIfAbsent(roomId, k -> new ArrayList<>())
            .add(connection);
        System.out.println("Added connection " + connection.getID() + " to " + gameMode + ", room " + roomId);
    }

    public static synchronized void removeConnection(String gameMode, int roomId, Connection connection) {
        Map<Integer, List<Connection>> roomMap = connectionMap.get(gameMode);
        if (roomMap != null) {
            List<Connection> connections = roomMap.get(roomId);
            if (connections != null) {
                connections.remove(connection);
                System.out.println("Removed connection " + connection.getID() + " from " + gameMode + ", room " + roomId);
                if (connections.isEmpty()) {
                    roomMap.remove(roomId);
                }
            }
            if (roomMap.isEmpty()) {
                connectionMap.remove(gameMode);
            }
        }
    }

    public static synchronized List<Connection> getConnections(String gameMode, int roomId) {
        Map<Integer, List<Connection>> roomMap = connectionMap.get(gameMode);
        return roomMap != null ? roomMap.getOrDefault(roomId, new ArrayList<>()) : new ArrayList<>();
    }

    public static synchronized void deleteRoom(String gameMode, int roomId) {
        Map<Integer, List<Connection>> roomMap = connectionMap.get(gameMode);
        if (roomMap != null) {
            roomMap.remove(roomId);
            System.out.println("Deleted room " + roomId + " from gameMode " + gameMode);
            if (roomMap.isEmpty()) {
                connectionMap.remove(gameMode);
                System.out.println("Removed gameMode " + gameMode + " as it has no more rooms");
            }
        }
    }


    public static synchronized void sendToConnections(String gameMode, int roomId, Object packet) {
        List<Connection> connections = new CopyOnWriteArrayList<>(getConnections(gameMode, roomId));
        for (Connection conn : connections) {
            conn.sendTCP(packet);
        }
        System.out.println("Sent packet to " + connections.size() + " connections in " + gameMode + ", room " + roomId);
    }


    public static synchronized void sendToConnectionsExcept(String gameMode, int roomId, int exceptId, Object packet) {
        List<Connection> connections = getConnections(gameMode, roomId);
        for (Connection conn : connections) {
            if (conn.getID() != exceptId) {
                conn.sendTCP(packet);
            }
        }
        System.out.println("Sent packet to " + (connections.size() - (connections.stream().anyMatch(c -> c.getID() == exceptId) ? 1 : 0)) + " connections (except " + exceptId + ") in " + gameMode + ", room " + roomId);
    }
}
