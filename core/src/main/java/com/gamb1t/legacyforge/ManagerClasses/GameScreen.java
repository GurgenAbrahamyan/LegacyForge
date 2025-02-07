package com.gamb1t.legacyforge.ManagerClasses;



import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.GameCharacters;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Enviroments.MapManaging;


import java.util.Random;



public class GameScreen implements Screen {

    private float playerX = ( float) GET_WIDTH / 2, playerY = (float) GET_HEIGHT / 2;
    private float cameraX, cameraY;
    private boolean movePlayer;

    private PointF lastTouchDiff;
    private Random rand = new Random();
    private TouchManager touchEvents;
    private PointF skeletonPos;
    private SpriteBatch batch;


    Player PLAYER = new Player("player_sprites/player_spritesheet.png", 4, 7);
    Enemy SKELETON = new Enemy("enemies_spritesheet/skeleton_spritesheet.png", 4, 7);



    private int skeletonDir = GameConstants.Face_Dir.DOWN;
    private long lastDirChange = System.currentTimeMillis();
    private int playerAniIndexY, playerFaceDir = GameConstants.Face_Dir.RIGHT;
    private int skeletonAniIndexY = 0;
    private int aniTick, aniTick2;
    private int aniSpeed = 10;

    private MapManaging mapManager;

    public GameScreen() {
        touchEvents = new TouchManager(this);
        Gdx.input.setInputProcessor(touchEvents);
        mapManager = new MapManaging("1room.txt","Tiles/tileset_floor.png", 30, 30);

        skeletonPos = new PointF(rand.nextInt(GET_WIDTH), rand.nextInt(GET_HEIGHT));
        batch = new SpriteBatch();


    }

    static class PointF{
        float x, y;
        PointF(float x, float y){
            this.x = x;
            this.y = y;
        }
    }


    public void update(double delta) {
        updatePlayerMove(delta);
        mapManager.setCameraValues(cameraX, cameraY);


        if (System.currentTimeMillis() - lastDirChange >= 3000) {
            skeletonDir = rand.nextInt(4);
            lastDirChange = System.currentTimeMillis();
        }

        switch (skeletonDir) {
            case GameConstants.Face_Dir.DOWN:
                skeletonPos.y += delta * 300;
                if (skeletonPos.y >= GET_HEIGHT)
                    skeletonDir = GameConstants.Face_Dir.UP;
                break;

            case GameConstants.Face_Dir.UP:
                skeletonPos.y -= delta * 300;
                if (skeletonPos.y <= 0)
                    skeletonDir = GameConstants.Face_Dir.DOWN;
                break;

            case GameConstants.Face_Dir.RIGHT:
                skeletonPos.x += delta * 300;
                if (skeletonPos.x >= GET_WIDTH)
                    skeletonDir = GameConstants.Face_Dir.LEFT;
                break;

            case GameConstants.Face_Dir.LEFT:
                skeletonPos.x -= delta * 300;
                if (skeletonPos.x <= 0)
                    skeletonDir = GameConstants.Face_Dir.RIGHT;
                break;
        }


        updatePlayerAnimation();
        updateEntity();
    }

    private void updatePlayerMove(double delta) {
        if (!movePlayer)
            return;

        float baseSpeed = (float) (delta * 300);
        float ratio = Math.abs(lastTouchDiff.y) / Math.abs(lastTouchDiff.x);
        double angle = Math.atan(ratio);

        float xSpeed = (float) Math.cos(angle);
        float ySpeed = (float) Math.sin(angle);


        if (xSpeed > ySpeed) {
            if (lastTouchDiff.x > 0) playerFaceDir = GameConstants.Face_Dir.RIGHT;
            else playerFaceDir = GameConstants.Face_Dir.LEFT;
        } else {
            if (lastTouchDiff.y > 0) playerFaceDir = GameConstants.Face_Dir.DOWN;
            else playerFaceDir = GameConstants.Face_Dir.UP;
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


        float deltaX = xSpeed * baseSpeed * -1;
        float deltaY = ySpeed * baseSpeed * -1;

        if (mapManager.canMoveHere(playerX + cameraX * -1 + deltaX * -1 + pWidth, playerY + cameraY * -1 + deltaY * -1 + pHeight)) {
            cameraX += deltaX;
            cameraY += deltaY;
        }


    }

    private void updatePlayerAnimation() {
        if (!movePlayer)
            return;
        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            playerAniIndexY++;
            if (playerAniIndexY >= 4)
                playerAniIndexY = 0;
        }
    }
    private void updateEntity(){
        aniTick2++;
        if (aniTick2 >= aniSpeed) {
            aniTick2 = 0;
            skeletonAniIndexY++;
            if (skeletonAniIndexY >= 4)
                skeletonAniIndexY = 0;
        }
    }


    public void setPlayerMoveTrue(PointF lastTouchDiff) {
        movePlayer = true;
        this.lastTouchDiff = lastTouchDiff;
    }

    public void setPlayerMoveFalse() {
        movePlayer = false;
        resetAnimation();
    }

    private void resetAnimation() {
        aniTick = 0;
        playerAniIndexY = 0;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);



        mapManager.draw(batch);

        batch.begin();
        batch.draw(PLAYER.getSprite(playerAniIndexY, playerFaceDir), playerX, playerY, GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE);
        batch.draw(SKELETON.getSprite(skeletonAniIndexY, skeletonDir), skeletonPos.x + cameraX, skeletonPos.y + cameraY, GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE );
        batch.end();

        touchEvents.draw(batch);
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
    }
}


