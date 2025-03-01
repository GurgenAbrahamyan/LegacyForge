package com.gamb1t.legacyforge.Entity;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;

import java.util.Random;

public class Enemy extends GameCharacters {

    private long lastDirChange = System.currentTimeMillis();
    private int damage = 10;
    private float playerPosX, playerPosY;
    Player player ;


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
    public void updateMove(double delta){

         speed = (float) (delta * 300);

    if (System.currentTimeMillis() - lastDirChange >= 3000) {
        int randFaceDir = gameScreen.getRandom(4);
        setFaceDir(randFaceDir);
        lastDirChange = System.currentTimeMillis();
    }
    float distanceBtwPlayer = (float) Math.hypot(playerPosX- entityPos.x, playerPosY - entityPos.y);


    if(distanceBtwPlayer > GameConstants.Sprite.SIZE*4){


        switch (getFaceDir()) {
        case GameConstants.Face_Dir.DOWN:
            entityPos.y += speed;
            if (entityPos.y >= GET_HEIGHT)
                setFaceDir( GameConstants.Face_Dir.UP);
            break;

        case GameConstants.Face_Dir.UP:
            entityPos.y -= speed;
            if (entityPos.y <= 0)
                setFaceDir(GameConstants.Face_Dir.DOWN);
            break;

        case GameConstants.Face_Dir.RIGHT:
            entityPos.x += speed;
            if (entityPos.x >= GET_WIDTH)
                setFaceDir(GameConstants.Face_Dir.LEFT);
            break;

        case GameConstants.Face_Dir.LEFT:
            entityPos.x -= speed;
            if (entityPos.x <= 0)
                setFaceDir(GameConstants.Face_Dir.RIGHT);
            break;
    }
        setHitboxPosition();

    }
    else{

        float dirX = playerPosX - entityPos.x;
        float dirY = playerPosY - entityPos.y;
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);

        if (length != 0) {
            dirX /= length;
            dirY /= length;
        }

        float speed = (float) (delta * 300);
        entityPos.x += dirX * speed;
        entityPos.y += dirY * speed;


        if (Math.abs(dirX) > Math.abs(dirY)) {
            setFaceDir(dirX > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT);
        } else {
            setFaceDir(dirY > 0 ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP);
        }

        setHitboxPosition();
    }




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

        entityPos.x = gameScreen.getRandom(21*GameConstants.Sprite.SIZE);
        entityPos.y = 21*GameConstants.Sprite.SIZE;

        hp = maxHp;

        addMoney(player);



    }

    public int getDamage(){
        return damage;
    }
    public Rectangle getHitbox(){
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

}
