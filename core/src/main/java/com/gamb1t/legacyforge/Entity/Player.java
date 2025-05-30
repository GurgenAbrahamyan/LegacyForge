package com.gamb1t.legacyforge.Entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Client;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.Networking.Network;
import com.gamb1t.legacyforge.Networking.PlayerChangeListener;
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.Equipment;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.ArrayList;
import java.util.List;

public class Player extends GameCharacters {


    private boolean movePlayer;
    private Vector2 lastTouchDiff;
    private int DEATH_COOLDOWN_TIME = 5000;
    private boolean isClient;

    private int level;
    private float experience;
    private int money;

    private float hpMultiplyer = 10;

    private String name;
    private volatile boolean dirty = false;


    private float manaRegenTimer = 0;
    private Vector2 respPoint;

    private Equipment equipment;

    private int unusedPoints;
    private int meleePoints;
    private int rangedPoints;
    private int magePoints;

    private float meleDamage;
    private float rangedDamage;
    private float mageDamage;

    private float armor;

    private String firebaseId;
    private  int wins;
    private int loses;
    private  int rating;



    private long lastTimeGetHit = System.currentTimeMillis();
    private long deathCooldown = System.currentTimeMillis();

    private final List<PlayerChangeListener> listeners = new ArrayList<>();
    private Inventory inventory;

    private Client client;



    public Player(String name, int level, int experience, int money, float x, float y, Weapon weapon, MapManaging mapManaging) {

        super(x, y, GameConstants.Sprite.SIZE*4/5, GameConstants.Sprite.SIZE*4/5, mapManaging);


        this.name= name;

        inventory =new Inventory();

        this.level = level;
        this.experience = experience;
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

        this.money = money;
        entityPos = new Vector2(x, y);

        cameraX = GameConstants.GET_WIDTH/2 - entityPos.x ;
        cameraY = GameConstants.GET_HEIGHT/2 - entityPos.y;


    }

    public Equipment getEquipment() {
        return equipment;
    }


    public void addChangeListener(PlayerChangeListener l) {
        listeners.add(l);
    }

    private void notifyListenersMoney() {
        for (PlayerChangeListener l : listeners) {
            l.onPlayerExpAndMoneyChange(money, experience);
        }
    }
    private void notifyListenerLevel(){
        for (PlayerChangeListener l : listeners) {
            l.onPlayerLevelChange(level);
        }
    }
    private void notifyListenersItem(Object o){
        for(PlayerChangeListener l : listeners){
            l.onPlayerNewInventoryAdd(o);
        }

    }
    private void notifyListenersItemEquiped(Object o){

        for(PlayerChangeListener l : listeners){
            l.onPlayerEquip(o);
        }

    }








