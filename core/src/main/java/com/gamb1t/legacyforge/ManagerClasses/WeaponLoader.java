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
import java.util.*;

public class WeaponLoader {
    private final ArrayList<Weapon> weaponList = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String recourcePath;
    private boolean isClient;
    private final Map<String, JsonNode> weaponJsonMap = new HashMap<>();

    private static final Map<String, Class<? extends Weapon>> weaponTypeMap = new HashMap<>();
    static {
        weaponTypeMap.put("melee", MeleeWeapon.class);
        weaponTypeMap.put("magic", MagicWeapon.class);
        weaponTypeMap.put("ranged", RangedWeapon.class);
    }

    public WeaponLoader(String resourceName, boolean isClient) {
        this.recourcePath = resourceName;
        this.isClient = isClient;
        loadWeapons(resourceName);
    }

    private void loadWeapons(String resourceName) {
        try {
            FileHandle file = Gdx.files.internal(resourceName);
            if (!file.exists()) {
                throw new IOException("File not found: " + resourceName);
            }
            System.out.println("good here 1");

            JsonNode root = objectMapper.readTree(file.read());
            if (!root.isArray()) {
                throw new IllegalArgumentException("Invalid JSON structure: Expected an array.");
            }
            System.out.println("Good here 2");

            for (JsonNode node : root) {
                Weapon weapon = parseWeaponNode(node);
                if (weapon != null) {
                    weaponList.add(weapon);
                    JsonNode nameNode = node.get("name");
                    if (nameNode != null && nameNode.isTextual()) {
                        String nameLower = nameNode.asText().toLowerCase();
                        weaponJsonMap.put(nameLower, node);
                        System.out.println(nameLower);
                        Gdx.app.log("WeaponLoader", "Loaded: " + nameNode.asText());
                    } else {
                        Gdx.app.error("WeaponLoader", "Missing valid 'name' for field, cannot store JSON node");
                    }
                }
            }
            Gdx.app.log("WeaponLoader", "Total weapons: " + weaponList.size());
        } catch (Exception e) {
            Gdx.app.error("WeaponLoader", "Load error: " + e.getMessage());
        }
    }

