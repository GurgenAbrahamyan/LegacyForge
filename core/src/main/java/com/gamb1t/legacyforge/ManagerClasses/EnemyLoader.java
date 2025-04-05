package com.gamb1t.legacyforge.ManagerClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Weapons.Weapon;
import com.badlogic.gdx.math.Vector2;

import java.io.IOException;
import java.util.ArrayList;


import java.util.ArrayList;

public class EnemyLoader {

    private final GameScreen gameScreen;
    private final Player player;
    private final ArrayList<Weapon> weapons;
    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<Vector2> respPos;

    public EnemyLoader(GameScreen gameScreen, Player player, ArrayList<Weapon> weapons, String jsonPath, ArrayList<Vector2> respPos) {
        this.gameScreen = gameScreen;
        this.player = player;
        this.weapons = weapons;
        this.respPos= respPos;
        System.out.println(respPos);
        loadEnemies(jsonPath);

    }

    public ArrayList<Enemy> loadEnemies(String jsonPath) {
        try {
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(Gdx.files.internal(jsonPath));

            for (JsonValue enemyData : root) {
                int hp = enemyData.getInt("hp", 100);
                int maxHp = enemyData.getInt("maxHp", hp);
                int damage = enemyData.getInt("damage", 10);
                float speed = enemyData.getFloat("speed", 100f);
                String weaponName = enemyData.getString("weapon");
                String spriteSheetPath = enemyData.getString("spriteSheet");

                Weapon weapon = findWeaponByName(weaponName);
                if (weapon == null) {
                    Gdx.app.error("EnemyLoader", "Weapon not found: " + weaponName);
                    continue;
                }
                System.out.println("works here");

                Enemy enemy = new Enemy(gameScreen, player, respPos, weapon);

                System.out.println( "works here???");
                enemy.setHp(hp);
                enemy.setMaxHp(maxHp);
                enemy.setDamage(damage);
                enemy.setSpeed(speed);
                enemy.setTexture(spriteSheetPath);


                enemies.add(enemy);
            }}
        catch (Exception e){
            Gdx.app.error("EnemyLoader", "Failed to load enemies: " + e.getMessage());
        }



        return enemies;
    }

    private Weapon findWeaponByName(String name) {
        for (Weapon weapon : weapons) {
            if (weapon.getName().equalsIgnoreCase(name)) {
                return weapon;
            }
        }
        return null;
    }
    public ArrayList<Enemy> getEnemyList(){
        return enemies;
    }
}
