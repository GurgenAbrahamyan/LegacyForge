package com.gamb1t.legacyforge.Entity;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

public class Enemy extends GameCharacters {

    private long lastDirChange = System.currentTimeMillis();
    private int damage = 10;
    private float playerPosX, playerPosY;
    Player player ;
    private ArrayList<Vector2> respPos = new ArrayList<>();


    GameScreen gameScreen;

    public Enemy(GameScreen gameScreen, Player player) {


        super(0 ,0, GameConstants.Sprite.SIZE ,  GameConstants.Sprite.SIZE);


        Random random = new Random();


        entityPos = new GameScreen.PointF(random.nextInt(GET_WIDTH), random.nextInt(GET_HEIGHT/2) + GET_HEIGHT/2);



        this.gameScreen = gameScreen;

        maxHp = 100;
        hp = maxHp;
        this.player = player;


    }
    public void updateMove(double delta) {

        boolean tileColided =false;

        speed = (float) (delta * 300);
        float deltaX = 0, deltaY = 0; // Define deltaX and deltaY

        if (System.currentTimeMillis() - lastDirChange >= 3000) {
            int randFaceDir = gameScreen.getRandom(4);
            setFaceDir(randFaceDir);
            lastDirChange = System.currentTimeMillis();
        }

        float distanceBtwPlayer = (float) Math.hypot(playerPosX - entityPos.x, playerPosY - entityPos.y);
        float dirX = playerPosX - entityPos.x;
        float dirY = playerPosY - entityPos.y;

        if (distanceBtwPlayer > GameConstants.Sprite.SIZE * 4) {

            switch (getFaceDir()) {
                case GameConstants.Face_Dir.DOWN:
                    deltaY = speed;
                    if (entityPos.y + deltaY >= GET_HEIGHT) setFaceDir(GameConstants.Face_Dir.UP);
                    break;

                case GameConstants.Face_Dir.UP:
                    deltaY = -speed;
                    if (entityPos.y + deltaY <= 0) setFaceDir(GameConstants.Face_Dir.DOWN);
                    break;

                case GameConstants.Face_Dir.RIGHT:
                    deltaX = speed;
                    if (entityPos.x + deltaX >= GET_WIDTH) setFaceDir(GameConstants.Face_Dir.LEFT);
                    break;

                case GameConstants.Face_Dir.LEFT:
                    deltaX = -speed;
                    if (entityPos.x + deltaX <= 0) setFaceDir(GameConstants.Face_Dir.RIGHT);
                    break;
            }
        } else {

            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            if (length != 0) {
                dirX /= length;
                dirY /= length;
            }

            deltaX = dirX * speed;
            deltaY = dirY * speed;


            if (Math.abs(dirX) > Math.abs(dirY)) {
                setFaceDir(dirX > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT);
            } else {
                setFaceDir(dirY > 0 ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP);
            }
        }
        if(!gameScreen.mapManager.checkNearbyWallCollision(hitbox, hitbox.getX() + deltaX, hitbox.getY() + deltaY)){

        entityPos.x += deltaX;
        entityPos.y += deltaY;
         tileColided = false;
        }
        else {
            tileColided = true;
        }
        setHitboxPosition();
    }



    @Override
    public void setHitboxPosition() {
        hitbox.setPosition(entityPos.x, entityPos.y);
    }

    @Override
    public void drawBar(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {


        float barWidth = GameConstants.Sprite.SIZE;
        float barHeight = (float) GameConstants.Sprite.SIZE / 4;

        float barX = entityPos.x;
        float barY = entityPos.y + GameConstants.Sprite.SIZE;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barX +player.cameraX, barY + player.cameraY, barWidth, barHeight);


        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(barX +player.cameraX, barY + player.cameraY, barWidth * ((float) hp / maxHp), barHeight);

        shapeRenderer.end();
    }

    public void takeDamage(float amout){
        if(hp > 0){
        hp -= amout;}
        else {
            die();
        }

    }

    public void die(){
        Vector2 currentResp = respPos.get(gameScreen.getRandom(respPos.size()));
        entityPos.x = currentResp.x;

        entityPos.y = currentResp.y;

        hp = maxHp;

        addMoney(player);



    }

    public int getDamage(){
        return damage;
    }
    public Polygon getHitbox(){
        return hitbox;
    }

    public void setPlayerPosX(float x ){
        playerPosX = x;
    }
    public void setPlayerPosY(float y ){
        playerPosY = y;
    }

    public void addMoney(Player player){
        player.addMoney(hp/10);
    }

    public void setRespPos(ArrayList<Vector2> respPos){
        this.respPos = respPos;
    }

}
