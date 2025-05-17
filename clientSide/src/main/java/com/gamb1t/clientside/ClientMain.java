package com.gamb1t.clientside;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.gamb1t.legacyforge.Networking.Network;

import java.io.IOException;

public class ClientMain extends Game {

    public String name;
    public int experience;
    public int level;
    public int hp;
    public int money;

    Client client;
    public ClientGameScreen gameScreen;
    public ClientListener clientListener;
    public static AssetManager assetManager;

    public ClientMain(String name, int experience, int level, int hp, int money) {

        this.name = name;
        this.experience = experience;
        this.level = level;
        this.hp = hp;
        this.money = money;

    }

    public boolean isInitialized() {
        if (gameScreen != null) {
            return true;
        }
        return false;
    }




    @Override
    public void create() {
        assetManager  = new AssetManager();

        client = new Client();
        Network.register(client.getKryo());

        clientListener = new ClientListener(this, name, experience, level, hp, money);
        client.addListener(clientListener);


        client.start();

        try {
            client.connect(100000, "192.168.10.139", Network.TCP_PORT, Network.UDP_PORT);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        new Thread(() -> {
            while (!client.isConnected()) {
                try {
                    client.update(16);
                    Thread.sleep(16);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    public void initGamescreen(Network.StateMessageOnConnection sm) {
        gameScreen = new ClientGameScreen(name, experience, level, money, clientListener.getStateMessageOnConnection(), client, assetManager);
        setScreen(gameScreen);
    }

    public int getMoney() {
        return gameScreen.getPlayerMoney();
    }

}
