package com.gamb1t.legacyforge.Entity;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Server;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.Networking.ConnectionManager;
import com.gamb1t.legacyforge.Networking.Network;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.MeleeWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Enemy extends GameCharacters {

    private long lastDirChange = System.currentTimeMillis();
    private int damage = 10;
    private float playerPosX, playerPosY;
    private List<Player> players;
    private Player player;

    private Vector2 respPos;
    float distanceBtwPlayer;

    private float dirX = 0;
    private float dirY = 0;

    private float manaRegenTimer = 0;
    private Random random;

    public Enemy(List<Player> player, Vector2 currentPos, Weapon weapon, MapManaging mapManager) {
        super(0, 0, GameConstants.Sprite.SIZE * 4 / 5, GameConstants.Sprite.SIZE * 4 / 5, mapManager);

        this.mapManager = mapManager;

        random = new Random();
        entityPos = new Vector2(currentPos.x, currentPos.y);
        this.weapon = weapon;

        maxHp = 100;
        hp = maxHp;
        this.players = player;
    }

    public void setRespPos(Vector2 respPos) {
        this.respPos = respPos;
    }

    @JsonSetter("spriteSheet")
    public void setTexture(String recourceName) {
        Texture entitiesTexture = new Texture(recourceName);

        SpriteSheet = new TextureRegion[entitiesTexture.getWidth() / GameConstants.Sprite.DEFAULT_SIZE][entitiesTexture.getWidth() / GameConstants.Sprite.DEFAULT_SIZE];

        SpriteSheet = TextureRegion.split(entitiesTexture, GameConstants.Sprite.DEFAULT_SIZE, GameConstants.Sprite.DEFAULT_SIZE);
    }

    public void regenerateMana(float delta) {
        manaRegenTimer += delta;
        if (manaRegenTimer >= 1) {
            mana = Math.min(mana + 3, maxMana);
            manaRegenTimer = 0;
        }
    }

    private void updateClosestPlayer() {
        float minDistance = Float.MAX_VALUE;
        Player closest = null;

        for (Player p : players) {
            float dx = p.entityPos.x - entityPos.x;
            float dy = p.entityPos.y - entityPos.y;
            float dist = dx * dx + dy * dy;

            if (dist < minDistance) {
                minDistance = dist;
                closest = p;
            }
        }

        if (closest != null) {
            playerPosX = closest.entityPos.x - GameConstants.Sprite.SIZE / 2;
            playerPosY = closest.entityPos.y - GameConstants.Sprite.SIZE / 2;
        } else {
            playerPosX = 0;
            playerPosY = 0;
        }
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void updateMove(double delta) {
        if (isAlive) {
            updateClosestPlayer();

            regenerateMana((float) delta);

            distanceBtwPlayer = (float) Math.hypot(playerPosX - entityPos.x + GameConstants.Sprite.SIZE / 2, playerPosY - entityPos.y + GameConstants.Sprite.SIZE / 2);

            float deltaSpeed = (float) (delta * speed);
            float deltaX = 0, deltaY = 0;

            if (System.currentTimeMillis() - lastDirChange >= 3000) {
                int randFaceDir = random.nextInt(4);
                setFaceDir(randFaceDir);
                lastDirChange = System.currentTimeMillis();
            }

            dirX = playerPosX - entityPos.x + GameConstants.Sprite.SIZE / 2;
            dirY = playerPosY - entityPos.y + GameConstants.Sprite.SIZE / 2;

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
            if (!mapManager.checkNearbyWallCollision(hitbox, hitbox.getX() + deltaX, hitbox.getY() + deltaY)) {
                entityPos.x += deltaX;
                entityPos.y += deltaY;
            }
            setHitboxPosition();
        }
    }

    public void sendCoordinates(Server server) {
        ConnectionManager.sendToConnections(roomName, roomId, new Network.EnemyPos(entityPos.x / GameConstants.Sprite.SIZE, entityPos.y / GameConstants.Sprite.SIZE, id));
    }

    public void setDirection(float dirX, float dirY) {
        this.dirX = dirX;
        this.dirY = dirY;
        System.out.println(dirX);
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

        if (entityPos.x != 0 && entityPos.y != 0) {
            prevX = entityPos.x;
            prevY = entityPos.y;
        }
    }

    private long lastAttackTime = 0;
    float cooldownDuration = 0;
    float elapsedTime = -7;

    public void attackStarted() {
        if (!weapon.getAiming()) {
            weapon.setAiming(true);
            if (weapon instanceof RangedWeapon) {
                ((RangedWeapon) weapon).setIsCharging(true);
                ((RangedWeapon) weapon).setAnimOver(true);
            }
        }
    }

    public void attackDragged(float angle) {
        if (weapon.getAiming()) {
            weapon.setRotation((float) Math.toDegrees(angle));
        }
    }

    public void attackReleased(float angle) {
        if (weapon.getAiming()) {
            weapon.setRotation((float) Math.toDegrees(angle));
            if (weapon instanceof MeleeWeapon) {
                weapon.attack();
            }
            if (weapon instanceof RangedWeapon) {
                ((RangedWeapon) weapon).attackNoProj();
            }
            if (weapon instanceof MagicWeapon) {
                ((MagicWeapon) weapon).attackNoProj();
            }
            lastAttackTime = System.currentTimeMillis();
            weapon.setAttacking(true);

            if (weapon instanceof RangedWeapon) {
                ((RangedWeapon) weapon).setAnimOver(false);
                ((RangedWeapon) weapon).setIsCharging(false);
            }
            if (weapon instanceof MagicWeapon) {
                ((MagicWeapon) weapon).setAnimOver(true);
            }
            weapon.setAiming(false);
            weapon.resetAnimation();
        }
    }

    Network.AttackStartPacket attackStartPacket = null;

    public void attackPlayer() {
        if (isAlive) {
            if (distanceBtwPlayer < weapon.getRange() * GameConstants.Sprite.SIZE) {
                if (server != null && attackStartPacket == null) {
                    attackStartPacket = new Network.AttackStartPacket(id, true);
                    ConnectionManager.sendToConnections(roomName, roomId, attackStartPacket );
                }

                float dirX = playerPosX - entityPos.x + GameConstants.Sprite.SIZE / 2;
                float dirY = playerPosY - entityPos.y + GameConstants.Sprite.SIZE / 2;
                double angle = Math.atan2(dirY, dirX);

                if (!weapon.getAiming()) {
                    weapon.setAiming(true);
                    if (weapon instanceof RangedWeapon) {
                        elapsedTime = weapon.getAttackSpeed();
                    }
                }

                long currentTime = System.currentTimeMillis();

                if (weapon instanceof RangedWeapon) {
                    weapon.setRotation((float) Math.toDegrees(angle));
                    weapon.setAttacking(true);
                    ((RangedWeapon) weapon).setAnimOver(true);

                    elapsedTime -= Gdx.graphics.getDeltaTime();

                    if (server != null) {
                        ConnectionManager.sendToConnections(roomName, roomId, new Network.AttackDragged((float) angle, id, true));
                    }

                    if (elapsedTime <= 0) {
                        elapsedTime = weapon.getAttackSpeed();
                        if (weapon.getAiming()) {
                            weapon.attack();
                            if (server != null) {
                                ConnectionManager.sendToConnections(roomName, roomId, new Network.AttackReleasePacket((float) angle, id, true));
                            }
                            ((RangedWeapon) weapon).setAnimOver(true);
                            ((RangedWeapon) weapon).setIsCharging(false);
                            weapon.resetAnimation();
                            lastAttackTime = System.currentTimeMillis();
                            attackStartPacket = null;
                        }
                    }
                } else {
                    if (currentTime - lastAttackTime >= weapon.getAttackSpeed() * 1000) {
                        if (server != null) {
                            ConnectionManager.sendToConnections(roomName, roomId, new Network.AttackStartPacket(id, true));
                        }

                        weapon.setAiming(true);
                        if (server != null) {
                            ConnectionManager.sendToConnections(roomName, roomId, new Network.AttackDragged((float) angle, id, true));
                        }
                        if (weapon.getAiming()) {
                            weapon.setRotation((float) Math.toDegrees(angle));
                            weapon.attack();
                            weapon.setAttacking(true);
                            if (server != null) {
                                ConnectionManager.sendToConnections(roomName, roomId, new Network.AttackReleasePacket((float) angle, id, true));
                            }
                            if (weapon instanceof MagicWeapon) {
                                ((MagicWeapon) weapon).setAnimOver(true);
                            }
                            lastAttackTime = System.currentTimeMillis();
                        }
                        weapon.setAiming(false);
                    }
                }
            } else {
                if (weapon.getAiming()) {
                    weapon.setAiming(false);
                    attackStartPacket = null;
                    if (server != null) {
                        Network.AttackCanceled canceled = new Network.AttackCanceled();
                        canceled.enemyId = id;
                        canceled.isEnemy = true;
                        ConnectionManager.sendToConnections(roomName, roomId,canceled );
                    }
                }
            }
        }
    }

    @Override
    public void setHitboxPosition() {
        hitbox.setPosition(entityPos.x + GameConstants.Sprite.SIZE / 8 - GameConstants.Sprite.SIZE / 2, entityPos.y - GameConstants.Sprite.SIZE / 2);
    }

    @Override
    public void drawBar(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {
        if (isAlive) {
            float barWidth = GameConstants.Sprite.SIZE;
            float barHeight = (float) GameConstants.Sprite.SIZE / 4;

            float barX = entityPos.x - GameConstants.Sprite.SIZE / 2;
            float barY = entityPos.y + GameConstants.Sprite.SIZE - GameConstants.Sprite.SIZE / 2;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(barX + player.cameraX, barY + player.cameraY, barWidth, barHeight);

            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(barX + player.cameraX, barY + player.cameraY, barWidth * ((float) hp / maxHp), barHeight);

            shapeRenderer.end();
        }
    }

    public void takeDamage(float amout, GameCharacters player) {
        if (hp > 0) {
            hp -= amout;
        }
        if (hp <= 0) {
            die(player);
        }

        sendHp(this);
    }

    public void die(GameCharacters gameCharacters) {
        if (isAlive) {
            hp = maxHp;
            if (gameCharacters instanceof Player && gameCharacters != null) {
                addMoney((Player) gameCharacters);
            }

            isAlive = false;

            Network.enemyDied e = new Network.enemyDied();
            e.diedEnemyId = id;
            if (server != null) {
                ConnectionManager.sendToConnections(roomName, roomId, e);
            }

            hp = 0;
        }
    }

    public int getDamage() {
        return damage;
    }

    public Polygon getHitbox() {
        return hitbox;
    }

    public void setPlayerPosX(float x) {
        playerPosX = x;
    }

    public void setPlayerPosY(float y) {
        playerPosY = y;
    }

    public void addMoney(Player player) {
        player.addWhenKilledEnemy(hp / 10, hp / 20);
    }

    public Weapon getWeapon() {
        return weapon;
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

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public float getSpeed() {
        return speed;
    }

    public float getDirX() {
        return dirX;
    }

    public float getDirY() {
        return dirY;
    }
}
