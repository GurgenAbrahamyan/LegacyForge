package com.gamb1t.legacyforge.Entity;

import com.badlogic.gdx.math.Rectangle;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.GameScreen;
import com.gamb1t.legacyforge.Weapons.Weapon;

public class Player extends GameCharacters {

    private boolean movePlayer;
    private GameScreen.PointF lastTouchDiff;

    private int level;
    private int experience;
    private float hpMultiplyer = 10;
    private float mana, maxMana;

    private int unusedPoints;
    private int meleePoints;
    private int rangedPoints;
    private int magePoints;

    private float meleDamage;
    private float rangedDamage;
    private float mageDamage;

    private float armor;




    public Player(float x, float y, GameScreen gameScreen, Weapon weapon) {

        super(x, y, GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE);

        this.gameScreen = gameScreen;

        this.level = 1;
        this.experience = 0;
        this.hp = 100;

        this.maxHp = 100;
        this.mana = 50;
        this.maxMana = 50;

        this.meleePoints = 0;
        this.rangedPoints = 0;
        this.magePoints = 0;

        this.meleDamage = 6;
        this.rangedDamage = 9;
        this.mageDamage = 12;

        this.armor = 10;

        this.weapon = weapon;
        entityPos = new GameScreen.PointF(x, y);


    }

    public void takeDamage(float damage) {}
    public void regenerateMana(float amout){}
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

        speed = (float) (delta * 300);
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

        int pWidth = GameConstants.Sprite.SIZE;
        int pHeight = GameConstants.Sprite.SIZE;

        if (xSpeed <= 0)
            pWidth = 0;
        if (ySpeed <= 0)
            pHeight = 0;


        float deltaX = xSpeed * speed * -1;
        float deltaY = ySpeed * speed * -1;

        if (gameScreen.mapManager.canMoveHere(gameScreen.playerX + cameraX * -1 + deltaX * -1 + pWidth, gameScreen.playerY + cameraY * -1 + deltaY * -1 + pHeight)) {
            cameraX += deltaX;
            cameraY += deltaY;
            entityPos.x = cameraX;
            entityPos.y = cameraY;
            System.out.println(cameraX + " " + cameraY);

        }

        setHitboxPosition();


    }

    public void getHit(Rectangle otherhitbox){
        if(hitbox.overlaps(otherhitbox)){
            System.out.println("Colides!");
        }
    }

    @Override
    public void setHitboxPosition() {
        hitbox.setPosition(  gameScreen.playerX - cameraX , gameScreen.playerY - cameraY);
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

    public void setHp(float hp) {
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


    public void equipWeapon(Weapon newWeapon) {
        this.weapon = newWeapon;
    }

    public void attack() {
        if (weapon != null) {
            switch (weapon.getName()){
            }
            weapon.attack();
        }
    }




}

