package com.gamb1t.legacyforge.Weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.esotericsoftware.kryonet.Client;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;

public class Armor {


    private String name;


    private String type;

    private String id;
    private Client client;


    private double damageMultiplier;

    private int armorPoints;


    private int hpBonus;

    private String spritePath;

    private  int level;
    private int price;

    @JsonIgnore
    private TextureRegion[][] spriteSheet;

    public Armor() {
    }


    @JsonIgnore
    public void setClient(Client client){
        this.client =client;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public int getArmorPoints() {
        return armorPoints;
    }

    public int getHpBonus() {
        return hpBonus;
    }

    public String getSpritePath() {
        return spritePath;
    }
@JsonIgnore
    public void setTextureRegion(TextureRegion[][] array){
        this.spriteSheet = array;
    }
    @JsonIgnore
    public TextureRegion[][] getTextureRegion(){
        return  spriteSheet;
    }

    public void setName(String name) {
        this.name = name;
    }
    @JsonSetter("type")
    public void setType(String type) {
        this.type = type;
    }

    public void setDamageMultiplier(double damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public void setArmorPoints(int armorPoints) {
        this.armorPoints = armorPoints;
    }

    public void setHpBonus(int hpBonus) {
        this.hpBonus = hpBonus;
    }

    public void setSpritePath(String spritePath) {
        this.spritePath = spritePath;
    }

    @JsonIgnore
    public void loadTexture() {
        if (spritePath == null) return;
        Texture entitiesTexture = new Texture(Gdx.files.internal(spritePath));

        spriteSheet = new TextureRegion[entitiesTexture.getWidth()/GameConstants.Sprite.DEFAULT_SIZE][entitiesTexture.getWidth()/GameConstants.Sprite.DEFAULT_SIZE];


        spriteSheet = TextureRegion.split(entitiesTexture, GameConstants.Sprite.DEFAULT_SIZE, GameConstants.Sprite.DEFAULT_SIZE);

        System.out.println(spriteSheet.length +" " +spriteSheet[0].length);
    }

    @JsonIgnore
    public void setLevel(int lvl){
        this.level = lvl;
    }
    public int getLevel(){
         return  level;
    }

    @JsonIgnore
    public void setFirebaseId(String s){
        this.id = s;
    }
    public String getFirebaseId(){
      return id;
    }
    @JsonSetter("price")
    public void setPrice(int i){
        this.price = i;
    }

    public int getPrice(){
        return price;
    }



    @JsonIgnore
    public TextureRegion[][] getSpriteSheet() {
        return spriteSheet;
    }

    @JsonIgnore
    public void draw(SpriteBatch batch, float x, float y, float width, float height, int faceDirection, int currentFrame) {
        if (spriteSheet == null) return;
        TextureRegion frame = spriteSheet[currentFrame][faceDirection];
        batch.draw(frame, x, y, width, height);
    }
}
