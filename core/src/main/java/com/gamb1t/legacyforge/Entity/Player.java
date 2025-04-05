package com.gamb1t.legacyforge.Entity;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.awt.Font;

public class Player extends GameCharacters {

    private boolean movePlayer;
    private GameScreen.PointF lastTouchDiff;
    private int DEATH_COOLDOWN_TIME = 5000;

    private int level;
    private int experience;
    private int money;
    private float hpMultiplyer = 10;


    private float manaRegenTimer = 0;
    private Vector2 respPoint;

    private int unusedPoints;
    private int meleePoints;
    private int rangedPoints;
    private int magePoints;

    private float meleDamage;
    private float rangedDamage;
    private float mageDamage;

    private float armor;

    private Weapon currentWeapon;

    private long lastTimeGetHit = System.currentTimeMillis();
    private long deathCooldown = System.currentTimeMillis();




    public Player(float x, float y, GameScreen gameScreen, Weapon weapon) {

        super(x, y, GameConstants.Sprite.SIZE*4/5, GameConstants.Sprite.SIZE*4/5);

        this.gameScreen = gameScreen;

        this.level = 1;
        this.experience = 0;
        this.hp = 100;

        this.maxHp = 100;
        this.mana = 100;
        this.maxMana = 100;

        this.meleePoints = 0;
        this.rangedPoints = 0;
        this.magePoints = 0;

        this.meleDamage = 6;
        this.rangedDamage = 9;
        this.mageDamage = 12;

        this.armor = 10;


        this.weapon = weapon;

        money = 9999;
        entityPos = new GameScreen.PointF(x, y);


    }



    public void addExperiance(float amout){
        experience += amout;
        if(experience >= level * 100){
            experience -= level*100;
            level ++;
            unusedPoints++;

        }
    }

    public void addMeleePoints(){
        if(unusedPoints >0){
        meleePoints++;
        meleDamage +=3;
        armor += 7;
        maxHp += 7 * hpMultiplyer;
        hp = maxHp;
        unusedPoints--;
        }
    }
    public void addMagePoints(){
        if(unusedPoints >0){
            magePoints++;
            mageDamage +=7;
            armor += 4;
            maxHp += 3 * hpMultiplyer;
            hp = maxHp;
            unusedPoints--;
        }
    }
    public void addRangedPoints(){
        if(unusedPoints >0){
            rangedPoints++;
            rangedDamage +=5;
            armor += 3;
            maxHp += 5 * hpMultiplyer;
            hp = maxHp;
            unusedPoints--;
        }
    }




    public void updatePlayerMove(double delta) {

        if (!movePlayer)
            return;

        if(!isDead()){

        speed = (float) (delta * 400);
        float ratio = Math.abs(lastTouchDiff.y) / Math.abs(lastTouchDiff.x);
        double angle = Math.atan(ratio);

        float xSpeed = (float) Math.cos(angle);
        float ySpeed = (float) Math.sin(angle);


        if (xSpeed > ySpeed) {
            if (lastTouchDiff.x > 0) setFaceDir(GameConstants.Face_Dir.RIGHT) ;
            else setFaceDir(GameConstants.Face_Dir.LEFT);
        } else {
            if (lastTouchDiff.y > 0) setFaceDir(GameConstants.Face_Dir.DOWN);
            else setFaceDir(GameConstants.Face_Dir.UP);
        }

        if (lastTouchDiff.x < 0)
            xSpeed *= -1;
        if (lastTouchDiff.y < 0)
            ySpeed *= -1;



            float deltaX = xSpeed * speed * -1;
            float deltaY = ySpeed * speed * -1;

            float xPosToCheck = gameScreen.playerX + cameraX * -1 + deltaX * -1 - GameConstants.Sprite.SIZE/2;
            float yPosToCheck = gameScreen.playerY + cameraY * -1 + deltaY * -1 - GameConstants.Sprite.SIZE/2;


           if (gameScreen.mapManager.canMoveHere(xPosToCheck , yPosToCheck)) {
            if(!gameScreen.mapManager.checkNearbyWallCollision(hitbox, hitbox.getX() + deltaX * -1, hitbox.getY() + deltaY * -1)){
                cameraX += deltaX;
                cameraY += deltaY;
                entityPos.x = GameConstants.GET_WIDTH/2-cameraX;
                entityPos.y = GameConstants.GET_HEIGHT/2- cameraY;

     }




    }
            setHitboxPosition();

        }
    }


