package com.gamb1t.legacyforge.ManagerClasses;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Server;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.Networking.Network;
import com.gamb1t.legacyforge.Structures.Shop;
import com.gamb1t.legacyforge.Weapons.*;

import java.util.ArrayList;

public class GameUpdate {

    private ArrayList<Enemy> enemies;
    private MapManaging mapManager;
    private Shop shop;
    private Server server;

    private ArrayList<Player> PLAYERS;

    public GameUpdate( ArrayList<Enemy> enemies, ArrayList<Player> allPlayers,
                      MapManaging mapManager, Shop shop) {

        this.enemies = enemies;
        this.mapManager = mapManager;
        this.shop = shop;
        this.PLAYERS = allPlayers;
    }

    public void setServ(Server server){
        this.server= server;
    }

    public void update(float delta) {

        for(Player PLAYER : PLAYERS) {
            if(server != null){
                server.sendToAllTCP(new Network.PlayerPos(PLAYER.getEntityPos().x/GameConstants.Sprite.SIZE, PLAYER.getEntityPos().y/GameConstants.Sprite.SIZE, PLAYER.getID()));
            }
            PLAYER.getCureentWeapon().setDelta(delta);
            PLAYER.update(delta);
            PLAYER.regenerateMana(delta);
            PLAYER.regenerateHP(delta);
            mapManager.setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            if (PLAYER.getCureentWeapon() instanceof RangedWeapon) {
                ((RangedWeapon) PLAYER.getCureentWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            }
            if (PLAYER.getCureentWeapon() instanceof MagicWeapon) {
                ((MagicWeapon) PLAYER.getCureentWeapon()).setCameraValues(PLAYER.cameraX, PLAYER.cameraY);
            }

            if (PLAYER.getMovePlayer()) {
                PLAYER.updateAnim();
            }
            if (Gdx.input.isTouched()) {
                shop.handleTouchInput(Gdx.input.getX(), Gdx.input.getY());
            }

            if (PLAYER.getCureentWeapon().getAttacking()) {


                PLAYER.getCureentWeapon().createHitbox(PLAYER.getEntityPos().x, PLAYER.getEntityPos().y);


            }
            if (PLAYER.getCureentWeapon().getAttacking()) {
                if (PLAYER.getCureentWeapon() instanceof RangedWeapon || PLAYER.getCureentWeapon() instanceof MeleeWeapon) {
                    PLAYER.getCureentWeapon().update(delta);
                    PLAYER.getCureentWeapon().checkHitboxCollisionsEntity(enemies);
                    PLAYER.getCureentWeapon().checkHitboxCollisionsMap(mapManager);
                }
            }

            if (PLAYER.getCureentWeapon() instanceof MagicWeapon) {
                PLAYER.getCureentWeapon().update(delta);
                PLAYER.getCureentWeapon().checkHitboxCollisionsEntity(enemies);
                PLAYER.getCureentWeapon().checkHitboxCollisionsMap(mapManager);
            }
        }


        for (Enemy enemy : enemies) {
            enemy.getWeapon().setDelta(delta);
            enemy.updateMove(delta);

            if(server != null){
                enemy.sendCoordinates(server);
            }
            enemy.updateAnimation();


            Player closest = getClosestPlayer(enemy);
            enemy.setPlayerPosX(GameConstants.GET_WIDTH/2 - GameConstants.Sprite.SIZE/2-closest.cameraX);
            enemy.setPlayerPosY(GameConstants.GET_HEIGHT/2 - GameConstants.Sprite.SIZE/2-closest.cameraY);

            enemy.attackPlayer();

            if(enemy.getWeapon() instanceof MagicWeapon){
                ((MagicWeapon) enemy.getWeapon()).setCameraValues(closest.cameraX, closest.cameraY);
            }
            if(enemy.getWeapon() instanceof RangedWeapon){
                ((RangedWeapon) enemy.getWeapon()).setCameraValues(closest.cameraX, closest.cameraY);
            }

            if (enemy.getWeapon().getAttacking()) {



                enemy.getWeapon().createHitbox(enemy.getEntityPos().x, enemy.getEntityPos().y);



            }

            if (enemy.getWeapon().getAttacking()) {
                if(enemy.getWeapon() instanceof MeleeWeapon){
                    enemy.getWeapon().update(delta);
                    enemy.getWeapon().checkHitboxCollisionsEntity(PLAYERS);
                    enemy.getWeapon().checkHitboxCollisionsMap(mapManager);}

            }
            if(enemy.getWeapon() instanceof RangedWeapon){
                enemy.getWeapon().update(delta);
                enemy.getWeapon().checkHitboxCollisionsEntity(PLAYERS);
                enemy.getWeapon().checkHitboxCollisionsMap(mapManager);
            }

            if(enemy.getWeapon() instanceof MagicWeapon){
                enemy.getWeapon().update(delta);
                enemy.getWeapon().checkHitboxCollisionsEntity(PLAYERS);
                enemy.getWeapon().checkHitboxCollisionsMap(mapManager);
            }






        }



    }

    private Player getClosestPlayer(Enemy enemy) {
        Player closest = null;
        float minDist = Float.MAX_VALUE;

        for (Player p : PLAYERS) {
            float dx = p.getEntityPos().x - enemy.getEntityPos().x;
            float dy = p.getEntityPos().y - enemy.getEntityPos().y;
            float dist = dx * dx + dy * dy;

            if (dist < minDist) {
                minDist = dist;
                closest = p;
            }
        }
        return closest;
    }
}
