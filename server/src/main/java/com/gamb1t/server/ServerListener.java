package com.gamb1t.server;

import com.esotericsoftware.kryonet.*;
import com.gamb1t.legacyforge.Networking.Network;

public class ServerListener implements Listener {
    private final RoomManager rm;
    public ServerListener(RoomManager rm){ this.rm = rm; }

    @Override public void connected(Connection c){


    }
    @Override public void disconnected(Connection c){
        rm.removeFromRoom(c);

    }

    @Override
    public void received(Connection c, Object o) {
        if (o instanceof Network.PlayerInitMessage) {

            System.out.println("NIGGA CONNECTED" + c.getID());
            rm.assignToRoom(c, (Network.PlayerInitMessage) o);


        }





        if( o instanceof Network.MovePacket){
          //  System.out.println( ((Network.MovePacket) o).x + " " + ((Network.MovePacket) o).y);
            rm.getRoomFor(c).applyMove((Network.MovePacket) o, c);
        }

        if( o instanceof Network.StopPlayerMove){
            System.out.println("stopped move for"+c.getID());
            rm.getRoomFor(c).cancleMove((Network.StopPlayerMove) o, c);
        }

        if( o instanceof  Network.AttackStartPacket){
            rm.getRoomFor(c).startAttack((Network.AttackStartPacket) o );
        }

        if(o instanceof  Network.AttackReleasePacket){
            rm.getRoomFor(c).endAttack((Network.AttackReleasePacket) o);
        }

        if(o instanceof  Network.AttackDragged){
            rm.getRoomFor(c).attackDragged((Network.AttackDragged) o);
        }
    }


}