    public void die() {
        if(deathCooldown- DEATH_COOLDOWN_TIME > 0) {
            isAlive = false;

            movePlayer = false;


            cameraX = gameScreen.playerX-respPoint.x - GameConstants.Sprite.SIZE / 2;
            cameraY = gameScreen.playerY-respPoint.y - GameConstants.Sprite.SIZE / 2;
            mana = maxMana;

            setHitboxPosition();

            hp = maxHp;
            System.out.println("died");

        }

    }

    public boolean getHit(Polygon otherhitbox){
        if(Intersector.overlapConvexPolygons(hitbox, otherhitbox) &&  System.currentTimeMillis() - lastTimeGetHit>= 1000  ){
            lastTimeGetHit = System.currentTimeMillis();
            System.out.println("hit!!");


            return true;

        }
        return false;
    }

    public void update(float delta) {
        if (isDead()) {

                deathCooldown = System.currentTimeMillis();
                die();
        }

        updatePlayerMove(delta);


    }


    private void resetAfterDeath() {
        isAlive = true;
        movePlayer = true;

    }
    public boolean isDead(){
        return hp <= 0;
    }


    public void takeDamage(int damage) {
            if(hp > 0){
                hp -= damage;
            }
            else{
                hp =0;
                die();

            }
        }




    @Override
    public void setHitboxPosition() {
        hitbox.setPosition(  entityPos.x - GameConstants.Sprite.SIZE/2+GameConstants.Sprite.SIZE/8 , entityPos.y - GameConstants.Sprite.SIZE/2);
    }

    @Override
    public void drawBar(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {


        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(GameConstants.Sprite.SIZE  ,  GameConstants.GET_HEIGHT-GameConstants.GET_HEIGHT/4, (float) GameConstants.GET_WIDTH /6, (float) GameConstants.GET_HEIGHT/5);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(GameConstants.Sprite.SIZE ,  GameConstants.GET_HEIGHT-GameConstants.GET_HEIGHT/4, GameConstants.GET_WIDTH /6 * hp / maxHp, GameConstants.GET_HEIGHT/5);



        shapeRenderer.end();
        batch.begin();

        font.getData().setScale(GameConstants.Sprite.SIZE/20);
        font.draw(batch, "HP " + hp, GameConstants.GET_WIDTH / 9-GameConstants.Sprite.SIZE, GameConstants.GET_HEIGHT - GameConstants.GET_HEIGHT / 8);
        font.draw(batch, "Money \n" + money, GameConstants.GET_WIDTH / 9- GameConstants.Sprite.SIZE , GameConstants.GET_HEIGHT - GameConstants.GET_HEIGHT / 3);
        if(weapon instanceof MagicWeapon) {
            font.draw(batch, "Manna \n" + mana, GameConstants.GET_WIDTH - GameConstants.GET_WIDTH / 5 + GameConstants.Sprite.SIZE, GameConstants.GET_HEIGHT - GameConstants.GET_HEIGHT / 8);
        }batch.end();
    }

    public void setPlayerMoveTrue(GameScreen.PointF lastTouchDiff) {
        movePlayer = true;
        this.lastTouchDiff = lastTouchDiff;
    }

    public void setPlayerMoveFalse() {
        movePlayer = false;
        resetAnimation();
    }

    public boolean getMovePlayer(){
        return  movePlayer;

    }

    public float getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int lvl) {
        this.level = lvl;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void setCurrentWeapon(Weapon weapon){

        currentWeapon =weapon;
    }
    public Weapon getCureentWeapon(){
        return  currentWeapon;
    }


    public void updateAnim(){
        if(!isDead()){

            updateAnimation();

        }
    }
    public void addMoney(int money){
        this. money += money;

    }


    public void setRespPoint(Vector2 respPoint){
        this.respPoint = respPoint;
    }

    public int getMoney(){return money;}

    public void regenerateMana(float delta) {
        manaRegenTimer += delta;
        if (manaRegenTimer >= 1) {
            mana = Math.min(mana + 3, maxMana);
            manaRegenTimer = 0;
        }
    }




}

