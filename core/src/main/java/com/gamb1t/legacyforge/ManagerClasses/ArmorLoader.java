package com.gamb1t.legacyforge.ManagerClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamb1t.legacyforge.Weapons.Armor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ArmorLoader {
    private final ArrayList<Armor> armorList = new ArrayList<>();
    private final Map<String, JsonNode> armorJsonMap = new HashMap<>();
    private final Map<String, Armor> armorByName = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String resourcePath;

    public ArmorLoader(String resourcePath) {
        this.resourcePath = resourcePath;
        loadArmors(resourcePath);
    }

    private void loadArmors(String resourceName) {
        try {
            FileHandle file = Gdx.files.internal(resourceName);

            if (!file.exists()) throw new IllegalArgumentException("File not found: " + resourceName);
            System.out.println("good here 1");
            JsonNode root = objectMapper.readTree(file.read());

            if (!root.isArray()) throw new IllegalArgumentException("Expected array in " + resourceName);


            for (JsonNode node : root) {
                JsonNode nameNode = node.get("name");

                if (nameNode == null || !nameNode.isTextual()) {
                    Gdx.app.error("ArmorLoader", "Skipping armor without valid 'name'");
                    continue;
                }
                String name = nameNode.asText();

                armorJsonMap.put(name.toLowerCase(), node);

                Armor a = objectMapper.treeToValue(node, Armor.class);

                armorList.add(a);

                armorByName.put(name.toLowerCase(), a);

                Gdx.app.log("ArmorLoader", "Loaded armor: " + name);
            }
        } catch (IOException | IllegalArgumentException e) {
            Gdx.app.error("ArmorLoader", "Error loading armors: " + e.getMessage());

        }
    }

    public Armor deepCopyArmor(String armorName) {
        if (armorName == null) return null;
        JsonNode node = armorJsonMap.get(armorName.toLowerCase());
        if (node == null) {
            Gdx.app.error("ArmorLoader", "No JSON found for armor: " + armorName);
            return null;
        }
        try {

            Armor a = objectMapper.treeToValue(node, Armor.class);
            System.out.println("deep copied");
            return a;


        } catch (Exception e) {
            Gdx.app.error("ArmorLoader", "Failed to copy armor: " + armorName, e);
            return null;
        }
    }

    public ArrayList<Armor> getArmorsFromMap(Map<String, Map<String, Object>> armorsMap) {
        ArrayList<Armor> out = new ArrayList<>();
        if (armorsMap == null) return out;

        for (Map.Entry<String, Map<String, Object>> e : armorsMap.entrySet()) {
            String firebaseId = e.getKey();
            Map<String, Object> data = e.getValue();
            if (data == null || !data.containsKey("name")) continue;
            Object nameObj = data.get("name");

            if (!(nameObj instanceof String)) continue;

            System.out.println((String) nameObj);

            Armor copy = deepCopyArmor((String) nameObj);
            if (copy == null) continue;
            
            if (data.containsKey("level") && data.get("level") instanceof Number) {
                copy.setLevel(((Number) data.get("level")).intValue());
            }

            copy.setFirebaseId(firebaseId);
            out.add(copy);
        }
        return out;
    }

    public ArrayList<Armor> getArmorList() {
        return armorList;
    }

    public String getArmorPath(){
        return resourcePath;
    }

    /** Lookup the original instance by name (case-insensitive) */
    public Armor getArmorByName(String name) {
        return name == null ? null : armorByName.get(name.toLowerCase());
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
