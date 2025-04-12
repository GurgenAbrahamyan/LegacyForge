package com.gamb1t.legacyforge.Interfaces;

import com.gamb1t.legacyforge.ManagerClasses.PlayerData;

import java.util.function.Consumer;

public interface SaveManager {
    void savePlayerData(PlayerData data);
    void loadPlayerData(Consumer<PlayerData> callback);
}
