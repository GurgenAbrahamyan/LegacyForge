package com.gamb1t.legacyforge.ManagerClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamb1t.legacyforge.Weapons.MeleeWeapon;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.io.IOException;
import java.util.ArrayList;

public class WeaponLoader {
    private ArrayList<Weapon> weaponList;

    public WeaponLoader(String resourceName) {
        weaponList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            FileHandle fileHandle = Gdx.files.internal(resourceName);

            if (!fileHandle.exists()) {
                throw new IllegalArgumentException("File not found: " + resourceName);
            }

            JsonNode jsonNode = objectMapper.readTree(fileHandle.read());

            if (jsonNode == null || !jsonNode.has("type")) {
                throw new IllegalArgumentException("Invalid JSON structure in: " + resourceName);
            }

            String type = jsonNode.get("type").asText();
            Weapon weapon;

            switch (type) {
                case "melee":
                    weapon = objectMapper.treeToValue(jsonNode, MeleeWeapon.class);
                    break;
                case "magic":
                    weapon = objectMapper.treeToValue(jsonNode, MagicWeapon.class);
                    break;
                case "ranged":
                    weapon = objectMapper.treeToValue(jsonNode, RangedWeapon.class);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown weapon type: " + type);
            }

            weaponList.add(weapon);
            Gdx.app.log("WeaponLoader", "Loaded weapon: " + weapon);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            Gdx.app.error("WeaponLoader", e.getMessage());
        }
        for(Weapon wp : weaponList){
            System.out.println(wp.getSprite());
        }
    }

    public ArrayList<Weapon> getWeaponList() {
        return weaponList;
    }
}