    private Weapon parseWeaponNode(JsonNode node) {
        // Validate type
        JsonNode typeNode = node.get("type");
        if (typeNode == null || !typeNode.isTextual()) {
            Gdx.app.error("WeaponLoader", "Skipping weapon with missing or invalid 'type' field");
            System.out.println("Good here 3");
            return null;
        }
        String type = typeNode.asText();
        System.out.println("Good here type: 4 " + type);

        Class<? extends Weapon> weaponClass = weaponTypeMap.get(type);
        System.out.println("Good here 5");
        if (weaponClass == null) {
            Gdx.app.error("WeaponLoader", "Unknown weapon type: " + type);
            return null;
        }

        // Create weapon
        Weapon weapon;
        try {
            weapon = weaponClass.getDeclaredConstructor().newInstance();
            System.out.println("Good here 6");
            System.out.println("Parsing node: " + node);
        } catch (Exception e) {
            Gdx.app.error("WeaponLoader", "Failed to create " + type + ": " + e.getMessage());
            return null;
        }

        try {

            if (node.has("name") && node.get("name").isTextual()) {
                weapon.setName(node.get("name").asText());
                System.out.println("Good here 7");
            } else {
                Gdx.app.error("WeaponLoader", "Missing name for " + node);
                return null;
            }
            if (node.has("damage") && node.get("damage").isNumber()) {
                weapon.setDamage((float) node.get("damage").asDouble());
            }
            if (node.has("attackSpeed") && node.get("attackSpeed").isNumber()) {
                weapon.setAttackSpeed((float) node.get("attackSpeed").asDouble());
            }
            if (node.has("range") && node.get("range").isNumber()) {
                weapon.setRange((float) node.get("range").asDouble());
            }
            if (node.has("knockbackInTiles") && node.get("knockbackInTiles").isNumber()) {
                weapon.setKnockbackInTiles(node.get("knockbackInTiles").asInt());
            }
            if (node.has("frameAmountX") && node.get("frameAmountX").isInt()) {
                weapon.setFrameAmountX(node.get("frameAmountX").asInt());
            }
            if (node.has("frameAmountY") && node.get("frameAmountY").isInt()) {
                weapon.setFrameAmountY(node.get("frameAmountY").asInt());
            }
            if (node.has("sprite") && node.get("sprite").isTextual()) {
                String spritePath = node.get("sprite").asText();
                weapon.setSprite(spritePath);
                try {
                    if(isClient){
                    weapon.setTexture(spritePath);
                    weapon.convertTxtRegToSprite();
                    System.out.println("Good here 8");}
                } catch (Exception e) {
                    Gdx.app.error("WeaponLoader", "Texture load failed for " + weapon.getName() + ": " + e.getMessage());
                    return null;
                }
            }
            if (node.has("price") && node.get("price").isInt()) {
                weapon.setPrice(node.get("price").asInt());
            }
            weapon.setIsClient(isClient);
            System.out.println("Good here 9");

            if (weapon instanceof RangedWeapon && node.has("projectilePath") && node.get("projectilePath").isTextual()) {
                String projectilePath = node.get("projectilePath").asText();
                ((RangedWeapon) weapon).setProjectiles(projectilePath);
                try {
                    if(isClient){
                    ((RangedWeapon) weapon).initProj();}
                } catch (Exception e) {
                    Gdx.app.error("WeaponLoader", "Projectile init failed for " + weapon.getName() + ": " + e.getMessage());
                    return null;
                }
            }
            if (weapon instanceof MagicWeapon) {
                if (node.has("projectilePath") && node.get("projectilePath").isTextual()) {
                    String projectilePath = node.get("projectilePath").asText();
                    ((MagicWeapon) weapon).setProjectiles(projectilePath);
                    try {
                        if(isClient){
                        ((MagicWeapon) weapon).initProj();}
                    } catch (Exception e) {
                        Gdx.app.error("WeaponLoader", "Projectile init failed for " + weapon.getName() + ": " + e.getMessage());
                        return null;
                    }
                }
                if (node.has("mannaUsage") && node.get("mannaUsage").isInt()) {
                    ((MagicWeapon) weapon).setMannaUsage(node.get("mannaUsage").asInt());
                }
            }
        } catch (Exception e) {
            Gdx.app.error("WeaponLoader", "Error parsing weapon " + node.get("name") + ": " + e.getMessage());
            return null;
        }

        return weapon;
    }

    public ArrayList<Weapon> getWeaponsFromMap(Map<String, Map<String, Object>> weaponsMap) {
        ArrayList<Weapon> result = new ArrayList<>();
        if (weaponsMap == null || weaponsMap.isEmpty()) return result;

        for (Map.Entry<String, Map<String, Object>> entry : weaponsMap.entrySet()) {
            String weaponId = entry.getKey();
            Map<String, Object> weaponData = entry.getValue();
            if (weaponData == null || !weaponData.containsKey("name")) continue;

            Object nameObj = weaponData.get("name");
            if (!(nameObj instanceof String)) continue;

            Weapon copy = deepCopyWeapon((String) nameObj);
            if (copy == null) continue;

            if (weaponData.containsKey("level") && weaponData.get("level") instanceof Integer) {
                copy.setLevel((Integer) weaponData.get("level"));
            }

            copy.setFirebaseId(weaponId);
            result.add(copy);
        }
        return result;
    }

    public Weapon deepCopyWeapon(String weaponName) {
        JsonNode node = weaponJsonMap.get(weaponName.toLowerCase());
        if (node == null) {
            Gdx.app.error("WeaponLoader", "No JSON node found for weapon name: " + weaponName);
            return null;
        }
        return parseWeaponNode(node);
    }

    public ArrayList<Weapon> getWeaponList() {
        return weaponList;
    }

    public Weapon getWeaponByName(String name) {
        if (name == null) return null;
        for (Weapon weapon : weaponList) {
            if (weapon.getName().equalsIgnoreCase(name)) {
                return weapon;
            }
        }
        Gdx.app.error("WeaponLoader", "Weapon not found: " + name);
        return weaponList.size() > 1 ? weaponList.get(1) : null;
    }

    public String getRecourcePath() {
        return recourcePath;
    }
}
