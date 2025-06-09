package com.gamb1t.clientside;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.Networking.Network;

import java.util.ArrayList;

public class ClientListener implements Listener {

    public User user;
    private Network.StateMessageOnConnection stateMessageOnConnection;
    private ClientMain clientM;
    private Network.RoundStart roundStart;

    public ClientListener(ClientMain clientMain, User user) {
        this.clientM = clientMain;
        this.user = user;
    }

    private ArrayList<Network.PlayerState> player = new ArrayList<>();

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof Network.StateMessageOnConnection) {
            System.out.println("RECEIVED!!!");
            this.stateMessageOnConnection = (Network.StateMessageOnConnection) object;
            Gdx.app.postRunnable(() -> {
                if (stateMessageOnConnection.gameMode.equals("Hub")) {
                    clientM.initGamescreen(stateMessageOnConnection);
                } else if (stateMessageOnConnection.gameMode.equals("Dungeon")) {
                    System.out.println(connection.getID());
                    clientM.initGamescreenDungeon(stateMessageOnConnection);
                } else if (stateMessageOnConnection.gameMode.equals("1v1")) {
                    System.out.println(connection.getID());
                    clientM.initGamescreenDungeon(stateMessageOnConnection);
                    if (roundStart != null) {
                        clientM.gameScreen.getMultiplayerUi().handleRoundStart(roundStart);
                    }
                }

                for (Network.PlayerState state : player) {
                    clientM.gameScreen.addPlayer(state);
                }
            });
        }

        if (clientM.gameScreen != null) {
            if (object instanceof Network.PlayerState) {
                clientM.gameScreen.addPlayer((Network.PlayerState) object);
            }
            if (object instanceof Network.OneVsOneQueueUpdate) {
                Network.OneVsOneQueueUpdate update = (Network.OneVsOneQueueUpdate) object;
                clientM.gameScreen.getMultiplayerUi().setOneVsOneQueueStatus(update.inQueue, update.countdown, update.opponentNames);
            }
            if (object instanceof Network.SquadUpdate) {
                Network.SquadUpdate update = (Network.SquadUpdate) object;
                clientM.gameScreen.getMultiplayerUi().setDungeonSquadStatus(update.inSquad, update.countdown, update.memberNames);
            }
            if (object instanceof Network.OnPlayerEquipWeapon) {
                clientM.gameScreen.setNewWeapon((Network.OnPlayerEquipWeapon) object);
            }
            if (object instanceof Network.PlayerDeleted) {
                clientM.gameScreen.removePlayerById(((Network.PlayerDeleted) object).id);
            }
            if (object instanceof Network.RoundStart) {
                Network.RoundStart roundStart = (Network.RoundStart) object;
                clientM.gameScreen.getMultiplayerUi().handleRoundStart(roundStart);
            }
            if (object instanceof Network.SetProjectilePositionMessage) {
                clientM.gameScreen.setProjectilePosition((Network.SetProjectilePositionMessage) object);
            }
            if (object instanceof Network.AttackCanceled) {
                clientM.gameScreen.cancelAttack((Network.AttackCanceled) object);
            }
            if (object instanceof Network.PlayerPos) {
                Gdx.app.postRunnable(() -> {
                    clientM.gameScreen.setPlayerPos((Network.PlayerPos) object);
                });
            }
            if (object instanceof Network.EnemyPos) {
                Gdx.app.postRunnable(() -> {
                    clientM.gameScreen.setEnemyPos((Network.EnemyPos) object);
                });
            }
            if (object instanceof Network.CurrentHp) {
                clientM.gameScreen.dealDamge((Network.CurrentHp) object);
            }
            if (object instanceof Network.AttackStartPacket) {
                System.out.println("received start packet");
                clientM.gameScreen.startAttack(((Network.AttackStartPacket) object));
            }
            if (object instanceof Network.AttackReleasePacket) {
                System.out.println("received release packet");
                clientM.gameScreen.endAttack((Network.AttackReleasePacket) object);
            }
            if (object instanceof Network.AttackDragged) {
                System.out.println("received attack dragged");
                clientM.gameScreen.attackDragged((Network.AttackDragged) object);
            }
            if (object instanceof Network.DoorOpened) {
                clientM.gameScreen.getMapManager().openDoor(new Vector2(((Network.DoorOpened) object).x, ((Network.DoorOpened) object).y));
            }
            if (object instanceof Network.DoorClosed) {

                    clientM.gameScreen.getMapManager().closeDoor(new Vector2(((Network.DoorClosed) object).x, ((Network.DoorClosed) object).y));


            }
            if (object instanceof Network.CreateProjectileMessage) {
                clientM.gameScreen.createProjectile((Network.CreateProjectileMessage) object);
            }
            if (object instanceof Network.DestroyProjectileMessage) {
                clientM.gameScreen.destroyProjectile((Network.DestroyProjectileMessage) object);
            }
            if (object instanceof Network.PlayerStatsAdded) {
                Network.PlayerStatsAdded message = (Network.PlayerStatsAdded) object;
                clientM.gameScreen.getPLAYER().addWhenKilledEnemy(message.moneyAmountAdded, message.expAmountAdded);
            }
            if (object instanceof Network.OnManaChange) {
                clientM.gameScreen.getPLAYER().setMana(((Network.OnManaChange) object).amount);
            }
            if (object instanceof Network.OnPlayerEquipArmor) {
                System.out.println("equipping");
                clientM.gameScreen.equipArmor((Network.OnPlayerEquipArmor) object);
            }
            if (object instanceof Network.enemyDied) {
                clientM.gameScreen.getEnemiesMap().get(((Network.enemyDied) object).diedEnemyId).setIsAlive(false);
            }
        } else {
            if (object instanceof Network.PlayerState) {
                player.add((Network.PlayerState) object);
            }
        }
    }

    @Override
    public void connected(Connection connection) {
        System.out.println("SENDING MESSAGE");
        connection.sendTCP(user);
    }

    @Override
    public void disconnected(Connection connection) {
        System.out.println("Disconnected from server (network issue)");
        Gdx.app.postRunnable(() -> {
            if (clientM.playerChangeListener != null && !clientM.isIntentionalDisconnect()) {
                clientM.playerChangeListener.onReturnToGameModeSelection(false);
            }
        });
    }

    public Network.StateMessageOnConnection getStateMessageOnConnection() {
        return stateMessageOnConnection;
    }
}
