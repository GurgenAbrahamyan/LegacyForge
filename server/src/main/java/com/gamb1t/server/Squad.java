package com.gamb1t.server;

import com.esotericsoftware.kryonet.Connection;
import com.gamb1t.legacyforge.Entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Squad {
    private static int nextSquadId = 1;
    private final int id;
    private final Map<Player, Connection> membersConnections = new ConcurrentHashMap<>();
    private static final int MAX_MEMBERS = 4;
    private static final float COUNTDOWN_DURATION = 10;
    private float countdown = -1;
    private boolean isActive = false;

    public Squad(Player leader, Connection connection) {
        this.id = nextSquadId++;
            membersConnections.put(leader, connection);
        this.isActive = true;
        startCountdown();
    }

    public void addMember(Player player, Connection connection) {
            if (!isFull() && isActive && !membersConnections.containsKey(player)) {
                membersConnections.put( player, connection);
                if (countdown < 0) {
                    startCountdown();
                }
            }
    }

    public void removeMember(Player player, Connection connection) {

            if(membersConnections.containsKey(player)){
                membersConnections.remove(player);
                if (membersConnections.isEmpty()) {
                    isActive = false;
                    countdown = -1;
                }
            }
    }

    public void update(float delta) {
        if (isActive && countdown > 0) {
            countdown -= delta;
            if (countdown <= 0) {
                countdown = 0;
            }
        }
    }


    public int getId() {
        return id;
    }

    public Map<Player, Connection> getMembersConnections() {
        return membersConnections;
    }

    public boolean isFull() {
        return membersConnections.size() >= MAX_MEMBERS;
    }

    public boolean isActive() {
        return isActive;
    }

    public float getCountdown() {
        return countdown;
    }

    private void startCountdown() {
        countdown = COUNTDOWN_DURATION;
    }
}
