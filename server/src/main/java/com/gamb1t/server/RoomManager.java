package com.gamb1t.server;

import com.esotericsoftware.kryonet.Connection;
import java.util.*;
import com.esotericsoftware.kryonet.Server;
import com.gamb1t.legacyforge.Networking.Network;

public class RoomManager {

    private final Server server;
    private final List<Room> rooms = new ArrayList<>();
    private int nextId = 1;

    private final Map<Connection, Room> connectionToRoom = new HashMap<>();

    public RoomManager(Server s){ server = s;
    }

    public synchronized Room assignToRoom(Connection c, Network.PlayerInitMessage initMessage){

        for(Room r:rooms){
            if(!r.isFull()){
                System.out.println("added connection");
                r.addConnection(c, initMessage);
                connectionToRoom.put(c, r);
                return r;
            }

        }
        Room r = new Room(nextId++, server);
        rooms.add(r);
        connectionToRoom.put(c, r);
        r.addConnection(c, initMessage);
        new Thread(r).start();

        return r;
    }

    public void removeFromRoom(Connection c) {
        Room room = connectionToRoom.get(c);
        if (room != null) {
            room.removeConnection(c);
            connectionToRoom.remove(c);
        }
        server.sendToAllExceptTCP(c.getID(), new Network.PlayerDeleted(c.getID()));
    }

    public Room getRoomFor(Connection c) {
        return connectionToRoom.get(c);
    }





}

