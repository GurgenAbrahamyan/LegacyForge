package com.gamb1t.server;

import com.badlogic.gdx.ApplicationListener;
import com.esotericsoftware.kryonet.Server;
import com.gamb1t.legacyforge.Networking.Network;

import java.io.IOException;

public class ServerCycle implements ApplicationListener {

    @Override
    public void create() {
        Server server = new Server( );
        Network.register(server.getKryo());

        RoomManager rm = new RoomManager(server);
        server.addListener(new ServerListener(rm));


        try {
            server.bind(Network.TCP_PORT, Network.UDP_PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        server.start();










    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
