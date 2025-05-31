package com.gamb1t.server;

import com.esotericsoftware.kryonet.*;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.Networking.Network;

public class ServerListener implements Listener {
    private final RoomManager rm;

    public ServerListener(RoomManager rm) {
        this.rm = rm;
    }

    @Override
    public void connected(Connection c) {
        // Connection handling if needed
    }

    @Override
    public void disconnected(Connection c) {

        rm.removeFromDungeon(c);
        rm.removeFrom1v1(c);
    }

    @Override
    public void received(Connection c, Object o) {
        if (o instanceof User) {
            rm.assignToRoom(c, (User) o);
        }

         if (o instanceof Network.SquadAction) {
            Network.SquadAction action = (Network.SquadAction) o;
            if (rm.containsInRoom(c)) {
                rm.getRoomFor(c).handleSquadAction(action.playerId, action.action);
            }
        }

        if (o instanceof Network.OnPlayerBoughtWeapon) {
            if (rm.containsInRoom(c)) rm.getRoomFor(c).playerAddNewWeapon((Network.OnPlayerBoughtWeapon) o, c);
        }

        if (o instanceof Network.OnPlayerEquipWeapon) {
            if (rm.containsInRoom(c)) rm.getRoomFor(c).playerEquipWeapon((Network.OnPlayerEquipWeapon) o, c);
        }

        if (o instanceof Network.OnPlayerBoughtArmor) {
            if (rm.containsInRoom(c)) rm.getRoomFor(c).playerAddNewArmor((Network.OnPlayerBoughtArmor) o, c);
        }

        if (o instanceof Network.OnPlayerEquipArmor) {
            if (rm.containsInRoom(c)) rm.getRoomFor(c).playerEquipArmor((Network.OnPlayerEquipArmor) o, c);
        }

        if (o instanceof Network.MovePacket) {
            if (rm.containsInRoom(c)) rm.getRoomFor(c).applyMove((Network.MovePacket) o, c);
            else if (rm.containsInDungeon(c)) rm.getDungeonFor(c).applyMove((Network.MovePacket) o, c);
            else if (rm.containsIn1v1(c)) rm.getRoomFor1v1(c).applyMove((Network.MovePacket) o, c);
        }

        if (o instanceof Network.StopPlayerMove) {
            System.out.println("stopped move for " + c.getID());
            if (rm.containsInRoom(c)) rm.getRoomFor(c).cancelMove((Network.StopPlayerMove) o, c);
            else if (rm.containsInDungeon(c)) rm.getDungeonFor(c).cancleMove((Network.StopPlayerMove) o, c);
            else if (rm.containsIn1v1(c)) rm.getRoomFor1v1(c).cancelMove((Network.StopPlayerMove) o, c);
        }

        if (o instanceof Network.AttackStartPacket) {
            if (rm.containsInRoom(c)) rm.getRoomFor(c).startAttack((Network.AttackStartPacket) o);
            else if (rm.containsInDungeon(c)) rm.getDungeonFor(c).startAttack((Network.AttackStartPacket) o);
            else if (rm.containsIn1v1(c)) rm.getRoomFor1v1(c).startAttack((Network.AttackStartPacket) o);
        }

        if (o instanceof Network.AttackReleasePacket) {
            if (rm.containsInRoom(c)) rm.getRoomFor(c).endAttack((Network.AttackReleasePacket) o);
            else if (rm.containsInDungeon(c)) rm.getDungeonFor(c).endAttack((Network.AttackReleasePacket) o);
            else if (rm.containsIn1v1(c)) rm.getRoomFor1v1(c).endAttack((Network.AttackReleasePacket) o);
        }

        if (o instanceof Network.AttackDragged) {
            if (rm.containsInRoom(c)) rm.getRoomFor(c).attackDragged((Network.AttackDragged) o);
            else if (rm.containsInDungeon(c)) rm.getDungeonFor(c).attackDragged((Network.AttackDragged) o);
            else if (rm.containsIn1v1(c)) rm.getRoomFor1v1(c).attackDragged((Network.AttackDragged) o);
        }

    }
}
