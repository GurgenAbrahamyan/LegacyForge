package com.gamb1t.legacyforge.Entity;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Server;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.Networking.ConnectionManager;
import com.gamb1t.legacyforge.Networking.Network;
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.Weapon;

public abstract class GameCharacters {

    protected int aniTick = 0;
    protected int currentFrame = 0;
    protected int animationFrameAmount = 4;
    protected  int aniSpeed = 10;
    protected  int FaceDirection = GameConstants.Face_Dir.DOWN;


    protected float speed;
    protected int hp, maxHp;
    protected float mana = 100, maxMana = 100;
    protected boolean isAlive = true;
    protected Vector2 entityPos;
    protected  float width;
    protected  float heigh;
    protected Server server;
    protected int id;
    protected int roomId;
    protected String roomName;

    public Polygon hitbox;

    protected Weapon weapon;
    protected Armor armour;
    protected MapManaging mapManager;

    public float cameraX, cameraY;

    protected TextureRegion[][] SpriteSheet;

    public GameCharacters(float x, float y, float width, float height, MapManaging mapManaging) {
        this.width = width;
        this.heigh = height;

        float[] vertices = {
            0, 0,
            width, 0,
            width, height,
            0, height
        };
        hitbox = new Polygon(vertices);
        hitbox.setPosition(x, y);
        this.mapManager = mapManaging;

    }
    public void setArmour(Armor armour){
        this.armour = armour;
    }


    public abstract void setHitboxPosition();

    public void setPosition(float x, float y){
        entityPos.x = x;
        entityPos.y = y;
        cameraX = GameConstants.GET_WIDTH/2 - entityPos.x ;
        cameraY = GameConstants.GET_HEIGHT/2 - entityPos.y;
        setHitboxPosition();
    }

    public void applyKnockback(Enemy enemy, Weapon weapon) {
        float knockbackForce = weapon.getKnockbackInTiles() * GameConstants.Sprite.SIZE;

        Vector2 knockbackDirection = new Vector2(enemy.getEntityPos().x - getEntityPos().x,
            enemy.getEntityPos().y - getEntityPos().y).nor();

        Vector2 knockbacks = knockbackDirection.scl(knockbackForce);

        enemy.setPosition(enemy.getEntityPos().x + knockbacks.x, enemy.getEntityPos().y + knockbacks.y);

    }





    public TextureRegion[][] getSpriteSheet() {
        return SpriteSheet;
    }


    public void setTexture(String recourceName){

        Texture entitiesTexture = new Texture(Gdx.files.internal(recourceName));

        SpriteSheet = new TextureRegion[entitiesTexture.getWidth()/GameConstants.Sprite.DEFAULT_SIZE][entitiesTexture.getWidth()/GameConstants.Sprite.DEFAULT_SIZE];


        SpriteSheet = TextureRegion.split(entitiesTexture, GameConstants.Sprite.DEFAULT_SIZE, GameConstants.Sprite.DEFAULT_SIZE);

        System.out.println(SpriteSheet.length +" " +SpriteSheet[0].length);
    }

    public void setTextureArray(TextureRegion[][] textureArray){
        this.SpriteSheet = textureArray;
    }

    public TextureRegion[][] getTextureArray(){
        return SpriteSheet;
    }


    public void sendHp(GameCharacters attackedEnemy){
        if(server != null){
            Network.CurrentHp dealedDamage = new Network.CurrentHp();
            dealedDamage.isEnemy = attackedEnemy instanceof Enemy;
            dealedDamage.idOfEnemy = id;
            dealedDamage.curHp = hp;
            System.out.println("sent hp!");
           ConnectionManager.sendToConnections(roomName, roomId, dealedDamage);
        }
    }




    public TextureRegion getSprite(int yPos, int xPos) {
        return SpriteSheet[yPos][xPos];
    }

    public void updateAnimation() {


        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            currentFrame++;
            if (currentFrame >= animationFrameAmount)
                currentFrame = 0;
        }
    }

    public abstract void drawBar(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font);

    public void resetAnimation() {
        aniTick = 0;
        currentFrame = 0;
    }

    public void setServer(Server server, int roomId, String name){
        this.server = server;
        this.roomId = roomId;
        this.roomName= name;
    }




    public int getAniIndex() {
        return currentFrame;
    }

    public int getFaceDir() {
        return FaceDirection;
    }

    public void setFaceDir(int faceDir) {
        this.FaceDirection = faceDir;
    }

    public Vector2 getEntityPos(){
        return entityPos;
    }

    public void addManna(float x){
        mana += x;
        onManaChange();
    }

    public void onManaChange(){
        if(server != null &&  this instanceof Player){
            Network.OnManaChange onManaChange = new Network.OnManaChange();
            onManaChange.amount = mana;
            server.sendToTCP(getID(), onManaChange);
        }
    }

    public float getManna(){
        return  mana;
    }

    public Polygon getHitbox(){
        return hitbox;
    }



    public abstract  void takeDamage(float x, GameCharacters gameCharacters);

    public float getHp(){

        return  hp;


    }
    public boolean getIsAlive(){return isAlive; }
    public void setIsAlive(boolean b){
        isAlive =b;
    }

    public int getID(){return id;}




}
