package com.gamb1t.clientside;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.gamb1t.legacyforge.Networking.Network;

public class ClientListener implements Listener {

    public String name;
    public int experience;
    public int level;
    public int hp;
    public int money;

    private  Network.StateMessageOnConnection stateMessageOnConnection;
    ClientMain clientM;

    public ClientListener(ClientMain clientMain, String name, int experience, int level, int hp, int money){
        this.name = name;
        this.experience = experience;
        this.level = level;
        this.hp = hp;
        this.money = money;
        this.clientM = clientMain;
    }

    @Override
    public void received(Connection connection, Object object) {

        if(object instanceof Network.StateMessageOnConnection){
            System.out.println("RECIEVED!!!ԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱԱ");
            this.stateMessageOnConnection = (Network.StateMessageOnConnection) object;
            Gdx.app.postRunnable(() -> {
                clientM.initGamescreen(stateMessageOnConnection);
            });

        }
        if(clientM.gameScreen != null){

        if (object instanceof Network.PlayerState) {

            System.out.println("NIGGA CONNECTED" + connection.getID());
            clientM.gameScreen.addPlayer((Network.PlayerState) object );


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

        if(object instanceof Network.CurentHp){

            clientM.gameScreen.dealDamge((Network.CurentHp) object);
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

        if(object instanceof Network.CreateProjectileMessage) {

            clientM.gameScreen.createProjectile((Network.CreateProjectileMessage) object);

        }
        if(object instanceof Network.DestroyProjectileMessage){
            clientM.gameScreen.destroyProjectile((Network.DestroyProjectileMessage) object);
        }
        }



    }

    @Override
    public void connected(Connection connection) {

        Network.PlayerInitMessage initMessage = new Network.PlayerInitMessage();

        initMessage.name = name;
        initMessage.experience = experience;
        initMessage.level = level;
        initMessage.hp = hp;
        initMessage.money = money;

        System.out.println("SENDING MESSEGE");
        connection.sendTCP(initMessage);

    }

    public Network.StateMessageOnConnection getStateMessageOnConnection() {
        return stateMessageOnConnection;
    }
}
