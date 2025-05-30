package com.gamb1t.clientside;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.esotericsoftware.kryonet.Client;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.Networking.Network;
import com.gamb1t.legacyforge.Networking.PlayerChangeListener;

import java.io.IOException;

public class ClientMain extends Game {

   public User user;


    Client client;
    public ClientGameScreen gameScreen;
    public ClientListener clientListener;
    public static AssetManager assetManager;
    public PlayerChangeListener playerChangeListener;
    String serverIp;

    public ClientMain(User user, PlayerChangeListener changeListener, String serverIp) {

        this.playerChangeListener = changeListener;
      this.user = user;
      this.serverIp = serverIp;
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

        clientListener = new ClientListener(this, user);
        client.addListener(clientListener);


        client.start();
        System.out.println(serverIp);

        try {
            client.connect(100000, serverIp, Network.TCP_PORT, Network.UDP_PORT);

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
        gameScreen = new ClientGameScreen(sm.user, clientListener.getStateMessageOnConnection(), client, playerChangeListener);
        setScreen(gameScreen);


    }

    public void initGamescreenDungeon(Network.StateMessageOnConnection sm) {

        gameScreen = null;
        gameScreen = new ClientGameScreen(sm.user, clientListener.getStateMessageOnConnection(), client, playerChangeListener);
        setScreen(gameScreen);

    }



    public int getMoney() {
        return gameScreen.getPlayerMoney();
    }


}