    public boolean getDirty(){
        return dirty;
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



    float prevX = 0, prevY = 0;

    public void noLogicMove() {

        float moveX = entityPos.x - prevX;
        float moveY = entityPos.y - prevY;

        if (Math.abs(moveX) > Math.abs(moveY)) {
            setFaceDir(moveX > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT);
        } else if (Math.abs(moveY) > 0) {
            setFaceDir(moveY > 0 ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP);
        }

        if(entityPos.x != prevX && entityPos.y != prevY){
            prevX = entityPos.x;
            prevY = entityPos.y;
            movePlayer = true;
        }
        else {
            movePlayer = false;
        }

    }

    public void addWeapon(Weapon weapon, int lvl){
        inventory.addWeapon(weapon);
        weapon.setEntity(this);
        weapon.setLevel(lvl);
        if(isClient) {
            weapon.setTexture(weapon.getSprite());
            weapon.convertTxtRegToSprite();
            if (weapon instanceof MagicWeapon) {
                ((MagicWeapon) weapon).setCurrentMap(mapManager);
                ((MagicWeapon)  weapon).initProj();
            }
            if(weapon instanceof RangedWeapon){
                ((RangedWeapon) weapon).initProj();
            }
        }

        if(client != null){
            Network.OnPlayerBoughtWeapon boughtWeapon = new Network.OnPlayerBoughtWeapon();
            boughtWeapon.weaponName = weapon.getName();
            boughtWeapon.playerId = getID();
            boughtWeapon.lvl = lvl;
            client.sendTCP(boughtWeapon);
        }

        if(server !=null){
            weapon.setServer(server, roomName, roomId);
        }

        if(!listeners.isEmpty()){

        notifyListenersItem(weapon);}

    }
    public void addInventoryWeapons(ArrayList<Weapon> weapons){
        for (Weapon w : weapons) {

            System.out.println(w.getName()+" THE NAMIE IS");
            w.setEntity(this);

            if(server !=null){
                w.setServer(server, roomName, roomId);
            }

            if(isClient){
            w.setTexture(w.getSprite());
            w.convertTxtRegToSprite();
            if(w instanceof MagicWeapon){
                ((MagicWeapon)  w).setCurrentMap(mapManager);
                ((MagicWeapon)  w).initProj();
            }

            if(w instanceof RangedWeapon){
                ((RangedWeapon)  w).initProj();
            }}
        }
        inventory.addWeaponList(weapons);
    }

    public void addInventoryArmors(ArrayList<Armor> armors){
        for (Armor w : armors) {

            System.out.println(w.getName()+" THE NAMIE IS");
            if(isClient){
                System.out.println(w.getSpritePath());
                w.loadTexture();

              }

        }
        inventory.addArmorList(armors);
    }
    public void addArmor(Armor armor, int lvl){
        inventory.addArmor(armor);
        armor.setLevel(lvl);
        if(isClient){
            armor.loadTexture();
        }
        if(!listeners.isEmpty()){
        notifyListenersItem(armor);
        }
        if(client != null){
            Network.OnPlayerBoughtArmor equipArmor = new Network.OnPlayerBoughtArmor();
            equipArmor.playerId = getID();
            equipArmor.weaponArmor = armor.getName();
            equipArmor.lvl = armor.getLevel();
            client.sendTCP(equipArmor);
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

            float xPosToCheck = GameConstants.GET_WIDTH/2 + cameraX * -1 + deltaX * -1 - GameConstants.Sprite.SIZE/2;
            float yPosToCheck = GameConstants.GET_HEIGHT/2 + cameraY * -1 + deltaY * -1 - GameConstants.Sprite.SIZE/2;


             if (mapManager.canMoveHere(xPosToCheck , yPosToCheck)) {
            if(!mapManager.checkNearbyWallCollision(hitbox, hitbox.getX() + deltaX * -1, hitbox.getY() + deltaY * -1)){
                entityPos.x -= deltaX;
                entityPos.y -= deltaY;
                setHitboxPosition();

                cameraX = -entityPos.x + GameConstants.GET_WIDTH / 2;
                cameraY = -entityPos.y + GameConstants.GET_HEIGHT / 2;

     }




    }
            setHitboxPosition();

        }
    }



    public void die() {
        if(deathCooldown- DEATH_COOLDOWN_TIME > 0) {
            isAlive = false;

            movePlayer = false;


            cameraX = GameConstants.GET_WIDTH/2-respPoint.x - GameConstants.Sprite.SIZE / 2;
            cameraY = GameConstants.GET_HEIGHT/2-respPoint.y - GameConstants.Sprite.SIZE / 2;
            entityPos.set(GameConstants.GET_WIDTH/2-cameraX, GameConstants.GET_HEIGHT/2-cameraY);

            mana = maxMana;

            setHitboxPosition();

            hp = maxHp;
            isAlive = true;

            sendHp(this);
        }

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
        return hp < 0;
    }


    @Override
    public void takeDamage(float incoming, GameCharacters source) {

        float totalArmorPoints = 0;
        if (equipment.getHelmet()   != null) totalArmorPoints += equipment.getHelmet().getArmorPoints();
        if (equipment.getChestplate()!= null) totalArmorPoints += equipment.getChestplate().getArmorPoints();

        float reduction = Math.min(0.8f, totalArmorPoints / 100f);

        float net = incoming * (1f - reduction);

        hp -= net;
        if (hp <= 0) {
            hp = 0;
            die();
        }

        sendHp(this);
    }


    private void recalcStats() {
        int baseHp = 100 + level * 10;
        int bonusHp = 0;

        if (equipment.getHelmet()    != null) bonusHp += equipment.getHelmet().getHpBonus();
        if (equipment.getChestplate() != null) bonusHp += equipment.getChestplate().getHpBonus();

        this.maxHp = baseHp + bonusHp;
        this.hp    = this.maxHp;
    }

    public void equipArmor(Armor armor) {
        if(equipment == null){
            equipment = new Equipment();
        }
        equipment.equipArmor(armor);
        recalcStats();

        if(!listeners.isEmpty()){

        notifyListenersItemEquiped(armor);}
        if(client != null){
            Network.OnPlayerEquipArmor equipArmor = new Network.OnPlayerEquipArmor();
            equipArmor.playerId = getID();
            equipArmor.armorName = armor.getName();
            client.sendTCP(equipArmor);
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
    public void setIsClient(boolean b){
        isClient =b;
    }

    public void drawBarMultiplayer(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font, float cameraX, float cameraY){

        float barWidth = GameConstants.Sprite.SIZE;
        float barHeight = (float) GameConstants.Sprite.SIZE / 4;

        float barX = entityPos.x - GameConstants.Sprite.SIZE/2;
        float barY = entityPos.y + GameConstants.Sprite.SIZE/2;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barX +cameraX, barY + cameraY, barWidth, barHeight);


        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(barX +cameraX, barY + cameraY, barWidth * ((float) hp / maxHp), barHeight);
        shapeRenderer.end();

        font.getData().setScale(GameConstants.Sprite.SIZE / 30f);
        GlyphLayout layout = new GlyphLayout(font, name);
        float textWidth = layout.width;

        batch.begin();

        float nameX = entityPos.x - textWidth / 2 + cameraX;
        float nameY = barY + GameConstants.Sprite.SIZE / 2 + cameraY+ barHeight;
        font.draw(batch, name, nameX, nameY);
        batch.end();
    }

    public void setPlayerMoveTrue(Vector2 lastTouchDiff) {
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
    public void regenerateHP(float delta){

        float hpRegenTimer =0;

        hpRegenTimer += delta;
        if (hpRegenTimer >= 1) {
            mana = Math.min(hp + 5, maxHp);
            hpRegenTimer = 0;
        }

    }

    public Inventory getInventory(){
        return inventory;
    }
    public float getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
        if(hp > maxHp){
            maxHp = hp;
        }
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int lvl) {
        this.level = lvl;
    }

    public float getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void setCurrentWeapon(Weapon weapon){

        if(inventory.contains(weapon)){
       this.weapon =weapon;

            if(client != null){
                Network.OnPlayerEquipWeapon equipWeapon = new Network.OnPlayerEquipWeapon();
                equipWeapon.playerId = getID();
                equipWeapon.weaponName = weapon.getName();
                System.out.println("SENT TO EQUIP" + weapon.getName());
                client.sendTCP(equipWeapon);
            }

            if(!listeners.isEmpty()){
                System.out.println("Listen sent");
       notifyListenersItemEquiped(weapon);}
        }
    }
    public Weapon getCurrentWeapon(){
        return  weapon;
    }

    public String getName() {
        return name;
    }

    public void updateAnim(){
        if(!isDead()){

            updateAnimation();

        }
    }
    public void addMoney(int money){
        this. money += money;


        if(!listeners.isEmpty()){

        notifyListenersMoney();}

    }




    public void addWhenKilledEnemy(int money, int experience){
        this.money += money;
        this.experience += experience;

        if(this.experience >= level * 100){
            this.experience -= level*100;
            level ++;
            unusedPoints+=3;
            if(!listeners.isEmpty()){

            notifyListenerLevel();}

        }
        if(server != null){
            Network.PlayerStatsAdded newStats = new Network.PlayerStatsAdded();
            newStats.moneyAmountAdded = money;
            newStats.expAmountAdded = experience;
            server.sendToTCP(id, newStats);
        }

        if(!listeners.isEmpty()){

        notifyListenersMoney();}

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
            onManaChange();
        }
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setMana(float amount){
        this.mana= amount;
        if(amount > maxMana){
            maxMana = amount;
        }
    }

    public void setClient(Client client){
        this.client =client;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setLoses(int loses) {
        this.loses = loses;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public int getWins() {
        return wins;
    }

    public int getLoses() {
        return loses;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
    public int getMaxHp(){
        return maxHp;
    }
}

