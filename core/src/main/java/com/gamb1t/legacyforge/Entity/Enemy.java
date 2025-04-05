package com.gamb1t.legacyforge.Entity;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.ArrayList;
import java.util.Random;

public class Enemy extends GameCharacters {

    private long lastDirChange = System.currentTimeMillis();
    private int damage = 10;
    private float playerPosX, playerPosY;
    Player player ;
    Weapon weapon;
    private ArrayList<Vector2> respPos = new ArrayList<>();
    float distanceBtwPlayer;


    GameScreen gameScreen;

    public Enemy(GameScreen gameScreen, Player player, ArrayList<Vector2> respPos, Weapon weapon) {


        super(0 ,0, GameConstants.Sprite.SIZE *4/5,  GameConstants.Sprite.SIZE * 4/5);


        Random random = new Random();
        this.respPos = respPos;
        Vector2 resp = respPos.get(random.nextInt(respPos.size()));
        entityPos = new GameScreen.PointF(resp.x, resp.y);
        this.weapon = weapon;



        this.gameScreen = gameScreen;

        maxHp = 100;
        hp = maxHp;
        this.player = player;



    }

    @JsonSetter("spriteSheet")
    public void setTexture(String recourceName){

        Texture entitiesTexture = new Texture(recourceName);

        SpriteSheet = new TextureRegion[entitiesTexture.getWidth()/GameConstants.Sprite.DEFAULT_SIZE][entitiesTexture.getWidth()/GameConstants.Sprite.DEFAULT_SIZE];

        SpriteSheet = TextureRegion.split(entitiesTexture, GameConstants.Sprite.DEFAULT_SIZE, GameConstants.Sprite.DEFAULT_SIZE);

    }
    public void updateMove(double delta) {

        distanceBtwPlayer = (float) Math.hypot(playerPosX - entityPos.x+GameConstants.Sprite.SIZE/2 , playerPosY - entityPos.y+GameConstants.Sprite.SIZE/2);



        float deltaSpeed = (float) (delta * speed);
        float deltaX = 0, deltaY = 0;

        if (System.currentTimeMillis() - lastDirChange >= 3000) {
            int randFaceDir = gameScreen.getRandom(4);
            setFaceDir(randFaceDir);
            lastDirChange = System.currentTimeMillis();
        }


        float dirX = playerPosX - entityPos.x + GameConstants.Sprite.SIZE/2;
        float dirY = playerPosY - entityPos.y + GameConstants.Sprite.SIZE/2;

        if (distanceBtwPlayer > GameConstants.Sprite.SIZE * 4) {

            switch (getFaceDir()) {
                case GameConstants.Face_Dir.DOWN:
                    deltaY = deltaSpeed;
                    if (entityPos.y + deltaY >= GET_HEIGHT) setFaceDir(GameConstants.Face_Dir.UP);
                    break;

                case GameConstants.Face_Dir.UP:
                    deltaY = -deltaSpeed;
                    if (entityPos.y + deltaY <= 0) setFaceDir(GameConstants.Face_Dir.DOWN);
                    break;

                case GameConstants.Face_Dir.RIGHT:
                    deltaX = deltaSpeed;
                    if (entityPos.x + deltaX >= GET_WIDTH) setFaceDir(GameConstants.Face_Dir.LEFT);
                    break;

                case GameConstants.Face_Dir.LEFT:
                    deltaX = -deltaSpeed;
                    if (entityPos.x + deltaX <= 0) setFaceDir(GameConstants.Face_Dir.RIGHT);
                    break;
            }
        } else {

            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            if (length != 0) {
                dirX /= length;
                dirY /= length;
            }

            deltaX = dirX * deltaSpeed;
            deltaY = dirY * deltaSpeed;


            if (Math.abs(dirX) > Math.abs(dirY)) {
                setFaceDir(dirX > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT);
            } else {
                setFaceDir(dirY > 0 ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP);
            }
        }
        if(!gameScreen.mapManager.checkNearbyWallCollision(hitbox, hitbox.getX() + deltaX, hitbox.getY() + deltaY)){

        entityPos.x += deltaX;
        entityPos.y += deltaY;

        }
        setHitboxPosition();
    }

    private long lastAttackTime = 0;
    float cooldownDuration =0;
    float elapsedTime = -7;

    public void attackPlayer() {
        if (distanceBtwPlayer < weapon.getRange() * GameConstants.Sprite.SIZE) {
            float dirX = playerPosX - entityPos.x + GameConstants.Sprite.SIZE / 2;
            float dirY = playerPosY - entityPos.y + GameConstants.Sprite.SIZE / 2;
            double angle = Math.atan2(dirY, dirX);

            long currentTime = System.currentTimeMillis();
            cooldownDuration = (weapon.getAttackSpeed() + 1);
            if(elapsedTime == -7){
                elapsedTime = cooldownDuration;
                System.out.println(elapsedTime);
                System.out.println(cooldownDuration);
            }



            if (weapon instanceof RangedWeapon) {
                weapon.setRotation((float) Math.toDegrees(angle));
                weapon.setAttacking(true);
                ((RangedWeapon) weapon).setAnimOver(true);
                weapon.setAiming(true);

                elapsedTime -= Gdx.graphics.getDeltaTime();

                System.out.println(Gdx.graphics.getDeltaTime());

                if (elapsedTime <= 0) {

                    elapsedTime=cooldownDuration;


                    if (weapon.getAiming()) {


                        weapon.attack();
                        lastAttackTime = System.currentTimeMillis();



                        weapon.setAttacking(true);

                            ((RangedWeapon) weapon).setAnimOver(true);


                            ((RangedWeapon) weapon).setIsCharging(false);

                            weapon.resetAnimation();


                    }



                    lastAttackTime = currentTime;


                }
            } else {
                if (currentTime - lastAttackTime > weapon.getAttackSpeed() * 1000) {
                    weapon.setAiming(true);
                    lastAttackTime = currentTime;


                    if (weapon.getAiming()) {

                        weapon.setRotation((float) Math.toDegrees(angle));





                        weapon.attack();



                        weapon.setAttacking(true);

                        if(weapon instanceof MagicWeapon){
                            ((MagicWeapon) weapon).setAnimOver(true);
                        }
                    }

                    weapon.setAiming(false);

                } else {
                }
            }
        }
        else {
            weapon.setAiming(false);
        }
    }




    @Override
    public void setHitboxPosition() {
        hitbox.setPosition(entityPos.x+GameConstants.Sprite.SIZE/8-GameConstants.Sprite.SIZE/2, entityPos.y - GameConstants.Sprite.SIZE/2);
    }

    @Override
    public void drawBar(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {


        float barWidth = GameConstants.Sprite.SIZE;
        float barHeight = (float) GameConstants.Sprite.SIZE / 4;

        float barX = entityPos.x - GameConstants.Sprite.SIZE/2;
        float barY = entityPos.y + GameConstants.Sprite.SIZE - GameConstants.Sprite.SIZE/2;

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
        if (hp<=0 ){
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
    public Weapon getWeapon(){
        return  weapon;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }


}
