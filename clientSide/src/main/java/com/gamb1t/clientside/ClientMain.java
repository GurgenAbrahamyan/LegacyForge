package com.gamb1t.clientside;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.esotericsoftware.kryonet.Client;
import com.gamb1t.legacyforge.Entity.Items;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.Networking.Network;
import com.gamb1t.legacyforge.Networking.PlayerChangeListener;
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientMain extends Game {

    public User user;
    private Client client;
    public ClientGameScreen gameScreen;
    public ClientListener clientListener;
    public static AssetManager assetManager;
    public PlayerChangeListener playerChangeListener;
    private String serverIp;
    private boolean isIntentionalDisconnect = false; // New flag

    public ClientMain(User user, PlayerChangeListener changeListener, String serverIp) {
        this.playerChangeListener = changeListener;
        this.user = user;
        this.serverIp = serverIp;
    }

    public boolean isInitialized() {
        return gameScreen != null;
    }

    public void disconnect() {
        if (client != null && client.isConnected()) {
            isIntentionalDisconnect = true;
            client.stop();
            client.close();
            System.out.println("Disconnected from server (intentional)");
        }
    }

    public boolean isIntentionalDisconnect() {
        return isIntentionalDisconnect;
    }

    @Override
    public void create() {
        assetManager = new AssetManager();

        client = new Client(131072, 131072);
        Network.register(client.getKryo());

        clientListener = new ClientListener(this, user);
        client.addListener(clientListener);

        client.start();
        System.out.println(serverIp);

        try {
            client.connect(5000, serverIp, Network.TCP_PORT, Network.UDP_PORT);
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            Gdx.app.postRunnable(() -> {
                if (playerChangeListener != null) {
                    playerChangeListener.onReturnToGameModeSelection(false);
                }
            });
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

    @Override
    public void dispose() {
           }
}
