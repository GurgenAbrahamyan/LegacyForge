package com.gamb1t.legacyforge.Networking;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Client;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Weapons.Weapon;

public class NetworkInputSender implements InputSender {
    private final Client client;
    private final Player player;
    private final Weapon weapon;
    public NetworkInputSender(Player player, Weapon weapon, Client client) {
        this.player = player;
        this.weapon = weapon;
        this.client = client;
    }

    @Override
    public void sendAttack(float angle) {
        client.sendTCP(new Network.AttackReleasePacket(angle, player.getID(), false));
    }


    @Override
    public void sendMove(Vector2 direction) {
        client.sendUDP(new Network.MovePacket(direction.x, direction.y, player.getID()));
    }

    @Override
    public void attackStart(boolean bool) {
        client.sendTCP(new Network.AttackStartPacket(player.getID(), false));
    }

    @Override
    public void stopMove()
    {

       Network.StopPlayerMove idk =  new Network.StopPlayerMove();
       idk.id = player.getID();
        client.sendTCP(idk);
    }

    @Override
    public void sendAttackDraged(float angle) {

        client.sendUDP(new Network.AttackDragged(angle, player.getID(), false));

    }


}
