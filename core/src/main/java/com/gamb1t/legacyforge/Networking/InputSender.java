package com.gamb1t.legacyforge.Networking;

import com.badlogic.gdx.math.Vector2;

public interface InputSender {

    void sendAttack(float angle);
    void sendMove(Vector2 direction);
    void attackStart(boolean bool);
    void stopMove();
    void sendAttackDraged(float angle);
}
