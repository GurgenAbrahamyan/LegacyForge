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
import java.util.HashMap;
import java.util.Map;

public class WeaponLoader {
    private final ArrayList<Weapon> weaponList = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, Class<? extends Weapon>> weaponTypeMap = new HashMap<>();

    static {
        weaponTypeMap.put("melee", MeleeWeapon.class);
        weaponTypeMap.put("magic", MagicWeapon.class);
        weaponTypeMap.put("ranged", RangedWeapon.class);
    }

    public WeaponLoader(String resourceName) {
        loadWeapons(resourceName);
    }

    private void loadWeapons(String resourceName) {
        try {
            FileHandle fileHandle = Gdx.files.internal(resourceName);
            if (!fileHandle.exists()) {
                throw new IllegalArgumentException("File not found: " + resourceName);
            }

            JsonNode rootNode = objectMapper.readTree(fileHandle.read());

            if (!rootNode.isArray()) {
                throw new IllegalArgumentException("Invalid JSON structure: Expected an array.");
            }

            for (JsonNode jsonNode : rootNode) {
                JsonNode typeNode = jsonNode.get("type");
                if (typeNode == null) {
                    Gdx.app.error("WeaponLoader", "Skipping weapon with missing 'type' field.");
                    continue;
                }

                String type = typeNode.asText();
                Class<? extends Weapon> weaponClass = weaponTypeMap.get(type);

                if (weaponClass == null) {
                    Gdx.app.error("WeaponLoader", "Unknown weapon type: " + type);
                    continue;
                }

                Weapon weapon = objectMapper.treeToValue(jsonNode, weaponClass);
                weaponList.add(weapon);
                Gdx.app.log("WeaponLoader", "Loaded weapon: " + weapon.getName());
            }

        } catch (IOException | IllegalArgumentException e) {
            Gdx.app.error("WeaponLoader", "Error loading weapons: " + e.getMessage());
        }
    }

    public ArrayList<Weapon> getWeaponList() {
        return weaponList;
    }
}
