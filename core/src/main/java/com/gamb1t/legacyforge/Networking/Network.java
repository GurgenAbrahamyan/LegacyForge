package com.gamb1t.legacyforge.Networking;

import com.esotericsoftware.kryo.Kryo;

import java.util.ArrayList;

public class Network {
    public static final int TCP_PORT = 54555;
    public static final int UDP_PORT = 54777;



    public static class StateMessageOnConnection {
        public int newPlayerId;
        public String weaponName;
        public ArrayList<PlayerState> players = new ArrayList<>();
        public ArrayList<EnemyState> enemies = new ArrayList<>();

        public String playerWeaponJson;
        public String enemyWeaponJson;

        public MapInfo mapInfo = new MapInfo();

        public ShopInfo shopInfo = new ShopInfo();


    }

    public static class PlayerPos{
        public float x;
        public float y;
        public int id;
        public  PlayerPos(float x, float y, int id){
            this.x= x;
            this.y= y;
            this.id= id;
        }
        public PlayerPos(){}
    }

    public static class PlayerDeleted{
        public int id;
        public PlayerDeleted(int id){
            this.id= id;
        }
        public PlayerDeleted(){
        }
    }

    public static class CurentHp {

        public boolean isEnemy;
        public int idOfEnemy;
        public int curHp;


    }

    public static class EnemyPos {
        public float X;
        public float Y;
        public int id;
        public EnemyPos(float x, float y, int id){
            this.X = x;
            this.Y = y;
            this.id = id;
        }
        public EnemyPos(){

        }
    }



    public static class MovePacket {

        public float x;
        public float y;
        public int id;

        public MovePacket() {}
        public MovePacket(float x, float y, int id) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }

    public static class AttackStartPacket {
        public int id;
        public boolean isEnemy;
        public AttackStartPacket() {}
        public AttackStartPacket(int id, boolean isEnemy){
            this.id = id;
            this.isEnemy = isEnemy;
        }
    }

    public static class PlayerState {
        public int id;
        public float x, y;
        public String name;
        public int level;
        public int hp;
        public int money;
        public float experience;
        public String weaponName;
    }

    public static class MapInfo{
        public String mapPath;
        public String hitboxPath;
        public String renderPath;



        public int height;
        public int width;



    }

    public static class ShopInfo{
        public String renderPath;
        public String weaponsJson;
        public float width;
        public float height;

        public float x;
        public float y;
    }



    public static class EnemyState {
        public int id;
        public float dirX;
        public float dirY;
        public float speed;
        public float x, y;
        public int hp;

    }

    public static class PlayerInitMessage {
        public String name;
        public int experience;
        public int level;

        public int hp;
        public int money;
        public String weaponName;
    }
    public static class playerDied{
    }

    public static class AttackReleasePacket {
        public boolean isEnemy;
        public float angle;
        public int id;
        public AttackReleasePacket() {}
        public AttackReleasePacket(float angle, int id, boolean isEnemy) {
            this.angle = angle;
            this.id = id;
            this.isEnemy = isEnemy;
        }
    }
    public static class StopPlayerMove {
        public int id;
    }

    public static class AttackDragged{
        public  float angle;
        public int id;
        public boolean isEnemy;
        public  AttackDragged(float angle, int id, boolean isEnemy){
            this.angle = angle;
            this.id= id;
            this.isEnemy = isEnemy;
        }
        public  AttackDragged(){

        }
    }

    public static class SetProjectilePositionMessage {
        public int projectileId;
        public int enemyId;
        public float x;
        public float y;
        public boolean isEnemy;

        public SetProjectilePositionMessage() {
        }

        public SetProjectilePositionMessage(int projectileId, int enemyId, float x, float y, boolean isEnemy) {
            this.projectileId = projectileId;
            this.enemyId = enemyId;
            this.x = x;
            this.y = y;
            this.isEnemy = isEnemy;
        }
    }

    public static class AttackCanceled{
        public int enemyId;
        public boolean isEnemy;
    }

    public static class CreateProjectileMessage {
        public float x;
        public float y;
        public int projectileId;
        public int enemyId;
        public float dx;
        public float dy;
        public boolean isEnemy;
    }
    public static class DestroyProjectileMessage {
        public int projectileId;
        public boolean isEnemy;
        public int enemyId;
    }

    public static void register(Kryo kryo) {

        kryo.register(java.util.ArrayList.class);
        kryo.register(StateMessageOnConnection.class);
        kryo.register(MapInfo.class);
        kryo.register(ShopInfo.class);
        kryo.register(PlayerState.class);
        kryo.register(EnemyState.class);
        kryo.register(PlayerInitMessage.class);
        kryo.register(ArrayList.class);
        kryo.register(ArrayList.class);
        kryo.register(String.class);
        kryo.register(StopPlayerMove.class);
        kryo.register(new ArrayList<Network.PlayerState>(){}.getClass());
        kryo.register(new ArrayList<Network.EnemyState>(){}.getClass());
        kryo.register(AttackReleasePacket.class);
        kryo.register(AttackStartPacket.class);
        kryo.register(MovePacket.class);
        kryo.register(EnemyPos.class);
        kryo.register(PlayerPos.class);
        kryo.register(AttackDragged.class);
        kryo.register(PlayerDeleted.class);
        kryo.register(CurentHp.class);
        kryo.register(CreateProjectileMessage.class);
        kryo.register(DestroyProjectileMessage.class);
        kryo.register(SetProjectilePositionMessage.class);
        kryo.register(AttackCanceled.class);

    }
}
