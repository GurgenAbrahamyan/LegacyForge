package com.gamb1t.legacyforge.ManagerClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.Weapons.Weapon;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyLoader {

    private final List<Player> player;
    private final ArrayList<Weapon> weapons;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Vector2> respPos;
    private MapManaging mapManaging;
    private String jsonPath;
    private ArrayList<String> spritesheetPath = new ArrayList<>();
    private Random random = new Random();
    private ArrayList<Weapon> usedWeapon = new ArrayList<>();

    public EnemyLoader(List<Player> player, ArrayList<Weapon> weapons, String jsonPath, ArrayList<Vector2> respPos, MapManaging mapManaging) {
        this.player = player;
        this.weapons = weapons;
        this.respPos = respPos;
        this.mapManaging = mapManaging;
        this.jsonPath = jsonPath;
        loadEnemies(jsonPath);
    }

    public ArrayList<Enemy> loadEnemies(String jsonPath) {
        try {
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(Gdx.files.internal(jsonPath));

            if (respPos.size() < root.size) {
                Gdx.app.error("EnemyLoader", "Not enough spawn positions (" + respPos.size() + ") for " + root.size + " enemies");
                return enemies;
            }

            for (JsonValue enemyData : root) {
                int hp = enemyData.getInt("hp", 100);
                int maxHp = enemyData.getInt("maxHp", hp);
                int damage = enemyData.getInt("damage", 10);
                float speed = enemyData.getFloat("speed", 100f);
                String weaponName = enemyData.getString("weapon");
                spritesheetPath.add(enemyData.getString("spriteSheet"));

                Weapon weapon = findWeaponByName(weaponName);
                if (weapon == null) {
                    Gdx.app.error("EnemyLoader", "Weapon not found: " + weaponName);
                    continue;
                }

                int randomIndex = random.nextInt(respPos.size());
                Vector2 spawnPos = respPos.get(randomIndex);
                respPos.remove(randomIndex);

                Enemy enemy = new Enemy(player, spawnPos, weapon, mapManaging);

                enemy.setHp(hp);
                enemy.setMaxHp(maxHp);
                enemy.setDamage(damage);
                enemy.setSpeed(speed);
                enemy.setId(enemies.size());



                Gdx.app.log("EnemyLoader", "New enemy created at position: " + spawnPos);

                enemies.add(enemy);
            }

            mapManaging.addEnemiesForDoor(enemies);

        } catch (Exception e) {
            e.printStackTrace();
            Gdx.app.error("EnemyLoader", "Error loading enemies from " + jsonPath + ": " + e.getMessage());
        }

        return enemies;
    }

    private Weapon findWeaponByName(String name) {
        for (Weapon weapon : weapons) {
            if (weapon.getName().equalsIgnoreCase(name) && !usedWeapon.contains(weapon)) {
                usedWeapon.add(weapon);
                return weapon;
            }
        }
        return null;
    }

    public ArrayList<Enemy> getEnemyList() {
        return enemies;
    }

    public ArrayList<String> getSpritesheetPath() {
        return spritesheetPath;
    }

    public String getJsonPath() {
        return jsonPath;
    }
}
