package com.gamb1t.legacyforge.Entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Items implements Serializable {
    public Map<String, Map<String, Object>> weapons = new HashMap<>();
    public Map<String, Map<String, Object>> armor = new HashMap<>();

    public Items() {}

    public Items(Map<String, Map<String, Object>> weapons, Map<String, Map<String, Object>> armor) {
        this.weapons = weapons;
        this.armor = armor;
    }

    public String getFireBaseIdWeaponByName(String name) {
        if (name == null || name.isEmpty()) {
            System.err.println("Weapon name is null or empty");
            return null;
        }

        for (Map.Entry<String, Map<String, Object>> entry : weapons.entrySet()) {
            String id = entry.getKey();
            Map<String, Object> weaponData = entry.getValue();
            if (weaponData != null && name.equals(weaponData.get("name"))) {
                return id;
            }
        }

        System.err.println("No weapon found with name: " + name);
        return null;
    }
}
