package com.gamb1t.legacyforge.Entity;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;

public class Enemy extends GameCharacters {

    private long lastDirChange = System.currentTimeMillis();


    GameScreen gameScreen;

    public Enemy(GameScreen gameScreen) {


        super(0 ,0, GameConstants.Sprite.SIZE ,  GameConstants.Sprite.SIZE);



        entityPos = new GameScreen.PointF(gameScreen.getRandom(GET_WIDTH), gameScreen.getRandom(GET_HEIGHT));



        this.gameScreen = gameScreen;


    }
    public void updateMove(double delta){

    if (System.currentTimeMillis() - lastDirChange >= 3000) {
        int randFaceDir = gameScreen.getRandom(4);
        setFaceDir(randFaceDir);
        lastDirChange = System.currentTimeMillis();
    }

        switch (getFaceDir()) {
        case GameConstants.Face_Dir.DOWN:
            entityPos.y += (float) (delta * 300);
            if (entityPos.y >= GET_HEIGHT)
                setFaceDir( GameConstants.Face_Dir.UP);
            break;

        case GameConstants.Face_Dir.UP:
            entityPos.y -= (float) (delta * 300);
            if (entityPos.y <= 0)
                setFaceDir(GameConstants.Face_Dir.DOWN);
            break;

        case GameConstants.Face_Dir.RIGHT:
            entityPos.x += (float) (delta * 300);
            if (entityPos.x >= GET_WIDTH)
                setFaceDir(GameConstants.Face_Dir.LEFT);
            break;

        case GameConstants.Face_Dir.LEFT:
            entityPos.x -= (float) (delta * 300);
            if (entityPos.x <= 0)
                setFaceDir(GameConstants.Face_Dir.RIGHT);
            break;
    }
        setHitboxPosition();

    }


    @Override
    public void setHitboxPosition() {
        hitbox.setPosition(entityPos.x, entityPos.y);
    }
    public void takeDamage(float amout){
        hp -= amout;

    }

}
