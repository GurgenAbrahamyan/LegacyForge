package com.gamb1t.legacyforge.ManagerClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.Weapons.Weapon;
import com.badlogic.gdx.math.Vector2;

import java.io.IOException;
import java.util.ArrayList;


import java.util.ArrayList;

public class EnemyLoader {

    private final ArrayList<Player> player;
    private final ArrayList<Weapon> weapons;
    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<Vector2> respPos;

    MapManaging mapManaging;

   ArrayList <String> spritesheetPath = new ArrayList<>();

    public EnemyLoader(ArrayList<Player> player, ArrayList<Weapon> weapons, String jsonPath, ArrayList<Vector2> respPos, MapManaging mapManaging) {

        this.player = player;
        this.weapons = weapons;
        this.respPos= respPos;
        System.out.println(respPos);
        this.mapManaging = mapManaging;
        loadEnemies(jsonPath);


    }

    public ArrayList<Enemy> loadEnemies(String jsonPath) {
        try {
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(Gdx.files.internal(jsonPath));

            for (JsonValue enemyData : root) {
                int id =0;
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


                Enemy enemy = new Enemy(player, respPos.get(0), weapon, mapManaging);

                enemy.setHp(hp);
                enemy.setMaxHp(maxHp);
                enemy.setDamage(damage);
                enemy.setSpeed(speed);
                enemy.setId(id);
                enemy.setRespPos(mapManaging.getRespEenemy());

                enemies.add(enemy);
                id++;
            }}
        catch (Exception e){
            e.printStackTrace();  }



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

    public ArrayList<String> getSpritesheetPath() {
        return spritesheetPath;
    }


}
