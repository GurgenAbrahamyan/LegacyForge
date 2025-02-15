package com.gamb1t.legacyforge.Entity;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;

public class Enemy extends GameCharacters {

    private long lastDirChange = System.currentTimeMillis();
    private int damage = 10;
    private float playerPosX, playerPosY;


    GameScreen gameScreen;

    public Enemy(GameScreen gameScreen) {


        super(0 ,0, GameConstants.Sprite.SIZE ,  GameConstants.Sprite.SIZE);



        entityPos = new GameScreen.PointF(gameScreen.getRandom(GET_WIDTH), gameScreen.getRandom(GET_HEIGHT));



        this.gameScreen = gameScreen;

        maxHp = 100;
        hp = maxHp;


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

    @Override
    public void drawHpBar(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {

        float barWidth = GameConstants.Sprite.SIZE;
        float barHeight = (float) GameConstants.Sprite.SIZE /4;
        float barX = entityPos.x ;
        float barY = entityPos.y + GameConstants.Sprite.SIZE ;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(playerPosX+barX, playerPosY+barY, barWidth, barHeight);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect( playerPosX+barX, playerPosY+barY, barWidth * ((float) hp / maxHp), barHeight);
        shapeRenderer.end();

    }

    public void takeDamage(float amout){
        hp -= amout;

    }

    public int getDamage(){
        return damage;
    }

    public void setPlayerPosX(float x ){
        playerPosX = x;
    }
    public void setPlayerPosY(float y ){
        playerPosY = y;
    }

}
