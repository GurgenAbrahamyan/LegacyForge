package com.gamb1t.legacyforge.ManagerClasses;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Enviroments.MapManaging;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.ArrayList;
import java.util.Random;

public class GameScreen implements Screen {

    public float playerX = (float) GET_WIDTH / 2, playerY = (float) GET_HEIGHT / 2;

    private Random rand = new Random();
    private TouchManager touchEvents;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private static ArrayList<Enemy> Enemies = new ArrayList<Enemy>();

    private static WeaponLoader weaponLoader = new WeaponLoader("melee.json");

    private static ArrayList<Weapon> weapon = weaponLoader.getWeaponList();

    Player PLAYER = new Player(playerX, playerY, this, weapon.get(0));
    public MapManaging mapManager;

    public GameScreen() {
        touchEvents = new TouchManager(PLAYER, weapon.get(0));
        Gdx.input.setInputProcessor(touchEvents);

        mapManager = new MapManaging("1room.txt", "Tiles/tileset_floor.png", 30, 30);

        batch = new SpriteBatch();

        PLAYER.setTexture("player_sprites/player_spritesheet.png", 4, 7);

        for (int i = 0; i < 10; i++) {
            Enemies.add(new Enemy(this, PLAYER));
        }

        for (Enemy enemy : Enemies) {
            enemy.setTexture("enemies_spritesheet/skeleton_spritesheet.png", 4, 7);
        }

        for (Weapon w : weapon) {
            w.setAttackJoystick(touchEvents);
            w.setTexture(w.getSprite(), 1, 4);
            w.convertTxtRegToSprite();
        }
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        PLAYER.setCurrentWeapon(weapon.get(0));
    }

    public int getRandom(int x) {
        return rand.nextInt(x);
    }

    public static class PointF {
        public float x;
        public float y;

        public PointF(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public void update(float delta) {
        PLAYER.update(delta);
        mapManager.setCameraValues(PLAYER.cameraX, PLAYER.cameraY);

        if (PLAYER.getMovePlayer()) {
            PLAYER.updateAnim();
        }
        if (PLAYER.getCureentWeapon().getAttacking()) {
            PLAYER.getCureentWeapon().update();
            PLAYER.getCureentWeapon().checkHitboxCollisions(Enemies);
        }

        for (Enemy enemy : Enemies) {
            enemy.updateMove(delta);
            enemy.updateAnimation();
            enemy.setPlayerPosX(playerX - GameConstants.Sprite.SIZE/2- PLAYER.cameraX);
            enemy.setPlayerPosY(playerY - GameConstants.Sprite.SIZE/2-PLAYER.cameraY);
        }
    }

    private void resetAnimation() {
        PLAYER.resetAnimation();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        update(delta);

        for (Enemy enemy : Enemies) {
            if (PLAYER.getHit(enemy.hitbox)) {
                System.out.println("Damaged: " + enemy.getDamage());
              PLAYER.takeDamage(enemy.getDamage());
            }
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapManager.draw(batch);

        batch.begin();

        batch.draw(PLAYER.getSprite(PLAYER.getAniIndex(), PLAYER.getFaceDir()), playerX - GameConstants.Sprite.SIZE/2, playerY - GameConstants.Sprite.SIZE/2,
            GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE);

        for (Enemy enemy : Enemies) {
            batch.draw(enemy.getSprite(enemy.getAniIndex(), enemy.getFaceDir()), enemy.getEntityPos().x + PLAYER.cameraX,
                enemy.getEntityPos().y + PLAYER.cameraY, GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE);
        }

        PLAYER.getCureentWeapon().draw(batch, playerX- GameConstants.Sprite.SIZE/2, playerY - GameConstants.Sprite.SIZE/2);

        batch.end();

        for (Enemy enemy : Enemies) {
            enemy.drawBar(batch, shapeRenderer, font);
        }
        PLAYER.drawBar(batch, shapeRenderer, font);

        touchEvents.draw(batch);

        if (PLAYER.getCureentWeapon().getAttacking()) {


            Polygon hitbox = PLAYER.getCureentWeapon().createHitbox(playerX - PLAYER.cameraX, playerY - PLAYER.cameraY);





        }
    }


    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
    }
}
