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
    private  Network.StateMessageOnConnection stateMessageOnConnection;
    ClientMain clientM;

    public ClientListener(ClientMain clientMain, User user){

        this.clientM = clientMain;
        this.user = user;
    }

    ArrayList<Network.PlayerState> player = new ArrayList<>();

    @Override
    public void received(Connection connection, Object object) {

        if(object instanceof Network.StateMessageOnConnection){
            System.out.println("RECIEVED!!!ԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱ");
            this.stateMessageOnConnection = (Network.StateMessageOnConnection) object;
            Gdx.app.postRunnable(() -> {
                if(stateMessageOnConnection.gameMode.equals("Hub")) {
                    clientM.initGamescreen(stateMessageOnConnection);}

                else if (stateMessageOnConnection.gameMode.equals("Dungeon")){
                    System.out.println(connection.getID());
                    clientM.initGamescreenDungeon(stateMessageOnConnection);
                }

                for(Network.PlayerState state : player){
                    clientM.gameScreen.addPlayer(state);
                }


        });

        }
        if(clientM.gameScreen != null){

            if (object instanceof Network.PvpMatchStart) {
                Network.PvpMatchStart match = (Network.PvpMatchStart) object;
                if (match.playerId.equals (clientM.gameScreen.getPLAYER().getFirebaseId())) {
                    clientM.gameScreen.getMultiplayerUi().setPvpMatch(match.opponentName);
                }
            }

        if (object instanceof Network.PlayerState) {


            clientM.gameScreen.addPlayer((Network.PlayerState) object );


        }
        if (object instanceof Network.SquadUpdate) {
                Network.SquadUpdate update = (Network.SquadUpdate) object;
                clientM.gameScreen.getMultiplayerUi().setSquadStatus(update.inSquad, update.countdown, update.memberNames);
        }

        if(object instanceof Network.OnPlayerEquipWeapon){
            clientM.gameScreen.setNewWeapon((Network.OnPlayerEquipWeapon) object);
        }

        if(object instanceof Network.PlayerDeleted){
            clientM.gameScreen.removePlayerById(((Network.PlayerDeleted) object).id);
        }

            if (object instanceof Network.SetProjectilePositionMessage) {

                clientM.gameScreen.setProjectilePosition((Network.SetProjectilePositionMessage)object);
            }

            if(object instanceof Network.AttackCanceled){
                clientM.gameScreen.cancelAttack((Network.AttackCanceled) object);
            }


        if (object instanceof  Network.PlayerPos){
            Gdx.app.postRunnable(() ->{
           //     System.out.println(((Network.PlayerPos) object).x+ " " +((Network.PlayerPos) object).y);
                clientM.gameScreen.setPlayerPos((Network.PlayerPos) object);

            });



        }

        if(object instanceof Network.EnemyPos){
            Gdx.app.postRunnable(()->{
//                System.out.println("recieved");

                    clientM.gameScreen.setEnemyPos((Network.EnemyPos) object);
                });

        }

        if(object instanceof Network.CurrentHp){

            clientM.gameScreen.dealDamge((Network.CurrentHp) object);
          //  System.out.println("recieved new hp!");
        }



        if( object instanceof  Network.AttackStartPacket){
            System.out.println("recieved start packet");
            clientM.gameScreen.startAttack(((Network.AttackStartPacket) object) );
        }

        if(object instanceof  Network.AttackReleasePacket){

            System.out.println("recieved reales packet");
            clientM.gameScreen.endAttack((Network.AttackReleasePacket) object);
        }

        if(object instanceof  Network.AttackDragged){
            System.out.println("recieved attack dragged");
            clientM.gameScreen.attackDragged((Network.AttackDragged) object);
        }

            if(object instanceof  Network.DoorOpened){

                clientM.gameScreen.getMapManager().openDoor(new Vector2(((Network.DoorOpened) object).x, ((Network.DoorOpened) object).y));
            }

        if(object instanceof Network.CreateProjectileMessage) {

            clientM.gameScreen.createProjectile((Network.CreateProjectileMessage) object);

        }
        if(object instanceof Network.DestroyProjectileMessage){
            clientM.gameScreen.destroyProjectile((Network.DestroyProjectileMessage) object);
        }

        if(object instanceof Network.PlayerStatsAdded){
            Network.PlayerStatsAdded message = (Network.PlayerStatsAdded) object;
                clientM.gameScreen.getPLAYER().addWhenKilledEnemy(message.moneyAmountAdded, message.expAmountAdded);
            }

        if(object instanceof Network.OnManaChange){
            clientM.gameScreen.getPLAYER().setMana(((Network.OnManaChange)object).amount);
        }

        if(object instanceof  Network.OnPlayerEquipArmor){
            System.out.println("equiping");
            clientM.gameScreen.equipArmor((Network.OnPlayerEquipArmor)object );
        }
        if(object instanceof Network.enemyDied){
            clientM.gameScreen.getEnemiesMap().get(((Network.enemyDied)object).diedEnemyId).setIsAlive(false);
        }

        }
        else {

            if (object instanceof Network.PlayerState) {


                player.add((Network.PlayerState) object);

            }
        }



    }

    @Override
    public void connected(Connection connection) {


        System.out.println("SENDING MESSEGE");
        connection.sendTCP(user);

    }

    public Network.StateMessageOnConnection getStateMessageOnConnection() {
        return stateMessageOnConnection;
    }
}
