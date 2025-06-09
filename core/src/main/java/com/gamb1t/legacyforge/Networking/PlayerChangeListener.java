package com.gamb1t.legacyforge.Networking;

public interface PlayerChangeListener {
   public boolean intended = false;
    void onPlayerExpAndMoneyChange(int money, float experience);
    void onPlayerLevelChange(int lvl);
    void onPlayerNewInventoryAdd(Object object);
    void removeItemById(Object object);
    void onPlayerEquip(Object o);
    void onReturnToGameModeSelection(boolean intended);
}
