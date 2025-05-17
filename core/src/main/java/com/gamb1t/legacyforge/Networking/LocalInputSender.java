package com.gamb1t.legacyforge.Networking;

import com.badlogic.gdx.math.Vector2;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Weapons.Weapon;

public class LocalInputSender implements InputSender {
    private final Player player;
    private final Weapon weapon;

    public LocalInputSender(Player player, Weapon weapon) {
        this.player = player;
        this.weapon = weapon;
    }

    @Override
    public void sendAttack(float angle) {
    }

      @Override
    public void sendMove(Vector2 direction) {
    }

    @Override
    public void attackStart(boolean bool) {
    }

    @Override
    public void stopMove() {
    }

    @Override
    public void sendAttackDraged(float angle) {

    }
}
