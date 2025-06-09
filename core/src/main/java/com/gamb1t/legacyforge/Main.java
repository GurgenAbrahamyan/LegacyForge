package com.gamb1t.legacyforge;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gamb1t.legacyforge.Entity.Items;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;
import com.gamb1t.legacyforge.Networking.PlayerChangeListener;
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {

    public static AssetManager assets;

    User user;
    public GameScreen gameScreen;
    PlayerChangeListener playerChangeListener;


    public Main (User user, PlayerChangeListener playerChangeListener){

        this.user = user;
        this.playerChangeListener = playerChangeListener;


    }



    @Override
    public void create() {

        assets = new AssetManager();


        gameScreen = new GameScreen(user, playerChangeListener, this);

        this.setScreen(gameScreen);


    }

    public void startSinglePlayerDungeon(){
        Player player = gameScreen.getPlayer();
        User user = convertPlayerToUser(player);
    gameScreen = new GameScreen(user, playerChangeListener, this, 1);

        this.setScreen(gameScreen);
    }

    public int getMoney(){


        return gameScreen.getPlayerMoney();


    }

    public boolean isInitialized(){
        if(gameScreen !=  null){
            return true;
        }
        return false;
    }




    @Override
    public void render() {
        super.render();

    }

    @Override
    public void dispose() {
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
}
