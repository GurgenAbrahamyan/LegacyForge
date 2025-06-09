package com.gamb1t.legacyforge.Networking;

import com.esotericsoftware.kryo.Kryo;
import com.gamb1t.legacyforge.Entity.Items;
import com.gamb1t.legacyforge.Entity.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Network {
    public static final int TCP_PORT = 54559;
    public static final int UDP_PORT = 54778;

    public static class StateMessageOnConnection {
        public int newPlayerId;
        public User user;
        public String gameMode;
        public String weaponName;
        public String currentHelmet;
        public String currentChestplate;
        public String jsonPath;
        public float curX, curY;
        public ArrayList<PlayerState> players = new ArrayList<>();
        public ArrayList<EnemyState> enemies = new ArrayList<>();
        public String playerWeaponJson;
        public String enemyWeaponJson;
        public MapInfo mapInfo = new MapInfo();
        public ShopInfo shopInfo;
        public ShopInfo armorShopInfo;
    }

    public static class OnPlayerBoughtWeapon {
        public String weaponName;
        public int playerId;
        public int lvl;
    }

    public static class OnPlayerEquipWeapon {
        public String weaponName;
        public int playerId;
    }

    public static class OnPlayerBoughtArmor {
        public String weaponArmor;
        public int playerId;
        public int lvl;
    }

    public static class OnPlayerEquipArmor {
        public String armorName;
        public int playerId;
    }

    public static class PlayerPos {
        public float x;
        public float y;
        public int id;
        public PlayerPos(float x, float y, int id) {
            this.x = x;
            this.y = y;
            this.id = id;
        }
        public PlayerPos() {}
    }

    public static class PlayerDeleted {
        public int id;
        public PlayerDeleted(int id) {
            this.id = id;
        }
        public PlayerDeleted() {}
    }

    public static class RoundStart{
        public int roundNumber; // Current round number
        public Map<Integer, Integer> playerScores;
        public RoundStart() {
            playerScores = new HashMap<>();
        }
    }

    public static class RoundReset {
        public int countdownInSeconds;
    }

    public static class MatchEnd {
        public int winnerId;
    }


    public static class CurrentHp {
        public boolean isEnemy;
        public int idOfEnemy;
        public int curHp;
    }

    public static class EnemyPos {
        public float X;
        public float Y;
        public int id;
        public EnemyPos(float x, float y, int id) {
            this.X = x;
            this.Y = y;
            this.id = id;
        }
        public EnemyPos() {}
    }

        public static class DoorClosed {
            public int x, y;
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
        public AttackStartPacket(int id, boolean isEnemy) {
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
        public int experience;
        public String weaponName;
        public String currentHelmet;
        public String currentChestplate;
    }

    public static class MapInfo {
        public String mapPath;
        public String hitboxPath;
        public String renderPath;
        public int height;
        public int width;
    }

    public static class ShopInfo {
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

    public static class playerDied {}

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

    public static class AttackDragged {
        public float angle;
        public int id;
        public boolean isEnemy;
        public AttackDragged(float angle, int id, boolean isEnemy) {
            this.angle = angle;
            this.id = id;
            this.isEnemy = isEnemy;
        }
        public AttackDragged() {}
    }

    public static class enemyDied {
        public int diedEnemyId;
    }

    public static class PlayerStatsAdded {
        public int playerId;
        public int moneyAmountAdded;
        public int expAmountAdded;
    }

    public static class DoorOpened{
        public int x;
        public int y;
    }


    public static class SetProjectilePositionMessage {
        public int projectileId;
        public int enemyId;
        public float x;
        public float y;
        public boolean isEnemy;
        public SetProjectilePositionMessage() {}
        public SetProjectilePositionMessage(int projectileId, int enemyId, float x, float y, boolean isEnemy) {
            this.projectileId = projectileId;
            this.enemyId = enemyId;
            this.x = x;
            this.y = y;
            this.isEnemy = isEnemy;
        }
    }

    public static class AttackCanceled {
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

    public static class PvpRequest {
        public String playerId;
        public String mode;
    }

    public static class PvpMatchStart {
        public String playerId;
        public String opponentId;
        public String opponentName;
    }


    public static class DestroyProjectileMessage {
        public int projectileId;
        public boolean isEnemy;
        public int enemyId;
    }

    public static class OnManaChange {
        public float amount;
    }

    public static class SquadAction {
        public int playerId;
        public String action; // "join" or "leave"
    }

    public static class SquadUpdate {
        public boolean inSquad;
        public float countdown;
        public ArrayList<String> memberNames;

    }

    public static class DungeonSquadUpdate {
        public boolean inSquad;
        public float countdown;
        public ArrayList<String> memberNames;
    }

    public static class OneVsOneQueueUpdate {
        public boolean inQueue;
        public float countdown;
        public ArrayList<String> opponentNames;
    }

    public static void register(Kryo kryo) {
        kryo.register(java.util.ArrayList.class);
        kryo.register(StateMessageOnConnection.class);
        kryo.register(MapInfo.class);
        kryo.register(ShopInfo.class);
        kryo.register(PlayerState.class);
        kryo.register(EnemyState.class);
        kryo.register(ArrayList.class);
        kryo.register(String.class);
        kryo.register(StopPlayerMove.class);
        kryo.register(new ArrayList<Network.PlayerState>(){}.getClass());
        kryo.register(new ArrayList<Network.EnemyState>(){}.getClass());
        kryo.register(HashMap.class);
        kryo.register(AttackReleasePacket.class);
        kryo.register(AttackStartPacket.class);
        kryo.register(MovePacket.class);
        kryo.register(EnemyPos.class);
        kryo.register(PlayerPos.class);
        kryo.register(AttackDragged.class);
        kryo.register(PlayerDeleted.class);
        kryo.register(CurrentHp.class);
        kryo.register(CreateProjectileMessage.class);
        kryo.register(DestroyProjectileMessage.class);
        kryo.register(SetProjectilePositionMessage.class);
        kryo.register(AttackCanceled.class);
        kryo.register(User.class);
        kryo.register(Items.class);
        kryo.register(OnPlayerEquipWeapon.class);
        kryo.register(OnPlayerBoughtWeapon.class);
        kryo.register(PlayerStatsAdded.class);
        kryo.register(OnManaChange.class);
        kryo.register(OnPlayerEquipArmor.class);
        kryo.register(OnPlayerBoughtArmor.class);
        kryo.register(enemyDied.class);
        kryo.register(playerDied.class);
        kryo.register(SquadAction.class);
        kryo.register(SquadUpdate.class);
        kryo.register(DoorOpened.class);
        kryo.register(PvpMatchStart.class);
        kryo.register(PvpRequest.class);
        kryo.register(RoundStart.class);
        kryo.register(DoorClosed.class);
        kryo.register(RoundReset.class);
        kryo.register(OneVsOneQueueUpdate.class);




    }
}
